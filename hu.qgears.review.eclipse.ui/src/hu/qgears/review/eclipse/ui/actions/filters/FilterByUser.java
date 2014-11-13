package hu.qgears.review.eclipse.ui.actions.filters;

import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;
import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class FilterByUser extends ViewerFilter{

	/**
	 * Represents filter option for showing sources that was not reviewed by
	 * anyone.
	 */
	public static final String NONE_USER = "NONE";
	private Set<String> enabledUsers = new HashSet<String>();

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof SourceTreeElement){
			SourceTreeElement sourceTreeElement = (SourceTreeElement) element;
			ReviewModel root = sourceTreeElement.getReviewModel();
			List<ReviewEntry> current = sourceTreeElement.getSource().getMatchingReviewEntries(root);
			for (ReviewEntry re :current){
				if (enabled(re.getUser())){
					return true;
				}
			}
			List<ReviewEntry> old = sourceTreeElement.getSource().getMatchingReviewEntriesPreviousVersion(root);
			for (ReviewEntry re :old){
				if (enabled(re.getUser())){
					return true;
				}
			}
			return old.isEmpty() && current.isEmpty() && enabled(NONE_USER);
		}
		if (element instanceof ReviewEntryView){
			ReviewEntry reviewEntry = ((ReviewEntryView) element).getModelElement();
			return enabled(reviewEntry.getUser());
		}
		return true;
	}
	
	/**
	 * Show / hide review sources and entries that are reviewed by given user.
	 * 
	 * @param userName
	 *            A valid username (see {@link ReviewModel#getUsers()}) or
	 *            {@link #NONE_USER}.
	 * @param enabled
	 *            <code>true</code> to enable, <code>false</code> to
	 *            disableSourceCache
	 */
	public void enableUser(String userName,boolean enabled){
		if (enabled){
			enabledUsers.add(userName);
		} else {
			enabledUsers.remove(userName);
		}
	}
	
	public boolean enabled(String userName){
		return enabledUsers.contains(userName);
	}

	public void enableUsers(Set<String> users, boolean b) {
		if (b){
			enabledUsers.addAll(users);
		} else {
			enabledUsers.removeAll(users);
		}
			
	}

}
