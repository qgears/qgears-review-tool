package hu.qgears.review.report;

import hu.qgears.review.web.AbstractRender;

import java.util.Comparator;

/**
 * {@link ColumnDefinition} implementation, which generates direct links for
 * report entries, to navigate SONAR and to review tool pages.
 * 
 * @author agostoni
 * 
 */
public class LinksColumnDefinition implements ColumnDefinition {

	@Override
	public String getPropertyValue(ReportEntry obj) {
		String ret = AbstractRender.link(obj.getSourceFileLink(), "[review]");
		if (obj.getSonarLink() != null){
			ret += AbstractRender.link(obj.getSonarLink(), " [SONAR]");
		}
		return ret;
	}

	@Override
	public String getTitle() {
		return "Navigate to...";
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
