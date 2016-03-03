package hu.qgears.review.action;

import hu.qgears.review.model.ReviewModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * The Java DTO representation of the review tool mapping configuration
 * (*.mapping file). Use {@link #load(File)} to build DTO from property file.
 * 
 * @author agostoni
 *
 */
public class ReviewToolConfig {

	private static final String P_ANNOTATIONSFOLDER = "annotationsfolder.";
	private static final String P_REVIEW_OUTPUTFOLDER_NAME = "review_outputfolder_name";
	private static final String P_MAP = "map.";
	private static final String P_CONFIG = "config";
	/**
	 * The property name in mappings file that contains {@link ReviewModel#getSonarProjectId()}.
	 */
	private static final String P_SONAR_PROJECT = "sonar_project";
	/**
	 * The property name in mappings file that contains {@link ReviewModel#getSonarProjectId()}.
	 */
	private static final String P_SONAR_URL = "sonar_url";
	/**
	 * Path relative to config dir, where source folder mappings can be found.
	 */
	private static final String SOURCE_FOLDERS = "sourcefolders";
	/**
	 * Path relative to config dir, where file sets are defined
	 */
	private static final String FILESETS = "filesets";
	private static final Logger LOG = Logger.getLogger(ReviewToolConfig.class);

	
	public static ReviewToolConfig load(File mappingfile) throws IOException {
		LOG.info("Loading configuration from mapping file: "+mappingfile);
		Properties props = new Properties();
		FileInputStream fis = new FileInputStream(mappingfile);
		try {
			props.load(fis);
		} finally {
			fis.close();
		}
		ReviewToolConfig rtc = new ReviewToolConfig();
		rtc.loadFromPropertiesFile(props);
		return rtc;
	}

	private File configDir;
	private Map<String,String> mappings = new HashMap<String, String>();
	private List<String> sourceFolders;
	private File fileSetsDir;
	private File reviewOutputFolder;
	private List<File> additionalAnnotationsFolder;
	private String sonarBaseUrl;
	private String sonarProjectId;
	
	private void loadFromPropertiesFile(Properties props) throws IOException {
		String configDirEntry = props.getProperty(P_CONFIG);
		loadConfigDir(configDirEntry);
		loadMAppings(props);
		loadReviewOutputFolderName(props);
		loadAnnotationFolderProperties(props);
		loadSonarConfiguration(props);
		parseSourceFolders();
		parseFileSets();
	}

	private void loadAnnotationFolderProperties(Properties props) {
		additionalAnnotationsFolder = new ArrayList<File>();
		int i = 1;
		String additionalReviewDirName = props.getProperty(P_ANNOTATIONSFOLDER + i);
		while (additionalReviewDirName != null) {
			File annotFolder = new File(configDir,additionalReviewDirName);
			LOG.info("Finding additional annotation folder: "+annotFolder);
			additionalAnnotationsFolder.add(annotFolder);
			i++;
			additionalReviewDirName = props.getProperty(P_ANNOTATIONSFOLDER+ i);
		}
	}

	private void loadReviewOutputFolderName(Properties props) {
		String reviewOutputFolderNameProp = 
				props.getProperty(P_REVIEW_OUTPUTFOLDER_NAME);
		if (reviewOutputFolderNameProp == null || reviewOutputFolderNameProp.isEmpty()){
			reviewOutputFolderNameProp = "review-" + System.getProperty("user.name");
		}
		reviewOutputFolder = new File (configDir,reviewOutputFolderNameProp);
		LOG.info("Directory into which reviews will be saved: " + reviewOutputFolder);
	}

	private void parseFileSets() {
		this.fileSetsDir = new File(configDir, FILESETS);
		if (!fileSetsDir.exists() || !fileSetsDir.isDirectory()) {
			LOG.error("A subdirectory called " + "'" + FILESETS
					+ "', containing the definition of file sets to "
					+ "be reviewed, does not exist within the configuration "
					+ "directory: " + configDir);
		}
	}

	private void loadMAppings(Properties props) {
		for (Map.Entry<Object, Object> e:props.entrySet()) {
			String key=""+e.getKey();
			if(key.startsWith(P_MAP)) {
				mappings.put(key.substring(P_MAP.length()), ""+e.getValue());
			}
		}
		if (mappings.isEmpty()) {
			LOG.error("No SCM working directories "
					+ "are specified in the root review configuration file."
					+ "Specify at least one SCM working directory in the "
					+ "config file by adding at least one "
					+ "'map.workingdir_id=workingdir_path' entry.");
		}
	}

	private void loadConfigDir(String configDirEntry) {
		if (configDirEntry == null) {
			LOG.error("The mandatory 'config=...' entry is missing from the mapping file!");
		} else {
			this.configDir = new File(configDirEntry);
		}
	}
	
	private void parseSourceFolders() throws IOException {
		sourceFolders = new ArrayList<String>();
		File srcFoldersDescriptor = new File (configDir,SOURCE_FOLDERS);
		if (configDir.exists()){
			BufferedReader bfr = new BufferedReader(new InputStreamReader(new FileInputStream(srcFoldersDescriptor),"UTF-8"));
			try {
				while (bfr.ready()){
					sourceFolders.add(bfr.readLine());
				}
			} finally {
				bfr.close();
			}
		}
		if (sourceFolders.isEmpty()){
			LOG.warn("Warning no source folders has been found. Please fill the file"+ srcFoldersDescriptor+" correctly");
		}
	}

	private void loadSonarConfiguration(Properties props) {
		this.sonarBaseUrl = props.getProperty(P_SONAR_URL);
		this.sonarProjectId = props.getProperty(P_SONAR_PROJECT);
		if (sonarBaseUrl == null || sonarBaseUrl.isEmpty()){
			LOG.warn("Warnign missing SONAR base URL, statistics will not be available in report. Set "+P_SONAR_URL+ " in mappings file");
		}
		if (sonarProjectId == null || sonarProjectId.isEmpty()){
			LOG.warn("Warnign missing SONAR project id, statistics will not be available in report. Set "+P_SONAR_PROJECT+ " in mappings file");
		}
	}
	public File getConfigDir() {
		return configDir;
	}

	public Map<String,String> getMappings() {
		return mappings;
	}

	public List<String> getSourceFolders() {
		return this.sourceFolders;
	}

	public File getFileSetsDir() {
		return fileSetsDir;
	}
	
	public File getReviewOutputFolder() {
		return reviewOutputFolder;
	}
	
	public String getSonarBaseUrl() {
		return sonarBaseUrl;
	}
	
	public String getSonarProjectId() {
		return sonarProjectId;
	}
	
	public List<File> getAdditionalAnnotationsFolder() {
		return additionalAnnotationsFolder;
	}
}
