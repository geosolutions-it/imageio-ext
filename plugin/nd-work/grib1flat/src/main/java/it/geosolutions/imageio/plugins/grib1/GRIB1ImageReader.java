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
package it.geosolutions.imageio.plugins.grib1;

import it.geosolutions.imageio.ndplugin.BaseImageReader;
import it.geosolutions.imageio.ndplugin.util.SoftValueHashMap;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.RasterFactory;

import net.sourceforge.jgrib.GribCollection;
import net.sourceforge.jgrib.GribFile;
import net.sourceforge.jgrib.GribRecord;
import net.sourceforge.jgrib.GribRecordBDS;
import net.sourceforge.jgrib.GribRecordGDS;
import net.sourceforge.jgrib.GribRecordPDS;
import net.sourceforge.jgrib.NoValidGribException;
import net.sourceforge.jgrib.NotSupportedException;
import net.sourceforge.jgrib.gds_grids.GribGDSLambert;
import net.sourceforge.jgrib.gds_grids.GribGDSRotatedLatLon;
import net.sourceforge.jgrib.tables.GribPDSLevel;
import net.sourceforge.jgrib.tables.GribPDSParameter;

/**
 * {@link GRIB1ImageReader} is a {@link ImageReader} able to create
 * {@link RenderedImage} from GRIB1 sources.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Alessio Fabiani, GeoSolutions
 */
public class GRIB1ImageReader extends BaseImageReader {

    /**
     * Simple boolean saying if this reader has a single file as its input,
     * instead of a whole directory.
     */
    private boolean isSingleFile = true;

    /**
     * The input file for this reader.
     */
    private GribFile gribFile = null;

    /**
     * Note that only one of gribFile and gribCollection will be set
     */
    private GribCollection gribCollection = null;

    private static ColorModel colorModel = RasterFactory
            .createComponentColorModel(DataBuffer.TYPE_FLOAT, // dataType
                    ColorSpace.getInstance(ColorSpace.CS_GRAY), // color space
                    false, // has alpha
                    false, // is alphaPremultiplied
                    Transparency.OPAQUE); // transparency

    /**
     * A SoftReferences based HashMap to cache {@link GribRecordWrapper}
     * instances. <BR>
     * This map contains couples <int globalIndex, GribRecordWrapper instance>
     */
    private SoftValueHashMap<Integer, GribRecordWrapper> gribRecordsMap = new SoftValueHashMap<Integer, GribRecordWrapper>();

    private Map<Integer, Integer[]> indexMap = null;

    /**
     * A class wrapping a GribRecord and its basic properties and structures
     */
    private class GribRecordWrapper {

        private String gribRecordParameterName;

        private String gribRecordParameterDescription;

        /** The product definition section (PDS) of a GRIB record */
        private GribRecordPDS pds;

        private GribPDSLevel level;

        /** The binary data section (BDS) of a GRIB record */
        private GribRecordBDS bds;

        /** The grid definition section (GDS) of a GRIB record */
        private GribRecordGDS gds;

        /** The GRIB record wrapped by this wrapper */
        private GribRecord record;

        /** The width of the wrapped GRIB record */
        private int width;

        /** The height of the wrapped GRIB record */
        private int height;

        /** the {@code SampleModel} used for the wrapped GRIB record */
        private SampleModel sampleModel;

        private String paramID;

        private String gribRecordParameterUnit;

        /**
         * Constructor of the {@link GribRecordWrapper} class which allows to
         * wrap a grib record related to the input imageIndex
         * 
         * @param imageIndex
         *                the index need to be wrapped by this
         *                {@link GribRecordWrapper}
         */
        public GribRecordWrapper(int imageIndex) {
            if (isSingleFile)
                record = gribFile.getRecord(imageIndex + 1);
            else {
                Integer[] indexes = indexMap.get(imageIndex);
                final int iteratorStep = indexes[0].intValue();
                final int recordIndex = indexes[1].intValue();
                GribFile gf = null;
                Iterator<GribFile> it = gribCollection.getGribIterator();
                for (int i = 0; i < iteratorStep; i++) {
                    gf = it.next();
                }
                record = gf.getRecord(recordIndex + 1);
            }
            bds = record.getBDS();
            pds = record.getPDS();
            level = pds.getLevel();
            gds = record.getGDS();
            width = gds.getGridNX();
            height = gds.getGridNY();
            sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width,
                    height, 1);
            final GribPDSParameter parameter = pds.getParameter();
            paramID = GRIB1Utilities.buildParameterDescriptor(parameter, pds.getParamTable());
            String paramsStrings[] = GRIB1Utilities.getNameAndDescription(
                    parameter.getNumber(), pds.getParamTable());
            gribRecordParameterName = paramsStrings[0];
            gribRecordParameterDescription = paramsStrings[1];
            gribRecordParameterUnit = parameter.getUnit();
        }

