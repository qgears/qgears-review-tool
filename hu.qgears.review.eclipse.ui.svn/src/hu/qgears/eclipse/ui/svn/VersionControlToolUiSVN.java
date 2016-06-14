package hu.qgears.eclipse.ui.svn;

import hu.qgears.review.eclipse.ui.vct.IVersionControlToolUi;
import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.util.vct.EVersionControlTool;

/**
 * SVN based implementation of {@link IVersionControlToolUi}
 * 
 * @author agostoni
 *
 */
public class VersionControlToolUiSVN implements IVersionControlToolUi {

	private CompareEditorImpl compareEditor = new CompareEditorImpl();
	
	public VersionControlToolUiSVN() {
		//this is an extension point implementation, so it must have a default ctor
	}

	@Override
	public void openCompareEditor(ReviewEntryView r1, ReviewEntryView r2) {
		compareEditor.openCompareEditor(r1.getModelElement(),r2.getModelElement());
	}


	@Override
	public void compareWithHead(ReviewEntryView modelElement) {
		compareEditor.compareWithHead(modelElement.getModelElement());
	}

	@Override
	public EVersionControlTool getToolId() {
		return EVersionControlTool.SVN;
	}
	
}
