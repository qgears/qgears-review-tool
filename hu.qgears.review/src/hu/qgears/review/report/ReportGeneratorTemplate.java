package hu.qgears.review.report;

import hu.qgears.review.web.HandleReport;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a HTML table from data provided by {@link ReportGenerator}.
 * 
 * @author agostoni
 * 
 */
public class ReportGeneratorTemplate {

	private PrintWriter rtout, rtcout;
	private final List<ReportEntry> entries;
	private List<ColumnDefinition> columnDefinitions;
	private String title;
	private final boolean renderLinks;

	/**
	 * @param writer
	 *            The target {@link PrintWriter} of template instance
	 * @param rg
	 *            The {@link ReportGenerator}, which the data is read from
	 * @param renderLinks
	 *            If <code>true</code> renders interactive elements in report
	 *            (useful when template is called by the web application).
	 *            Otherwise only static code will be generated, without any
	 *            links.
	 */
	public ReportGeneratorTemplate(PrintWriter writer, ReportGenerator rg,boolean renderLinks) {
		this.renderLinks = renderLinks;
		this.entries = rg.collectReportEntries();
		columnDefinitions = new ArrayList<ColumnDefinition>();
		if (renderLinks){
			columnDefinitions.add(new LinksColumnDefinition());
		}
		columnDefinitions.addAll(rg.getColumnDefinitions());
		this.title = rg.getTitle();
		rtout = rtcout = writer;
	}

	public void generate() {
		rtout.write("\t<h1>");
		rtcout.write(getTitle());
		rtout.write("</h1>\n");
		ReviewStatsSummary stats = ReviewStatsSummary.create(entries);
		generateReviewSummary(stats);
		generateSonarMetricSummary(stats);
		rtout.write("\t\t<h3>Detailed statistics</h3>\n\t\t<table>\t\t\t\n\t\t\t<tr>\n");
		int i = 0;
		for (ColumnDefinition cd : columnDefinitions) {
			rtout.write("\t\t\t\t<th>");
			rtcout.write(cd.getTitle());
			rtout.write("\n");
			if (renderLinks){
				rtout.write("\t\t\t\t\t\t<a href=\"?");
				rtcout.write(HandleReport.Q_PARAM_ORDER_BY);
				rtout.write("=");
				rtcout.write(String.valueOf(i));
				rtout.write("&");
				rtcout.write(HandleReport.Q_PARAM_ORDER_ASC);
				rtout.write("=false\">&#8595;</a> <a href=\"?");
				rtcout.write(HandleReport.Q_PARAM_ORDER_BY);
				rtout.write("=");
				rtcout.write(String.valueOf(i));
				rtout.write("&");
				rtcout.write(HandleReport.Q_PARAM_ORDER_ASC);
				rtout.write("=true\">&#8593;</a>\n");
			}
			rtout.write("\t\t\t\t\t</th>\n");
			i++;
		}
		rtout.write("\t\t\t</tr>\n");
		for (ReportEntry entry : entries) {
			generateEntry(entry);
		}
		rtout.write("\t\t</table>\n");
	}

	/**
	 * Generates a summary on top of the page, that is the abstract of the
	 * detailed statistics.
	 */
	private void generateReviewSummary(ReviewStatsSummary stats) {
		float progressByFileCount = stats.asPercentage(stats.getReviewOkCount());
		String cssClazzReviewStats = ReportEntryCSSHelper.getCSSClassByPercentage(""+progressByFileCount);
		rtout.write("\t\t<h3>Review statistics</h3>\n\t\t\t<p>\n\t\t\t\tProgress (estimation based on the sum of reviewed source files' size)\n\t\t\t</p>\n\t\t\t<p>\n\t\t\t\t<progress value=\"");
		rtcout.write(String.valueOf(stats.getOverallProgress()));
		rtout.write("\" max=\"100\" ></progress> (");
		rtcout.write(String.valueOf(stats.getOverallProgress()));
		rtout.write(" %)\n\t\t\t</p>\n\n\t\t\t<table>\n");
		for (ReviewStatus st : ReviewStatus.values()){
			String count;
			String asPercentage;
			if (stats.getReviewStatusSummary().containsKey(st)){
				int countI = stats.getReviewStatusSummary().get(st);
				count =""+ countI;
				asPercentage =  String.format("%.2f %%",stats.asPercentage(countI));
			} else {
				count = asPercentage = ReportEntryCSSHelper.NO_DATA;
			}
			rtout.write("\t\t\t\t<tr ");
			rtcout.write(generateCssClassDef(cssClazzReviewStats));
			rtout.write("><td>");
			rtcout.write(st.toString());
			rtout.write("</td><td>");
			rtcout.write(String.valueOf(count));
			rtout.write(" files </td><td>");
			rtcout.write(asPercentage);
			rtout.write("</td></tr>\n");
		}
		rtout.write("\t\t\t</table>\n");
	}

	protected void generateSonarMetricSummary(ReviewStatsSummary stats) {
		rtout.write("\t\t<h3>SONAR metric statistics</h3>\n");
		if (stats.getMetricsAVGs().isEmpty()){
		rtout.write("\t\t\t\t<h4>SONAR stats completely missing! Check mapping file whether SONAR configuration is OK!</h4>\n");
		}
		rtout.write("\t\t\t<table>\n");
		for (ColumnDefinition cd : columnDefinitions){
			if (cd instanceof SonarMetricsColumnDefinition){
				SonarMetricsColumnDefinition smcd = (SonarMetricsColumnDefinition) cd;
				String formatted;
				String value;
				if (stats.getMetricsAVGs().containsKey(smcd.getMetricsKey())){
					float avgF = stats.getMetricsAVGs().get(smcd.getMetricsKey());
					formatted = String.format("%.2f %%",avgF);
					value = ""+avgF;
				} else {
					formatted = value = ReportEntryCSSHelper.NO_DATA;
				}
				rtout.write("\t\t\t\t\t<tr ");
				rtcout.write(generateCssClassDef(ReportEntryCSSHelper.getCSSClassByPercentage(value)));
				rtout.write(" ><td>");
				rtcout.write(smcd.getTitle());
				rtout.write(" [AVG]</td><td>");
				rtcout.write(formatted);
				rtout.write("</td></tr>\n");
			}
		}
		rtout.write("\t\t\t</table>\n");
	}

	/**
	 * Returns a 'class' HTML attribute that refers the given CSS class. If
	 * input is <code>null</code>, than an empty String is returned
	 * 
	 * @param cssClass
	 * @return
	 */
	private String generateCssClassDef(String cssClass) {
		if (cssClass == null){
			return "";
		} else {
			return "class=\""+cssClass+"\"";
		}
	} 

	/**
	 * Generates the details of a {@link ReportEntry}.
	 * 
	 * @param entry
	 */
	private void generateEntry(ReportEntry entry) {
		rtout.write("\t\t\t<tr>\n");
		for (ColumnDefinition cd : columnDefinitions) {
				rtout.write("\t\t\t\t<td ");
				rtcout.write(generateCssClassDef(cd.getEntryClass(entry)));
				rtout.write(">");
				rtcout.write(cd.getPropertyValue(entry));
				rtout.write("</td>\n");
		}
		rtout.write("\t\t\t</tr>\n");
	}

	private String getTitle() {
		return title;
	}

}
