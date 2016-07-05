package hu.qgears.review.report;

import hu.qgears.review.model.EReviewAnnotation;

/**
 * Represents the review state of a review source. Used by report generator
 * component, to report review status.
 * 
 * @author agostoni
 * 
 */
public enum ReviewStatus {
	/**
	 * The current version of source has been reviewed and accepted by more than one developer.
	 */
	OK_MORE_REVIEWERS,
	/**
	 * The current version of source has been reviewed and accepted by one developer.
	 */
	OK_ONE_REVIEWER,
	/**
	 * At least one developer has reviewed the current version of the source,
	 * and found some defects, that must be fixed.
	 */
	TODO,
	/**
	 * An earlier version of source has been reviewed by a developer.
	 */
	OLD,
	/**
	 * Nobody has been reviewed neither the current nor the earlier versions of
	 * this source yet.
	 */
	MISSING,
	/**
	 * At least one developer marked the current version of the source with
	 * {@link EReviewAnnotation#reviewOff} annotation'.
	 */
	OFF,
	/**
	 * At least one developer marked the current version of the source with
	 * {@link EReviewAnnotation#reviewWontReview} annotation'.
	 * @since 3.0
	 */
	WONT_REVIEW
}
