package hu.qgears.review.util;

/**
 * Getter method to indirectly get a property of objects. Used to build indexes
 * on a set of objects.
 * 
 * @author rizsi
 * 
 * @param <T> The type of object
 * @see IndexByProperty
 */
public interface IPropertyGetter<T> {
	/**
	 * Returns the property value of given object
	 * 
	 * @param obj
	 *            The object holding the property to query
	 * @return
	 */
	String getPropertyValue(T obj);
}
