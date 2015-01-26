package hu.qgears.review.report;

import hu.qgears.commons.UtilFile;
import hu.qgears.review.web.HandleReport;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Generates a HTML page, that includes a review statistic report.
 * 
 * @author agostoni
 *
 */
public class ReportGeneratorHtml {
	
	private static final String STYLE_CSS = "style.css";
	private static final String HTML_START = "<html><head><link rel=\"stylesheet\" type=\"text/css\" href=\""+STYLE_CSS+"\"></head><body>";
	private static final String HTML_END = "</body></html>";

	/**
	 * Generates the HTML report base using specified report generator instance
	 * to collect report entries. The results will be saved into specified file.
	 * 
	 * @param reportGenerator The report generator for querying report entries
	 * @param outputFile The target file
	 * @param generateReviewStats If true than generates summary about review status
	 * @param generateSonarStats If true than generates summary about SONAR metrics
	 * @param generateTodoList see {@link ReportGeneratorTemplate#setRenderTodos(boolean)}
	 * 
	 * @throws Exception
	 */
	public void generateReport(ReportGenerator reportGenerator,File outputFile, boolean generateReviewStats, boolean generateSonarStats, boolean generateTodoList) throws Exception{
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outputFile, "UTF-8");
			
			writer.write(HTML_START);
			ReportGeneratorTemplate template = new ReportGeneratorTemplate(writer, reportGenerator,false);
			template.setRenderReviewStats(generateReviewStats);
			template.setRenderSonarStats(generateSonarStats);
			template.setRenderTodos(generateTodoList);
			writer.write(HTML_END);
			template.generate();
		} finally {
			if (writer != null){
				writer.close();
			}
		}
	}
	
	
	/**
	 * Copies a CSS style sheet into specified folder. The styles are used by
	 * generated HTML documents.
	 * 
	 * @param outputFolder
	 * @throws IOException
	 */
	public void copyStyle(File outputFolder) throws IOException {
		File style = new File(outputFolder,STYLE_CSS);
		UtilFile.copyFileFromUrl(style,HandleReport.class.getResource(STYLE_CSS));
	}
}
