package it.geosolutions.imageio.matfile5.sas;

import it.geosolutions.imageio.utilities.ImageIOUtilities;

import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferFloat;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;

import org.junit.Ignore;

import com.sun.imageio.plugins.common.ImageUtil;

public class DummyTest {

	@Ignore
	public void testMe() throws IOException {
		byte data [] = new byte[7500];
		for (int i=0;i<2500;i++)
			data[i]=(byte)i;
		
		SampleModel sm = new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, 50, 50,3,150,new int[]{0,1,2});
		
		ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), false, false,
		        ComponentColorModel.OPAQUE,DataBuffer.TYPE_BYTE);
		WritableRaster raster = Raster.createWritableRaster(sm,new DataBufferByte(data, 7500), null); 
		BufferedImage bi = new BufferedImage(cm, raster, false, null);
		ImageIOUtilities.visualize(bi);
		final AffineTransform transform= AffineTransform.getRotateInstance(0);// identity

        final AffineTransform transposeTransform= AffineTransform.getRotateInstance(0);// identity
        transposeTransform.preConcatenate(AffineTransform.getScaleInstance(1,1));
		transform.preConcatenate(transposeTransform);
		
	BufferedImage bi3 = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(bi,null);
	ImageIOUtilities.visualize(bi3);		
		
		
	}
	
    public static void main(String[] args) throws IOException {
        float data [] = new float[5000];
        for (int i=0;i<5000;i++)
            if (i%100 == 1)
                data[i]=80;
            else if (i<500)
                data[i]=0;
            else if (i>=500 && i<2500)
                data[i]=20000;
            else
                data[i]=120000;
            
        SampleModel sm = new PixelInterleavedSampleModel(DataBuffer.TYPE_FLOAT, 125, 20,1,250,new int[]{0,1});
        ColorModel cm = ImageUtil.createColorModel(sm);
        WritableRaster raster = Raster.createWritableRaster(sm,new DataBufferFloat(data, 5000), null); 
        BufferedImage bi = new BufferedImage(cm, raster, false, null);

        final AffineTransform transform= AffineTransform.getRotateInstance(0);// identity
        final AffineTransform transposeTransform=AffineTransform.getRotateInstance(0);// identity 
        transposeTransform.concatenate(AffineTransform.getRotateInstance(Math.PI*1.5d));
        transposeTransform.concatenate(AffineTransform.getTranslateInstance(-125,0));
        transposeTransform.concatenate(AffineTransform.getScaleInstance(1,-1));
        transposeTransform.concatenate(AffineTransform.getTranslateInstance(0,-20));
        transform.preConcatenate(transposeTransform);
                
       SASAffineTransformOp transformOp = new SASAffineTransformOp(transform,null);
       BufferedImage dst = transformOp.filter(bi, null);
   }
}
