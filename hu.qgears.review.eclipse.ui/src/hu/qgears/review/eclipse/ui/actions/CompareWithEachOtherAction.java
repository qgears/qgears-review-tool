package hu.qgears.review.eclipse.ui.actions;

import hu.qgears.review.eclipse.ui.vct.IVersionControlToolUi;
import hu.qgears.review.eclipse.ui.vct.VersionContolExtensionManager;
import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;
import hu.qgears.review.model.ReviewModel;

import org.eclipse.jface.action.Action;

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
		setEnabled(checkEnabled());
	}
	
	private boolean checkEnabled() {
		if (next != null && prev != null) {
			if (prev.getParent() != null && next.getParent()!= null && prev.getParent().getParent() != null) {
				return prev.getParent().getParent().equals(next.getParent().getParent());
			}
		}
		return false;
	}

	@Override
	public void run() {
		ReviewModel rm = prev.getReviewModel();
		IVersionControlToolUi vui = VersionContolExtensionManager.getVersionControlToolUi(prev.getModelElement().getReviewSource(rm).getVersionControlTool());
		vui.openCompareEditor(prev,next);
	}

}
