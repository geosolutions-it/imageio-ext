/*
 * $RCSfile: TIFFImageReader.java,v $
 *
 * 
 * Copyright (c) 2005 Sun Microsystems, Inc. All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 
 * 
 * - Redistribution of source code must retain the above copyright 
 *   notice, this  list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in 
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL 
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR 
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES. 
 * 
 * You acknowledge that this software is not designed or intended for 
 * use in the design, construction, operation or maintenance of any 
 * nuclear facility. 
 *
 * $Revision: 1.13 $
 * $Date: 2007/12/19 20:17:02 $
 * $State: Exp $
 */
/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2016, GeoSolutions
 *    All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of GeoSolutions nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY GeoSolutions ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GeoSolutions BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package it.geosolutions.imageioimpl.plugins.tiff;

import com.sun.media.imageioimpl.common.ImageUtil;
import com.sun.media.imageioimpl.common.PackageUtil;
import it.geosolutions.imageio.maskband.DatasetLayout;
import it.geosolutions.imageio.plugins.tiff.BaselineTIFFTagSet;
import it.geosolutions.imageio.plugins.tiff.PrivateTIFFTagSet;
import it.geosolutions.imageio.plugins.tiff.TIFFColorConverter;
import it.geosolutions.imageio.plugins.tiff.TIFFDecompressor;
import it.geosolutions.imageio.plugins.tiff.TIFFField;
import it.geosolutions.imageio.plugins.tiff.TIFFImageReadParam;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.imageioimpl.plugins.tiff.gdal.GDALMetadata;
import it.geosolutions.imageioimpl.plugins.tiff.gdal.GDALMetadataParser;

