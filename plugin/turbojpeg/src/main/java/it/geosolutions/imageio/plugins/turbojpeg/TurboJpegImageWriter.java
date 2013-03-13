/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2012, GeoSolutions
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
package it.geosolutions.imageio.plugins.turbojpeg;

import it.geosolutions.imageio.plugins.exif.EXIFMetadata;
import it.geosolutions.imageio.plugins.exif.EXIFUtilities;
import it.geosolutions.imageio.utilities.ImageOutputStreamAdapter2;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import com.sun.media.jai.opimage.CopyOpImage;
import org.libjpegturbo.turbojpeg.TJ;
import org.libjpegturbo.turbojpeg.TJCompressor;


/**
 * @author Daniele Romagnoli, GeoSolutions SaS
 * @author Simone Giannecchini, GeoSolutions SaS
 * @author Emanuele Tajariol, GeoSolutions SaS
 */
public class TurboJpegImageWriter extends ImageWriter
{

    /** The LOGGER for this class. */
    private static final Logger LOGGER = Logger.getLogger("it.geosolutions.imageio.plugins.turbojpeg");

    private ImageOutputStream outputStream = null;

    public TurboJpegImageWriter(ImageWriterSpi originatingProvider)
    {
        super(originatingProvider);
    }

    /**
     * Get a default {@link ImageWriteParam} instance.
     */
    @Override
    public ImageWriteParam getDefaultWriteParam()
    {
        TurboJpegImageWriteParam wparam = new TurboJpegImageWriteParam();
        wparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        wparam.setCompressionType(TurboJpegImageWriteParam.DEFAULT_COMPRESSION_SCHEME);
        wparam.setCompressionQuality(TurboJpegImageWriteParam.DEFAULT_COMPRESSION_QUALITY);
        return wparam;
    }

    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType,
        ImageWriteParam param)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param)
    {
    	throw new UnsupportedOperationException();
    }

    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param)
    {
        return null;
    }

    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param)
    {
        return null;
    }

    /**
     * Sets the destination to the given <code>Object</code>.
     * For this TurboJPEG specific implementation, it needs to be
     * an instance of  {@link ImageOutputStreamAdapter2}.
     *
     * @param output
     *            the <code>Object</code> to use for future writing.
     */
    public void setOutput(Object output) 
    {
        if (output instanceof OutputStream) {
            outputStream = new ImageOutputStreamAdapter2((OutputStream) output);
        } else if (output instanceof ImageOutputStreamAdapter2) {
            outputStream = (ImageOutputStreamAdapter2) output;
        } else if (output instanceof File){
            try {
                outputStream = new ImageOutputStreamAdapter2(new FileOutputStream((File) output));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        super.setOutput(output);
    }

    @Override
    public void write(IIOMetadata metadata, IIOImage image, ImageWriteParam writeParam) throws IOException
    {

        // Getting image properties
        RenderedImage srcImage = image.getRenderedImage();
        srcImage = refineImage(srcImage);

        final ComponentSampleModel sm = (ComponentSampleModel) srcImage.getSampleModel();
        int[] bandOffsets = sm.getBandOffsets();

        // Getting image Write params
        TurboJpegImageWriteParam param = (TurboJpegImageWriteParam) writeParam;
        // use default as needed
        if (param == null) {
            param = (TurboJpegImageWriteParam) getDefaultWriteParam();
        }
        final EXIFMetadata exif = param.getExif();
        int componentSampling = param.getComponentSubsampling();
        final int quality = (int) (param.getCompressionQuality() * 100);

        int pf = TJ.PF_RGB;
        if (bandOffsets.length == 3)
        {
            if (componentSampling == -1) {
                componentSampling = TurboJpegImageWriteParam.DEFAULT_RGB_COMPONENT_SUBSAMPLING;
            }
            if ((bandOffsets[0] == 2) && (bandOffsets[2] == 0))
            {
                pf = TJ.PF_BGR;
            }
        }
        else if (bandOffsets.length == 1 && componentSampling == -1)
        {
            pf = TJ.PF_GRAY;
            componentSampling = TJ.SAMP_GRAY;
        }
        else
        {
            throw new IllegalArgumentException("TurboJPEG won't work with this type of sampleModel");
        }

        if (componentSampling < 0)
        {
            throw new IOException("Subsampling level not set");
        }
        
        final int pixelsize = sm.getPixelStride();
        final int width = srcImage.getWidth();
        final int height = srcImage.getHeight();
        final int pitch = pixelsize * width;
        
        TJCompressor compressor = null;
        try
        {
//            final long jsize = TurboJpegUtilities.bufSize(width, height);

            Rectangle rect = new Rectangle(srcImage.getMinX(), srcImage.getMinY(), srcImage.getWidth(), srcImage.getHeight());
            Raster data = srcImage.getData(rect);
            final byte[] inputImageData = ((DataBufferByte) data.getDataBuffer()).getData();
            
            final byte[] outputImageData;
            try {
                compressor = new TJCompressor();
                compressor.setSourceImage(inputImageData, width, pitch, height, pf);
                compressor.setJPEGQuality(quality);
                compressor.setSubsamp(componentSampling);
                
                outputImageData = compressor.compress(TJ.FLAG_FASTDCT);                
            } catch (Exception ex) {
                throw new IOException("Error in turbojpeg comressor: " + ex.getMessage(), ex);
            }            
            
            final int imageDataSize = compressor.getCompressedSize();            
            
            if (exif != null)
            {
                EXIFUtilities.insertEXIFintoStream(
                        ((ImageOutputStreamAdapter2) outputStream).getOs(), outputImageData, imageDataSize, exif);
            }
            else
            {
                outputStream.write(outputImageData, 0, imageDataSize);
            }
        }

        finally
        {
            if(compressor != null) {
                try
                {
                    compressor.close();
                }
                catch (Exception t)
                {
                    LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
                }
            }
        }
    }

    /**
     * Performs a few check in order to make sure to provide the proper data bytes to the
     * incoming encoding phase. When calling getData(Rectangle).getDataBuffer() on an image having size
     * smaller than the tile size, the underlying data buffer will be made of the data contained in the
     * full tile (Even having used the getData(Rectangle) call. As an instance, a Rectangle(0,0,64,64)
     * extracted from a 64x64 image with tiling 128x128 will result into a ByteBuffer filled with
     * 128x128xBands bytes. (Therefore a lot of zeros). The encoded image will have a lot of scattered
     * black stripes.
     * This method do a copy of the only needed part of data when such a condition is met, or return the
     * original image otherwise.
     *
     * @param srcImage The source image to be refined.
     * @return
     */
    private RenderedImage refineImage(RenderedImage srcImage)
    {
        final int w = srcImage.getWidth();
        final int h = srcImage.getHeight();
        final int minX = srcImage.getMinX();
        final int minY = srcImage.getMinY();
        final int tw = srcImage.getTileWidth();
        final int th = srcImage.getTileHeight();
        
        // Checking if the tiling size is bigger than the actual image size,
        // in that case we need to do something like a crop by copying a 
        // portion of the original data 
        if ((tw > w) || (th > h))
        {
            RenderingHints hints = null;
            ImageLayout layout = null;
            if (srcImage instanceof RenderedOp)
            {
                hints = ((RenderedOp) srcImage).getRenderingHints();
                if ((hints != null) && hints.containsKey(JAI.KEY_IMAGE_LAYOUT))
                {
                    layout = (ImageLayout) hints.get(JAI.KEY_IMAGE_LAYOUT);
                }
                else
                {
                    layout = new ImageLayout(srcImage);
                }
            }
            else
            {
                layout = new ImageLayout(srcImage);
                hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
            }
            
            // Imposing the layout of the requested image
            // as well as reducing the tile layout which was 
            // bigger than the image
            layout.setTileHeight(h);
            layout.setTileWidth(w);
            layout.setTileGridXOffset(minX);
            layout.setTileGridYOffset(minY);
            layout.setMinX(minX);
            layout.setMinY(minY);
            layout.setWidth(w);
            layout.setHeight(h);
            srcImage = new CopyOpImage(srcImage, hints, layout);
        }
        return srcImage;
    }

}
