package hu.qgears.review.eclipse.ui.views.model;

/**
 * Visitor interface for traversing review model elements
 * 
 * @author agostoni
 * 
 * @since 3.0
 * 
 */
public interface IReviewModelVisitor {
	/**
	 * Visits the specified model object.
	 * 
	 * @param reviewModelElement The model object from this content provider
	 * @return <code>true</code> if the child elements must be traversed,
	 *         <code>false</code> if children must be skipped.
	 */
	public boolean visit(AbstractViewModel<?> reviewModelElement);
		
}
