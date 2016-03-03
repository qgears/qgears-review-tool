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
import hu.qgears.review.tool.WhiteListFileSet;
import hu.qgears.review.util.UtilFileFilter;
import hu.qgears.review.util.UtilSimpleString;
import hu.qgears.review.util.vct.EVersionControlTool;
import hu.qgears.review.util.vct.IVersionControlTool;
import hu.qgears.review.util.vct.VersionControlToolManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Loads a review configuration via a root configuration file.  
 * 
 * @author rizsi
 */
public class LoadConfiguration {

	private static final Logger LOG = Logger.getLogger(LoadConfiguration.class);

	/**
	 * Extension of the files storing saved reviews. 
	 */
	private static final String REVIEW_FILE_EXTENSION = ".annot";
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
		LOG.info("Current working directory : " + new File(".").getAbsolutePath());
		
		final List<Problem> problems = new ArrayList<Problem>();
		ReviewToolConfig cfg = ReviewToolConfig.load(mappingfile);
		
		final File configdir = cfg.getConfigDir();
		
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
		
		final ReviewModel model = loadReviewModel(cfg, configdir, problems);
		final ReviewInstance reviewInstance = new ReviewInstance(model, 
				new File(cfg.getReviewOutputFolder(), 
						System.currentTimeMillis() + REVIEW_FILE_EXTENSION));
		final ConfigParsingResult configParsingResult = 
				new ConfigParsingResult(reviewInstance, problems);
		
