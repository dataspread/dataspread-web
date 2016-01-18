package org.zkoss.zss.model.sys.dependency;

/**
 * The object Reef to represent a object, and is always a dependent ref.
 * @author dennis
 * @since 3.5.0
 */
public interface ObjectRef extends Ref{
	/**
	 * @since 3.5.0
	 */
	public enum ObjectType{
		CHART, DATA_VALIDATION, AUTO_FILTER
	}
	public ObjectType getObjectType();
	public String getObjectId();
	public String[] getObjectIdPath();
}
