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
package it.geosolutions.imageio.plugins.png;

import it.geosolutions.imageio.stream.output.ImageOutputStreamAdapter;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;

import ar.com.hjg.pngj.FilterType;

/**
 * {@link ImageWriter} implementation for the high performance PNG encoder.
 * 
 * @author Simone Giannecchini, GeoSolutions SaS
 */
public class PNGImageWriter extends ImageWriter {
    /**
     * This differs from the core PNG ImageWriteParam in that:
     *
     * . 'canWriteCompressed' is set to 'true' so that canWriteCompressed()
     * will return 'true'.
     * . compression types are: "DEFAULT", "FILTERED", and "HUFFMAN_ONLY"
     * and are used to set the encoder strategy to Z_DEFAULT, Z_FILTERED,
     * and Z_HUFFMAN_ONLY as described in the PNG specification.
     * . compression modes are: MODE_DEFAULT, MODE_EXPLICIT ; MODE_DISABLED is not allowed.
     * . compression quality is used to set the compression level of the
     * encoder according to:
     *
     *     compressionLevel = (Round)(9*(1.0F - compressionQuality))
     *
     */
    static public class PNGImageWriteParam extends ImageWriteParam {


        static final FilterType DEFAULT_FILTER_TYPE = FilterType.FILTER_DEFAULT;
    
        static final float DEFAULT_COMPRESSION_QUALITY = 1.0F/3.0F;
    
    
        // Compression descriptions
        private static final String[] compressionQualityDescriptions =
            new String[] {
                "Best Compression",
                "Best Speed",
                "No Compression"
            };


        private FilterType filterType;

        PNGImageWriteParam() {
    
            canWriteCompressed = true;
            canWriteProgressive = true;
    
            compressionQuality = DEFAULT_COMPRESSION_QUALITY;
            filterType    = DEFAULT_FILTER_TYPE;
        }
    
        public FilterType getFilterType() {
            return filterType;
        }
        
        public void setFilterType(FilterType filterType) {
            this.filterType = filterType;
        }
    
        public String[] getCompressionQualityDescriptions() {
            super.getCompressionQualityDescriptions(); // Performs checks.
    
            return compressionQualityDescriptions;
        }
    
        public float[] getCompressionQualityValues() {
            super.getCompressionQualityValues(); // Performs checks.
    
            // According to the java.util.zip.Deflater class, the Deflater
            // level 1 gives the best speed (short of no compression). Since
            // quality is derived from level as
            //
            //     quality = 1 - level/9
            //
            // this gives a value of 8.0/9.0 for the corresponding quality.
            return new float[] { 0.0F,               // "Best Compression"
                                 (float)(8.0F/9.0F), // "Best Speed"
                                 1.0F };             // "No Compression"
        }
    
        public void setCompressionMode(int mode) {
            if(mode == MODE_DISABLED) {
                throw new UnsupportedOperationException("mode == MODE_DISABLED");
            }
    
            super.setCompressionMode(mode); // This sets the instance variable.
        }
    
        public void unsetCompression() {
            super.unsetCompression(); // Performs checks.
    
            compressionQuality = DEFAULT_COMPRESSION_QUALITY;
            filterType=FilterType.FILTER_DEFAULT;
        }
    }
    /** The LOGGER for this class. */
    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.png");
    
    private OutputStream outputStream;
    
    public PNGImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }
    
    /**
     * Get a default {@link ImageWriteParam} instance.
     */
    @Override
    public ImageWriteParam getDefaultWriteParam() {
        PNGImageWriteParam wparam = new PNGImageWriteParam();
        wparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        wparam.setFilterType(FilterType.FILTER_DEFAULT);
        wparam.setCompressionQuality(PNGImageWriteParam.DEFAULT_COMPRESSION_QUALITY);
        return wparam;
    }
    
    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData,
            ImageTypeSpecifier imageType, ImageWriteParam param) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData,
            ImageWriteParam param) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType,
            ImageWriteParam param) {
        return null;
    }
    
    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        return null;
    }
    
    /**
     * Sets the destination to the given <code>Object</code>. For this TurboJPEG
     * specific implementation, it needs to be an instance of
     * {@link ImageOutputStreamAdapter2}.
     *
     * @param output the <code>Object</code> to use for future writing.
     */
    public void setOutput(Object output) {
        if (output instanceof OutputStream) {
            outputStream = (OutputStream) output;
        } else if (output instanceof ImageOutputStreamAdapter) {
            outputStream = ((ImageOutputStreamAdapter) output).getTarget();
        } else if (output instanceof File) {
            try {
                outputStream = new FileOutputStream(
                        (File) output);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        super.setOutput(output);
    }
    
    @Override
    public void write(IIOMetadata metadata, IIOImage image,
            ImageWriteParam writeParam) throws IOException {
    
        // Getting image properties
        RenderedImage srcImage = image.getRenderedImage();

    
        // Getting image Write params
        float quality=PNGImageWriteParam.DEFAULT_COMPRESSION_QUALITY;
        FilterType filter=PNGImageWriteParam.DEFAULT_FILTER_TYPE;
        if(writeParam!=null){
            PNGImageWriteParam param = (PNGImageWriteParam) writeParam;
            quality=param.getCompressionQuality();
            filter=param.getFilterType();
        }
    
        // actual write
        try{
            new PNGWriter().writePNG(srcImage, outputStream, quality, filter);
        } catch (Exception e){
            throw new IOException(e);
        } finally{
            if(outputStream!=null){
                try{
                    outputStream.close();
                }catch(Throwable t){
                    // swallow
                }
            }
        }
    }
}
