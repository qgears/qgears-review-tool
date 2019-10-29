package hu.qgears.review.eclipse.ui.wizard;

import hu.qgears.review.report.ReportGenerator;

/**
 * Wizard page that contains editors for the parameters of HTML report
 * generator. All public methods must be accessed before the parent wizard has
 * been closed.
 * 
 * @author agostoni
 * 
 */
public class ExportHtmlReportParametersPage extends AbstractExportReportParametersPage {


	private static final String TITLE = "Specify HTML export parameters";
	
	protected ExportHtmlReportParametersPage(ReportGenerator rg) {
		super(rg, TITLE, ".xhtml", true);
	}
	
}
