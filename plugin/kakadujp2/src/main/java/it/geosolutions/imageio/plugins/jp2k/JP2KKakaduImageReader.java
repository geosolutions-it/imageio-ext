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

import it.geosolutions.imageio.plugins.jp2k.box.BitsPerComponentBox;
import it.geosolutions.imageio.plugins.jp2k.box.BoxUtilities;
import it.geosolutions.imageio.plugins.jp2k.box.ChannelDefinitionBox;
import it.geosolutions.imageio.plugins.jp2k.box.ColorSpecificationBox;
import it.geosolutions.imageio.plugins.jp2k.box.ComponentMappingBox;
import it.geosolutions.imageio.plugins.jp2k.box.ImageHeaderBox;
import it.geosolutions.imageio.plugins.jp2k.box.PaletteBox;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;
import it.geosolutions.imageio.utilities.Utilities;
import it.geosolutions.util.KakaduUtilities;

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import kdu_jni.Jp2_family_src;
import kdu_jni.Jpx_codestream_source;
import kdu_jni.Jpx_input_box;
import kdu_jni.Jpx_source;
import kdu_jni.KduException;
import kdu_jni.Kdu_codestream;
import kdu_jni.Kdu_coords;
import kdu_jni.Kdu_dims;
import kdu_jni.Kdu_global;
import kdu_jni.Kdu_simple_file_source;
import kdu_jni.Kdu_stripe_decompressor;

import com.sun.media.imageioimpl.common.ImageUtil;

