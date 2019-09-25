/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2019, GeoSolutions
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
package it.geosolutions.imageioimpl.plugins.cog;

import it.geosolutions.imageio.plugins.cog.CogImageReadParam;
import it.geosolutions.imageio.plugins.tiff.BaselineTIFFTagSet;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageWriter;

import javax.imageio.ImageReadParam;
import javax.imageio.spi.ImageReaderSpi;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author joshfix
 * Created on 2019-08-22
 */
public class CogImageReader extends TIFFImageReader {

    private final static Logger LOGGER = Logger.getLogger(CogImageReader.class.getName());

    public CogImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        // if the image input stream isn't a DefaultCogImageInputStream, skip all this nonsense and just use the original code
        if (!(stream instanceof DefaultCogImageInputStream)) {
            return super.read(imageIndex, param);
        }

        // the input stream needs to have either been initialized with a RangeReader implementation or the RangeReader
        // implementation class needs to have been provided in a CogImageReadParam
        if (!((DefaultCogImageInputStream)stream).isInitialized() && param instanceof CogImageReadParam) {
            ((DefaultCogImageInputStream) stream).init((CogImageReadParam) param);
        }

        if (!((DefaultCogImageInputStream)stream).isInitialized()) {
            throw new IOException("The DefaultCogImageInputStream has not been initialized.  Either pass a RangeReader"
                    + " implementation to the DefaultCogImageInputStream via the constructor or init method, or declare"
                    + " a valid RangeReader implementation class in the CogImageReadParam.");
        }

        LOGGER.fine("Reading pixels at offset (" + param.getSourceRegion().getX() + ", "
                + param.getSourceRegion().getY() + ") with a width of " + param.getSourceRegion().getWidth()
                + "px and height of " + param.getSourceRegion().getHeight() + "px");

        // prepare for reading
        prepareRead(imageIndex, param);
        this.theImage = getDestination(param, getImageTypes(imageIndex), width, height, noData);

        // This could probably be made more efficient...
        Rectangle srcRegion = new Rectangle(0, 0, 0, 0);
        Rectangle destRegion = new Rectangle(0, 0, 0, 0);

        computeRegions(imageReadParam, width, height, theImage, srcRegion, destRegion);
        tilesAcross = (width + tileOrStripWidth - 1) / tileOrStripWidth;
        tilesDown = (height + tileOrStripHeight - 1) / tileOrStripHeight;

        // Compute bounds on the tile indices for this source region.
        int minTileX = TIFFImageWriter.XToTileX(srcRegion.x, 0, tileOrStripWidth);
        int minTileY = TIFFImageWriter.YToTileY(srcRegion.y, 0, tileOrStripHeight);
        int maxTileX = TIFFImageWriter.XToTileX(srcRegion.x + srcRegion.width - 1, 0, tileOrStripWidth);
        int maxTileY = TIFFImageWriter.YToTileY(srcRegion.y + srcRegion.height - 1, 0, tileOrStripHeight);

        LOGGER.fine("Reading tiles (" + minTileX + "," + minTileY + ") - (" + maxTileX + "," + maxTileY + ")");

        CogTileInfo cogTileInfo = ((DefaultCogImageInputStream)stream).getCogTileInfo();

        // loops through each requested tile and complies information about each tile offset and byte length
        if (planarConfiguration == BaselineTIFFTagSet.PLANAR_CONFIGURATION_PLANAR) {
            for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
                for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
                    for (int band = 0; band < numBands; band++) {
                        int tileIndex = band * tilesAcross * tilesDown;
                        long offset = getTileOrStripOffset(tileIndex);
                        long byteLength = getTileOrStripByteCount(tileIndex);
                        cogTileInfo.addTileRange(tileIndex, offset, byteLength);
                    }
                }
            }
        } else {
            for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
                for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
                    int tileIndex = tileY * tilesAcross + tileX;
                    long offset = getTileOrStripOffset(tileIndex);
                    long byteLength = getTileOrStripByteCount(tileIndex);
                    cogTileInfo.addTileRange(tileIndex, offset, byteLength);
                }
            }
        }

        // read the ranges and cache them in the image input stream delegate
        ((DefaultCogImageInputStream) stream).readRanges();

        // At this point, the DefaultCogImageInputStream has fetched and cached all of the bytes from the requested tiles.
        // Now we proceed with the legacy TIFFImageReader code.
        return super.read(imageIndex, param);
    }

}
