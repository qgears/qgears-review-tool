package hu.qgears.review.report;

import hu.qgears.review.model.EReviewAnnotation;
import hu.qgears.review.model.ReviewEntry;
import hu.qgears.review.model.ReviewModel;
import hu.qgears.review.model.ReviewSource;

import java.util.Comparator;
import java.util.List;

/**
 * Column definition that reads comments from current valid OK entries (if any) and print into a multiline String.
 * 
 * @author agostoni
 *
 */
public class OkWithMessageColumnDefinition implements ColumnDefinition{

	private ReviewModel model;
	
	public OkWithMessageColumnDefinition(ReviewModel model) {
		super();
		this.model = model;
	}

	@Override
	public String getPropertyValue(ReportEntry obj) {
		StringBuilder bld = new StringBuilder();
		ReviewSource sourceFile = obj.getSourceFile();
		//current reviews
		List<ReviewEntry>reviews = sourceFile.getMatchingReviewEntries(model);
		for (ReviewEntry r : reviews){
			if (r.getAnnotation() == EReviewAnnotation.reviewOk
				&& !model.isInvalidated(r.getSha1Sum())
				&& r.getComment() != null 
				&& !r.getComment().isEmpty()){
				
				bld.append(String.format("%s%n",""+r.getComment()));
			}
		}
		return bld.toString();
	}


	@Override
	public String getTitle() {
		return "Review OK comments";
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
