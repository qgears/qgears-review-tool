package hu.qgears.review.eclipse.ui.actions;

import hu.qgears.review.eclipse.ui.ReviewToolUI;
import hu.qgears.review.eclipse.ui.views.stats.StatisticsTableInput;
import hu.qgears.review.eclipse.ui.wizard.ExportReportWizard;
import hu.qgears.review.model.ReviewModel;
import hu.qgears.review.model.ReviewSourceSet;
import hu.qgears.review.report.ReportEntry;
import hu.qgears.review.report.ReportGenerator;
import hu.qgears.review.report.ReportGeneratorHtml;
import hu.qgears.review.report.ReportGeneratorODS;

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
 * @see ExportReportWizard
 */
public class ExportStatisticsAction extends Action {


	private ReviewModel model;
	private ReviewSourceSet sourceSet;
	private List<ReportEntry> entries;
	private boolean isHtml;
	
	public ExportStatisticsAction(StatisticsTableInput statisticsTableInput ,boolean isHtml) {
		this(statisticsTableInput.getReviewInstance().getModel(),statisticsTableInput.getSourceSet(),isHtml);
		this.entries = statisticsTableInput.getReport();
	}
	
	public ExportStatisticsAction(ReviewModel model, ReviewSourceSet sourceSet,boolean isHtml) {
		super();
		this.model = model;
		this.sourceSet = sourceSet;
		this.isHtml = isHtml;
		if (isHtml){
			setText("Export review statistics as HTML...");
		} else {
			setText("Export review statistics as ODS...");
		}
		
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
	}



	@Override
	public void run() {
		final ExportReportWizard w = new ExportReportWizard(model,sourceSet,entries,isHtml);
		WizardDialog d = new WizardDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), w);
		d.setHelpAvailable(false);
		if (WizardDialog.OK == d.open()){
			Job j = new Job(getText()){
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					final ReportGenerator rg = w.getReportGenerator();
					
					try {
						if (isHtml){
							ReportGeneratorHtml htmlTemplate = new ReportGeneratorHtml();
							htmlTemplate.generateReport(rg, w.getTargetFile(),w.mustGenerateReviewStats(),w.mustGenerateSonarStats(),w.mustGenerateTodoList(),w.mustGenerateCss());
						} else {
							ReportGeneratorODS odsTemplate = new ReportGeneratorODS();
							odsTemplate.generateReport(rg, w.getTargetFile(),w.mustGenerateReviewStats(),w.mustGenerateSonarStats(),w.mustGenerateTodoList());
						}
					} catch (final Exception e) {
						return new Status(IStatus.ERROR,ReviewToolUI.PLUGIN_ID,"Export failed",e);
					}
					return Status.OK_STATUS;
				}
			};
			j.schedule();
		}
	}

}
