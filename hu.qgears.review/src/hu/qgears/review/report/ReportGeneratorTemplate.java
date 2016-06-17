package hu.qgears.review.report;

import hu.qgears.review.model.ReviewModel;
import hu.qgears.review.util.UtilHtml;

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
	private boolean renderReviewStats = true;
	private boolean renderSonarStats = true;
	private boolean renderTodos = true;
	private ReviewModel modelRoot;
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
		this.modelRoot = rg.getModelRoot();
		rtout = rtcout = writer;
	}

	public void generate() {
		rtout.write("\t\t<h2>Table of contents</h2>\n<!-- DOXIA macro |section=3|fromDepth=3|toDepth=3 -->\n<!-- MACRO{toc} -->\t\t\n");
		rtout.write("\t<h2>");
		rtcout.write(getTitle());
		rtout.write("</h2>\n");
		if (renderReviewStats || renderSonarStats){
			ReviewStatsSummary stats = ReviewStatsSummary.create(entries);
			if (renderReviewStats){
				generateReviewSummary(stats);
			}
			if (renderSonarStats){
				generateSonarMetricSummary(stats);
			}
		}
		rtout.write("\t\t<h3>Detailed statistics</h3>\n\t\t<table>\t\t\t\n\t\t\t<tr>\n");
		int i = 0;
		for (ColumnDefinition cd : columnDefinitions) {
			rtout.write("\t\t\t\t<th>");
			rtcout.write(cd.getTitle());
			rtout.write("\n");
			if (renderLinks){
				rtout.write("\t\t\t\t\t\t<a href=\"?");
				rtcout.write(UtilHtml.Q_PARAM_ORDER_BY);
				rtout.write("=");
				rtcout.write(String.valueOf(i));
				rtout.write("&");
				rtcout.write(UtilHtml.Q_PARAM_ORDER_ASC);
				rtout.write("=false\">&#8595;</a> <a href=\"?");
				rtcout.write(UtilHtml.Q_PARAM_ORDER_BY);
				rtout.write("=");
				rtcout.write(String.valueOf(i));
				rtout.write("&");
				rtcout.write(UtilHtml.Q_PARAM_ORDER_ASC);
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
		if (renderTodos){
			generateTodos();
		}
	}

	private void generateTodos() {
		List<ColumnDefinition> todoTableColumns = new ArrayList<ColumnDefinition>();
		todoTableColumns.add(new ClassNameColumnDefinition());
		todoTableColumns.add(new TodoMessageColumnDefinition(modelRoot));
		OkWithMessageColumnDefinition om = new OkWithMessageColumnDefinition(modelRoot);
		todoTableColumns.add(om);
		rtout.write("\t\t<h3>Classes with TODO-s</h3>\n\t\t<table>\n\t\t\t<tr>\n");
		for (ColumnDefinition c : todoTableColumns) {
			rtout.write("\t\t\t\t<th>");
			rtcout.write(c.getTitle());
			rtout.write("</th>\n");
		}
		boolean wasTodo = false;
		rtout.write("\t\t\t</tr>\n");
		for (ReportEntry entry : entries) {
			if (entry.getReviewStatus() == ReviewStatus.TODO || !om.getPropertyValue(entry).isEmpty()){
				wasTodo = true;
				rtout.write("\t\t\t<tr>\n");
				for (ColumnDefinition c : todoTableColumns) {
					rtout.write("\t\t\t\t<td><pre>");
					rtcout.write(c.getPropertyValue(entry));
					rtout.write("</pre></td>\n");
				}
				rtout.write("\t\t\t</tr>\n");
			}
		}
		if (!wasTodo) {
			rtout.write("\t\t\t\t<tr><td colspan=\"");
			rtcout.write(String.valueOf(todoTableColumns.size()));
			rtout.write("\">There are no TODO-s in this source set</td></tr>\n");
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

	public void setRenderReviewStats(boolean renderReviewStats) {
		this.renderReviewStats = renderReviewStats;
	}
	
	public void setRenderSonarStats(boolean renderSonarStats) {
		this.renderSonarStats = renderSonarStats;
	}
	
	/**
	 * If this is enabled, than a new section will be rendered into report. The
	 * section includes the list of classes with {@link ReviewStatus#TODO}
	 * status, and all todo messages that was not invalidated.
	 * 
	 * @param renderTodos
	 */
	public void setRenderTodos(boolean renderTodos) {
		this.renderTodos = renderTodos;
	}
}
