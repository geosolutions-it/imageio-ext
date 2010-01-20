package it.geosolutions.imageio.matfile5.sas;

import java.awt.geom.AffineTransform;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferDouble;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;

import org.junit.Test;

import com.sun.imageio.plugins.common.ImageUtil;

public class DummyTest {

	@Test
    public void testAffine() throws IOException {
        final double data [][] = new double[2][2500];
        for (int j=0;j<2;j++)
        	for (int i=0;i<2500;i++)
	            if (i%100 == 1)
	                data[j][i]=80;
	            else if (i<500)
	                data[j][i]=0;
	            else if (i>=500 && i<1500)
	                data[j][i]=20000;
	            else
	                data[j][i]=120000;
            
        final BandedSampleModel sampleModel = new BandedSampleModel(DataBuffer.TYPE_DOUBLE, 125, 20, 2);
        ColorModel cm = ImageUtil.createColorModel(sampleModel);
        final DataBufferDouble dbb = new DataBufferDouble(data, 2500);
        WritableRaster raster = Raster.createWritableRaster(sampleModel, dbb, null); 
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
