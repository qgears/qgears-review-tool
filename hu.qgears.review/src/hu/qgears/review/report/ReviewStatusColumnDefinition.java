package hu.qgears.review.report;

import java.util.Comparator;

/**
 * {@link ColumnDefinition} implementation to show review status of report
 * entries.
 * 
 * @author agostoni
 * 
 */
public class ReviewStatusColumnDefinition implements ColumnDefinition {

	public class hhh implements Comparator<ReportEntry> {

		@Override
		public int compare(ReportEntry o1, ReportEntry o2) {
			// TODO Auto-generated method stub
			return 0;
		}

	}

	@Override
	public String getPropertyValue(ReportEntry obj) {
		if (obj.getReviewStatus() != null){
			return obj.getReviewStatus().toString();
		}
		return ReportEntryCSSHelper.NO_DATA;
	}

	@Override
	public String getTitle() {
		return "Review status";
	}

	@Override
	public String getEntryClass(ReportEntry e) {
		return ReportEntryCSSHelper.classForReviewStatus(e.getReviewStatus());
	}
	
	@Override
	public Comparator<ReportEntry> getComparator() {
		return new Comparator<ReportEntry>() {

			@Override
			public int compare(ReportEntry o1, ReportEntry o2) {
				int i1 = o1.getReviewStatus() == null ? -1 : o1.getReviewStatus().ordinal();
				int i2 = o2.getReviewStatus() == null ? -1 : o2.getReviewStatus().ordinal();
				return i2 -i1;
			}
		};
	}

}
