package hu.qgears.review.eclipse.ui.wizard;

import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewModel;
import hu.qgears.review.model.ReviewSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard, that shows the properties of an existing {@link ReviewEntry} in read
 * only mode.
 * 
 * @author agostoni
 * 
 * @see ReviewEntryDetailsPageReadOnly
 * @see ReviewEntryDetailsOptionalPageReadOnly
 */
public class OpenReviewEntryDetailsWizard extends Wizard {

	private final ReviewEntry displayedEntry;
	private final ReviewModel model;

	@Override
	public boolean performFinish() {
		return true;
	}

	public OpenReviewEntryDetailsWizard(ReviewEntry displayedEntry, ReviewModel model) {
		this.displayedEntry = displayedEntry;
		this.model = model;
		setWindowTitle("Review entry details (read only)");
	}
	
	@Override
	public void addPages() {
		ReviewSource reviewSource = displayedEntry.getReviewSource(model);
		Collection<ReviewEntry> existingEntries;
		String description = "Review entry details on source: "; 
		if (reviewSource != null){
			description +=reviewSource.getFullyQualifiedJavaName();
			existingEntries = new ArrayList<ReviewEntry>(
					model.getReviewEntryByUrl().getMappedObjects(reviewSource.getSourceUrl())
			);
			existingEntries.remove(displayedEntry);
		} else {
			description += "null";
			existingEntries = Collections.emptyList();
		}
		
		ReviewEntryDetailsPageReadOnly p1 = new ReviewEntryDetailsPageReadOnly(displayedEntry,existingEntries);
		p1.setDescription(description);
		addPage(p1);

		ReviewEntryDetailsOptionalPageReadOnly p2 = new ReviewEntryDetailsOptionalPageReadOnly(displayedEntry);
		p2.setDescription(description);
		addPage(p2);
	}
}
