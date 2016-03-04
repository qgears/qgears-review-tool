package hu.qgears.review.eclipse.ui.vct;

import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.util.vct.EVersionControlTool;

/**
 * Interface that hides version control tool specific implementation of
 * functions, that are related to optional review tool features.
 * 
 * @author agostoni
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
	 */
	void openCompareEditor(ReviewEntry r1, ReviewEntry r2);

	/**
	 * Opens a compare editor that compares the given revision of the source
	 * file with the actual head version.
	 * 
	 * @param modelElement
	 *            The review entry attached to current version of the review
	 *            source.
	 */
	void compareWithHead(ReviewEntry modelElement);
	/**
	 * Returns the {@link EVersionControlTool} that is implemented by this class.
	 * 
	 * @return
	 */
	EVersionControlTool getToolId();

	
}
