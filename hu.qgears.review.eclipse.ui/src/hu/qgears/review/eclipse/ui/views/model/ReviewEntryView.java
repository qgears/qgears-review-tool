package hu.qgears.review.eclipse.ui.views.model;

import hu.qgears.review.model.ReviewEntry;

import java.util.Collections;
import java.util.List;

/**
 * View model wrapper for {@link ReviewEntry}.
 * 
 * @author agostoni
 * @see AbstractViewModel
 * @since 3.0
 */
public class ReviewEntryView extends AbstractViewModel<ReviewEntry> {

	private final ReviewEntryGroup parent;

	public ReviewEntryView(ReviewEntry modelElement, ReviewEntryGroup parent) {
		super(modelElement);
		this.parent = parent;
	}

	@Override
	public List<? extends AbstractViewModel<?>> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public ReviewEntryGroup getParent() {
		return parent;
	}

}
