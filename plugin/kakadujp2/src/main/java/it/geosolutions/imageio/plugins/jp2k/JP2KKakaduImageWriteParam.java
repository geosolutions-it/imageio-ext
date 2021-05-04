/* JP2K Kakadu Image Writer V. 1.0 
 * 
 * (c) 2008, 2018 OnePacs, LLC, info@onepacs.com
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

import kdu_jni.Kdu_global;

/**
 * Class holding Write parameters to customize the write operations, and set several fields, tags,
 * markers through the kakadu machinery. 
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
public class JP2KKakaduImageWriteParam extends ImageWriteParam {

    public static final int UNSPECIFIED_ORG_GEN_TLM = -1;
    
    public enum ProgressionOrder{
        
        LRCP { 
            int getValue(){
                return Kdu_global.Corder_LRCP;
            }
        },
        RLCP { 
            int getValue(){
                return Kdu_global.Corder_RLCP;
            }
        },
        RPCL { 
            int getValue(){
                return Kdu_global.Corder_RPCL;
            }
        },
        PCRL { 
            int getValue(){
                return Kdu_global.Corder_PCRL;
            }
        },
        CPRL { 
            int getValue(){
                return Kdu_global.Corder_CPRL;
            }
        };
        
        abstract int getValue();        
    };
    
    /** 
     * Type of compression to better customize the quality specification. 
     */
    public enum Compression {
        NUMERICALLY_LOSSLESS, LOSSY, UNDEFINED
    }
    
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

    @Override
    public boolean canWriteTiles() {
        return true;
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
     * Specify the number of guard bits
     */
    private int qGuard = -1;
    
    /**
     * Specify the progression order parameter.
     */
    private ProgressionOrder cOrder;
    
    /**
     * Specify the number of decompositions levels.
     */
    private int cLevels;
    
    /**
     * if true, request the insertion of packet length information in the header of tile-parts
     */
    private boolean orgGen_plt;
    
    /**
     * Controls the division of each tile's packets into tile-parts
     */
    private String orgT_parts;
    
    /**
     * the cPrecincts settings 
     */
    private String cPrecincts;
    
    /**
     *  the bit rate for each quality layers
     */
    private double qualityLayersBitRates[];
    
    /**
     * The type of compression. 
     */
    private Compression compression = Compression.UNDEFINED;
    
    /**
     * Specify the TLM (tile-part-length) marker segments in the main header.
     */
    private int orgGen_tlm = UNSPECIFIED_ORG_GEN_TLM;
    
    /**
     * The default number of decomposition levels.
     */
    final static int DEFAULT_C_LEVELS = 5;
    
    /**
     * The default SProfile, actually: Profile2
     */
    final static int DEFAULT_SPROFILE = 2;
    
    /**
     * 
     */
    private byte[] geoJp2 = null;
    
    /**
     * Field to override the static property related to adding Comment Markers 
     * whithin the produced image. 
     */
    private boolean addCommentMarker = true;
    
    /**
     * Restricted profile to which the code-stream conforms.
     */
    private int sProfile = DEFAULT_SPROFILE;

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
     *                ratio. This parameter will be ignored in case the 
     *                qualityLayersBitRates parameter have been specified or
     *                in case the Compression parameter has been specified 
     *                through the {@link #setCompression(Compression)} method
     *                using a {@link Compression#NUMERICALLY_LOSSLESS}
     * 
     * @see #getQuality()
     * @see #setQualityLayersBitRates(double[])
     * @see #setCompression(Compression)
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
     * @see #setQualityLayersBitRates(double[])
     * @see #setCompression(Compression)
     */
    public void setQualityLayers(final int qualityLayers) {
        this.qualityLayers = qualityLayers;
    }
    
    public byte[] getGeoJp2() {
        return geoJp2;
    }

    public void setGeoJp2(byte[] geoJp2) {
        this.geoJp2 = geoJp2;
    }

    public void setOrgGen_plt(boolean orgGen_plt) {
        this.orgGen_plt = orgGen_plt;
    }

    public boolean isOrgGen_plt() {
        return orgGen_plt;
    }

    public void setOrgGen_tlm(int orgGen_tlm) {
        this.orgGen_tlm = orgGen_tlm;
    }

    public int getOrgGen_tlm() {
        return orgGen_tlm;
    }
    
    public void setOrgT_parts(String orgT_parts) {
        this.orgT_parts = orgT_parts;
    }

    public String getOrgT_parts() {
        return orgT_parts;
    }

    public ProgressionOrder getcOrder() {
        return cOrder;
    }

    public void setcOrder(ProgressionOrder cOrder) {
        this.cOrder = cOrder;
    }

    public int getqGuard() {
        return qGuard;
    }

    public void setqGuard(int qGuard) {
        this.qGuard = qGuard;
    }

    public String getcPrecincts() {
        return cPrecincts;
    }

    public void setcPrecincts(String cPrecincts) {
        this.cPrecincts = cPrecincts;
    }
    
    /**
     * Set the qualityLayer bitRates. This parameter will override any quality value specified with 
     * {@link #setQuality(double)} 
     * 
     * @param qualityLayersBitRates an array representing the cumulative bitRate 
     *          for each qualityLayer. The length of the array should be equals
     *          to the specified qualityLayers value.
     * 
     * @see #setQualityLayers
     * @see #setCompression(Compression)
     */
    public void setQualityLayersBitRates(double qualityLayersBitRates[]) {
        this.qualityLayersBitRates = qualityLayersBitRates;
    }

    public double[] getQualityLayersBitRates() {
        return qualityLayersBitRates;
    }

    public boolean isAddCommentMarker() {
        return addCommentMarker;
    }

    public void setAddCommentMarker(boolean addCommentMarker) {
        this.addCommentMarker = addCommentMarker;
    }

    public int getsProfile() {
        return sProfile;
    }

    public void setsProfile(int sProfile) {
        this.sProfile = sProfile;
    }

    /**
     * Set the compression type. One of {@link Compression#NUMERICALLY_LOSSLESS},
     * {@link Compression#LOSSY}.
     * 
     * @param compression the type of compression to apply which could be numerically lossless,
     * visually lossless (leveraging on quality bitrates) or lossy (leveraging on quality factor).
     * <UL>
     * <LI>Use NUMERICALLY_LOSSLESS if you want to specify a LossLess (reversible) compression
     * (which is equivalent to specifying a quality = 1 parameter). The quality parameter will be 
     * ignored.
     * Optionally specify a qualityLayersBitRate parameter (make sure to set 0 as the last value 
     * of the array) if you want to specify the quality layers structure. Otherwise the
     * quality layers structure will be built using a dicothomic scale.</LI>
     * <LI>Use LOSSY if you want to specify a Visually LossLess compression (which is still lossy).
     * You need to specify a qualityLayersBitRate parameter when using this value.
     * The quality parameter will be ignored.
     * </LI>
     * <LI>Use LOSSY if you want to specify a Lossy compression leveraging on the quality 
     * parameter which should be < 1. In order to leverage on the quality parameter, the 
     * qualityLayersBitRates parameter shouldn't be specified.
     * </LI>
     * </UL>
     * 
     * @see #setQualityLayers
     * @see #setQuality(double)
     * @see #setQualityLayersBitRates(double[])
     */
    public void setCompression(Compression compression) {
        this.compression = compression;
    }

    public Compression getCompression() {
        return compression;
    }

    @Override
    public String toString() {
        return "JP2KKakaduImageWriteParam [writeCodeStreamOnly="
                + writeCodeStreamOnly + ", quality=" + quality
                + ", qualityLayers=" + qualityLayers + ", cOrder="
                + cOrder + ", cLevels=" + cLevels + ", ORGgen_plt="
                + orgGen_plt + ", cPrecincts=" + cPrecincts + ", ORGgen_tlm="
                + orgGen_tlm + ", ORGt_parts=" + orgT_parts +", tilingMode=" 
                + tilingMode + ", tileWidth=" + tileWidth
                + ", tileHeight=" + tileHeight + "]";
    }
}
