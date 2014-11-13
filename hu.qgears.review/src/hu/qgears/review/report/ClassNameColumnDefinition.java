package hu.qgears.review.report;

import java.util.Comparator;

/**
 * {@link ColumnDefinition} implementation that access to the simple name of
 * Java resource.
 * 
 * @author agostoni
 * 
 */
public class ClassNameColumnDefinition implements ColumnDefinition {

	@Override
	public String getPropertyValue(ReportEntry obj) {
		return obj.getSourceFile().getFullyQualifiedJavaName();
	}

	@Override
	public String getTitle() {
		return "Class name";
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
