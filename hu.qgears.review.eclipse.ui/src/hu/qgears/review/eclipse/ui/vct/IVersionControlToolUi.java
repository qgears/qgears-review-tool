package hu.qgears.review.eclipse.ui.vct;

import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;
import hu.qgears.review.util.vct.EVersionControlTool;

/**
 * Interface that hides version control tool specific implementation of
 * functions, that are related to optional review tool features.
 * 
 * @author agostoni
 * @since 2.0
 *
 */
public interface IVersionControlToolUi {

	/**
	 * Opens a compare editor that compares the given revisions of the same
	 * source file.
	 * 
	 * @param r1
	 *            The review entry attached to A version of the review source.
	 * @param r2
	 *            The review entry attached the B version of the review source.
	 * @since 3.0
	 */
	void openCompareEditor(ReviewEntryView r1, ReviewEntryView r2);

	/**
	 * Opens a compare editor that compares the given revision of the source
	 * file with the actual head version.
	 * 
	 * @param modelElement
	 *            The review entry attached to current version of the review
	 *            source.
	 * @since 3.0
	 */
	void compareWithHead(ReviewEntryView modelElement);
	/**
	 * Returns the {@link EVersionControlTool} that is implemented by this class.
	 * 
	 * @return
	 */
	EVersionControlTool getToolId();

	
}
