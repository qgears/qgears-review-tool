package hu.qgears.review.report;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.odftoolkit.odfdom.type.Color;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.style.Border;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.StyleTypeDefinitions.CellBordersType;
import org.odftoolkit.simple.style.StyleTypeDefinitions.FontStyle;
import org.odftoolkit.simple.style.StyleTypeDefinitions.SupportedLinearMeasure;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;

/**
 * Generates an ODS document, that includes a review statistic report.
 * 
 * @author agostoni
 *
 */
public class ReportGeneratorODS {

	private List<ReportEntry> entries;
	private ReportGenerator reportGenerator;
	private Font font_header;
	private Font font_title;
	private Font font_p;
	private int rowIndex;
	private Border border_single;


	/**
	 * Generates the ODS report base using specified report generator instance
	 * to collect report entries. The results will be saved into specified file.
	 * 
	 * @param reportGenerator The report generator for querying report entries
	 * @param outputFile The target file
	 * @param generateReviewStats If true than generates summary about review status
	 * @param generateSonarStats If true than generates summary about SONAR metrics
	 * @param generateTodoList see {@link ReportGeneratorTemplate#setRenderTodos(boolean)}
	 * 
	 * @throws Exception
	 * @since 2.0
	 */
	public void generateReport(ReportGenerator reportGenerator,File outputFile, boolean generateReviewStats, boolean generateSonarStats, boolean generateTodoList) throws Exception{
		SpreadsheetDocument d = SpreadsheetDocument.newSpreadsheetDocument();
		
		this.reportGenerator = reportGenerator;
		this.entries = reportGenerator.collectReportEntries();

		font_header = new Font("Arial",FontStyle.BOLD,12);
		font_title = new Font("Arial",FontStyle.BOLD,20);
		font_p = new Font("Arial",FontStyle.REGULAR,12);
		border_single = new Border(new Color(0,0,0), 0.05,SupportedLinearMeasure.PT );
		Table table = d.getSheetByIndex(0);
		table.setCellStyleInheritance(false);
		
		Row titleRow = table.getRowByIndex(0);
		Cell title = titleRow.getCellByIndex(0);
		title.setStringValue("Statistics of review source set "+reportGenerator.getTargetSourceSet());
		title.setFont(font_title);
		titleRow.setHeight(20, false);
		
		rowIndex = 2;
		
		if (generateReviewStats || generateSonarStats){
			ReviewStatsSummary stats = ReviewStatsSummary.create(entries);
			if (generateReviewStats){
				generateReviewStats(table,stats);
				rowIndex++;
			}
			if (generateSonarStats){
				generateSonarStats(table,stats);
				rowIndex++;
			}
		}
		generateReviewEntries( table);
		if (generateTodoList) {
			rowIndex++;
			generateTodoList(table);
		}
		
		//XXX as ODS is zip file, we don't need to set encoding explicitly, do we?
		d.save(outputFile);
	}

	private void generateSonarStats(Table table, ReviewStatsSummary stats) {
		Row row = table.getRowByIndex(rowIndex++);
		
		Cell sonarTitle = row.getCellByIndex(0);
		sonarTitle.setStringValue("SONAR metric statistics");
		sonarTitle.setFont(font_header);
		if (stats.getMetricsAVGs().isEmpty()){
			row = table.getRowByIndex(rowIndex++);
			row.getCellByIndex(0).setStringValue("SONAR stats completely missing! Check mapping file whether SONAR configuration is OK!");
			row.getCellByIndex(0).setFont(font_p);
		}
		
		for (ColumnDefinition cd : reportGenerator.getColumnDefinitions()){
			if (cd instanceof SonarMetricsColumnDefinition){
				row = table.getRowByIndex(rowIndex++);
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
				String style = ReportEntryCSSHelper.getCSSClassByPercentage(value);
				Cell t1 = row.getCellByIndex(0);
				Cell t2 = row.getCellByIndex(1);
				t1.setBorders(CellBordersType.ALL_FOUR,border_single);
				t2.setBorders(CellBordersType.ALL_FOUR,border_single);
				t1.setStringValue(smcd.getTitle()+" [AVG]");
				t2.setStringValue(formatted);
				applyStyle(style, t1);
				applyStyle(style, t2);
			}
		}
	}

