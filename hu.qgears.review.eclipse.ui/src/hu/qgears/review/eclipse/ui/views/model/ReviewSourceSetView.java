package hu.qgears.review.eclipse.ui.views.model;

import hu.qgears.review.model.ReviewSourceSet;

import java.util.ArrayList;
import java.util.List;

/**
 * View model wrapper for {@link ReviewSourceSet}.
 * 
 * @author agostoni
 * 
 * @see AbstractViewModel
 */
public class ReviewSourceSetView extends AbstractViewModel<ReviewSourceSet>{

	private final ReviewModelView parent;

	public ReviewSourceSetView(ReviewSourceSet modelElement,ReviewModelView parent) {
		super(modelElement);
		this.parent = parent;
	}

	@Override
	public List<SourceTreeElement> getChildren() {
		List<SourceTreeElement> elements = new ArrayList<SourceTreeElement>();
		for (String s:getModelElement().sourceFiles){
			elements.add(new SourceTreeElement(getReviewModel().getSource(s),this));
		}
		return elements;
	}

	@Override
	public ReviewModelView getParent() {
		return parent;
	}

}
