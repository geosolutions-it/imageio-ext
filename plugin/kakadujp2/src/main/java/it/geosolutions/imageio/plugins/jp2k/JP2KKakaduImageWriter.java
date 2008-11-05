/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2008, GeoSolutions
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

import it.geosolutions.imageio.stream.output.FileImageOutputStreamExt;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.media.jai.PlanarImage;

import kdu_jni.Jp2_colour;
import kdu_jni.Jp2_dimensions;
import kdu_jni.Jp2_family_tgt;
import kdu_jni.Jp2_target;
import kdu_jni.KduException;
import kdu_jni.Kdu_codestream;
import kdu_jni.Kdu_compressed_target;
import kdu_jni.Kdu_simple_file_target;
import kdu_jni.Kdu_stripe_compressor;
import kdu_jni.Siz_params;

public class JP2KKakaduImageWriter extends ImageWriter {

    private final static int MAX_BUFFER_SIZE = 16 * 1024 * 1024;

    private final static int MIN_BUFFER_SIZE = 1024 * 1024;

    /**
     * In case the ratio between the stripe_height and the image height is
     * greater than this value, set the stripe_height to the image height in
     * order to do a single push
     */
    private static final double SINGLE_PUSH_THRESHOLD_RATIO = 0.90;

    private File outputFile;

