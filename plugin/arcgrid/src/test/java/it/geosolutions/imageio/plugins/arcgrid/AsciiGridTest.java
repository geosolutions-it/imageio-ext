/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
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
package it.geosolutions.imageio.plugins.arcgrid;

import it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageMetadata.RasterSpaceType;
import it.geosolutions.imageio.plugins.arcgrid.raster.AsciiGridRaster;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.resources.TestData;

import java.awt.Rectangle;
import java.awt.image.Raster;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.w3c.dom.Node;

import com.sun.media.jai.operator.ImageReadDescriptor;
import com.sun.media.jai.operator.ImageWriteDescriptor;

/**
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public class AsciiGridTest extends TestCase {
    public AsciiGridTest(String name) {
        super(name);
    }

    private final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.arcgrid");
    private static final double DELTA = 1E-6d;

    protected void setUp() throws Exception {
        super.setUp();
        File file = TestData.file(this, "arcgrid.zip");
        assertTrue(file.exists());

        // unzip it
        TestData.unzipFile(this, "arcgrid.zip");
        
        
        //ungizip spearfish
		BufferedInputStream fis = new BufferedInputStream(new FileInputStream(TestData.file(this, "spearfish.asc.gz")));
		GZIPInputStream gs = new GZIPInputStream(fis);
		
		BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(new File(TestData.file(this, "."),"spearfish_dem.arx"))); 
		
		byte buffer []= new byte[512];
		int i=0;
		while((i=gs.read(buffer))>0)
			fos.write(buffer,0,i);
		fos.close();
		gs.close();

    }
    
    public void testSetInputTwice() throws IOException {
        final File testFilesDirectory = TestData.file(this, ".");
        final File dem = new File(testFilesDirectory, "dem.asc");
        FileImageInputStreamExtImpl stream = new FileImageInputStreamExtImpl(dem);
        ImageReader reader = ImageIO.getImageReaders(stream).next();
        assertTrue(reader instanceof AsciiGridsImageReader);
        try {
            // used to throw an exception
            reader.setInput(stream);
            reader.setInput(stream);
            reader.read(0);
        } finally {
            reader.dispose();
        }
    }

    /**
     * Read an ArcGrid file and write it back to another file
     */
    public void testReadWrite() throws FileNotFoundException, IOException {
        String title = new String("Simple JAI ImageRead operation test");
        
        final String[] files = TestData.file(this,".").list(new FilenameFilter() {
			
			public boolean accept(File dir, String name) {
				return name.endsWith("asc")|name.endsWith("arx");
			}
		});
        File inputDirectory = TestData.file(this, ".");
        for(String fileName:files){
	        ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
	        pbjImageRead.setParameter("Input", new File(inputDirectory,fileName));
	        RenderedOp image = JAI.create("ImageRead", pbjImageRead);
	        if (TestData.isInteractiveTest())
	            ImageIOUtilities.visualize(image, title, true);
	        else
	            image.getTiles();
	
	        // //
	        //
	        // Writing it out
	        //
	        // //
	        final File foutput = TestData.temp(this, "file.asc", true);
	        final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI(
	                "ImageWrite");
	        pbjImageWrite.setParameter("Output", foutput);
	        pbjImageWrite.addSource(image);
	
	        // //
	        //
	        // What I am doing here is crucial, that is getting the used writer and
	        // disposing it. This will force the underlying stream to write data on
	        // disk.
	        //
	        // //
	        final RenderedOp op = JAI.create("ImageWrite", pbjImageWrite);
	        final ImageWriter writer = (ImageWriter) op.getProperty(ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITER);
	        writer.dispose();
	
	        // //
	        //
	        // Reading it back
	        //
	        // //
	        pbjImageRead = new ParameterBlockJAI("ImageRead");
	        pbjImageRead.setParameter("Input", foutput);
	        RenderedOp image2 = JAI.create("ImageRead", pbjImageRead);
	        title = new String("Read Back the just written image");
	        if (TestData.isInteractiveTest())
	            ImageIOUtilities.visualize(image, title, true);
	        else
	            Assert.assertNotNull(image2.getTiles());
	
	        final String error[] = new String[1];
	        final boolean result = compare(image, image2, error);
	        assertTrue(error[0], result);
        }
    }

    /**
     * Read an ESRI ArcGrid file and write it back as GRASS
     */
    public void testReadAsEsriAndWriteAsGrass() throws FileNotFoundException, IOException {
        String title = new String("Simple JAI ImageRead operation test");

        File testFile = TestData.file(this, "095b_dem_90m.asc");
        ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", testFile);
        RenderedOp image = JAI.create("ImageRead", pbjImageRead);
        if (TestData.isInteractiveTest()) {
            ImageIOUtilities.visualize(image, title, true);
        } else {
            assertNotNull(image.getTiles());
        }
        // //
        //
        // Writing it out
        //
        // //
        final File foutput = TestData.temp(this, "file.asc", true);
        final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI(
                "ImageWrite");
        pbjImageWrite.setParameter("Output", foutput);
        pbjImageWrite.addSource(image);

        final ImageReader reader = (ImageReader) image
                .getProperty(ImageReadDescriptor.PROPERTY_NAME_IMAGE_READER);
        final AsciiGridRaster raster = ((AsciiGridsImageReader) reader).getRasterReader();

        AsciiGridsImageMetadata grassMetadata = new AsciiGridsImageMetadata(
                raster.getNCols(), raster.getNRows(), raster.getCellSizeX(), raster.getCellSizeY(),
                raster.getXllCellCoordinate(), raster.getYllCellCoordinate(), 
                raster.isCorner(), true, raster.getNoData());
//        writer.write (new IIOImage(image, null, grassMetadata));
//        writer.dispose();
        pbjImageWrite.setParameter("ImageMetadata", grassMetadata);
        pbjImageWrite.setParameter("Transcode", false);

        
        // //
        //
        // What I am doing here is crucial, that is getting the used writer and
        // disposing it. This will force the underlying stream to write data on
        // disk.
        //
        // //
        final RenderedOp op = JAI.create("ImageWrite", pbjImageWrite);
        final ImageWriter writer = (ImageWriter) op.getProperty(ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITER);
        writer.dispose();


        
        
        // //
        //
        // Reading it back
        //
        // //
        pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", foutput);
        RenderedOp image2 = JAI.create("ImageRead", pbjImageRead);
        title = new String("Read Back the just written image");
        if (TestData.isInteractiveTest()) {
            ImageIOUtilities.visualize(image2, title, true);
        }else{
            assertNotNull(image2.getTiles());
        }

        final String error[] = new String[1];
        final boolean result = compare(image, image2, error, raster.getNoData());
        assertTrue(error[0], result);
    }

    /**
     * Read a GRASS, compressed (GZ) file
     */
    public void testReadGrassGZ() throws FileNotFoundException, IOException {
        // This test may require 20 seconds to be executed. Therefore it will
        // be run only when extensive tests are requested.
        if (TestData.isExtensiveTest()) {
            String title = new String("JAI ImageRead on a GRASS GZipped file ");
            LOGGER.info("\n\n " + title + " \n");
            File inputFile = TestData.file(this, "spearfish.asc.gz");
            final GZIPInputStream stream = new GZIPInputStream(
                    new FileInputStream(inputFile));
            ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
            pbjImageRead.setParameter("Input", stream);
            RenderedOp image = JAI.create("ImageRead", pbjImageRead);
            if (TestData.isInteractiveTest())
                ImageIOUtilities.visualize(image, title, true);
            else{
            	assertNotNull(image.getTiles());
            	image.dispose();
            }
            
        }

    }

    /**
     * Read a file using subSampling and sourceRegion settings
     */
    public void testReadRegionAndMetadata() throws FileNotFoundException, IOException {
        String title = new String(
                "JAI ImageRead using subSampling and sourceRegion ");
        LOGGER.info("\n\n " + title + " \n");

        //
        //   				DEM.asc
        //

        //
        // Preparing ImageRead parameters
        //
        File inputFile = TestData.file(this, "dem.asc");
        ParameterBlockJAI pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", inputFile);
        ImageReadParam irp = new ImageReadParam();

        // Setting sourceRegion on the original image
        irp.setSourceRegion(new Rectangle(200, 300, 1000, 1000));

        // Setting subSampling factors
        irp.setSourceSubsampling(2, 2, 0, 0);
        pbjImageRead.setParameter("ReadParam", irp);
        RenderedOp image = JAI.create("ImageRead", pbjImageRead);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, title, true);
        else{
        	// load data
        	assertNotNull(image.getTiles());
        	
        	// chec metadata
        	final AsciiGridsImageMetadata metadata=(AsciiGridsImageMetadata) image.getProperty(ImageReadDescriptor.PROPERTY_NAME_METADATA_IMAGE);
        	assertNotNull(metadata);
        	final Node mf = metadata.getAsTree(metadata.getNativeMetadataFormatName());
        	// not grass
        	assertEquals(Boolean.parseBoolean(mf.getChildNodes().item(0).getNodeValue()), false);
        	
        	// raster space type
        	assertEquals(mf.getChildNodes().item(1).getAttributes().getNamedItem("rasterSpaceType").getNodeValue(),RasterSpaceType.PixelIsArea.toString());
        	
        	// no data 
        	assertEquals(mf.getChildNodes().item(1).getAttributes().getNamedItem("noDataValue").getNodeValue(),"-9999.0");
        	
        	//width
        	assertEquals(mf.getChildNodes().item(1).getAttributes().getNamedItem("nColumns").getNodeValue(),"1404");
        	
        	//height
        	assertEquals(mf.getChildNodes().item(1).getAttributes().getNamedItem("nRows").getNodeValue(),"1400");
        	
        	//cellsizeX
        	assertEquals(mf.getChildNodes().item(2).getAttributes().getNamedItem("cellsizeX").getNodeValue(),"22.5");
        	
        	//cellsizeY
        	assertEquals(mf.getChildNodes().item(2).getAttributes().getNamedItem("cellsizeY").getNodeValue(),"22.5");
        	
        	//xll
        	assertEquals(mf.getChildNodes().item(2).getAttributes().getNamedItem("xll").getNodeValue(),"969870.0");
        	
        	//yll
        	assertEquals(mf.getChildNodes().item(2).getAttributes().getNamedItem("yll").getNodeValue(),"642840.0");        	
        	
        }
        image.dispose();
        
        

        //
        //   				DEM.asc
        //

        //
        // Preparing ImageRead parameters
        //
        inputFile = TestData.file(this, "spearfish_dem.arx");
        pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", inputFile);
        irp = new ImageReadParam();

        // Setting subSampling factors
        irp.setSourceSubsampling(4, 4, 0, 0);
        pbjImageRead.setParameter("ReadParam", irp);
        image = JAI.create("ImageRead", pbjImageRead);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, title, true);
        else{
        	// load data
        	assertNotNull(image.getTiles());
        	
        	// chec metadata
        	final AsciiGridsImageMetadata metadata=(AsciiGridsImageMetadata) image.getProperty(ImageReadDescriptor.PROPERTY_NAME_METADATA_IMAGE);
        	assertNotNull(metadata);
        	final Node mf = metadata.getAsTree(metadata.getNativeMetadataFormatName());
        	// not grass
        	assertEquals(Boolean.parseBoolean(mf.getChildNodes().item(0).getNodeValue()), false);
        	
        	// raster space type
        	assertEquals(mf.getChildNodes().item(1).getAttributes().getNamedItem("rasterSpaceType").getNodeValue(),RasterSpaceType.PixelIsArea.toString());
        	
        	// no data 
        	assertEquals(mf.getChildNodes().item(1).getAttributes().getNamedItem("noDataValue").getNodeValue(),"NaN");
        	
        	//width
        	assertEquals(mf.getChildNodes().item(1).getAttributes().getNamedItem("nColumns").getNodeValue(),"634");
        	
        	//height
        	assertEquals(mf.getChildNodes().item(1).getAttributes().getNamedItem("nRows").getNodeValue(),"477");
        	
        	//cellsizeX
        	assertEquals(mf.getChildNodes().item(2).getAttributes().getNamedItem("cellsizeX").getNodeValue(),"30.0");
        	
        	//cellsizeY
        	assertEquals(mf.getChildNodes().item(2).getAttributes().getNamedItem("cellsizeY").getNodeValue(),"30.0");
        	
        	//xll
        	assertEquals(mf.getChildNodes().item(2).getAttributes().getNamedItem("xll").getNodeValue(),"589980.0");
        	
        	//yll
        	assertEquals(mf.getChildNodes().item(2).getAttributes().getNamedItem("yll").getNodeValue(),"4913700.0");        	
        	
        }
        
        //
        //   				SWAN_NURC_LigurianSeaL07_HSIGN.asc
        //

        //
        // Preparing ImageRead parameters
        //
        inputFile = TestData.file(this, "SWAN_NURC_LigurianSeaL07_HSIGN.asc");
        pbjImageRead = new ParameterBlockJAI("ImageRead");
        pbjImageRead.setParameter("Input", inputFile);
        irp = new ImageReadParam();

        // Setting subSampling factors
        irp.setSourceSubsampling(4, 4, 0, 0);
        pbjImageRead.setParameter("ReadParam", irp);
        image = JAI.create("ImageRead", pbjImageRead);
        if (TestData.isInteractiveTest())
            ImageIOUtilities.visualize(image, title, true);
        else{
        	// load data
        	assertNotNull(image.getTiles());
        	
        	// chec metadata
        	final AsciiGridsImageMetadata metadata=(AsciiGridsImageMetadata) image.getProperty(ImageReadDescriptor.PROPERTY_NAME_METADATA_IMAGE);
        	assertNotNull(metadata);
        	final Node mf = metadata.getAsTree(metadata.getNativeMetadataFormatName());
        	// not grass
        	assertEquals(Boolean.parseBoolean(mf.getChildNodes().item(0).getNodeValue()), false);
        	
        	// raster space type
        	assertEquals(mf.getChildNodes().item(1).getAttributes().getNamedItem("rasterSpaceType").getNodeValue(),RasterSpaceType.PixelIsPoint.toString());
        	
        	// no data 
        	assertEquals(mf.getChildNodes().item(1).getAttributes().getNamedItem("noDataValue").getNodeValue(),"-9.0");
        	
        	//width
        	assertEquals(mf.getChildNodes().item(1).getAttributes().getNamedItem("nColumns").getNodeValue(),"278");
        	
        	//height
        	assertEquals(mf.getChildNodes().item(1).getAttributes().getNamedItem("nRows").getNodeValue(),"144");
        	
        	//cellsizeX
        	assertEquals(mf.getChildNodes().item(2).getAttributes().getNamedItem("cellsizeX").getNodeValue(),"0.008999999478566561");
        	
        	//cellsizeY
        	assertEquals(mf.getChildNodes().item(2).getAttributes().getNamedItem("cellsizeY").getNodeValue(),"0.008999999478566561");
        	
        	//xll
        	assertEquals(mf.getChildNodes().item(2).getAttributes().getNamedItem("xll").getNodeValue(),"8.118000030517578");
        	
        	//yll
        	assertEquals(mf.getChildNodes().item(2).getAttributes().getNamedItem("yll").getNodeValue(),"43.191001892089844");        	
        	
        }             
    }

    private static boolean compare(final RenderedOp image, final RenderedOp image2,
            final String error[]) {
        return compare(image, image2, error, Double.NaN);
    }
    
    /**
     * Compare images by testing each pixel of the first image equals the pixel
     * of the second image. Return <code>true</code> if compare is
     * successfully.
     * 
     * @param image
     *                the first image to be compared
     * @param image2
     *                the first image to be compared
     * @param error
     *                a container for error messages in case of differences.
     * @param noData 
     *                a value representing noData
     * @return <code>true</code> if everything is ok.
     */
    private static boolean compare(final RenderedOp image, final RenderedOp image2,
            final String error[], final double noData) {
        final int minTileX1 = image.getMinTileX();
        final int minTileY1 = image.getMinTileY();
        final int width = image.getTileWidth();
        final int height = image.getTileHeight();
        final int maxTileX1 = minTileX1 + image.getNumXTiles();
        final int maxTileY1 = minTileY1 + image.getNumYTiles();
        double value1 = 0, value2 = 0;

        //compare values
        for (int tileIndexX = minTileX1; tileIndexX < maxTileX1; tileIndexX++)
            for (int tileIndexY = minTileY1; tileIndexY < maxTileY1; tileIndexY++) {

                final Raster r1 = image.getTile(tileIndexX, tileIndexY);
                final Raster r2 = image2.getTile(tileIndexX, tileIndexY);

                for (int i = r1.getMinX(); i < width; i++) {
                    for (int j = r1.getMinY(); j < height; j++) {
                        value1 = r1.getSampleDouble(i, j, 0);
                        value2 = r2.getSampleDouble(i, j, 0);
                        if (!Double.isNaN(noData)) {
                            value1 = replaceNoData(value1, noData);
                            value2 = replaceNoData(value2, noData);
                        }
                        
                        if (!(Double.isNaN(value1)&&Double.isNaN(value2))&& Math.abs(value1 - value2) > DELTA) {
                            error[0] = new StringBuilder(
                                    "Written back image is not equal to the original one: ")
                                    .append(value1).append(", ").append(value2)
                                    .toString();
                            return false;
                        }
                    }
                }
            }
        
        
        // compare metadata
        return true;
    }

    private static double replaceNoData(double value, double noData) {
        if (!Double.isNaN(value) && Math.abs(value - noData) < DELTA) {
            value = Double.NaN;
        }
        return value;

    }

}
