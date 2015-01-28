package hu.qgears.review.action;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilFileVisitor;
import hu.qgears.commons.UtilString;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.model.ReviewModel;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.tool.ConfigParsingResult;
import hu.qgears.review.tool.ConfigParsingResult.Problem;
import hu.qgears.review.tool.ConfigParsingResult.Problem.Type;
import hu.qgears.review.tool.PomFileSet;
import hu.qgears.review.tool.PomFileSet.Params;
import hu.qgears.review.tool.SvnStatus;
import hu.qgears.review.tool.WhiteListFileSet;
import hu.qgears.review.util.UtilFileFilter;
import hu.qgears.review.util.UtilSimpleString;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Loads a review configuration via a root configuration file.  
 * 
 * @author rizsi
 */
public class LoadConfiguration {
	private static final Logger logger = Logger.getLogger(LoadConfiguration.class.getName());
	/**
	 * Extension of the files storing saved reviews. 
	 */
	private static final String REVIEW_FILE_EXTENSION = ".annot";
	/**
	 * The property name in mappings file that contains {@link ReviewModel#getSonarProjectId()}.
	 */
	private static final String PROPERTY_SONAR_PROJECT = "sonar_project";
	/**
	 * The property name in mappings file that contains {@link ReviewModel#getSonarProjectId()}.
	 */
	private static final String PROPERTY_SONAR_URL = "sonar_url";
	private String reviewOutputFolderName;
	/**
	 * This is the separator and escape sequence in the annotation entries file that separates the blocks.
	 */
	public static final UtilSimpleString ussBlocks=new UtilSimpleString("#####\n", "%%", "NB");
	/**
	 * This is the separator and escape sequence in the annotation entry that separates the properties.
	 */
	public static final UtilSimpleString ussProperties=new UtilSimpleString("\n", "\\", "n");
	
	public static final UtilFileFilter scmSubdirFilter=new UtilFileFilter();
	
	/**
	 * 
	 * @param mappingfile the root configuration file of a review project
	 * @return
	 * @throws Exception
	 */
	public ConfigParsingResult loadConfiguration(final File mappingfile) 
			throws Exception {
		final List<Problem> problems = new ArrayList<Problem>();
		Properties props = new Properties();
		FileInputStream fis = new FileInputStream(mappingfile);
		
		try {
			props.load(fis);
		} finally {
			fis.close();
		}
		
		logger.info("Current working directory : " + new File(".").getAbsolutePath());
		
		final Object configDirEntry = props.get("config");
		
		if (configDirEntry == null) {
			problems.add(new Problem(Type.ERROR, "The mandatory 'config=...' " +
					"entry is missing from the root configuration file: " +
					mappingfile));
		}
		
		final File configdir = new File(""+configDirEntry);
		
		if (!configdir.exists() || !configdir.isDirectory()) {
			final String problemDetail;
			
			if (!configdir.exists()) {
				problemDetail = "The referred directory does not exist.";
			} else {
				problemDetail = configdir.getPath() + " is not a directory";
			}
			
			problems.add(new Problem(Type.ERROR, "The 'config' entry in the" +
					"configuration file " + mappingfile + " must refer to an" +
							"existing directory." + configdir, problemDetail));
		}
		
		final ReviewModel model = loadReviewModel(props, configdir, problems);
		final ReviewInstance reviewInstance = new ReviewInstance(model, 
				new File(configdir, reviewOutputFolderName + "/" + 
						System.currentTimeMillis() + REVIEW_FILE_EXTENSION));
		final ConfigParsingResult configParsingResult = 
				new ConfigParsingResult(reviewInstance, problems);
		
		return configParsingResult;
	}
	private ReviewModel loadReviewModel(final Properties props, 
			final File configdir, final List<Problem> problems) throws Exception {
		ReviewModel model=new ReviewModel();
		parseMappings(model, props, problems);
		loadSourceFolders(model, props, configdir);
		loadFileSets(model, props, configdir, problems);
		loadExistingReviews(model, props, configdir);
		loadSonarConfiguration(model,props);
		return model;
	}
	
	private void loadSonarConfiguration(ReviewModel model, Properties props) {
		model.setSonarBaseURL(props.getProperty(PROPERTY_SONAR_URL));
		model.setSonarProjectId(props.getProperty(PROPERTY_SONAR_PROJECT));
	}
	
	private String getReviewOutputFolder(final Properties rootConfigProps) {
		final String reviewOutputFolderNameProp = 
				rootConfigProps.getProperty("review_outputfolder_name");
		final String reviewOutputFolderName = reviewOutputFolderNameProp == null 
				|| reviewOutputFolderNameProp.length() == 0 ?
						"review-" + System.getProperty("user.name")
						: reviewOutputFolderNameProp;
						
		return reviewOutputFolderName;
	}
	
