package it.geosolutions.imageio.plugins.nitronitf;

import it.geosolutions.imageio.plugins.nitronitf.NITFImageWriteParam;
import it.geosolutions.imageio.plugins.nitronitf.NITFImageWriter;
import it.geosolutions.imageio.plugins.nitronitf.NITFImageWriterSpi;
import it.geosolutions.imageio.plugins.nitronitf.NITFUtilities;
import it.geosolutions.imageio.plugins.nitronitf.NITFUtilities.WriteCompression;
import it.geosolutions.imageio.plugins.nitronitf.wrapper.HeaderWrapper;
import it.geosolutions.imageio.plugins.nitronitf.wrapper.ImageWrapper;
import it.geosolutions.imageio.plugins.nitronitf.wrapper.NITFProperties;
import it.geosolutions.imageio.plugins.nitronitf.wrapper.TextWrapper;
import it.geosolutions.imageio.plugins.nitronitf.wrapper.ImageWrapper.Category;
import it.geosolutions.imageio.plugins.nitronitf.wrapper.ImageWrapper.ImageBand;
import it.geosolutions.imageio.plugins.nitronitf.wrapper.ImageWrapper.Representation;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.FileImageInputStream;

import nitf.NITFException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class WriterTest extends Assert {

    private final static String DEFAULT_IMAGE_SOURCE = "GeoSolutions";

    private static final String DATE_FORMAT_NOW = "yyyyMMddHHmmss";

    private static final String[] DECIMAL_DIGITS = new String[] { "", "0", "00", "000", "0000",
            "00000", "000000", "0000000" };

    private static final SimpleDateFormat SDF = new SimpleDateFormat(DATE_FORMAT_NOW);

    static {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new NITFImageWriterSpi());
    }

    @Test
    @Ignore
    /**
     * Simple writing code which won't be run. You can use it as a sample on how to 
     * setup some writing machinery.
     * 
     * @throws IOException
     * @throws NITFException
     */
    public void testNITFWrite() throws IOException, NITFException {
        // This TEST won't run, you can use it as a sample on how to setup some writing code
        if (!NITFUtilities.isNITFAvailable()) {
            System.out.println("NITF native libs aren't available: skipping tests");
            return;
        }

        final String inputFilePaths[] = new String[] { "/tmp/sampleForNitf.tif" };
        final String requestedCrs[] = new String[] { "EPSG:32638" };

        FileImageInputStream fisi = new FileImageInputStream(new File("/tmp/license.txt"));
        int length = (int) fisi.length();
        byte[] data = new byte[length];
        fisi.read(data);

//        final int j = 0;
//        String inputFilePath = inputFilePaths[0];
//
        // final GeoTiffReader gtReader = new GeoTiffReader(new File(inputFilePath));
        // final GridCoverage2D gridCoverage = gtReader.read(null);
        // final RenderedImage ri = gridCoverage.getRenderedImage();

        RenderedImage ri = null; // Image to be written
        final int numBands = ri.getSampleModel().getNumBands();

        // Geometry geom = null;
        // ShapeFileWrapper shpWrapper = buildShape(geom);
        LinkedHashMap<String, Map<String, String>> tresMap = new LinkedHashMap<String, Map<String, String>>();

        // //
        //
        // Populating TaggedRecordExtensions field with "FAKE" values,
        // for testing purposes
        //
        // //
        NITFImageWriterSpi SPI = new NITFImageWriterSpi();
        final WriteCompression[] compressions = new WriteCompression[] { WriteCompression.NPJE_NL,
                WriteCompression.EPJE_NL, WriteCompression.NPJE_VL, WriteCompression.EPJE_VL, };

        for (int w = 0; w < 4; w++) {

            final NITFImageWriter writer = new NITFImageWriter(SPI);
            final WriteCompression compression = compressions[w];
            String fileName = "/tmp/output_" + (numBands == 1 ? "PAN_" : "MULTI_")
                    + compression.toString() + "_jp2.ntf";
            File nitfFile = new File(fileName);

            HeaderWrapper header = setupDefaultHeaderWrapper();
            Calendar cal = Calendar.getInstance();
            String timeStamp = null;
            synchronized (SDF) {
                timeStamp = SDF.format(cal.getTime()); // not thread safe
            }
            header.setDateTime(timeStamp);
            List<TextWrapper> texts = new LinkedList<TextWrapper>();

            // Adding 3 text segments
            for (int i = 0; i < 3; i++) {
                TextWrapper text = setupDefaultNITFText();
                if (i > 0) {
                    text.setTitle("SAMPLE" + i);
                    text.setId("ID" + i);
                }
                text.setTextContent(data);
                texts.add(text);
            }
            header.setTitle(nitfFile.getName());

            NITFImageWriteParam param = new NITFImageWriteParam();
            NITFProperties metadata = new NITFProperties();
            param.setWriteCompression(compression);
            writer.setOutput(nitfFile);

            ImageWrapper image = new ImageWrapper();
            image.setImage(ri);
            image.setSource(DEFAULT_IMAGE_SOURCE);
            image.setTitle(numBands == 1 ? "SamplePanchromaticImagery.ntf"
                    : "SampleMultiSpectralImagery.NTF");
            image.setId(numBands == 1 ? "P100000000" : "M100000000");
            List<String> comments = new ArrayList<String>(5);
            comments.add("The imagery and metadata data has been added ");
            comments.add("for testing purposes");
            comments.add("This is a test comment");
            comments.add("made of 5 lines");
            comments.add("of text.");
            image.setComments(comments);
            image.setCompression(compression);
            image.setDateTime(timeStamp);
            image.setImageCoordinateSystem("G");
            image.setIgeolo("453131173651215453133773651210453133713650902453131113650907");
            image.setImageMagnification("1.0");
            final int nBands = ri.getSampleModel().getNumBands();
            image.setImageCategory(nBands > 1 ? Category.MS : Category.VIS);
            Representation r = nBands > 1 ? Representation.RGB : Representation.MONO;
            String rString = r.toString();
            image.setRepresentation(r);
            ImageBand[] imageBands = new ImageBand[nBands];
            if (nBands == 1) {
                imageBands[0] = new ImageBand("", "" + rString.charAt(0));
            } else {
                for (int i = 0; i < nBands; i++) {
                    imageBands[i] = new ImageBand("" + (rString.charAt(i)), ""
                            + (rString.charAt(i)));
                }
            }

            image.setBands(imageBands);
            List<ImageWrapper> imagesWrapper = new ArrayList<ImageWrapper>();

            imagesWrapper.add(image);

            metadata.setHeader(header);
            // metadata.setShape(shpWrapper);
            image.setTres(tresMap);
            metadata.setImagesWrapper(imagesWrapper);
            metadata.setTextsWrapper(texts);
            param.setNitfProperties(metadata);
            writer.write(null, new IIOImage(ri, null, null), param);

            writer.dispose();
            // gtReader.dispose();
        }

    }

    // static void writeShape(final String filePath, Geometry geometry) throws IOException {
    // // create feature type
    // final SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
    // b.setName("raster2vector");
    // b.setCRS(DEF_CRS);
    // b.add("the_geom", Polygon.class);
    // b.add("cat", Integer.class);
    // SimpleFeatureType type = b.buildFeatureType();
    // final SimpleFeatureCollection outGeodata = FeatureCollections.newCollection();
    //
    // // add features
    // final SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
    // final Object[] values = new Object[] { geometry, 0 };
    // builder.addAll(values);
    // final SimpleFeature feature = builder.buildFeature(type.getTypeName() + "." + 0);
    // outGeodata.add(feature);
    //
    // // create shapefile
    // writeShape(filePath, DEF_CRS, outGeodata);
    // }
    //
    // static void writeShape(final String filePath, final CoordinateReferenceSystem crs,
    // SimpleFeatureCollection data) throws IOException {
    // Utilities.ensureNonNull("String", filePath);
    // Utilities.ensureNonNull("file Path", data);
    //
    // final File file = new File(filePath);
    //
    // // Creating the schema
    // final DataStoreFactorySpi dataStoreFactory = new ShapefileDataStoreFactory();
    // SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
    // b.setName("raster2vector");
    // if (crs != null) {
    // b.setCRS(crs);
    // }
    // b.add("the_geom", Polygon.class);
    // b.add("cat", Integer.class);
    // SimpleFeatureType type = b.buildFeatureType();
    // final Map<String, Serializable> params = new HashMap<String, Serializable>();
    // params.put("url", DataUtilities.fileToURL(file));
    // params.put("create spatial index", Boolean.TRUE);
    // final ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory
    // .createNewDataStore(params);
    // newDataStore.createSchema(type);
    // if (crs != null)
    // newDataStore.forceSchemaCRS(crs);
    //
    // // Write the features to the shapefile
    // Transaction transaction = new DefaultTransaction("create");
    //
    // String typeName = newDataStore.getTypeNames()[0];
    // FeatureSource featureSource = newDataStore.getFeatureSource(typeName);
    //
    // if (featureSource instanceof FeatureStore) {
    // FeatureStore featureStore = (FeatureStore) featureSource;
    //
    // featureStore.setTransaction(transaction);
    // try {
    // featureStore.addFeatures(data);
    // transaction.commit();
    //
    // } catch (Exception problem) {
    // transaction.rollback();
    //
    // } finally {
    // transaction.close();
    // }
    // }
    //
    // try {
    // if (newDataStore != null)
    // newDataStore.dispose();
    // } catch (Throwable e) {
    //
    // }
    //
    // }
    //
    // private ShapeFileWrapper buildShape(Geometry footprint) throws NITFException, IOException {
    // if (footprint == null) {
    // return null;
    // }
    // final File shpFile = File.createTempFile("csshpa", ".shp");
    // final String shpFilePath = shpFile.getAbsolutePath();
    // writeShape(shpFilePath, footprint);
    //
    // byte[] bshp = new byte[2048];
    // byte[] bshx = new byte[2048];
    // byte[] bdbf = new byte[2048];
    // final String shpFilePrefix = shpFilePath.substring(0, shpFilePath.length() - 3);
    // final String dbfFilePath = shpFilePrefix + "dbf";
    // final String shxFilePath = shpFilePrefix + "shx";
    // final File shxFile = new File(shxFilePath);
    // final File dbfFile = new File(dbfFilePath);
    // if (!(shxFile.exists() && shxFile.canRead() && dbfFile.exists() && dbfFile.canRead()
    // && shpFile.exists() && shpFile.canRead())) {
    // throw new NITFException("Unable to write CSSHPA ShapeFile");
    // }
    //
    // FileInputStream fisSHP = new FileInputStream(shpFilePath);
    // FileInputStream fisSHX = new FileInputStream(shxFilePath);
    // FileInputStream fisDBF = new FileInputStream(dbfFilePath);
    //
    // try {
    // final int shp = fisSHP.read(bshp);
    // final int shx = fisSHX.read(bshx);
    // final int dbf = fisDBF.read(bdbf);
    // ShapeFileWrapper wrapper = new ShapeFileWrapper(bshp, bshx, bdbf, shp, shx, dbf);
    // return wrapper;
    // } finally {
    // if (fisSHP != null) {
    // IOUtils.closeQuietly(fisSHP);
    // }
    // if (fisSHX != null) {
    // IOUtils.closeQuietly(fisSHX);
    // }
    // if (fisDBF != null) {
    // IOUtils.closeQuietly(fisDBF);
    // }
    // if (!shpFile.delete()) {
    // FileUtils.deleteQuietly(shpFile);
    // }
    // if (!shxFile.delete()) {
    // FileUtils.deleteQuietly(shxFile);
    // }
    //
    // if (!dbfFile.delete()) {
    // FileUtils.deleteQuietly(dbfFile);
    // }
    // }
    //
    // }

    public static HeaderWrapper setupDefaultHeaderWrapper() {
        HeaderWrapper headerWrapper = new HeaderWrapper();
        headerWrapper.setOriginatorName("GeoSolutions");
        headerWrapper.setOriginatorPhone("+390584962313");
        headerWrapper.setOriginStationId("GS");
        headerWrapper.setBackgroundColor(new byte[] { 0x7e, 0x7e, 0x7e });
        return headerWrapper;
    }

    public static TextWrapper setupDefaultNITFText() {
        TextWrapper text = new TextWrapper();
        text.setId("License");
        text.setTitle("Sample License");
        text.setFormat("STA");
        text.setAttachmentLevel("000");
        text.setDateTime("20121212000000");
        return text;

    }

    /**
     * Check the provided UTM easting coordinates is within the valid range
     * 
     * @param corner
     * @param x
     * @return
     */
    private static boolean checkUtmEasting(final String corner, final double x) {
        final int floor = (int) Math.floor((x) + 0.5);
        if ((floor <= -100000) || (floor >= 1000000)) {
            throw new IllegalArgumentException("Unable to write UTM easting " + floor
                    + " for corner " + corner + "being outside of valid range.");
        }

        return true;
    }

    /**
     * Check the provided coordinates UTM Northing is within the valid range
     * 
     * @param corner
     * @param x
     * @return
     */
    private static boolean checkUtmNorthing(final String corner, final double y) {
        final int floor = (int) Math.floor((y) + 0.5);
        if ((floor <= -1000000) || (floor >= 10000000)) {
            throw new IllegalArgumentException("Unable to write UTM northing " + floor
                    + " for corner " + corner + "being outside of valid range.");
        }

        return true;
    }

    // /**
    // * Parse the imageSpecification and setup a proper IGEOLO field.
    // *
    // * @param retCoverage
    // * @param is
    // * @return
    // */
    // private static void setImageCoordinates(GridCoverage2D retCoverage, String requestedCRS,
    // ImageWrapper wrapper) {
    // double ulx, uly, urx, ury, lrx, lry, llx, lly;
    //
    // char iCord = ' ';
    // int utmZone = -1;
    //
    // if (requestedCRS.contains("4326")) {
    // iCord = 'G';
    // } else {
    // String code = requestedCRS.replace("EPSG:", "");
    // // TODO IMPROVE ME
    // if ((code.length() == 5) && (code.startsWith("326") || code.startsWith("327"))) {
    // utmZone = Integer.valueOf(code.substring(3));
    // if (code.startsWith("326")) {
    // iCord = 'N';
    // } else {
    // iCord = 'S';
    // }
    // }
    //
    // }
    //
    // final RenderedImage ri = retCoverage.getRenderedImage();
    // final int nRasterXSize = ri.getWidth();
    // final int nRasterYSize = ri.getHeight();
    // final GridGeometry2D gg = retCoverage.getGridGeometry();
    // final MathTransform mt = gg.getGridToCRS();
    //
    // if (mt instanceof AffineTransform2D) {
    // AffineTransform2D at = (AffineTransform2D) mt;
    // double m00 = at.getScaleX();
    // double m11 = at.getScaleY();
    // double m01 = at.getShearX();
    // double m10 = at.getShearY();
    // double m02 = at.getTranslateX();
    // double m12 = at.getTranslateY();
    //
    // ulx = m02; // + 0.5 * padfGeoTransform[1] + 0.5 * padfGeoTransform[2];
    // uly = m12; // + 0.5 * padfGeoTransform[4] + 0.5 * padfGeoTransform[5];
    // urx = ulx + (m00 * (nRasterXSize - 1));
    // ury = uly + (m10 * (nRasterXSize - 1));
    // lrx = ulx + (m00 * (nRasterXSize - 1)) + (m01 * (nRasterYSize - 1));
    // lry = uly + (m10 * (nRasterXSize - 1)) + (m11 * (nRasterYSize - 1));
    // llx = ulx + (m01 * (nRasterYSize - 1));
    // lly = uly + (m11 * (nRasterYSize - 1));
    //
    // setupIGEOLO(iCord, utmZone, ulx, uly, urx, ury, lrx, lry, llx, lly, wrapper);
    // }
    //
    // }
    //
    // private static void setupIGEOLO(final char iCords, final int nZone, final double ulx,
    // final double uly, final double urx, final double ury, final double lrx,
    // final double lry, final double llx, final double lly, final ImageWrapper wrapper) {
    //
    // String iCord = " ";
    // String iGeolo = "";
    // if (iCords == ' ') {
    //
    // }
    //
    // if ((iCords != 'G') && (iCords != 'N') && (iCords != 'S') && (iCords != 'D')) {
    // throw new IllegalArgumentException("Unsupported ICORD field" + iCords);
    // } else {
    // iCord = "" + iCords;
    // }
    //
    // if (iCords == 'G') {
    // if ((Math.abs(ulx) > 180) || (Math.abs(urx) > 180) || (Math.abs(lrx) > 180)
    // || (Math.abs(llx) > 180) || (Math.abs(uly) > 90) || (Math.abs(ury) > 90)
    // || (Math.abs(lry) > 90) || (Math.abs(lly) > 90)) {
    // throw new IllegalArgumentException(
    // "Unable to write IGEOLO due to geographic coordinates outside of valid range.");
    // }
    //
    // iGeolo = new StringBuilder().append(encodeDMScoordinates(uly, "Lat"))
    // .append(encodeDMScoordinates(ulx, "Long"))
    // .append(encodeDMScoordinates(ury, "Lat"))
    // .append(encodeDMScoordinates(urx, "Long"))
    // .append(encodeDMScoordinates(lry, "Lat"))
    // .append(encodeDMScoordinates(lrx, "Long"))
    // .append(encodeDMScoordinates(lly, "Lat"))
    // .append(encodeDMScoordinates(llx, "Long")).toString();
    // }
    // // ////////////////////////////
    // // UTM coordinates
    // // ////////////////////////////
    // else if ((iCords == 'N') || (iCords == 'S')) {
    // checkUtmEasting("ULX", ulx);
    // checkUtmNorthing("ULY", uly);
    // checkUtmEasting("URX", urx);
    // checkUtmNorthing("URY", ury);
    // checkUtmEasting("LRX", lrx);
    // checkUtmNorthing("LRY", lry);
    // checkUtmEasting("LLX", llx);
    // checkUtmNorthing("LLY", lly);
    //
    // String zone = integerFormat(nZone, 2);
    // String ulxS = integerFormat((int) Math.floor(ulx + 0.5), 6);
    // String ulyS = integerFormat((int) Math.floor(uly + 0.5), 7);
    // String urxS = integerFormat((int) Math.floor(urx + 0.5), 6);
    // String uryS = integerFormat((int) Math.floor(ury + 0.5), 7);
    // String lrxS = integerFormat((int) Math.floor(lrx + 0.5), 6);
    // String lryS = integerFormat((int) Math.floor(lry + 0.5), 7);
    // String llxS = integerFormat((int) Math.floor(llx + 0.5), 6);
    // String llyS = integerFormat((int) Math.floor(lly + 0.5), 7);
    //
    // iGeolo = new StringBuilder().append(zone).append(ulxS).append(ulyS).append(zone)
    // .append(urxS).append(uryS).append(zone).append(lrxS).append(lryS).append(zone)
    // .append(llxS).append(llyS).toString();
    // }
    //
    // wrapper.setImageCoordinateSystem(iCord);
    // wrapper.setIgeolo(iGeolo);
    // }
    //
    // private static String encodeDMScoordinates(double coord, final String axis) {
    // char hemisphere;
    // int degrees, minutes, seconds;
    //
    // if (axis.equalsIgnoreCase("Lat")) {
    // if (coord < 0.0) {
    // hemisphere = 'S';
    // } else {
    // hemisphere = 'N';
    // }
    // } else {
    // if (coord < 0.0) {
    // hemisphere = 'W';
    // } else {
    // hemisphere = 'E';
    // }
    // }
    //
    // coord = Math.abs(coord);
    //
    // degrees = (int) coord;
    // coord = (coord - degrees) * 60.0;
    //
    // minutes = (int) coord;
    // coord = (coord - minutes) * 60.0;
    //
    // seconds = (int) (coord + 0.5);
    // if (seconds == 60) {
    // seconds = 0;
    // minutes += 1;
    // if (minutes == 60) {
    // minutes = 0;
    // degrees += 1;
    // }
    // }
    //
    // String deg = null;
    // String min = integerFormat(minutes, 2);
    // String sec = integerFormat(seconds, 2);
    //
    // if (axis.equalsIgnoreCase("Lat")) {
    // deg = integerFormat(degrees, 2);
    // } else {
    // deg = integerFormat(degrees, 3);
    // }
    //
    // return deg + min + sec + hemisphere;
    //
    // }

    private static String integerFormat(final int number, final int digits) {
        // Using a new instance each time being not thread safe.
        DecimalFormat df = new DecimalFormat(DECIMAL_DIGITS[digits]);
        return df.format(number);
    }

}
