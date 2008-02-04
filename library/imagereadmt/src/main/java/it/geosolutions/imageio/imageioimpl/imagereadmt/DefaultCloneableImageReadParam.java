package it.geosolutions.imageio.imageioimpl.imagereadmt;

public class DefaultCloneableImageReadParam extends BaseClonableImageReadParam {

	public Object clone() throws CloneNotSupportedException {
		DefaultCloneableImageReadParam param = new DefaultCloneableImageReadParam();
		return narrowClone(param);
	}
}
