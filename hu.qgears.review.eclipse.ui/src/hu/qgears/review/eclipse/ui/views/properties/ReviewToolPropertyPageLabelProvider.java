package hu.qgears.review.eclipse.ui.views.properties;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * {@link LabelProvider} for review tool property page.
 * 
 * @author agostoni
 *
 */
public class ReviewToolPropertyPageLabelProvider extends LabelProvider implements ITableLabelProvider{

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof ReviewToolPropertyPageContentProvider.PropertyPageContent){
			ReviewToolPropertyPageContentProvider.PropertyPageContent propertyPageContent = (ReviewToolPropertyPageContentProvider.PropertyPageContent) element;
			if (columnIndex == 0){
				return propertyPageContent.getPropertyName();
			} else {
				return propertyPageContent.getPropertyValue();
			}
		}
		if (element != null){
			return element.toString();
		}
		return null;
	}
}
