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
package it.geosolutions.imageio.plugins.nitronitf;

import it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageWriteParam;
import it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageWriteParam.Compression;
import it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageWriter;
import it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageWriterSpi;
import it.geosolutions.imageio.plugins.nitronitf.NITFUtilities.WriteCompression;
import it.geosolutions.imageio.plugins.nitronitf.wrapper.HeaderWrapper;
import it.geosolutions.imageio.plugins.nitronitf.wrapper.ImageWrapper;
import it.geosolutions.imageio.plugins.nitronitf.wrapper.NITFProperties;
import it.geosolutions.imageio.plugins.nitronitf.wrapper.ShapeFileWrapper;
import it.geosolutions.imageio.plugins.nitronitf.wrapper.TextWrapper;
import it.geosolutions.imageio.plugins.nitronitf.wrapper.ImageWrapper.Category;
import it.geosolutions.imageio.plugins.nitronitf.wrapper.ImageWrapper.ImageBand;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;
import it.geosolutions.imageio.stream.output.FileImageOutputStreamExt;

import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.media.jai.operator.BandSelectDescriptor;

import nitf.BandInfo;
import nitf.BandSource;
import nitf.DESegment;
import nitf.Extensions;
import nitf.FileHeader;
import nitf.IOHandle;
import nitf.IOInterface;
import nitf.ImageSegment;
import nitf.ImageSource;
import nitf.ImageSubheader;
import nitf.MemorySource;
import nitf.NITFException;
import nitf.Record;
import nitf.SegmentSource;
import nitf.SegmentWriter;
import nitf.StreamIOWriteHandler;
import nitf.TRE;
import nitf.TextSegment;
import nitf.TextSubheader;
import nitf.Version;
import nitf.WriteHandler;
import nitf.Writer;

import org.apache.commons.io.FilenameUtils;

public class NITFImageWriter extends ImageWriter {

    private File outputFile;

    private static final Logger LOGGER = Logger.getLogger("it.geosolutions.imageio.plugins.nitronitf.NITFImageWriter");

    private final static JP2KKakaduImageWriterSpi KAKADU_SPI = new JP2KKakaduImageWriterSpi();

    private static final String JP2_TEMP_FOLDER;

    public static final String JP2_TEMP_FOLDER_PROPERTY = "nitf.imageio.jp2folder";

    private static final boolean DO_VALIDATION = true;

