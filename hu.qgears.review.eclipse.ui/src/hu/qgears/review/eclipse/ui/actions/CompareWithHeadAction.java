package hu.qgears.review.eclipse.ui.actions;

import org.eclipse.jface.action.Action;

import hu.qgears.review.eclipse.ui.vct.IVersionControlToolUi;
import hu.qgears.review.eclipse.ui.vct.VersionContolExtensionManager;
import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;
import hu.qgears.review.model.ReviewModel;

/**
 * Compares the HEAD version and the version of the source file, specified by
 * currently selected review entry
 * 
 * @author agostoni
 * 
 */
public class CompareWithHeadAction extends Action {

	private final ReviewEntryView entry;

	public CompareWithHeadAction(ReviewEntryView entry) {
		this.entry = entry;
		setText("Compare with head");
	}
	
	@Override
	public void run() {
		ReviewModel rm = entry.getReviewModel();
		IVersionControlToolUi vui = VersionContolExtensionManager.getVersionControlToolUi(entry.getModelElement().getReviewSource(rm).getVersionControlTool());
		vui.compareWithHead(entry.getModelElement());
	}
}
