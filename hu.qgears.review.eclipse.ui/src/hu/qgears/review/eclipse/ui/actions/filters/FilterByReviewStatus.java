package hu.qgears.review.eclipse.ui.actions.filters;

import hu.qgears.review.eclipse.ui.views.model.SourceTreeElement;
import hu.qgears.review.report.ReportGenerator;
import hu.qgears.review.report.ReviewStatus;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filters review sources by {@link ReviewStatus}.
 * 
 * @author agostoni
 * 
 */
public class FilterByReviewStatus extends ViewerFilter {
	private Set<ReviewStatus> enabledStatus = new HashSet<ReviewStatus>();
	
	public FilterByReviewStatus() {
		super();
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof SourceTreeElement){
			SourceTreeElement sourceTreeElement = (SourceTreeElement) element;
			ReviewStatus stat = ReportGenerator.getReviewStatus(sourceTreeElement.getSource(), sourceTreeElement.getReviewModel());
			return enabledStatus.contains(stat);
		}
		return true;
	}
	
	/**
	 * Show / hide source elements with specified type.
	 * 
	 * @param status
	 *            A valid string literal from {@link ReviewStatus} enum.
	 * @param enabled
	 *            <code>true</code> to show, <code>false</code> to hide
	 */
	public void enableStatus(ReviewStatus status,boolean enabled){
		if (enabled){
			enabledStatus.add(status);
		} else {
			enabledStatus.remove(status);
		}
	}
	
	public boolean enabled (ReviewStatus annot){
		return enabledStatus.contains(annot);
	}
	
}
