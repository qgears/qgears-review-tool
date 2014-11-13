package hu.qgears.review.eclipse.ui.actions.filters;

import hu.qgears.review.eclipse.ui.views.model.ReviewEntryView;
import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;
import hu.qgears.review.model.EReviewAnnotation;
import hu.qgears.review.model.ReviewEntry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


/**
 * Viewer filter that can be used select or hide review sources and review
 * entries, based on review entry type ({@link ReviewEntry#getAnnotation()})
 * 
 * @author agostoni
 * 
 */
public class FilterByReviewAnnotation extends ViewerFilter{

	private Set<String> enabledAnnots = new HashSet<String>();
	/**
	 * Special type, for showing review sources that have not been reviewed at
	 * all.
	 */
	public static final String NOT_REVIEWED ="not reviewed";
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof SourceTreeElement){
			SourceTreeElement sourceTreeElement = (SourceTreeElement) element;
			for (ReviewEntry re :sourceTreeElement.getSource().getMatchingReviewEntries(sourceTreeElement.getReviewModel())){
				if (enabled(re.getAnnotation().toString())){
					return true;
				}
			}
			for (ReviewEntry re :sourceTreeElement.getSource().getMatchingReviewEntriesPreviousVersion(sourceTreeElement.getReviewModel())){
				if (enabled(re.getAnnotation().toString())){
					return true;
				}
			}
			return enabledAnnots.contains(NOT_REVIEWED);
		}
		if (element instanceof ReviewEntryView){
			ReviewEntry ste = ((ReviewEntryView) element).getModelElement();
			return enabled(ste.getAnnotation().toString());
		}
		return true;
	}
	
	/**
	 * Show / hide review entries with specified annotation type.
	 * 
	 * @param annot
	 *            A String literal from {@link EReviewAnnotation} enum or
	 *            {@link #NOT_REVIEWED}
	 * 
	 * @param enabled
	 *            <code>true</code> to show, <code>false</code> to hide.
	 */
	public void enableAnnotation(String annot,boolean enabled){
		if (enabled){
			enabledAnnots.add(annot);
		} else {
			enabledAnnots.remove(annot);
		}
	}
	
	public boolean enabled (String annot){
		return enabledAnnots.contains(annot);
	}
	
	public void enableAnnotations(Collection<String> annots, boolean b) {
		if (b){
			enabledAnnots.addAll(annots);
		} else {
			enabledAnnots.removeAll(annots);
		}
	}
}
