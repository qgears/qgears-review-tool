package hu.qgears.review.report;

/**
 * Utility class for assigning CSS classes to the table cells of generated
 * report.
 * 
 * @author agostoni
 * 
 */
public class ReportEntryCSSHelper {

	private static final String CLASS_METRIC_NODATA = "metric_nodata";
	private static final String CLASS_METRIC_PERFECT = "metric_perfect";
	private static final String CLASS_METRIC_VERY_GOOD = "metric_very_good";
	private static final String CLASS_METRIC_GOOD = "metric_good";
	private static final String CLASS_METRIC_AVERAGE = "metric_average";
	private static final String CLASS_METRIC_LOW = "metric_low";
	private static final String CLASS_WONT_REVIEW = "wont_review";

	private ReportEntryCSSHelper(){}

	public  static final String NO_DATA = "N/A";
	
	/**
	 * Tries to parse the specified String as a percentage value (between
	 * 0% and 100%), and returns a CSS class based on this. 0-50 -> metric_low,
	 * 50-70 metric_average, and so on. See impl for details.
	 * 
	 * @return The CSS class name, or <code>null</code> if the value could not
	 *         be parsed.
	 */
	public static String getCSSClassByPercentage(String val){
		if (NO_DATA.equals(val)){
			return CLASS_METRIC_NODATA;
		} else {
			try {
				float numberValue = Float.parseFloat(val);
				if (numberValue < 50){
					return CLASS_METRIC_LOW;
				} else if (numberValue < 70){
					return CLASS_METRIC_AVERAGE;
				} else if (numberValue < 85){
					return CLASS_METRIC_GOOD;
				} else if (numberValue < 100){
					return CLASS_METRIC_VERY_GOOD;
				} else if (numberValue == 100){
					return CLASS_METRIC_PERFECT;
				}
			} catch (Exception e){
				
			}
		}
		return null;
	}

	/**
	 * Returns a CSS class for specified review status.
	 * 
	 * @param reviewStatus
	 * @return
	 */
	public static String classForReviewStatus(ReviewStatus reviewStatus) {
		if (reviewStatus != null){
			switch (reviewStatus) {
			case MISSING:
				return CLASS_METRIC_LOW;
			case OFF:
				return CLASS_METRIC_GOOD;
			case WONT_REVIEW:
				return CLASS_WONT_REVIEW;
			case OK_MORE_REVIEWERS:
				return CLASS_METRIC_PERFECT;
			case OK_ONE_REVIEWER:
				return CLASS_METRIC_VERY_GOOD;
			case TODO:
				return CLASS_METRIC_GOOD;
			case OLD:
				return CLASS_METRIC_AVERAGE;
			}
		}
		return CLASS_METRIC_NODATA;
	}
	
}
