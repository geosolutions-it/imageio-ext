package it.geosolutions.imageio.plugins.jp2k;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import kdu_jni.KduException;

import com.sun.media.imageio.plugins.jpeg2000.J2KImageWriteParam;
import com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriter;
import com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriterSpi;

public class JP2KKakaduWriteTest extends TestCase{
    
    public JP2KKakaduWriteTest(String name) {
        super(name);
    }
    
    private final static String testPath = "C://Work//data//kakadu//";
    
//    private final static String testPath = "C://";
    
    private final static String inputFileName = testPath + "prova.tif";
    
    private final static String outputFileName = testPath + "writtenImage";

    private final static String outputFileNameKakadu = outputFileName + "_Kakadu.jp2";

    private final static String outputFileNameImageIO= outputFileName + "_ImageIO.jp2";

    
    public static void testKakaduWriter() throws KduException,
            FileNotFoundException, IOException {

        final File file = new File(inputFileName);

        final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
                "ImageRead");
        ImageReader reader = ImageIO.getImageReaders(
                ImageIO.createImageInputStream(file)).next();

        pbjImageRead.setParameter("reader", reader);
        pbjImageRead.setParameter("Input", file);
        RenderedOp image = JAI.create("ImageRead", pbjImageRead);

        // BufferedImage readImage = ImageIO.read(new FileImageInputStream(
        // new File("C://Work//data//kakadu//12bit.jp2")));
        // KakaduUtilities.initializeKakaduMessagesManagement();

        final ImageWriter writer = new JP2KKakaduImageWriterSpi()
                .createWriterInstance();
        writer.setOutput(ImageIO.createImageOutputStream(new File(outputFileNameKakadu)));

        JP2KKakaduImageWriteParam param = new JP2KKakaduImageWriteParam();
        param.setQuality(0.2);
        param.setWriteCodeStreamOnly(true);

        writer.write(null, new IIOImage(image, null, null), param);
        writer.dispose();

        reader = new JP2KKakaduImageReaderSpi().createReaderInstance();

        ImageReadParam readParam = new ImageReadParam();
        // readParam.setSourceSubsampling(8,8,0,0);
        pbjImageRead.setParameter("readParam", readParam);
        pbjImageRead.setParameter("reader", reader);
        pbjImageRead.setParameter("Input", new File(outputFileNameKakadu));
        image = JAI.create("ImageRead", pbjImageRead);
//        ImageIOUtilities.visualize(image);

    }
    
    public static void testImageIOJP2KWriter() throws FileNotFoundException, IOException{
        BufferedImage imageToBeWritten = ImageIO.read(new
              FileImageInputStream(new File(inputFileName)));
              J2KImageWriter writer = new J2KImageWriter(new J2KImageWriterSpi());
              writer.setOutput(new FileImageOutputStream(new File(outputFileNameImageIO)));
              J2KImageWriteParam param = new J2KImageWriteParam();
              param.setWriteCodeStreamOnly(true);
//              param.setCompressionQuality(0.1f);
              writer.write(null, new IIOImage(imageToBeWritten,null,null),param);
             writer.dispose();
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        suite.addTest(new JP2KKakaduWriteTest("testKakaduWriter"));

//        suite.addTest(new JP2KKakaduWriteTest("testImageIOJP2KWriter"));

        return suite;
    }

}