	/**
	 * Loads all the existing reviews into the review model, which can be
	 * enumerated within the review configuration directory. Side effect of this
	 * method: it sets the folder where the reviews of the current will be 
	 * saved, to 'review-$USER', where the value of the $USER property will be
	 * loaded from the config file, if the 'review_username' entry is found,
	 * or, if not, its value will be the current OS-level login name.  
	 * @param model the partial review model, to which reviews are to be added
	 * @param rootConfigProps configuration properties 
	 * @param reviewProjectConfigDir the directory containing the project-specific review
	 * configuration files and reviews. All the review files found within this
	 * directory and its subdirectories recursively, will be loaded.  
	 * @throws Exception
	 */
	private void loadExistingReviews(final ReviewModel model, 
			final Properties rootConfigProps, final File reviewProjectConfigDir) 
					throws Exception {
		this.reviewOutputFolderName = getReviewOutputFolder(rootConfigProps);
		
		logger.fine("Directory into which reviews will be saved: " + reviewOutputFolderName);
		
		final UtilFileVisitor reviewSearch = new UtilFileVisitor() {
			@Override
			protected boolean visited(final File file, final String localPath)
					throws Exception {
				/* Skipping hidden directories, such as SCM-info dirs. */
				if (file.isDirectory() && file.getName().startsWith(".")) {
					return false;
				}
				if (file.isFile() && file.getName().endsWith(REVIEW_FILE_EXTENSION)) {
					loadAnnotationsFile(model, rootConfigProps, file);
				}
				return true;
			}
		};
		
		reviewSearch.visit(new File(reviewProjectConfigDir, reviewOutputFolderName));
		
		/* 
		 * Sirectory of additional directories, from which to load reviews.
		 */
		String additionalReviewDirName;
		int i = 1;
		
		while ((additionalReviewDirName = rootConfigProps.getProperty("annotationsfolder." + i)) != null) {
			final File additionalReviewDir = new File(reviewProjectConfigDir,
					additionalReviewDirName);
			
			reviewSearch.visit(additionalReviewDir);
			i++;
		}
	}
	
	private void loadAnnotationsFile(ReviewModel model,
			Properties props, File annotationsFile) throws Exception {
		String file=UtilFile.loadAsString(annotationsFile);
		List<String> blocks=ussBlocks.splitAndUnescape(file);
		int index=0;
		for(String block: blocks)
		{
			try
			{
				ReviewEntry entry=ReviewEntry.parseFromString(block);
				model.addEntry(entry);
			}catch(Exception e)
			{
				System.err.println("error in annotation: "+annotationsFile+" "+index);
				e.printStackTrace();
			}
			index++;
		}
	}
	
	private void loadFileSets(final ReviewModel model, final Properties props,
			final File configdir, final List<Problem> problems) throws Exception {
		final File fileSetsDir = new File(configdir, "filesets");
		
		if (!fileSetsDir.exists() || !fileSetsDir.isDirectory()) {
			problems.add(new Problem(Type.ERROR, "A subdirectory called " +
					"'filesets', containing the definition of file sets to " +
					"be reviewed, does not exist within the configuration " +
					"directory: " + configdir));
		} else {
			final File[] fileSetDefSubdirs=fileSetsDir.listFiles();
			
			/* 
			 * fileSetDefSubdirs will never be null according to the above
			 * branch conditions and javadocs of listFiles().
			 */
			if (fileSetDefSubdirs.length == 0) {
				problems.add(new Problem(Type.ERROR, "No subdirectories, " +
						"containing file set definition descriptors, are" +
						"found within the " + fileSetsDir + " directory.",
						"Create one or more subdirectory within the " +
						fileSetsDir + " with one file named " +
						"'filesetdefinition' in each subdirectory."));
			} else {
				for (final File fileSetSubdir : fileSetDefSubdirs) {
					if (!scmSubdirFilter.isFilteredOut(fileSetSubdir) 
							&& fileSetSubdir.isDirectory()) {
						logger.info("Loading fileset from: " + 
							fileSetSubdir.getName());
						
						parseFilesetDefinition(model, fileSetSubdir, problems);
						
						logger.info("Loading fileset: " + 
							fileSetSubdir.getName() + " ready");
					}
				}
			}
		}
	}
	
	
	private void loadSourceFolders(ReviewModel model, Properties props,
			File configdir) throws Exception {
		for(String line: UtilString.split(UtilFile.loadAsString(new File(configdir, "sourcefolders")),"\r\n"))
		{
			if(line.startsWith("svn "))
			{
				String id=line.substring(4);
				String folder=model.mappings.get(id);
				System.out.println("Loading svn: "+id);
				List<ReviewSource> files = null;
				if (SourceCache.isCacheEnabled()){
					try {
					SourceCache cache = new SourceCache(id);
						if (cache.exists()){
							System.out.println("Loading SVN content from cache!");
							files = cache.load();
						} else {
							System.out.println("Creating cache file...");
							files = new SvnStatus().execute(id, new File(folder));
							cache.save(files);
							System.out.println("Cache file is ready to use in next startup.");
						}
					} catch (Exception e) {
						System.err.println("Exception during loading cache for SVN repo "+id);
						e.printStackTrace();
					}
				} 
				if (files == null){
					files = new SvnStatus().execute(id, new File(folder));
				}
				model.addSourceFiles(files);
				System.out.println("Creating md5sum: "+id);
				int n=0;
				System.out.println("Loading svn: "+id+" ready number of md5sums: "+n);
			}
		}
	}

