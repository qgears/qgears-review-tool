package hu.qgears.review.eclipse.ui.vct;

import hu.qgears.review.eclipse.ui.util.UtilLog;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.util.vct.EVersionControlTool;

/**
 * Default implementation of {@link IVersionControlToolUi} interface that opens
 * error dialog when interface methods are called.
 * 
 * @author agostoni
 *
 */
public class DefaultVersionControlToolImplementation implements
		IVersionControlToolUi {

	private EVersionControlTool tool;

	public DefaultVersionControlToolImplementation(EVersionControlTool tool) {
		this.tool = tool;
	}

	@Override
	public void openCompareEditor(ReviewEntry r1, ReviewEntry r2) {
		openErrorDialog();
	}

	@Override
	public EVersionControlTool getToolId() {
		return tool;
	}

	private void openErrorDialog() {
		UtilLog.showErrorDialog("This function is not supported with " + tool,
				null);
	}

	@Override
	public void compareWithHead(ReviewEntry modelElement) {
		openErrorDialog();
	}
}
