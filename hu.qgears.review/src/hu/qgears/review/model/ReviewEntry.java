package hu.qgears.review.model;

import hu.qgears.commons.UtilString;
import hu.qgears.review.action.LoadConfiguration;
import hu.qgears.review.util.UtilSha1;
import hu.qgears.review.util.UtilSimpleProperties;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A single review entry.
 * Currently a review entry corresponds to:
 *  <li> A single file with a single SVN url and revision number.
 *  <li> Also stores the md5 sum of the file and the context
 * @author rizsi
 *
 */
public class ReviewEntry {
	private String folderId;
	private String folderUrl;
	private String folderVersion;
	private String fileSha1sum;
	private String fileVersion;
	private String fileUrl;
	private String comment;
	private EReviewAnnotation annotation;
	private String user;
	private String lines;
	private long date;
	private String sha1Sum;
	private List<String> invalidates;
	
	public ReviewEntry(String folderId, String folderUrl, String folderVersion,
			String fileSha1sum, String fileVersion, String fileUrl,
			String comment, EReviewAnnotation annotation, String user, String lines, long date,
			List<String> invalidates) {
		super();
		checkNotNull(folderId);
		checkNotNull(folderUrl);
		checkNotNull(folderVersion);
		checkNotNull(fileSha1sum);
		checkNotNull(fileVersion);
		checkNotNull(fileUrl);
		checkNotNull(comment);
		checkNotNull(annotation);
		checkNotNull(user);
		checkNotNull(lines);
		checkNotNull(invalidates);
		this.date=date;
		this.folderId = folderId;
		this.folderUrl = folderUrl;
		this.folderVersion = folderVersion;
		this.fileSha1sum = fileSha1sum;
		this.fileVersion = fileVersion;
		this.fileUrl = fileUrl;
		this.comment = comment;
		this.annotation = annotation;
		this.user = user;
		this.lines = lines;
		this.invalidates=invalidates;
		try {
			this.sha1Sum = UtilSha1.getSHA1(toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// Never happens
		}
	}

	private void checkNotNull(Object param) {
		if(param==null)
		{
			throw new NullPointerException();
		}
	}
	private void checkNotNull(List<String> param) {
		if(param==null)
		{
			throw new NullPointerException();
		}
	}

	private void addInvalidatesParam(Map<String, String> params) {
		if(invalidates.size()>0)
		{
			params.put("invalidates", UtilString.concat(invalidates, ","));
		}
	}

	@Override
		public String toString()
		{
			Map<String, String> params=new TreeMap<String, String>();
			params.put("folderId", folderId);
			params.put("folderUrl", folderUrl);
			params.put("folderVersion", folderVersion);
			params.put("fileSha1sum", fileSha1sum);
			params.put("fileVersion", fileVersion);
			params.put("fileUrl", fileUrl);
			params.put("comment", comment);
			params.put("annotation", annotation.name());
			params.put("user", user);
			params.put("lines", lines);
			params.put("date", ""+date);
			addInvalidatesParam(params);
	//		params.put("invalidates", ""+);
			return LoadConfiguration.ussProperties.escapeAndConcat(UtilSimpleProperties.propertiesToList(params));
		}

	public static ReviewEntry parseFromString(String stringrepresentation)
	{
		Map<String, String> params=UtilSimpleProperties.parseProperties(LoadConfiguration.ussProperties.splitAndUnescape(stringrepresentation));
		String invalidates=params.get("invalidates");
		if(invalidates==null) invalidates="";
		List<String> inv=UtilString.split(invalidates, ",");
		return new ReviewEntry(params.get("folderId"),
				params.get("folderUrl"),
				params.get("folderVersion"),
				params.get("fileSha1sum"),
				params.get("fileVersion"),
				params.get("fileUrl"),
				params.get("comment"),
				EReviewAnnotation.valueOf(params.get("annotation")),
				params.get("user"),
				params.get("lines"),
				Long.parseLong(params.get("date")),
				inv);
	}

	/**
	 * The id of the source folder, to which this review entry belongs to. See
	 * {@link ReviewSource#getSourceFolderId()}
	 * 
	 * @return
	 */
	public String getFolderId() {
		return folderId;
	}

	/**
	 * The URL of the source folder, to which this review entry belongs to. See
	 * {@link ReviewSource#getSourceFolderUrl()}
	 * 
	 * @return
	 */
	public String getFolderUrl() {
		return folderUrl;
	}

	public String getFolderVersion() {
		return folderVersion;
	}

	public String getFileSha1sum() {
		return fileSha1sum;
	}

	/**
	 * The SVN revision of source file, when this review entry was created.
	 * 
	 * @return
	 */
	public String getFileVersion() {
		return fileVersion;
	}

	/**
	 * The source URL of corresponding review source, see
	 * {@link ReviewSource#getSourceUrl()}.
	 * 
	 * @return
	 */
	public String getFileUrl() {
		return fileUrl;
	}

	public String getComment() {
		return comment;
	}

	/**
	 * The annotation specifies the type of this {@link ReviewEntry} instance.
	 * See javadoc on the literals on {@link EReviewAnnotation} for available
	 * review types.
	 * 
	 * @return
	 */
	public EReviewAnnotation getAnnotation() {
		return annotation;
	}

	public String getUser() {
		return user;
	}

	public String getLines() {
		return lines;
	}

	public long getDate() {
		return date;
	}
	public List<String> getInvalidates() {
		return invalidates;
	}

	/**
	 * This review is valid for this source.
	 * @param rs
	 * @return true in case the file Version Control version and its SHA1 sum and file URL and folder ID 
	 *  equals to the annotation's stored values.
	 */
	public boolean matches(ReviewSource rs) {
		boolean sha1eq=fileSha1sum.equals(rs.getSha1());
		if(sha1eq)
		{
			return true;
		}
		//review off is version independent
		boolean versionEq =
				fileVersion.equals(rs.getFileVersion()) 
				|| getAnnotation() == EReviewAnnotation.reviewOff; 
		return 
				fileUrl.equals(rs.getSourceUrl()) 
				&& folderId.equals(rs.getSourceFolderId())
				&& versionEq; 
	}
	/**
	 * This review was valid for a previous version of this source.
	 * @param rs
	 * @return
	 */
	public boolean matchesPrevious(ReviewSource rs) {
		if (fileUrl.equals(rs.getSourceUrl())) {
			//url is the same - but we are not the latest
			return !matches(rs);
		} else {
			return rs.getPreviousSourceUrls().contains(fileUrl);	
		}
	}
	public String getSha1Sum() {
		return sha1Sum;
	}

	/**
	 * Returns the full URL of target {@link ReviewSource}, which this entry
	 * instance belongs to. Same as {@link ReviewSource#modelUrl()}.
	 * 
	 * @return
	 */
	public String getFullUrl() {
		return folderId+"/"+fileUrl;
	}
	
	/**
	 * Helper method for finding {@link ReviewSource} instance, which this
	 * review Entry belongs to.
	 * 
	 * @param model
	 *            The {@link ReviewModel} that contains this {@link ReviewEntry}
	 *            .
	 * @return
	 */
	public ReviewSource getReviewSource(ReviewModel model){
		return model.getSource(getFullUrl());
	}
}
