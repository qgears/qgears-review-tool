package hu.qgears.review.report;

import hu.qgears.review.model.EReviewAnnotation;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewModel;
import hu.qgears.review.model.ReviewSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TodoMessageColumnDefinition implements ColumnDefinition {

	private ReviewModel model;
	
	public TodoMessageColumnDefinition(ReviewModel model) {
		super();
		this.model = model;
	}

	@Override
	public String getPropertyValue(ReportEntry obj) {
		StringBuilder bld = new StringBuilder();
		if (obj.getReviewStatus() == ReviewStatus.TODO){
			ReviewSource sourceFile = obj.getSourceFile();
			List<ReviewEntry> reviews = new ArrayList<ReviewEntry>();
			reviews.addAll(sourceFile.getMatchingReviewEntries(model));
			reviews.addAll(sourceFile.getMatchingReviewEntriesPreviousVersion(model));
			for (ReviewEntry r : reviews){
				if (r.getAnnotation() == EReviewAnnotation.reviewTodo && !model.isInvalidated(r.getSha1Sum())){
					bld.append(String.format("%s%n",""+r.getComment()));
				}
			}
		}
		return bld.toString();
	}


	@Override
	public String getTitle() {
		return "Existing todos";
	}

	@Override
	public String getEntryClass(ReportEntry e) {
		return null;
	}

	@Override
	public Comparator<ReportEntry> getComparator() {
		return new DefaultReportEntryComparator(this);
	}

}
