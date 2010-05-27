/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
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
package it.geosolutions.hdf.object.h4;

import it.geosolutions.resources.TestData;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.media.imageioimpl.common.ImageUtil;
import com.sun.media.jai.codecimpl.util.RasterFactory;

/**
 * Test class to leverage on the revisited JHDF Access framework:<BR>
 * Sample data requested:<BR>
 * 
 * 1) MISR_AM1_CGLS_WIN_2005_F04_0017.hdf<BR>
 * Available at:<BR>
 * <a href="http://eosweb.larc.nasa.gov/PRODOCS/misr/level3/download_data.html">
 * http://eosweb.larc.nasa.gov/PRODOCS/misr/level3/download_data.html</a><BR>
 * (Year 2005: Winter - Land/Surface Data)<BR>
 * 
 * ------------------------------------------------------------------------<BR>
 * 2) MODPM2007027121858.L3_000_EAST_MED.hdf<BR>
 * Available at:<BR>
 * <a
 * href="ftp://ftp.geo-solutions.it/incoming/MODPM2007027121858.L3_000_EAST_MED.hdf">
 * ftp://ftp.geo-solutions.it/incoming/MODPM2007027121858.L3_000_EAST_MED.hdf</a><BR>
 * (as anonymous ftp access, using active mode)<BR>
 * 
 * ------------------------------------------------------------------------<BR>
 * 3) TOVS_BROWSE_MONTHLY_AM_B861001.E861031_NF.HDF<BR>
 * 4) TOVS_5DAYS_AM_B870511.E870515_NG.HDF<BR>
 * 5) TOVS_BROWSE_DAILY_AM_861031_NF.HDF<BR>
 * Available at: <a
 * href="http://www.hdfgroup.uiuc.edu/UserSupport/code-examples/sample-programs/convert/Conversion.html">
 * http://www.hdfgroup.uiuc.edu/UserSupport/code-examples/sample-programs/convert/Conversion.html</a><BR>
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class HDFTest  {
    // Actually, HDF on Linux is not tested. Test are disabled.
    private static final Logger LOGGER = Logger
            .getLogger("it.geosolutions.hdf.object.h4");

    static {
        runTests = H4Utilities.isJHDFLibAvailable();
    }

    private static final boolean runTests;

    /**
     * Test a MISR HDF source
     */
    @Test
    public void testMisrSDS() throws HDFException, IOException {
        if (!runTests)
            return;
        StringBuffer outSb = new StringBuffer();
        File file = null;
        try {
            file = TestData.file(this, "MISR_AM1_CGLS_WIN_2005_F04_0017.hdf");
        } catch (FileNotFoundException fnfe) {
            warningMessage("MISR_AM1_CGLS_WIN_2005_F04_0017.hdf");
            return;
        }

        final H4File myFile;
        H4SDS sds;

        outSb.append("\n*************************************************"
                + "\n\t\tSDS test\n"
                + "*************************************************\n");
        myFile = new H4File(file.getAbsolutePath());

        H4SDSCollection sdsColl = myFile.getH4SdsCollection();
        final int sdsNum = sdsColl.size();

        // Simple constant to reduce the time and memory use of this test.
        int numOfSDSNeedToBeVisualized = 2;
        outSb.append("SDS num = ").append(sdsNum).append("\n");
        // //
        //
        // SDSs scan
        //
        // //
        for (int s = 0; s < sdsNum; s++) {
            sds = (H4SDS) sdsColl.get(s);
            outSb.append(printInfo(sds));

            // Description Annotations visualization
            List annotations = sds.getAnnotations(HDFConstants.AN_DATA_DESC);
            if (annotations != null) {
                final int annSize = annotations.size();
                for (int i = 0; i < annSize; i++) {
                    H4Annotation ann = (H4Annotation) annotations.get(i);
                    outSb.append(printInfo(ann));
                }
            }

            // Label Annotations visualization
            List annotations2 = sds.getAnnotations(HDFConstants.AN_DATA_LABEL);
            if (annotations2 != null) {
                final int annSize = annotations2.size();
                for (int i = 0; i < annSize; i++) {
                    H4Annotation ann = (H4Annotation) annotations2.get(i);
                    outSb.append(printInfo(ann));
                }
            }

            outSb.append("\n---------------------------").append(
                    "\n\tSDS Dimensions Info\n").append(
                    "---------------------------\n");

            // SDS Dimensions management
            final List dimList = sds.getDimensions();
            if (dimList != null) {
                final int dimSizes = dimList.size();
                if (dimSizes != 0)

                    // Dimensions scan
                    for (int i = 0; i < dimSizes; i++) {
                        H4Dimension dim = sds.getDimension(i);
                        outSb.append(printInfo(dim));
                        if (dim.isHasDimensionScaleSet())
                            outSb.append(printDimensionScaleValues(dim));
                        final int nAttrib = dim.getNumAttributes();

                        // Dimension's attributes visualization
                        if (nAttrib != 0) {
                            for (int j = 0; j < nAttrib; j++) {
                                H4Attribute attrib = (H4Attribute) dim
                                        .getAttribute(j);
                                outSb.append(printInfo(attrib));
                            }
                        }
                    }
            }
            outSb.append("\n---------------------------").append(
                    "\n\tSDS Attributes Info\n").append(
                    "---------------------------\n");

            // SDS Attributes visualization
            final int attrNum = sds.getNumAttributes();
            if (attrNum != 0) {
                for (int i = 0; i < attrNum; i++) {
                    H4Attribute attrib = (H4Attribute) sds.getAttribute(i);
                    outSb.append(printInfo(attrib));
                }
            }

            // Visualize 2D datasets
            if (sds.getRank() == 2 && (numOfSDSNeedToBeVisualized--) > 0) {
                BufferedImage bimage = getBufferedImage(sds);
                if (TestData.isInteractiveTest())
                    visualize("", bimage, 7, 800, 600);
            }
            sds.dispose();
        }
        myFile.dispose();
        LOGGER.info("\n" + outSb.toString());
    }

    /**
     * Testing File Annotations (Label/Description), Data Object (SDS/GRImage)
     * Annotations (Label/Description)
     */
    @Test
    public void testAnnotations() throws HDFException, IOException {
        if (!runTests)
            return;
        StringBuffer outSb = new StringBuffer();
        File file = null;
        File file2 = null;
        try {
            file = TestData
                    .file(this, "MODPM2007027121858.L3_000_EAST_MED.hdf");
        } catch (FileNotFoundException fnfe) {
            warningMessage("MODPM2007027121858.L3_000_EAST_MED.hdf");
        }
        try {
            file2 = TestData.file(this,
                    "TOVS_BROWSE_MONTHLY_AM_B861001.E861031_NF.HDF");
        } catch (FileNotFoundException fnfe) {
            warningMessage("TOVS_BROWSE_MONTHLY_AM_B861001.E861031_NF.HDF");
        }
        if (file == null || file2 == null)
            return;

        H4File myFile;
        H4SDS sds;
        H4GRImage grImage;
        List annotations = null;

        // ////////////////////////////////////////////////////////////////////
        //
        // File annotations
        //
        // ////////////////////////////////////////////////////////////////////
        final int[] anTypes = new int[] { HDFConstants.AN_FILE_DESC,
                HDFConstants.AN_FILE_LABEL };

        outSb.append("*************************************************"
                + "\n\t\tFile Annotations test\n"
                + "*************************************************\n");

        myFile = new H4File(file.getAbsolutePath());

        H4AnnotationManager annManager = myFile.getH4AnnotationManager();
        outSb.append("File descriptions: ").append(
                annManager.getNFileDescriptions()).append(" | File labels: ")
                .append(annManager.getNFileLabels()).append(
                        " | Data Object descriptions: ").append(
                        annManager.getNDataObjectDescriptions()).append(
                        " | Data Object labels: ").append(
                        annManager.getNDataObjectLabels()).append("\n");

        for (int j = 0; j < 2; j++) {
            List fileAnn = myFile.getAnnotations(anTypes[j]);
            if (fileAnn != null) {
                final int fileDescNum = fileAnn.size();
                for (int i = 0; i < fileDescNum; i++) {
                    H4Annotation ann = (H4Annotation) fileAnn.get(i);
                    outSb.append(printInfo(ann));
                }
            }
        }
        if (TestData.isInteractiveTest()) {
            visualizeText("FILE ANNOTATIONS", outSb.toString());
        } else
            LOGGER.info("\n" + outSb.toString());
        outSb = new StringBuffer();
        // ////////////////////////////////////////////////////////////////////
        //
        // SDS annotations
        //
        // ////////////////////////////////////////////////////////////////////
        outSb.append("*************************************************"
                + "\n\t\tSDS Annotations test\n"
                + "*************************************************\n");
        final H4SDSCollection sdsCollection = myFile.getH4SdsCollection();
        final int nSDS = sdsCollection.size();

        // SDS scan
        for (int s = 0; s < nSDS; s++) {
            sds = (H4SDS) sdsCollection.get(s);
            outSb.append(printInfo(sds));
            // Description Annotations
            annotations = sds.getAnnotations(HDFConstants.AN_DATA_DESC);
            if (annotations != null) {
                final int annSize = annotations.size();
                for (int i = 0; i < annSize; i++) {
                    H4Annotation ann = (H4Annotation) annotations.get(i);
                    outSb.append(printInfo(ann));
                    ann.dispose();
                }
            }
            // Label Annotations
            annotations = sds.getAnnotations(HDFConstants.AN_DATA_LABEL);
            if (annotations != null) {
                final int annSize = annotations.size();
                for (int i = 0; i < annSize; i++) {
                    H4Annotation ann = (H4Annotation) annotations.get(i);
                    outSb.append(printInfo(ann));
                    ann.dispose();
                }
            }
            sds.dispose();
        }
        myFile.dispose();
        if (TestData.isInteractiveTest()) {
            visualizeText("SDS ANNOTATIONS", outSb.toString());
        } else
            LOGGER.info("\n" + outSb.toString());
        outSb = new StringBuffer();

        // ////////////////////////////////////////////////////////////////////
        //
        // GRImage annotations
        //
        // ////////////////////////////////////////////////////////////////////
        outSb.append("*************************************************"
                + "\n\t\tGRImages Annotations test\n"
                + "*************************************************\n");

        myFile = new H4File(file2.getAbsolutePath());

        annManager = myFile.getH4AnnotationManager();
        outSb.append("File descriptions: ").append(
                annManager.getNFileDescriptions()).append(" | File labels: ")
                .append(annManager.getNFileLabels()).append(
                        " | Data Object descriptions: ").append(
                        annManager.getNDataObjectDescriptions()).append(
                        " | Data Object labels: ").append(
                        annManager.getNDataObjectLabels()).append("\n");

        final H4GRImageCollection grImageColl = myFile.getH4GRImageCollection();
        final int nImages = grImageColl.size();

        // GRImage scan
        for (int im = 0; im < nImages; im++) {
            grImage = (H4GRImage) grImageColl.get(im);

            // Description Annotations
            annotations = grImage.getAnnotations(HDFConstants.AN_DATA_DESC);
            if (!annotations.isEmpty()) {
                final int annSize = annotations.size();
                for (int i = 0; i < annSize; i++) {
                    H4Annotation ann = (H4Annotation) annotations.get(i);
                    outSb.append(printInfo(ann));
                }
            }

            // Label Annotations
            annotations = grImage.getAnnotations(HDFConstants.AN_DATA_LABEL);
            if (!annotations.isEmpty()) {
                final int annSize = annotations.size();
                for (int i = 0; i < annSize; i++) {
                    H4Annotation ann = (H4Annotation) annotations.get(i);
                    outSb.append(printInfo(ann));
                }
            }

            // Annotations are returned as unmodifiable
            try {
                annotations.remove(2);
                Assert.assertTrue(false);
            } catch (UnsupportedOperationException uoe) {
                // annotations are returned as an unmodifiable list
                annotations = null;
            }

        }
        myFile.dispose();
        if (TestData.isInteractiveTest()) {
            visualizeText("GRIMAGE ANNOTATIONS", outSb.toString());
        } else
            LOGGER.info("\n" + outSb.toString());
    }

    /**
     * Test attributes management from various object.
     */
    @Test
    public void testAttributes() throws HDFException, IOException {
        if (!runTests)
            return;
        StringBuffer outSb = new StringBuffer();
        File file = null;
        try {
            file = TestData
                    .file(this, "MODPM2007027121858.L3_000_EAST_MED.hdf");
        } catch (FileNotFoundException fnfe) {
            warningMessage("MODPM2007027121858.L3_000_EAST_MED.hdf");
            return;
        }

        // ////////////////////////////////////////////////////////////////////
        //
        // SDS Collection attributes TEST
        //
        // ////////////////////////////////////////////////////////////////////
        outSb.append("\n\n\n*************************************************"
                + "\n\t\tAttribute from SDSCollection\n"
                + "*************************************************\n");
        H4File myFile = new H4File(file.getAbsolutePath());
        H4SDSCollection sdsColl = myFile.getH4SdsCollection();
        H4SDS sds;

        int attrNum = sdsColl.getNumAttributes();
        if (attrNum != 0) {
            for (int i = 0; i < attrNum; i++) {
                H4Attribute attrib = (H4Attribute) sdsColl.getAttribute(i);
                outSb.append(printInfo(attrib));
            }
        }
        if (TestData.isInteractiveTest()) {
            visualizeText("ATTRIBUTES FROM SDS COLLECTION", outSb.toString());
        } else
            LOGGER.info("\n" + outSb.toString());
        outSb = new StringBuffer();
        outSb.append("\n\n\n======================================="
                + "\n\t\tDatasets scan\n"
                + "=======================================\n");
        final int sdsNum = sdsColl.size();

        // ////////////////////////////////////////////////////////////////////
        // 
        // Test all SDS in the collection
        //
        // ////////////////////////////////////////////////////////////////////
        for (int i = 0; i < sdsNum; i++) {
            sds = (H4SDS) sdsColl.get(i);
            attrNum = sds.getNumAttributes();
            outSb
                    .append("-----------> Dataset ")
                    .append(i)
                    .append(" has ")
                    .append(attrNum)
                    .append(
                            " attributes\n=========================================\n");
            // find all attributes of the current SDS
            if (attrNum != 0) {
                for (int j = 0; j < attrNum; j++) {
                    H4Attribute attrib = (H4Attribute) sds.getAttribute(j);
                    outSb.append(printInfo(attrib));
                }
            }
            // find predefined attribute
            H4Attribute attribute = sds
                    .getAttribute(H4Utilities.SDS_PREDEF_ATTR_LONG_NAME);
            if (attribute != null)
                printInfo(attribute);
            sds.dispose();
        }
        if (TestData.isInteractiveTest()) {
            visualizeText("ATTRIBUTES FROM SDS", outSb.toString());
        } else
            LOGGER.info("\n" + outSb.toString());
        myFile.dispose();
    }

    /**
     * Test predefined dimension attributes management.
     */
    @Test
    public void testPredefinedDimensionAttributes() throws HDFException,
            IOException {
        if (!runTests)
            return;
        StringBuffer outSb = new StringBuffer();
        File file = null;
        try {
            file = TestData.file(this, "TOVS_5DAYS_AM_B870511.E870515_NG.HDF");
        } catch (FileNotFoundException fnfe) {
            warningMessage("TOVS_5DAYS_AM_B870511.E870515_NG.HDF");
            return;
        }

        // //
        // 
        // Test Predefined Dimension Attributes
        //
        // //
        outSb.append("\n\n\n*************************************************"
                + "\n\t\tPredefined Dimension Attributes test\n"
                + "*************************************************\n");
        final H4File myFile = new H4File(file.getAbsolutePath());
        final H4SDSCollection sdsCollection = myFile.getH4SdsCollection();
        final int nSDSDatasets = sdsCollection.size();

        // SDSs scan
        for (int s = 0; s < nSDSDatasets; s++) {
            H4SDS h4sds = (H4SDS) sdsCollection.get(s);
            final int rank = h4sds.getRank();
            outSb.append(printInfo(h4sds));

            // Dimensions scan
            for (int i = 0; i < rank; i++) {
                H4Dimension dimension = h4sds.getDimension(i);
                final String[] predefinedAttributes = new String[] {
                        H4Utilities.PREDEF_ATTR_LABEL,
                        H4Utilities.PREDEF_ATTR_UNIT,
                        H4Utilities.PREDEF_ATTR_FORMAT };
                for (int attr = 0; attr < 3; attr++) {
                    String attrName = predefinedAttributes[attr];
                    H4Attribute attrib = dimension.getAttribute(attrName);
                    if (attrib != null) {
                        outSb.append("Explicitly requested ").append(attrName)
                                .append(" predefined attribute:\n").append(
                                        printInfo(attrib));
                    }
                }
            }
            outSb.append("======================================\n");
        }
        if (TestData.isInteractiveTest()) {
            visualizeText("DIMENSION PREDEFINED ATTRIBUTES", outSb.toString());
        } else
            LOGGER.info("\n" + outSb.toString());
        myFile.dispose();
    }

    /**
     * Test group Structure
     */
    @Test
    public void testGroups() throws HDFException, IOException {
        if (!runTests)
            return;
        StringBuffer outSb = new StringBuffer();
        File file = null;
        try {
            file = TestData.file(this, "MISR_AM1_CGLS_WIN_2005_F04_0017.hdf");
        } catch (FileNotFoundException fnfe) {
            warningMessage("MISR_AM1_CGLS_WIN_2005_F04_0017.hdf");
            return;
        }

        outSb.append("\n\n\n*************************************************"
                + "\n\t\tGroups test\n"
                + "*************************************************\n");
        final H4File myFile = new H4File(file.getAbsolutePath());
        final H4VGroupCollection grColl = myFile.getH4VGroupCollection();
        final int nGroups = grColl.size();
        for (int k = 0; k < nGroups; k++) {
            H4VGroup group = (H4VGroup) grColl.get(k);
            outSb.append(printInfo(group));
            List list = group.getTagRefList();
            if (list != null) {
                final int listSize = list.size();
                outSb
                        .append("\nscanning found objects\n======================\n");
                for (int i = 0; i < listSize; i++) {
                    int[] tagRefs = (int[]) list.get(i);
                    outSb.append("INDEX  = ").append(i).append("|---> TAG=")
                            .append(tagRefs[0]).append(" REF=").append(
                                    tagRefs[1]).append("\n");
                    if (H4Utilities.isAVGroup(group, tagRefs[1])) {
                        outSb.append("is a VGroup\n");
                        H4VGroup newGroup = new H4VGroup(group, tagRefs[1]);
                        outSb.append(printInfo(newGroup));
                    }
                }
            }
            outSb.append("\n");
        }
        if (TestData.isInteractiveTest()) {
            visualizeText("GROUPS", outSb.toString());
        } else
            LOGGER.info("\n" + outSb.toString());
        myFile.dispose();
    }

    /**
     * Test Dimension scales management
     */
    @Test
    public void testDimensionScales() throws HDFException, IOException {
        if (!runTests)
            return;
        StringBuffer outSb = new StringBuffer();
        File file = null;
        try {
            file = TestData.file(this, "TOVS_5DAYS_AM_B870511.E870515_NG.HDF");
        } catch (FileNotFoundException fnfe) {
            warningMessage("TOVS_5DAYS_AM_B870511.E870515_NG.HDF");
            return;
        }

        // //
        // 
        // Test SDS Dimension scales
        //
        // //
        outSb.append("\n\n\n*************************************************"
                + "\n\t\tDimension scales test\n"
                + "*************************************************\n");
        final H4File myFile = new H4File(file.getAbsolutePath());
        final H4SDSCollection sdsCollection = myFile.getH4SdsCollection();
        final int nSDSDatasets = sdsCollection.size();

        // SDSs scan
        for (int s = 0; s < nSDSDatasets; s++) {
            H4SDS h4sds = (H4SDS) sdsCollection.get(s);
            final int rank = h4sds.getRank();
            outSb.append(printInfo(h4sds));

            // Dimensions scan
            for (int i = 0; i < rank; i++) {
                H4Dimension dimension = h4sds.getDimension(i);
                outSb.append(printInfo(dimension));
                if (dimension.isHasDimensionScaleSet()) {
                    // Print dimension scale values
                    outSb.append("Dimension ").append(i).append(
                            " has Dimension Scale set\n");
                    outSb.append(printDimensionScaleValues(dimension));
                }
                int attrNum = dimension.getNumAttributes();
                if (attrNum != 0) {
                    for (int k = 0; k < attrNum; k++) {
                        H4Attribute attrib = (H4Attribute) dimension
                                .getAttribute(k);
                        outSb.append(printInfo(attrib));
                    }
                }
            }
            outSb.append("======================================\n");
        }
        if (TestData.isInteractiveTest()) {
            visualizeText("DIMENSION SCALES", outSb.toString());
        } else
            LOGGER.info("\n" + outSb.toString());
        myFile.dispose();
    }

    /**
     * Test Paletted GR Images
     */
    @Test
    @Ignore
    public void testVisualizePalettedGRImage() throws HDFException, IOException {
        if (!runTests)
            return;
        StringBuilder outSb = new StringBuilder();
        final File file = TestData.file(this, "palette.hdf4");

        final H4File myFile;
        H4GRImage grImage;

        // //
        // 
        // Test GRImages
        //
        // //
        outSb.append("*************************************************"
                + "\n\t\tGRImages ( + Palette ) test\n"
                + "*************************************************\n");
        myFile = new H4File(file.getAbsolutePath());
        final H4GRImageCollection grImageCollection = myFile
                .getH4GRImageCollection();

        final Iterator it = grImageCollection.iterator();

        try {
            it.remove();
            Assert.assertTrue(false);
        } catch (Exception e) {
            // TODO: handle exception
        }
        int im = 0;
        // GRImages scan
        while (it.hasNext()) {
            grImage = (H4GRImage) it.next();
            final int palettes = grImage.getNumPalettes();
            ColorModel cm = null;
            if (palettes != 0) {
                // Getting the first palette
                H4Palette palette = grImage.getPalette(0);
                final int numEntries = palette.getNumEntries();

                // Getting palette values
                byte[] paletteData = palette.getValues();
                final int paletteInterlace = palette.getInterlaceMode();
                if (paletteData != null) {
                    byte[][] myPalette = new byte[3][numEntries];
                    if (paletteInterlace == HDFConstants.MFGR_INTERLACE_PIXEL) {
                        // color components are arranged in RGB, RGB, RGB, ...
                        for (int i = 0; i < numEntries; i++) {
                            myPalette[0][i] = paletteData[i * 3];
                            myPalette[1][i] = paletteData[i * 3 + 1];
                            myPalette[2][i] = paletteData[i * 3 + 2];
                        }
                    } else {
                        for (int i = 0; i < numEntries; i++) {
                            myPalette[0][i] = paletteData[i];
                            myPalette[1][i] = paletteData[256 + i];
                            myPalette[2][i] = paletteData[512 + i];
                        }
                    }
                    cm = new IndexColorModel(8, // bits - the number of bits
                            // each pixel occupies
                            numEntries, // size - the size of the color
                            // component arrays
                            myPalette[0], // the array of red color comps
                            myPalette[1], // the array of green color comps
                            myPalette[2]); // the array of blue color comps
                }
            }

            final int rank = 2; // Images are always 2D
            final int dimSizes[] = grImage.getDimSizes();
            final int width = dimSizes[0];
            final int height = dimSizes[1];
            final int strideX = 1; // subsamplingx
            final int strideY = 1; // subsamplingy
            final Rectangle sourceRegion = new Rectangle(0, 0, width / 1,
                    height / 1);
            final Rectangle destinationRegion = new Rectangle(0, 0, width / 1,
                    height / 1);
            final ImageReadParam rp = new ImageReadParam();

            rp.setSourceRegion(sourceRegion);
            rp.setSourceSubsampling(strideX, strideY, 0, 0);
            computeRegions(rp, width, height, null, sourceRegion,
                    destinationRegion);

            final int[] start = new int[rank];
            final int[] stride = new int[rank];
            final int[] sizes = new int[rank];

            start[0] = sourceRegion.x;
            start[1] = sourceRegion.y;
            sizes[0] = sourceRegion.width / strideX;
            sizes[1] = sourceRegion.height / strideY;
            stride[0] = strideX;
            stride[1] = strideY;

            final int datatype = grImage.getDatatype();

            WritableRaster wr = null;
            Object data = null;
            data = grImage.read(start, stride, sizes);

            // bands variables
            final int[] banks = new int[1];
            final int[] offsets = new int[1];
            for (int band = 0; band < 1; band++) {
                banks[band] = band;
                offsets[band] = 0;
            }

            // Setting SampleModel and ColorModel
            final int bufferType = H4Utilities
                    .getBufferTypeFromDataType(datatype);
            SampleModel sm = cm.createCompatibleSampleModel(
                    destinationRegion.width, destinationRegion.height);

            // ////////////////////////////////////////////////////////////////////
            //
            // DATA READ
            //
            // ////////////////////////////////////////////////////////////////////

            final int size = destinationRegion.width * destinationRegion.height;
            DataBuffer dataBuffer = null;

            switch (bufferType) {
            case DataBuffer.TYPE_BYTE:
                dataBuffer = new DataBufferByte((byte[]) data, size);
                break;
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_USHORT:
                dataBuffer = new DataBufferShort((short[]) data, size);
                break;
            case DataBuffer.TYPE_INT:
                dataBuffer = new DataBufferInt((int[]) data, size);
                break;
            case DataBuffer.TYPE_FLOAT:
                dataBuffer = new DataBufferFloat((float[]) data, size);
                break;
            case DataBuffer.TYPE_DOUBLE:
                dataBuffer = new DataBufferDouble((double[]) data, size);
                break;
            }

            // Visualize image
            wr = Raster.createWritableRaster(sm, dataBuffer, null);
            BufferedImage bimage = new BufferedImage(cm, wr, false, null);
            final String name = grImage.getName();
            if (TestData.isInteractiveTest())
                visualize(name, bimage, im);

            im++;
        }
        LOGGER.info("\n" + outSb.toString());
        myFile.dispose();
    }

    /**
     * SDS Data Read/Visualization Test
     */
    @Test
    public void testSDSReadAndVisualize() throws HDFException, IOException {
        if (!runTests)
            return;
        StringBuffer outSb = new StringBuffer();
        File file = null;
        try {
            file = TestData.file(this, "TOVS_5DAYS_AM_B870511.E870515_NG.HDF");
        } catch (FileNotFoundException fnfe) {
            warningMessage("TOVS_5DAYS_AM_B870511.E870515_NG.HDF");
            return;
        }

        final H4File myFile;

        outSb.append("*************************************************"
                + "\n\t\tVisualization test\n"
                + "*************************************************\n");
        myFile = new H4File(file.getAbsolutePath());

        final H4SDSCollection sdsCollection = myFile.getH4SdsCollection();
        int nSDS = sdsCollection.size();

        // this HDF file contains several SDS with statistic data (STD e COUNT).
        nSDS /= 3;
        for (int sd = 0; sd < nSDS; sd++) {
            H4SDS sds = (H4SDS) sdsCollection.get(sd);
            final int nAttributes = sds.getNumAttributes();
            String title = "";
            if (nAttributes > 0) {
                H4Attribute attrib = sds
                        .getAttribute(H4Utilities.SDS_PREDEF_ATTR_LONG_NAME);
                if (attrib != null)
                    title = new String((byte[]) attrib.getValues());
            }
            if (title.length() == 0)
                title = "dataset " + sd;
            BufferedImage bimage = getBufferedImage(sds);
            if (TestData.isInteractiveTest())
                visualize(title, bimage, sd);
            else
                Assert.assertNotNull(bimage.getData());
        }
        LOGGER.info("\n" + outSb.toString());
        myFile.dispose();
    }

    /**
     * Build a BufferedImage given a H4SDS.
     * 
     * @param sds
     *                the H4SDS to be read
     * @return
     * @throws HDFException
     */
    private BufferedImage getBufferedImage(H4SDS sds) throws HDFException {
        final int rank = sds.getRank();
        final int dimSizes[] = sds.getDimSizes();
        final int width = dimSizes[rank - 1];
        final int height = dimSizes[rank - 2];

        final int strideX = 1; // subsamplingx
        final int strideY = 1; // subsamplingy
        final Rectangle sourceRegion = new Rectangle(0, 0, width / 1,
                height / 1);
        final Rectangle destinationRegion = new Rectangle(0, 0, width / 1,
                height / 1);
        final ImageReadParam rp = new ImageReadParam();

        rp.setSourceRegion(sourceRegion);
        rp.setSourceSubsampling(strideX, strideY, 0, 0);
        computeRegions(rp, width, height, null, sourceRegion, destinationRegion);

        final int[] start = new int[rank];
        final int[] stride = new int[rank];
        final int[] sizes = new int[rank];

        start[rank - 2] = sourceRegion.y;
        start[rank - 1] = sourceRegion.x;
        if (rank > 0) {
            for (int r = 0; r < rank - 2; r++) {
                start[r] = 0;
                sizes[r] = 1;
                stride[r] = 1;
            }
        }

        sizes[rank - 2] = sourceRegion.height / strideY;
        sizes[rank - 1] = sourceRegion.width / strideX;
        stride[rank - 2] = strideY;
        stride[rank - 1] = strideX;

        final int datatype = sds.getDatatype();

        WritableRaster wr = null;
        Object data = null;
        data = sds.read(start, stride, sizes);

        // bands variables
        final int[] banks = new int[1];
        final int[] offsets = new int[1];
        for (int band = 0; band < 1; band++) {
            banks[band] = band;
            offsets[band] = 0;
        }

        // Setting SampleModel and ColorModel
        final int bufferType = H4Utilities.getBufferTypeFromDataType(datatype);
        SampleModel sm = new BandedSampleModel(bufferType,
                destinationRegion.width, destinationRegion.height,
                destinationRegion.width, banks, offsets);

        final int nBands = sm.getNumBands();
        ColorModel cm = null;
        ColorSpace cs = null;
        if (nBands > 1) {
            // Number of Bands > 1.
            // ImageUtil.createColorModel provides to Creates a
            // ColorModel that may be used with the specified
            // SampleModel
            cm = ImageUtil.createColorModel(sm);

        } else if ((bufferType == DataBuffer.TYPE_BYTE)
                || (bufferType == DataBuffer.TYPE_USHORT)
                || (bufferType == DataBuffer.TYPE_INT)
                || (bufferType == DataBuffer.TYPE_FLOAT)
                || (bufferType == DataBuffer.TYPE_DOUBLE)) {

            // Just one band. Using the built-in Gray Scale Color Space
            cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            cm = RasterFactory.createComponentColorModel(bufferType, // dataType
                    cs, // color space
                    false, // has alpha
                    false, // is alphaPremultiplied
                    Transparency.OPAQUE); // transparency
        } else {
            if (bufferType == DataBuffer.TYPE_SHORT) {
                // Just one band. Using the built-in Gray Scale Color
                // Space
                cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
                cm = new ComponentColorModel(cs, false, false,
                        Transparency.OPAQUE, DataBuffer.TYPE_SHORT);
            }
        }

        // ////////////////////////////////////////////////////////////////////
        //
        // DATA READ
        //
        // ////////////////////////////////////////////////////////////////////

        final int size = destinationRegion.width * destinationRegion.height;
        DataBuffer dataBuffer = null;

        switch (bufferType) {
        case DataBuffer.TYPE_BYTE:
            dataBuffer = new DataBufferByte((byte[]) data, size);
            break;
        case DataBuffer.TYPE_SHORT:
        case DataBuffer.TYPE_USHORT:
            dataBuffer = new DataBufferShort((short[]) data, size);
            break;
        case DataBuffer.TYPE_INT:
            dataBuffer = new DataBufferInt((int[]) data, size);
            break;
        case DataBuffer.TYPE_FLOAT:
            dataBuffer = new DataBufferFloat((float[]) data, size);
            break;
        case DataBuffer.TYPE_DOUBLE:
            dataBuffer = new DataBufferDouble((double[]) data, size);
            break;
        }

        wr = Raster.createWritableRaster(sm, dataBuffer, null);
        BufferedImage bimage = new BufferedImage(cm, wr, false, null);
        return bimage;
    }

    /**
     * Computes the source region of interest and the destination region of
     * interest, taking the width and height of the source image, an optional
     * destination image, and an optional <code>ImageReadParam</code> into
     * account. The source region begins with the entire source image. Then that
     * is clipped to the source region specified in the
     * <code>ImageReadParam</code>, if one is specified.
     * 
     * <p>
     * If either of the destination offsets are negative, the source region is
     * clipped so that its top left will coincide with the top left of the
     * destination image, taking subsampling into account. Then the result is
     * clipped to the destination image on the right and bottom, if one is
     * specified, taking subsampling and destination offsets into account.
     * 
     * <p>
     * Similarly, the destination region begins with the source image, is
     * translated to the destination offset given in the
     * <code>ImageReadParam</code> if there is one, and finally is clipped to
     * the destination image, if there is one.
     * 
     * <p>
     * If either the source or destination regions end up having a width or
     * height of 0, an <code>IllegalArgumentException</code> is thrown.
     * 
     * <p>
     * The {@link #getSourceRegion <code>getSourceRegion</code>} method may be
     * used if only source clipping is desired.
     * 
     * @param param
     *                an <code>ImageReadParam</code>, or <code>null</code>.
     * @param srcWidth
     *                the width of the source image.
     * @param srcHeight
     *                the height of the source image.
     * @param image
     *                a <code>BufferedImage</code> that will be the
     *                destination image, or <code>null</code>.
     * @param srcRegion
     *                a <code>Rectangle</code> that will be filled with the
     *                source region of interest.
     * @param destRegion
     *                a <code>Rectangle</code> that will be filled with the
     *                destination region of interest.
     * @exception IllegalArgumentException
     *                    if <code>srcRegion</code> is <code>null</code>.
     * @exception IllegalArgumentException
     *                    if <code>dstRegion</code> is <code>null</code>.
     * @exception IllegalArgumentException
     *                    if the resulting source or destination region is
     *                    empty.
     */
    private static void computeRegions(ImageReadParam param, int srcWidth,
            int srcHeight, BufferedImage image, Rectangle srcRegion,
            Rectangle destRegion) {
        if (srcRegion == null) {
            throw new IllegalArgumentException("srcRegion == null!");
        }
        if (destRegion == null) {
            throw new IllegalArgumentException("destRegion == null!");
        }

        // Start with the entire source image
        srcRegion.setBounds(0, 0, srcWidth, srcHeight);

        // Destination also starts with source image, as that is the
        // maximum extent if there is no subsampling
        destRegion.setBounds(0, 0, srcWidth, srcHeight);

        // Clip that to the param region, if there is one
        int periodX = 1;
        int periodY = 1;
        int gridX = 0;
        int gridY = 0;
        if (param != null) {
            Rectangle paramSrcRegion = param.getSourceRegion();
            if (paramSrcRegion != null) {
                srcRegion.setBounds(srcRegion.intersection(paramSrcRegion));
            }
            periodX = param.getSourceXSubsampling();
            periodY = param.getSourceYSubsampling();
            gridX = param.getSubsamplingXOffset();
            gridY = param.getSubsamplingYOffset();
            srcRegion.translate(gridX, gridY);
            srcRegion.width -= gridX;
            srcRegion.height -= gridY;
            destRegion.setLocation(param.getDestinationOffset());
        }

        // Now clip any negative destination offsets, i.e. clip
        // to the top and left of the destination image
        if (destRegion.x < 0) {
            int delta = -destRegion.x * periodX;
            srcRegion.x += delta;
            srcRegion.width -= delta;
            destRegion.x = 0;
        }
        if (destRegion.y < 0) {
            int delta = -destRegion.y * periodY;
            srcRegion.y += delta;
            srcRegion.height -= delta;
            destRegion.y = 0;
        }

        // Now clip the destination Region to the subsampled width and height
        int subsampledWidth = (srcRegion.width + periodX - 1) / periodX;
        int subsampledHeight = (srcRegion.height + periodY - 1) / periodY;
        destRegion.width = subsampledWidth;
        destRegion.height = subsampledHeight;

        // Now clip that to right and bottom of the destination image,
        // if there is one, taking subsampling into account
        if (image != null) {
            Rectangle destImageRect = new Rectangle(0, 0, image.getWidth(),
                    image.getHeight());
            destRegion.setBounds(destRegion.intersection(destImageRect));
            if (destRegion.isEmpty()) {
                throw new IllegalArgumentException("Empty destination region!");
            }

            int deltaX = destRegion.x + subsampledWidth - image.getWidth();
            if (deltaX > 0) {
                srcRegion.width -= deltaX * periodX;
            }
            int deltaY = destRegion.y + subsampledHeight - image.getHeight();
            if (deltaY > 0) {
                srcRegion.height -= deltaY * periodY;
            }
        }
        if (srcRegion.isEmpty() || destRegion.isEmpty()) {
            throw new IllegalArgumentException("Empty region!");
        }
    }

    private void visualize(String title, BufferedImage bimage, int step) {
        visualize(title, bimage, step, 0, 0);
    }

    private void visualize(String title, BufferedImage bimage, int step,
            int width, int height) {
        if (width == 0 && height == 0) {
            width = 368;
            height = 188;
        }
        int offset = 0;
        if (title.length() == 0)
            offset += 400;
        final JFrame jf = new JFrame(title);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.getContentPane().add(new ScrollingImagePanel(bimage, width, height));
        jf.setLocation(250 * (step % 4), offset + (100 * (step / 4)));
        jf.pack();
        jf.setVisible(true);
    }

    /**
     * Simple utility method which returns a String containing information of
     * the provided HDF Object
     * 
     * @param object
     * @throws HDFException
     */
    private String printInfo(Object object) throws HDFException {
        StringBuffer sb = new StringBuffer();

        if (object instanceof H4Dimension) {
            // //
            //
            // H4Dimension Information
            //
            // //
            H4Dimension dim = (H4Dimension) object;
            sb
                    .append("\nDimension INFO\n--------------------------\n")
                    .append("index = ")
                    .append(dim.getIndex())
                    .append("\nname = ")
                    .append(dim.getName())
                    .append("\nID = ")
                    .append(dim.getIdentifier())
                    .append("\nsize = ")
                    .append(dim.getSize())
                    .append(
                            (dim.isHasDimensionScaleSet() ? ("\ndatatype = " + HDFConstants
                                    .getType(dim.getDatatype()))
                                    : (""))).append("\n");
        } else if (object instanceof H4Annotation) {
            // //
            //
            // H4Annotation Information
            //
            // //
            H4Annotation ann = (H4Annotation) object;
            sb
                    .append("\nANNOTATION INFO\n--------------------------\n")
                    .append("annotation Type = ")
                    .append(H4Annotation.getAnnotationTypeString(ann.getType()))
                    .append("\nID = ")
                    .append(ann.getIdentifier())
                    .append("\nTAG = ")
                    .append(ann.getTag())
                    .append("\nREF = ")
                    .append(ann.getReference())
                    .append("\ncontent = ")
                    .append(ann.getContent())
                    .append(
                            "\n--------------------------------------------------------------\n");
        } else if (object instanceof H4Attribute) {
            // //
            //
            // H4Attribute Information
            //
            // //
            H4Attribute attr = (H4Attribute) object;
            final int datatype = attr.getDatatype();
            sb.append("ATTRIBUTE INFO: ").append("index = ").append(
                    attr.getIndex()).append(" | name = ")
                    .append(attr.getName()).append(" | size = ").append(
                            attr.getSize()).append(" | datatype = ").append(
                            HDFConstants.getType(datatype))
                    .append("\nvalue = ");
            Object buf = attr.getValues();
            if (buf == null) {
                sb.append("\nNo values found\n");
                return sb.toString();
            }
            sb.append(attr.getValuesAsString());
            sb.append("\n--------------------------------\n");
        } else if (object instanceof H4VGroup) {
            // //
            //
            // H4VGroup Information
            //
            // //
            H4VGroup vgroup = (H4VGroup) object;
            sb.append("\nGROUP INFO\n--------------------------\n").append(
                    "ID = ").append(vgroup.getIdentifier()).append("\nTAG = ")
                    .append(vgroup.getTag()).append("\nREF = ").append(
                            vgroup.getReference()).append("\nclassName = ")
                    .append(vgroup.getClassName()).append("\nname = ").append(
                            vgroup.getName()).append(
                            "\nnumber of attributes = ").append(
                            vgroup.getNumAttributes()).append(
                            "\nnumber of objects in the group = ").append(
                            vgroup.getNumObjects()).append(
                            "\n======================================\n");

        } else if (object instanceof H4SDS) {
            // //
            //
            // H4SDS Information
            //
            // //
            H4SDS sds = (H4SDS) object;
            sb
                    .append(
                            "\nSDS INFO\n------------------------------------------------\n")
                    .append("ID = ")
                    .append(sds.getIdentifier())
                    .append(" | index = ")
                    .append(sds.getIndex())
                    .append(" | Dimensions number (RANK) = ")
                    .append(sds.getRank())
                    .append(" | name = ")
                    .append(sds.getName())
                    .append(" | label annotations = ")
                    .append(sds.getNLabels())
                    .append(" | description annotations = ")
                    .append(sds.getNDescriptions())
                    .append(
                            "\n_____________________________________________________\n");
        }
        return sb.toString();
    }

    /**
     * Returns a string containing dimension scale values of the provided
     * {@link H4Dimension}
     * 
     * @param dim
     * @throws HDFException
     */
    private String printDimensionScaleValues(H4Dimension dim)
            throws HDFException {
        final int datatype = dim.getDatatype();
        StringBuffer sb = new StringBuffer();
        sb.append("\nDimension Scale size = ").append(dim.getSize()).append(
                " datatype ").append(datatype);
        Object buf = dim.getDimensionScaleValues();
        if (buf == null) {
            sb.append("No values found\n");
            return sb.toString();
        }
        sb.append("\nDimension Scale values:\n");
        sb.append(H4Utilities.getValuesAsString(datatype, buf));
        sb.append("\n--------------------------------\n");
        return sb.toString();
    }

    private void warningMessage(String fileName) {
        StringBuffer sb = new StringBuffer(
                "WARNING!\n"
                        + fileName
                        + " Test files not available. Please refer to the source code to "
                        + "download requested sample data");
        LOGGER.info(sb.toString());
    }

    private void visualizeText(final String title, final String string) {
        JFrame frame = new JFrame("Testing " + title);
        frame.getContentPane().setLayout(new BorderLayout());

        // Sometime, we dont want to display image, only text data which
        // need to be placed to the start of the area.
        JTextArea textArea = new JTextArea();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        textArea.setText(string);
        textArea.setEditable(false);

        frame.getContentPane().add(textArea);
        frame.getContentPane().add(new JScrollPane(textArea));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(1024, 768);
        frame.setVisible(true);
    }
}
