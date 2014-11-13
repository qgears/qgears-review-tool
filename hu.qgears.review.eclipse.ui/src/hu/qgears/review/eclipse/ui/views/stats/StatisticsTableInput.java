package hu.qgears.review.eclipse.ui.views.stats;

import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.model.ReviewSourceSet;
import hu.qgears.review.report.ReportEntry;
import hu.qgears.review.report.ReviewStatsSummary;

import java.util.List;

/**
 * Describes the review statistics, that is the input of the table in
 * {@link ReviewToolStatisticsView}. Gathering data may be a long running
 * operation, so this input is designed to support initialization on a BG
 * thread.
 * <p>
 * The input can be created by the only constructor of this class, and data can
 * be loaded using {@link UpdateStatisticsTableInputJob}. If data is ready, than
 * {@link #loaded(List, ReviewStatsSummary)} method must be called.
 * 
 * @author agostoni
 * 
 */
public class StatisticsTableInput {
	private ReviewStatsSummary summary;
	private ReviewInstance reviewInstance;
	private ReviewSourceSet sourceSet;
	private List<ReportEntry> report;
	private boolean forceUpdate;
	
	public StatisticsTableInput(ReviewInstance instance, ReviewSourceSet sourceSet,boolean forceUpdate) {
		super();
		this.reviewInstance = instance;
		this.sourceSet = sourceSet;
		this.forceUpdate = forceUpdate;
	}

	public List<ReportEntry> getReport() {
		return report;
	}
	
	public ReviewSourceSet getSourceSet() {
		return sourceSet;
	}
	
	public ReviewInstance getReviewInstance() {
		return reviewInstance;
	}
	
	public ReviewStatsSummary getSummary() {
		return summary;
	}

	public boolean isForceUpdate() {
		return forceUpdate;
	}

	/**
	 * Update this input instance with report data.
	 * 
	 * @param report
	 * @param sum
	 */
	public void loaded(List<ReportEntry> report, ReviewStatsSummary sum) {
		this.report = report;
		summary = sum;
	}
	
}
