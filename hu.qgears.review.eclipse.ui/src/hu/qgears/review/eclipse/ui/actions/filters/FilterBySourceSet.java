package hu.qgears.review.eclipse.ui.actions.filters;

import hu.qgears.review.eclipse.ui.views.model.ReviewSourceSetView;
import hu.qgears.review.model.ReviewSourceSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filter for showing / hiding specific {@link ReviewSourceSet}s.
 * 
 * @author agostoni
 * 
 */
public class FilterBySourceSet extends ViewerFilter{

	private Set<String> enabledSourceSets = new HashSet<String>();

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof ReviewSourceSetView){
			ReviewSourceSet ste = ((ReviewSourceSetView) element).getModelElement();
			return enabled(ste.id);
		}
		return true;
	}
	
	/**
	 * Show / hide sourceset.
	 * 
	 * @param sourceSet
	 *            the id of source set ({@link ReviewSourceSet#id})
	 * @param enabled
	 *            <code>true</code> to show, <code>false</code> to hide
	 */
	public void enableSourceSet(String sourceSet,boolean enabled){
		if (enabled){
			enabledSourceSets.add(sourceSet);
		} else {
			enabledSourceSets.remove(sourceSet);
		}
	}
	
	public boolean enabled(String userName){
		return enabledSourceSets.contains(userName);
	}

	public void enableSourceSets(Collection<String> users, boolean b) {
		if (b){
			enabledSourceSets.addAll(users);
		} else {
			enabledSourceSets.removeAll(users);
		}
			
	}
}