	private void generateReviewStats(Table table, ReviewStatsSummary stats) {
		float progressByFileCount = stats.asPercentage(stats.getReviewOkCount());
		String cssClazzReviewStats = ReportEntryCSSHelper.getCSSClassByPercentage(""+progressByFileCount);
		
		Row row = table.getRowByIndex(rowIndex++);
		Cell reviewTitle = row.getCellByIndex(0);
		reviewTitle.setStringValue("Review statistics");
		reviewTitle.setFont(font_header);
		
		
		row = table.getRowByIndex(rowIndex++);
		row.getCellByIndex(0).setStringValue("Progress (estimation based on the sum of reviewed source files' size) : "+String.valueOf(stats.getOverallProgress()) + " %");
		row.getCellByIndex(0).setFont(font_p);
		for (ReviewStatus st : ReviewStatus.values()){
			row = table.getRowByIndex(rowIndex++);
			String count;
			String asPercentage;
			if (stats.getReviewStatusSummary().containsKey(st)){
				int countI = stats.getReviewStatusSummary().get(st);
				count =""+ countI;
				asPercentage =  String.format("%.2f %%",stats.asPercentage(countI));
			} else {
				count = asPercentage = ReportEntryCSSHelper.NO_DATA;
			}
			Cell t1 = row.getCellByIndex(0);
			Cell t2 = row.getCellByIndex(1);
			Cell t3 = row.getCellByIndex(2);
			
			t1.setStringValue(st.toString());
			t2.setStringValue(String.valueOf(count) + " files");
			t3.setStringValue(asPercentage);
			t1.setBorders(CellBordersType.ALL_FOUR,border_single);
			t2.setBorders(CellBordersType.ALL_FOUR,border_single);
			t3.setBorders(CellBordersType.ALL_FOUR,border_single);
			applyStyle(cssClazzReviewStats,t1);
			applyStyle(cssClazzReviewStats,t2);
			applyStyle(cssClazzReviewStats,t3);
		}
	}


	private void generateTodoList(Table table) {
		List<ColumnDefinition> todoTableColumns = new ArrayList<ColumnDefinition>();
		todoTableColumns.add(new ClassNameColumnDefinition());
		todoTableColumns.add(new TodoMessageColumnDefinition(reportGenerator.getModelRoot()));
		OkWithMessageColumnDefinition om = new OkWithMessageColumnDefinition(reportGenerator.getModelRoot());
		todoTableColumns.add(om);
		
		Row row = table.getRowByIndex(rowIndex++);
		Cell todoTitle = row.getCellByIndex(0); 
		todoTitle.setStringValue("Classes with TODO-s");
		todoTitle.setFont(font_header);
		
		int ci = 0;
		row = table.getRowByIndex(rowIndex++);
		for (ColumnDefinition c : todoTableColumns) {
			Cell t = row.getCellByIndex(ci++);
			t.setStringValue(c.getTitle());
			t.setFont(font_header);
		}
		boolean wasTodo = false;
		for (ReportEntry entry : entries) {
			if (entry.getReviewStatus() == ReviewStatus.TODO || !om.getPropertyValue(entry).isEmpty()){
				row = table.getRowByIndex(rowIndex++);
				ci = 0;
				wasTodo = true;
				for (ColumnDefinition c : todoTableColumns) {
					Cell v = row.getCellByIndex(ci++);
					v.setStringValue(c.getPropertyValue(entry));
				}
			}
		}
		if (!wasTodo) {
			row = table.getRowByIndex(rowIndex++);
			row.getCellByIndex(0).setStringValue("There are no TODO-s in this source set");
		}
		
	}

	private void generateReviewEntries(Table table) {
		Row firstRow = table.getRowByIndex(rowIndex++);
		int idx = 0;
		
		for (ColumnDefinition cd : reportGenerator.getColumnDefinitions()) {
			Cell c = firstRow.getCellByIndex(idx++);
			c.setStringValue(cd.getTitle());
			c.setFont(font_header);
		}
		table.getColumnByIndex(0).setWidth(150);
		for (ReportEntry entry : entries) {
			Row row = table.getRowByIndex(rowIndex++);
			idx = 0;
			for (ColumnDefinition cd : reportGenerator.getColumnDefinitions()) {
				Cell c = row.getCellByIndex(idx++);
				c.setStringValue(cd.getPropertyValue(entry));
				c.setFont(font_p);
				applyStyle(cd.getEntryClass(entry),c);
			}
		}
	}
	
	private void applyStyle(String entryClass, Cell c) {
		if (entryClass != null){
			Color color;
			switch (entryClass) {
			case ReportEntryCSSHelper.CLASS_METRIC_NODATA:
				//lightgray
				color = new Color(192,192,192);
				break;
			case ReportEntryCSSHelper.CLASS_METRIC_LOW:
				//lightred
				color = new Color(255,102,102);
				break;
			case ReportEntryCSSHelper.CLASS_METRIC_AVERAGE:
				//orange
				color = new Color(255,173,51);
				break;
			case ReportEntryCSSHelper.CLASS_METRIC_GOOD:
				//lightyellow
				color = new Color(255,255,102);
				break;
			case ReportEntryCSSHelper.CLASS_METRIC_VERY_GOOD:
				//yellowgreen
				color = new Color(153,255,51);
				break;
			case ReportEntryCSSHelper.CLASS_METRIC_PERFECT:
				//green
				color = new Color(51,102,0);
				break;
			default:
				//white
				color = new Color(255,255,255);
				break;
			}
			c.setCellBackgroundColor(color);
		}
		c.setFont(font_p);
	}

}
