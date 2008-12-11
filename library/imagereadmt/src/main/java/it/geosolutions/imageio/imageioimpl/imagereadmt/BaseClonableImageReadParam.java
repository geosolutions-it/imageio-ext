/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2007 - 2008, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.imageioimpl.imagereadmt;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.imageio.ImageReadParam;

/**
 * The base abstract class for cloning {@link ImageReadParam}s.
 * 
 * @author Simone Giannecchini, GeoSolutions
 * @author Daniele Romagnoli, GeoSolutions
 */
public abstract class BaseClonableImageReadParam extends
		CloneableImageReadParam {
	/**
	 * Performs a narrow clone of this {@link ImageReadParam}.
	 * 
	 * @param param the {@link ImageReadParam} instance containing the clone.
	 * @return the narrow clone of this {@link ImageReadParam}.
	 */
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
