package hu.qgears.review.action;

import hu.qgears.commons.UtilFile;
import hu.qgears.commons.UtilFileVisitor;
import hu.qgears.commons.UtilString;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.model.ReviewModel;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.tool.PomFileSet;
import hu.qgears.review.tool.PomFileSet.Params;
import hu.qgears.review.tool.SvnStatus;
import hu.qgears.review.tool.WhiteListFileSet;
import hu.qgears.review.util.UtilFileFilter;
import hu.qgears.review.util.UtilSimpleString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class LoadConfiguration {
	/**
	 * The property name in mappings file that contains {@link ReviewModel#getSonarProjectId()}.
	 */
	private static final String PROPERTY_SONAR_PROJECT = "sonar_project";
	/**
	 * The property name in mappings file that contains {@link ReviewModel#getSonarProjectId()}.
	 */
	private static final String PROPERTY_SONAR_URL = "sonar_url";
	private String outputfoldername;
	/**
	 * This is the separator and escape sequence in the annotation entries file that separates the blocks.
	 */
	public static final UtilSimpleString ussBlocks=new UtilSimpleString("#####\n", "%%", "NB");
	/**
	 * This is the separator and escape sequence in the annotation entry that separates the properties.
	 */
	public static final UtilSimpleString ussProperties=new UtilSimpleString("\n", "\\", "n");
	
	public static final UtilFileFilter fileFilter=new UtilFileFilter();
	
	public ReviewInstance loadConfiguration(File mappingfile) throws Exception
	{
		Properties props=new Properties();
		FileInputStream fis=new FileInputStream(mappingfile);
		try
		{
			props.load(fis);
		}finally
		{
			fis.close();
		}
		System.out.println("Current working directory : "+new File(".").getAbsolutePath());
		File configdir=new File(""+props.get("config"));
		ReviewModel model=loadReviewModel(props, configdir);
		return new ReviewInstance(model, new File(configdir, outputfoldername+"/"+System.currentTimeMillis()+".annot"));
	}
	private ReviewModel loadReviewModel(Properties props, File configdir) throws Exception {
		ReviewModel model=new ReviewModel();
		parseMappings(model, props);
		loadSourceFolders(model, props, configdir);
		loadFileSets(model, props, configdir);
		loadAnnotations(model, props, configdir);
		loadSonarConfiguration(model,props);
		return model;
	}
	
	private void loadSonarConfiguration(ReviewModel model, Properties props) {
		model.setSonarBaseURL(props.getProperty(PROPERTY_SONAR_URL));
		model.setSonarProjectId(props.getProperty(PROPERTY_SONAR_PROJECT));
	}
	
	private void loadAnnotations(final ReviewModel model, final Properties props,
			File configdir) throws Exception {
		List<String> folders=new ArrayList<String>();
		for(int i=1;true;++i)
		{
			Object o=props.get("annotationsfolder."+i);
			if(o!=null)
			{
				String foldername=""+o;
				if(i==1)
				{
					outputfoldername=foldername;
				}
				folders.add(foldername);
			}else
			{
				break;
			}
		}
		System.out.println("review folders: "+folders);
		for(String s:folders)
		{
			File annotationsFolder=new File(configdir, s);
			new UtilFileVisitor()
			{
				@Override
				protected boolean visited(File dir, String localPath)
						throws Exception {
					if(dir.isDirectory() && dir.getName().startsWith("."))
					{
						return false;
					}
					if(dir.isFile())
					{
						loadAnnotationsFile(model, props, dir);
					}
					return true;
				}
			}
			.visit(annotationsFolder);
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
	private void loadFileSets(ReviewModel model, Properties props,
			File configdir) throws Exception {
		File[] sets=new File(configdir, "filesets").listFiles();
		if(sets!=null)
		{
			for(File f:sets)
			{
				if(!fileFilter.isFilteredOut(f)&& f.isDirectory())
				{
					System.out.println("Loading fileset: "+f.getName());
					loadFileset(model, f);
					System.out.println("Loading fileset: "+f.getName()+" ready");
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

	private void parseMappings(ReviewModel model, Properties props) {
		for(Map.Entry<Object, Object> e:props.entrySet())
		{
			String key=""+e.getKey();
			if(key.startsWith("map."))
			{
				model.mappings.put(key.substring(4), ""+e.getValue());
			}
		}
	}

	/**
	 * Finds source files from svn model, that belong to specified file set
	 * definition.
	 * <p>
	 * Multiple fileset definitions are supported.
	 * 
	 * @param model The {@link ReviewModel} containing sources
	 * @param f The {@link File} that contains the file set definition
	 * @throws Exception
	 */
	private void loadFileset(ReviewModel model, File f) throws Exception {
		String id=f.getName();
		File g=new File(f, "filesetdefinition");
		System.out.println("Loading fileset definition: "+g);
		String def=UtilFile.loadAsString(g);
		List<String> lines=UtilString.split(def, "\r\n");
		if (!lines.isEmpty()){
			List<String> ret;
			if(lines.get(0).equals("pomfileset"))
			{
				ret = parsePomFileSet(model, lines);
			} else if (lines.get(0).equals(WhiteListFileSet.ID)){
				ret = new WhiteListFileSet(lines).reduce(toStringList(model.getSources()));
			} else {
				throw new RuntimeException("unknown fileset type - not pomfileset");
			}
			model.addSourceSet(id, ret);
		} else {
			throw new RuntimeException("Empty fileset file! "+g.getAbsolutePath());
		}
	}
	
	/**
	 * Parses file set from a pom.xml describing a SONAR analisys job. Use
	 * module definitions as whitelist of required components, and use
	 * sonar.exclusion property as blacklist.
	 * 
	 * @param model
	 * @param lines
	 * @return
	 * @throws IOException
	 * @throws Exception
	 * 
	 * @see PomFileSet
	 */
	protected List<String> parsePomFileSet(ReviewModel model, List<String> lines)
			throws IOException, Exception {
		List<String> ret;
		Params params=new Params();
		params.params=new HashMap<String, String>();
		String pomfile=lines.get(1);
		params.pom=UtilFile.loadFile(model.getFile(pomfile));
		for(int i=2;i<lines.size();++i)
		{
			String line=lines.get(i);
			List<String> parts=UtilString.split(line, "=");
			if(parts.size()==2)
			{
				params.params.put(parts.get(0), parts.get(1));
			}
		}
		List<String> strings=toStringList(model.getSources());
		params.fileset=strings;
		ret =new PomFileSet().reduce(params);
		return ret;
	}
	private List<String> toStringList(List<ReviewSource> sources) {
		List<String> ret=new ArrayList<String>(sources.size());
		for(ReviewSource s: sources)
		{
			ret.add(s.toString());
		}
		return ret;
	}
}
