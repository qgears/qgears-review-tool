package hu.qgears.review.eclipse.ui.actions;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;

/**
 * Opens {@link OpenReviewEntryDetailsAction} on double click event. This will
 * open a wizard showing the properties of currently selected review entry in
 * read only mode.
 * 
 * @author agostoni
 * 
 */
public class OpenReviewEntryDetailsDoubleClickListener implements IDoubleClickListener {

	private OpenReviewEntryDetailsAction action;

	public OpenReviewEntryDetailsDoubleClickListener(
			OpenReviewEntryDetailsAction action) {
		super();
		this.action = action;
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		action.run();
	}
	

}
