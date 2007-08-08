/**
 * 
 */
package it.geosolutions.imageio.imageioimpl.imagereadmt;

import javax.imageio.ImageReadParam;

/**
 * @author simone
 * 
 */
public abstract class CloneableImageReadParam extends ImageReadParam implements
		Cloneable {

	public abstract Object clone() throws CloneNotSupportedException;

}
 