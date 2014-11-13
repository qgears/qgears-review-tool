package hu.qgears.review.model;

import hu.qgears.review.util.IPropertyGetter;

/**
 * {@link IPropertyGetter} implementation used to index {@link ReviewEntry}s by
 * {@link ReviewEntry#getSha1Sum() SHA1 sum} property.
 * 
 * @author rizsi
 * 
 */
public class PropSha1 implements IPropertyGetter<ReviewEntry>{

	@Override
	public String getPropertyValue(ReviewEntry obj) {
		return obj.getFileSha1sum();
	}

}