		return configParsingResult;
	}
	private ReviewModel loadReviewModel(final ReviewToolConfig cfg, 
			final File configdir, final List<Problem> problems) throws Exception {
		ReviewModel model=new ReviewModel();
		parseMappings(model, cfg);
		loadSourceFolders(model, cfg);
		loadFileSets(model, cfg);
		loadExistingReviews(model, cfg);
		loadSonarConfiguration(model,cfg);
		return model;
	}
	
	private void loadSonarConfiguration(ReviewModel model, ReviewToolConfig cfg) {
		model.setSonarBaseURL(cfg.getSonarBaseUrl());
		model.setSonarProjectId(cfg.getSonarProjectId());
	}
	
	/**
	 * Loads all the existing reviews into the review model, which can be
	 * enumerated within the review configuration directory. Side effect of this
	 * method: it sets the folder where the reviews of the current will be 
	 * saved, to 'review-$USER', where the value of the $USER property will be
	 * loaded from the config file, if the 'review_username' entry is found,
	 * or, if not, its value will be the current OS-level login name.  
	 * @param model the partial review model, to which reviews are to be added
	 * @param cfg configuration properties 
	 * @param reviewProjectConfigDir the directory containing the project-specific review
	 * configuration files and reviews. All the review files found within this
	 * directory and its subdirectories recursively, will be loaded.  
	 * @throws Exception
	 */
	private void loadExistingReviews(final ReviewModel model, 
			final ReviewToolConfig cfg) 
					throws Exception {
		final Set<String> loadedReviews = new HashSet<String>();
		
		final UtilFileVisitor reviewSearch = new UtilFileVisitor() {
			@Override
			protected boolean visited(final File file, final String localPath)
					throws Exception {
				/* Skipping hidden directories, such as SCM-info dirs. */
				if (file.isDirectory() && file.getName().startsWith(".")) {
					return false;
				}
				if (file.isFile() && file.getName().endsWith(REVIEW_FILE_EXTENSION)) {
					loadAnnotationsFile(model, file, loadedReviews);
				}
				return true;
			}
		};
		reviewSearch.visit(cfg.getReviewOutputFolder());
		for (File additionalReviewDir : cfg.getAdditionalAnnotationsFolder()){
			reviewSearch.visit(additionalReviewDir);
		}
	}
	
	private void loadAnnotationsFile(final ReviewModel model, final File annotationsFile, 
			final Set<String> loadedReviews) throws Exception {
		String file=UtilFile.loadAsString(annotationsFile);
		List<String> blocks=ussBlocks.splitAndUnescape(file);
		int index=0;
		
		LOG.info("Loading review file: " + annotationsFile.getAbsolutePath());
		
		for(String block: blocks)
		{
			try
			{
				final String trimmedReviewRaw = block.trim();
				final boolean duplicate = loadedReviews.contains(trimmedReviewRaw);
				
				if (!duplicate) {
					ReviewEntry entry=ReviewEntry.parseFromString(block);
					model.addEntry(entry);
					loadedReviews.add(trimmedReviewRaw);
				}
			}catch(Exception e)
			{
				LOG.error("error in annotation: "+annotationsFile+" "+index,e);
			}
			index++;
		}
	}
	
	private void loadFileSets(final ReviewModel model, final ReviewToolConfig cfg) throws Exception {
		
		File fileSetsDir = cfg.getFileSetsDir();
		if (fileSetsDir.isDirectory()) {
			final File[] fileSetDefSubdirs=fileSetsDir.listFiles();
			/* 
			 * fileSetDefSubdirs will never be null according to the above
			 * branch conditions and javadocs of listFiles().
			 */
			if (fileSetDefSubdirs.length == 0) {
				LOG.error("No subdirectories, "
						+ "containing file set definition descriptors, are"
						+ "found within the " + fileSetsDir + " directory."
						+ "Create one or more subdirectory within the "
						+ fileSetsDir + " with one file named "
						+ "'filesetdefinition' in each subdirectory.");
			} else {
				for (final File fileSetSubdir : fileSetDefSubdirs) {
					if (!scmSubdirFilter.isFilteredOut(fileSetSubdir) 
							&& fileSetSubdir.isDirectory()) {
						LOG.info("Loading fileset from: " + 
							fileSetSubdir.getName());
						try {
							parseFilesetDefinition(model, fileSetSubdir);
							LOG.info("Loading fileset: " + 
									fileSetSubdir.getName() + " ready");
						} catch (Exception e){
							LOG.warn("Cannot load fileset : "+fileSetSubdir,e);
						}
						
					}
				}
			}
		}
	}
	
	private void loadSourceFolders(ReviewModel model, ReviewToolConfig cfg) throws Exception {
		for(String line : cfg.getSourceFolders())
		{
			EVersionControlTool tool = getVersionControlTool (line);
			if(tool == null) {
				LOG.error("Unknown source fodler type: "+line);
			} else {
				String id = tool.getSourceFolder(line);
				String folder=model.mappings.get(id);
				LOG.info("Loading svn: "+id);
				List<ReviewSource> files = null;
				IVersionControlTool loader = VersionControlToolManager.getInstance().getImplementationFor(tool);
				File targetFolder = new File(folder);
				if (SourceCache.isCacheEnabled()){
					try {
					SourceCache cache = new SourceCache(id);
						if (cache.exists()){
							LOG.info("Loading SVN content from cache!");
							files = cache.load();
						} else {
							LOG.info("Creating cache file...");
							files = loader.loadSources(id, targetFolder);
							cache.save(files);
							LOG.info("Cache file is ready to use in next startup.");
						}
					} catch (Exception e) {
						LOG.error("Exception during loading cache for SVN repo "+id,e);
					}
				} 
				if (files == null){
					files = loader.loadSources(id, targetFolder);
				}
				model.addSourceFiles(files);
			}
		}
	}

	private EVersionControlTool getVersionControlTool(String line) {
		for (EVersionControlTool t : EVersionControlTool.values()){
			if (t.supports(line)){
				return t;
			}
		}
		return null;
	}
	/**
	 * Parses SCM roots.
	 * @param model
	 * @param cfg
	 * @param problems
	 */
	private void parseMappings(final ReviewModel model, final ReviewToolConfig cfg) {
		model.mappings.putAll(cfg.getMappings());
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
			final File fileSetDefSubdir) 
					throws Exception {
		final String fileSetId = fileSetDefSubdir.getName();
		final File fileSetDefFile = new File(fileSetDefSubdir, "filesetdefinition");
		
		LOG.info("Loading fileset definition: " + fileSetDefFile);
		
		final String fileSetDefinition = UtilFile.loadAsString(fileSetDefFile);
		final List<String> fileSetDefLines = 
				UtilString.split(fileSetDefinition, "\r\n");
		
		if (!fileSetDefLines.isEmpty()){
			final String fileSetDefType = fileSetDefLines.get(0);
			final List<String> ret;
			
			if (PomFileSet.POMFILESET_ID.equals(fileSetDefType)) {
				ret = parsePomFileSet(model, fileSetDefFile, fileSetDefLines);
			} else if (WhiteListFileSet.ID.equals(fileSetDefType)) {
				ret = new WhiteListFileSet(fileSetDefLines).reduce(
						toSourceFileList(model.getSources()));
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
			final File pomFileSetDefFile, final List<String> fileSetDefLines) throws Exception {
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
			ret = new PomFileSet().filter(params, pomFileSetDefFile, pomXmlFile);
			
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
