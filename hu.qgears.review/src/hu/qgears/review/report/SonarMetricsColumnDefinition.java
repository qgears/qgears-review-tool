package hu.qgears.review.report;

import java.util.Comparator;

import hu.qgears.sonar.client.model.ResourceMetric;
import hu.qgears.sonar.client.model.SonarResource;

/**
 * {@link ColumnDefinition} implementation to query SONAR metric values (such as
 * line coverage, or IT branch coverage) from a {@link ReportEntry}.
 * 
 * @author agostoni
 * 
 */
public class SonarMetricsColumnDefinition implements ColumnDefinition {

	private final class NumbericValueComparator implements
			Comparator<ReportEntry> {
		@Override
		public int compare(ReportEntry o1, ReportEntry o2) {
			Double v1 = getValue(o1);
			Double v2 = getValue(o2);
			if (v1 == null){
				return v2 == null ? 0 : -1;
			} else if (v2 == null){
				return 1;
			} else {
				return v1.compareTo(v2);
			}
		}
		
		private Double getValue(ReportEntry o){
			ResourceMetric m = getMetric(o);
			if (m != null){
				try {
					return Double.parseDouble(m.getValue());
				} catch (Exception e) {
				}
			}
			return null;
		} 
	}

	private String title;
	private String metricsKey;
	
	/**
	 * Constructs a new instance.
	 * 
	 * @param title The title assigned to this column definition. See {@link ColumnDefinition#getTitle()}
	 * @param metricsKey The identifier of required SONAR metric
	 */
	public SonarMetricsColumnDefinition(String title, String metricsKey) {
		super();
		this.title = title;
		this.metricsKey = metricsKey;
	}

	@Override
	public String getPropertyValue(ReportEntry obj) {
		ResourceMetric m = getMetric(obj);
		if (m != null){
			return m.getFormattedValue();
		}
		return ReportEntryCSSHelper.NO_DATA;
	}

	private ResourceMetric getMetric(ReportEntry obj) {
		SonarResource res = obj.getResource();
		if (res != null){
			return res.getMetric(metricsKey);
		}
		return null;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getEntryClass(ReportEntry e) {
		ResourceMetric m = getMetric(e);
		String val = m == null ? ReportEntryCSSHelper.NO_DATA : m.getValue();
		return ReportEntryCSSHelper.getCSSClassByPercentage(val);
	}

	public String getMetricsKey() {
		return metricsKey;
	}

	@Override
	public Comparator<ReportEntry> getComparator() {
		return new NumbericValueComparator();
	}
}
