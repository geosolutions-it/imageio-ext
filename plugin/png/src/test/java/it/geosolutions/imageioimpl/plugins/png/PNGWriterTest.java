package it.geosolutions.imageioimpl.plugins.png;

import it.geosolutions.imageio.plugins.png.PNGJWriter;
import it.geosolutions.resources.TestData;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.junit.Assert;

import ar.com.hjg.pngj.FilterType;

/**
 * Unit test for simple App.
 */
public class PNGWriterTest extends TestCase {

    public void testWriter(){
        PNGJWriter writer = new PNGJWriter();
        OutputStream out = null;
        try{
            
        // read test image
        BufferedImage read = ImageIO.read(TestData.file(this, "sample.jpeg"));
        
        File pngOut = TestData.temp(this, "test.png",true);
        out = new FileOutputStream(pngOut);
        
        writer.writePNG(read, out, 1, FilterType.FILTER_NONE);
        BufferedImage test = ImageIO.read(pngOut);
        Assert.assertNotNull(test);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(out!=null){
                try {
                    out.close();
                } catch (IOException e) {
                    
                }
                out=null;
            }
        }
    }
}
