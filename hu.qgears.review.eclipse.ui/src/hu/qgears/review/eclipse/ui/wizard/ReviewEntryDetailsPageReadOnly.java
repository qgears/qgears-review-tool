package hu.qgears.review.eclipse.ui.wizard;

import hu.qgears.review.model.ReviewEntry;

import java.util.Collection;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

/**
 * Shows the same properties of a {@link ReviewEntry} as
 * {@link ReviewEntryDetailsPage} does, but in read only way.
 * 
 * @author agostoni
 * 
 */
public class ReviewEntryDetailsPageReadOnly extends ReviewEntryDetailsPage {

	private final ReviewEntry displayedEntry;

	protected ReviewEntryDetailsPageReadOnly(ReviewEntry displayedEntry,
			Collection<ReviewEntry> existingEntriesOnReviewSource) {
		super(existingEntriesOnReviewSource);
		this.displayedEntry = displayedEntry;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		String s = displayedEntry.getAnnotation().toString(); 
		for (int i = 0; i<  getAnnotationTypeCombo().getItemCount(); i++){
			if (s.equals(getAnnotationTypeCombo().getItems()[i])){
				getAnnotationTypeCombo().select(i);
				break;
			}
		}
		getAnnotationTypeCombo().setEnabled(false);
		
		getCommentText().setText(displayedEntry.getComment());
		getCommentText().setEditable(false);
		
		List<String> invalidates = displayedEntry.getInvalidates();
		if (!invalidates.isEmpty()){
			for (ReviewEntry inve : getExistingEntries()){
				getTable().setChecked(inve,invalidates.contains(inve.getSha1Sum()));
			}
		}
		getTable().getTable().setEnabled(false);
		checkPageComplete();
	}
	
	@Override
	protected void checkPageComplete() {
		setPageComplete(true);
	}
	
}
