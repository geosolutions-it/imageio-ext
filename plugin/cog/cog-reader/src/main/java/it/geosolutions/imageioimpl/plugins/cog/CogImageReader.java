package it.geosolutions.imageioimpl.plugins.cog;

import it.geosolutions.imageio.plugins.tiff.BaselineTIFFTagSet;
import it.geosolutions.imageio.plugins.cog.CogImageReadParam;
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
        // if the image input stream isn't a CogImageInputStream, skip all this nonsense and just use the original code
        if (!(stream instanceof CogImageInputStream)) {
            return super.read(imageIndex, param);
        }

        // the input stream needs to have either been initialized with a RangeReader implementation or the RangeReader
        // implementation class needs to have been provided in a CogImageReadParam
        if (!((CogImageInputStream)stream).isInitialized() && param instanceof CogImageReadParam) {
            ((CogImageInputStream) stream).init((CogImageReadParam) param);
        }

        if (!((CogImageInputStream)stream).isInitialized()) {
            throw new IOException("The CogImageInputStream has not been initialized.  Either pass a RangeReader"
                    + " implementation to the CogImageInputStream via the constructor or init method, or declare"
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

        CogTileInfo cogTileInfo = ((CogImageInputStream)stream).getCogTileInfo();

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
        ((CogImageInputStream) stream).readRanges();

        // At this point, the CogImageInputStream has fetched and cached all of the bytes from the requested tiles.
        // Now we proceed with the legacy TIFFImageReader code.
        return super.read(imageIndex, param);
    }

}
