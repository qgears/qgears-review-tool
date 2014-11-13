package hu.qgears.review.report;

import hu.qgears.review.util.IPropertyGetter;

import java.util.Comparator;

/**
 * Default comparator for {@link ColumnDefinition}s that performs simple String
 * comparison.
 * 
 * @author agostoni
 * 
 */
public class DefaultReportEntryComparator implements Comparator<ReportEntry> {

	private IPropertyGetter<ReportEntry> getter;
	
	public DefaultReportEntryComparator(IPropertyGetter<ReportEntry> getter) {
		super();
		this.getter = getter;
	}

	@Override
	public int compare(ReportEntry o1, ReportEntry o2) {
		String p1 = "" + getter.getPropertyValue(o1);
		String p2 = "" + getter.getPropertyValue(o2);
		if (ReportEntryCSSHelper.NO_DATA.equals(p1)){
			return 1;
		}
		if (ReportEntryCSSHelper.NO_DATA.equals(p2)){
			return -1;
		}
		
		return p1.compareTo(p2);
	}
}
