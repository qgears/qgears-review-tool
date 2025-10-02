package hu.qgears.review.eclipse.ui.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import hu.qgears.review.eclipse.ui.preferences.Preferences;
import hu.qgears.review.eclipse.ui.util.UtilLog;
import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;
import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;
import hu.qgears.review.model.EReviewAnnotation;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.model.ReviewSource;

public class CheckForRenameAction extends Action {

	private List<SourceTreeElement> currentSelection;
	private TreeViewer viewerToRefresh;
	private ReviewInstance reviewInstance;
	private String userName;

	public CheckForRenameAction(List<SourceTreeElement> selectedSources, TreeViewer viewerToRefresh,
			ReviewInstance reviewInstance) {
		this.currentSelection = selectedSources;
		this.viewerToRefresh = viewerToRefresh;
		this.reviewInstance = reviewInstance;
		this.userName = Preferences.getDefaultUserName();
		setText("Renew exisiting review entries after a rename");
		setToolTipText("Check whether the source file was moved to another location, without any change in the file content. If the older version is found and has already been reviewed, then a reviewOk will be placed on the new version.");
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ADD));
	}
	
	@Override
	public void run() {
		List<ReviewEntry> newEntries = new ArrayList<>();
		for (SourceTreeElement ste : currentSelection) {
			ReviewEntry re = getMatchingVersionWithSameSha(ste);
			if (re != null) {
				ReviewEntry newEntry = createFrom(re,ste.getSource());
				newEntries.add(newEntry);
			}
		}
		if (newEntries.size() > 0) {
			boolean ans = MessageDialog.openQuestion(getShell(), getText(), newEntries.size() + " moved sources have been found. Do you want to create new review entries for them?");
			if (ans) {
				try {
					for (ReviewEntry re : newEntries) {
						reviewInstance.saveEntry(re);
					}
				} catch (IOException e) {
					UtilLog.showErrorDialog(getText(), "Failed to save new review entries", e); 
				}
				updateSeletion(newEntries);
			}
		} else {
			MessageDialog.openInformation(getShell(), getText(), "No ");
		}
	}


	private ReviewEntry createFrom(ReviewEntry old, ReviewSource reviewSource) {
		String comment = "File was moved from "+old.getFileUrl()+ " with unchanged content, and old version was already OK.";
		EReviewAnnotation annotation = EReviewAnnotation.reviewOk;
		String user = Preferences.getDefaultUserName();
		long date = System.currentTimeMillis();
		ReviewEntry re = new ReviewEntry(
				reviewSource.getSourceFolderId(),
				reviewSource.getSourceFolderUrl(),
				reviewSource.getFolderVersion(),
				reviewSource.getSha1(),
				reviewSource.getFileVersion(),
				reviewSource.getSourceUrl(),
				comment, annotation, user, "all", date,Collections.emptyList());
		return re ;
	}

	private ReviewEntry getMatchingVersionWithSameSha(SourceTreeElement ste) {
		ReviewSource src = ste.getSource();
		if (!hasUpToDateReviewForUser(src)) {
			
			for (ReviewEntry r :  src.getMatchingReviewEntriesPreviousVersion(reviewInstance.getModel())) {
				if (r.matches(src)) {
					if (EReviewAnnotation.reviewOk.equals( r.getAnnotation()) && r.getUser().equals(userName)) {
						return r;
					}
				}
			}
		}
		return null;
	}
	
	private boolean hasUpToDateReviewForUser(ReviewSource src) {
		for (ReviewEntry r :src.getMatchingReviewEntries(reviewInstance.getModel())) {
			if (EReviewAnnotation.reviewOk.equals(r.getAnnotation())) {
				if (r.getUser().equals(userName)) {
					//review already created
					return true;
				}
			}
		}
		return false;
	}

	private void updateSeletion(List<ReviewEntry> newEntries) {
		List<ReviewEntryView> newItems = new ArrayList<>();
		for (ReviewEntry re : newEntries) {
			for (SourceTreeElement ste : currentSelection) {
				ReviewEntryView v = ste.getMatchingView(re);
				if (v != null) {
					newItems.add(v);
					break;
				}
			}		
		}
		viewerToRefresh.setSelection(new StructuredSelection(newItems));
	}
	
	private Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}
}
