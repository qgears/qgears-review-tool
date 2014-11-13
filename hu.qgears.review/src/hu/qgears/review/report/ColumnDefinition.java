package hu.qgears.review.report;

import java.util.Comparator;

import hu.qgears.review.util.IPropertyGetter;

/**
 * Provides indirect access to a single attribute of {@link ReportEntry}
 * instances. Also specifies a "title", that is a user readable name of the
 * property.
 * <p>
 * Using this interface allows to generator components to easily generate a
 * single column of table-based document.
 * 
 * @author agostoni
 * @see ReportGeneratorTemplate
 */
public interface ColumnDefinition extends IPropertyGetter<ReportEntry>{
	
	/**
	 * The user readable name of the property which implementation class access
	 * to.
	 * 
	 * @return
	 */
	public String getTitle();
	
	/**
	 * Returns the CSS class that must be assigned to given element. May return
	 * <code>null</code>, indication no CSS class attribute must be generated.
	 * 
	 * @param e
	 * @return
	 */
	public String getEntryClass(ReportEntry e);

	/**
	 * Returns a comparator that is used when the report entries must be ordered
	 * by this column.
	 * 
	 * @return
	 */
	public Comparator<ReportEntry> getComparator();
}
