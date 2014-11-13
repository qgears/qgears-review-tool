package hu.qgears.review.model;

import hu.qgears.commons.MultiMap;
import hu.qgears.commons.MultiMapHashImpl;
import hu.qgears.review.report.ReportGenerator;
import hu.qgears.review.util.IndexByProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The root element of the data model of a review server.
 * 
 * @author rizsi
 * 
 */
public class ReviewModel {

	private Map<String,ReviewSource> sources = new HashMap<String, ReviewSource>();
	public Map<String, String> mappings=new HashMap<String, String>();
	public Map<String, ReviewSourceSet> sourcesets=new HashMap<String, ReviewSourceSet>();
	public List<ReviewEntry> entries=new ArrayList<ReviewEntry>();
	private IndexByProperty<ReviewEntry> reviewEntryByUrl;
	public IndexByProperty<ReviewEntry> reviewEntryByFileSha1;
	List<IndexByProperty<ReviewEntry>> indexOnReview=new ArrayList<IndexByProperty<ReviewEntry>>();
	MultiMap<String, ReviewEntry> invalidates=new MultiMapHashImpl<String, ReviewEntry>();
	private String sonarBaseURL;
	private String sonarProjectId;
	private Set<String> users = new HashSet<String>();
	private Comparator<? super ReviewSource> sourcesComparator;
	public ReviewModel() {
		reviewEntryByUrl = new IndexByProperty<ReviewEntry>(new PropUrl());
		reviewEntryByFileSha1=new IndexByProperty<ReviewEntry>(new PropSha1());
		indexOnReview.add(getReviewEntryByUrl());
		indexOnReview.add(reviewEntryByFileSha1);
		sourcesComparator = new Comparator<ReviewSource>() {
			@Override
			public int compare(ReviewSource o1, ReviewSource o2) {
				String s1 = o1 == null ? "" : o1.modelUrl();
				String s2 = o2 == null ? "" : o2.modelUrl();
				return s1.compareTo(s2);
			}
		};
	}
	public File getFile(String fname) {
		int idx=fname.indexOf("/");
		String id=fname.substring(0, idx);
		String path=fname.substring(idx);
		return new File(mappings.get(id)+path);
	}
	public void addSourceSet(String id, List<String> ret) {
		sourcesets.put(id, new ReviewSourceSet(id, ret));
	}
	/**
	 * Returns {@link ReviewSource}, that has the specified model url.
	 * 
	 * @param url {@link ReviewSource#modelUrl()}
	 * @return
	 */
	public ReviewSource getSource(String url) {
		return sources.get(url);
	}
	public void addEntry(ReviewEntry entry) {
		entries.add(entry);
		for(IndexByProperty<ReviewEntry> index: indexOnReview)
		{
			index.addObject(entry);
		}
		for(String s: entry.getInvalidates())
		{
			invalidates.putSingle(s, entry);
		}
		addUser(entry.getUser());
	}
	/**
	 * Get all annotations that have the sha1 given.
	 * Normally it should be 0 or 1 (more than one means duplication or sha1 hit)
	 * @param sha1
	 */
	public List<ReviewEntry> getAnnotationsBySha1(String sha1) {
		// TODO use an index!
		List<ReviewEntry> ret=new ArrayList<ReviewEntry>(1);
		for(ReviewEntry e: entries)
		{
			if(sha1.equals(e.getSha1Sum()))
			{
				ret.add(e);
			}
		}
		return ret;
	}
	public List<ReviewSourceSet> getContainingSourceSets(String modelUrl) {
		List<ReviewSourceSet> ret=new ArrayList<ReviewSourceSet>();
		for(ReviewSourceSet rss: sourcesets.values())
		{
			if(rss.sourceFiles.contains(modelUrl))
			{
				ret.add(rss);
			}
		}
		return ret;
	}
	public boolean isInvalidated(String sha1Sum)
	{
		return !getInvalidates(sha1Sum).isEmpty();
	}
	public Collection<ReviewEntry> getInvalidates(String sha1Sum) {
		return invalidates.get(sha1Sum);
	}
	
	/**
	 * Returns the {@link ReviewSource sources files}, that are accessible in
	 * this model. The returned value is an ordered, read only list.
	 * <p>
	 * Use {@link #addSourceFiles(List)} to add a new source file.
	 * 
	 * @return
	 */
	public List<ReviewSource> getSources() {
		ArrayList<ReviewSource> src = new ArrayList<ReviewSource>(sources.values());
		Collections.sort(src,sourcesComparator);
		return Collections.unmodifiableList(src);
	}
	
	/**
	 * Adds the specified source files into a modelURLBased cache. Use
	 * {@link #getSource(String)} and {@link #getSources()} methods to access
	 * the them in the future.
	 * 
	 * @param files
	 */
	public void addSourceFiles(List<ReviewSource> files) {
		for (ReviewSource rs : files){
			sources.put(rs.modelUrl(), rs);
		}
	}

	/**
	 * Returns an index that maps {@link ReviewEntry}s by source URL (
	 * {@link ReviewSource#sourceURL()} or {@link ReviewEntry#getFileUrl()}).
	 * 
	 * @return
	 */
	public IndexByProperty<ReviewEntry> getReviewEntryByUrl() {
		return reviewEntryByUrl;
	}
	public void setSonarBaseURL(String sonarBaseURL) {
		this.sonarBaseURL = sonarBaseURL;
	}
	
	/**
	 * Returns the URL, that must be used when we need to access SONAR's REST
	 * API. Mainly used by {@link ReportGenerator} to get metrics from SONAR.
	 * 
	 * @return
	 */
	public String getSonarBaseURL() {
		return sonarBaseURL;
	}
	public void setSonarProjectId(String sonarProjectId) {
		this.sonarProjectId = sonarProjectId;
	}
	
	/**
	 * Identifies the SONAR project that contains the code analysis data for
	 * sources. Mainly used by {@link ReportGenerator} to get metrics from
	 * SONAR.
	 * 
	 * @return
	 */
	public String getSonarProjectId() {
		return sonarProjectId;
	}
	
	public void addUser(String user) {
		users.add(user);
	}
	public Set<String> getUsers() {
		return users;
	}
}
