package hu.qgears.review.eclipse.ui.wizard;

import hu.qgears.review.model.ReviewEntry;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Labelprovider that shows existing annotations for a review source. Used to
 * render 'invalidates' section of {@link ReviewEntryDetailsPage}.
 * 
 * @author agostoni
 * 
 */
public class ReviewEntryLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof ReviewEntry){
			ReviewEntry re = (ReviewEntry) element;
			if (columnIndex == 0){
				return re.getAnnotation() + " by "+re.getUser();
			} else {
				return re.getComment();
			}
		}
		return null;
	}
}
