package hu.qgears.review.eclipse.ui.actions;

import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;

/**
 * Compares the HEAD version and the version of the source file, specified by
 * currently selected review entry
 * 
 * @author agostoni
 * 
 */
public class CompareWithHeadAction extends CompareWithEachOtherAction {

	private final ReviewEntryView entry;

	public CompareWithHeadAction(ReviewEntryView entry) {
		this.entry = entry;
		setText("Compare with head");
	}
	
	@Override
	public void run() {
		String svnUrl = getFullSVNUrl(entry.getModelElement());
		if (svnUrl != null){
			openCompareEditor(svnUrl, entry.getModelElement().getFileVersion(), svnUrl, "HEAD");
		}
	}
}
