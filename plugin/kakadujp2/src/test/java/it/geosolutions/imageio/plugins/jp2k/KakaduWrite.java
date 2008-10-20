package it.geosolutions.imageio.plugins.jp2k;

import it.geosolutions.imageio.plugins.jp2k.box.ContiguousCodestreamBox;
import it.geosolutions.util.KakaduUtilities;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;

import kdu_jni.Jp2_family_tgt;
import kdu_jni.Jp2_output_box;
import kdu_jni.KduException;
import kdu_jni.Kdu_codestream;
import kdu_jni.Kdu_coords;
import kdu_jni.Kdu_dims;
import kdu_jni.Siz_params;

import com.sun.media.imageio.plugins.jpeg2000.J2KImageWriteParam;
import com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriter;
import com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageWriterSpi;

public class KakaduWrite {

    
    public static void main(String[] args) throws KduException, FileNotFoundException, IOException{
//        KakaduUtilities.initializeKakaduMessagesManagement();
//        Jp2_output_box outBox = new Jp2_output_box();
//        Jp2_family_tgt familyTarget = new Jp2_family_tgt();
//        familyTarget.Open("E:/miomio.jp2");
//        outBox.Open(familyTarget,(long)ContiguousCodestreamBox.BOX_TYPE);
//        Kdu_codestream codeStream = new Kdu_codestream();
//        Siz_params params = new Siz_params();
////        params = codeStream.Access_siz();
//        params.Set("Ssize",0,0,2);
//        params.Set("Ssize",0,1,2);
//        params.Set("Sprofile",0,0,2);
//        params.Set("Sorigin", 0,0,0);
//        params.Set("Sorigin", 0,1,0);
//        params.Set("Scomponents", 0, 0, 1);
//        params.Set("Sprecision", 0, 0, 8);
//        params.Set("Sdims",0,0,2);
//        params.Set("Sdims",0,1,2);
//        params.Set("Ssigned", 0,0,false);
//        params.Finalize_all();
//        codeStream.Create(params, outBox, null);
//        outBox.Write(4);
//        outBox.Close();
//        familyTarget.Close();

        BufferedImage imageToBeWritten = ImageIO.read(new FileImageInputStream(new File("E:/mono.bmp")));
        J2KImageWriter writer = new J2KImageWriter(new J2KImageWriterSpi());
        writer.setOutput(new FileImageOutputStream(new File("E:/mono.jp2")));
        J2KImageWriteParam param = new J2KImageWriteParam();
        param.setWriteCodeStreamOnly(true);
        writer.write(null, new IIOImage(imageToBeWritten,null,null),param);
        writer.dispose();
        
        
        
    }
}
