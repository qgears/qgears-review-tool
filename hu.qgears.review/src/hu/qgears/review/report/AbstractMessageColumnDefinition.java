package hu.qgears.review.report;

import hu.qgears.review.model.EReviewAnnotation;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewModel;
import hu.qgears.review.model.ReviewSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Abstract column definition that reads the comments from review entries and
 * joins then into as a String.
 * 
 * @since 3.0
 */
public abstract class AbstractMessageColumnDefinition implements ColumnDefinition {

	private ReviewModel model;
	private ReviewStatus status;
	private EReviewAnnotation annotation;

	public AbstractMessageColumnDefinition(ReviewModel model, ReviewStatus status,
			EReviewAnnotation annotation) {
		super();
		this.model = model;
		this.status = status;
		this.annotation = annotation;
	}


	@Override
	public String getPropertyValue(ReportEntry obj) {
		StringBuilder bld = new StringBuilder();
		if (obj.getReviewStatus() == status){
			ReviewSource sourceFile = obj.getSourceFile();
			List<ReviewEntry> reviews = new ArrayList<ReviewEntry>();
			reviews.addAll(sourceFile.getMatchingReviewEntries(model));
			reviews.addAll(sourceFile.getMatchingReviewEntriesPreviousVersion(model));
			for (ReviewEntry r : reviews){
				if (r.getAnnotation() == annotation && !model.isInvalidated(r.getSha1Sum())){
					bld.append(String.format("%s%n",""+r.getComment()));
				}
			}
		}
		if (bld.length() > 0) {
			//removing last '\n'
			bld.deleteCharAt(bld.length()-1);
		}
		return bld.toString();
	}

	@Override
	public Comparator<ReportEntry> getComparator() {
		return new DefaultReportEntryComparator(this);
	}
}
