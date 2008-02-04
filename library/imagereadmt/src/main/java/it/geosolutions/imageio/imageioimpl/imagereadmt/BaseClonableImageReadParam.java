package it.geosolutions.imageio.imageioimpl.imagereadmt;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.imageio.ImageReadParam;

public abstract class BaseClonableImageReadParam extends
		CloneableImageReadParam {
	protected Object narrowClone(ImageReadParam param) {
		param.setDestination(this.getDestination());
		int[] destBands = this.getDestinationBands();
		if (destBands != null)
			param.setDestinationBands((int[]) destBands.clone());
		Point p = this.getDestinationOffset();
		if (p != null) {
			param.setDestinationOffset((Point) p.clone());
		}

		if (this.getDestinationType() != null)
			param.setDestinationType(this.getDestinationType());
		int[] srcBands = this.getSourceBands();
		if (srcBands != null)
			param.setSourceBands((int[]) srcBands.clone());

		param.setSourceProgressivePasses(this.getSourceMinProgressivePass(),
				this.getSourceNumProgressivePasses());
		Rectangle srcRegion = this.getSourceRegion();
		if (srcRegion != null) {
			param.setSourceRegion((Rectangle) srcRegion.clone());
		}

		param.setSourceSubsampling(this.getSourceXSubsampling(), this
				.getSourceYSubsampling(), this.getSubsamplingXOffset(), this
				.getSubsamplingYOffset());
		param.setController(this.getController());
		Dimension d = this.getSourceRenderSize();
		if (d != null) {
			param.setSourceRenderSize((Dimension) d.clone());
		}
		return param;
	}
}
