package hu.qgears.review.model;

import hu.qgears.review.util.IPropertyGetter;

/**
 * {@link IPropertyGetter} implementation used to index {@link ReviewEntry}s by
 * {@link ReviewEntry#getFileUrl() file url} property.
 * 
 * @author rizsi
 * 
 */
public class PropUrl implements IPropertyGetter<ReviewEntry>{

	@Override
	public String getPropertyValue(ReviewEntry obj) {
		return obj.getFileUrl();
	}

}
