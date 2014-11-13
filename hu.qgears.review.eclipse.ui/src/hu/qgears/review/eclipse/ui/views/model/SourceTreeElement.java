package hu.qgears.review.eclipse.ui.views.model;

import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewModel;
import hu.qgears.review.model.ReviewSource;
import hu.qgears.review.model.ReviewSourceSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for {@link ReviewSource}s, that makes easier to embed review
 * sources in a Jface content provider. Maintains a link for parent
 * {@link ReviewSourceSet}, the makes easier to traverse parent-child
 * relationships.
 * 
 * @author agostoni
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
		List<ReviewEntry> invalidated = new ArrayList<ReviewEntry>();
		for (ReviewEntry re : current){
			if (reviewModel.isInvalidated(re.getSha1Sum())){
				invalidated.add(re);
			}
		}
		for (ReviewEntry re : old){
			if (reviewModel.isInvalidated(re.getSha1Sum())){
				invalidated.add(re);
			}
		}
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
}
