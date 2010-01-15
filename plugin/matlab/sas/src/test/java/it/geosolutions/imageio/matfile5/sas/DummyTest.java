package it.geosolutions.imageio.matfile5.sas;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import org.junit.Test;

import com.sun.imageio.plugins.common.ImageUtil;

public class DummyTest {

	@Test
	public void testMe() {
		byte data [] = new byte[9];
		for (int i=0;i<9;i++)
			data[i]=(byte)i;
		
		SampleModel sm = new BandedSampleModel(DataBuffer.TYPE_BYTE, 3, 3, 1);
		ColorModel cm = ImageUtil.createColorModel(sm);
		WritableRaster raster = Raster.createWritableRaster(sm,new DataBufferByte(data, 9), null); 
		BufferedImage bi = new BufferedImage(cm, raster, false, null);
		
		
		final AffineTransform transform= AffineTransform.getRotateInstance(0);// identity

        // //
        //
        // Transposing the Matlab data matrix
        //
        // //
        final AffineTransform transposeTransform= AffineTransform.getRotateInstance(0);// identity
			// TransposeDescriptor.FLIP_ANTIDIAGONAL
        transposeTransform.preConcatenate(AffineTransform.getScaleInstance(1,1));
//        transposeTransform.preConcatenate(AffineTransform.getRotateInstance(Math.PI*1.5d));


		// preconcatenate transposition
		transform.preConcatenate(transposeTransform);
		
		
		//
		// apply the geometric transform
		//
		BufferedImage bi2 = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(bi,null);
		
		
		
		
		
	}
}
