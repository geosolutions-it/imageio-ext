/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.plugins.jp2k;

import it.geosolutions.imageio.imageioimpl.EnhancedImageReadParam;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;

/**
 * Class extending {@link ImageReadParam} with add for support of JP2 specific
 * parameters.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class JP2KKakaduImageReadParam extends EnhancedImageReadParam {

    public Object clone() throws CloneNotSupportedException {
        final JP2KKakaduImageReadParam retVal = new JP2KKakaduImageReadParam();
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

    /**
     * @uml.property name="interpolationType"
     */
    private int interpolationType;

    /**
     * Represents the number of available quality layers We set this to -1 by
     * default. If this value does not change, the reader makes no restrictions
     * on the number of quality layers which will be used during read
     * operations. Otherwise, setting this field allows the reader to use only
     * the specified number of quality layers.
     * 
     * @uml.property name="qualityLayers"
     */
    private int qualityLayers;

    // private int resolutionLevel;

    /** Constructs a default instance of <code>JP2KakaduImageReadParam</code>. */
    public JP2KKakaduImageReadParam() {
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
     *                the quality layers involved within the read operation.
     * @see #getQualityLayers()
     * @uml.property name="qualityLayers"
     */
    public void setQualityLayers(final int qualityLayers) {
        this.qualityLayers = qualityLayers;
    }

    /**
     * Gets <code>qualityLayers</code>.
     * 
     * @return the number of quality layers.
     * @see #setQualityLayers(int)
     * @uml.property name="qualityLayers"
     */
    public final int getQualityLayers() {
        return qualityLayers;
    }

    /**
     * Gets <code>InterpolationType</code>.
     * 
     * @return the interpolation algorithm which will be used when image need to
     *         be warped
     * @uml.property name="interpolationType"
     */
    public final int getInterpolationType() {
        return interpolationType;
    }

    /**
     * Sets <code>InterpolationType</code>.
     * 
     * @param interpolationType
     *                the interpolation type used during <code>WarpAffine</code>
     *                operation interpolationType should be one of: -<em>INTERPOLATION_NEAREST</em> -<em>INTERPOLATION_BILINEAR</em> -<em>INTERPOLATION_BICUBIC</em> -<em>INTERPOLATION_BICUBIC2</em>
     * @uml.property name="interpolationType"
     */
    public final void setInterpolationType(final int interpolationType) {
        this.interpolationType = interpolationType;
    }

    protected void initialize(ImageReadParam param) {
        if (param.hasController()) 
            setController(param.getController());
        setSourceRegion(param.getSourceRegion());
        setSourceBands(param.getSourceBands());
        setDestinationBands(param.getDestinationBands());
        setDestination(param.getDestination());
        setDestinationOffset(param.getDestinationOffset());
        setSourceSubsampling(param.getSourceXSubsampling(), param
                .getSourceYSubsampling(), param.getSubsamplingXOffset(), param
                .getSubsamplingYOffset());
        final ImageTypeSpecifier type = param.getDestinationType();
        if (type != null)
        	setDestinationType(type);

    }
}
