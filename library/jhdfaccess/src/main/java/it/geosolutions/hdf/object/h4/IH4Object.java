package it.geosolutions.hdf.object.h4;

import it.geosolutions.hdf.object.IHObject;
import ncsa.hdf.hdflib.HDFException;

interface IH4Object extends IHObject {

	/**
	 * getter of <code>numAttributes</code>
	 * 
	 * @return the number of attributes associated to this object
	 */
	public abstract int getNumAttributes();

	/**
	 * Returns a specific attribute of this object, given its name.
	 * 
	 * @param attributeName
	 *                the name of the required attribute
	 * @return the {@link H4Attribute} related to the specified name.
	 * @throws HDFException
	 */
	public abstract H4Attribute getAttribute(final String attributeName)
			throws HDFException;

	/**
	 * Returns a specific attribute of this object, given its index.
	 * 
	 * @param attributeIndex
	 *                the index of the required attribute
	 * @return the {@link H4Attribute} related to the specified index.
	 * @throws HDFException
	 */
	public abstract H4Attribute getAttribute(final int attributeIndex)
			throws HDFException;

}