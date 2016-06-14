package hu.qgears.eclipse.ui.svn;

import hu.qgears.review.model.ReviewEntry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.eclipse.team.svn.ui.operation.CompareRepositoryResourcesOperation;

/**
 * Responsible for opening compare editor fro SVN resources.
 * 
 * @author agostoni
 *
 */
public class CompareEditorImpl {


	public void openCompareEditor(ReviewEntry prev, ReviewEntry next) {
		openCompareEditor(getFullSVNUrl(prev),prev.getFileVersion(),getFullSVNUrl(next),next.getFileVersion());
	}
	
	protected void openCompareEditor(String prevURL,String prevRevision, String nextURL, String nextRevision) {
		if (prevURL != null && nextURL != null){
			IRepositoryResource prevRes = SVNUtility.asRepositoryResource(prevURL, false);
			IRepositoryResource nextRes = SVNUtility.asRepositoryResource(nextURL, false);
			if (prevRes != null && nextRes != null){
				prevRes.setSelectedRevision(SVNRevision.fromString(prevRevision));
				nextRes.setSelectedRevision(SVNRevision.fromString(nextRevision));
				final CompareRepositoryResourcesOperation ds = new CompareRepositoryResourcesOperation(prevRes, nextRes);
				Job j = new Job("Comparing source revisions...") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						return ds.run(monitor).getStatus();
					}
				};
				j.setUser(true);
				j.schedule();
			}
		}
	}
	
	protected String getFullSVNUrl (ReviewEntry entry){
		if (entry != null){
			return entry.getFolderUrl() +"/"+ entry.getFileUrl();
		}
		return null;
	}


	public void compareWithHead(ReviewEntry modelElement) {
		String svnUrl = getFullSVNUrl(modelElement);
		if (svnUrl != null){
			openCompareEditor(svnUrl, modelElement.getFileVersion(), svnUrl, "HEAD");
		}
	}
}
