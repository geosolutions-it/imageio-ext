package it.geosolutions.imageio.plugins.jp2k;

import it.geosolutions.imageio.utilities.ImageIOUtilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import kdu_jni.KduException;

public class KakaduWrite {

    public static void main(String[] args) throws KduException,
            FileNotFoundException, IOException {

        final File file = new File("C://Work//data//kakadu//IM-0001-30023.bmp");

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

        final String fileName = "c:/kakaduoutput.jp2";

        final ImageWriter writer = new JP2KKakaduImageWriterSpi()
                .createWriterInstance();
        writer.setOutput(ImageIO.createImageOutputStream(new File(fileName)));

        JP2KKakaduImageWriteParam param = new JP2KKakaduImageWriteParam();
        param.setQuality(0.02);
        param.setWriteCodeStreamOnly(true);

        writer.write(null, new IIOImage(image, null, null), param);
        writer.dispose();

        reader = new JP2KKakaduImageReaderSpi().createReaderInstance();

        ImageReadParam readParam = new ImageReadParam();
        // readParam.setSourceSubsampling(8,8,0,0);
        pbjImageRead.setParameter("readParam", readParam);
        pbjImageRead.setParameter("reader", reader);
        pbjImageRead.setParameter("Input", new File(fileName));
        image = JAI.create("ImageRead", pbjImageRead);
        ImageIOUtilities.visualize(image);

    }

}
