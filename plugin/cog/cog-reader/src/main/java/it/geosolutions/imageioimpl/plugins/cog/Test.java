package it.geosolutions.imageioimpl.plugins.cog;

import it.geosolutions.imageio.plugins.cog.CogImageReadParam;
import it.geosolutions.imageio.stream.input.spi.URLImageInputStreamSpi;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReader;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFImageReaderSpi;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author joshfix
 * Created on 2019-08-20
 */
public class Test {

    static long cogSum = 0;
    static List<Long> cogTimes = new ArrayList<>();
    static long tiffSum = 0;
    static List<Long> tiffTimes = new ArrayList<>();
    static CogImageReadParam param = new CogImageReadParam();
    static boolean saveFile = false;
    static String cogImageUrl = "https://s3-us-west-2.amazonaws.com/landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF";
    //static String cogImageUrl = "s3://landsat-pds/c1/L8/153/075/LC08_L1TP_153075_20190515_20190515_01_RT/LC08_L1TP_153075_20190515_20190515_01_RT_B2.TIF";
    //static String cogImageUrl = "wasb://destination@imageryproducts.blob.core.windows.net/1000004_2128820_2017-12-19_100c/1000004_2128820_2017-12-19_100c-6u03f6c6-d437da84facd6e6f187b8cb1a3e85cf4-zoneclip-20171219224236000.tif";
    static ImageInputStream cogStream = new CachingHttpCogImageInputStream(cogImageUrl);
    //static ImageInputStream cogStream = new HttpCogImageInputStream(cogImageUrl);

    static {
        int x = 1000;
        int y = 1000;
        int width = 2000;
        int height = 2000;
        param.setSourceRegion(new Rectangle(x, y, width, height));
        //param.setRangeReaderClass(AzureRangeReader.class);
        //param.setRangeReaderClass(S3RangeReader.class);
        param.setRangeReaderClass(HttpRangeReader.class);
    }

    public static void main(String... args) throws Exception {
        //headToHeadTest(5);
        //readCog();
        //readTiff();
        readCog(4);

    }

    public static void readCog(int numReads) throws Exception {
        for (int i = 0; i < numReads; i++) {
            System.out.println("Reading COG (" + i + ")");
            readCog();
        }
    }

    public static void readCog() throws Exception {
        cogStream = new CachingHttpCogImageInputStream(cogImageUrl);
        display(readCog(param));
    }

    public static void readTiff() throws Exception {
        display(readTiff(param));
    }


    public static void headToHeadTest(int count) throws Exception {
        for (int i = 0; i < count; i++) {
            readCog(param);
        }
        long cogAverage = cogSum / count;
        System.out.println("Average COG time: " + cogAverage);

        for (int i = 0; i < count; i++) {
            readTiff(param);
        }
        long tiffAverage = tiffSum / count;
        System.out.println("\nAverage TIFF time: " + tiffAverage + " - Average COG time: " + cogAverage);

        long diff = Math.abs(tiffAverage - cogAverage);
        String winner = tiffAverage < cogAverage ? "TIFF Reader" : "COG Reader";
        System.out.println("-------------------------------------------");
        System.out.println(winner + " won by " + diff + "ms");
    }

    public static BufferedImage readTiff(ImageReadParam param) throws Exception {
        Instant tiffStart = Instant.now();
        ImageInputStream urlStream = new URLImageInputStreamSpi().createInputStreamInstance(new URL(cogImageUrl));
        TIFFImageReader tiffReader = new TIFFImageReader(new TIFFImageReaderSpi());
        tiffReader.setInput(urlStream);
        BufferedImage tiffImage = tiffReader.read(0, param);
        Instant tiffEnd = Instant.now();
        long duration = Duration.between(tiffStart, tiffEnd).toMillis();
        System.out.println("Time for TIFFImageReader: " + duration + "ms");
        tiffTimes.add(duration);
        tiffSum += duration;
        return tiffImage;
    }

    public static BufferedImage readCog(ImageReadParam param) throws Exception {
        Instant cogStart = Instant.now();
        CogImageReader reader = new CogImageReader(new CogImageReaderSpi());
        reader.setInput(cogStream);
        BufferedImage cogImage = reader.read(0, param);
        Instant cogEnd = Instant.now();
        long duration = Duration.between(cogStart, cogEnd).toMillis();
        System.out.println("Time for COG: " + duration + "ms");
        cogTimes.add(duration);
        cogSum += duration;
        cogStream.seek(0);
        return cogImage;
    }
static int count = 0;
    public static void display(BufferedImage bi) throws Exception {
        if (count++ != 1) {
            return;
        }
        if (saveFile) {
            File outputfile = new File("example.png");
            ImageIO.write(bi, "png", outputfile);
        }

        JLabel picLabel = new JLabel(new ImageIcon(bi));
        picLabel.setVisible(true);
        picLabel.setSize(1000, 1000);
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        panel.add(picLabel);
        frame.add(panel);
        frame.setSize(1000, 1000);
        panel.setSize(1000, 1000);
        panel.setVisible(true);
        frame.setVisible(true);
    }
}