	/**
	 * Parses SCM roots.
	 * @param model
	 * @param props
	 * @param problems
	 */
	private void parseMappings(final ReviewModel model, final Properties props, 
			final List<Problem> problems) {
		for (Map.Entry<Object, Object> e:props.entrySet()) {
			String key=""+e.getKey();
			if(key.startsWith("map.")) {
				model.mappings.put(key.substring(4), ""+e.getValue());
			}
		}
		
		if (model.mappings.isEmpty()) {
			problems.add(new Problem(Type.ERROR, "No SCM working directories " +
					"are specified in the root review configuration file.",
					"Specify at least one SCM working directory in the " +
					"config file by adding at least one " +
					"'map.workingdir_id=workingdir_path' entry."));
		}
	}

	/**
	 * Finds source files from svn model, that belong to specified file set
	 * definition.
	 * <p>
	 * Multiple fileset definitions are supported.
	 * 
	 * @param model The {@link ReviewModel} containing sources
	 * @param fileSetDefSubdir The {@link File} that contains the file set definition
	 * @throws Exception
	 */
	private void parseFilesetDefinition(final ReviewModel model, 
			final File fileSetDefSubdir, final List<Problem> problems) 
					throws Exception {
		final String fileSetId = fileSetDefSubdir.getName();
		final File fileSetDefFile = new File(fileSetDefSubdir, "filesetdefinition");
		
		logger.fine("Loading fileset definition: " + fileSetDefFile);
		
		final String fileSetDefinition = UtilFile.loadAsString(fileSetDefFile);
		final List<String> fileSetDefLines = 
				UtilString.split(fileSetDefinition, "\r\n");
		
		if (!fileSetDefLines.isEmpty()){
			final String fileSetDefType = fileSetDefLines.get(0);
			final List<String> ret;
			
			if (PomFileSet.POMFILESET_ID.equals(fileSetDefType)) {
				ret = parsePomFileSet(model, fileSetDefFile, fileSetDefLines, 
						problems);
			} else if (WhiteListFileSet.ID.equals(fileSetDefType)) {
				ret = new WhiteListFileSet(fileSetDefLines).reduce(
						toSourceFileList(model.getSources()), problems);
			} else {
				throw new RuntimeException("Unexpected fileset type '" + 
						fileSetDefType + "' in " + fileSetDefFile + "; must " +
								"be either " + PomFileSet.POMFILESET_ID +
								" or " + WhiteListFileSet.ID);
			}
			
			if (ret != null) {
				model.addSourceSet(fileSetId, ret);
			}
		} else {
			throw new RuntimeException("Empty fileset definition descriptor " +
					"file: " + fileSetDefFile.getAbsolutePath());
		}
	}
	
	/**
	 * Parses file set from a pomFileBytes.xml describing a SONAR analisys job. Use
	 * module definitions as whitelist of required components, and use
	 * sonar.exclusion property as blacklist.
	 * 
	 * @param model
	 * @param pomFileSetDefFile name of the processed POM file set definition
	 * file, only used in error messages
	 * @param fileSetDefLines contents of the POM file set definition
	 * @return
	 * @throws Exception
	 * 
	 * @see PomFileSet
	 */
	protected List<String> parsePomFileSet(final ReviewModel model, 
			final File pomFileSetDefFile, final List<String> fileSetDefLines, 
			final List<Problem> problems) throws Exception {
		List<String> ret;
		final Params params = new Params();
		params.params = new HashMap<String, String>();
		
		/* 
		 * The path of the POM xml file is strictly the second line in the file
		 * set definition.
		 */
		if (fileSetDefLines.size() >= 2) {
			final String pomXmlFilePath = fileSetDefLines.get(1);
			final File pomXmlFile = model.getFile(pomXmlFilePath);
			params.pomFileBytes = UtilFile.loadFile(pomXmlFile);
			
			/* 
			 * Parsing parameters, if any, that will be substituted into the
			 * POM xml.
			 */
			for (int i = 2; i < fileSetDefLines.size(); ++i) {
				final String pomParamLine = fileSetDefLines.get(i);
				final List<String> parts = UtilString.split(pomParamLine, "=");
				
				if (parts.size() == 2) {
					params.params.put(parts.get(0), parts.get(1));
				}
			}
			
			final List<String> strings=toSourceFileList(model.getSources());
			
			params.fileset=strings;
			ret = new PomFileSet().filter(params, pomFileSetDefFile, pomXmlFile,
					problems);
			
			return ret;
		} else {
			throw new IllegalArgumentException("The POM file set definition " +
					pomFileSetDefFile + " must contain at least two lines.");
		}
	}
	
	
	private List<String> toSourceFileList(List<ReviewSource> sources) {
		List<String> ret=new ArrayList<String>(sources.size());
		for(ReviewSource s: sources)
		{
			ret.add(s.toString());
		}
		return ret;
	}
}