import org.w3c.dom.Node;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteOrder;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TIFFImageReader extends ImageReader {

    private final static Logger LOGGER = Logger.getLogger(TIFFImageReader.class.toString());

    /**
     * This class can be used to cache basic information about a tiff page.
     * <p>
     * Notice that we hold {@link IIOMetadata} using {@link SoftReference}s since 
     * they might be big so we might want to reclaim the least used ones.
     * 
     * @author Simone Giannecchini, GeoSoltions S.A.S.
     *
     */
    protected final static class PageInfo {

        private SoftReference<TIFFImageMetadata> imageMetadata;

        protected PageInfo(
                TIFFImageMetadata imageMetadata,
                boolean bigtiff, 
                int[] bitsPerSample, 
                char[] colorMap, 
                int compression,
                int height, 
                int numBands, 
                int photometricInterpretation, 
                int width,
                int tileOrStripWidth, 
                int tileOrStripHeight, 
                int planarConfiguration,
                boolean isImageTiled, 
                int samplesPerPixel, 
                int[] sampleFormat, 
                int[] extraSamples,
                Double noData,
                Double[] offsets,
                Double[] scales) {
            this.imageMetadata = new SoftReference<TIFFImageMetadata>(imageMetadata);
            this.bigtiff = bigtiff;
            this.bitsPerSample = bitsPerSample;
            this.colorMap = colorMap;
            this.compression = compression;
            this.height = height;
            this.numBands = numBands;
            this.photometricInterpretation = photometricInterpretation;
            this.width = width;
            this.tileOrStripWidth = tileOrStripWidth;
            this.tileOrStripHeight = tileOrStripHeight;
            this.planarConfiguration = planarConfiguration;
            this.isImageTiled = isImageTiled;
            this.samplesPerPixel = samplesPerPixel;
            this.sampleFormat = sampleFormat;
            this.extraSamples = extraSamples;
            this.noData = noData;
            this.offsets = offsets;
            this.scales = scales;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (bigtiff ? 1231 : 1237);
            result = prime * result + Arrays.hashCode(bitsPerSample);
            result = prime * result + Arrays.hashCode(colorMap);
            result = prime * result + compression;
            result = prime * result + Arrays.hashCode(extraSamples);
            result = prime * result + height;
            result = prime * result + (isImageTiled ? 1231 : 1237);
            result = prime * result + ((noData == null) ? 0 : noData.hashCode());
            result = prime * result + numBands;
            result = prime * result + photometricInterpretation;
            result = prime * result + planarConfiguration;
            result = prime * result + Arrays.hashCode(sampleFormat);
            result = prime * result + samplesPerPixel;
            result = prime * result + tileOrStripHeight;
            result = prime * result + tileOrStripWidth;
            result = prime * result + width;
            result = prime * result + Arrays.hashCode(offsets);
            result = prime * result + Arrays.hashCode(scales);
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof PageInfo))
                return false;
            PageInfo other = (PageInfo) obj;
            if (bigtiff != other.bigtiff)
                return false;
            if (!Arrays.equals(bitsPerSample, other.bitsPerSample))
                return false;
            if (!Arrays.equals(colorMap, other.colorMap))
                return false;
            if (compression != other.compression)
                return false;
            if (!Arrays.equals(extraSamples, other.extraSamples))
                return false;
            if (height != other.height)
                return false;
            if (isImageTiled != other.isImageTiled)
                return false;
            if (noData == null) {
                if (other.noData != null)
                    return false;
            } else if (!noData.equals(other.noData))
                return false;
            if (numBands != other.numBands)
                return false;
            if (photometricInterpretation != other.photometricInterpretation)
                return false;
            if (planarConfiguration != other.planarConfiguration)
                return false;
            if (!Arrays.equals(sampleFormat, other.sampleFormat))
                return false;
            if (samplesPerPixel != other.samplesPerPixel)
                return false;
            if (tileOrStripHeight != other.tileOrStripHeight)
                return false;
            if (tileOrStripWidth != other.tileOrStripWidth)
                return false;
            if (width != other.width)
                return false;
            if (!Arrays.equals(offsets, other.offsets))
                return false;
            if (!Arrays.equals(scales, other.scales))
                return false;

            return true;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "PageInfo [bigtiff=" + bigtiff + ", bitsPerSample="
                    + Arrays.toString(bitsPerSample) + ", colorMap=" + Arrays.toString(colorMap)
                    + ", compression=" + compression + ", extraSamples="
                    + Arrays.toString(extraSamples) + ", height=" + height + ", isImageTiled="
                    + isImageTiled + ", numBands=" + numBands + ", photometricInterpretation="
                    + photometricInterpretation + ", planarConfiguration=" + planarConfiguration
                    + ", sampleFormat=" + Arrays.toString(sampleFormat) + ", samplesPerPixel="
                    + samplesPerPixel + ", tileOrStripHeight=" + tileOrStripHeight
                    + ", tileOrStripWidth=" + tileOrStripWidth + ", width=" + width 
                    + ", noData=" + noData
                    + ", offsets=" + Arrays.toString(offsets)
                    + ", scales=" + Arrays.toString(scales)
                    + "]";
        }

        private boolean bigtiff = false;

        private int[] bitsPerSample;

        private char[] colorMap;

        private int compression;

        private int height = -1;

        private int numBands = -1;

        private int photometricInterpretation;

        private int width = -1;
        
        private int tileOrStripWidth = -1, tileOrStripHeight = -1;

        private int planarConfiguration = BaselineTIFFTagSet.PLANAR_CONFIGURATION_CHUNKY;

        private boolean isImageTiled= false;

        private int samplesPerPixel;

        private int[] sampleFormat;

        private int[] extraSamples;

        private Double noData = null; //Using a Double to allow null value.

        private Double[] scales;

        private Double[] offsets;

    }

    private static final boolean DEBUG = false; // XXX 'false' for release!!!

    /** Constant Value for External Mask suffix*/
    private static final String MASK_SUFFIX = ".msk";

    /** Constant Value for External Overview suffix*/
    private static final String OVR_SUFFIX = ".ovr";

    private int magic = -1;
    
    private Map<Integer, PageInfo> pagesInfo = new HashMap<Integer, PageInfo>();

    private boolean bigtiff = false;
        
    // The current ImageInputStream source.
    protected ImageInputStream stream = null;

    // True if the file header has been read.
    protected boolean gotTiffHeader = false;
    
    // true if we already have parsed metadata for this element
    protected boolean initialized = false;

    protected ImageReadParam imageReadParam = getDefaultReadParam();

    // Stream metadata, or null.
    protected TIFFStreamMetadata streamMetadata = null;

    // The current image index.
    protected int currIndex = -1;

    // Metadata for image at 'currIndex', or null.
    protected TIFFImageMetadata imageMetadata = null;
    
    /**
     * A <code>List</code> of <code>Long</code>s indicating the stream positions of the start of the
     * IFD for each image. Entries are added as needed.
     */
    List<Long> imageStartPosition = new ArrayList<Long>();

    // The number of images in the stream, if known, otherwise -1.
    int numImages = -1;

    // The ImageTypeSpecifiers of the images in the stream.
    // Contains a map of Integers to Lists.
    HashMap<Integer, List<ImageTypeSpecifier>> imageTypeMap = new HashMap<Integer, List<ImageTypeSpecifier>>();

    protected BufferedImage theImage = null;

    protected int width = -1;
    protected int height = -1;
    protected int numBands = -1;
    protected int tileOrStripWidth = -1, tileOrStripHeight = -1;

    protected int planarConfiguration = BaselineTIFFTagSet.PLANAR_CONFIGURATION_CHUNKY;

    protected int compression;
    protected int photometricInterpretation;
    protected int samplesPerPixel;
    protected int[] sampleFormat;
    protected int[] bitsPerSample;
    protected int[] extraSamples;
    protected char[] colorMap;

    protected int sourceXOffset;
    protected int sourceYOffset;
    protected int srcXSubsampling;
    protected int srcYSubsampling;

    protected int dstWidth;
    protected int dstHeight;
    protected int dstMinX;
    protected int dstMinY;
    protected int dstXOffset;
    protected int dstYOffset;

    protected int tilesAcross;
    protected int tilesDown;

    protected int pixelsRead;
    protected int pixelsToRead;


    private boolean isImageTiled= false;

    protected Double noData = null;

    private Double[] scales;

    private Double[] offsets;

    // BAND MASK RELATED FIELDS
    /** {@link DatasetLayout} implementation containing info about overviews and masks*/
    private TiffDatasetLayoutImpl layout;

    /** External File containing TIFF masks*/
    private File externalMask;

    /** External File containing TIFF Overviews*/
    private File externalOverviews;

    /** External File containing TIFF masks overviews*/
    private File maskOverviews;

    public TIFFImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    public void setInput(Object input,
                         boolean seekForwardOnly,
                         boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);

        // Clear all local values based on the previous stream contents.
        resetLocal();

        if (input != null) {
            if (!(input instanceof ImageInputStream)) {
                throw new IllegalArgumentException
                    ("input not an ImageInputStream!"); 
            }
            this.stream = (ImageInputStream)input;
            // Check for external masks/overviews
            if (!ImageIOUtilities.isSkipExternalFilesLookup() && input instanceof FileImageInputStreamExtImpl) {

                FileImageInputStreamExtImpl stream = (FileImageInputStreamExtImpl) input;
                // Getting File path
                File inputFile = stream.getFile();
                if (inputFile != null) {
                    // Getting Parent
                    File parent = inputFile.getParentFile();
                    // Getting Mask file name
                    File mask = new File(parent, inputFile.getName() + MASK_SUFFIX);
                    // Check if exists and can be read
                    if (mask.exists() && mask.canRead()) {
                        externalMask = mask;
                        // Getting external Mask Overviews
                        File mskOverviews = new File(mask.getAbsolutePath() + OVR_SUFFIX);
                        // Check if the file exists and can be read
                        if (mskOverviews.exists() && mskOverviews.canRead()) {
                            maskOverviews = mskOverviews;
                        }
                    }
                    // Getting Overviews file name
                    File ovr = new File(parent, inputFile.getName() + OVR_SUFFIX);
                    // Check if exists and can be read
                    if (ovr.exists() && ovr.canRead()) {
                        externalOverviews = ovr;
                    }
                }
            }
        } else {
            this.stream = null;
        }
        // Creating the New DatasetLayout for handling Overviews and Masking
        layout = new TiffDatasetLayoutImpl();
    }

    // Do not seek to the beginning of the stream so as to allow users to
    // point us at an IFD within some other file format
    private void readHeader() throws IIOException {
        if (gotTiffHeader) {
            return;
        }
        if (stream == null) {
            throw new IllegalStateException("Input not set!");
        }

        // Create an object to store the stream metadata
        this.streamMetadata = new TIFFStreamMetadata();
        
        try {
            int byteOrder = stream.readUnsignedShort();
            if (byteOrder == 0x4d4d) {
                streamMetadata.byteOrder = ByteOrder.BIG_ENDIAN;
                stream.setByteOrder(ByteOrder.BIG_ENDIAN);
            } else if (byteOrder == 0x4949) {
                streamMetadata.byteOrder = ByteOrder.LITTLE_ENDIAN;
                stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            } else {
                processWarningOccurred(
                           "Bad byte order in header, assuming little-endian");
                streamMetadata.byteOrder = ByteOrder.LITTLE_ENDIAN;
                stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            }
          
            magic = stream.readUnsignedShort();
            if (magic != 42 && magic != 43) {
                processWarningOccurred(
                                     "Bad magic number in header, continuing");
            }
            
            if (magic == 43)
                bigtiff=true;
            else 
                bigtiff=false;
            
            // Seek to start of first IFD
            long offset = -1;
            
            switch(magic) {
            case 42:
                offset = stream.readUnsignedInt();
                break;
            case 43:
                int alwaysEight = stream.readUnsignedShort();
                if (alwaysEight != 8){
                        processWarningOccurred("No BigTiff file format");
                }
                int alwaysZero = stream.readUnsignedShort();
                if (alwaysZero != 0){
                        processWarningOccurred("No BigTiff file format");
                }
                
                offset = stream.readLong();
                break;
            }
 
            if (offset >= 0) {
                imageStartPosition.add(Long.valueOf(offset));
                stream.seek(offset);
            } else 
                processWarningOccurred("Error calculating offset");
                     
            
        } catch (IOException e) {
            throw new IIOException("I/O error reading header!", e);
        }
        
        
        gotTiffHeader = true;
    }

    /**
     * Method used for populating reader's {@link DatasetLayout}
     * 
     * @throws IIOException
     */
    private void defineDatasetLayout() throws IIOException {
        if (!(layout.getNumInternalOverviews() == -1 && layout.getNumInternalMasks() == -1)) {
            return;
        }
        // Initialize
        readHeader();
        // Getting Image Number
        int numImg;
        try {
            numImg = getNumImages(true);
        } catch (IOException e) {
            throw new IIOException(e.getMessage(), e);
        }
        // Extracting the TIFF Tag NewSubfileType from each Image
        int numOverviews = 0;
        int numMasks = 0;
        int numMaskOverView = 0;
        // If not all the Images Metadata have been loaded, loop through images in order to add them
        // if(pagesInfo != null && (numImg != pagesInfo.size())){
        // Getting current Index which will be restored at the end of the operation
        int currentIdx = currIndex;
        // Loop the images
        for (int i = 0; i < numImg; i++) {
            // Seek to the image index i
            seekToImage(i);
            // Getting PageInfo
            PageInfo info = pagesInfo.get(i);
            // Getting Metadata
            TIFFImageMetadata metadata = info.imageMetadata.get();
            // Getting TIFF TAG_NEW_SUBFILE_TYPE
            TIFFField f = metadata.getTIFFField(BaselineTIFFTagSet.TAG_NEW_SUBFILE_TYPE);
            // Checking if exists
            if (f != null) {
                Object data = f.getData();
                // Checking if the data is LONG
                if (data instanceof long[]) {
                    long ldata = ((long[]) data)[0];
                    numMasks += ((ldata & BaselineTIFFTagSet.NEW_SUBFILE_TYPE_TRANSPARENCY) > 0) ? 1
                            : 0;
                    numOverviews += ((ldata & BaselineTIFFTagSet.NEW_SUBFILE_TYPE_REDUCED_RESOLUTION) > 0) ? 1
                            : 0;
                    numMaskOverView += ((ldata & BaselineTIFFTagSet.NEW_SUBFILE_TYPE_REDUCED_RESOLUTION) > 0 && (ldata & BaselineTIFFTagSet.NEW_SUBFILE_TYPE_TRANSPARENCY) > 0) ? 1
                            : 0;
                }
            }
        }
        // Restore current Index
        seekToImage(currentIdx);
        // Setting Masks number and Overviews number
        layout.setNumInternalMasks(numMasks);
        layout.setNumInternalOverviews(numOverviews - numMaskOverView);
    }

    /**
     * Method which checks if external masks or overviews are present and, if so, 
     * updates the {@link DatasetLayout}
     * 
     * @throws IIOException
     */
    private void defineExternalMasks() throws IIOException {
        // Check if already initialized
        if ((externalMask == null || layout.getExternalMasks() != null)
                && (maskOverviews == null || layout.getExternalMaskOverviews() != null)
                && (externalOverviews == null || layout.getExternalOverviews() != null)) {
            return;
        }
        // Initialize
        readHeader();
        // Setting Values to the DatasetLayout
        layout.setExternalMasks(externalMask);
        layout.setExternalMaskOverviews(maskOverviews);
        layout.setExternalOverviews(externalOverviews);
        layout.setNumExternalMasks(getNumImages(externalMask));
        layout.setNumExternalMaskOverviews(getNumImages(maskOverviews));
        layout.setNumExternalOverviews(getNumImages(externalOverviews));
    }

    /**
     * Simple method for accessing the number of images inside an input file
     * 
     * @param inputFile Input {@link File} to check
     * @return An integer defining the number of images contained in the input file
     * @throws IIOException
     */
    private int getNumImages(File inputFile) throws IIOException {
        // Init value to 0
        int numImg = 0;
        // Creating a new TIFFImageReader instance for reading
        ImageReader reader = null;
        // Initializing a null stream
        FileImageInputStreamExtImpl stream = null;
        // Searching for external Overviews
        if (inputFile != null) {
            try {
                // Getting mask data stream
                stream = new FileImageInputStreamExtImpl(inputFile);
                // Creating the Reader
                reader = originatingProvider.createReaderInstance();
                reader.setInput(stream);
                // Getting the image number (Indicates the Mask number)
                numImg = reader.getNumImages(true);
            } catch (IOException e) {
                throw new IIOException("Unable to open input .msk file", e);
            } finally {
                // Closing reader
                if (reader != null) {
                    try {
                        reader.dispose();
                    } catch (Exception e) {
                        // Eat the Exception
                    }
                }
                // Closing stream
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (Exception e) {
                        // Eat the Exception
                    }
                }
            }
        }
        return numImg;
    }
    
    private int locateImage(int imageIndex) throws IIOException {
        readHeader();

        try {
            // Find closest known index
            int index = Math.min(imageIndex, imageStartPosition.size() - 1);

            // Seek to that position
            long l = imageStartPosition.get(index);
            stream.seek(l);

            // Skip IFDs until at desired index or last image found
            switch(magic) {
            case 42:
                while (index < imageIndex) {
                    int count = stream.readUnsignedShort();
                    stream.skipBytes(12 * count);

                    long offset = stream.readUnsignedInt();
                    if (offset == 0) {
                        if(DEBUG)
                            System.out.println("Offset 0 in locate");
                        currIndex=index;
                        imageMetadata = null;
                        // the current image index has changed, we got to reinitialized
                        initialized = false;                        
                        return index;
                    }

                    imageStartPosition.add(Long.valueOf(offset));
                    stream.seek(offset);
                    ++index;
                }
                break;
            
            case 43:
                while (index < imageIndex) {
                    long count = stream.readLong();
                    stream.skipBytes(20*count);

                    long offset = stream.readLong();
                    if (offset == 0) {
                        currIndex=index;
                        imageMetadata = null;
                        // the current image index has changed, we got to reinitialized
                        initialized = false;                        
                        return index;
                    }
                    
                    imageStartPosition.add(Long.valueOf(offset));
                    stream.seek(offset);
                    ++index;
                }
                break;
            }
        } catch (IOException e) {
            throw new IIOException("Couldn't seek!", e);
        }

        // are we changing ImageIndex??? If so, we got to reload the ImageMetadata as well as the
        // various fields we initialized from them!
        if (currIndex != imageIndex) {
            imageMetadata = null;
            // the current image index has changed, we got to reinitialized
            initialized = false;
        }
        currIndex = imageIndex;
        return imageIndex;
    }

    public int getNumImages(boolean allowSearch) throws IOException {
        if (stream == null) {
            throw new IllegalStateException("Input not set!");
        }
        if (seekForwardOnly && allowSearch) {
            throw new IllegalStateException
                ("seekForwardOnly and allowSearch can't both be true!");
        }

        if (numImages > 0) {
            return numImages;
        }
        if (allowSearch) {
            this.numImages = locateImage(Integer.MAX_VALUE) + 1;
        }
        return numImages;
    }

    public IIOMetadata getStreamMetadata() throws IIOException {
        readHeader();
        // Defining DatasetLayout
        defineDatasetLayout();
        // Defining External Masks
        defineExternalMasks();
        // Setting the Layout
        streamMetadata.dtLayout = layout;
        return streamMetadata;
    }

    /**
     * Throw an IndexOutOfBoundsException if index < minIndex, and bump minIndex if required.
     * @param imageIndex
     */
    private void checkIndex(int imageIndex) {
        if (imageIndex < minIndex) {
            throw new IndexOutOfBoundsException("imageIndex < minIndex!");
        }
        if (seekForwardOnly) {
            minIndex = imageIndex;
        }
    }

    /** 
     * Verify that imageIndex is in bounds, find the image IFD, read the image metadata, initialize instance variables from the metadata.
     * @param imageIndex
     * @throws IIOException
     */
    private void seekToImage(int imageIndex) throws IIOException {
        seekToImage(imageIndex, true);
    }
    
    /** 
     * Verify that imageIndex is in bounds, find the image IFD, read the image metadata, initialize instance variables from the metadata.
     * @param imageIndex
     * @param optimized
     * @throws IIOException
     */
    private void seekToImage(int imageIndex, boolean optimized) throws IIOException {
        checkIndex(imageIndex);

        // TODO we should do this initialization just once!!!
        int index = locateImage(imageIndex);
        if (index != imageIndex) {
            throw new IndexOutOfBoundsException("imageIndex out of bounds!");
        }
        
        final Integer i= Integer.valueOf(index);
        //optimized branch
        if(!optimized){
            
            readMetadata();
            initializeFromMetadata();
        	return;
        }
        // in case we have cache the info for this page
        if(pagesInfo.containsKey(i)){
            // initialize from cachedinfo only if needed
            // TODO Improve
            if(imageMetadata == null || !initialized) {// this means the curindex has changed
                final PageInfo info = pagesInfo.get(i);
                final TIFFImageMetadata metadata = info.imageMetadata.get();
                if (metadata != null) {
                    initializeFromCachedInfo(info, metadata);
                    return;
                }
                pagesInfo.put(i,null);
                    
            }
        }
        
        readMetadata();
        initializeFromMetadata();
    }

    private void initializeFromCachedInfo(PageInfo pageInfo, TIFFImageMetadata imageMetadata) {
        this.bigtiff = pageInfo.bigtiff;
        this.bitsPerSample = pageInfo.bitsPerSample;
        this.colorMap = pageInfo.colorMap;
        this.compression = pageInfo.compression;
        this.extraSamples = pageInfo.extraSamples;
        this.height = pageInfo.height;
        this.isImageTiled = pageInfo.isImageTiled;
        this.numBands = pageInfo.numBands;
        this.photometricInterpretation = pageInfo.photometricInterpretation;
        this.planarConfiguration = pageInfo.planarConfiguration;
        this.sampleFormat = pageInfo.sampleFormat;
        this.samplesPerPixel = pageInfo.samplesPerPixel;
        this.tileOrStripHeight = pageInfo.tileOrStripHeight;
        this.tileOrStripWidth = pageInfo.tileOrStripWidth;
        this.width = pageInfo.width;
        this.noData = pageInfo.noData;
        this.imageMetadata = imageMetadata;
    }

    // Stream must be positioned at start of IFD for 'currIndex'
    private void readMetadata() throws IIOException {

        if (imageMetadata != null) {
            return;
        }
        
        if (stream == null) {
            throw new IllegalStateException("Input not set!");
        }
        
        try {
            // Create an object to store the image metadata
            List<BaselineTIFFTagSet> tagSets;
            if (imageReadParam instanceof TIFFImageReadParam) {
                tagSets =
                    ((TIFFImageReadParam)imageReadParam).getAllowedTagSets();
            } else {
                tagSets = new ArrayList<BaselineTIFFTagSet>(1);
                tagSets.add(BaselineTIFFTagSet.getInstance());
            }

            this.imageMetadata = new TIFFImageMetadata(tagSets);
            imageMetadata.initializeFromStream(stream, ignoreMetadata, bigtiff);
            // we got to reinitialize!!!
            initialized = false;
        } catch (IIOException iioe) {
            throw iioe;
        } catch (IOException ioe) {
            throw new IIOException("I/O error reading image metadata!", ioe);
        }
    }

    // Returns tile width if image is tiled, else image width
    private int getTileOrStripWidth() {
        TIFFField f =
                imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_TILE_WIDTH);
        return (f == null) ? width : f.getAsInt(0);
    }

    // Returns tile height if image is tiled, else strip height
    private int getTileOrStripHeight() {
        int h =-1;
        TIFFField f =
            imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_TILE_LENGTH);
        if (f != null) {
            h= f.getAsInt(0);
        }else{
        
            f = imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_ROWS_PER_STRIP);
            // Default for ROWS_PER_STRIP is 2^32 - 1, i.e., infinity
                h = (f == null) ? -1 : f.getAsInt(0);
        }
        return (h == -1) ? height : h;
    }

    private int getPlanarConfiguration() {
        TIFFField f =
        imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_PLANAR_CONFIGURATION);
        if (f != null) {
            int planarConfigurationValue = f.getAsInt(0);
            if(planarConfigurationValue ==
               BaselineTIFFTagSet.PLANAR_CONFIGURATION_PLANAR) {
                // Some writers (e.g. Kofax standard Multi-Page TIFF
                // Storage Filter v2.01.000; cf. bug 4929147) do not
                // correctly set the value of this field. Attempt to
                // ascertain whether the value is correctly Planar.
                if(compression ==
                   BaselineTIFFTagSet.COMPRESSION_OLD_JPEG &&
                   imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_JPEG_INTERCHANGE_FORMAT) !=
                   null) {
                    // JPEG interchange format cannot have
                    // PlanarConfiguration value Chunky so reset.
                    processWarningOccurred("PlanarConfiguration \"Planar\" value inconsistent with JPEGInterchangeFormat; resetting to \"Chunky\".");
                    planarConfigurationValue =
                        BaselineTIFFTagSet.PLANAR_CONFIGURATION_CHUNKY;
                } else {
                    TIFFField offsetField =
                        imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_TILE_OFFSETS);
                    if (offsetField == null) {
                        // Tiles
                        offsetField =
                            imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_STRIP_OFFSETS);
                        int tw = tileOrStripWidth;
                        int th = tileOrStripHeight;
                        int tAcross = (width + tw - 1)/tw;
                        int tDown = (height + th - 1)/th;
                        int tilesPerImage = tAcross*tDown;
                        long[] offsetArray = offsetField.getAsLongs();
                        if(offsetArray != null &&
                           offsetArray.length == tilesPerImage) {
                            // Length of offsets array is
                            // TilesPerImage for Chunky and
                            // SamplesPerPixel*TilesPerImage for Planar.
                            processWarningOccurred("PlanarConfiguration \"Planar\" value inconsistent with TileOffsets field value count; resetting to \"Chunky\".");
                            planarConfigurationValue =
                                BaselineTIFFTagSet.PLANAR_CONFIGURATION_CHUNKY;
                        }
                    } else {
                        // Strips
                        int rowsPerStrip = tileOrStripHeight;
                        int stripsPerImage =
                            (height + rowsPerStrip - 1)/rowsPerStrip;
                        long[] offsetArray = offsetField.getAsLongs();
                        if(offsetArray != null &&
                           offsetArray.length == stripsPerImage) {
                            // Length of offsets array is
                            // StripsPerImage for Chunky and
                            // SamplesPerPixel*StripsPerImage for Planar.
                            processWarningOccurred("PlanarConfiguration \"Planar\" value inconsistent with StripOffsets field value count; resetting to \"Chunky\".");
                            planarConfigurationValue =
                                BaselineTIFFTagSet.PLANAR_CONFIGURATION_CHUNKY;
                        }
                    }
                }
            }
            return planarConfigurationValue;
        }

        return BaselineTIFFTagSet.PLANAR_CONFIGURATION_CHUNKY;
    }

    protected long getTileOrStripOffset(int tileIndex) throws IIOException {
        TIFFField f =
            imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_TILE_OFFSETS);
        if (f == null) {
            f = imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_STRIP_OFFSETS);
        }
        if (f == null) {
            f = imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_JPEG_INTERCHANGE_FORMAT);
        }

        if(f == null) {
            throw new IIOException
                ("Missing required strip or tile offsets field.");
        }

        return f.getAsLong(tileIndex);
    }

    protected long getTileOrStripByteCount(int tileIndex) throws IOException {
        TIFFField f =
           imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_TILE_BYTE_COUNTS);
        if (f == null) {
            f =
          imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_STRIP_BYTE_COUNTS);
        }
        if (f == null) {
            f = imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH);
        }

        long tileOrStripByteCount;
        if(f != null) {
            tileOrStripByteCount = f.getAsLong(tileIndex);
        } else {
            processWarningOccurred("TIFF directory contains neither StripByteCounts nor TileByteCounts field: attempting to calculate from strip or tile width and height.");

            // Initialize to number of bytes per strip or tile assuming
            // no compression.
            int bitsPerPixel = bitsPerSample[0];
            for(int i = 1; i < samplesPerPixel; i++) {
                bitsPerPixel += bitsPerSample[i];
            }
            int bytesPerRow = (tileOrStripWidth*bitsPerPixel + 7)/8;
            tileOrStripByteCount = bytesPerRow*tileOrStripHeight;

            // Clamp to end of stream if possible.
            long streamLength = stream.length();
            if(streamLength != -1) {
                tileOrStripByteCount =
                    Math.min(tileOrStripByteCount,
                             streamLength - getTileOrStripOffset(tileIndex));
            } else {
                processWarningOccurred("Stream length is unknown: cannot clamp estimated strip or tile byte count to EOF.");
            }
        }

        return tileOrStripByteCount;
    }

    protected int getCompression() {
        TIFFField f =
            imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_COMPRESSION);
        if (f == null) {
            processWarningOccurred("Compression field is missing; assuming no compression");
            return BaselineTIFFTagSet.COMPRESSION_NONE;
        } else {
            return f.getAsInt(0);
        }
    }

    public int getWidth(int imageIndex) throws IOException {
        seekToImage(imageIndex);
        return width;
    }

    public int getHeight(int imageIndex) throws IOException {
        seekToImage(imageIndex);
        return height;
    }

    /**
     * Initializes these instance variables from the image metadata:
     * <pre>
     * compression
     * width
     * height
     * samplesPerPixel
     * numBands
     * colorMap
     * photometricInterpretation
     * sampleFormat
     * bitsPerSample
     * extraSamples
     * tileOrStripWidth
     * tileOrStripHeight
     * </pre>
     */
    private void initializeFromMetadata() {
        if(initialized)
            return;

        //
        // Planar Config
        //
        this.planarConfiguration = getPlanarConfiguration();
        
        //
        // Compression
        //
        compression=getCompression();

        // Whether key dimensional information is absent.
        boolean isMissingDimension = false;

        //
        // ImageWidth -> width
        //
        TIFFField f = imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_IMAGE_WIDTH);
        if (f != null) {
            this.width = f.getAsInt(0);
        } else {
            processWarningOccurred("ImageWidth field is missing.");
            isMissingDimension = true;
        }

        //
        // ImageLength -> height
        //
        f = imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_IMAGE_LENGTH);
        if (f != null) {
            this.height = f.getAsInt(0);
        } else {
            processWarningOccurred("ImageLength field is missing.");
            isMissingDimension = true;
        }
        
        //
        // Tiling
        //
        tileOrStripWidth = getTileOrStripWidth();
        tileOrStripHeight = getTileOrStripHeight();
        f =imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_TILE_WIDTH);
        isImageTiled = f != null;
        
        //
        // SamplesPerPixel
        //
        f =
          imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_SAMPLES_PER_PIXEL);
        if (f != null) {
            samplesPerPixel = f.getAsInt(0);
        } else {
            samplesPerPixel = 1;
            isMissingDimension = true;
        }

        // If any dimension is missing and there is a JPEG stream available
        // get the information from it.
        int defaultBitDepth = 1;
        if(isMissingDimension &&
           (f = imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_JPEG_INTERCHANGE_FORMAT)) != null) {
            Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName("JPEG");
            if(iter != null && iter.hasNext()) {
                ImageReader jreader = iter.next();
                try {
                    stream.mark();
                    stream.seek(f.getAsLong(0));
                    jreader.setInput(stream);
                    if(imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_IMAGE_WIDTH) == null) {
                        this.width = jreader.getWidth(0);
                    }
                    if(imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_IMAGE_LENGTH) == null) {
                        this.height = jreader.getHeight(0);
                    }
                    ImageTypeSpecifier imageType = jreader.getRawImageType(0);
                    if(imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_SAMPLES_PER_PIXEL) == null) {
                        this.samplesPerPixel =
                            imageType.getSampleModel().getNumBands();
                    }
                    stream.reset();
                    defaultBitDepth =
                        imageType.getColorModel().getComponentSize(0);
                } catch(IOException e) {
                    // Ignore it and proceed: an error will occur later.
                }
                jreader.dispose();
            }
        }

        if (samplesPerPixel < 1) {
            processWarningOccurred("Samples per pixel < 1!");
        }

        //
        // SamplesPerPixel -> numBands
        //
        numBands = samplesPerPixel;

        //
        // ColorMap
        //
        this.colorMap = null;
        f = imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_COLOR_MAP);
        if (f != null) {
            // Grab color map
            colorMap = f.getAsChars();
        }

        //
        // PhotometricInterpretation
        //
        f =
        imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_PHOTOMETRIC_INTERPRETATION);
        if (f == null) {
            if (compression == BaselineTIFFTagSet.COMPRESSION_CCITT_RLE ||
                compression == BaselineTIFFTagSet.COMPRESSION_CCITT_T_4 ||
                compression == BaselineTIFFTagSet.COMPRESSION_CCITT_T_6) {
                processWarningOccurred
                    ("PhotometricInterpretation field is missing; "+
                     "assuming WhiteIsZero");
                photometricInterpretation =
                   BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_WHITE_IS_ZERO;
            } else if(this.colorMap != null) {
                photometricInterpretation =
                    BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_PALETTE_COLOR;
            } else if(samplesPerPixel == 3 || samplesPerPixel == 4) {
                photometricInterpretation =
                    BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_RGB;
            } else {
                processWarningOccurred
                    ("PhotometricInterpretation field is missing; "+
                     "assuming BlackIsZero");
                photometricInterpretation =
                   BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_BLACK_IS_ZERO;
            }
        } else {
            photometricInterpretation = f.getAsInt(0);
        }

        //
        // SampleFormat
        //
        boolean replicateFirst = false;
        int first = -1;

        f = imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_SAMPLE_FORMAT);
        sampleFormat = new int[samplesPerPixel];
        replicateFirst = false;
        if (f == null) {
            replicateFirst = true;
            first = BaselineTIFFTagSet.SAMPLE_FORMAT_UNDEFINED;
        } else if (f.getCount() != samplesPerPixel) {
            replicateFirst = true;
            first = f.getAsInt(0);
        }

        for (int i = 0; i < samplesPerPixel; i++) {
            sampleFormat[i] = replicateFirst ? first : f.getAsInt(i);
            if (sampleFormat[i] !=
                  BaselineTIFFTagSet.SAMPLE_FORMAT_UNSIGNED_INTEGER &&
                sampleFormat[i] !=
                  BaselineTIFFTagSet.SAMPLE_FORMAT_SIGNED_INTEGER &&
                sampleFormat[i] !=
                  BaselineTIFFTagSet.SAMPLE_FORMAT_FLOATING_POINT &&
                sampleFormat[i] !=
                  BaselineTIFFTagSet.SAMPLE_FORMAT_UNDEFINED) {
                processWarningOccurred(
          "Illegal value for SAMPLE_FORMAT, assuming SAMPLE_FORMAT_UNDEFINED");
                sampleFormat[i] = BaselineTIFFTagSet.SAMPLE_FORMAT_UNDEFINED;
            }
        }

        //
        // BitsPerSample
        //
        f = imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_BITS_PER_SAMPLE);
        this.bitsPerSample = new int[samplesPerPixel];
        replicateFirst = false;
        if (f == null) {
            replicateFirst = true;
            first = defaultBitDepth;
        } else if (f.getCount() != samplesPerPixel) {
            replicateFirst = true;
            first = f.getAsInt(0);
        }
        
        for (int i = 0; i < samplesPerPixel; i++) {
            // Replicate initial value if not enough values provided
            bitsPerSample[i] = replicateFirst ? first : f.getAsInt(i);

            if (DEBUG) {
                System.out.println("bitsPerSample[" + i + "] = "
                                   + bitsPerSample[i]);
            }
        }

        //
        // ExtraSamples
        //
        this.extraSamples = null;
        f = imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_EXTRA_SAMPLES);
        if (f != null) {
            extraSamples = f.getAsInts();
        }

        // 
        // NoData (if any, leveraging on GDAL tag)
        //
        this.noData = null;
        f = imageMetadata.getTIFFField(PrivateTIFFTagSet.TAG_GDAL_NODATA);
        if (f != null) {
            String value = f.getAsString(0);
            if ("nan".equalsIgnoreCase(value)) {
                noData = Double.NaN;
            } else {
                noData = Double.parseDouble(value);
            }
        }

        // scales and offsets, if any
        f = imageMetadata.getTIFFField(PrivateTIFFTagSet.TAG_GDAL_METADATA);
        this.offsets = null;
        this.scales = null;
        if (f != null) {
            // guard with try/catch to avoid regressions on upgrade (e.g., files that used to
            // read fine that fail to read after an upgrade)
            try {
                String xml = f.getAsString(0);
                GDALMetadata gdalMetadata = GDALMetadataParser.parse(xml);
                // parse both before assigning to fields, to avoid partially setup data structure
                Double[] offsets = gdalMetadata.getOffsets(numBands);
                Double[] scales = gdalMetadata.getScales(numBands);
                this.offsets = offsets;
                this.scales = scales;
            } catch(Exception e) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Skipping GDAL metadata parsing due to errors", e);
                }
            }
        }
        
        // signal that this image is initialized
        initialized = true;
        
        // cache the page info for later reuse
        pagesInfo.put(
                currIndex, 
                new PageInfo(
                        imageMetadata,
                        bigtiff, 
                        bitsPerSample, 
                        colorMap, 
                        compression,
                        height,
                        numBands,
                        photometricInterpretation,
                        width,
                        tileOrStripWidth,
                        tileOrStripHeight,
                        planarConfiguration,
                        isImageTiled,
                        samplesPerPixel,
                        sampleFormat,
                        extraSamples,
                        noData,
                        offsets,
                        scales));

    }

    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IIOException {
        
        Integer imageIndexInteger = Integer.valueOf(imageIndex);
        if(imageTypeMap.containsKey(imageIndexInteger)) 
            // Return the cached ITS List.
            return imageTypeMap.get(imageIndexInteger).iterator();
        // Create a new ITS List.
        final List<ImageTypeSpecifier> l= new ArrayList<ImageTypeSpecifier>();

        // Create the ITS and cache if for later use so that this method
        // always returns an Iterator containing the same ITS objects.
        seekToImage(imageIndex, true);
        ImageTypeSpecifier itsRaw = 
            TIFFDecompressor.getRawImageTypeSpecifier
                (photometricInterpretation,
                 compression,
                 samplesPerPixel,
                 bitsPerSample,
                 sampleFormat,
                 extraSamples,
                 colorMap);

        // Check for an ICCProfile field.
        TIFFField iccProfileField =
            imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_ICC_PROFILE);

        // If an ICCProfile field is present change the ImageTypeSpecifier
        // to use it if the data layout is component type.
        if(iccProfileField != null &&
           itsRaw.getColorModel() instanceof ComponentColorModel) {
            // Create a ColorSpace from the profile.
            byte[] iccProfileValue = iccProfileField.getAsBytes();
            ICC_Profile iccProfile =
                ICC_Profile.getInstance(iccProfileValue);
            ICC_ColorSpace iccColorSpace =
                new ICC_ColorSpace(iccProfile);

            // Get the raw sample and color information.
            ColorModel cmRaw = itsRaw.getColorModel();
            ColorSpace csRaw = cmRaw.getColorSpace();
            SampleModel smRaw = itsRaw.getSampleModel();

            // Get the number of samples per pixel and the number
            // of color components.
            int numBands = smRaw.getNumBands();
            int numComponents = iccColorSpace.getNumComponents();

            // Replace the ColorModel with the ICC ColorModel if the
            // numbers of samples and color components are amenable.
            if(numBands == numComponents ||
               numBands == numComponents + 1) {
                // Set alpha flags.
                boolean hasAlpha = numComponents != numBands;
                boolean isAlphaPre =
                    hasAlpha && cmRaw.isAlphaPremultiplied();

                // Create a ColorModel of the same class and with
                // the same transfer type.
                ColorModel iccColorModel =
                    new ComponentColorModel(iccColorSpace,
                                            cmRaw.getComponentSize(),
                                            hasAlpha,
                                            isAlphaPre,
                                            cmRaw.getTransparency(),
                                            cmRaw.getTransferType());

                // Prepend the ICC profile-based ITS to the List. The
                // ColorModel and SampleModel are guaranteed to be
                // compatible as the old and new ColorModels are both
                // ComponentColorModels with the same transfer type
                // and the same number of components.
                l.add(new ImageTypeSpecifier(iccColorModel, smRaw));

                // Append the raw ITS to the List if and only if its
                // ColorSpace has the same type and number of components
                // as the ICC ColorSpace.
                if(csRaw.getType() == iccColorSpace.getType() &&
                   csRaw.getNumComponents() ==
                   iccColorSpace.getNumComponents()) {
                    l.add(itsRaw);
                }
            } else { // ICCProfile not compatible with SampleModel.
                // Append the raw ITS to the List.
                l.add(itsRaw);
            }
        } else { // No ICCProfile field or raw ColorModel not component.
            // Append the raw ITS to the List.
            l.add(itsRaw);
        }

        // Cache the ITS List.
        imageTypeMap.put(imageIndexInteger, l);
        return l.iterator();

    }

    public IIOMetadata getImageMetadata(int imageIndex) throws IIOException {
        seekToImage(imageIndex, true);
        TIFFImageMetadata im =
            new TIFFImageMetadata(imageMetadata.getRootIFD().getTagSetList());
        Node root =
            imageMetadata.getAsTree(TIFFImageMetadata.nativeMetadataFormatName);
        im.setFromTree(TIFFImageMetadata.nativeMetadataFormatName, root);
        if (noData != null) {
            im.setNoData(new double[] {noData, noData});
        }
        if (scales != null && offsets != null) {
            im.setScales(scales);
            im.setOffsets(offsets);
        }
        return im;
    }

    public IIOMetadata getStreamMetadata(int imageIndex) throws IIOException {
        readHeader();
        TIFFStreamMetadata sm = new TIFFStreamMetadata();
        Node root = sm.getAsTree(TIFFStreamMetadata.nativeMetadataFormatName);
        sm.setFromTree(TIFFStreamMetadata.nativeMetadataFormatName, root);
        return sm;
    }

    public boolean isRandomAccessEasy(int imageIndex) throws IOException {
        if(currIndex != -1) {
            seekToImage(currIndex);
            return compression == BaselineTIFFTagSet.COMPRESSION_NONE;
        } else {
            return false;
        }
    }

    // Thumbnails

    public boolean readSupportsThumbnails() {
        return false;
    }

    public boolean hasThumbnails(int imageIndex) {
        return false;
    }

    public int getNumThumbnails(int imageIndex) throws IOException {
        return 0;
    }

    public ImageReadParam getDefaultReadParam() {
        return new TIFFImageReadParam();
    }

    public boolean isImageTiled(int imageIndex) throws IOException {
        seekToImage(imageIndex);

        return isImageTiled;
    }

    public int getTileWidth(int imageIndex) throws IOException {
        seekToImage(imageIndex);
        return tileOrStripWidth;
    }

    public int getTileHeight(int imageIndex) throws IOException {
        seekToImage(imageIndex);
        return tileOrStripHeight;
    }

    public BufferedImage readTile(int imageIndex, int tileX, int tileY)
        throws IOException {

        int w = getWidth(imageIndex);
        int h = getHeight(imageIndex);
        int tw = getTileWidth(imageIndex);
        int th = getTileHeight(imageIndex);

        int x = tw*tileX;
        int y = th*tileY;

        if(tileX < 0 || tileY < 0 || x >= w || y >= h) {
            throw new IllegalArgumentException
                ("Tile indices are out of bounds!");
        }

        if (x + tw > w) {
            tw = w - x;
        }

        if (y + th > h) {
            th = h - y;
        }

        ImageReadParam param = getDefaultReadParam();
        Rectangle tileRect = new Rectangle(x, y, tw, th);
        param.setSourceRegion(tileRect);

        return read(imageIndex, param);
    }

    public boolean canReadRaster() {
        // Enable this?
        return false;
    }

    public Raster readRaster(int imageIndex, ImageReadParam param)
        throws IOException {
        // Enable this?
        throw new UnsupportedOperationException();
    }

    private int[] sourceBands;
    private int[] destinationBands;

    private TIFFDecompressor decompressor;

    // floor(num/den)
    private static int ifloor(int num, int den) {
        if (num < 0) {
            num -= den - 1;
        }
        return num/den;
    }

    // ceil(num/den)
    private static int iceil(int num, int den) {
        if (num > 0) {
            num += den - 1;
        }
        return num/den;
    }

    protected void prepareRead(int imageIndex, ImageReadParam param)
        throws IOException {
        if (stream == null) {
            throw new IllegalStateException("Input not set!");
        }

        // A null ImageReadParam means we use the default
        if (param == null) {
            param = getDefaultReadParam();
        }

        this.imageReadParam = param;

        // ensure everything is initialized
        seekToImage(imageIndex);

        this.sourceBands = param.getSourceBands();
        if (sourceBands == null) {
            sourceBands = new int[numBands];
            for (int i = 0; i < numBands; i++) {
                sourceBands[i] = i;
            }
        }

        // Initialize the destination image
        Iterator<ImageTypeSpecifier> imageTypes = getImageTypes(imageIndex);
        ImageTypeSpecifier theImageType =
            ImageUtil.getDestinationType(param, imageTypes);

        int destNumBands = theImageType.getSampleModel().getNumBands();

        this.destinationBands = param.getDestinationBands();
        if (destinationBands == null) {
            destinationBands = new int[destNumBands];
            for (int i = 0; i < destNumBands; i++) {
                destinationBands[i] = i;
            }
        }

        if (sourceBands.length != destinationBands.length) {
            throw new IllegalArgumentException(
                              "sourceBands.length != destinationBands.length");
        }

        for (int i = 0; i < sourceBands.length; i++) {
            int sb = sourceBands[i];
            if (sb < 0 || sb >= numBands) {
                throw new IllegalArgumentException(
                                                  "Source band out of range!");
            }
            int db = destinationBands[i];
            if (db < 0 || db >= destNumBands) {
                throw new IllegalArgumentException(
                                             "Destination band out of range!");
            }
        }
    }

    public RenderedImage readAsRenderedImage(int imageIndex,
                                             ImageReadParam param)
        throws IOException {
        prepareRead(imageIndex, param);
        return new TIFFRenderedImage(this, imageIndex, imageReadParam,
                                     width, height);
    }

    private void decodeTile(int ti, int tj, int band) throws IOException {
        if(DEBUG) {
            System.out.println("decodeTile("+ti+","+tj+","+band+")");
        }

        // Compute the region covered by the strip or tile
        Rectangle tileRect = new Rectangle(ti*tileOrStripWidth,
                                           tj*tileOrStripHeight,
                                           tileOrStripWidth,
                                           tileOrStripHeight);

        // Clip against the image bounds if the image is not tiled. If it
        // is tiled, the tile may legally extend beyond the image bounds.
        if(!this.isImageTiled) {
            tileRect =
                tileRect.intersection(new Rectangle(0, 0, width, height));
        }

        // Return if the intersection is empty.
        if(tileRect.width <= 0 || tileRect.height <= 0) {
            return;
        }
        
        int srcMinX = tileRect.x;
        int srcMinY = tileRect.y;
        int srcWidth = tileRect.width;
        int srcHeight = tileRect.height;

        // Determine dest region that can be derived from the
        // source region
        
        dstMinX = iceil(srcMinX - sourceXOffset, srcXSubsampling);
        int dstMaxX = ifloor(srcMinX + srcWidth - 1 - sourceXOffset,
                         srcXSubsampling);
        
        dstMinY = iceil(srcMinY - sourceYOffset, srcYSubsampling);
        int dstMaxY = ifloor(srcMinY + srcHeight - 1 - sourceYOffset,
                             srcYSubsampling);
        
        dstWidth = dstMaxX - dstMinX + 1;
        dstHeight = dstMaxY - dstMinY + 1;
        
        dstMinX += dstXOffset;
        dstMinY += dstYOffset;
        
        // Clip against image bounds
        
        Rectangle dstRect = new Rectangle(dstMinX, dstMinY,
                                          dstWidth, dstHeight);
        dstRect =
            dstRect.intersection(theImage.getRaster().getBounds());
        
        dstMinX = dstRect.x;
        dstMinY = dstRect.y;
        dstWidth = dstRect.width;
        dstHeight = dstRect.height;
        
        if (dstWidth <= 0 || dstHeight <= 0) {
            return;
        }
        
        // Backwards map dest region to source to determine
        // active source region
        
        int activeSrcMinX = (dstMinX - dstXOffset)*srcXSubsampling +
            sourceXOffset;
        int sxmax = 
            (dstMinX + dstWidth - 1 - dstXOffset)*srcXSubsampling +
            sourceXOffset;
        int activeSrcWidth = sxmax - activeSrcMinX + 1;
        
        int activeSrcMinY = (dstMinY - dstYOffset)*srcYSubsampling +
            sourceYOffset;
        int symax =
            (dstMinY + dstHeight - 1 - dstYOffset)*srcYSubsampling +
            sourceYOffset;
        int activeSrcHeight = symax - activeSrcMinY + 1;
        
        decompressor.setSrcMinX(srcMinX);
        decompressor.setSrcMinY(srcMinY);
        decompressor.setSrcWidth(srcWidth);
        decompressor.setSrcHeight(srcHeight);
        
        decompressor.setDstMinX(dstMinX);
        decompressor.setDstMinY(dstMinY);
        decompressor.setDstWidth(dstWidth);
        decompressor.setDstHeight(dstHeight);
        
        decompressor.setActiveSrcMinX(activeSrcMinX);
        decompressor.setActiveSrcMinY(activeSrcMinY);
        decompressor.setActiveSrcWidth(activeSrcWidth);
        decompressor.setActiveSrcHeight(activeSrcHeight);

        int tileIndex = tj*tilesAcross + ti;

        if (planarConfiguration ==
            BaselineTIFFTagSet.PLANAR_CONFIGURATION_PLANAR) {
            tileIndex += band*tilesAcross*tilesDown;
        }
        
        long offset = getTileOrStripOffset(tileIndex);
        long byteCount = getTileOrStripByteCount(tileIndex);

        //
        // Attempt to handle truncated streams, i.e., where reading the
        // compressed strip or tile would result in an EOFException. The
        // number of bytes to read is clamped to the number available
        // from the stream starting at the indicated position in the hope
        // that the decompressor will handle it.
        //
        long streamLength = stream.length();
        if(streamLength > 0 && offset + byteCount > streamLength) {
            processWarningOccurred("Attempting to process truncated stream.");
            if(Math.max(byteCount = streamLength - offset, 0) == 0) {
                processWarningOccurred("No bytes in strip/tile: skipping.");
                return;
            }
        }

        decompressor.setStream(stream);
        decompressor.setOffset(offset);
        decompressor.setByteCount((int)byteCount);
        ((TIFFDecompressor)decompressor).setNoData(noData);
        decompressor.beginDecoding();

        stream.mark();
        decompressor.decode();
        stream.reset();
    }

    private void reportProgress() {
        // Report image progress/update to listeners after each tile
        pixelsRead += dstWidth*dstHeight;
        processImageProgress(100.0f*pixelsRead/pixelsToRead);
        processImageUpdate(theImage,
                           dstMinX, dstMinY, dstWidth, dstHeight,
                           1, 1,
                           destinationBands);
    }

    public BufferedImage read(int imageIndex, ImageReadParam param)
        throws IOException {
        prepareRead(imageIndex, param);

        // prepare for reading
        this.theImage = getDestination(param,
                                       getImageTypes(imageIndex),
                                       width, height, noData);

        srcXSubsampling = imageReadParam.getSourceXSubsampling();
        srcYSubsampling = imageReadParam.getSourceYSubsampling();

        Point p = imageReadParam.getDestinationOffset();
        dstXOffset = p.x;
        dstYOffset = p.y;

        // This could probably be made more efficient...
        Rectangle srcRegion = new Rectangle(0, 0, 0, 0);
        Rectangle destRegion = new Rectangle(0, 0, 0, 0);

        computeRegions(imageReadParam, width, height, theImage,
                       srcRegion, destRegion);

        // Initial source pixel, taking source region and source
        // subsamplimg offsets into account
        sourceXOffset = srcRegion.x;
        sourceYOffset = srcRegion.y;

        pixelsToRead = destRegion.width*destRegion.height;
        pixelsRead = 0;

        processImageStarted(imageIndex);
        processImageProgress(0.0f);

        tilesAcross = (width + tileOrStripWidth - 1)/tileOrStripWidth;
        tilesDown = (height + tileOrStripHeight - 1)/tileOrStripHeight;

        // Attempt to get decompressor and color converted from the read param
        
        TIFFColorConverter colorConverter = null;
        if (imageReadParam instanceof TIFFImageReadParam) {
            TIFFImageReadParam tparam =
                (TIFFImageReadParam)imageReadParam;
            this.decompressor = tparam.getTIFFDecompressor();
            colorConverter = tparam.getColorConverter();
        }

        // If we didn't find one, use a standard decompressor
        if (this.decompressor == null) {
            if (compression ==
                BaselineTIFFTagSet.COMPRESSION_NONE) {
                // Get the fillOrder field.
                TIFFField fillOrderField =
                    imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_FILL_ORDER);

                // Set the decompressor based on the fill order.
                if(fillOrderField != null && fillOrderField.getAsInt(0) == 2) {
                    this.decompressor = new TIFFLSBDecompressor();
                } else {
                    this.decompressor = new TIFFNullDecompressor();
                }
            } else if (compression ==
                       BaselineTIFFTagSet.COMPRESSION_CCITT_T_6) {

                // Try to create the codecLib decompressor.
                if(PackageUtil.isCodecLibAvailable()) {
                    try {
                        this.decompressor =
                            new TIFFCodecLibFaxDecompressor(compression);
                        if(DEBUG) {
                            System.out.println
                                ("Using codecLib T.6 decompressor");
                        }
                    } catch (RuntimeException re) {
                        if(DEBUG) {
                            System.out.println(re);
                        }
                    }
                }

                // Fall back to the Java decompressor.
                if (this.decompressor == null) {
                    if(DEBUG) {
                        System.out.println("Using Java T.6 decompressor");
                    }
                    this.decompressor = new TIFFFaxDecompressor();
                }
            } else if (compression ==
                       BaselineTIFFTagSet.COMPRESSION_CCITT_T_4) {

                if(PackageUtil.isCodecLibAvailable()) {
                    // Try to create the codecLib decompressor.
                    try {
                        this.decompressor =
                            new TIFFCodecLibFaxDecompressor(compression);
                        if(DEBUG) {
                            System.out.println
                                ("Using codecLib T.4 decompressor");
                        }
                    } catch (RuntimeException re) {
                        if(DEBUG) {
                            System.out.println(re);
                        }
                    }
                }

                // Fall back to the Java decompressor.
                if (this.decompressor == null) {
                    if(DEBUG) {
                        System.out.println("Using Java T.4 decompressor");
                    }
                    this.decompressor = new TIFFFaxDecompressor();
                }
            } else if (compression ==
                       BaselineTIFFTagSet.COMPRESSION_CCITT_RLE) {
                this.decompressor = new TIFFFaxDecompressor();
            } else if (compression ==
                       BaselineTIFFTagSet.COMPRESSION_PACKBITS) {
                if(DEBUG) {
                    System.out.println("Using TIFFPackBitsDecompressor");
                }
                this.decompressor = new TIFFPackBitsDecompressor();
            } else if (compression ==
                       BaselineTIFFTagSet.COMPRESSION_LZW) {
                if(DEBUG) {
                    System.out.println("Using TIFFLZWDecompressor");
                }
                TIFFField predictorField =
                    imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_PREDICTOR);
                int predictor = ((predictorField == null) ?
                                 BaselineTIFFTagSet.PREDICTOR_NONE :
                                 predictorField.getAsInt(0));
                this.decompressor = new TIFFLZWDecompressor(predictor);
            } else if (compression ==
                       BaselineTIFFTagSet.COMPRESSION_JPEG) {
                this.decompressor = new TIFFJPEGDecompressor();
            } else if (compression ==
                       BaselineTIFFTagSet.COMPRESSION_ZLIB ||
                       compression ==
                       BaselineTIFFTagSet.COMPRESSION_DEFLATE) {
                TIFFField predictorField =
                    imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_PREDICTOR);
                int predictor = ((predictorField == null) ?
                                 BaselineTIFFTagSet.PREDICTOR_NONE :
                                 predictorField.getAsInt(0));
                this.decompressor = new TIFFDeflateDecompressor(predictor);
            } else if (compression ==
                       BaselineTIFFTagSet.COMPRESSION_OLD_JPEG) {
                TIFFField JPEGProcField =
                    imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_JPEG_PROC);
                if(JPEGProcField == null) {
                    processWarningOccurred
                        ("JPEGProc field missing; assuming baseline sequential JPEG process.");
                } else if(JPEGProcField.getAsInt(0) !=
                   BaselineTIFFTagSet.JPEG_PROC_BASELINE) {
                    throw new IIOException
                        ("Old-style JPEG supported for baseline sequential JPEG process only!");
                }
                this.decompressor = new TIFFOldJPEGDecompressor();
                //throw new IIOException("Old-style JPEG not supported!");
            } else if (compression == PrivateTIFFTagSet.COMPRESSION_ZSTD) {
                TIFFField predictorField =
                        imageMetadata.getTIFFField(BaselineTIFFTagSet.TAG_PREDICTOR);
                int predictor = ((predictorField == null) ?
                        BaselineTIFFTagSet.PREDICTOR_NONE :
                        predictorField.getAsInt(0));
                this.decompressor = new TIFFZSTDDecompressor(predictor);
            }


            else {
                throw new IIOException
                    ("Unsupported compression type (tag number = "+
                     compression+")!");
            }

            if (photometricInterpretation ==
                BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_Y_CB_CR &&
                compression != BaselineTIFFTagSet.COMPRESSION_JPEG &&
                compression != BaselineTIFFTagSet.COMPRESSION_OLD_JPEG) {
                boolean convertYCbCrToRGB =
                    theImage.getColorModel().getColorSpace().getType() ==
                    ColorSpace.TYPE_RGB;
                TIFFDecompressor wrappedDecompressor =
                    this.decompressor instanceof TIFFNullDecompressor ?
                    null : this.decompressor;
                this.decompressor =
                    new TIFFYCbCrDecompressor(wrappedDecompressor,
                                              convertYCbCrToRGB);
            }
        }

        if(DEBUG) {
            System.out.println("\nDecompressor class = "+
                               decompressor.getClass().getName()+"\n");
        }

        if (colorConverter == null) {
            if (photometricInterpretation ==
                BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_CIELAB &&
                theImage.getColorModel().getColorSpace().getType() ==
                ColorSpace.TYPE_RGB) {
                colorConverter = new TIFFCIELabColorConverter();
             } else if (photometricInterpretation ==
                        BaselineTIFFTagSet.PHOTOMETRIC_INTERPRETATION_Y_CB_CR &&
                        !(this.decompressor instanceof TIFFYCbCrDecompressor) &&
                        compression != BaselineTIFFTagSet.COMPRESSION_JPEG &&
                        compression != BaselineTIFFTagSet.COMPRESSION_OLD_JPEG) {
                 colorConverter = new TIFFYCbCrColorConverter(imageMetadata);
            }
        }
        
        decompressor.setReader(this);
        decompressor.setMetadata(imageMetadata);
        decompressor.setImage(theImage);

        decompressor.setPhotometricInterpretation(photometricInterpretation);
        decompressor.setCompression(compression);
        decompressor.setSamplesPerPixel(samplesPerPixel);
        decompressor.setBitsPerSample(bitsPerSample);
        decompressor.setSampleFormat(sampleFormat);
        decompressor.setExtraSamples(extraSamples);
        decompressor.setColorMap(colorMap);

        decompressor.setColorConverter(colorConverter);

        decompressor.setSourceXOffset(sourceXOffset);
        decompressor.setSourceYOffset(sourceYOffset);
        decompressor.setSubsampleX(srcXSubsampling);
        decompressor.setSubsampleY(srcYSubsampling);

        decompressor.setDstXOffset(dstXOffset);
        decompressor.setDstYOffset(dstYOffset);

        decompressor.setSourceBands(sourceBands);
        decompressor.setDestinationBands(destinationBands);

        // Compute bounds on the tile indices for this source region.
        int minTileX =
            TIFFImageWriter.XToTileX(srcRegion.x, 0, tileOrStripWidth);
        int minTileY =
            TIFFImageWriter.YToTileY(srcRegion.y, 0, tileOrStripHeight);
        int maxTileX =
            TIFFImageWriter.XToTileX(srcRegion.x + srcRegion.width - 1,
                                     0, tileOrStripWidth);
        int maxTileY =
            TIFFImageWriter.YToTileY(srcRegion.y + srcRegion.height - 1,
                                     0, tileOrStripHeight);

        boolean isAbortRequested = false;
        if (planarConfiguration ==
            BaselineTIFFTagSet.PLANAR_CONFIGURATION_PLANAR) {
            
            decompressor.setPlanar(true);
            
            int[] sb = new int[1];
            int[] db = new int[1];
            for (int tj = minTileY; tj <= maxTileY; tj++) {
                for (int ti = minTileX; ti <= maxTileX; ti++) {
                    for (int band = 0; band < numBands; band++) {
                        sb[0] = sourceBands[band];
                        decompressor.setSourceBands(sb);
                        db[0] = destinationBands[band];
                        decompressor.setDestinationBands(db);
                        //XXX decompressor.beginDecoding();

                        // The method abortRequested() is synchronized
                        // so check it only once per loop just before
                        // doing any actual decoding.
                        if(abortRequested()) {
                            isAbortRequested = true;
                            break;
                        }

                        decodeTile(ti, tj, band);
                    }

                    if(isAbortRequested) break;

                    reportProgress();
                }

                if(isAbortRequested) break;
            }
        } else {
            //XXX decompressor.beginDecoding();

            for (int tj = minTileY; tj <= maxTileY; tj++) {
                for (int ti = minTileX; ti <= maxTileX; ti++) {
                    // The method abortRequested() is synchronized
                    // so check it only once per loop just before
                    // doing any actual decoding.
                    if(abortRequested()) {
                        isAbortRequested = true;
                        break;
                    }

                    decodeTile(ti, tj, -1);

                    reportProgress();
                }

                if(isAbortRequested) break;
            }
        }

        if (isAbortRequested) {
            processReadAborted();
        } else {
            processImageComplete();
        }

        return theImage;
    }

    public void reset() {
        super.reset();
        resetLocal();
    }

    protected void resetLocal() {
        imageStartPosition.clear();
        pagesInfo.clear();
        stream = null;
        gotTiffHeader = false;
        imageReadParam = getDefaultReadParam();
        streamMetadata = null;
        currIndex = -1;
        imageMetadata = null;
        initialized = false;
        imageStartPosition = new ArrayList<Long>();
        numImages = -1;
        imageTypeMap = new HashMap<Integer, List<ImageTypeSpecifier>>();
        width = -1;
        height = -1;
        numBands = -1;
        tileOrStripWidth = -1;
        tileOrStripHeight = -1;
        planarConfiguration = BaselineTIFFTagSet.PLANAR_CONFIGURATION_CHUNKY;
    }

    /**
     * Package scope method to allow decompressors, for example, to
     * emit warning messages.
     */
    void forwardWarningMessage(String warning) {
        processWarningOccurred(warning);
    }
    
    protected static BufferedImage getDestination(ImageReadParam param,
                                                  Iterator<ImageTypeSpecifier> imageTypes,
                                                  int width, int height, Double noData)
            throws IIOException {
        if (imageTypes == null || !imageTypes.hasNext()) {
            throw new IllegalArgumentException("imageTypes null or empty!");
        }        
        
        BufferedImage dest = null;
        ImageTypeSpecifier imageType = null;

        // If param is non-null, use it
        if (param != null) {
            // Try to get the image itself
            dest = param.getDestination();
            if (dest != null) {
                return dest;
            }
        
            // No image, get the image type
            imageType = param.getDestinationType();
        }

        // No info from param, use fallback image type
        if (imageType == null) {
            Object o = imageTypes.next();
            if (!(o instanceof ImageTypeSpecifier)) {
                throw new IllegalArgumentException
                    ("Non-ImageTypeSpecifier retrieved from imageTypes!");
            }
            imageType = (ImageTypeSpecifier)o;
        } else {
            boolean foundIt = false;
            while (imageTypes.hasNext()) {
                ImageTypeSpecifier type =
                    imageTypes.next();
                if (type.equals(imageType)) {
                    foundIt = true;
                    break;
                }
            }

            if (!foundIt) {
                throw new IIOException
                    ("Destination type from ImageReadParam does not match!");
            }
        }

        Rectangle srcRegion = new Rectangle(0,0,0,0);
        Rectangle destRegion = new Rectangle(0,0,0,0);
        computeRegions(param,
                       width,
                       height,
                       null,
                       srcRegion,
                       destRegion);
        
        int destWidth = destRegion.x + destRegion.width;
        int destHeight = destRegion.y + destRegion.height;
        // Create a new image based on the type specifier
        
        if ((long)destWidth*destHeight > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                ("width*height > Integer.MAX_VALUE!");
        }

        SampleModel sampleModel = imageType.getSampleModel(destWidth, destHeight);
        ColorModel colorModel = imageType.getColorModel();
        WritableRaster raster = Raster.createWritableRaster(sampleModel,  new Point(0, 0));
        Hashtable properties = new Hashtable();
        if ( noData != null ) {
            properties.put("GC_NODATA", noData);
        }
        return new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), properties);
    }
    
    @Override
    public void dispose() {
        if (this.decompressor != null) {
            this.decompressor.dispose();
        }
        this.layout = null;
        if (this.theImage != null) {
            this.theImage.flush();
        }
        this.theImage = null;
        this.imageStartPosition = null;
        this.imageMetadata = null;
        this.imageReadParam = null;
        this.stream = null;
    }

}
