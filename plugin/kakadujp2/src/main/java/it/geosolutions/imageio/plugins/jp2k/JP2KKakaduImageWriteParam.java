/* JP2K Kakadu Image Writer V. 1.0 
 * 
 * (c) 2008 Quality Nighthawk Teleradiology Group, Inc.
 * Contact: info@qualitynighthawk.com
 *
 * Produced by GeoSolutions, Eng. Daniele Romagnoli and Eng. Simone Giannecchini
 * GeoSolutions S.A.S. ---  Via Carignoni 51, 55041 Camaiore (LU) Italy
 * Contact: info@geo-solutions.it
 *
 * Released under the Gnu Lesser General Public License version 3. 
 * All rights otherwise reserved. 
 *
 * JP2K Kakadu Image Writer is distributed on an "AS IS" basis, 
 * WITHOUT ANY WARRANTY, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  
 *
 * See the GNU Lesser General Public License version 3 for more details. 
 * http://www.fsf.org/licensing/licenses/lgpl.html
 */
package it.geosolutions.imageio.plugins.jp2k;

import javax.imageio.ImageWriteParam;

/**
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
public class JP2KKakaduImageWriteParam extends ImageWriteParam {

    /**
     * Default Constructor.
     */
    public JP2KKakaduImageWriteParam() {
        cLevels = DEFAULT_C_LEVELS;
        quality = DEFAULT_QUALITY;
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
     */
    private double quality;
    
    /**
     * The default number of decomposition levels.
     */
    final static double DEFAULT_QUALITY = 1.0;

    /**
     * Specify the number of quality layers
     */
    private int qualityLayers;

    /**
     * Specify the number of decompositions levels.
     */
    private int cLevels;
    
    /**
     * The default number of decomposition levels.
     */
    final static int DEFAULT_C_LEVELS = 7;

    /**
     * Sets <code>writeCodeStreamOnly</code>.
     * 
     * @param value
     *                Whether the jpeg2000 code stream only or the jp2 format
     *                will be written into the output.
     * @see #isWriteCodeStreamOnly()
     */
    public void setWriteCodeStreamOnly(final boolean writeCodeStreamOnly) {
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
