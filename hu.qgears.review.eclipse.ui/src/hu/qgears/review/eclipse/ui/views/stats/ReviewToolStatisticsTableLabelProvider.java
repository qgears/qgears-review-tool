package hu.qgears.review.eclipse.ui.views.stats;

import hu.qgears.review.report.ColumnDefinition;
import hu.qgears.review.report.ReportEntry;
import hu.qgears.review.report.ReportGenerator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Labelprovider for {@link ReviewToolStatisticsView}.
 * 
 * @author agostoni
 *
 */
public class ReviewToolStatisticsTableLabelProvider extends LabelProvider implements ITableLabelProvider{

	private List<ColumnDefinition> columns = new ReportGenerator(null, null).getColumnDefinitions();
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (columnIndex >= 0 && columnIndex < columns.size()){
			if (element instanceof ReportEntry){
				ColumnDefinition cd = columns.get(columnIndex); 
				return cd.getPropertyValue((ReportEntry) element);
			} else if (columnIndex == 0){
				return element == null ? null : element.toString();
			}
		}
		return null;
	}

	/**
	 * Returns the name of columns in the target table viewer.
	 * 
	 * @return
	 */
	public List<String> getColumnNames() {
		List<String> cols = new ArrayList<String>();
		for (ColumnDefinition cd : columns){
			cols.add(cd.getTitle());
		}
		return cols;
	}
}
