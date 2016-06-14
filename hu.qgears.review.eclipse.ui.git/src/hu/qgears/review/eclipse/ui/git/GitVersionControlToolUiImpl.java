package hu.qgears.review.eclipse.ui.git;

import hu.qgears.review.eclipse.ui.util.UtilLog;
import hu.qgears.review.eclipse.ui.util.UtilWorkspace;
import hu.qgears.review.eclipse.ui.vct.IVersionControlToolUi;
import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.util.vct.EVersionControlTool;

import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.egit.ui.internal.CompareUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * EGit based implementation of {@link IVersionControlToolUi} interface. This
 * enables the Compare with HEAD and Compare with each others action for GIT
 * sources.
 * 
 * @author agostoni
 *
 */
/* Using internal API of egit to open compare editor. This API seems to be stable, despite of
 * internal declaration. */
@SuppressWarnings("restriction")
public class GitVersionControlToolUiImpl implements IVersionControlToolUi {

	private static final String TITLE = "Git compare";

	public GitVersionControlToolUiImpl() {
	}

	@Override
	public void openCompareEditor(ReviewEntryView r1, ReviewEntryView r2) {
		IResource res = getResourceForEntry(r1);
		if (res == null) {
			notImportedError();
			//res for r2 should be same as r1
		} else {
			openCompareEditor(res,getVersion(r1), getVersion(r2));
		}
		
	}

	private String getVersion(ReviewEntryView r1) {
		return r1.getModelElement().getFileVersion();
	}

	private void notImportedError() {
		MessageDialog.openError(getShell(), TITLE,
				"Please import the surce files into workspace first!");
	}

	private void openCompareEditor(IResource res, String  v1, String v2) {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		if (page != null) {
			IResource[] resources = new IResource[] { res };
			RepositoryMapping rm = RepositoryMapping.getMapping(res);
			if (rm != null && rm.getRepository() != null) {
				try {
					CompareUtils.compare(resources, rm.getRepository(), v1, v2,
							false, page);
				} catch (IOException e) {
					UtilLog.showErrorDialog("Cannot open GIT compare dialog:(", e);
				}
			} else {
				MessageDialog.openError(getShell(), TITLE,
						"Cannot find GIT repo for resources!");
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
			openCompareEditor(res,"HEAD",getVersion(modelElement));
		}
	}

	@Override
	public EVersionControlTool getToolId() {
		return EVersionControlTool.GIT;
	}

}
