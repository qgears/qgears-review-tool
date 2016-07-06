package hu.qgears.review.report;

import hu.qgears.commons.UtilFile;
import hu.qgears.review.util.UtilHtml;

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
	
	private static final String CHARSET = "UTF-8";
	private static final String DOC_TYPE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n";
	private static final String HTML_START_TEMPLATE = DOC_TYPE +"<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta charset=\""
			+ CHARSET + "\"/><style>%s</style></head><body>";
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
	 * @param generateCss
	 * 
	 * @throws Exception
	 * @since 2.0
	 */
	public void generateReport(ReportGenerator reportGenerator,File outputFile, boolean generateReviewStats, boolean generateSonarStats, boolean generateTodoList,boolean generateCss) throws Exception{
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outputFile, CHARSET);
			String style = generateCss ? readStyle() : "";
			writer.write(String.format(HTML_START_TEMPLATE, style));
			ReportGeneratorTemplate template = new ReportGeneratorTemplate(writer, reportGenerator,false);
			template.setRenderReviewStats(generateReviewStats);
			template.setRenderSonarStats(generateSonarStats);
			template.setRenderTodos(generateTodoList);
			template.generate();
			writer.write(HTML_END);
		} finally {
			if (writer != null){
				writer.close();
			}
		}
	}
	
	private static String readStyle() throws IOException{
		return UtilFile.loadAsString(UtilHtml.getStyle());
	}

}
