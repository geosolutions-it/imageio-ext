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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferDouble;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.TransposeDescriptor;

import com.jmatio.io.MatFileFilter;
import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLNumericArray;

public class SASTileImageReader extends MatFileImageReader {

    public SASTileImageReader(SASTileImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    private final static boolean COMPUTE_LOGARITHM;

    static{
        final String cl = System.getenv("SAS_COMPUTE_LOG");
        if (cl!=null && cl.trim().length()>0)
        	COMPUTE_LOGARITHM = Boolean.parseBoolean(cl);
        else 
        	COMPUTE_LOGARITHM = true;
    }
    
    private boolean isInitialized = false;

    private SASTileMetadata sasTile = null;

    protected synchronized void initialize() {
        if (!isInitialized) {
            final Object datainput = super.getInput();
            final String fileName = getDatasetSource(datainput)
                    .getAbsolutePath();

            final MatFileFilter filter = new MatFileFilter();
            initFilter(filter, SASTileMetadata.getFilterElements());

            try {

                matReader = new MatFileReader(new String(fileName), filter,
                        true);

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
        final ByteBuffer real = ((MLNumericArray<Number>) mlArrayRetrived)
                .getRealByteBuffer();
        final ByteBuffer imaginary = ((MLNumericArray<Number>) mlArrayRetrived)
                .getImaginaryByteBuffer();

        final int imageSize = width * height;
        final double[] dstReal = new double[imageSize];
        
        //TODO: Possible performance improvement when leveraging on subsampling and source region
        // could be working on getting samples separately instead of getting all the buffer.
        final DoubleBuffer buffReal = real.asDoubleBuffer();
        buffReal.get(dstReal);
        final DataBuffer imgBufferReal = new DataBufferDouble(dstReal,
                imageSize);

        final double[] dstImaginary = new double[imageSize];
        final DoubleBuffer buffImaginary = imaginary.asDoubleBuffer();
        buffImaginary.get(dstImaginary);

        final DataBuffer imgBufferImaginary = new DataBufferDouble(
                dstImaginary, imageSize);
        
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
        final PixelInterleavedSampleModel sampleModel = new PixelInterleavedSampleModel(
                    DataBuffer.TYPE_DOUBLE, smWidth, smHeight, 1, smWidth,
                    new int[] { 0 });
        
        final ColorModel cm = buildColorModel(sampleModel);
        final BufferedImage realBandOriginal = new BufferedImage(cm, Raster
                .createWritableRaster(sampleModel, imgBufferReal, null), false,
                null);
        final BufferedImage imaginaryBandOriginal = new BufferedImage(cm, Raster
                .createWritableRaster(sampleModel, imgBufferImaginary, null),
                false, null);

        ImageLayout layout = new ImageLayout();
        layout.setTileGridXOffset(0).setTileGridYOffset(0).setTileHeight(2048)
                .setTileWidth(2048);
        RenderingHints rHints = null; 
        rHints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
        
        //TODO: Improve these operations.
        PlanarImage realBand=null;
        PlanarImage imaginaryBand=null;

        boolean doCrop = false;
        boolean doSubSampling = false;
        if (srcRegion != null) {
            
            //Transpose haven't been executed yet.
            //the actual image has x-y swapped.
            
            final int x = roi.y;
            final int y = roi.x;
            final int w = roi.height;
            final int h = roi.width;
            
            final ParameterBlockJAI pbCropReal = new ParameterBlockJAI("Crop");
            pbCropReal.addSource(realBandOriginal);
            pbCropReal.setParameter("x", Float.valueOf(x));
            pbCropReal.setParameter("y", Float.valueOf(y));
            pbCropReal.setParameter("width", Float.valueOf(w));
            pbCropReal.setParameter("height", Float.valueOf(h));
            
            RenderedOp cropReal = JAI.create("Crop", pbCropReal,
                   rHints);
            realBand = cropReal;

            final ParameterBlockJAI pbCropImaginary = new ParameterBlockJAI(
                    "Crop");
            pbCropImaginary.addSource(imaginaryBandOriginal);
            pbCropImaginary.setParameter("x", Float.valueOf(x));
            pbCropImaginary.setParameter("y", Float.valueOf(y));
            pbCropImaginary.setParameter("width", Float.valueOf(w));
            pbCropImaginary.setParameter("height", Float.valueOf(h));
            RenderedOp cropImaginary = JAI.create("Crop", pbCropImaginary,
                    rHints);
            imaginaryBand = cropImaginary;
            doCrop=true;
        } 

        if (xSubsamplingFactor != 1 || ySubsamplingFactor != 1) {
            if (!doCrop){
            realBand = PlanarImage.wrapRenderedImage(realBandOriginal);
            imaginaryBand = PlanarImage.wrapRenderedImage(imaginaryBandOriginal);
            }
            
            // Translating the child element to the proper location.
            final Raster translatedRasterReal = realBand.getData()
                    .createTranslatedChild(0, 0);

            // ////////////////////////////////////////////////////////////////////
            //
            // -------------------------------------------------------------------
            // Raster Creation >>> Step 3: Performing optional subSampling
            // -------------------------------------------------------------------
            //
            // ////////////////////////////////////////////////////////////////////
            final int dstW = dstHeight;
            final int dstH = dstWidth;
            
            final WritableRaster destRasterReal = Raster.createWritableRaster(
                    translatedRasterReal.getSampleModel()
                            .createCompatibleSampleModel(dstW, dstH),
                    new Point(0, 0));

            final int origRasterWidth = translatedRasterReal.getWidth();
            final int origRasterHeight = translatedRasterReal.getHeight();
            float data[] = null;
            for (int i = 0; i < origRasterHeight; i += ySubsamplingFactor)
                for (int j = 0; j < origRasterWidth; j += xSubsamplingFactor) {
                    data = translatedRasterReal.getPixel(j, i, data);
                    destRasterReal.setPixel(j / xSubsamplingFactor, i
                            / ySubsamplingFactor, data);
                }
            realBand = PlanarImage.wrapRenderedImage(new BufferedImage(cm,
                    destRasterReal, false, null));

            final Raster translatedRasterImaginary = imaginaryBand.getData()
                    .createTranslatedChild(0, 0);

            final WritableRaster destRasterImaginary = Raster.createWritableRaster(
                    translatedRasterImaginary.getSampleModel()
                            .createCompatibleSampleModel(dstW, dstH), new Point(0, 0));

            data = null;
            for (int i = 0; i < origRasterHeight; i += ySubsamplingFactor)
                for (int j = 0; j < origRasterWidth; j += xSubsamplingFactor) {
                    data = translatedRasterImaginary.getPixel(j, i, data);
                    destRasterImaginary.setPixel(j / xSubsamplingFactor, i
                            / ySubsamplingFactor, data);
                }
            imaginaryBand = PlanarImage.wrapRenderedImage(new BufferedImage(cm,
                    destRasterImaginary, false, null));
            doSubSampling=true;
        }

        // //
        //
        // Merging real and imaginary bands together for future
        // Magnitude computation.
        //
        // //
        final ParameterBlockJAI pbMerge = new ParameterBlockJAI("BandMerge");
        if (doSubSampling || doCrop){
            pbMerge.addSource(realBand);
            pbMerge.addSource(imaginaryBand);
        }
        else{
            pbMerge.addSource(realBandOriginal);
            pbMerge.addSource(imaginaryBandOriginal);
        }
        RenderedOp banded = JAI.create("BandMerge", pbMerge,rHints);

        // //
        //
        // Computing the magnitude of the stored complex values.
        //
        // //
        final ParameterBlockJAI pbMagnitude = new ParameterBlockJAI("Magnitude");
        pbMagnitude.addSource(banded);
        RenderedOp magnitude = JAI.create("Magnitude", pbMagnitude);

        // //
        //
        // Transposing the Matlab data matrix
        //
        // //
        RenderedOp beforeLogarithm = magnitude;
        
          if (channel == Channel.STARBOARD){
              final ParameterBlockJAI pbTranspose = new ParameterBlockJAI("Transpose");
              pbTranspose.addSource(magnitude);
              pbTranspose.setParameter("type", TransposeDescriptor.FLIP_DIAGONAL);
              beforeLogarithm = JAI.create("Transpose", pbTranspose);
              
              final ParameterBlockJAI pbFlippedH = new ParameterBlockJAI("Transpose");
              pbFlippedH.addSource(beforeLogarithm);
              pbFlippedH.setParameter("type", TransposeDescriptor.FLIP_VERTICAL);
              beforeLogarithm = JAI.create("Transpose", pbFlippedH);
          }
          else {
              final ParameterBlockJAI pbTranspose = new ParameterBlockJAI("Transpose");
              pbTranspose.addSource(magnitude);
              pbTranspose.setParameter("type", TransposeDescriptor.FLIP_ANTIDIAGONAL);
              beforeLogarithm = JAI.create("Transpose", pbTranspose);
          }
    
          if(COMPUTE_LOGARITHM){
            // //
            //
            // Computing the Natural Logarithm.
            //
            // //
            final ParameterBlockJAI pbLog = new ParameterBlockJAI("Log");
            pbLog.addSource(beforeLogarithm);
            RenderedOp logarithm = JAI.create("Log", pbLog);
    
            // //
            //
            // Applying a rescale to handle Decimal Logarithm.
            //
            // //
            final ParameterBlock pbRescale = new ParameterBlock();
            
            // Using logarithmic properties 
            final double scaleFactor = 20 / Math.log(10);
    
            final double[] scaleF = new double[] { scaleFactor };
            final double[] offsetF = new double[] { 0 };
    
            pbRescale.add(scaleF);
            pbRescale.add(offsetF);
            pbRescale.addSource(logarithm);
    
            RenderedOp rescale = JAI.create("Rescale", pbRescale);
    
            return rescale.getAsBufferedImage();
          }
          else
              return beforeLogarithm.getAsBufferedImage();
        }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex)
            throws IOException {
        initialize();
        final int width = sasTile.getXPixels();
        final int height = sasTile.getYPixels();
        final PixelInterleavedSampleModel sampleModel = new PixelInterleavedSampleModel(
                DataBuffer.TYPE_DOUBLE, width, height, 1, width,
                new int[] { 0 });

        final ColorModel cm = buildColorModel(sampleModel);
        final List<ImageTypeSpecifier> l = new java.util.ArrayList<ImageTypeSpecifier>(1);
        ImageTypeSpecifier imageType = new ImageTypeSpecifier(cm,sampleModel);
        l.add(imageType);
        return l.iterator();
    }
}
