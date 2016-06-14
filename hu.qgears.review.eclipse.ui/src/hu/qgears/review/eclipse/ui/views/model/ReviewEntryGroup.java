package hu.qgears.review.eclipse.ui.views.model;

import hu.qgears.review.eclipse.ui.views.main.ReviewSourceContentProvier;
import hu.qgears.review.model.ReviewEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * This class can be used for grouping {@link ReviewEntry}s, and display the
 * group as an element in a {@link ReviewSourceContentProvier}.
 * <p>
 * Typical usage is to create two groups for each Review Source : one for review
 * entries of the current version, and an other for the older version.
 * 
 * @author agostoni
 * @since 3.0
 * 
 */
public class ReviewEntryGroup extends AbstractViewModel<List<ReviewEntry>>{

	private final String name;
	private final SourceTreeElement parent;
	private List<ReviewEntryView> children;

	/**
	 * 
	 * @param name The name of this group
	 * @param matchingReviewEntries The review entries assigned to this group
	 * @param rs The parent element that contains this group.
	 */
	public ReviewEntryGroup(String name,
			List<ReviewEntry> matchingReviewEntries, SourceTreeElement rs) {
		super(matchingReviewEntries);
		this.name = name;
		this.parent = rs;
	}
	
	/**
	 * Returns the name of the group.
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the parent model element that contains this group.
	 * 
	 * @return
	 */
	public SourceTreeElement getParent() {
		return parent;
	}
	
	@Override
	public List<ReviewEntryView> getChildren() {
		if (children == null){
			children = new ArrayList<ReviewEntryView>();
			for (ReviewEntry re : getModelElement()){
				children.add(new ReviewEntryView(re, this));
			}
		}
		return children;
	}
}
