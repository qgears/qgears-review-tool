package hu.qgears.review.eclipse.ui.views.model;

import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.model.ReviewModel;
import hu.qgears.review.model.ReviewSourceSet;

import java.util.ArrayList;
import java.util.List;

/**
 * View model wrapper for {@link ReviewModel}.
 * 
 * @author agostoni
 * 
 * @see AbstractViewModel
 * @since 3.0
 */
public class ReviewModelView extends AbstractViewModel<ReviewModel>{
	
	private final ReviewInstance reviewInstance;

	public ReviewModelView(ReviewInstance instance) {
		super(instance.getModel());
		this.reviewInstance = instance;
	}

	@Override
	public List<ReviewSourceSetView> getChildren() {
		List<ReviewSourceSetView> ret = new ArrayList<ReviewSourceSetView>();
		for (ReviewSourceSet s :getModelElement().sourcesets.values()){
			ret.add(new ReviewSourceSetView(s,this));
		};
		return ret;
	}

	@Override
	public AbstractViewModel<?> getParent() {
		//root element, does not have parent
		return null;
	}
	
	@Override
	public ReviewModel getReviewModel() {
		return getModelElement();
	}

	/**
	 * Return the {@link ReviewInstance}. which belongs to the underlying
	 * {@link ReviewModel}.
	 * 
	 * @return
	 */
	public ReviewInstance getReviewInstance() {
		return reviewInstance;
	}
}
