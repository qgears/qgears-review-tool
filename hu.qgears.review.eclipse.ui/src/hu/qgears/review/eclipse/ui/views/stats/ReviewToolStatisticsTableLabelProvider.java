package hu.qgears.review.eclipse.ui.views.stats;

import hu.qgears.review.report.ColumnDefinition;
import hu.qgears.review.report.ReportEntry;
import hu.qgears.review.report.ReportGenerator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Image;

/**
 * Labelprovider for {@link ReviewToolStatisticsView}.
 * 
 * @author agostoni
 *
 */
public class ReviewToolStatisticsTableLabelProvider extends LabelProvider implements ITableLabelProvider{

	private List<ColumnDefinition> columns = new ReportGenerator(null, null).getColumnDefinitions();
	private int sortColumnIndex;
	private boolean direction;
	private ViewerSorter sorter = new ViewerSorter (){
		public int compare(org.eclipse.jface.viewers.Viewer viewer, Object e1, Object e2) {
			if (sortColumnIndex >= 0 && sortColumnIndex < columns.size()){
				ColumnDefinition cd = columns.get(sortColumnIndex); 
				Comparator<ReportEntry> cmp = cd.getComparator();
				if (e1 instanceof ReportEntry && e2 instanceof ReportEntry){
					return (direction ? 1 : -1) * cmp.compare((ReportEntry)e1,(ReportEntry) e2);
				}
			}
			return 0;
		};
	};
	
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
		int index = 0;
		for (ColumnDefinition cd : columns){
			String title = cd.getTitle();
			if (index == sortColumnIndex){
				title = title + (getDirection() ? " ↑" : " ↓");
			}
			cols.add( title);
			index++;
		}
		return cols;
	}

	/**
	 * Returns the viewer sorter that is able to order elements in associated
	 * table.
	 * 
	 * @return
	 */
	public ViewerSorter getSorter() {
		return sorter;
	}

	/**
	 * Returns the header text for specifed column
	 * 
	 * @param index
	 * @return
	 */
	public String getHeaderText(int index) {
		List<String> columnNames = getColumnNames();
		if (columnNames.size() > index && index >= 0){
			return columnNames.get(index);
		} else {
			return null;
		}
	}
	
	/**
	 * Updates comparator, the table content will be ordered by specified column index.
	 * 
	 * @param selectedColumn
	 */
	public void update(int selectedColumn){
		if (sortColumnIndex == selectedColumn){
			direction = !direction;
		} else {
			sortColumnIndex = selectedColumn;
			direction = false;
		}
	}

	/**
	 * Returns the index of column, which determines the sorting order
	 * currently.
	 * 
	 * @return
	 */
	public int getSortColumnIndex() {
		return sortColumnIndex;
	}
	
	/**
	 * <code>true</code> means ascending order, <code>false</code> means
	 * descending order.
	 * 
	 * @return
	 */
	public boolean getDirection(){
		return direction;
	}
}
