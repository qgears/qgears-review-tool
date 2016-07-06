package hu.qgears.review.report;

import hu.qgears.review.model.EReviewAnnotation;
import hu.qgears.review.model.ReviewModel;

/**
 * Generates the comments a of a review source with status {@link ReviewStatus#WONT_REVIEW}. 
 * @author agostoni
 * @since 3.0
 */
public class WongReviewMessageColumnDefinition extends
		AbstractMessageColumnDefinition {

	public WongReviewMessageColumnDefinition(ReviewModel model) {
		super(model,ReviewStatus.WONT_REVIEW,EReviewAnnotation.reviewWontReview);
	}

	@Override
	public String getTitle() {
		return "Reason";
	}

	@Override
	public String getEntryClass(ReportEntry e) {
		return null;
	}

}
