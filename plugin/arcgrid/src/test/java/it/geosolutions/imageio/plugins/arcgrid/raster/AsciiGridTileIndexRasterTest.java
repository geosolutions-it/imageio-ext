package it.geosolutions.imageio.plugins.arcgrid.raster;

import java.awt.*;
import java.awt.image.Raster;
import java.awt.image.renderable.ParameterBlock;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import it.geosolutions.imageio.plugins.arcgrid.spi.AsciiGridsImageReaderSpi;
import junit.framework.TestCase;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageWriter;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import com.sun.media.jai.operator.ImageWriteDescriptor;

public class AsciiGridTileIndexRasterTest extends TestCase {
  public AsciiGridTileIndexRasterTest(String name) {
    super(name);
  }

  // needs to be big enough to trigger use of tiles in underlying java imageio code - these
  // values seem reasonable?
  private final int width = 1000;
  private final int height = 1000;

  private File ascFile;
  private long headerBytes;
  private long padHeaderTo = 99;

  public void createRaster(boolean alignNumbers) throws IOException {
    ascFile = File.createTempFile("grid-test", ".asc");
    ascFile.deleteOnExit();

    PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(ascFile)));
    out.format("ncols %d\n", width);
    out.format("nrows %d\n", height);
    out.format("xllcorner 0\n");
    out.format("yllcorner 0\n");
    out.format("cellsize 1");
    out.flush();
    this.headerBytes = ascFile.length();

    while (headerBytes++ < padHeaderTo) {
      out.write(' ');
    }

    // we can space them out regularly so that seeking to x gives the value (x - 100) / 10,
    // e.g. we find the number 114 at 1240
    int counter = 0;

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        if (!alignNumbers) {
          if (y == 0) {
            out.format("%n%d", counter);
          } else {
            out.format(" %d", counter);
          }

        } else {
          String padded = String.format("%-9d", counter);
          if (padded.length() > 9) {
            throw new RuntimeException();
          }

          if (y == 0) {
            out.format("%n%s", padded);
          } else {
            out.format("%s ", padded);
          }
        }
        counter++;
      }
    }
    out.println("");
    out.flush();
    out.close();
  }

  protected PlanarImage openRaster(File file) throws Exception {
    // here, we open the raster as closely as possible to how geotools is doing
    // it, to make sure the tiling happens, which is the cause of the bug
    final ImageReadParam readP = new ImageReadParam();

    final ParameterBlock pbjImageRead = new ParameterBlock();
    // prepare input to handle possible parallelism between different
    // readers
    AsciiGridsImageReaderSpi readerSPI = new AsciiGridsImageReaderSpi();
    pbjImageRead.add(ImageIO.createImageInputStream(file));
    pbjImageRead.add(0);
    pbjImageRead.add(Boolean.FALSE);
    pbjImageRead.add(Boolean.FALSE);
    pbjImageRead.add(Boolean.FALSE);
    pbjImageRead.add(null);
    pbjImageRead.add(null);
    pbjImageRead.add(readP);
    pbjImageRead.add(readerSPI.createReaderInstance());
    final RenderedOp asciiCoverage = JAI.create("ImageRead", pbjImageRead, new RenderingHints(null));
    return PlanarImage.wrapRenderedImage(asciiCoverage);
  }

  public void testCanSamplePixelsFromGridInSequence() throws Exception {
    createRaster(false);
    doInSequenceCheck(ascFile);
  }

  public void testCanSamplePixelsFromGridInSequenceAligned() throws Exception {
    createRaster(true);
    doInSequenceCheck(ascFile);
  }

  public void testCanSamplePixelsFromGridWithGapsBetweenUnderlyingImageTiles() throws Exception {
    createRaster(false);
    doOutOfOrderCheck(ascFile);
  }
  
  public void testCanSamplePixelsFromGridWithGapsBetweenUnderlyingImageTilesAligned() throws Exception {
    createRaster(true);
    doOutOfOrderCheck(ascFile);
  }

  public void testCanFetchAllTilesThenAssertPixelValuesInSequence() throws Exception {
    createRaster(false);
    canFetchAllTilesThenAssertPixelValuesInSequence();
  }

  public void testCanFetchAllTilesThenAssertPixelValuesInSequenceAligned() throws Exception {
    createRaster(true);
    canFetchAllTilesThenAssertPixelValuesInSequence();
  }

  public void canFetchAllTilesThenAssertPixelValuesInSequence() throws Exception {
    PlanarImage image = openRaster(ascFile);
    assertNotNull(image.getTiles());

    final int numPixels = width * height;
    int pixelCount = 0;
    while (pixelCount < numPixels) {
      assertPixelValue(image, pixelCount, pointFromExpectedPixelValue(pixelCount));
      pixelCount++;
    }

    image.dispose();
  }

  public void testOutOfOrderTileAccess() throws Exception {
    createRaster(false);
    outOfOrderTileAccess();
  }

  public void testOutOfOrderTileAccessAligned() throws Exception {
    createRaster(true);
    outOfOrderTileAccess();
  }

  public void outOfOrderTileAccess() throws Exception {
    // this test triggers the skipping routine and makes sure that it lands us in
    // the right position so that the first desired sample for the tile is read correctly
    ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
    pbjImageRead.setParameter("Input", ascFile.getAbsolutePath());
    RenderedOp image = JAI.create("ImageRead", pbjImageRead);

    final int pixelsPerTile = image.getTileWidth() * image.getTileHeight();
    int pixel = (pixelsPerTile);
    assertPixelValue(image, pixel, pointFromExpectedPixelValue(pixel));
  }

  public void testOutOfOrderTileAccessWithIndexedSkippedPositions() throws Exception {
    createRaster(false);
    outOfOrderTileAccessWithIndexedSkippedPositions();
  }
  public void testOutOfOrderTileAccessWithIndexedSkippedPositionsAligned() throws Exception {
    createRaster(true);
    outOfOrderTileAccessWithIndexedSkippedPositions();
  }

  	public void outOfOrderTileAccessWithIndexedSkippedPositions() throws Exception {
    // this test confirms that the stream positions remembered during skipping are correct
    ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
    pbjImageRead.setParameter("Input", ascFile.getAbsolutePath());
    RenderedOp image = JAI.create("ImageRead", pbjImageRead);

    // this causes the second tile to be built without using any index
    // it used to be flakey and pointed the stream at the wrong position in the file
    image.getTile(0, 2);

    final int pixelsPerTile = image.getTileWidth() * image.getTileHeight();
    int pixel = (pixelsPerTile) + 10;
    assertPixelValue(image, pixel, pointFromExpectedPixelValue(pixel));
  }

  public void testGetExactMatchTileUsingIndexEntriesSetDuringReading() throws Exception {
    createRaster(false);
    getExactMatchTileUsingIndexEntriesSetDuringReading();
  }

  public void testGetExactMatchTileUsingIndexEntriesSetDuringReadingAligned() throws Exception {
    createRaster(true);
    getExactMatchTileUsingIndexEntriesSetDuringReading();
  }

  public void getExactMatchTileUsingIndexEntriesSetDuringReading() throws Exception {
    ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
    pbjImageRead.setParameter("Input", ascFile.getAbsolutePath());
    RenderedOp image = JAI.create("ImageRead", pbjImageRead);

    // this causes us to do throw away a lot of samples but put a whole bunch of entries in to the sample
    // stream index
    image.getTile(0, 3);

    // let's look up a value contained in the fith tile - this uses an index position
    // set once reading was finished and should not require any sample skipping
    final int pixelsPerTile = image.getTileWidth() * image.getTileHeight();
    int pixel = (pixelsPerTile * 4);
    assertPixelValue(image, pixel, pointFromExpectedPixelValue(pixel));
  }

  public void testGetTilesUsingIndexEntriesSetDuringReading() throws Exception {
    createRaster(false);
    getTilesUsingIndexEntriesSetDuringReading();
  }
  public void testGetTilesUsingIndexEntriesSetDuringReadingAligned() throws Exception {
    createRaster(true);
    getTilesUsingIndexEntriesSetDuringReading();
  }

  public void getTilesUsingIndexEntriesSetDuringReading() throws Exception {
    ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
    pbjImageRead.setParameter("Input", ascFile.getAbsolutePath());
    RenderedOp image = JAI.create("ImageRead", pbjImageRead);

    // this causes us to do throw away a lot of samples but put a whole bunch of entries in to the sample
    // stream index
    image.getTile(0, 3);

    // let's look up a value contained in the sixth tile - this uses an index position
    // set once reading was finished and also requires some skipping
    final int pixelsPerTile = image.getTileWidth() * image.getTileHeight();
    int pixel = (pixelsPerTile * 5);
    assertPixelValue(image, pixel, pointFromExpectedPixelValue(pixel));
  }

  public void testCanWriteAndRead() throws Exception {
    createRaster(false);
    canWriteAndRead();
  }

  public void testCanWriteAndReadAligned() throws Exception {
    createRaster(true);
    canWriteAndRead();
  }

  public void canWriteAndRead() throws Exception {
    ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
    pbjImageRead.setParameter("Input", ascFile.getAbsolutePath());
    RenderedOp image = JAI.create("ImageRead", pbjImageRead);
    image.getTiles();

    final int numPixels = width * height;
    int pixelCount = 0;
    while (pixelCount < numPixels) {
      assertPixelValue(image, pixelCount, pointFromExpectedPixelValue(pixelCount));
      pixelCount++;
    }


    final File foutput = File.createTempFile("grid-written-test", ".asc");
    foutput.deleteOnExit();

    final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI(
            "ImageWrite");
    pbjImageWrite.setParameter("Output", foutput);
    pbjImageWrite.addSource(image);

    final RenderedOp op = JAI.create("ImageWrite", pbjImageWrite);
    final ImageWriter writer = (ImageWriter) op.getProperty(ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITER);
    writer.dispose();

    // assert the written out data can be read in propertly
    doInSequenceCheck(foutput);
    doOutOfOrderCheck(foutput);

  }

  public void doOutOfOrderCheck(File foutput) throws Exception {
    PlanarImage image = openRaster(foutput);

    int tileHeight = image.getTileHeight();
    if (tileHeight != image.getHeight() && image.getWidth() == image.getTileWidth()) {

      // regression test - there was a bug where indexes in to the file were cached, but those values
      // were used inappropriately, resulting in an off by one error
      int pixelsBetweenTiles = width * tileHeight * 2;

      int[] values = new int[] {1001, 1001 + pixelsBetweenTiles, 1001 + (pixelsBetweenTiles * 2)};

      for (int value : values) {
        assertPixelValue(image, value, pointFromExpectedPixelValue(value));
      }
    } else {
      fail("image tiling not as expected");
    }
    image.dispose();
  }

  public void doInSequenceCheck(File foutput) throws Exception {
    PlanarImage image = openRaster(foutput);
    final int numPixels = width * height;
    int pixelCount = 0;
    while (pixelCount < numPixels) {
      assertPixelValue(image, pixelCount, pointFromExpectedPixelValue(pixelCount));
      pixelCount++;
    }
    image.dispose();
  }

  // because grid dimensions are 1-to-1 with the bitmap (apart from y axis reversal) and the bitmap
  // is a sequence of increasing integers, we can build a point from an expected value.
  private Point pointFromExpectedPixelValue(int expectedValue) {
    int x = expectedValue % width;
    int y =  (expectedValue / height);
    return new Point(x, y);
  }

  private void assertPixelValue(PlanarImage image, int expected, Point point) {
    Raster tile = image.getTile(image.XToTileX(point.x), image.YToTileY(point.y));
    assertEquals(expected, tile.getSample(point.x, point.y, 0));
  }
}