    public JP2KKakaduImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData,
            ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData,
            ImageWriteParam param) {
        return null;
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
     * Sets the destination to the given <code>Object</code>, usually a
     * <code>File</code> or a {@link FileImageOutputStreamExt}.
     * 
     * @param output
     *                the <code>Object</code> to use for future writing.
     */
    public void setOutput(Object output) {
        super.setOutput(output); // validates output
        if (output instanceof File)
            outputFile = (File) output;
        else if (output instanceof FileImageOutputStreamExt)
            outputFile = ((FileImageOutputStreamExt) output).getFile();
        else if (output instanceof URL) {
            final URL tempURL = (URL) output;
            if (tempURL.getProtocol().equalsIgnoreCase("file")) {
                try {
                    outputFile = new File(URLDecoder.decode(tempURL.getFile(),
                            "UTF-8"));

                } catch (IOException e) {
                    throw new RuntimeException("Not a Valid Input", e);
                }
            }
        }
    }

    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image,
            ImageWriteParam param) throws IOException {

        final String fileName = outputFile.getAbsolutePath();
        if (param == null)
            param = getDefaultWriteParam();

        final PlanarImage inputRenderedImage = PlanarImage
                .wrapRenderedImage(image.getRenderedImage());

        // ////////////////////////////////////////////////////////////////////
        //
        // Image properties initialization
        //
        // ////////////////////////////////////////////////////////////////////
        final int sourceWidth = inputRenderedImage.getWidth();
        final int sourceHeight = inputRenderedImage.getHeight();
        final int sourceMinX = inputRenderedImage.getMinX();
        final int sourceMinY = inputRenderedImage.getMinY();
        final int dataType = inputRenderedImage.getSampleModel().getDataType();
        final ColorModel cm = inputRenderedImage.getColorModel();

        final int[] cmComponentBits = cm.getComponentSize();
        final int componentBits = cmComponentBits[0];

        int dataTypeSize = 8;
        switch (dataType) {
        case DataBuffer.TYPE_BYTE:
            dataTypeSize = 8;
            break;
        case DataBuffer.TYPE_USHORT:
            dataTypeSize = 16;
            break;
        case DataBuffer.TYPE_SHORT:
            dataTypeSize = 16;
            break;
        case DataBuffer.TYPE_INT:
            dataTypeSize = 32;
            break;
        }

        final int nComponents = inputRenderedImage.getNumBands();

        // NOTE: We assume all bands share the same bitdepth.
        final int bitDepth = inputRenderedImage.getSampleModel().getSampleSize(
                0);

        JP2KKakaduImageWriteParam jp2Kparam;
        final boolean writeCodeStreamOnly;
        final double quality;
        if (param instanceof JP2KKakaduImageWriteParam) {
            jp2Kparam = (JP2KKakaduImageWriteParam) param;
            writeCodeStreamOnly = jp2Kparam.isWriteCodeStreamOnly();
            quality = jp2Kparam.getQuality();
        } else {
            writeCodeStreamOnly = true;
            quality = 1;
        }

        // //
        //
        // Setting regions and sizes and retrieving parameters
        //
        // //
        final int xSubsamplingFactor = param.getSourceXSubsampling();
        final int ySubsamplingFactor = param.getSourceYSubsampling();
        final Rectangle imageBounds = new Rectangle(sourceMinX, sourceMinY,
                sourceWidth, sourceHeight);
        final Dimension destSize = new Dimension();
        computeRegions(imageBounds, destSize, param);

        // Destination sizes
        final int destinationWidth = destSize.width;
        final int destinationHeight = destSize.height;
        final int rowSize = (destinationWidth * nComponents);
        final int imageSize = rowSize * destinationHeight * bitDepth/8;
        final long qualityLayersSize = (long) (imageSize * quality);

        // ////////////////////////////////////////////////////////////////////
        //
        // Kakadu objects initialization
        //
        // ////////////////////////////////////////////////////////////////////
        Kdu_compressed_target outputTarget = null;
        Jp2_target target = null;
        Jp2_family_tgt familyTarget = null;

        try {
            if (writeCodeStreamOnly) {
                outputTarget = new Kdu_simple_file_target();
                ((Kdu_simple_file_target) outputTarget).Open(fileName);
            } else {
                familyTarget = new Jp2_family_tgt();
                familyTarget.Open(fileName);
                target = new Jp2_target();
                target.Open(familyTarget);
            }

            Kdu_codestream codeStream = new Kdu_codestream();
            Siz_params params = new Siz_params();
            initParams(params, destinationWidth, destinationHeight,
                    componentBits, nComponents);

            if (writeCodeStreamOnly)
                codeStream.Create(params, outputTarget, null);
            else
                codeStream.Create(params, target, null);

            params = codeStream.Access_siz();
            params.Parse_string("Creversible=yes");
            params.Parse_string("Cycc=yes");
            params.Parse_string("Clevels=5");
//            params.Parse_string("Corder=PCRL");
//            params.Parse_string("Qguard=2");
            

            final int qualityLayers = 2;
            // TODO: Test
            params.Parse_string("Clayers=" + qualityLayers);

            if (!writeCodeStreamOnly) {
                Jp2_dimensions dims = target.Access_dimensions();
                dims.Init(params);

                Jp2_colour colour = target.Access_colour();
                final int cs = cm.getColorSpace().getType();
                if (cs == ColorSpace.TYPE_RGB) {
                    colour.Init(kdu_jni.Kdu_global.JP2_sRGB_SPACE);
                } else if (cs == ColorSpace.TYPE_GRAY) {
                    colour.Init(kdu_jni.Kdu_global.JP2_sLUM_SPACE);
                }

                target.Write_header();
                target.Open_codestream();
            }

//            params.Finalize_all();

            // //
            //
            // Setting parameters for stripe compression
            //
            // //

            Kdu_stripe_compressor compressor = new Kdu_stripe_compressor();

            // Array with one entry for each image component, identifying the
            // number of lines supplied for that component in the present call.
            // All entries must be non-negative.
            final int[] stripeHeights = new int[nComponents];

            // Array with one entry for each image component, identifying
            // the separation between horizontally adjacent samples within the
            // corresponding stripe buffer found in the stripe_bufs array.
            final int[] sampleGaps = new int[nComponents];

            // Array with one entry for each image component, identifying
            // the separation between vertically adjacent samples within the
            // corresponding stripe buffer found in the stripe_bufs array.
            final int[] rowGaps = new int[nComponents];

            // Array with one entry for each image component, identifying the
            // position of the first sample of that component within the buffer
            // array.
            final int[] sampleOffsets = new int[nComponents];

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
            final int precisions[] = new int[nComponents];
            
            final long[] qualityLayerSizes = new long[qualityLayers];
            final long[] cumulativeQualityLayerSizes = new long[qualityLayers];
            
            final int[] multipliers = new int[qualityLayers];
            
            int multi = 1;
            int totals = 0;
            for (int i=0;i<qualityLayers;i++){
                multi = i!=0? multi*2 : multi;
                totals+= multi;
                multipliers[i]= multi;
            }
            double qualityStep = Math.floor((double)qualityLayersSize)/((double)totals);
            for (int i=0;i<qualityLayers;i++){
                long step = i!=0?qualityLayerSizes[i-1]:0;
                qualityLayerSizes[i]=(long)Math.floor(qualityStep*multipliers[i]);
                cumulativeQualityLayerSizes[i]=qualityLayerSizes[i]+step;
            }

            int maxStripeHeight = MAX_BUFFER_SIZE / (rowSize);
            if (maxStripeHeight > destinationHeight)
                maxStripeHeight = destinationHeight;
            else {

                // In case the computed stripeHeight is near to the
                // destination height, I will avoid multiple calls by
                // doing a single push.
                double ratio = (double) maxStripeHeight
                        / (double) destinationHeight;
                if (ratio > SINGLE_PUSH_THRESHOLD_RATIO)
                    maxStripeHeight = destinationHeight;

            }

            int minStripeHeight = MIN_BUFFER_SIZE / (rowSize);
            if (minStripeHeight<1){
                minStripeHeight=1;
            }
            

            for (int component = 0; component < nComponents; component++) {
                stripeHeights[component] = maxStripeHeight;
                sampleGaps[component] = nComponents;
                rowGaps[component] = destinationWidth * nComponents;
                sampleOffsets[component] = component;
                precisions[component] = cmComponentBits[component];
            }

            // ////////////////////////////////////////////////////////////////
            //
            // Pushing stripes
            //
            // ////////////////////////////////////////////////////////////////

            compressor.Start(codeStream, qualityLayers, qualityLayerSizes,
                    null, 0, false, false, true, 0, nComponents, false);
            boolean useRecommendations = compressor
                    .Get_recommended_stripe_heights(minStripeHeight, 1024,
                            stripeHeights, null);
            if (!useRecommendations) {
                for (int i = 0; i < nComponents; i++)
                    stripeHeights[i] = maxStripeHeight;
            }
            boolean goOn = true;
            int stripeSize = rowSize * stripeHeights[0];
            int stripeBytes = 0;

            // //
            //
            // Byte Buffer
            //
            // //
            if (bitDepth <= 8) {
                byte[] bufferValues = new byte[stripeSize];
                int y = 0;
                while (goOn) {
                    if (sourceHeight - y < stripeHeights[0]) {
                        for (int i = 0; i < nComponents; i++)
                            stripeHeights[i] = sourceHeight - y;
                        stripeSize = rowSize * stripeHeights[0];
                        bufferValues = new byte[stripeSize];

                    }
                    final Rectangle rect = new Rectangle(0, y,
                            destinationWidth, stripeHeights[0]);
                    Raster rasterData = inputRenderedImage.getData(rect);
                    rasterData.getDataElements(0, y, destinationWidth,
                            stripeHeights[0], bufferValues);
                    goOn = compressor.Push_stripe(bufferValues, stripeHeights,
                            sampleOffsets, sampleGaps, rowGaps, precisions, 0);
                    y += stripeHeights[0];
                    stripeBytes += stripeSize;
                }
            } else if (bitDepth > 8 && bitDepth <= 16) {
                // //
                //
                // Short Buffer
                //
                // //
                final boolean isGlobalSigned = dataType == DataBuffer.TYPE_USHORT;
                final boolean[] isSigned = new boolean[nComponents];
                for (int i = 0; i < isSigned.length; i++)
                    isSigned[i] = isGlobalSigned;
                short[] bufferValues = new short[stripeSize];
                int y = 0;
                while (goOn) {
                    if (sourceHeight - y < stripeHeights[0]) {
                        for (int i = 0; i < nComponents; i++)
                            stripeHeights[i] = sourceHeight - y;
                        stripeSize = rowSize * stripeHeights[0];
                        bufferValues = new short[stripeSize];
                    }

                    final Rectangle rect = new Rectangle(0, y,
                            destinationWidth, stripeHeights[0]);
                    final Raster rasterData = inputRenderedImage.getData(rect);
                    rasterData.getDataElements(0, y, destinationWidth,
                            stripeHeights[0], bufferValues);
                    goOn = compressor.Push_stripe(bufferValues, stripeHeights,
                            sampleOffsets, sampleGaps, rowGaps, precisions,
                            isSigned, 0);
                    y += stripeHeights[0];
                    stripeBytes += stripeSize;
                }

            } else if (bitDepth > 16 && bitDepth <= 32) {
                // //
                //
                // Int Buffer
                //
                // //
                int[] bufferValues = new int[stripeSize];
                int y = 0;
                while (goOn) {
                    if (sourceHeight - y < stripeHeights[0]) {
                        for (int i = 0; i < nComponents; i++)
                            stripeHeights[i] = sourceHeight - y;

                        stripeSize = rowSize * stripeHeights[0];
                        bufferValues = new int[stripeSize];

                    }
                    final Rectangle rect = new Rectangle(0, y,
                            destinationWidth, stripeHeights[0]);
                    final Raster rasterData = inputRenderedImage.getData(rect);
                    rasterData.getDataElements(0, y, destinationWidth,
                            stripeHeights[0], bufferValues);
                    goOn = compressor.Push_stripe(bufferValues, stripeHeights,
                            sampleOffsets, sampleGaps, rowGaps, precisions);
                    y += stripeHeights[0];
                    stripeBytes += stripeSize;
                }
            }

            // ////////////////////////////////////////////////////////////////
            //
            // Kakadu Objects Finalization
            //
            // ////////////////////////////////////////////////////////////////
            compressor.Finish();
            compressor.Native_destroy();
            codeStream.Destroy();

            if (writeCodeStreamOnly) {
                outputTarget.Close();
                outputTarget.Native_destroy();
            } else {
                target.Close();
                target.Native_destroy();
                familyTarget.Close();
                familyTarget.Native_destroy();
            }

        } catch (KduException e) {
            throw new RuntimeException(
                    "Error caused by a Kakadu exception during write operation",
                    e);
        }
    }

    private void initParams(Siz_params params, final int destinationWidth,
            final int destinationHeight, final int precision,
            final int components) throws KduException {
        params.Set("Ssize", 0, 0, destinationHeight);
        params.Set("Ssize", 0, 1, destinationWidth);
        params.Set("Sprofile", 0, 0, 2);
        params.Set("Sorigin", 0, 0, 0);
        params.Set("Sorigin", 0, 1, 0);
        params.Set("Scomponents", 0, 0, components);
        params.Set("Sprecision", 0, 0, precision);
        params.Set("Sdims", 0, 0, destinationHeight);
        params.Set("Sdims", 0, 1, destinationWidth);
        params.Set("Ssigned", 0, 0, false);
        params.Finalize();
    }

    /**
     * Compute the source region and destination dimensions taking any parameter
     * settings into account.
     */
    private static void computeRegions(Rectangle sourceBounds,
            Dimension destSize, ImageWriteParam p) {
        int periodX = 1;
        int periodY = 1;
        if (p != null) {
            int[] sourceBands = p.getSourceBands();
            if (sourceBands != null
                    && (sourceBands.length != 1 || sourceBands[0] != 0)) {
                throw new IllegalArgumentException("Cannot sub-band image!");
            }

            // ////////////////////////////////////////////////////////////////
            //
            // Get source region and subsampling settings
            //
            // ////////////////////////////////////////////////////////////////
            Rectangle sourceRegion = p.getSourceRegion();
            if (sourceRegion != null) {
                // Clip to actual image bounds
                sourceRegion = sourceRegion.intersection(sourceBounds);
                sourceBounds.setBounds(sourceRegion);
            }

            // Get subsampling factors
            periodX = p.getSourceXSubsampling();
            periodY = p.getSourceYSubsampling();

            // Adjust for subsampling offsets
            int gridX = p.getSubsamplingXOffset();
            int gridY = p.getSubsamplingYOffset();
            sourceBounds.x += gridX;
            sourceBounds.y += gridY;
            sourceBounds.width -= gridX;
            sourceBounds.height -= gridY;
        }

        // ////////////////////////////////////////////////////////////////////
        //
        // Compute output dimensions
        //
        // ////////////////////////////////////////////////////////////////////
        destSize.setSize((sourceBounds.width + periodX - 1) / periodX,
                (sourceBounds.height + periodY - 1) / periodY);
        if (destSize.width <= 0 || destSize.height <= 0) {
            throw new IllegalArgumentException("Empty source region!");
        }
    }

}
