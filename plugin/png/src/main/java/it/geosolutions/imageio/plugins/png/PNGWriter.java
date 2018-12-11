/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2014, GeoSolutions
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

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import ar.com.hjg.pngj.FilterType;
import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.ChunksListForWrite;
import ar.com.hjg.pngj.chunks.PngChunkPLTE;
import ar.com.hjg.pngj.chunks.PngChunkTRNS;
import ar.com.hjg.pngj.chunks.PngMetadata;

/**
 * Encodes the image in PNG using the PNGJ library
 * 
 * @author Andrea Aime - GeoSolutions
 */
public class PNGWriter {

    private static final Logger LOGGER = Logger.getAnonymousLogger();

    public RenderedImage writePNG(RenderedImage image, OutputStream outStream, float quality,
            FilterType filterType) throws Exception {
        return writePNG(image, outStream, quality, filterType, null);
    }

    public RenderedImage writePNG(RenderedImage image, OutputStream outStream, float quality,
            FilterType filterType, Map<String,String> text) throws Exception {
        
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
            ChunksListForWrite chunkList = pw.getChunksList();
            PngMetadata metadata = pw.getMetadata();
            if (indexed) {
                IndexColorModel icm = (IndexColorModel) colorModel;
                PngChunkPLTE palette = metadata.createPLTEChunk();
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
                    chunkList.queue(transparent);

                }
            }
            if (text != null && !text.isEmpty()) {
                Iterator<Entry<String, String>> entrySetIterator = text.entrySet().iterator();
                while (entrySetIterator.hasNext()) {
                    Entry<String, String> entrySet = entrySetIterator.next();
                    metadata.setText(entrySet.getKey(), entrySet.getValue(), true, false);
                }
            }

            // write out the actual image lines
            for (int row = 0; row < image.getHeight(); row++) {
                pw.writeRow(scanlines);
            }
            pw.end();
        } catch (Exception e) {
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
