package hu.qgears.review.eclipse.ui.views.model;

import java.util.ArrayList;
import java.util.List;

import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewModel;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.model.ReviewSourceSet;

/**
 * Wrapper class for {@link ReviewSource}s, that makes easier to embed review
 * sources in a Jface content provider. Maintains a link for parent
 * {@link ReviewSourceSet}, the makes easier to traverse parent-child
 * relationships.
 * 
 * @author agostoni
 * @since 3.0
 * 
 */
public class SourceTreeElement extends AbstractViewModel<ReviewSource>{
	
	private ReviewSourceSetView parent;
	
	public SourceTreeElement(ReviewSource reviewSource, ReviewSourceSetView reviewSourceSetView) {
		super(reviewSource);
		this.parent = reviewSourceSetView;
	}

	public ReviewSourceSet getSet() {
		return parent.getModelElement();
	}

	@Override
	public List<ReviewEntryGroup> getChildren() {
		List<ReviewEntryGroup> reivewEntries = new ArrayList<ReviewEntryGroup>();
		ReviewModel reviewModel = getReviewModel(); 
		ReviewSource rs = getSource();
		
		List<ReviewEntry> current = new ArrayList<ReviewEntry>(rs.getMatchingReviewEntries(reviewModel));
		List<ReviewEntry> old = new ArrayList<ReviewEntry>(rs.getMatchingReviewEntriesPreviousVersion(reviewModel));
		List<ReviewEntry> invalidated = rs.getInValidReviewEntries(reviewModel);
		current.removeAll(invalidated);
		old.removeAll(invalidated);
	
		reivewEntries.add(new ReviewEntryGroup("Current reviews",current,this));
		reivewEntries.add(new ReviewEntryGroup("Old reviews",old,this));
		reivewEntries.add(new ReviewEntryGroup("Invalidated reviews",invalidated,this));
		return reivewEntries;
	}

	@Override
	public ReviewSourceSetView getParent() {
		return parent;
	}
	
	/**
	 * Returns the review source of this wrapper class.
	 * 
	 * @return
	 */
	public ReviewSource getSource() {
		return getModelElement();
	}
	
	public ReviewEntryView getMatchingView(ReviewEntry re) {
		 return new IReviewModelVisitor() {
			private ReviewEntryView ret;
			@Override
			public boolean visit(AbstractViewModel<?> reviewModelElement) {
				if (reviewModelElement instanceof ReviewEntryView) {
					ReviewEntryView rev = (ReviewEntryView) reviewModelElement;
					if (rev.getModelElement().equals(re)){
						ret = rev;
					}
				}
				return ret == null;
			}
			public ReviewEntryView find() {
				SourceTreeElement.this.visit(this);
				return ret;
			}
		}.find();
	}
}
