/* Copyright (c) 2013 OpenPlans - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package it.geosolutions.imageio.plugins.png;

import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

/**
 * Base class providing common traits to all scanline providers
 * 
 * @author Andrea Aime - GeoSolutions
 */
public abstract class AbstractScanlineProvider implements ScanlineProvider {

    protected final int width;
    
    protected final int height;
    
    protected final int scanlineLength;

    protected final ScanlineCursor cursor;

    protected final IndexColorModel palette;
    
    protected final byte bitDepth;
    
    protected int currentRow = 0;

    public AbstractScanlineProvider(Raster raster, int bitDepth, int scanlineLength) {
        this(raster, (byte) bitDepth, scanlineLength, null);
    }
    
    public AbstractScanlineProvider(Raster raster, int bitDepth, int scanlineLength, IndexColorModel palette) {
        this(raster, (byte) bitDepth, scanlineLength, palette);
    }
    
    protected AbstractScanlineProvider(Raster raster, byte bitDepth, int scanlineLength, IndexColorModel palette) {
        this.width = raster.getWidth();
        this.height = raster.getHeight();
        this.bitDepth = bitDepth;
        this.palette = palette;
        this.cursor = new ScanlineCursor(raster);
        this.scanlineLength = scanlineLength;
    }

    
    public final int getWidth() {
        return width;
    }

    
    public final int getHeight() {
        return height;
    }

    
    public final byte getBitDepth() {
        return bitDepth;
    }

    
    public final IndexColorModel getPalette() {
        return palette;
    }

    
    public final int getScanlineLength() {
        return scanlineLength;
    }
    
    public void readFromPngRaw(byte[] raw, int len, int offset, int step) {
        throw new UnsupportedOperationException("This bridge works write only");

    }

    public void endReadFromPngRaw() {
        throw new UnsupportedOperationException("This bridge works write only");
    }

    public void writeToPngRaw(byte[] raw) {
        // PNGJ stores in the first byte the filter type
        this.next(raw, 1, raw.length - 1);
    }


}
