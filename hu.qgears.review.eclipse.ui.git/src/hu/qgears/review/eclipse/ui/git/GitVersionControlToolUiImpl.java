package hu.qgears.review.eclipse.ui.git;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.egit.ui.internal.CompareUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import hu.qgears.review.eclipse.ui.util.UtilLog;
import hu.qgears.review.eclipse.ui.util.UtilWorkspace;
import hu.qgears.review.eclipse.ui.vct.IVersionControlToolUi;
import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.util.vct.EVersionControlTool;

/**
 * EGit based implementation of {@link IVersionControlToolUi} interface. This
 * enables the Compare with HEAD and Compare with each others action for GIT
 * sources.
 * 
 * @author agostoni
 *
 */
/*
 * Using internal API of egit to open compare editor. This API seems to be
 * stable, despite of internal declaration.
 */
@SuppressWarnings("restriction")
public class GitVersionControlToolUiImpl implements IVersionControlToolUi {

	private static final String TITLE = "Git compare";
	private boolean searchByDateIsEnabled;

	public GitVersionControlToolUiImpl() {
	}

	@Override
	public void openCompareEditor(ReviewEntryView r1, ReviewEntryView r2) {
		IResource res = getResourceForEntry(r1);
		if (res == null) {
			notImportedError();
		} else {
			try {
				openCompareEditor(res, r1.getModelElement().getFileUrl(),r2.getModelElement().getFileUrl(), getVersion(res,r1), getVersion(res,r2));
			} catch (Exception e) {
				UtilLog.showErrorDialog(TITLE,"Git compare failed", e);
			}
		}
	}

	private String getVersion(IResource res, ReviewEntryView r1) throws Exception {
		RepositoryMapping rm = RepositoryMapping.getMapping(res);
		if (rm != null && rm.getRepository() != null) {
			ReviewEntry modelElement = r1.getModelElement();
			Repository repo = rm.getRepository();
			String versionSha1 = modelElement.getFileVersion();
			if (commitExists(repo, versionSha1)) {
				return versionSha1;
			} else {
				if (userAllowsSearchByDate(versionSha1)) {
					long date = modelElement.getDate();
					String path = modelElement.getFileUrl();
					
					DateFormat f = SimpleDateFormat.getDateTimeInstance();
					RevCommit closest = findCommitClosestToDate(repo, path,date);
					if (closest == null) {
						throw new Exception("Cannot any commit of "+path +" around " + f.format( new Date(date)));
					} else {
						String sha = closest.getId().getName();
						Date newDate = new Date(closest.getCommitTime() * 1000l);
						UtilLog.logInfo("Commit '"+versionSha1+"' is resolved to '"+sha + "' at "+f.format(newDate)+
								". This is the closest commit on "+ path +" before date "+f.format(new Date(date)));
						return sha;
					}
				} else {
					throw new OperationCanceledException("Cancel compare, as search by date is not allowed");
				}
			}
		} else {
			throw new Exception("Cannot find GIT repo for resources! Try Team -> Share Project... on project "
					+ res.getProject());
		}
	}

	private boolean userAllowsSearchByDate(String versionSha1) {
		if (!searchByDateIsEnabled) {
			//ask this question only once 
			searchByDateIsEnabled = MessageDialog.openQuestion(getShell(), TITLE, "'"+ versionSha1 + "' seems not to be a valid GIT commit id, do you want to search commit by date?");
		}
		return searchByDateIsEnabled;
	}

	private void notImportedError() {
		MessageDialog.openError(getShell(), TITLE, "Please import the surce files into workspace first!");
	}

	private void openCompareEditor(IResource res, String lefPath, String rightPath, String v1, String v2) throws Exception {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page != null) {
			RepositoryMapping rm = RepositoryMapping.getMapping(res);
			if (rm != null && rm.getRepository() != null) {
				CompareUtils.compareBetween(rm.getRepository(), lefPath, rightPath, v1, v2, page);
			} else {
				throw new Exception(
						"Cannot find GIT repo for resources! Try Team -> Share Project... on project "
								+ res.getProject());
			}
		} else {
			// no active workbench page, nothing to do
		}
	}

	private IResource getResourceForEntry(ReviewEntryView re) {
		ReviewSource source = re.getParent().getParent().getModelElement();
		return UtilWorkspace.getFileInWorkspace(source.getFileInWorkingCopy());
	}

	private Shell getShell() {
		return Display.getDefault().getActiveShell();
	}

	@Override
	public void compareWithHead(ReviewEntryView modelElement) {
		IResource res = getResourceForEntry(modelElement);
		if (res == null) {
			notImportedError();
		} else {
			try {
				String localSrc = modelElement.getParent().getParent().getSource().getSourceUrl();
				openCompareEditor(res,localSrc,modelElement.getModelElement().getFileUrl() ,"HEAD", getVersion(res,modelElement));
			} catch (Exception e) {
				UtilLog.showErrorDialog(TITLE,"Git compare failed", e);
			}
		}
	}

	@Override
	public EVersionControlTool getToolId() {
		return EVersionControlTool.GIT;
	}

	public static RevCommit findCommitClosestToDate(Repository repository,String filePath, long targetMillisSinceEpoch) throws Exception {
		 try (Git git = new Git(repository)) {
	            Iterable<RevCommit> commits = git.log()
	                    .addPath(filePath)
	                    .call();

	            RevCommit closest = null;
	            long smallestDiff = Long.MAX_VALUE;

	            for (RevCommit commit : commits) {
	            	//committime is stored as "unix time stamp", so SECONDS since epoch
	                long commitTime = commit.getCommitTime() * 1000L;

	                long diff = targetMillisSinceEpoch - commitTime;
	                if (diff >= 0) {
	                	if (diff < smallestDiff) {
	                		smallestDiff = diff;
	                		closest = commit;
	                	}
	                } else {
	                	//the commit is created AFTER the date we are looking for, ignore
	                }
	            }
	            return closest;
	        }
	}

	/**
	 * Checks whether a given commit SHA exists in the repository.
	 *
	 * @param repository JGit Repository object
	 * @param commitSha  The commit SHA (can be full or abbreviated)
	 * @return true if the commit exists, false otherwise
	 */
	public static boolean commitExists(Repository repository, String commitSha) {
		try {
			// Resolve the commit SHA to an ObjectId
			ObjectId commitId = repository.resolve(commitSha);
			if (commitId == null) {
				return false;
			}
			return true;
		} catch (Exception e) {
			// Any error means the commit doesn't exist or is not valid
			return false;
		}
	}
}
