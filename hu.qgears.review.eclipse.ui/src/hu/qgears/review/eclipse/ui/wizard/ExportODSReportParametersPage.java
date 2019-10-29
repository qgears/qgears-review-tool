package hu.qgears.review.eclipse.ui.wizard;

import hu.qgears.review.report.ReportGenerator;

public class ExportODSReportParametersPage extends AbstractExportReportParametersPage {

	protected ExportODSReportParametersPage(ReportGenerator rg) {
		super(rg, "Specify ODS export parameters", ".ods",false);
	}

}
