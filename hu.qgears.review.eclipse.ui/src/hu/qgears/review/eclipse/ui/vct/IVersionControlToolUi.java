package hu.qgears.review.eclipse.ui.vct;

import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.util.vct.EVersionControlTool;

public interface IVersionControlToolUi {

	void openCompareEditor(ReviewEntry r1, ReviewEntry r2);

	EVersionControlTool getToolId();

	void compareWithHead(ReviewEntry modelElement);
	
}
