package hu.qgears.review.report;

import hu.qgears.review.model.ReviewSource;
import hu.qgears.sonar.client.model.SonarResource;

/**
 * DTO that connects {@link ReviewSource}s and corresponding
 * {@link SonarResource} instances. This way statistics from review tool and
 * SONAR can be merged into a single object, that is more suitable for further
 * processing (e.g report generation.)
 * <p>
 * Mainly used as input data of {@link ReportGeneratorTemplate}.
 * 
 * @author agostoni
 * 
 */
public class ReportEntry {

	private final ReviewSource sourceFile;
	private final SonarResource resource;
	private final ReviewStatus reviewStatus;
	private String sourceFileLink;
	private String sonarLink;
	
	public ReportEntry(ReviewSource sourceFile, SonarResource resource,ReviewStatus reviewStatus) {
		this.sourceFile = sourceFile;
		this.resource = resource;
		this.reviewStatus = reviewStatus;
	}
	
	public ReviewSource getSourceFile() {
		return sourceFile;
	}

	public SonarResource getResource() {
		return resource;
	}

	public ReviewStatus getReviewStatus() {
		return reviewStatus;
	}

	/**
	 * Returns a link that navigates to the underlying source file in review
	 * tool.
	 * 
	 * @return
	 */
	public String getSourceFileLink() {
		return sourceFileLink;
	}

	/**
	 * Returns a link that navigates to SONAR web application, and shown the
	 * stats of underlying source file. May return null.
	 * 
	 * @return
	 */
	public String getSonarLink() {
		return sonarLink;
	}
	
	public void setSonarLink(String sonarLink) {
		this.sonarLink = sonarLink;
	}
	
	public void setSourceFileLink(String sourceFileLink) {
		this.sourceFileLink = sourceFileLink;
	}
	
}
