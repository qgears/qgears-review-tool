package hu.qgears.review.eclipse.ui.views.model;

import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewModel;
import hu.qgears.review.model.ReviewSource;

import java.util.List;

/**
 * Abstract class that represents the view model of Review tool. Wraps Review
 * tool model elements (e.g. {@link ReviewSource}, {@link ReviewEntry}), in
 * order to organize them into regular tree model, where parent-child
 * relationships are fixed.
 * <p>
 * This model fits better into JFace viewer API, which this GUI is based on, and
 * traversing model is also simplified (implementing visitor pattern is simple).
 * 
 * @author agostoni
 * 
 * @param <WrappedType>
 *            The type from Review tool data model, that is wrapped by this
 *            class.
 * @since 3.0
 */
public abstract class AbstractViewModel<WrappedType> {

	private static final int MAX_MODEL_DEPTH = 10;
	
	private WrappedType modelElement;
	
	public AbstractViewModel(WrappedType modelElement) {
		this.modelElement = modelElement;
	}
	
	/**
	 * Returns the underlying model element (ReviewSource / ReviewEntry ...).
	 * 
	 * @return
	 */
	public WrappedType getModelElement() {
		return modelElement;
	}
	
	/**
	 * Returns the direct children of this element in model tree. If this
	 * element does not have any child elements, then it must return an empty
	 * list. Should never return <code>null</code>.
	 * 
	 * @return
	 */
	public abstract List<? extends AbstractViewModel<?>> getChildren();

	/**
	 * Returns the direct parent element of this element in model tree. Must not
	 * return <code>null</code>, except this element is the root element of the
	 * model tree.
	 * 
	 * @return
	 */
	public abstract AbstractViewModel<?> getParent();
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbstractViewModel<?>){
			AbstractViewModel<?> other = (AbstractViewModel<?>) obj;
			if (modelElement == null){
				return other.modelElement == null;
			} else {
				return modelElement.equals(other.modelElement);
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		if (modelElement != null){
			return modelElement.hashCode();
		}
		return 0;
	}
	
	@Override
	public String toString() {
		if (modelElement != null){
			return modelElement.toString();
		}
		return super.toString();
	}
	
	/**
	 * Shortcut method to access the {@link ReviewModel root element} of the
	 * review tool model.
	 * 
	 * @return
	 */
	public ReviewModel getReviewModel() {
		if (getParent() != null){
			return getParent().getReviewModel();
		}
		return null;
	}
	
	/**
	 * Visit the child-elements within model element.
	 * 
	 * @param visitor the visitor to call for each model element.
	 */
	public final void visit(IReviewModelVisitor visitor){
		if (visitor != null){
			visit(visitor,this,0);
		}
	}
	
	private void visit(IReviewModelVisitor visitor,AbstractViewModel<?> o, int level){
		if (level < MAX_MODEL_DEPTH){
			boolean traverseChildren = visitor.visit(o);
			if (traverseChildren){
				for (AbstractViewModel<?> o2 : o.getChildren()){
					visit(visitor,o2,level+1);
				}
			}
		}
	}
}
