package hu.qgears.review.eclipse.ui.views.stats;

import hu.qgears.review.report.ReportEntry;
import hu.qgears.review.report.ReportGenerator;
import hu.qgears.review.report.ReviewStatsSummary;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Eclipse {@link Job} for loading review statistics on a background thread.
 * <p>
 * This operation may run for a long time, and might block the UI if it was run
 * on GUI thread.
 * 
 * @author agostoni
 * @see ReportGenerator
 * 
 */
public final class UpdateStatisticsTableInputJob extends Job {
	private StatisticsTableInput input;
	private boolean canceled;

	public UpdateStatisticsTableInputJob(String name, StatisticsTableInput input) {
		super(name);
		this.input = input;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (!canceled){
			ReportGenerator gen;
			synchronized (this) {
				gen = new ReportGenerator(input.getReviewInstance().getModel(), input.getSourceSet());
			}
			List<ReportEntry> report = gen.collectReportEntries();
			ReviewStatsSummary sum = null;
			if (report != null){
				sum = ReviewStatsSummary.create(report);
			}
			synchronized (this) {
				input.loaded(report,sum);
			}
			return Status.OK_STATUS;
		}
		return Status.OK_STATUS;
	}
	@Override
	protected void canceling() {
		this.canceled = true;
	}
	
	public StatisticsTableInput getInput() {
		return input;
	}
	
	/**
	 * Switch input to specified input. First the results of existing input will
	 * be copie to new input, than updates the input reference, and deltes old.
	 * 
	 * @param input
	 */
	public void updateInput(StatisticsTableInput input) {
		synchronized (this) {
			input.loaded(this.input.getReport(), this.input.getSummary());
			this.input = input;
		}
	}
}
