package hu.qgears.review.model;

import hu.qgears.review.util.UtilSha1;
import hu.qgears.review.util.vct.EVersionControlTool;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Source file that is accessible to the review server.
 * @author rizsi
 *
 */
public class ReviewSource implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5769188355921693423L;
	
	private String sourceFolderId;
	private String sourceFolderUrl;
	private String sourceUrl;
	private String folderVersion;
	private String fileVersion;
	private String sha1;
	private EVersionControlTool vct;
	
	private final File fileInWorkingCopy;
	
	public ReviewSource(String sourceFolderId, String sourceFolderUrl, 
			String sourceUrl, String folderVersion, String fileVersion, 
			String sha1, File fileInWorkingCopy,EVersionControlTool vct) {
		super();
		this.fileInWorkingCopy = fileInWorkingCopy;
		this.vct = vct;
		this.setSourceFolderId(sourceFolderId);
		this.setSourceFolderUrl(sourceFolderUrl);
		this.setSourceUrl(sourceUrl);
		this.setFolderVersion(folderVersion);
		this.setFileVersion(fileVersion);
		this.setSha1(sha1);
	}
	@Override
	public String toString() {
		return getSourceFolderId()+"/"+getSourceUrl();
	}
	public String toStringLong() {
		return getSourceFolderId()+"/"+getSourceUrl()+" "+getFolderVersion()+" "+getFileVersion();
	}

	/**
	 * Returns model URL, which consist of {@link #getSourceFolderId()
	 * sourcefolder id} and {@link ReviewSource#getSourceUrl() source URL}
	 * concatenated using "/" as separator char.
	 * 
	 * @return
	 */
	public String modelUrl() {
		return getSourceFolderId()+"/"+getSourceUrl();
	}

	/**
	 * Returns the last segment of source file url. For instance it returns
	 * <code>'MyClass.java'</code> for url 
	 * <code>'repo_root_folder_id/com/samplecompany/example/MyClass.java'</code>
	 * 
	 * @return the last segment of the file URL
	 */
	public String getSimpleName() {
		if(getSourceUrl().indexOf("/")>=0)
		{
			return getSourceUrl().substring(getSourceUrl().lastIndexOf("/")+1);
		}
		return getSourceUrl();
	}
	
	/**
	 * Returns the identifier of the folder into which an SVN repository is
	 * checked out, which contains this source file, as defined in the 
	 * sourcefoldermappings.property file. This id can be resolved to valid SVN 
	 * URL by calling {@link #getSourceFolderUrl()}).
	 * @return the identifier of the 
	 */
	public String getSourceFolderId() {
		return sourceFolderId;
	}
	
	public void setSourceFolderId(String sourceFolderId) {
		this.sourceFolderId = sourceFolderId;
	}
	
	/**
	 * The URL of the SVN repository, which this source file belongs to (e.g.
	 * svn+ssh://qgears.net/opensource/trunk). It is the resolved pair of
	 * {@link #getSourceFolderId()}.
	 */
	public String getSourceFolderUrl() {
		return sourceFolderUrl;
	}
	public void setSourceFolderUrl(String sourceFolderUrl) {
		this.sourceFolderUrl = sourceFolderUrl;
	}
	
	/**
	 * The URL of this resource, relatively to {@link #getSourceFolderUrl()}.
	 */
	public String getSourceUrl() {
		return sourceUrl;
	}
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
	/**
	 * The SHA1 sum of this source file. See {@link UtilSha1}.
	 */
	public String getSha1() {
		return sha1;
	}
	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}
	
	/**
	 * The current revision of SVN repository root ({@link #getSourceFolderUrl()}).
	 * @return
	 */
	public String getFolderVersion() {
		return folderVersion;
	}
	public void setFolderVersion(String folderVersion) {
		this.folderVersion = folderVersion;
	}
	/**
	 * The current SVN revision of the source file.
	 * 
	 * @return
	 */
	public String getFileVersion() {
		return fileVersion;
	}
	public void setFileVersion(String fileVersion) {
		this.fileVersion = fileVersion;
	}
	
	/**
	 * Parses the fully qualified Java name from {@link #getSourceUrl() source
	 * url}.
	 * 
	 * @return
	 */
	public String getFullyQualifiedJavaName() {
		String fn = getSourceUrl();
		if (fn.contains("src")){
			String[] parts = fn.split("\\/src\\/");
			String fqn = parts[parts.length-1];
			//removing trailing '.java'
			fqn = fqn.substring(0, fqn.length()-5);
			return fqn.replace('/', '.');
		} else {
			return fn;
		}
	}
	
	/**
	 * Returns the {@link ReviewEntry}s that are connected to current version of
	 * this source class (the source did not change since the review has been
	 * created).
	 * 
	 * @param modelRoot The {@link ReviewModel} model root to search within
	 * @return The list of matching {@link ReviewEntry}s, list may be empty.
	 */
	public List<ReviewEntry> getMatchingReviewEntries(ReviewModel modelRoot){
		Collection<ReviewEntry> entries = modelRoot.getReviewEntryByUrl().getMappedObjects(getSourceUrl());
		List<ReviewEntry> ret = new ArrayList<ReviewEntry>();
		for(ReviewEntry e:entries)
		{
			if(e.matches(this))
			{
				ret.add(e);
			}
		}
		return ret;
	}

	/**
	 * Returns the {@link ReviewEntry}s that are connected to an older version
	 * of this source class (the source changed since the review has
	 * been created).
	 * 
	 * @param modelRoot
	 *            The {@link ReviewModel} model root to search within
	 * @return The list of matching {@link ReviewEntry}s, list may be empty.
	 */
	public List<ReviewEntry> getMatchingReviewEntriesPreviousVersion(ReviewModel modelRoot){
		Collection<ReviewEntry> entries = modelRoot.getReviewEntryByUrl().getMappedObjects(getSourceUrl());
		List<ReviewEntry> ret = new ArrayList<ReviewEntry>();
		for(ReviewEntry e:entries)
		{
			if(!e.matches(this) && e.matchesPrevious(this))
			{
				ret.add(e);
			}
		}
		return ret;
	}

	/**
	 * Returns the {@link ReviewEntry}s that are connected to this source class
	 * and are invalidated by an other review entry. The returned list
	 * includes current and old review entries.
	 * 
	 * @param modelRoot
	 *            The {@link ReviewModel} model root to search within
	 * @return The invalidated {@link ReviewEntry}s, list may be empty.
	 * 
	 * @see ReviewModel#isInvalidated(String)
	 * @see #getMatchingReviewEntriesPreviousVersion(ReviewModel) Query old
	 *      reviews
	 * @see #getMatchingReviewEntries(ReviewModel) Query current reviewes
	 */
	public List<ReviewEntry> getInValidReviewEntries(ReviewModel modelRoot){
		Collection<ReviewEntry> entries = modelRoot.getReviewEntryByUrl().getMappedObjects(getSourceUrl());
		List<ReviewEntry> ret = new ArrayList<ReviewEntry>();
		for (ReviewEntry re : entries){
			if (modelRoot.isInvalidated(re.getSha1Sum())){
				ret.add(re);
			}
		}
		return ret;
	}
	
	/**
	 * Returns the {@link File} that represents this source file in current
	 * working copy.
	 * 
	 * @return
	 */
	public File getFileInWorkingCopy() {
		return fileInWorkingCopy;
	}
	/**
	 * Returns the vct that loaded this source file.
	 * 
	 * @return
	 */
	public EVersionControlTool getVersionControlTool(){
		return vct;
	}
}
