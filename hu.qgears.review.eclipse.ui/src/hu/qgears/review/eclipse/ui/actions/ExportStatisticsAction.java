package hu.qgears.review.eclipse.ui.actions;

import hu.qgears.review.eclipse.ui.ReviewToolUI;
import hu.qgears.review.eclipse.ui.views.stats.StatisticsTableInput;
import hu.qgears.review.eclipse.ui.wizard.ExportHtmlReportWizard;
import hu.qgears.review.model.ReviewModel;
import hu.qgears.review.model.ReviewSourceSet;
import hu.qgears.review.report.ReportEntry;
import hu.qgears.review.report.ReportGenerator;
import hu.qgears.review.report.ReportGeneratorHtml;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Action for exporting a HTML report about review progress.
 * 
 * @author agostoni
 * @see ExportHtmlReportWizard
 */
public class ExportStatisticsAction extends Action {

	private static final String TITLE = "Export review statistics as HTML...";


	private ReviewModel model;
	private ReviewSourceSet sourceSet;
	private List<ReportEntry> entries;
	
	public ExportStatisticsAction(StatisticsTableInput statisticsTableInput ) {
		this(statisticsTableInput.getReviewInstance().getModel(),statisticsTableInput.getSourceSet());
		this.entries = statisticsTableInput.getReport();
	}
	
	public ExportStatisticsAction(ReviewModel model, ReviewSourceSet sourceSet) {
		super();
		this.model = model;
		this.sourceSet = sourceSet;
		setText(TITLE);
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
	}


	@Override
	public void run() {
		final ExportHtmlReportWizard w = new ExportHtmlReportWizard(model,sourceSet,entries);
		WizardDialog d = new WizardDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), w);
		d.setHelpAvailable(false);
		if (WizardDialog.OK == d.open()){
			Job j = new Job(TITLE){
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					final ReportGenerator rg = w.getReportGenerator();
					ReportGeneratorHtml htmlTemplate = new ReportGeneratorHtml();
					try {
						htmlTemplate.generateReport(rg, w.getTargetFile(),w.mustGenerateReviewStats(),w.mustGenerateSonarStats(),w.mustGenerateTodoList(),w.mustGenerateCss());
					} catch (final Exception e) {
						return new Status(IStatus.ERROR,ReviewToolUI.PLUGIN_ID,"Html export failed",e);
					}
					return Status.OK_STATUS;
				}
			};
			j.schedule();
		}
	}

}
