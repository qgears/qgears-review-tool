package hu.qgears.review.util;

import java.util.Collection;
import java.util.Collections;

import hu.qgears.commons.MultiMap;
import hu.qgears.commons.MultiMapHashImpl;

/**
 * Utility that builds a multimap based index on a given property of objects.
 * The property is specified by an implementation of {@link IPropertyGetter}
 * interface, passed as ctor parameter.
 * 
 * @author rizsi
 * 
 * @param <T> The type of objects within index
 */
public class IndexByProperty<T> {
	private IPropertyGetter<T> prop;
	private MultiMap<String, T> map = new MultiMapHashImpl<String, T>();

	/**
	 * See {@link IndexByProperty head comment}.
	 * 
	 * @param prop
	 */
	public IndexByProperty(IPropertyGetter<T> prop) {
		super();
		this.prop = prop;
	}

	/**
	 * Adds an object to the index.
	 * 
	 * @param obj
	 */
	public void addObject(T obj) {
		String key = prop.getPropertyValue(obj);
		map.putSingle(key, obj);
	}

	/**
	 * Returns objects from index, that are assigned to given property value.
	 * 
	 * @param propertyValue
	 *            the property value to use as key when searching in index
	 * @return
	 */
	public Collection<T> getMappedObjects(String propertyValue) {
		return Collections.unmodifiableCollection(map.get(propertyValue));
	}
}
