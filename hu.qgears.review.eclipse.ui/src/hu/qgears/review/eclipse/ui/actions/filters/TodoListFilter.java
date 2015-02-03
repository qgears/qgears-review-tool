package hu.qgears.review.eclipse.ui.actions.filters;

import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;
import hu.qgears.review.model.ReviewEntry;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filter that selects the review sources that were not reviewed by current
 * user (this review sources must be reviewed).
 * 
 * @author agostoni
 * 
 */
public class TodoListFilter extends ViewerFilter{

	private String user;
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (user != null && !user.isEmpty()){
			if (element instanceof SourceTreeElement){
				SourceTreeElement ste = (SourceTreeElement) element;
				for (ReviewEntry re : ste.getSource().getMatchingReviewEntries(ste.getReviewModel())){
					if (user.equals(re.getUser()) && !ste.getReviewModel().isInvalidated(re.getSha1Sum())){
						//this source has been already reviewed by selected user
						return false;
					}
				}
			}
		}
		return true;
	}
}
