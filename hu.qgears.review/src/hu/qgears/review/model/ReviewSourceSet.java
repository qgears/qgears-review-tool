package hu.qgears.review.model;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ReviewSourceSet {
	public String id;
	public Set<String> sourceFiles;

	public ReviewSourceSet(String id, List<String> sourceFiles) {
		super();
		this.id=id;
		this.sourceFiles = new TreeSet<String>(sourceFiles);
	}
	@Override
	public String toString() {
		return "Source set: "+id;
	}
}
