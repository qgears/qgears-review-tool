package hu.qgears.eclipse.ui.svn;

import hu.qgears.review.eclipse.ui.vct.IVersionControlToolUi;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.util.vct.EVersionControlTool;

public class VersionControlToolUiSVN implements IVersionControlToolUi {

	private CompareEditorImpl compareEditor = new CompareEditorImpl();
	
	public VersionControlToolUiSVN() {
		//this is an extension point implementation, so it must have a default ctor
	}

	@Override
	public void openCompareEditor(ReviewEntry r1, ReviewEntry r2) {
		compareEditor.openCompareEditor(r1,r2);
	}


	@Override
	public void compareWithHead(ReviewEntry modelElement) {
		compareEditor.compareWithHead(modelElement);
	}

	@Override
	public EVersionControlTool getToolId() {
		return EVersionControlTool.SVN;
	}
	
}
