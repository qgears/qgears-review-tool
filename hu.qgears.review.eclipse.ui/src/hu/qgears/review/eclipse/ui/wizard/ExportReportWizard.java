package hu.qgears.review.eclipse.ui.wizard;

import hu.qgears.review.model.ReviewModel;
import hu.qgears.review.model.ReviewSourceSet;
import hu.qgears.review.report.ColumnDefinition;
import hu.qgears.review.report.ReportEntry;
import hu.qgears.review.report.ReportGenerator;
import hu.qgears.review.report.ReportGeneratorTemplate;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard for collection HTML report generation paramters from user.
 * 
 * @author agostoni
 *
 */
public class ExportReportWizard extends Wizard{

	/**
	 * Optimization : do not query report results again, if they are already
	 * available. This class extends {@link ReportGenerator} returning a list of
	 * previously generated report entries.
	 * 
	 * @author agostoni
	 * 
	 */
	private class WrappedReportGenerator extends ReportGenerator{

		private List<ReportEntry> entries;

		public WrappedReportGenerator(ReviewModel modelRoot,
				ReviewSourceSet targetSourceSet, List<ReportEntry> entries) {
			super(modelRoot, targetSourceSet);
			this.entries = entries;
		}
		
		@Override
		protected List<ReportEntry> generateReportEntries() {
			return entries == null ? Collections.<ReportEntry>emptyList() : entries;
		}
	}
	
	private ReportGenerator reportGenerator;
	private AbstractExportReportParametersPage exportHtmlPage;
	private boolean generateCss;
	private File targetFile;
	private boolean generateSonarStats;
	private boolean generateReviewStats;
	private boolean generateTodoList;
	private boolean isHtml;

	/**
	 * Creates a new wizard.
	 * 
	 * @param model
	 *            The {@link ReviewModel} as input
	 * @param targetSourceSet
	 *            The input {@link ReviewSourceSet}, which about the report will
	 *            be created.
	 * @param existingEntries
	 *            The existing report entries, or <code>null</code> if the
	 *            report must be regenerated.
	 */
	public ExportReportWizard(ReviewModel model,
			ReviewSourceSet targetSourceSet,List<ReportEntry> existingEntries, boolean isHtml) {
		super();
		this.isHtml = isHtml;
		if (existingEntries == null || existingEntries.isEmpty()){
			reportGenerator = new ReportGenerator(model, targetSourceSet);
		} else {
			reportGenerator = new WrappedReportGenerator(model, targetSourceSet,existingEntries);
		}
	}

	@Override
	public boolean performFinish() {
		List<ColumnDefinition> def = exportHtmlPage.getSelectedColumnDefinitions();
		reportGenerator.setColumnDefinitions(def);
		reportGenerator.setOrderBy(exportHtmlPage.getOrderByColumn(), exportHtmlPage.getOrderByDirection());
		generateCss = exportHtmlPage.getGenerateCSS();
		targetFile = new File(exportHtmlPage.getTargetFilePath());
		generateReviewStats = exportHtmlPage.mustGenerateReviewStats();
		generateSonarStats = exportHtmlPage.mustGenerateSonarStats();
		generateTodoList = exportHtmlPage.mustGenerateTodoList();
		return true;
	}

	@Override
	public void addPages() {
		if (isHtml) {
			addPage(exportHtmlPage = new ExportHtmlReportParametersPage(reportGenerator));
		} else  {
			addPage(exportHtmlPage = new ExportODSReportParametersPage(reportGenerator));
		}
	}

	/**
	 * The parameterized report generator, that must be passed to HTML generator
	 * template.
	 * 
	 * @return
	 */
	public ReportGenerator getReportGenerator() {
		return reportGenerator;
	}
	
	/**
	 * If true, than a CSS stylesheet must be generated for HTML document.
	 * @return
	 */
	public boolean mustGenerateCss() {
		return generateCss;
	}
	
	/**
	 * The target file, where results will be written.
	 * 
	 * @return
	 */
	public File getTargetFile() {
		return targetFile;
	}
	
	/**
	 * If true, that the HTML output must include a summary about review status and progress.
	 * 
	 * @return
	 */
	public boolean mustGenerateReviewStats(){
		return generateReviewStats;
	}
	
	/**
	 * If true, that the HTML output must include a summary about SONAR reports.
	 * 
	 * @return
	 */
	public boolean mustGenerateSonarStats(){
		return generateSonarStats;
		
	}

	/**
	 * See {@link ReportGeneratorTemplate#setRenderTodos(boolean)}.
	 * 
	 * @return
	 */
	public boolean mustGenerateTodoList() {
		return generateTodoList;
	}
}
