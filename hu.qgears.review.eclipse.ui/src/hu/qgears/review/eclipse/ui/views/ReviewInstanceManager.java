package hu.qgears.review.eclipse.ui.views;

import hu.qgears.commons.Pair;
import hu.qgears.commons.UtilEvent;
import hu.qgears.review.model.ReviewInstance;

/**
 * The manager maintains a {@link ReviewInstance}, and fires change events if
 * the instance has been changed.
 * <p>
 * This manager is designed to be a singleton, and can be accessed only from
 * {@link AbstractReviewToolView}.
 * 
 * @author agostoni
 * 
 */
public class ReviewInstanceManager {
	
	ReviewInstanceManager() {
		//package protected ctor
	}
	
	private ReviewInstance reviewInstace;
	private UtilEvent<Pair<ReviewInstance, ReviewInstance>> reviewInstanceChangedEvent = new UtilEvent<Pair<ReviewInstance,ReviewInstance>>();
	
	/**
	 * Returns the managed instance.
	 * 
	 * @return
	 */
	public ReviewInstance getReviewInstace() {
		return reviewInstace;
	}
	
	/**
	 * Set the managed instance, and fires change event.
	 * 
	 * @param reviewInstace
	 */
	public void setReviewInstace(ReviewInstance reviewInstace) {
		ReviewInstance old = this.reviewInstace;
		this.reviewInstace = reviewInstace;
		reviewInstanceChangedEvent.eventHappened(new Pair<ReviewInstance, ReviewInstance>(old, reviewInstace));
	}
	
	/**
	 * This event is fired when a new {@link ReviewInstance} has been set. The
	 * event parameter is a {@link Pair}, containing the old instance (
	 * {@link Pair#getA()}), and the new instance {@link Pair#getB()}.
	 * 
	 * @return
	 */
	public UtilEvent<Pair<ReviewInstance, ReviewInstance>> getReviewInstanceChangedEvent() {
		return reviewInstanceChangedEvent;
	}
}
