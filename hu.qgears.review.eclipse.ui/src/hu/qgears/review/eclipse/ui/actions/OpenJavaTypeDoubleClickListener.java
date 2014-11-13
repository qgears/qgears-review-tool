package hu.qgears.review.eclipse.ui.actions;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;

/**
 * {@link IDoubleClickListener} that executes {@link OpenJavaTypeAction} on
 * target element.
 * 
 * @author agostoni
 * 
 */
public class OpenJavaTypeDoubleClickListener implements IDoubleClickListener{
	
	private OpenJavaTypeAction action;
	
	public OpenJavaTypeDoubleClickListener(OpenJavaTypeAction action) {
		this.action = action;
	}
	
	@Override
	public void doubleClick(DoubleClickEvent event) {
		action.run();
	}

}
