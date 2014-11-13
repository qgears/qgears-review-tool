package hu.qgears.review.eclipse.ui.actions;

import hu.qgears.review.eclipse.ui.ReviewToolImages;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.Viewer;

/**
 * Action that refreshes a given viewer (calls only {@link Viewer#refresh()}).
 * 
 * @author agostoni
 * 
 */
public class RefreshViewerAction extends Action
{
	private final Viewer viewerToRefresh;

	public RefreshViewerAction(Viewer viewerToRefresh) {
		this.viewerToRefresh = viewerToRefresh;
		setText("Refresh viewer");
		setImageDescriptor(ReviewToolImages.getImageDescriptor(ReviewToolImages.ICON_REFRESH));
	}

	@Override
	public boolean isEnabled() {
		return viewerToRefresh != null;
	}
	
	@Override
	public void run() {
		if (viewerToRefresh != null){
			viewerToRefresh.refresh();
		}
	}
}