    static {
        String jp2TempFolder = System.getProperty(JP2_TEMP_FOLDER_PROPERTY);
        if (jp2TempFolder != null) {
            final File file = new File(jp2TempFolder);
            final boolean exist = file.exists();
            final boolean isDirectory = file.isDirectory();
            final boolean canWrite = file.canWrite();
            if (!exist || !isDirectory || !canWrite) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    final StringBuilder warningMessage = new StringBuilder(
                            "The specified folder can't be used as jp2 temporary folder: "
                                    + jp2TempFolder);
                    warningMessage.append(" since it ");
                    boolean comma = false;
                    if (!exist) {
                        warningMessage.append("doesn't exist");
                        comma = true;
                    }
                    if (!isDirectory) {
                        warningMessage.append(comma ? "," : "").append("isn't a directory");
                    }
                    if (!canWrite) {
                        warningMessage.append(comma ? "," : "").append("can't be written");
                    }
                    warningMessage.append("\nUsing default temp dir: ");
                    jp2TempFolder = System.getProperty("java.io.tmpdir");
                    warningMessage.append(jp2TempFolder);
                    LOGGER.log(Level.WARNING, warningMessage.toString());
                }
            }
        } else {
            jp2TempFolder = null;
        }
        JP2_TEMP_FOLDER = jp2TempFolder;
    }

    public NITFImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }

    public void setOutput(Object output) {
        if (output instanceof FileImageOutputStreamExt) {
            outputFile = ((FileImageOutputStreamExt) output).getFile();
        } else if (output instanceof File) {
            outputFile = (File) output;
        } else {
            throw new IllegalArgumentException("unsupported output type");
        }

    }

    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        throw new UnsupportedOperationException("getDefaultStreamMetadata not implemented yet");
    }

    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
        throw new UnsupportedOperationException("getDefaultImageMetadata not implemented yet");
    }

    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType,
            ImageWriteParam param) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Setup all the header fields taking them from the wrapper
     * 
     * @param record
     * @param headerWrapper
     * @throws NITFException
     */
    private static void initFileHeader(Record record, HeaderWrapper headerWrapper)
            throws NITFException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Populating file header");
        }
        final FileHeader header = record.getHeader();
        NITFUtilities.setField("FHDR", header.getFileHeader(), NITFUtilities.Consts.DEFAULT_FILE_HEADER);
        NITFUtilities.setField("FVER", header.getFileVersion(), NITFUtilities.Consts.DEFAULT_FILE_VERSION);
        NITFUtilities.setField("STYPE", header.getSystemType(), NITFUtilities.Consts.DEFAULT_SYSTEM_TYPE);
        if (headerWrapper != null) {
            NITFUtilities.setField("OSTAID", header.getOriginStationID(), headerWrapper.getOriginStationId());
            NITFUtilities.setField("FDT", header.getFileDateTime(), headerWrapper.getDateTime());
            NITFUtilities.setField("FTITLE", header.getFileTitle(), headerWrapper.getTitle());
            NITFUtilities.setField("FSCLAS", header.getClassification(), headerWrapper.getSecurityClassification());
            NITFUtilities.setField("FSCLSY", header.getSecurityGroup().getClassificationSystem(), headerWrapper.getSecurityClassificationSystem());
            NITFUtilities.setField("ENCRYP", header.getEncrypted(), Integer.toString(headerWrapper.getEncrypted()));
            NITFUtilities.setField("FBKGC", header.getBackgroundColor(), headerWrapper.getBackgroundColor());
            NITFUtilities.setField("ONAME", header.getOriginatorName(), headerWrapper.getOriginatorName());
            NITFUtilities.setField("OPHONE", header.getOriginatorPhone(), headerWrapper.getOriginatorPhone());
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "file header has been setup");
        }

        // Setting main header TREs if any
        Map<String, Map<String, String>> tresMap = headerWrapper.getTres();
        if (tresMap != null && !tresMap.isEmpty()) {
            Extensions extendedSection = header.getExtendedSection();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Populating Main Header TREs");
            }

            Set<String> keys = tresMap.keySet();
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String treName = it.next();
                Map<String, String> fieldsMapping = tresMap.get(treName);
                TRE tre = setTRE(treName, fieldsMapping);
                extendedSection.appendTRE(tre);
            }
        }
    }

    /**
     * Setup the ImageSegment
     * 
     * @param record
     * @param images
     * @param fis
     * @param compression
     * @return
     * @throws NITFException
     * @throws IOException
     */
    private static void addImageSegment(final Record record, final List<ImageWrapper> images,
            final FileImageInputStreamExt fis, final WriteCompression compression)
            throws NITFException, IOException {
        ImageSegment segment = null;
        ImageSubheader subheader = null;
        int img = 0;

        // Using a loop for future usage of the cloudCover image
        for (ImageWrapper image : images) {

            // Getting compression parameter and imageProperties
            WriteCompression writeCompression = img == 0 ? compression
                    : WriteCompression.UNCOMPRESSED;
            segment = record.newImageSegment();

            // Setting up the image Sub Header
            subheader = segment.getSubheader();
            double bpppb = initImageSubHeader(image, subheader, writeCompression, fis);

            if (img == 0) {
                initTREs(subheader, image, writeCompression, bpppb);
            }
            img++;

        }
    }

    /**
     * 
     * @param subheader
     * @param isSingleBand
     * @param compression
     * @param tresMap
     * @param bpppb
     * @throws NITFException
     */
    private static void initTREs(final ImageSubheader subheader, final ImageWrapper wrapper,
            final WriteCompression compression, final double bpppb) throws NITFException {
        Extensions extendedSection = subheader.getExtendedSection();
        final boolean isSingleBand = wrapper.getImage().getSampleModel().getNumBands() == 1;
        final Map<String, Map<String, String>> tresMap = wrapper.getTres();
        if (tresMap != null && !tresMap.isEmpty()) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Populating TRE");
            }

            Set<String> keys = tresMap.keySet();
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String treName = it.next();
                Map<String, String> fieldsMapping = tresMap.get(treName);
                TRE tre = setTRE(treName, fieldsMapping);
                extendedSection.appendTRE(tre);
            }
        }

        if (compression != WriteCompression.UNCOMPRESSED) {
            //Setting the J2KLRA TRE in case the image need to be jp2 compressed
            setJ2KLRA(extendedSection, compression, isSingleBand, bpppb);
        }

    }

    /**
     * Set the J2KLRA Tagged record extension containing information about quality layers and bit rates.
     * 
     * @param extendedSection
     * @param compression
     * @param isSingleBand
     * @param lastRate
     * @throws NITFException
     */
    private static void setJ2KLRA(final Extensions extendedSection,
            final WriteCompression compression, final boolean isSingleBand, final double lastRate)
            throws NITFException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Populating J2KLRA TRE");
        }

        // Initialize values
        final String compressionString = compression.toString();
        final String origData = compressionString.startsWith("NPJE") ? "0" : compressionString
                .startsWith("EPJE") ? "2" : "8";
        final boolean isVL = compression.getCompression() == Compression.LOSSY;
        final int nLayers = compression.getQualityLayers();
        final double bitRates[] = compression.getBitRates();

        Map<String, String> j2klraMapping = new LinkedHashMap<String, String>();
        j2klraMapping.put("ORIG", origData);
        j2klraMapping.put("NLEVELS_O", "5");
        j2klraMapping.put("NBANDS_O", isSingleBand ? "1" : "3");

        j2klraMapping.put("NLAYERS_O", String.valueOf(nLayers));
        for (int i = 0; i < nLayers; i++) {
            double rate = i != nLayers - 1 ? bitRates[i] : isVL ? bitRates[i] : (!Double
                    .isNaN(lastRate) ? lastRate : NITFUtilities.BPPPB[i - 1]);
            String bitrate = customFormat(rate);
            j2klraMapping.put("LAYER_ID[" + i + "]", String.valueOf(i));
            j2klraMapping.put("BITRATE[" + i + "]", bitrate);
        }
        extendedSection.appendTRE(setTRE("J2KLRA", j2klraMapping));
    }

    /**
     * 
     * @param imageWrapper
     * @param subheader
     * @param compression
     * @param fis
     * @return
     * @throws IOException
     * @throws NITFException
     */
    private static double initImageSubHeader(final ImageWrapper imageWrapper,
            final ImageSubheader subheader, final WriteCompression compression,
            final FileImageInputStreamExt fis) throws IOException, NITFException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Populating ImageSubHeader");
        }

        final RenderedImage ri = imageWrapper.getImage();
        final List<String> comments = imageWrapper.getComments();

        // Setting up rows, cols, blocks, bits properties
        final boolean isJP2 = compression != null && compression != WriteCompression.UNCOMPRESSED;
        final int nCols = ri.getWidth();
        final int nRows = ri.getHeight();
        final int nBits = ri.getSampleModel().getSampleSize(0);
        final String numBlocksPerRow = isJP2 ? String.valueOf((int) Math.ceil((double) nCols
                / NITFUtilities.DEFAULT_TILE_WIDTH)) : String.valueOf(1);
        final String numBlocksPerCol = isJP2 ? String.valueOf((int) Math.ceil((double) nRows
                / NITFUtilities.DEFAULT_TILE_HEIGHT)) : String.valueOf(1);
        final String numPixelsPerVertBlock = isJP2 ? String
                .valueOf(NITFUtilities.DEFAULT_TILE_HEIGHT) : "0000"; // As per specification
        final String numPixelsPerHorizBlock = isJP2 ? String
                .valueOf(NITFUtilities.DEFAULT_TILE_WIDTH) : "0000"; // As per specification

        double ratio = Double.NaN;

        NITFUtilities.setField("IM", subheader.getFilePartType(), "IM");
        NITFUtilities.setField("IID1", subheader.getImageId(), imageWrapper.getId());
        NITFUtilities.setField("IDATIM", subheader.getImageDateAndTime(), imageWrapper.getDateTime());
        NITFUtilities.setField("IID2", subheader.getImageTitle(), imageWrapper.getTitle());
        NITFUtilities.setField("ISCLAS", subheader.getImageSecurityClass(), imageWrapper.getSecurityClassification());
        NITFUtilities.setField("ISCLSY", subheader.getSecurityGroup().getClassificationSystem(), imageWrapper.getSecurityClassificationSystem());
        NITFUtilities.setField("ENCRYP", subheader.getEncrypted(), Integer.toString(imageWrapper.getEncrypted()));
        NITFUtilities.setField("ISORCE", subheader.getImageSource(), imageWrapper.getSource());
        NITFUtilities.setField("NROWS", subheader.getNumRows(), String.valueOf(nRows));
        NITFUtilities.setField("NCOLS", subheader.getNumCols(), String.valueOf(nCols));
        NITFUtilities.setField("PVTYPE", subheader.getPixelValueType(), NITFUtilities.Consts.DEFAULT_PVTYPE);
        NITFUtilities.setField("IREP", subheader.getImageRepresentation(), imageWrapper.getRepresentation().toString());
        NITFUtilities.setField("ICAT", subheader.getImageCategory(), imageWrapper.getImageCategory().toString());
        NITFUtilities.setField("ABPP", subheader.getActualBitsPerPixel(), Integer.toString(nBits));
        NITFUtilities.setField("PJUST", subheader.getPixelJustification(), imageWrapper.getPixelJustification());
        NITFUtilities.setField("ICORDS", subheader.getImageCoordinateSystem(), imageWrapper.getImageCoordinateSystem());
        NITFUtilities.setField("IGEOLO", subheader.getCornerCoordinates(), imageWrapper.getIgeolo());

        if (comments != null && !comments.isEmpty()) {
            int i = 0;
            for (String comment : comments) {
                subheader.insertImageComment(comment, i++);
            }
        }

        ratio = setImageCompression(subheader, compression, ri, fis);
        setImageBands(subheader, imageWrapper);

        NITFUtilities.setField("ISYNC", subheader.getImageSyncCode(), NITFUtilities.Consts.ZERO);
        NITFUtilities.setField("IMODE", subheader.getImageMode(), NITFUtilities.Consts.DEFAULT_IMODE);
        NITFUtilities.setField("NBPR", subheader.getNumBlocksPerRow(), numBlocksPerRow);
        NITFUtilities.setField("NBPC", subheader.getNumBlocksPerCol(), numBlocksPerCol);
        NITFUtilities.setField("NPPBH", subheader.getNumPixelsPerHorizBlock(), numPixelsPerHorizBlock);
        NITFUtilities.setField("NPPBV", subheader.getNumPixelsPerVertBlock(), numPixelsPerVertBlock);
        NITFUtilities.setField("NBPP", subheader.getNumBitsPerPixel(), Integer.toString(nBits));
        NITFUtilities.setField("IDLVL", subheader.getImageDisplayLevel(), NITFUtilities.Consts.ONE);
        NITFUtilities.setField("IALVL", subheader.getImageAttachmentLevel(), NITFUtilities.Consts.ZERO);
        NITFUtilities.setField("ILOC", subheader.getImageLocation(), NITFUtilities.Consts.ZERO);
        NITFUtilities.setField("IMAG", subheader.getImageMagnification(), imageWrapper.getImageMagnification());
        return ratio;

    }

    private static void setImageBands(final ImageSubheader subheader, final ImageWrapper imageWrapper) throws NITFException {
        BandInfo[] bandInfos = null;
        final ImageBand[] bands = imageWrapper.getBands();
        if (bands == null || bands.length == 0) {
            throw new IllegalArgumentException("ImageBands must be specified");
        }
        subheader.createBands(bands.length);
        bandInfos = subheader.getBandInfo();
        for (int i = 0; i < bandInfos.length; i++) {
            BandInfo bandInfo = bandInfos[i];
            NITFUtilities.setField("IREPBAND" + i, bandInfo.getRepresentation(), bands[i].getRepresentation());
            // ISUBCAT shouldn't be null for MultiSpectral Imagery
            NITFUtilities.setField("ISUBCAT" + i, bandInfo.getSubcategory(), bands[i].getSubCategory(), imageWrapper.getImageCategory() == Category.MS);
            NITFUtilities.setField("IFC" + i, bandInfo.getImageFilterCondition(), NITFUtilities.Consts.NONE);
            NITFUtilities.setField("NLUTS" + i, bandInfo.getNumLUTs(), NITFUtilities.Consts.ZERO);
        }
    }

    /**
     * Setup Image Compression related fields depending on the compression properties.
     * 
     * @param subheader the {@link ImageSubheader} to be set
     * @param compression the specified {@link WriteCompression}
     * @param isJP2 whether a JP2
     * @param ri the renderedImage
     * @param fis
     * @return
     * @throws IOException
     */
    private static double setImageCompression(final ImageSubheader subheader,
            final WriteCompression compression, final RenderedImage ri,
            final FileImageInputStreamExt fis) throws IOException {
        double ratio = Double.NaN;
        final int numBits = ri.getSampleModel().getSampleSize(0);
        if (compression != null && compression != WriteCompression.UNCOMPRESSED) {
            // Setting up compression type and compression ratio
            NITFUtilities.setField("IC", subheader.getImageCompression(), NITFUtilities.Consts.COMPRESSION_JP2);
            if (fis != null) {
                final long codeStreamSize = fis.length();
                final long imageSize = ri.getWidth() * ri.getHeight() * ri.getSampleModel().getNumBands();
                ratio = codeStreamSize / (double) (imageSize / (double) numBits);
            }
            String comrat = "";
            if (compression.getCompression() == Compression.NUMERICALLY_LOSSLESS) {
                String ratioString = Double.toString(ratio);
                ratioString = ratioString.replace(".", "");
                ratioString = ratioString.replace(",", "");
                ratioString = "N0" + ratioString.substring(0, 2); // ImproveMe
                comrat = ratioString;
            } else if (compression == WriteCompression.RATIO_15_1) {
                // Wait for NGA feedbacks to fix this value
                comrat = NITFUtilities.Consts.COMPRESSION_L005;
            } else if (compression.toString().endsWith("VL")) {
                // Visually lossless uses a static value
                comrat = NITFUtilities.Consts.COMPRESSION_V039;
            }
            NITFUtilities.setField("COMRAT", subheader.getCompressionRate(), comrat);
        } else {
            NITFUtilities.setField("IC", subheader.getImageCompression(), NITFUtilities.Consts.COMPRESSION_NONE);
        }
        return ratio;
    }

    /**
     * Set a new Tagged Record Extension on top of the specified fields map.
     * 
     * @param treName the name of the TRE to be setup
     * @param fieldsMap the map of fields <key,value> pairs
     * @return the populated TRE
     * @throws NITFException
     */
    private static TRE setTRE(String treName, Map<String, String> fieldsMap) throws NITFException {
        TRE tre = new TRE(treName);
        Set<String> keysSet = fieldsMap.keySet();
        Iterator<String> keys = keysSet.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = fieldsMap.get(key);
            if (key.contains("[") && key.contains("]")) {
                // Fields involved in LOOPS require special management
                // Getting the field from the TRE won't work, therefore
                // we need to directly set it
                NITFUtilities.setTREFieldDirect(tre, key, value);
            } else {
                NITFUtilities.setTREField(tre, key, value, true);
            }
        }
        return tre;
    }

    /**
     * Do the real write operation (writing images, texts, ...)
     * 
     * @param record
     * @param images
     * @param shp
     * @param fis
     * @param text
     * @return
     * @throws NITFException
     * @throws IOException
     */
    private boolean writeNITF(final Record record, final List<ImageWrapper> images,
            final ShapeFileWrapper shp, final FileImageInputStreamExt fis,
            final List<TextWrapper> texts) throws NITFException, IOException {
        final int numImages = images.size();
        ImageWrapper image = images.get(0);
        RenderedImage ri = image.getImage();
        WriteCompression compression = image.getCompression();
        int nBands = ri.getSampleModel().getNumBands();
        boolean written = false;
        Writer writer = new Writer();
        IOHandle handle = new IOHandle(outputFile.getCanonicalPath(), IOHandle.NITF_ACCESS_WRITEONLY, IOHandle.NITF_CREATE);

        byte[] shapeFileData = null;
        final boolean isJP2 = !(compression == WriteCompression.UNCOMPRESSED);
        if (shp != null) {
            shapeFileData = getShapeData(record, shp);
        }

        boolean prepared = false;
        if (isJP2) {
            // //
            //
            // get the JP2 Codestream previously written with Kakadu and transfer its content within
            // the NITF imageSegment
            //
            // //
            WriteHandler codeStream = null;
            IOInterface io;
            final int size = (int) fis.length();
            io = new IOFileInputStream(fis);

            writer.prepare(record, handle);

            if (shapeFileData != null) {
                writeData(shapeFileData, writer);
            }

            codeStream = new StreamIOWriteHandler(io, 0, size);
            writer.setImageWriteHandler(0, codeStream);
            prepared = true;

        }
        if (!isJP2 || numImages > 1) {

            if (!prepared) {
                writer.prepare(record, handle);
            }

            if (numImages == 1) {

                // setup a Writer
                if (shapeFileData != null) {
                    writeData(shapeFileData, writer);
                }

                ImageSource imageSource = new ImageSource();
                nitf.ImageWriter imageWriter = writer.getNewImageWriter(0);
                boolean[] successes = new boolean[nBands];
                final boolean isMono = images.get(0).getImage().getSampleModel().getNumBands() == 1;
                if (isMono) {
                    DataBufferByte dbb = (DataBufferByte) ri.getData().getDataBuffer();
                    BandSource bs = new MemorySource(dbb.getData(), dbb.getSize(), 0, 0, 0);
                    successes[0] = imageSource.addBand(bs);
                } else {
                    for (int i = 0; i < nBands; i++) {
                        RenderedImage band = BandSelectDescriptor.create(ri, new int[] { i }, null);
                        DataBufferByte dbb = (DataBufferByte) band.getData().getDataBuffer();
                        BandSource bs = new MemorySource(dbb.getData(), dbb.getSize(), 0, 0, 0);
                        successes[i] = imageSource.addBand(bs);
                    }
                }

                imageWriter.attachSource(imageSource);
            } else {
                ImageWrapper img = images.get(1);
                ri = img.getImage();
                nBands = ri.getSampleModel().getNumBands();
                ImageSource imageSource = new ImageSource();
                nitf.ImageWriter imageWriter2 = writer.getNewImageWriter(1);
                boolean[] successes = new boolean[nBands];
                DataBufferByte dbb = (DataBufferByte) ri.getData().getDataBuffer();
                BandSource bs = new MemorySource(dbb.getData(), dbb.getSize(), 0, 0, 0);
                successes[0] = imageSource.addBand(bs);
                imageWriter2.attachSource(imageSource);
            }

        }

        // Adding text
        if (texts != null && !texts.isEmpty()) {
            int i = 0;
            for (TextWrapper text : texts) {
                byte[] textContent = text.getTextContent();
                if (textContent != null) {
                    SegmentWriter textWriter = writer.getNewTextWriter(i++);
                    SegmentSource source = SegmentSource.makeSegmentMemorySource(textContent,
                            textContent.length, 0, 0);
                    textWriter.attachSource(source);
                }
            }
        }

        written = writer.write();
        if (handle != null) {
            handle.close();
        }

        return written;
    }

    /**
     * Encode a RenderedImage as a JP2K codestream on the specified outputFile, using the proper set of compression parameters.
     * 
     * @param outputFile
     * @param compression
     * @param ri
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void prepareJP2Image(final RenderedImage ri, final File outputFile,
            final WriteCompression compression) throws FileNotFoundException, IOException {
        JP2KKakaduImageWriter kakaduWriter = null;

        try {
            // TODO: Check PAN/MULTI can really be known from number of bands
            final int numBands = ri.getSampleModel().getNumBands();
            final boolean isMulti = numBands == 1 ? false : true;
            kakaduWriter = new JP2KKakaduImageWriter(KAKADU_SPI);
            kakaduWriter.setOutput(outputFile);
            JP2KKakaduImageWriteParam param = NITFUtilities.getCompressionParam(kakaduWriter,
                    compression, isMulti);

            kakaduWriter.write(null, new IIOImage(ri, null, null), param);
        } finally {
            if (kakaduWriter != null) {
                try {
                    kakaduWriter.dispose();
                } catch (Throwable t) {

                }
            }
        }
    }

    /**
     * Write data extension on a segment
     */
    private void writeData(byte[] fullData, Writer writer) throws NITFException {
        final SegmentWriter deWriter = writer.getNewDEWriter(0);
        final SegmentSource source = SegmentSource.makeSegmentMemorySource(fullData, fullData.length, 0, 0);
        deWriter.attachSource(source);
    }

    /**
     * 
     * @param record
     * @param shape
     * @return
     * @throws NITFException
     * @throws IOException
     */
    private byte[] getShapeData(Record record, ShapeFileWrapper shape) throws NITFException,
            IOException {
        final DESegment des = record.newDESegment();
        byte[] fullData = null;
        TRE csshpa = new TRE("CSSHPA");
        TRE tre = des.getSubheader().setSubheaderFields(csshpa);

        final byte[] bshp = shape.getShp();
        final byte[] bshx = shape.getShx();
        final byte[] bdbf = shape.getDbf();
        final int shp = shape.getShpLength();
        final int shx = shape.getShxLength();
        final int dbf = shape.getDbfLength();

        if (bshp == null || bshx == null || bdbf == null) {
            throw new NITFException("Unable to write CSSHPA ShapeFile");
        }

        fullData = new byte[shp + shx + dbf];
        System.arraycopy(bshp, 0, fullData, 0, shp);
        System.arraycopy(bshx, 0, fullData, shp, shx);
        System.arraycopy(bdbf, 0, fullData, shp + shx, dbf);

        tre.setField("SHAPE_USE", "IMAGE_SHAPE              ");
        NITFUtilities.setTREField(tre, "SHAPE_CLASS", "POLYGON   ", false);
        NITFUtilities.setTREField(tre, "SHAPE1_NAME", "SHP", false);
        NITFUtilities.setTREField(tre, "SHAPE1_START", NITFUtilities.Consts.ZERO, false);
        NITFUtilities.setTREField(tre, "SHAPE2_NAME", "SHX", false);
        NITFUtilities.setTREField(tre, "SHAPE2_START", String.valueOf(shp), false);
        NITFUtilities.setTREField(tre, "SHAPE3_NAME", "DBF", false);
        NITFUtilities.setTREField(tre, "SHAPE3_START", String.valueOf(shx + shp), false);

        NITFUtilities.setField("DE", des.getSubheader().getFilePartType(), "DE");
        NITFUtilities.setField("DESID", des.getSubheader().getTypeID(), "CSSHPA DES");
        NITFUtilities.setField("DESVER", des.getSubheader().getVersion(), "01");
        NITFUtilities.setField("DECLAS", des.getSubheader().getSecurityClass(), "U");
        NITFUtilities.setField("DESCLSY", des.getSubheader().getSecurityGroup().getClassificationSystem(), "US");

        return fullData;

    }

    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param)
            throws IOException {

        // Headers and segments initialization
        NITFImageWriteParam nitfParam = null;
        HeaderWrapper header = null;
        Map<String, Map<String, String>> extensionsMap = null;
        ShapeFileWrapper shape = null;
        List<TextWrapper> texts = null;
        WriteCompression compression = null;
        List<ImageWrapper> inputImages = null;
        if (param != null && param instanceof NITFImageWriteParam) {
            nitfParam = (NITFImageWriteParam) param;
            NITFProperties nitfMetadata = nitfParam.getNitfProperties();
            if (nitfMetadata != null) {
                header = nitfMetadata.getHeader();
                shape = nitfMetadata.getShape();
                texts = nitfMetadata.getTextsWrapper();
                inputImages = nitfMetadata.getImagesWrapper();
            }
            compression = nitfParam.getWriteCompression();

        }

        ImageWrapper imageW = inputImages.get(0);
        RenderedImage ri = imageW.getImage();
        final boolean isJP2 = (compression != null && compression != WriteCompression.UNCOMPRESSED);
        FileImageInputStreamExt jp2Stream = null;
        File tempFile = null;
        try {
            Record record = new Record(Version.NITF_21);
            if (isJP2) {
                // Proceeding with jp2 compression
                if (JP2_TEMP_FOLDER != null) {
                    tempFile = File.createTempFile("jp2compressed", ".jpc", new File(
                            JP2_TEMP_FOLDER));
                }
                String parentPath = outputFile.getParent();
                String name = FilenameUtils.getBaseName(outputFile.getCanonicalPath());
                tempFile = new File(parentPath + File.separatorChar + name + ".j2c");
                prepareJP2Image(ri, tempFile, compression);
                jp2Stream = new FileImageInputStreamExtImpl(tempFile);
            }

            // populating the file header
            initFileHeader(record, header);

            // adding an image segment to the record
            addImageSegment(record, inputImages, jp2Stream, compression);
            if (texts != null && !texts.isEmpty()) {
                // adding a text segment if present
                addTextSegment(record, texts);
            }

            if (!writeNITF(record, inputImages, shape, jp2Stream, texts)) {
                throw new IOException("Unable to successfully write");
            }
        } catch (Throwable t) {
            IOException ioe = new IOException();
            ioe.initCause(t);
            throw ioe;
        } finally {

            // Releasing resources
            if (jp2Stream != null) {
                try {
                    jp2Stream.close();
                } catch (Throwable thr) {
                    // Eat exception
                }
            }
            if (tempFile != null) {
                try {
                    tempFile.delete();
                } catch (Throwable thr) {

                }
            }

        }

        // record.destruct();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Successfully wrote NITF: " + outputFile);
        }
    }

    /**
     * Add a new text Segment to the record with the information provided by the {@link TextWrapper} instance
     * 
     * @param record
     * @param wrapper
     * @throws NITFException
     */
    private void addTextSegment(Record record, List<TextWrapper> texts) throws NITFException {
        if (texts != null && !texts.isEmpty()) {
            for (TextWrapper wrapper : texts) {
                TextSegment text = record.newTextSegment();
                TextSubheader textSubHeader = text.getSubheader();
                NITFUtilities.setField("TEXTID", textSubHeader.getTextID(), wrapper.getId());
                NITFUtilities.setField("TXTITL", textSubHeader.getTitle(), wrapper.getTitle());
                NITFUtilities.setField("TXTALVL", textSubHeader.getAttachmentLevel(), wrapper.getAttachmentLevel());
                NITFUtilities.setField("TSCLSY", textSubHeader.getSecurityGroup().getClassificationSystem(), wrapper.getSecurityClassificationSystem());
                NITFUtilities.setField("TXTDT", textSubHeader.getDateTime(), wrapper.getDateTime());
                NITFUtilities.setField("ENCRYP", textSubHeader.getEncrypted(), Integer.toString(wrapper.getEncrypted()));
                NITFUtilities.setField("TXTFMT", textSubHeader.getFormat(), wrapper.getFormat());
            }
        }
    }

    private static String customFormat(double value) {
        // Creating a new one since it isn't thread safe
        DecimalFormat myFormatter = new DecimalFormat("00.000000");
        return myFormatter.format(value).replace(",", ".");
    }
}