        /** Return the width of the wrapped GRIB record */
        public int getHeight() {
            return height;
        }

        /** Return the width of the wrapped GRIB record */
        public int getWidth() {
            return width;
        }

        /** Return the {@code SampleModel} used for the wrapped GRIB record */
        public SampleModel getSampleModel() {
            return sampleModel;
        }

        /** Return the binary data section (BDS) of the wrapped GRIB record */
        public GribRecordBDS getBDS() {
            return bds;
        }

        /**
         * Return the product definition section (PDS) of the wrapped GRIB
         * record
         */
        public GribRecordPDS getPDS() {
            return pds;
        }

        /** Return grid definition section (GDS) of the wrapped GRIB record */
        public GribRecordGDS getGDS() {
            return gds;
        }

        public String getGribRecordParameterName() {
            return gribRecordParameterName;
        }

        public String getGribRecordParameterDescription() {
            return gribRecordParameterDescription;
        }

        public String getParamID() {
            return paramID;
        }

        public GribPDSLevel getLevel() {
            return level;
        }

        public String getGribRecordParameterUnit() {
            return gribRecordParameterUnit;
        }

    }

    /**
     * Sets the input source to use within this reader. {@code URI}s,
     * {@code File}s (also representing a Directory), {@code String}s (also
     * representing the path of a Directory), {@code URL}s,
     * {@code ImageInputStream}s are accepted input types.<BR>
     * Other parameters ({@code seekForwardOnly} and {@code ignoreMetadata})
     * are actually ignored.
     * 
     * @param input
     *                the {@code Object} to be set as input of this reader.
     * 
     * @throws {@link IllegalArgumentException}
     *                 in case the provided input {@code Object} cannot be
     *                 properly parsed and used as input for the reader.
     */
    public void setInput(Object input, boolean seekForwardOnly,
            boolean ignoreMetadata) {
        try {

            if (input instanceof URI) {
                input = ((URI) input).toURL();
            }
            if (gribFile == null) {

                if (input instanceof File) {
                    if (((File) input).isDirectory())
                        gribCollection = new GribCollection((File) input);
                    else
                        gribFile = new GribFile(ImageIO
                                .createImageInputStream((File) input), null);
                } else if (input instanceof String) {
                    File file = new File((String) input);
                    if (file.isDirectory())
                        gribCollection = new GribCollection(file);
                    else
                        gribFile = new GribFile(ImageIO
                                .createImageInputStream(file), null);
                } else if (input instanceof URL) {
                    gribFile = new GribFile((URL) input, null);
                } else if (input instanceof ImageInputStream) {
                    gribFile = new GribFile((ImageInputStream) input, null);

                } else if (input instanceof GribFile) {
                    this.gribFile = (GribFile) input;
                }
                if (gribFile != null)
                    gribFile.parseGribFile();
            }

        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Error occurred during grib file parsing", e);
        } catch (NotSupportedException e) {
            throw new IllegalArgumentException(
                    "Error occurred during grib file parsing", e);
        } catch (NoValidGribException e) {
            throw new IllegalArgumentException(
                    "Error occurred during grib file parsing", e);
        }
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        initialize();
    }

    /**
     * Initialize main properties for this reader.
     */
    private void initialize() {
        isSingleFile = gribFile != null ? true : false;
        int numImages = 0;
        if (isSingleFile) {
            numImages = gribFile.getRecordCount();
            indexMap = null;
        } else {
            Iterator<GribFile> gribFilesIt = gribCollection.getGribIterator();
            numImages = 0;

            // Estimates the number of records contained in the gribCollection
            // Just to estimate, we suppose grib files provided as a directory
            // are small files containing a single record
            final int numFiles = gribCollection.size();
            indexMap = Collections
                    .synchronizedMap(new HashMap<Integer, Integer[]>(numFiles));
            int iteration = 0;
            int globalIndex = 0;
            while (gribFilesIt.hasNext()) {
                iteration++;
                GribFile gribFile = gribFilesIt.next();
                final int numRecords = gribFile.getRecordCount();
                for (int i = 0; i < numRecords; i++) {
                    Integer[] indexes = new Integer[] {
                            Integer.valueOf(iteration), Integer.valueOf(i) };
                    indexMap.put(globalIndex, indexes);
                    globalIndex++;
                }
                numImages += numRecords;
            }
        }
        setNumImages(numImages);
    }

    /**
     * Sets the input source to use within this reader. {@code URI}s,
     * {@code File}s (also representing a Directory), {@code String}s (also
     * representing the path of a Directory), {@code URL}s,
     * {@code ImageInputStream}s are accepted input types.<BR>
     * The parameter ({@code seekForwardOnly} is actually ignored.
     * 
     * @param input
     *                the {@code Object} to be set as input of this reader.
     * 
     * @throws {@link IllegalArgumentException}
     *                 in case the provided input {@code Object} cannot be
     *                 properly parsed and used as input for the reader.
     */
    public void setInput(Object input, boolean seekForwardOnly) {
        this.setInput(input);
    }

    /**
     * Sets the input source to use within this reader. {@code URI}s,
     * {@code File}s (also representing a Directory), {@code String}s (also
     * representing the path of a Directory), {@code URL}s,
     * {@code ImageInputStream}s are accepted input types.<BR>
     * 
     * @param input
     *                the {@code Object} to be set as input of this reader.
     * 
     * @throws {@link IllegalArgumentException}
     *                 in case the provided input {@code Object} cannot be
     *                 properly parsed and used as input for the reader.
     */
    public void setInput(Object input) {
        this.setInput(input, true, true);
    }

    public GRIB1ImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    /**
     * Returns an {@code Iterator} containing possible image types to which the
     * requested image may be decoded, in the form of
     * {@code ImageTypeSpecifiers}s. At least one legal image type will be
     * returned.
     * 
     * @param imageIndex
     *                the index of the image to be retrieved.
     * 
     * @return an {@code Iterator} containing the image types.
     */
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex)
            throws IOException {
        final List<ImageTypeSpecifier> l = new java.util.ArrayList<ImageTypeSpecifier>(
                1);

        // Getting a proper GribRecordWrapper for the specified index
        final GribRecordWrapper gw = getGribRecordWrapper(imageIndex);
        ImageTypeSpecifier imageType = new ImageTypeSpecifier(colorModel, gw
                .getSampleModel());
        l.add(imageType);
        return l.iterator();
    }

    /**
     * Returns a {@link GribRecordWrapper} instance given a specified
     * imageIndex.
     * 
     * @param imageIndex
     *                the index of the record for which to retrieve a
     *                {@link GribRecordWrapper}
     * @return a {@link GribRecordWrapper}.
     */
    private GribRecordWrapper getGribRecordWrapper(int imageIndex) {
        // TODO: add more logic
        checkImageIndex(imageIndex);
        GribRecordWrapper wrapper;
        synchronized (gribRecordsMap) {
            if (!gribRecordsMap.containsKey(imageIndex)) {
                wrapper = new GribRecordWrapper(imageIndex);
                gribRecordsMap.put(imageIndex, wrapper);
            } else {
                wrapper = (GribRecordWrapper) gribRecordsMap.get(imageIndex);
                if (wrapper == null) {
                    wrapper = new GribRecordWrapper(imageIndex);
                    gribRecordsMap.put(imageIndex, wrapper);
                }
            }
        }
        return wrapper;
    }

    /**
     * Returns the width of the specified image.
     * 
     * @param imageIndex
     *                the index of the required image
     * @return the width of the specified image.
     * @throws IOException
     */
    public int getWidth(int imageIndex) throws IOException {
        return getGribRecordWrapper(imageIndex).getWidth();
    }

    /**
     * Returns the height of the specified image.
     * 
     * @param imageIndex
     *                the index of the required image
     * @return the height of the specified image.
     * @throws IOException
     */
    public int getHeight(int imageIndex) throws IOException {
        return getGribRecordWrapper(imageIndex).getHeight();
    }

    /**
     * Returns the x-increment/distance between two grid points.
     * 
     * @param imageIndex
     *                the index of the required image
     * @return the x-increment/distance between two grid points.
     * @throws IOException
     */
    double getGridDeltaX(int imageIndex) throws IOException {
        return getGribRecordWrapper(imageIndex).getGDS().getGridDX();
    }

    /**
     * Returns the y-increment/distance between two grid points.
     * 
     * @param imageIndex
     *                the index of the required image
     * @return y-increment/distance between two grid points.
     * @throws IOException
     */
    double getGridDeltaY(int imageIndex) throws IOException {
        return getGribRecordWrapper(imageIndex).getGDS().getGridDY();
    }

    /**
     * Returns the type of grid for the specified image.
     * 
     * @param imageIndex
     *                the index of the required image
     * @return the type of grid for the specified image.
     * @throws IOException
     */
    int getGridType(int imageIndex) throws IOException {
        return getGribRecordWrapper(imageIndex).getGDS().getGridType();
    }

    /**
     * Returns the grid rotation angle of a rotated latitude/longitude grid for
     * the specified image.
     * 
     * @param imageIndex
     *                the index of the required image
     * @return the grid rotation angle of a rotated latitude/longitude grid for
     *         the specified image.
     * @throws IOException
     */
    double getGridRotationAngle(int imageIndex) throws IOException {
        return getGribRecordWrapper(imageIndex).getGDS().getGridRotAngle();
    }

    /**
     * Returns the grid latitude of South Pole of a rotated latitude/longitude
     * grid for the specified image.
     * 
     * @param imageIndex
     *                the index of the required image
     * @return the grid rotation angle of a rotated latitude/longitude grid for
     *         the specified image.
     * @throws IOException
     */
    double getGridLatSP(int imageIndex) throws IOException {
        return getGribRecordWrapper(imageIndex).getGDS().getGridLatSP();
    }

    /**
     * Returns the grid longitude of South Pole of a rotated latitude/longitude
     * grid for the specified image.
     * 
     * @param imageIndex
     *                the index of the required image
     * @return the grid rotation angle of a rotated latitude/longitude grid for
     *         the specified image.
     * @throws IOException
     */
    double getGridLonSP(int imageIndex) throws IOException {
        return getGribRecordWrapper(imageIndex).getGDS().getGridLonSP();
    }

    /**
     * Returns the x-coordinate/latitude of grid start point for the specified
     * image.
     * 
     * @param imageIndex
     *                the index of the required image
     * @return the x-coordinate/latitude of grid start point for the specified
     *         image.
     * @throws IOException
     */
    double getGridLon1(int imageIndex) throws IOException {
        return getGribRecordWrapper(imageIndex).getGDS().getGridLon1();
    }

    /**
     * Returns the x-coordinate/latitude of grid end point for the specified
     * image.
     * 
     * @param imageIndex
     *                the index of the required image
     * @return the x-coordinate/latitude of grid end point for the specified
     *         image.
     * @throws IOException
     */
    double getGridLon2(int imageIndex) throws IOException {
        return getGribRecordWrapper(imageIndex).getGDS().getGridLon2();
    }

    /**
     * Returns the y-coordinate/latitude of grid start point for the specified
     * image.
     * 
     * @param imageIndex
     *                the index of the required image
     * @return the y-coordinate/latitude of grid start point for the specified
     *         image.
     * @throws IOException
     */
    double getGridLat1(int imageIndex) throws IOException {
        return getGribRecordWrapper(imageIndex).getGDS().getGridLat1();
    }

    /**
     * Returns the y-coordinate/latitude of grid end point for the specified
     * image.
     * 
     * @param imageIndex
     *                the index of the required image
     * @return the y-coordinate/latitude of grid end point for the specified
     *         image.
     * @throws IOException
     */
    double getGridLat2(int imageIndex) throws IOException {
        return getGribRecordWrapper(imageIndex).getGDS().getGridLat2();
    }

    /**
     * Get y-coordinate/latitude of south pole of stretching
     * 
     * @param imageIndex
     *                the index of the required image
     * @return latitude of south pole of stretching
     * @throws IOException
     * 
     */
    double getGridLatSPST(int imageIndex) {
        final GribRecordGDS gds = getGribRecordWrapper(imageIndex).getGDS();
        if (gds instanceof GribGDSRotatedLatLon)
            return ((GribGDSRotatedLatLon) gds).getGridLatSPST();
        else
            throw new IllegalArgumentException(
                    "Unable to get the requested parameter for a Not Lambert Conformal Projected Grid");
    }

    /**
     * Get x-coordinate/longitude of south pole of of stretching.
     * 
     * @param imageIndex
     *                the index of the required image
     * @return longitude of south pole of stretching
     * @throws IOException
     * 
     */
    double getGridLonSPST(int imageIndex) {
        final GribRecordGDS gds = getGribRecordWrapper(imageIndex).getGDS();
        if (gds instanceof GribGDSRotatedLatLon)
            return ((GribGDSRotatedLatLon) gds).getGridLonSPST();
        else
            throw new IllegalArgumentException(
                    "Unable to get the requested parameter for a Not Lambert Conformal Projected Grid");
    }

    /**
     * Returns the first latitude from the pole at which cone cuts spherical
     * earth - see note 8 of Table D
     * 
     * @param imageIndex
     *                the index of the required image
     * @return the first latitude from the pole at which cone cuts spherical
     *         earth.
     * @throws IOException
     */
    double getGridLatin1(int imageIndex) throws IOException {
        final GribRecordGDS gds = getGribRecordWrapper(imageIndex).getGDS();
        if (gds instanceof GribGDSLambert)
            return ((GribGDSLambert) gds).getGridLatin1();
        else
            throw new IllegalArgumentException(
                    "Unable to get the requested parameter for a Not Lambert Conformal Projected Grid");
    }

    /**
     * Returns the second latitude from the pole at which cone cuts spherical
     * earth - see note 8 of Table D
     * 
     * @param imageIndex
     *                the index of the required image
     * @return the second latitude from the pole at which cone cuts spherical
     *         earth.
     * @throws IOException
     */
    double getGridLatin2(int imageIndex) throws IOException {
        final GribRecordGDS gds = getGribRecordWrapper(imageIndex).getGDS();
        if (gds instanceof GribGDSLambert)
            return ((GribGDSLambert) gds).getGridLatin2();
        else
            throw new IllegalArgumentException(
                    "Unable to get the requested parameter for a Not Lambert Conformal Projected Grid");
    }

    /**
     * Get starting x value for this grid - THIS IS NOT A LONGITUDE, but an x
     * value calculated for this specific projection, based on an origin of
     * latin1, lov.
     * 
     * @param imageIndex
     *                the index of the required image
     * @return x grid value of first point of this grid
     * @throws IOException
     * 
     */
    double getGridStartX(int imageIndex) {
        final GribRecordGDS gds = getGribRecordWrapper(imageIndex).getGDS();
        if (gds instanceof GribGDSLambert)
            return ((GribGDSLambert) gds).getStartX();
        else
            throw new IllegalArgumentException(
                    "Unable to get the requested parameter for a Not Lambert Conformal Projected Grid");
    }

    /**
     * Get starting y value for this grid - THIS IS NOT A LATITUDE, but an y
     * value calculated for this specific projection, based on an origin of
     * latin1, lov.
     * 
     * @param imageIndex
     *                the index of the required image
     * @return y grid value of first point of this grid
     * @throws IOException
     * 
     */
    double getGridStartY(int imageIndex) {
        final GribRecordGDS gds = getGribRecordWrapper(imageIndex).getGDS();
        if (gds instanceof GribGDSLambert)
            return ((GribGDSLambert) gds).getStartY();
        else
            throw new IllegalArgumentException(
                    "Unable to get the requested parameter for a Not Lambert Conformal Projected Grid");
    }

    // //
    //
    // PDSLevel getters
    //
    // //
    String getPDSLevelDescription(int imageIndex) throws IOException {
        final GribPDSLevel level = getGribRecordWrapper(imageIndex).getLevel();
        return level.getDesc();
    }

    String getPDSLevelName(int imageIndex) throws IOException {
        final GribPDSLevel level = getGribRecordWrapper(imageIndex).getLevel();
        return level.getName();
    }

    String getPDSLevelUnits(int imageIndex) throws IOException {
        final GribPDSLevel level = getGribRecordWrapper(imageIndex).getLevel();
        return level.getUnits();
    }

    String getPDSLevelIsNumeric(int imageIndex) throws IOException {
        final GribPDSLevel level = getGribRecordWrapper(imageIndex).getLevel();
        return Boolean.toString(level.getIsNumeric());
    }
    
    String getPDSLevelIndex(int imageIndex) throws IOException {
        final GribPDSLevel level = getGribRecordWrapper(imageIndex).getLevel();
        return Integer.toString(level.getIndex());
    }

    String getPDSLevelValues(int imageIndex) throws IOException {
        final GribPDSLevel level = getGribRecordWrapper(imageIndex).getLevel();
        float v1 = level.getValue1();
        float v2 = level.getValue2();
        StringBuffer sb = new StringBuffer("");
        if (!Float.isNaN(v1)) {
            sb.append(Float.toString(v1));
            if (!Float.isNaN(v2)) {
                sb.append(GRIB1Utilities.VALUES_SEPARATOR).append(
                        Float.toString(v2));
            }
        }
        return sb.toString();
    }

    String getPDSLevelShortName(int imageIndex) throws IOException {
        final GribPDSLevel level = getGribRecordWrapper(imageIndex).getLevel();
        return level.getLevel();
    }

    /**
     * Returns the orientation of the grid
     * 
     * @param imageIndex
     *                the index of the required image
     * @return east longitude value of meridian parallel to y axis.
     * @throws IOException
     */
    double getGridLov(int imageIndex) throws IOException {
        final GribRecordGDS gds = getGribRecordWrapper(imageIndex).getGDS();
        if (gds instanceof GribGDSLambert)
            return ((GribGDSLambert) gds).getGridLov();
        else
            throw new IllegalArgumentException(
                    "Unable to get the requested parameter for a Not Lambert Conformal Projected Grid");
    }

    String getTime(int imageIndex) throws IOException {
        return GRIB1Utilities
                .getTime(getGribRecordWrapper(imageIndex).getPDS());
    }
    
    int getTimeRangeIndicator(int imageIndex) throws IOException {
        return GRIB1Utilities
                .getTimeRangeIndicator(getGribRecordWrapper(imageIndex).getPDS());
    }

    /**
     * Return a {@code WritableRaster} containing data for a specified
     * {@code GribRecord}
     * 
     * @param item
     *                a {@link GribRecordWrapper} wrapping a specific
     *                {@code GribRecord}
     * @param param
     *                an ImageReadParam to customize read operations
     * @return a {@code WritableRaster} containing data for a specified
     *         {@code GribRecord}
     * @throws IOException
     * @throws NoValidGribException
     */
    private WritableRaster readBDSRaster(GribRecordWrapper item,
            ImageReadParam param) throws IOException, NoValidGribException {

        // ////////////////////////////////////////////////////////////////////
        //
        // -------------------------------------------------------------------
        // Raster Creation >>> Step 1: Initialization
        // -------------------------------------------------------------------
        //
        // ////////////////////////////////////////////////////////////////////

        final int width = item.getWidth();
        final int height = item.getHeight();
        GribRecordBDS bds = item.getBDS();

        if (param == null)
            param = getDefaultReadParam();

        int dstWidth = -1;
        int dstHeight = -1;
        int srcRegionWidth = -1;
        int srcRegionHeight = -1;
        int srcRegionXOffset = -1;
        int srcRegionYOffset = -1;
        int xSubsamplingFactor = -1;
        int ySubsamplingFactor = -1;

        // //
        //
        // Retrieving Information about Source Region and doing
        // additional initialization operations.
        //
        // //
        Rectangle srcRegion = param.getSourceRegion();
        if (srcRegion != null) {
            srcRegionWidth = (int) srcRegion.getWidth();
            srcRegionHeight = (int) srcRegion.getHeight();
            srcRegionXOffset = (int) srcRegion.getX();
            srcRegionYOffset = (int) srcRegion.getY();

            // //
            //
            // Minimum correction for wrong source regions
            //
            // When you do sub-sampling or source sub-setting it might happen
            // that the given source region in the read parameter is incorrect,
            // which means it can be or a bit larger than the original file or
            // can begin a bit before original limits.
            //
            // We got to be prepared to handle such case in order to avoid
            // generating ArrayIndexOutOfBoundsException later in the code.
            //
            // //

            if (srcRegionXOffset < 0)
                srcRegionXOffset = 0;
            if (srcRegionYOffset < 0)
                srcRegionYOffset = 0;
            if ((srcRegionXOffset + srcRegionWidth) > width) {
                srcRegionWidth = width - srcRegionXOffset;
            }
            // initializing dstWidth
            dstWidth = srcRegionWidth;

            if ((srcRegionYOffset + srcRegionHeight) > height) {
                srcRegionHeight = height - srcRegionYOffset;
            }
            // initializing dstHeight
            dstHeight = srcRegionHeight;

        } else {
            // Source Region not specified.
            // Assuming Source Region Dimension equal to Source Image Dimension
            dstWidth = width;
            dstHeight = height;
            srcRegionXOffset = srcRegionYOffset = 0;
            srcRegionWidth = width;
            srcRegionHeight = height;
        }

        // SubSampling variables initialization
        xSubsamplingFactor = param.getSourceXSubsampling();
        ySubsamplingFactor = param.getSourceYSubsampling();
        dstWidth = ((dstWidth - 1) / xSubsamplingFactor) + 1;
        dstHeight = ((dstHeight - 1) / ySubsamplingFactor) + 1;

        // ////////////////////////////////////////////////////////////////////
        //
        // -------------------------------------------------------------------
        // Raster Creation >>> Step 2: Reading the required region
        // -------------------------------------------------------------------
        //
        // ////////////////////////////////////////////////////////////////////

        Rectangle roi = srcRegion != null ? srcRegion : new Rectangle(
                srcRegionXOffset, srcRegionYOffset, srcRegionWidth,
                srcRegionHeight);

        // Translating the child element to the proper location.
        WritableRaster translatedRaster = bds.getValues(roi)
                .createWritableTranslatedChild(0, 0);

        // ////////////////////////////////////////////////////////////////////
        //
        // -------------------------------------------------------------------
        // Raster Creation >>> Step 3: Performing optional subSampling
        // -------------------------------------------------------------------
        //
        // ////////////////////////////////////////////////////////////////////

        // TODO: use some more optimized JAI operation to do subSampling?
        WritableRaster destRaster = Raster.createWritableRaster(
                translatedRaster.getSampleModel().createCompatibleSampleModel(
                        dstWidth, dstHeight), new Point(0, 0));

        final int origRasterWidth = translatedRaster.getWidth();
        final int origRasterHeight = translatedRaster.getHeight();
        float data[] = null;
        for (int i = 0; i < origRasterHeight; i += ySubsamplingFactor)
            for (int j = 0; j < origRasterWidth; j += xSubsamplingFactor) {
                data = translatedRaster.getPixel(j, i, data);
                destRaster.setPixel(j / xSubsamplingFactor, i
                        / ySubsamplingFactor, data);
            }

        return destRaster;
    }

    /**
     * Reads the image indexed by {@code imageIndex} and returns it as a
     * complete {@code BufferedImage}, using a supplied {@code ImageReadParam}.
     * 
     * @param imageIndex
     *                the index of the required image
     * @param param
     *                an {@code ImageReadParam} used to customize the reading
     *                process, or {@code null}
     * @return the requested image as a {@code BufferedImage}
     */
    public BufferedImage read(final int imageIndex, ImageReadParam param)
            throws IOException {
        BufferedImage image = null;

        try {
            // Reading the raster
            final WritableRaster raster = readBDSRaster(
                    getGribRecordWrapper(imageIndex), param);
            image = new BufferedImage(colorModel, raster, false, null);
        } catch (NoSuchElementException e) {
            IOException ioe = new IOException("Error while reading the Raster");
            ioe.initCause(e);
            throw ioe;
        } catch (IndexOutOfBoundsException e) {
            IOException ioe = new IOException("Error while reading the Raster");
            ioe.initCause(e);
            throw ioe;
        } catch (NoValidGribException e) {
            IOException ioe = new IOException("Error while reading the Raster");
            ioe.initCause(e);
            throw ioe;
        }
        return image;
    }

    /**
     */
    public synchronized IIOMetadata getStreamMetadata() {
        throw new UnsupportedOperationException();
    }

    /**
     */
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        return new GRIB1ImageMetadata(this, imageIndex);
    }

    /**
     * Allows any resources held by this reader to be released. <BR>
     * 
     * @todo To grant thread safety, we may prevent a user call of this method.
     */
    public synchronized void dispose() {
        super.dispose();
        if (isSingleFile) {
            if (gribFile != null)
                gribFile.dispose();
        } else {
            if (gribCollection != null) {
                final Iterator<GribFile> filesIt = gribCollection
                        .getGribIterator();
                while (filesIt.hasNext()) {
                    final GribFile file = filesIt.next();
                    file.dispose();
                }
                gribCollection = null;
            }
            // TODO: Improve clear??
            indexMap.clear();
            gribRecordsMap.clear();
        }
    }

    /**
     * Returns {@code true} since this plug-in also supports reading just a
     * Raster of pixel data
     */
    public boolean canReadRaster() {
        return true;
    }

    /**
     * Returns {@code false} since the image is not organized into tiles.
     */
    public boolean isImageTiled(int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        return false;
    }

    /**
     * Returns a {@code RenderedImage} object that contains the contents of the
     * image indexed by {@code imageIndex}.
     * 
     * @param imageIndex
     *                the index of the required image
     * @param param
     *                an {@code ImageReadParam} used to customize the reading
     *                process, or {@code null}
     * @return the requested image as a {@code RenderedImage}
     */
    public RenderedImage readAsRenderedImage(int imageIndex,
            ImageReadParam param) throws IOException {
        return read(imageIndex, param);
    }

    /**
     * Returns a {@code Raster} object containing the raw pixel data from the
     * image stream, without any color conversion applied.
     * 
     * @param imageIndex
     *                the index of the required image
     * @param param
     *                an {@code ImageReadParam} used to customize the reading
     *                process, or {@code null}
     * @return the requested image as a {@code Raster}.
     */
    public Raster readRaster(int imageIndex, ImageReadParam param)
            throws IOException {
        try {
            return readBDSRaster(getGribRecordWrapper(imageIndex), param);
        } catch (NoValidGribException e) {
            IOException ioe = new IOException("Error while reading the Raster");
            ioe.initCause(e);
            throw ioe;
        }
    }

    /**
     * Reset the status of this reader
     */
    public void reset() {
        dispose();
        gribCollection = null;
        gribFile = null;
        gribRecordsMap.clear();
        gribRecordsMap = null;
        isSingleFile = false;
        indexMap = null;
    }

    String getRecordName(int imageIndex) {
        String name = "";
        GribRecordWrapper wrapper = getGribRecordWrapper(imageIndex);
        if (wrapper != null) {
            name = wrapper.getGribRecordParameterName();
        }
        return name;
    }
    
    String getRecordUnit(int imageIndex) {
        String unit = "";
        GribRecordWrapper wrapper = getGribRecordWrapper(imageIndex);
        if (wrapper != null) {
            unit = wrapper.getGribRecordParameterUnit();
        }
        return unit;
    }

    String getRecordDescription(int imageIndex) {
        String description = "";
        GribRecordWrapper wrapper = getGribRecordWrapper(imageIndex);
        if (wrapper != null) {
            description = wrapper.getGribRecordParameterDescription();
        }
        return description;
    }

    int[] getParameterDescriptor(int imageIndex) {
        GribRecordWrapper wrapper = getGribRecordWrapper(imageIndex);
        if (wrapper != null) {
            String parameterId = wrapper.getParamID();
            return GRIB1Utilities.getParamDescriptors(parameterId);
        }
        return null;
    }
}
