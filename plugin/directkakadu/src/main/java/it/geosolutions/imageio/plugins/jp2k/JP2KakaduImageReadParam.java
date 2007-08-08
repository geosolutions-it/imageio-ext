/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    (C) 2007, GeoSolutions
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
package it.geosolutions.imageio.plugins.jp2k;

import it.geosolutions.imageio.imageioimpl.imagereadmt.CloneableImageReadParam;

import javax.imageio.ImageReadParam;

public class JP2KakaduImageReadParam extends CloneableImageReadParam {

	public Object clone() throws CloneNotSupportedException {
		final JP2KakaduImageReadParam retVal = new JP2KakaduImageReadParam();
		retVal.setInterpolationType(this.getInterpolationType());
		this.setQualityLayers(this.getQualityLayers());
		retVal.setController(this.getController());
		retVal.setDestination(getDestination());
		retVal.setDestinationBands(getDestinationBands());
		retVal.setDestinationOffset(getDestinationOffset());
		retVal.setDestinationType(getDestinationType());
		retVal.setSourceBands(getSourceBands());
		retVal.setSourceProgressivePasses(getSourceMinProgressivePass(),
				getSourceNumProgressivePasses());
		retVal.setSourceRegion(getSourceRegion());
		try {
			retVal.setSourceRenderSize(getSourceRenderSize());
		} catch (Throwable t) {
		}
		retVal.setSourceSubsampling(getSourceXSubsampling(),
				getSourceYSubsampling(), getSubsamplingXOffset(),
				getSubsamplingYOffset());
		return retVal;
	}

	public static final int INTERPOLATION_NEAREST = 1;

	public static final int INTERPOLATION_BILINEAR = 2;

	private int interpolationType;

	/**
	 * Represents the number of available quality layers We set this to -1 by
	 * default. If this value does not change, the reader makes no restrictions
	 * on the number of quality layers which will be used during read
	 * operations. Otherwise, setting this field allows the reader to use only
	 * the specified number of quality layers.
	 */
	private int qualityLayers;

	// private int resolutionLevel;

	/** Constructs a default instance of <code>JP2KakaduImageReadParam</code>. */
	public JP2KakaduImageReadParam() {
		super();
		interpolationType = INTERPOLATION_NEAREST;
		// resolutionLevel=0;
		qualityLayers = -1;

	}

	// /**
	// * Sets <code>resolutionLevel</code>.
	// *
	// * @param resolutionLevel
	// * the resolution level with 0 being the lowest available.
	// * @see #getResolutionLevel()
	// */
	// public void setResolutionLevel(int resolutionLevel) {
	// this.resolutionLevel = resolutionLevel;
	// }
	//
	// /**
	// * Gets <code>resolutionLevel</code>.
	// *
	// * @return the resolution level with 0 being the lowest available.
	// * @see #setResolutionLevel(int)
	// */
	// public final int getResolutionLevel() {
	// return resolutionLevel;
	// }

	/**
	 * Sets <code>qualityLayers</code>.
	 * 
	 * @param qualityLayers
	 *            the quality layers involved within the read operation.
	 * @see #getQualityLayers()
	 */
	public void setQualityLayers(int qualityLayers) {
		this.qualityLayers = qualityLayers;
	}

	/**
	 * Gets <code>qualityLayers</code>.
	 * 
	 * @return the number of quality layers.
	 * @see #setQualityLayers(int)
	 */
	public final int getQualityLayers() {
		return qualityLayers;
	}

	/**
	 * Gets <code>InterpolationType</code>.
	 * 
	 * @return the interpolation algorithm which will be used when image need to
	 *         be warped
	 */
	public final int getInterpolationType() {
		return interpolationType;
	}

	/**
	 * Sets <code>InterpolationType</code>.
	 * 
	 * @param interpolationType
	 *            the interpolation type used during <code>WarpAffine</code>
	 *            operation
	 * 
	 * interpolationType should be one of: -<em>INTERPOLATION_NEAREST</em> -<em>INTERPOLATION_BILINEAR</em> -<em>INTERPOLATION_BICUBIC</em> -<em>INTERPOLATION_BICUBIC2</em>
	 */
	public final void setInterpolationType(int interpolationType) {
		this.interpolationType = interpolationType;
	}

	protected void intialize(ImageReadParam param) {
		if (param.hasController()) {
			setController(param.getController());
		}
		setSourceRegion(param.getSourceRegion());
		setSourceBands(param.getSourceBands());
		setDestinationBands(param.getDestinationBands());
		setDestination(param.getDestination());

		setDestinationOffset(param.getDestinationOffset());
		setSourceSubsampling(param.getSourceXSubsampling(), param
				.getSourceYSubsampling(), param.getSubsamplingXOffset(), param
				.getSubsamplingYOffset());
		setDestinationType(param.getDestinationType());

	}
}
