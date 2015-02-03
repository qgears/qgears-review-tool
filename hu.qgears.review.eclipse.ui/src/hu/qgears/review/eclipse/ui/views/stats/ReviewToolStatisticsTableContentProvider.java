package hu.qgears.review.eclipse.ui.views.stats;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for {@link ReviewToolStatisticsView}. This class is handles
 * {@link StatisticsTableInput} inputs. Until the input is loading on a
 * background thread, status informations are provided as input, that can be
 * displayed by target viewer.
 * 
 * @author agostoni
 * 
 */
public class ReviewToolStatisticsTableContentProvider extends JobChangeAdapter implements IStructuredContentProvider, ISelectionProvider{

	private Viewer viewer;
	private UpdateStatisticsTableInputJob currentJob; 
	private ISelection selection ;
	
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
		if (newInput instanceof StatisticsTableInput){
			StatisticsTableInput input = (StatisticsTableInput) newInput;
			boolean newJob =
					//no job executed yet
					currentJob == null 
					//input is an other sourceset
					|| currentJob.getInput().getSourceSet() != input.getSourceSet()
					//update is forced by user
					|| input.isForceUpdate();
			if (newJob){
				if (currentJob != null){
					currentJob.cancel();
				}
				this.currentJob = new UpdateStatisticsTableInputJob("Query statistics for review source set "+input.getSourceSet(),input);
				currentJob.addJobChangeListener(this);
				// eclipse selection change events may occur fast after each other,
				// we need to have time to cancel job that is started earlier
				currentJob.schedule(500L);
			} else {
				currentJob.updateInput(input);
			}
		} else {
			this.currentJob = null;
		}
		viewer.refresh();
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (currentJob != null){
			switch (currentJob.getState()){
			case Job.NONE: {
				//job is ended
				if (currentJob.getInput().getReport() == null){
					if (currentJob.getResult().getSeverity() == IStatus.OK){
						//end with no data
						return new Object[] {"No statistics avalable for "+currentJob.getInput().getSourceSet()};
					} else {
						//end with failure
						return new Object[] {"Error occured during loading stats...",currentJob.getResult()}; 
					}
				} else {
					//successful load
					updateSelection(currentJob.getInput());
					return currentJob.getInput().getReport().toArray();
				}
			}
			default : {
				//job is running
				return new Object[] {"Loading statistics..."}; 
			}
			}
		} else {
			//no input
			return new Object[] {}; 
		}
	}

	@Override
	public void done(IJobChangeEvent event) {
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				viewer.refresh();
			}
		});
	}

	private void updateSelection(StatisticsTableInput input) {
		selection = input.getSummary() == null ? StructuredSelection.EMPTY :  new StructuredSelection(input.getSummary());
	}
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		//do not support listeners
	}

	@Override
	public ISelection getSelection() {
		return selection;
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		//do not support listeners
	}

	@Override
	public void setSelection(ISelection selection) {
		//do not support setting selection outside of this class
	}

}
