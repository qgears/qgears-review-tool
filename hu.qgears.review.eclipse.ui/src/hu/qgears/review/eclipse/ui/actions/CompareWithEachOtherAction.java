package hu.qgears.review.eclipse.ui.actions;

import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;
import hu.qgears.review.model.ReviewEntry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.operation.CompareRepositoryResourcesOperation;

/**
 * Opens compare dialog, that load two different version of same file. (Using
 * functonality of SVN team provider.)
 * 
 * @author agostoni
 * 
 */
public class CompareWithEachOtherAction extends Action{

	private final ReviewEntryView prev;
	private final ReviewEntryView next;

	protected CompareWithEachOtherAction (){
		prev = null;
		next = null;
	}
	
	public CompareWithEachOtherAction(ReviewEntryView prev, ReviewEntryView next) {
		setText("Compare with each other...");
		this.prev = prev;
		this.next = next;
	}
	
	@Override
	public void run() {
		String prevURL = getFullSVNUrl(prev.getModelElement());
		String nextURL = getFullSVNUrl(next.getModelElement());
		openCompareEditor(prevURL,prev.getModelElement().getFileVersion(),
				nextURL, next.getModelElement().getFileVersion());
	}

	protected void openCompareEditor(String prevURL,String prevRevision, String nextURL, String nextRevision) {
		if (prevURL != null && nextURL != null){
			IRepositoryResource prevRes = SVNUtility.asRepositoryResource(prevURL, false);
			IRepositoryResource nextRes = SVNUtility.asRepositoryResource(nextURL, false);
			if (prevRes != null && nextRes != null){
				prevRes.setSelectedRevision(SVNRevision.fromString(prevRevision));
				nextRes.setSelectedRevision(SVNRevision.fromString(nextRevision));
				final CompareRepositoryResourcesOperation ds = new CompareRepositoryResourcesOperation(prevRes, nextRes);
				new Job("Comparing source revisions...") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						return ds.run(monitor).getStatus();
					}
				}.schedule();
			}
		}
	}
	
	protected String getFullSVNUrl (ReviewEntry entry){
		if (entry != null){
			return entry.getFolderUrl() +"/"+ entry.getFileUrl();
		}
		return null;
	}
}
