/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *        https://imageio-ext.dev.java.net/
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

import javax.imageio.ImageWriteParam;

public class JP2KKakaduImageWriteParam extends ImageWriteParam {

    public JP2KKakaduImageWriteParam() {
        cLevels = 7;
        qualityLayers = 1;
        canWriteCompressed = true;
        compressionMode = MODE_EXPLICIT;
    }

    /**
     * Specifies whether write only the jpeg2000 code stream. The output will be
     * a raw codestream.
     */
    private boolean writeCodeStreamOnly = false;

    /**
     * Specifies the quality of the output image as a compression ratio. As an
     * instance, a value of 0.2 means a 5:1 compression ratio.
     * 
     */
    private double quality = 1.0;

    /**
     * Specify the number of quality layers
     */
    private int qualityLayers;

    /**
     * Specify the number of decompositions levels.
     */
    private int cLevels;

    /**
     * Sets <code>writeCodeStreamOnly</code>.
     * 
     * @param value
     *                Whether the jpeg2000 code stream only or the jp2 format
     *                will be written into the output.
     * @see #isWriteCodeStreamOnly()
     */
    public void setWriteCodeStreamOnly(boolean writeCodeStreamOnly) {
        this.writeCodeStreamOnly = writeCodeStreamOnly;
    }

    /**
     * Gets <code>writeCodeStreamOnly</code>.
     * 
     * @return whether the jpeg2000 code stream only or the jp2 format will be
     *         written into the output.
     * @see #setWriteCodeStreamOnly(boolean)
     */
    public boolean isWriteCodeStreamOnly() {
        return writeCodeStreamOnly;
    }

    /**
     * Gets <code>quality</code>.
     * 
     * @return the quality parameter.
     * 
     * @see #setQuality(double)
     */
    public double getQuality() {
        return quality;
    }

    /**
     * Sets <code>quality</code>.
     * 
     * @param quality
     *                a quality parameter representing a compression ratio. As
     *                an instance, a 0.2 quality represents a 5:1 compression
     *                ratio.
     * 
     * @see #getQuality()
     */
    public void setQuality(final double quality) {
        this.quality = quality;
    }

    /**
     * Gets <code>cLevels</code>.
     * 
     * @return the number of decomposition levels.
     * 
     * @see #setCLevels(int)
     */
    public int getCLevels() {
        return cLevels;
    }

    /**
     * Sets <code>cLevels</code>.
     * 
     * @param cLevels
     *                the number of decomposition levels.
     * 
     * @see #getCLevels()()
     */
    public void setCLevels(final int cLevels) {
        this.cLevels = cLevels;
    }

    /**
     * Gets <code>qualityLayers</code>.
     * 
     * @return the number of quality layers.
     * 
     * @see #setQualityLayers(int)
     */
    public int getQualityLayers() {
        return qualityLayers;
    }

    /**
     * Sets <code>qualityLayers</code>.
     * 
     * @param qualityLayers
     *                the number of quality layers.
     * 
     * @see #getQualityLayers()
     */
    public void setQualityLayers(final int qualityLayers) {
        this.qualityLayers = qualityLayers;
    }
}
