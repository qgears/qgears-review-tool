package hu.qgears.review.report;

import hu.qgears.sonar.client.model.ResourceMetric;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates summary data for report.
 * 
 * @author agostoni
 *
 */
public class ReviewStatsSummary {

	private Map<ReviewStatus,Integer> numbers = new HashMap<ReviewStatus, Integer>(); 
	private int size;
	private Map<String,Float>  metricsAVGs = new HashMap<String, Float>();
	private float overallProgress;
	private ReviewStatsSummary(){}
	
	/**
	 * Creates a new instance, and initializes content from reportentries.
	 * 
	 * @param entries
	 * @return
	 */
	public static ReviewStatsSummary create(List<ReportEntry> entries){
		int ok2, ok, missing, off, old, todo,wontreview;
		ok2= ok= missing= off= old= todo = wontreview = 0;
		ReviewStatsSummary ret = new ReviewStatsSummary();
		Map<String,Integer> resourceCount = new HashMap<String, Integer>();
		long sumSizeBytes = 0;
		long okSizeBytes = 0;
		for (ReportEntry e : entries){
			if (e.getReviewStatus() != null){
				switch (e.getReviewStatus()) {
				case OK_MORE_REVIEWERS:
					okSizeBytes += fileSize(e);
					ok2++; break;
				case OK_ONE_REVIEWER:
					okSizeBytes += fileSize(e);
					ok++; break;
				case MISSING:
					missing++; break;
				case OFF:
					okSizeBytes += fileSize(e);
					off ++; break;
				case OLD:
					old++; break;
				case TODO:
					todo++; break;
				case WONT_REVIEW:
					wontreview ++;
					break;
				default:
					break;
				}
			}
			if (e.getResource() != null){
				for (ResourceMetric m : e.getResource().getMetrics()){
					String metricKey = m.getMetricKey();
					int c = resourceCount.get(metricKey) == null ? 0 : resourceCount.get(metricKey);
					resourceCount.put(metricKey, c+1);
					try {
						float val = Float.parseFloat(m.getValue());
						float current = ret.metricsAVGs.get(metricKey) == null ? 0 : ret.metricsAVGs.get(metricKey); 
						ret.metricsAVGs.put(metricKey, val+current);
					} catch (Exception ex){
						
					}
				}
			}
			sumSizeBytes += fileSize(e);
		}
		for (String key : ret.metricsAVGs.keySet()){
			ret.metricsAVGs.put(key, ret.metricsAVGs.get(key)/resourceCount.get(key));
		}
		
		ret.size = entries.size();
		ret.numbers.put(ReviewStatus.OK_MORE_REVIEWERS,ok2);
		ret.numbers.put(ReviewStatus.OK_ONE_REVIEWER,ok);
		ret.numbers.put(ReviewStatus.MISSING,missing);
		ret.numbers.put(ReviewStatus.OFF,off);
		ret.numbers.put(ReviewStatus.OLD,old);
		ret.numbers.put(ReviewStatus.TODO,todo);
		ret.numbers.put(ReviewStatus.WONT_REVIEW,wontreview);
		ret.overallProgress = (100f * okSizeBytes) / sumSizeBytes;
		return ret;
	}

	private static long fileSize(ReportEntry e) {
		if (e.getSourceFile() != null && e.getSourceFile().getFileInWorkingCopy() != null){
			return e.getSourceFile().getFileInWorkingCopy().length();
		}
		return 0;
	}

	/**
	 * Returns the proportion of specified number and the number of all report
	 * entries, expressed as a percentage value (0% - 100%).
	 * 
	 * @param count
	 * @return
	 */
	public float asPercentage(int count){
		if (size == 0){
			return 0;
		} else {
			return (100f * count) / size;
		}
	}

	/**
	 * Returns a map that contains the frequency of {@link ReviewStatus} states
	 * within the input review entry set.
	 * 
	 * @return
	 */
	public Map<ReviewStatus, Integer> getReviewStatusSummary() {
		return numbers;
	}
	
	/**
	 * Returns the average value of SONAR metrics attached to report entries.
	 * The key of the Map is the {@link ResourceMetric#getMetricKey() metric
	 * identifier}, and value is the corresponding avg.
	 * 
	 * @return
	 */
	public Map<String, Float> getMetricsAVGs() {
		return metricsAVGs;
	}

	/**
	 * Returns the frequency of {@link ReviewStatus#OK_MORE_REVIEWERS} and
	 * {@link ReviewStatus#OK_ONE_REVIEWER} review statuses.
	 * 
	 * @return
	 */
	public int getReviewOkCount() {
		Integer i1 = getReviewStatusSummary().get(ReviewStatus.OK_MORE_REVIEWERS);
		Integer i2 = getReviewStatusSummary().get(ReviewStatus.OK_ONE_REVIEWER);
		return (i1 == null ? 0 : i1.intValue()) + (i2 == null ? 0 : i2.intValue());
	}
	
	/**
	 * Returns the count of report entries.
	 * 
	 * @return
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * This value indicates overall progress of review on target review source
	 * set. The value is calculated based on file sizes (proportion of sources
	 * to review, and the sources that was already reviewed).
	 * 
	 * @return
	 */
	public float getOverallProgress() {
		return overallProgress;
	}
	
}
