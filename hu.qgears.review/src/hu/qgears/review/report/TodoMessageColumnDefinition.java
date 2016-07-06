package hu.qgears.review.report;

import hu.qgears.review.model.EReviewAnnotation;
import hu.qgears.review.model.ReviewModel;

/**
 * Column definition that reads comment from valid review todos and prints into
 * a multi-line String.
 * 
 * @author agostoni
 * 
 */
public class TodoMessageColumnDefinition extends AbstractMessageColumnDefinition {


	public TodoMessageColumnDefinition(ReviewModel model) {
		super(model, ReviewStatus.TODO,EReviewAnnotation.reviewTodo);
	}

	@Override
	public String getTitle() {
		return "Existing todos";
	}

	@Override
	public String getEntryClass(ReportEntry e) {
		return null;
	}

}
