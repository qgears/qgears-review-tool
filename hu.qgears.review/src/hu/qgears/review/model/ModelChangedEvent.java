package hu.qgears.review.model;

/**
 * Event object that is passed to event listeners on Review model change events.
 * 
 * @author agostoni
 * @see ReviewModel#getReviewModelChangedEvent()
 */
public class ModelChangedEvent {

	private ReviewModel reviewModel;
	
	public ModelChangedEvent(ReviewModel reviewModel) {
		super();
		this.reviewModel = reviewModel;
	}

	public ReviewModel getReviewModel() {
		return reviewModel;
	}
}
