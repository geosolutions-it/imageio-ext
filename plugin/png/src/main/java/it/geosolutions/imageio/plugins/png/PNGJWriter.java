/* Copyright (c) 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package it.geosolutions.imageio.plugins.png;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import ar.com.hjg.pngj.FilterType;
import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.PngChunkPLTE;
import ar.com.hjg.pngj.chunks.PngChunkTRNS;

/**
 * Encodes the image in PNG using the PNGJ library
 * 
 * @author Andrea Aime - GeoSolutions
 */
public class PNGJWriter {

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    public RenderedImage writePNG(RenderedImage image, OutputStream outStream, float quality,
            FilterType filterType) throws Exception {
        
        // compute the compression level similarly to what the Clib code does
        int level = Math.round(9 * (1f - quality));
        // get the optimal scanline provider for this image
        RenderedImage original = image;
        ScanlineProvider scanlines = ScanlineProviderFactory.getProvider(image);
        if (scanlines == null) {
            throw new IllegalArgumentException("Could not find a scanline extractor for "
                    + original);
        }

        // encode using the PNGJ library and the GeoServer own scanline providers
        ColorModel colorModel = image.getColorModel();
        boolean indexed = colorModel instanceof IndexColorModel;
        ImageInfo ii = getImageInfo(image, scanlines, colorModel, indexed);
        PngWriter pw = new PngWriter(outStream, ii);
        pw.setShouldCloseStream(false);
        try {
            pw.setCompLevel(level);
            pw.setFilterType(filterType);

            if (indexed) {
                IndexColorModel icm = (IndexColorModel) colorModel;
                PngChunkPLTE palette = pw.getMetadata().createPLTEChunk();
                int ncolors = icm.getMapSize();
                palette.setNentries(ncolors);
                for (int i = 0; i < ncolors; i++) {
                    final int red = icm.getRed(i);
                    final int green = icm.getGreen(i);
                    final int blue = icm.getBlue(i);
                    palette.setEntry(i, red, green, blue);
                }
                if (icm.hasAlpha()) {
                    PngChunkTRNS transparent = new PngChunkTRNS(ii);
                    int[] alpha = new int[ncolors];
                    for (int i = 0; i < ncolors; i++) {
                        final int a = icm.getAlpha(i);
                        alpha[i] = a;
                    }
                    transparent.setPalletteAlpha(alpha);
                    pw.getChunksList().queue(transparent);

                }
            }

            // write out the actual image lines
            for (int row = 0; row < image.getHeight(); row++) {
                pw.writeRow(scanlines);
            }
            pw.end();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to encode the PNG", e);
            throw e;
        } finally {
            pw.close();
        }

        return image;
    }

    /**
     * Quick method used for checking if the image can be optimized with the selected scanline extractors or if the image must be rescaled to byte
     * before writing the image.
     * 
     * @param image
     * @return
     */
    public boolean isScanlineSupported(RenderedImage image) {
        ScanlineProvider scanlines = ScanlineProviderFactory.getProvider(image);
        return scanlines != null;
    }

    private ImageInfo getImageInfo(RenderedImage image, ScanlineProvider scanlines,
            ColorModel colorModel, boolean indexed) {
        int numColorComponents = colorModel.getNumColorComponents();
        boolean grayscale = !indexed && numColorComponents < 3;
        byte bitDepth = scanlines.getBitDepth();
        boolean hasAlpha = !indexed && colorModel.hasAlpha();
        ImageInfo ii = new ImageInfo(image.getWidth(), image.getHeight(), bitDepth, hasAlpha,
                grayscale, indexed);
        return ii;
    }
}
