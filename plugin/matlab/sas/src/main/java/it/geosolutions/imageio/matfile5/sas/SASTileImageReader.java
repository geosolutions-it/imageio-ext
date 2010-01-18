/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
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
package it.geosolutions.imageio.matfile5.sas;

import it.geosolutions.imageio.matfile5.MatFileImageReader;
import it.geosolutions.imageio.matfile5.sas.SASTileMetadata.Channel;
import it.geosolutions.imageio.utilities.Utilities;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;

import com.jmatio.io.MatFileFilter;
import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLNumericArray;

public class SASTileImageReader extends MatFileImageReader {

    public SASTileImageReader(SASTileImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    private final static boolean COMPUTE_LOGARITHM;
    
    private final static boolean DISABLE_MEDIALIB_LOG;

    static{
        final String cl = System.getenv("SAS_COMPUTE_LOG");
        final String disableMediaLog = System.getenv("DISABLE_MEDIALIB_LOG");
        if (cl!=null && cl.trim().length()>0)
        	COMPUTE_LOGARITHM = Boolean.parseBoolean(cl);
        else 
            COMPUTE_LOGARITHM = true;
        if (disableMediaLog!=null && disableMediaLog.trim().length()>0)
            DISABLE_MEDIALIB_LOG = Boolean.parseBoolean(disableMediaLog);
        else 
            DISABLE_MEDIALIB_LOG = false;
        if (DISABLE_MEDIALIB_LOG){
            Utilities.setNativeAccelerationAllowed("Log",false);
        }
    }
    
    private boolean isInitialized = false;

    private SASTileMetadata sasTile = null;

    protected synchronized void initialize() {
        if (!isInitialized) {
            final Object datainput = super.getInput();
            final String fileName = getDatasetSource(datainput).getAbsolutePath();

            final MatFileFilter filter = new MatFileFilter();
            initFilter(filter, SASTileMetadata.getFilterElements());

            try {

                matReader = new MatFileReader(fileName, filter, true);
                
                sasTile = new SASTileMetadata(matReader);

            } catch (IOException e) {
                throw new RuntimeException("Unable to Initialize the reader", e);
            }

        }
        isInitialized = true;
    }

    /**
     * Returns the height of the raster.
     * 
     * @param imageIndex
     *                the index of the specified raster
     * @return raster height
     */
    @Override
    public int getHeight(int imageIndex) throws IOException {
        initialize();
        return sasTile.getYPixels();
    }

    /**
     * Returns the width of the raster.
     * 
     * @param imageIndex
     *                the index of the specified raster
     * @return raster width
     */
    @Override
    public int getWidth(int imageIndex) throws IOException {
        initialize();
        return sasTile.getXPixels();
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        initialize();
        return sasTile;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param)
            throws IOException {
        initialize();

        final int width = sasTile.getXPixels();
        final int height = sasTile.getYPixels();
        
        Channel channel = sasTile.getChannel();

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
        // Reading data
        //
        // ////////////////////////////////////////////////////////////////////
        final Rectangle roi = new Rectangle(
                srcRegionXOffset, srcRegionYOffset, srcRegionWidth,
                srcRegionHeight);

        final MLArray mlArrayRetrived = sasTile.isLogScale() ? matReader
                .getMLArray(SASTileMetadata.SAS_TILE_LOG) : matReader
                .getMLArray(SASTileMetadata.SAS_TILE_RAW);
        final ByteBuffer real = ((MLNumericArray<Number>) mlArrayRetrived).getRealByteBuffer();
        final ByteBuffer imaginary = ((MLNumericArray<Number>) mlArrayRetrived).getImaginaryByteBuffer();
        
        final boolean isDouble = (mlArrayRetrived instanceof MLDouble)? true : false;
        final int imageSize = width * height;
        
        // //
        //
        // Note that the underlying matrix fill a buffer where samples are sorted as:
        // First row, first column, second row, first column, third row, first column...
        // Therefore I'm getting a transposed image. I will transpose it afterwards.
        // Note that I'm building a Sample Model with height and width swapped.
        //
        // //
        final int smWidth = height;
        final int smHeight = width;
        final PixelInterleavedSampleModel sampleModel = new PixelInterleavedSampleModel(isDouble?DataBuffer.TYPE_DOUBLE:DataBuffer.TYPE_FLOAT, smWidth, smHeight, 2, smWidth*2, new int[] { 0,1 });

        final ColorModel cm = buildColorModel(sampleModel);
        final WritableRaster originalRasterData;
        if (isDouble){
	        final double[][] dataArray = new double[2][imageSize];

	        final DoubleBuffer buffReal = real.asDoubleBuffer();
	        buffReal.get(dataArray[0]);
	
	        final DoubleBuffer buffImaginary = imaginary.asDoubleBuffer();
	        buffImaginary.get(dataArray[1]);
	        
	        final DataBufferDouble dbb= new DataBufferDouble(dataArray, imageSize);
	        originalRasterData= Raster.createWritableRaster(sampleModel,dbb, null);
	
        } else {
        	final float[][] dataArray = new float[2][imageSize];
	        
	        final FloatBuffer buffReal = real.asFloatBuffer();
	        buffReal.get(dataArray[0]);
	
	        final FloatBuffer buffImaginary = imaginary.asFloatBuffer();
	        buffImaginary.get(dataArray[1]);
	
	        final DataBufferFloat dbb= new DataBufferFloat(dataArray, imageSize);
	        originalRasterData= Raster.createWritableRaster(sampleModel,dbb, null);
        }
        BufferedImage data = new BufferedImage(cm, originalRasterData, false,null);
        
        //
        // CROP
        //
        if (srcRegion != null) {
            
            //Transpose haven't been executed yet.
            //the actual image has x-y swapped.
            
            final int x = roi.y;
            final int y = roi.x;
            final int w = roi.height;
            final int h = roi.width;
            data=data.getSubimage(x, y, w, h);
        } 
        
        
        final AffineTransform transform = AffineTransform.getRotateInstance(0);// identity
        // geometric scale to subsample
        if (xSubsamplingFactor != 1 || ySubsamplingFactor != 1) 
        	transform.preConcatenate(AffineTransform.getScaleInstance(xSubsamplingFactor, ySubsamplingFactor));

        // //
        //
        // Transposing the Matlab data matrix
        //
        // //
        final AffineTransform transposeTransform= AffineTransform.getRotateInstance(0);// identity
		if (channel == Channel.STARBOARD){
			
			// TransposeDescriptor.FLIP_DIAGONAL
			transposeTransform.preConcatenate(new AffineTransform(0, 1, 0, 1, 0, 0));
			
			
			// TransposeDescriptor.FLIP_VERTICAL
			transposeTransform.preConcatenate(AffineTransform.getScaleInstance(1,-1));
		}
		else {
			// TransposeDescriptor.FLIP_ANTIDIAGONAL
			transposeTransform.preConcatenate(AffineTransform.getScaleInstance(-1,-1));
		}
		// preconcatenate transposition
		transform.preConcatenate(transposeTransform);
		
		
		//
		// apply the geometric transform
		//
		data= new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(data,null);
		
		// //
		//
		// Computing the magnitude of the stored complex values.
		//
		// //
		return new SASBufferedImageOp(COMPUTE_LOGARITHM, null).filter(data,null);
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex)
            throws IOException {
        initialize();
        final int width = sasTile.getXPixels();
        final int height = sasTile.getYPixels();
        final PixelInterleavedSampleModel sampleModel = new PixelInterleavedSampleModel(DataBuffer.TYPE_DOUBLE, width, height, 1, width,new int[] { 0 });

        final ColorModel cm = buildColorModel(sampleModel);
        final List<ImageTypeSpecifier> l = new java.util.ArrayList<ImageTypeSpecifier>(1);
        ImageTypeSpecifier imageType = new ImageTypeSpecifier(cm,sampleModel);
        l.add(imageType);
        return l.iterator();
    }
}