/**
 * <code>JP2KakaduImageReader</code> is a <code>ImageReader</code> able to
 * create {@link RenderedImage} from JP2 files, leveraging on Kdu_jni bindings.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class JP2KKakaduImageReader extends ImageReader {
    // private boolean initializedJp2Boxes = false;

    private static Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.jp2k");

    static {
        final String level = System.getProperty("it.geosolutions.loggerlevel");
        if (level != null && level.equalsIgnoreCase("FINE")) {
            LOGGER.setLevel(Level.FINE);
        }
    }

    /** Size of the Temp Buffer, used when reading from an image input stream */
    private final static int TEMP_BUFFER_SIZE = 64 * 1024;

    private boolean deleteInputFile;

    /** The dataset input source */
    private File inputFile = null;

    /** The data input source name */
    private String fileName = null;

    private boolean isRawSource;

    /** A fileWalker to scan boxes */
    private JP2KFileWalker fileWalker;

    private final List<JP2KCodestreamProperties> multipleCodestreams = new ArrayList<JP2KCodestreamProperties>();

    private int numImages = 1;

    protected JP2KKakaduImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
        KakaduUtilities.initializeKakaduMessagesManagement();
    }

    /**
     * Checks if the specified ImageIndex is valid.
     * 
     * @param imageIndex
     *                the specified imageIndex
     * 
     * @throws IndexOutOfBoundsException
     *                 if imageIndex is lower than 0 or if is greater than the
     *                 max number (-1) of images available within the data
     *                 source contained within the source
     */
    protected void checkImageIndex(final int imageIndex) {
        if (imageIndex < 0 || imageIndex > numImages) {
            final StringBuffer sb = new StringBuffer(
                    "Illegal imageIndex specified = ").append(imageIndex)
                    .append(", while the valid imageIndex");
            if (numImages > 1)
                // There are N Images.
                sb.append(" range should be [0,").append(numImages - 1).append("]!");
            else
                // Only the imageIndex 0 is valid.
                sb.append(" should be 0!");
            throw new IndexOutOfBoundsException(sb.toString());
        }
    }

    /**
     * Returns the height in pixel of the image
     * 
     * @param the
     *                index of the selected image
     */
    public int getHeight(int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        return multipleCodestreams.get(imageIndex).getHeight();
    }

    /**
     * Returns the width in pixel of the image
     * 
     * @param the
     *                index of the selected image
     */
    public int getWidth(int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        return multipleCodestreams.get(imageIndex).getWidth();
    }

    /**
     * Returns an <code>IIOMetadata</code> object containing metadata
     * associated with the given image.
     *
     * @param imageIndex the index of the image whose metadata is to
     * be retrieved. 
     *
     * @return an <code>IIOMetadata</code> object.
     * @see javax.imageio.ImageReader#getImageMetadata(int)
     */
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return new JP2KImageMetadata(multipleCodestreams.get(imageIndex));
    }

    /**
     * Returns an <code>IIOMetadata</code> object representing the
     * metadata associated with the input source as a whole. 
     *
     * @return an <code>IIOMetadata</code> object.
     * @see javax.imageio.ImageReader#getStreamMetadata()
     */
    public IIOMetadata getStreamMetadata() throws IOException {
        if (isRawSource)
            throw new UnsupportedOperationException(
                    "Raw source detected. Actually, unable to get stream metadata");
        return new JP2KStreamMetadata(fileWalker.getJP2KBoxesTree(), numImages);
    }

    /**
     * Returns an <code>Iterator</code> containing possible image types to
     * which the given image may be decoded, in the form of
     * <code>ImageTypeSpecifiers</code>s. At least one legal image type will
     * be returned. This implementation simply returns an
     * <code>ImageTypeSpecifier</code> set in compliance with the property of
     * the dataset contained within the underlying data source.
     * 
     * @param imageIndex
     *                the index of the image to be retrieved.
     * 
     * @return an <code>Iterator</code> containing possible image types to
     *         which the given image may be decoded, in the form of
     *         <code>ImageTypeSpecifiers</code>s
     * @see javax.imageio.ImageReader#getImageTypes(int)
     */
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex)
            throws IOException {
        checkImageIndex(imageIndex);
        final List<ImageTypeSpecifier> l = new java.util.ArrayList<ImageTypeSpecifier>();
        final JP2KCodestreamProperties codestreamP = multipleCodestreams.get(imageIndex);

        // Setting SampleModel and ColorModel for the whole image
        if (codestreamP.getColorModel() == null || codestreamP.getSampleModel() == null) {
            try {
                initializeSampleModelAndColorModel(codestreamP);
            } catch (KduException kdue) {
                throw new RuntimeException(
                        "Error while setting sample and color model", kdue);
            }
        }

        final ImageTypeSpecifier imageType = new ImageTypeSpecifier(codestreamP.getColorModel(), codestreamP.getSampleModel());
        l.add(imageType);
        return l.iterator();
    }

    /**
     * Returns the number of images contained in the source.
     */
    public int getNumImages(boolean allowSearch) throws IOException {
        return numImages;
    }

    /**
     * Read the image and returns it as a complete <code>BufferedImage</code>,
     * using a supplied <code>ImageReadParam</code>.
     * 
     * @param imageIndex
     *                the index of the desired image.
     */
    public BufferedImage read(int imageIndex, ImageReadParam param)
            throws IOException {
        checkImageIndex(imageIndex);
        // ///////////////////////////////////////////////////////////
        //
        // STEP 0.
        // -------
        // Setting local variables for i-th codestream
        //
        // ///////////////////////////////////////////////////////////
        JP2KCodestreamProperties codestreamP = multipleCodestreams.get(imageIndex);
        final int maxAvailableQualityLayers = codestreamP.getMaxAvailableQualityLayers();
        final int[] componentIndexes = codestreamP.getComponentIndexes();
        final int nComponents = codestreamP.getNumComponents();
        final int maxBitDepth = codestreamP.getMaxBitDepth();
        final int height = codestreamP.getHeight();
        final int width = codestreamP.getWidth();
        final ColorModel cm = codestreamP.getColorModel();
        final SampleModel sm = codestreamP.getSampleModel();

        Kdu_simple_file_source localRawSource = null;
        Jp2_family_src localFamilySource = null;
        Jpx_source localWrappedSource = null;

        // get a default set of ImageReadParam if needed.
        if (param == null)
            param = getDefaultReadParam();

        // ///////////////////////////////////////////////////////////
        //
        // STEP 1.
        // -------
        // local variables initialization
        //
        // ///////////////////////////////////////////////////////////
        // The destination image properties
        final Rectangle destinationRegion = new Rectangle(0, 0, -1, -1);

        // The source region image properties
        final Rectangle sourceRegion = new Rectangle(0, 0, -1, -1);

        // The properties of the image we need to load from Kakadu
        final Rectangle requiredRegion = new Rectangle(0, 0, -1, -1);

        // Subsampling Factors
        int xSubsamplingFactor = -1;
        int ySubsamplingFactor = -1;

        // boolean used to specify when resampling is required (as an instance,
        // different subsampling factors (xSS != ySS) require resampling)
        boolean resamplingIsRequired = false;

        // ///////////////////////////////////////////////////////////
        //
        // STEP 2.
        // -------
        // parameters management (retrieve user defined readParam and
        // further initializations)
        //
        // ///////////////////////////////////////////////////////////
        if (!(param instanceof JP2KKakaduImageReadParam)) {
            // The parameter is not of JP2KakaduImageReadParam type but
            // simply an ImageReadParam instance (the superclass)
            // we need to build a proper JP2KakaduImageReadParam prior
            // to start parameters parsing.
            JP2KKakaduImageReadParam jp2kParam = (JP2KKakaduImageReadParam) getDefaultReadParam();
            jp2kParam.initialize(param);
            param = jp2kParam;
        }

        // selected interpolation type
        final int interpolationType = ((JP2KKakaduImageReadParam) param).getInterpolationType();

        // specified quality layers
        int qualityLayers = ((JP2KKakaduImageReadParam) param).getQualityLayers();
        if (qualityLayers != -1) {
            // qualityLayers != -1 means that the user have specified that value
            if (qualityLayers > maxAvailableQualityLayers)
                // correct the user defined qualityLayers parameter if this
                // exceed the max number of available quality layers
                qualityLayers = maxAvailableQualityLayers;
        } else
            qualityLayers = 0;

        // SubSampling variables initialization
        xSubsamplingFactor = param.getSourceXSubsampling();
        ySubsamplingFactor = param.getSourceYSubsampling();

        // //
        // Retrieving Information about Source Region and doing additional
        // initialization operations.
        // //
        computeRegions(width, height, param, sourceRegion, destinationRegion);

        // ////////////////////////////////////////////////////////////////////
        //
        // STEP 3.
        // -------
        // check if the image need to be resampled/rescaled and find the proper
        // scale factor as well as the size of the required region we need to
        // provide to kakadu.
        //
        // ////////////////////////////////////////////////////////////////////
        final int[] resolutionInfo = new int[2];
        resamplingIsRequired = getRequiredRegionsAndResolutions(codestreamP,
                xSubsamplingFactor, ySubsamplingFactor, sourceRegion,
                destinationRegion, requiredRegion, resolutionInfo);
        final int nDiscardLevels = resolutionInfo[1];

        // Setting the destination Buffer Size which will contains the samples
        // coming from stripe_decompressor

        BufferedImage bi = null;

        try {

            DataBuffer imageBuffer = null;
            // ////////////////////////////////////////////////////////////////
            //
            // STEP 4.
            // -------
            // Initialize sources and codestream
            // ////////////////////////////////////////////////////////////////

            // Opening data source
            Kdu_codestream codestream = new Kdu_codestream();

            if (!isRawSource) {
                localFamilySource = new Jp2_family_src();
                localWrappedSource = new Jpx_source();
                localFamilySource.Open(fileName);
                localWrappedSource.Open(localFamilySource, true);
                final Jpx_codestream_source stream = localWrappedSource.Access_codestream(imageIndex);
                final Jpx_input_box inputbox = stream.Open_stream();
                codestream.Create(inputbox);
            } else {
                localRawSource = new Kdu_simple_file_source(fileName);
                codestream.Create(localRawSource);
            }

            // ////////////////////////////////////////////////////////////////
            //
            // STEP 5.
            // -------
            // Set parameters for stripe decompression
            // ////////////////////////////////////////////////////////////////
            final Kdu_dims dims = new Kdu_dims();
            codestream.Apply_input_restrictions(0, nComponents, nDiscardLevels, qualityLayers, null, Kdu_global.KDU_WANT_OUTPUT_COMPONENTS);
            
            if (LOGGER.isLoggable(Level.FINE)){
            	final Kdu_dims original_dims = new Kdu_dims();
            	codestream.Get_dims(-1, original_dims);
            	StringBuilder sb = new StringBuilder("Original Hi-Res Image Region is: x=").append(original_dims.Access_pos().Get_x())
            	.append("  y=").append(original_dims.Access_pos().Get_y())
            	.append("  w=").append(original_dims.Access_size().Get_x()).append("  h=")
            	.append(original_dims.Access_size().Get_y());
            	LOGGER.fine(sb.toString());
            }
            
            final Kdu_dims dimsROI = new Kdu_dims();
            codestream.Get_dims(0, dims);
            
            
            if (LOGGER.isLoggable(Level.FINE)){
            	final int mappedX = dims.Access_pos().Get_x();
                final int mappedY = dims.Access_pos().Get_y();
                final int mappedWidth = dims.Access_size().Get_x();
                final int mappedHeight = dims.Access_size().Get_y();
            	StringBuilder sb = new StringBuilder("Mapped Region is: x=").append(mappedX).append("  y=").append(mappedY)
            	.append("  w=").append(mappedWidth).append("  h=").append(mappedHeight);
            	LOGGER.fine(sb.toString());
            }
            
            // Checks on the required region and mapped region, in compliance with
            // Formula 11.1 of Taubman's JPEG2000 compression book.
            checkBounds(dims, requiredRegion);
          
            final int destBufferSize = (requiredRegion.height * requiredRegion.width) * nComponents;
            setDimsForCrop(dims,requiredRegion);

            // Getting a region of interest.
            codestream.Map_region(0, dims, dimsROI);
            if (LOGGER.isLoggable(Level.FINE)){
            	 final int mappedROIWidth = dimsROI.Access_size().Get_x();
                 final int mappedROIHeight = dimsROI.Access_size().Get_y();
                 final int mappedROIX= dimsROI.Access_pos().Get_x();
                 final int mappedROIY = dimsROI.Access_pos().Get_y();
            	StringBuilder sb = new StringBuilder("ROI Region is: x=").append(mappedROIX).append("  y=").append(mappedROIY)
            	.append("  w=").append(mappedROIWidth).append("  h=").append(mappedROIHeight);
            	LOGGER.fine(sb.toString());
            }
            
            codestream.Apply_input_restrictions(nComponents, componentIndexes,
                    nDiscardLevels, qualityLayers, dimsROI,
                    Kdu_global.KDU_WANT_OUTPUT_COMPONENTS);

            // //
            //
            // Setting parameters for stripe decompression
            //
            // //

            // Array with one entry for each image component, identifying the
            // number of lines to be decompressed for that component in the
            // present call. All entries must be non-negative.
            final int[] stripeHeights = new int[nComponents];

            // Array with one entry for each image component, identifying
            // the separation between horizontally adjacent samples within the
            // corresponding stripe buffer found in the stripe_bufs array.
            final int[] sampleGap = new int[nComponents];

            // Array with one entry for each image component, identifying
            // the separation between vertically adjacent samples within the
            // corresponding stripe buffer found in the stripe_bufs array.
            final int[] rowGap = new int[nComponents];

            // Array with one entry for each image component, identifying the
            // position of the first sample of that component within the buffer
            // array.
            final int[] sampleOffset = new int[nComponents];

            // Array with one entry for each image component, identifying the
            // number of significant bits used to represent each sample.
            // There is no implied connection between the precision values, P,
            // and the bit-depth, B, of each image component, as found in the
            // code-stream's SIZ marker segment, and returned via
            // kdu_codestream::get_bit_depth. The original image sample
            // bit-depth, B, may be larger or smaller than the value of P
            // supplied via the precisions argument. The samples returned by
            // pull_stripe all have a nominally signed representation unless
            // otherwise indicated by a non-NULL isSigned argument
            final int precision[] = new int[nComponents];

            for (int component = 0; component < nComponents; component++) {
                stripeHeights[component] = requiredRegion.height;
                sampleGap[component] = nComponents;
                rowGap[component] = requiredRegion.width * nComponents;
                sampleOffset[component] = component;
                precision[component] = codestream.Get_bit_depth(component);
            }

            // ////////////////////////////////////////////////////////////////
            //
            // STEP 6.
            // -------
            // Stripe decompressor data loading
            // ////////////////////////////////////////////////////////////////
            Kdu_stripe_decompressor decompressor = new Kdu_stripe_decompressor();
            decompressor.Start(codestream);

            /**
             * @todo: actually, we only handle fixed point types. No float or
             *        double data are handled.
             */
            if (maxBitDepth <= 8) {
                final byte[] bufferValues = new byte[destBufferSize];
                decompressor.Pull_stripe(bufferValues, stripeHeights,
                        sampleOffset, sampleGap, rowGap, precision);
                imageBuffer = new DataBufferByte(bufferValues, destBufferSize);
            } else if (maxBitDepth > 8 && maxBitDepth <= 16) {
                final boolean[] isSigned = new boolean[nComponents];
                for (int i = 0; i < isSigned.length; i++)
                    isSigned[i] = codestream.Get_signed(i);
                final short[] bufferValues = new short[destBufferSize];
                decompressor.Pull_stripe(bufferValues, stripeHeights,
                        sampleOffset, sampleGap, rowGap, precision, isSigned);
                imageBuffer = new DataBufferUShort(bufferValues, destBufferSize);
            } else if (maxBitDepth > 16 && maxBitDepth <= 32) {
                final int[] bufferValues = new int[destBufferSize];
                decompressor.Pull_stripe(bufferValues, stripeHeights,
                        sampleOffset, sampleGap, rowGap, precision);
                imageBuffer = new DataBufferInt(bufferValues, destBufferSize);
            }

            // ////////////////////////////////////////////////////////////////
            //
            // STEP 6.bis
            // ----------
            // Kakadu items deallocation/finalization
            // ////////////////////////////////////////////////////////////////
            decompressor.Finish();
            decompressor.Native_destroy();
            codestream.Destroy();

            // ////////////////////////////////////////////////////////////////
            //
            // STEP 7.
            // -------
            // BufferedImage Creation
            //
            // ////////////////////////////////////////////////////////////////
            final SampleModel sampleModel = sm.createCompatibleSampleModel(
                    requiredRegion.width, requiredRegion.height);
            bi = new BufferedImage(cm, Raster.createWritableRaster(sampleModel,
                    imageBuffer, null), false, null);

        } catch (KduException e) {
            throw new RuntimeException(
                    "Error caused by a Kakadu exception during creation of key objects! ", e);
        } catch (RasterFormatException rfe) {
            throw new RuntimeException("Error during raster creation", rfe);
        } finally {
        	if (!isRawSource) {
                try {
					if (localWrappedSource.Exists())
					    localWrappedSource.Close();
				} catch (KduException e) {
				}
                localWrappedSource.Native_destroy();
                try {
					if (localFamilySource.Exists())
					    localFamilySource.Close();
				} catch (KduException e) {
				}
                localFamilySource.Native_destroy();

            } else {
                localRawSource.Native_destroy();
            }
        
        	
            if (deleteInputFile && inputFile.exists()) {
                inputFile.delete();
            }
        }

        // ////////////////////////////////////////////////////////////////
        //
        // STEP 8 (optional).
        // ------------------
        // Image resampling if needed (See STEP 3 for additional info)
        //
        // ////////////////////////////////////////////////////////////////
        if (resamplingIsRequired && bi != null)
            return KakaduUtilities.subsampleImage(codestreamP.getColorModel(), bi, destinationRegion.width, destinationRegion.height,interpolationType);
        return bi;
    }
    
    /**
     * Checks on the required region and mapped region, in compliance with
     * Formula 11.1 of Taubman's JPEG2000 compression book.
     *  
     * @param dims 
     * 			dimensions of the image region in the actual resolution level
     * @param requiredRegion 
     * 			The required region
     * @throws KduException
     */
    private void checkBounds(final Kdu_dims dims, final Rectangle requiredRegion) throws KduException {
        if (dims == null)
            throw new IllegalArgumentException("Provided Kdu_dims object is null");
        if (requiredRegion == null)
        	throw new IllegalArgumentException("Provided region is null");
        final int mappedWidth = dims.Access_size().Get_x();
        final int mappedHeight = dims.Access_size().Get_y();
        if (requiredRegion.y + requiredRegion.height > mappedHeight)
            requiredRegion.height--;
        if (requiredRegion.x + requiredRegion.width > mappedWidth)
            requiredRegion.width --;
	}

    /**
     * Set a Kdu_dims object to crop the required region. 
     */
	private void setDimsForCrop(final Kdu_dims dims, final Rectangle requiredRegion) throws KduException {
    	if (dims == null)
    		throw new IllegalArgumentException("Provided Kdu_dims object is null");
    	if (requiredRegion == null)
    		throw new IllegalArgumentException("Provided region is null");
        dims.Access_pos().Set_x(dims.Access_pos().Get_x() + requiredRegion.x);
        dims.Access_pos().Set_y(dims.Access_pos().Get_y() + requiredRegion.y);
        dims.Access_size().Set_x(requiredRegion.width);
        dims.Access_size().Set_y(requiredRegion.height);
		
	}

	/**
     * Parses the input <code>ImageReadParam</code> and provides to setup a
     * proper sourceRegion and a proper destinationRegion. After the call of
     * this method, the input parameters <code>sourceRegion</code> and
     * <code>destinationRegion</code> will contain the computed regions.
     * Region computations include adjusting edges and subsampling factors
     * evaluation for the determination of the destination region.
     * 
     * @param width
     *                the original image width
     * @param height
     *                the original image height
     * @param param
     *                an <code>ImageReadParam</code> specifying subsampling
     *                factors and, optionally, a user specified source region.
     * @param sourceRegion
     *                a <code>Rectangle</code> which will contain the source
     *                region to be used in the read operation.
     * @param destinationRegion
     *                a <code>Rectangle</code> which will contain the computed
     *                destination region.
     */
    private static void computeRegions(final int width, final int height,
            final ImageReadParam param, final Rectangle sourceRegion,
            final Rectangle destinationRegion) {

        // //
        //
        // Getting input parameters
        //
        // //
        final Rectangle paramRegion = param.getSourceRegion();
        final int xSubsamplingFactor = param.getSourceXSubsampling();
        final int ySubsamplingFactor = param.getSourceYSubsampling();

        if (paramRegion != null) {
            sourceRegion.setBounds(paramRegion);

            // ////////////////////////////////////////////////////////////////
            //
            // Minimum correction for wrong source regions
            //
            // When you do subsampling or source subsetting it might happen that
            // the given source region in the read param is uncorrect, which
            // means it can be or a bit larger than the original file or can
            // begin a bit before original limits.
            //
            // We got to be prepared to handle such case in order to avoid
            // generating ArrayIndexOutOfBoundsException later in the code.
            //
            // ////////////////////////////////////////////////////////////////
            if (sourceRegion.x < 0)
                sourceRegion.x = 0;
            if (sourceRegion.y < 0)
                sourceRegion.y = 0;

            // initializing destination image properties
            destinationRegion.x = sourceRegion.x;
            if ((sourceRegion.x + sourceRegion.width) > width) {
                sourceRegion.width = width - sourceRegion.x;
            }
            destinationRegion.width = sourceRegion.width;
            
            destinationRegion.y = sourceRegion.y;
            if ((sourceRegion.y + sourceRegion.height) > height) {
                sourceRegion.height = height - sourceRegion.y;
            }
            destinationRegion.height = sourceRegion.height;

        } else {

            // Source Region not specified.
            // Assuming Source Region Dimension equal to Source Image Dimension
            destinationRegion.setBounds(0, 0, width, height);
            sourceRegion.setBounds(0, 0, width, height);
        }

        // ////////////////////////////////////////////////////////////////////
        //
        // Updating the destination size in compliance with the subSampling
        // parameters
        //
        // ////////////////////////////////////////////////////////////////////
        destinationRegion.width = ((destinationRegion.width - 1) / xSubsamplingFactor) + 1;
        destinationRegion.height = ((destinationRegion.height - 1) / ySubsamplingFactor) + 1;

        if (sourceRegion.x != 0) {
            destinationRegion.x = sourceRegion.x  / xSubsamplingFactor;
        }
        if (sourceRegion.y != 0) {
            destinationRegion.y = sourceRegion.y / ySubsamplingFactor;
        }
    }

    /**
     * Computes the set of parameters to be used by the
     * {@code Kdu_stripe_decompressor} in the next operation. Mainly, given a
     * specified sourceRegion, a destinationRegion and a set of x/y subsampling
     * factors, it allows to setup a requiredRegion, the optimal resolution to
     * be used as parameters of the stripe decompressor. Moreover, it decides if
     * the image coming from the stripe decompressor should be resampled.
     * Resampling is usually needed when requesting asymmetric subsampling
     * factors (x != y) or when requesting a subsampling factor which isn't a
     * power of 2.
     * 
     * @param maxSupportedSubSamplingFactor
     *                the max supported subsamplingfactor
     * @param xSubsamplingFactor
     *                the requested subsamplingfactor along x
     * @param ySubsamplingFactor
     *                the requested subsamplingfactor along y
     * @param sourceRegion
     *                the input source region
     * @param destinationRegion
     *                the desired output region
     * @param requiredRegion
     *                a suitable region related to the data values which will be
     *                obtained by the stripe decompressor
     * @param resolutionInfo
     *                an {@code int[]} containing: the optimal subsampling
     *                factor at index 0; the number of DWT levels to be
     *                discarded at index 1
     * @return {@code true} in case the data obtained by the stripe decompressor
     *         in the following operation need to be resampled. {@code false} in
     *         case the data is ready to be used.
     */
    private boolean getRequiredRegionsAndResolutions(
            final JP2KCodestreamProperties codestreamP,
            final int xSubsamplingFactor, final int ySubsamplingFactor,
            final Rectangle sourceRegion, final Rectangle destinationRegion,
            final Rectangle requiredRegion, final int resolutionInfo[]) {
        boolean resamplingIsRequired = false;
        int newSubSamplingFactor = 0;
        final int maxSupportedSubSamplingFactor = codestreamP
                .getMaxSupportedSubSamplingFactor();

        // Preliminar check: Are xSSF and ySSF different?
        final boolean subSamplingFactorsAreDifferent = (xSubsamplingFactor != ySubsamplingFactor);

        // Let be nSSF the minimum of xSSF and ySSF (They may be equals).
        newSubSamplingFactor = (xSubsamplingFactor <= ySubsamplingFactor) ? xSubsamplingFactor
                : ySubsamplingFactor;
        // if nSSF is greater than the maxSupportedSubSamplingFactor
        // (MaxSupSSF), it needs to be adjusted.
        final boolean changedSubSamplingFactors = (newSubSamplingFactor > maxSupportedSubSamplingFactor);
        if (newSubSamplingFactor > maxSupportedSubSamplingFactor)
            newSubSamplingFactor = maxSupportedSubSamplingFactor;
        final int info[] = KakaduUtilities.findOptimalResolutionInfo(
                codestreamP.getSourceDWTLevels(), newSubSamplingFactor);

        resamplingIsRequired = subSamplingFactorsAreDifferent
                || changedSubSamplingFactors || info[0] != newSubSamplingFactor;
        if (!resamplingIsRequired) {
            // xSSF and ySSF are equal and they are not greater than MaxSuppSSF
            requiredRegion.setBounds(destinationRegion);
        } else {
            // xSSF and ySSF are different or they are greater than MaxSuppSFF.
            // We need to find a new subsampling factor to load a proper region.
            newSubSamplingFactor = info[0];
            requiredRegion.width = ((sourceRegion.width - 1) / newSubSamplingFactor) + 1;
            requiredRegion.height = ((sourceRegion.height - 1) / newSubSamplingFactor) + 1;
            if (sourceRegion.x != 0) {
                requiredRegion.x = sourceRegion.x / newSubSamplingFactor;
            }
            if (sourceRegion.y != 0) {
                requiredRegion.y = sourceRegion.y / newSubSamplingFactor;
            }
        }
        resolutionInfo[0] = newSubSamplingFactor;
        resolutionInfo[1] = info[1];
        return resamplingIsRequired;
    }

    public void setInput(Object input, boolean seekForwardOnly,
            boolean ignoreMetadata) {

        // //
        //
        // Reset the reader prior to do anything with it
        //
        // //
        reset();

        // //
        //
        // Check the input
        //
        // //
        if (input == null)
            throw new NullPointerException("The provided input is null!");

        // Checking if the provided input is a File
        if (input instanceof File) {
            inputFile = (File) input;
            // Checking if the provided input is a FileImageInputStreamExt
        } else if (input instanceof FileImageInputStreamExt) {
            inputFile = ((FileImageInputStreamExt) input).getFile();
            // Checking if the provided input is a URL
        } else if (input instanceof URL) {
            final URL tempURL = (URL) input;
            if (tempURL.getProtocol().equalsIgnoreCase("file")) {
                    inputFile = Utilities.urlToFile(tempURL);
            }
        } else if (input instanceof ImageInputStream) {
            try {
                inputFile = File.createTempFile("buffer", ".j2c");
                FileOutputStream fos = new FileOutputStream(inputFile);
                final byte buff[] = new byte[TEMP_BUFFER_SIZE];
                int bytesRead = 0;
                while ((bytesRead = ((ImageInputStream) input).read(buff)) != -1)
                    fos.write(buff, 0, bytesRead);
                fos.close();

                input = inputFile;
                deleteInputFile = true;
            } catch (IOException ioe) {
                throw new RuntimeException("Unable to create a temp file", ioe);
            }
        }

        if (this.inputFile == null)
            throw new IllegalArgumentException("Invalid source provided.");
        fileName = inputFile.getAbsolutePath();

        // //
        //
        // Open it up
        //
        // //
        Kdu_simple_file_source rawSource = null; // Must be disposed last
        final Jp2_family_src familySource = new Jp2_family_src(); // Dispose
        // last
        final Jpx_source wrappedSource = new Jpx_source(); // Dispose in the
        // middle
        Kdu_codestream codestream = new Kdu_codestream();
        try {

            // Open input file as raw codestream or a JP2/JPX file
            familySource.Open(fileName);
            final int success = wrappedSource.Open(familySource, true);
            if (success < 0) {
                // //
                //
                // Must open as raw file
                //
                // //
                familySource.Close();
                wrappedSource.Close();
                rawSource = new Kdu_simple_file_source(fileName);
                if (rawSource != null) {
                	 if (fileName != null){
                         FileInputStream fis = new FileInputStream (new File(fileName));
                         byte[] jp2SocMarker = new byte[2];
                         fis.read(jp2SocMarker);
                         if (jp2SocMarker[0] != (byte)0xFF || jp2SocMarker[1] != (byte) 0x4F)
                        	 throw new IllegalArgumentException("Not a JP2K source.");
                         fis.close();
                     }
                    isRawSource = true;
                    if (LOGGER.isLoggable(Level.FINE))
                        LOGGER.fine("Detected raw source");
                    numImages = 1;
                }
            } else {
                // we have a valid jp2/jpx file
                isRawSource = false;

                // //
                //
                // get the number of codestreams in this jpeg2000 file
                //
                // //
                final int[] count = new int[1];
                if (wrappedSource.Count_codestreams(count))
                    numImages = count[0];
                else
                    numImages = 0;
            }

            if (!isRawSource)
                fileWalker = new JP2KFileWalker(this.fileName);
            for (int cs = 0; cs < numImages; cs++) {
                if (isRawSource) {
                    codestream.Create(rawSource);
                } else {
                    final Jpx_codestream_source stream = wrappedSource
                            .Access_codestream(cs);
                    final Jpx_input_box inputbox = stream.Open_stream();
                    codestream.Create(inputbox);
                }
                final JP2KCodestreamProperties codestreamP = new JP2KCodestreamProperties();

                // //
                //
                // Initializing size-related properties (Image dims, Tiles)
                //
                // //
                final Kdu_dims imageDims = new Kdu_dims();
                codestream.Access_siz().Finalize_all();
                codestream.Get_dims(-1, imageDims, false);
                final Kdu_dims tileSize = new Kdu_dims();
                codestream.Get_tile_dims(new Kdu_coords(0, 0), -1, tileSize);

                int tileWidth = tileSize.Access_size().Get_x();
                int tileHeight = tileSize.Access_size().Get_y();

                // Tuning tiles in case the tile size is greater than the
                // maximum integer
                if ((float) tileWidth * tileHeight >= Integer.MAX_VALUE) {
                    // TODO: Customize these settings
                    tileHeight = 1024;
                    tileWidth = 1024;
                }
                codestreamP.setTileWidth(tileWidth);
                codestreamP.setTileHeight(tileHeight);

                codestreamP.setWidth(imageDims.Access_size().Get_x());
                codestreamP.setHeight(imageDims.Access_size().Get_y());
                final int nComponents = codestream.Get_num_components();
                int maxBitDepth = -1;
                int[] componentIndexes = new int[nComponents];
                int[] bitsPerComponent = new int[nComponents];
                boolean isSigned = false;
                for (int i = 0; i < nComponents; i++) {
                    // TODO: FIX THIS
                    bitsPerComponent[i] = codestream.Get_bit_depth(i);
                    if (maxBitDepth < bitsPerComponent[i]) {
                        maxBitDepth = bitsPerComponent[i];
                    }
                    isSigned |= codestream.Get_signed(i);
                    componentIndexes[i] = i;
                }
                codestreamP.setNumComponents(nComponents);
                codestreamP.setBitsPerComponent(bitsPerComponent);
                codestreamP.setComponentIndexes(componentIndexes);
                codestreamP.setMaxBitDepth(maxBitDepth);
                codestreamP.setSigned(isSigned);

                // Initializing Resolution levels and Quality Layers
                final int sourceDWTLevels = codestream.Get_min_dwt_levels();
                codestreamP.setSourceDWTLevels(sourceDWTLevels);
                codestreamP
                        .setMaxSupportedSubSamplingFactor(1 << sourceDWTLevels);
                Kdu_coords tileCoords = new Kdu_coords();
                tileCoords.Set_x(0);
                tileCoords.Set_y(0);
                codestream.Open_tile(tileCoords);
                codestreamP.setMaxAvailableQualityLayers(codestream
                        .Get_max_tile_layers());
                initializeSampleModelAndColorModel(codestreamP);
                codestream.Destroy();
                multipleCodestreams.add(codestreamP);

                if (isRawSource)
                    break;
            }

        } catch (KduException e) {
            throw new RuntimeException(
                    "Error caused by a Kakadu exception during creation of key objects!" ,e);
        } catch (FileNotFoundException e) {
        	 throw new RuntimeException(
                     "Exception occurred during kakadu reader initialization", e);
		} catch (IOException e) {
			throw new RuntimeException(
                    "Exception occurred during kakadu reader initialization", e);
			
		} finally {
            if (!isRawSource && wrappedSource != null) {
                try {
                    if (wrappedSource.Exists())
                        wrappedSource.Close();
                } catch (Throwable e) {
                    // yeah I am eating this
                }
                try {
                    wrappedSource.Native_destroy();
                } catch (Throwable e) {
                    // yeah I am eating this
                }

                if (familySource != null) {
                    try {
                        if (familySource.Exists())
                            familySource.Close();
                    } catch (Throwable e) {
                        // yeah I am eating this
                    }
                    try {
                        familySource.Native_destroy();
                    } catch (Throwable e) {
                        // yeah I am eating this
                    }
                }

            } else if (isRawSource && rawSource != null) {
                try {
                    if (wrappedSource.Exists())
                        wrappedSource.Close();
                } catch (Throwable e) {
                    // yeah I am eating this
                }
            }
        }

        // //
        //
        // Setting input for superclass
        //
        // //
        super.setInput(input, seekForwardOnly, ignoreMetadata);
    }

    /**
     * Disposes all the resources, native and non, used by this
     * {@link ImageReader} subclass.
     */
    public void dispose() {
        super.dispose();
        if (multipleCodestreams != null) {
            multipleCodestreams.clear();
        }
        numImages = 1;
        fileWalker = null;
    }

    /**
     * Returns the height of a tile
     * 
     * @param the
     *                index of the selected image
     */
    public int getTileHeight(int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        final int tileHeight = multipleCodestreams.get(imageIndex)
                .getTileHeight();
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine(new StringBuffer("tileHeight:").append(
                    Integer.toString(tileHeight)).toString());
        return tileHeight;
    }

    /**
     * Returns the width of a tile
     * 
     * @param the
     *                index of the selected image
     */
    public int getTileWidth(int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        final int tileWidth = multipleCodestreams.get(imageIndex)
                .getTileWidth();
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine(new StringBuffer("tileWidth:").append(
                    Integer.toString(tileWidth)).toString());
        return tileWidth;
    }

    /**
     * Initialize SampleModel and ColorModel
     * 
     * @throws KduException
     */
    private synchronized void initializeSampleModelAndColorModel(
            JP2KCodestreamProperties codestreamP) throws KduException {
        if (codestreamP.getSampleModel() != null
                && codestreamP.getColorModel() != null)
            return;
        parseBoxes(codestreamP);
        if (codestreamP.getColorModel() == null)
            codestreamP.setColorModel(getColorModel(codestreamP));

        if (codestreamP.getSampleModel() == null)
            codestreamP.setSampleModel(getSampleModel(codestreamP));
    }

    /**
     * Setup a proper <code>ColorModel</code>
     * 
     * @return a color model.
     * @throws KduException
     * 
     */
    private static ColorModel getColorModel(JP2KCodestreamProperties codestreamP)
            throws KduException {
        if (codestreamP.getColorModel() != null)
            return codestreamP.getColorModel();

        
        if (codestreamP.getColorModel() != null)
            return codestreamP.getColorModel();

        final int nComponents = codestreamP.getNumComponents();
        if (nComponents <= 4) {
            ColorSpace cs;
            if (nComponents > 2) {
                cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            } else {
                cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            }

            boolean hasAlpha = nComponents % 2 == 0;

            final int maxBitDepth = codestreamP.getMaxBitDepth();
            if (maxBitDepth <= 8) {
                codestreamP.setDataBufferType(DataBuffer.TYPE_BYTE);
            } else if (maxBitDepth <= 16) {
                if (codestreamP.isSigned())
                    codestreamP.setDataBufferType(DataBuffer.TYPE_SHORT);
                else
                    codestreamP.setDataBufferType(DataBuffer.TYPE_USHORT);
            } else if (maxBitDepth <= 32) {
                codestreamP.setDataBufferType(DataBuffer.TYPE_INT);
            }

            final int dataBufferType = codestreamP.getDataBufferType();
            if (dataBufferType != -1) {
                if (nComponents == 1
                        && (maxBitDepth == 1 || maxBitDepth == 2 || maxBitDepth == 4)) {
                    codestreamP.setColorModel(ImageUtil
                            .createColorModel(getSampleModel(codestreamP)));
                } else {
                    codestreamP.setColorModel(new ComponentColorModel(cs,
                            codestreamP.getBitsPerComponent(), hasAlpha, false,
                            hasAlpha ? Transparency.TRANSLUCENT
                                    : Transparency.OPAQUE, dataBufferType));
                }
                return codestreamP.getColorModel();
            }
        }

        if (codestreamP.getSampleModel() == null)
            codestreamP.setSampleModel(getSampleModel(codestreamP));

        if (codestreamP.getSampleModel() == null)
            return null;

        return ImageUtil.createColorModel(codestreamP.getSampleModel());
    }

    /**
     * Get basic image properties by querying several JP2Boxes. Then, properly
     * set the ColorModel of the input object.
     * 
     * @param codestreamP
     */
    private void parseBoxes(JP2KCodestreamProperties codestreamP) {
        if (isRawSource)
            return;
        short numComp = 1;
        byte[] bitDepths = null;
        byte[] maps = null;
        int bitDepth = -1;
        ICC_Profile profile = null;
        int colorSpaceType = -1;

        // //
        //
        // ImageHeader Box
        //
        // //
        final ImageHeaderBox ihBox = (ImageHeaderBox) getJp2Box(ImageHeaderBox.BOX_TYPE);
        if (ihBox != null) {
            numComp = ihBox.getNumComponents();
            bitDepth = ihBox.getBitDepth();
        }

        // //
        //
        // ColorSpecification Box
        //
        // //
        final ColorSpecificationBox csBox = (ColorSpecificationBox) getJp2Box(ColorSpecificationBox.BOX_TYPE);
        if (csBox != null) {
            profile = csBox.getICCProfile();
            colorSpaceType = csBox.getEnumeratedColorSpace();
        }

        // //
        //
        // ComponentMapping Box
        //
        // //
        final ComponentMappingBox cmBox = (ComponentMappingBox) getJp2Box(ComponentMappingBox.BOX_TYPE);
        if (cmBox != null) {
            maps = cmBox.getComponentAssociation();
        }

        // //
        //
        // Palette Box
        //
        // //
        final PaletteBox palBox = (PaletteBox) getJp2Box(PaletteBox.BOX_TYPE);
        if (palBox != null) {
            byte[][] lookUpTable = palBox.getLUT();

            if (lookUpTable != null && numComp == 1) {
                int tableComps = lookUpTable.length;

                int maxDepth = 1 + (bitDepth & 0x7F);

                if (maps == null) {
                    maps = new byte[tableComps];
                    for (int i = 0; i < tableComps; i++)
                        maps[i] = (byte) i;
                }
                if (tableComps == 3) {
                    codestreamP.setColorModel(new IndexColorModel(maxDepth,
                            lookUpTable[0].length, lookUpTable[maps[0]],
                            lookUpTable[maps[1]], lookUpTable[maps[2]]));
                    return;
                } else if (tableComps == 4) {
                    codestreamP.setColorModel(new IndexColorModel(maxDepth,
                            lookUpTable[0].length, lookUpTable[maps[0]],
                            lookUpTable[maps[1]], lookUpTable[maps[2]],
                            lookUpTable[maps[3]]));
                    return;
                }
            }
        }

        // //
        //
        // BitsPerComponent Box
        //
        // //
        final BitsPerComponentBox bpcBox = (BitsPerComponentBox) getJp2Box(BitsPerComponentBox.BOX_TYPE);
        if (bpcBox != null) {
            bitDepths = bpcBox.getBitDepth();
        }

        // //
        //
        // ChannelDefinition Box
        //
        // //
        final ChannelDefinitionBox chBox = (ChannelDefinitionBox) getJp2Box(ChannelDefinitionBox.BOX_TYPE);
        if (chBox != null) {
            final short[] channels = chBox.getChannel();
            final short[] associations = chBox.getAssociation();
            final int[] cType = chBox.getTypes();
            boolean hasAlpha = false;
            final int alphaChannel = numComp - 1;

            for (int i = 0; i < channels.length; i++) {
                if (cType[i] == 1 && channels[i] == alphaChannel)
                    hasAlpha = true;
            }

            boolean[] isPremultiplied = new boolean[] { false };

            if (hasAlpha) {
                isPremultiplied = new boolean[alphaChannel];

                for (int i = 0; i < alphaChannel; i++)
                    isPremultiplied[i] = false;

                for (int i = 0; i < channels.length; i++) {
                    if (cType[i] == 2)
                        isPremultiplied[associations[i] - 1] = true;
                }

                for (int i = 1; i < alphaChannel; i++)
                    isPremultiplied[0] &= isPremultiplied[i];
            }

            ColorSpace cs = null;
            
            //RGBN Workaround
        	if (associations.length == 4/* && associations[0]==1 && associations[1]==2 && associations[2]==3*/){
        		cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        		hasAlpha = true;
        	}
        	else if (profile != null)
                cs = new ICC_ColorSpace(profile);
            else if (colorSpaceType == ColorSpecificationBox.ECS_sRGB)
                cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            else if (colorSpaceType == ColorSpecificationBox.ECS_GRAY)
                cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            else if (colorSpaceType == ColorSpecificationBox.ECS_YCC)
                cs = ColorSpace.getInstance(ColorSpace.CS_PYCC);
            else	
                LOGGER.warning("JP2 type only handle sRGB, GRAY and YCC Profiles");

            // TODO: Check these settings
            int[] bits = new int[numComp];
            for (int i = 0; i < numComp; i++)
                if (bitDepths != null)
                    bits[i] = (bitDepths[i] & 0x7F) + 1;
                else
                    bits[i] = (bitDepth & 0x7F) + 1;

            int maxBitDepth = 1 + (bitDepth & 0x7F);
            boolean isSigned = (bitDepth & 0x80) == 0x80;
            if (bitDepths != null)
                isSigned = (bitDepths[0] & 0x80) == 0x80;

            if (bitDepths != null)
                for (int i = 0; i < numComp; i++)
                    if (bits[i] > maxBitDepth)
                        maxBitDepth = bits[i];

            int type = -1;

            if (maxBitDepth <= 8)
                type = DataBuffer.TYPE_BYTE;
            else if (maxBitDepth <= 16)
                type = isSigned ? DataBuffer.TYPE_SHORT
                        : DataBuffer.TYPE_USHORT;
            else if (maxBitDepth <= 32)
                type = DataBuffer.TYPE_INT;

            if (type == -1)
                return;

            if (cs != null) {
                codestreamP.setColorModel(new ComponentColorModel(cs, bits,
                        hasAlpha, isPremultiplied[0],
                        hasAlpha ? Transparency.TRANSLUCENT
                                : Transparency.OPAQUE, type));
            }
        }
    }

    /**
     * Setup a proper <code>SampleModel</code>
     * 
     * @return a sample model.
     * @throws KduException
     */
    private static SampleModel getSampleModel(JP2KCodestreamProperties codestreamP) {
        if (codestreamP == null)
            throw new IllegalArgumentException(
                    "null codestream properties provided");
        if (codestreamP.getSampleModel() != null)
            return codestreamP.getSampleModel();

        final int nComponents = codestreamP.getNumComponents();
        final int maxBitDepth = codestreamP.getMaxBitDepth();
        final int tileWidth = codestreamP.getTileWidth();
        final int tileHeight = codestreamP.getTileHeight();
        if (nComponents == 1
                && (maxBitDepth == 1 || maxBitDepth == 2 || maxBitDepth == 4))
            codestreamP.setSampleModel(new MultiPixelPackedSampleModel(
                    DataBuffer.TYPE_BYTE, tileWidth, tileHeight, maxBitDepth));
        else if (maxBitDepth <= 8)
            codestreamP
                    .setSampleModel(new PixelInterleavedSampleModel(
                            DataBuffer.TYPE_BYTE, tileWidth, tileHeight,
                            nComponents, tileWidth * nComponents, codestreamP
                                    .getComponentIndexes()));
        else if (maxBitDepth <= 16)
            codestreamP.setSampleModel(new PixelInterleavedSampleModel(
                    codestreamP.isSigned() ? DataBuffer.TYPE_SHORT
                            : DataBuffer.TYPE_USHORT, tileWidth, tileHeight,
                    nComponents, tileWidth * nComponents, codestreamP
                            .getComponentIndexes()));
        else if (maxBitDepth <= 32)
            codestreamP
                    .setSampleModel(new PixelInterleavedSampleModel(
                            DataBuffer.TYPE_INT, tileWidth, tileHeight,
                            nComponents, tileWidth * nComponents, codestreamP
                                    .getComponentIndexes()));
        else
            throw new IllegalArgumentException("Unhandled sample model");
        return codestreamP.getSampleModel();
    }

    /**
     * Build a default {@link JP2KKakaduImageReadParam}
     */
    public ImageReadParam getDefaultReadParam() {
        return new JP2KKakaduImageReadParam();
    }

    /**
     * returns the number of source DWT levels for the specified image.
     * 
     * @return the number of source DWT levels.
     */
    public int getSourceDWTLevels(int imageIndex) {
        checkImageIndex(imageIndex);
        return multipleCodestreams.get(imageIndex).getSourceDWTLevels();
    }

    /**
     * Returns a {@link JP2KBox} given the boxType name.
     * 
     * @param boxType
     *                the type of required Jp2 Box
     * @return a {@link JP2KBox} given the boxType name. Return
     *         <code>null</code> if not found.
     */
    private JP2KBox getJp2Box(final String boxType) {
        final TreeModel boxesTree = fileWalker.getJP2KBoxesTree();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) boxesTree.getRoot();
        DefaultMutableTreeNode node = null;
        if (root != null)
            for (Enumeration e = root.breadthFirstEnumeration(); e.hasMoreElements();) {
                final DefaultMutableTreeNode current = (DefaultMutableTreeNode) e.nextElement();
                final JP2KBox box = (JP2KBox) current;
                if (box != null && BoxUtilities.getTypeInt(boxType) == box.getType()) {
                    node = current;
                    break;
                }
            }

        if (node != null) {
            return LazyJP2KBox.getAsOriginalBox((JP2KBox) node);
        }

        return null;

    }

    /**
     * Returns a proper {@link JP2KBox} subclass instance given a specified box
     * type.
     * 
     * @param boxType
     * @return a proper
     */
    private JP2KBox getJp2Box(final int boxType) {
        return getJp2Box(BoxUtilities.getBoxName(boxType));
    }

    /**
     * Reset main values
     */
    public void reset() {
        super.setInput(null, false, false);
        dispose();
        numImages = -1;
        isRawSource = false;
    }

    File getInputFile() {
        return inputFile;
    }

    String getFileName() {
        return fileName;
    }
}