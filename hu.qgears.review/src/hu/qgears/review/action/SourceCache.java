package hu.qgears.review.action;

import hu.qgears.review.model.ReviewSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Utility for caching {@link ReviewSource}s read from SVN working copies. Can
 * be used to decrease loading time of the review tool. Useful when server must be
 * restarted frequently, e.g. during testing server codes.
 * <p>
 * Should not be used in released application. Feature can be enabled / disabled
 * using system property {@value #SYS_PROP_CACHE_ENABLED}.
 * 
 * @author agostoni
 * 
 */
public class SourceCache {

	/**
	 * System property for enabling cache on SVN load.
	 */
	private static final String SYS_PROP_CACHE_ENABLED = "use.svn.cache";
	
	private File cacheFile;

	/**
	 * Creates a review source cache. See {@link SourceCache head comment} for details.
	 * 
	 * @param svnRepoId Identifies the SVN repository, which this cache must be created for.
	 */
	public SourceCache(String svnRepoId) {
		super();
		this.cacheFile = getCacheFile(svnRepoId);
	}

	/**
	 * Loads source list from correpsonding cache file.
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<ReviewSource> load() throws Exception{
		List<ReviewSource> ret = null;
		if (exists()){
			ObjectInputStream stream = new ObjectInputStream(new FileInputStream(cacheFile));
			try {
				ret = (List<ReviewSource>) stream.readObject();
			} finally {
				stream.close();
			}
		}
		return ret ;
	}

	/**
	 * Returns <code>true</code> if the cache file exists.
	 * 
	 * @return
	 */
	public boolean exists() {
		return cacheFile.isFile();
	}
	
	/**
	 * Saves source list in corresponding cache file.
	 * 
	 * @param toSave
	 * @throws Exception
	 */
	public void save(List<ReviewSource> toSave) throws Exception{
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(cacheFile));
		try {
			stream.writeObject(toSave);
		} finally {
			stream.close();
		}
	}
	
	private File getCacheFile(String svnid) {
		return new File(System.getProperty("java.io.tmpdir"),svnid+".cache");
	}

	/**
	 * Returns <code>true</code> if cache is enabled. Reads the value of
	 * {@value #SYS_PROP_CACHE_ENABLED} system property.
	 * 
	 * @return
	 */
	public static boolean isCacheEnabled() {
		return Boolean.getBoolean(SYS_PROP_CACHE_ENABLED);
	}
}
