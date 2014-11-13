package hu.qgears.review.eclipse.ui.views.main;

import hu.qgears.review.eclipse.ui.views.model.AbstractViewModel;
import hu.qgears.review.model.ReviewModel;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ReviewSourceContentProvier implements ITreeContentProvider {

	private Object input;
	
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (oldInput != newInput){
			this.input = newInput;
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IStatus){
			return new Object[]{inputElement};
		} else {
			return getChildren(inputElement);
		}
	}

	@Override
	public Object[] getChildren(Object element) {
		if (element instanceof AbstractViewModel<?>){
			return ((AbstractViewModel<?>) element).getChildren().toArray();
		}
		return new Object[]{};
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof AbstractViewModel<?>){
			return ((AbstractViewModel<?>) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		Object[] ch = getChildren(element);
		return ch != null && ch.length >0;
	}
	
	/**
	 * Returns the currently set review model.
	 * 
	 * @return
	 */
	public ReviewModel getReviewModel() {
		if (input != null && input instanceof AbstractViewModel<?>){
			return ((AbstractViewModel<?>) input).getReviewModel();
		}
		return null;
	}
}
