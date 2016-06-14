package hu.qgears.review.eclipse.ui.vct;

import hu.qgears.review.eclipse.ui.util.UtilLog;
import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;
import hu.qgears.review.util.vct.EVersionControlTool;

/**
 * Default implementation of {@link IVersionControlToolUi} interface that opens
 * error dialog when interface methods are called.
 * 
 * @author agostoni
 * @since 2.0
 *
 */
public class DefaultVersionControlToolImplementation implements
		IVersionControlToolUi {

	private EVersionControlTool tool;

	public DefaultVersionControlToolImplementation(EVersionControlTool tool) {
		this.tool = tool;
	}

	/**
	 * @since 3.0
	 */
	@Override
	public void openCompareEditor(ReviewEntryView r1, ReviewEntryView r2) {
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

	/**
	 * @since 3.0
	 */
	@Override
	public void compareWithHead(ReviewEntryView modelElement) {
		openErrorDialog();
	}
}
