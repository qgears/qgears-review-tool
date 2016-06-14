package hu.qgears.review.eclipse.ui.actions;

import hu.qgears.review.eclipse.ui.views.model.ReviewSourceSetView;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

/**
 * Action that calls {@link ImportProject}.
 * 
 * @author agostoni
 */
public class ImportProjectForSourcesetAction extends Action {

	private ReviewSourceSetView reviewSourceSetView;
	private Viewer toRefresh;

	public ImportProjectForSourcesetAction(ReviewSourceSetView reviewSourceSetView,Viewer toRefresh) {
		this.reviewSourceSetView = reviewSourceSetView;
		this.toRefresh = toRefresh;
		setText("Import projects to workspace");
		setDescription("Imports the containing eclipse project(s) of the sources of this sourceset into the workspace.");
		//TODO add icon to this action
	}

	@Override
	public void run() {
		Job importJob = new ImportProject(reviewSourceSetView).createImportJob();
		importJob.addJobChangeListener(new JobChangeAdapter(){
			@Override
			public void done(IJobChangeEvent event) {
				Display.getDefault().asyncExec(new RefreshViewerAction(toRefresh));
			}
		});
		importJob.schedule();
	}
}
