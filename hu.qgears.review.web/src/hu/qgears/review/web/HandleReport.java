package hu.qgears.review.web;

import hu.qgears.review.model.ReviewInstance;
import hu.qgears.review.model.ReviewSourceSet;
import hu.qgears.review.report.ColumnDefinition;
import hu.qgears.review.report.ReportGenerator;
import hu.qgears.review.report.ReportGeneratorTemplate;
import hu.qgears.review.util.UtilHtml;

import java.util.List;

public class HandleReport extends AbstractRender {

	public static final String URL_PREXIX = "report";
	
	public HandleReport(WebQuery query, ReviewInstance instance) {
		super(query, instance);
	}

	@Override
	public void render() throws Exception {
		String sourceSetId = query.getAfterModule();
		renderHeader(sourceSetId+ "stats");
		ReviewSourceSet set = instance.getModel().sourcesets.get(sourceSetId);
		if (set != null){
			ReportGenerator rg = new ReportGenerator(instance.getModel(), set);
			int orderBy = getOrderByIndex();
			boolean orderAsc = isOrderAscendant();
			List<ColumnDefinition> defs = rg.getColumnDefinitions();
			if (orderBy >= 0 && orderBy < defs.size()){
				rg.setOrderBy(defs.get(orderBy),orderAsc );
			}
			ReportGeneratorTemplate template = new ReportGeneratorTemplate(out, rg,true);
			template.generate();
		}
		renderFooter();
	}

	private boolean isOrderAscendant() {
		try {
			String asc = query.request.getParameter(UtilHtml.Q_PARAM_ORDER_ASC);
			if (asc != null){
				return Boolean.valueOf(asc);
			}
		} catch (Exception e) {
		}
		return true;
	}

	private int getOrderByIndex() {
		try {
			return Integer.valueOf(query.request.getParameter(UtilHtml.Q_PARAM_ORDER_BY));
		} catch (Exception e) {
			return 0;
		}
	}

}
