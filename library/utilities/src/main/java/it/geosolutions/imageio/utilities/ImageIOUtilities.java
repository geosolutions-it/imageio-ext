package it.geosolutions.imageio.utilities;

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;

import javax.imageio.ImageReadParam;

import com.sun.media.imageioimpl.common.ImageUtil;
import com.sun.media.jai.codecimpl.util.RasterFactory;

public class ImageIOUtilities {

	/**
	 * Computes the source region of interest and the destination region of
	 * interest, taking the width and height of the source image, an optional
	 * destination image, and an optional <code>ImageReadParam</code> into
	 * account. The source region begins with the entire source image. Then that
	 * is clipped to the source region specified in the
	 * <code>ImageReadParam</code>, if one is specified.
	 * 
	 * <p>
	 * If either of the destination offsets are negative, the source region is
	 * clipped so that its top left will coincide with the top left of the
	 * destination image, taking subsampling into account. Then the result is
	 * clipped to the destination image on the right and bottom, if one is
	 * specified, taking subsampling and destination offsets into account.
	 * 
	 * <p>
	 * Similarly, the destination region begins with the source image, is
	 * translated to the destination offset given in the
	 * <code>ImageReadParam</code> if there is one, and finally is clipped to
	 * the destination image, if there is one.
	 * 
	 * <p>
	 * If either the source or destination regions end up having a width or
	 * height of 0, an <code>IllegalArgumentException</code> is thrown.
	 * 
	 * <p>
	 * The {@link #getSourceRegion <code>getSourceRegion</code>} method may be
	 * used if only source clipping is desired.
	 * 
	 * @param param
	 *                an <code>ImageReadParam</code>, or <code>null</code>.
	 * @param srcWidth
	 *                the width of the source image.
	 * @param srcHeight
	 *                the height of the source image.
	 * @param image
	 *                a <code>BufferedImage</code> that will be the
	 *                destination image, or <code>null</code>.
	 * @param srcRegion
	 *                a <code>Rectangle</code> that will be filled with the
	 *                source region of interest.
	 * @param destRegion
	 *                a <code>Rectangle</code> that will be filled with the
	 *                destination region of interest.
	 * @exception IllegalArgumentException
	 *                    if <code>srcRegion</code> is <code>null</code>.
	 * @exception IllegalArgumentException
	 *                    if <code>dstRegion</code> is <code>null</code>.
	 * @exception IllegalArgumentException
	 *                    if the resulting source or destination region is
	 *                    empty.
	 */
	protected static void computeRegions(ImageReadParam param, int srcWidth,
	        int srcHeight, BufferedImage image, Rectangle srcRegion,
	        Rectangle destRegion) {
	    if (srcRegion == null) {
	        throw new IllegalArgumentException("srcRegion == null!");
	    }
	    if (destRegion == null) {
	        throw new IllegalArgumentException("destRegion == null!");
	    }
	
	    // Start with the entire source image
	    srcRegion.setBounds(0, 0, srcWidth, srcHeight);
	
	    // Destination also starts with source image, as that is the
	    // maximum extent if there is no subsampling
	    destRegion.setBounds(0, 0, srcWidth, srcHeight);
	
	    // Clip that to the param region, if there is one
	    int periodX = 1;
	    int periodY = 1;
	    int gridX = 0;
	    int gridY = 0;
	    if (param != null) {
	        Rectangle paramSrcRegion = param.getSourceRegion();
	        if (paramSrcRegion != null) {
	            srcRegion.setBounds(srcRegion.intersection(paramSrcRegion));
	        }
	        periodX = param.getSourceXSubsampling();
	        periodY = param.getSourceYSubsampling();
	        gridX = param.getSubsamplingXOffset();
	        gridY = param.getSubsamplingYOffset();
	        srcRegion.translate(gridX, gridY);
	        srcRegion.width -= gridX;
	        srcRegion.height -= gridY;
	        destRegion.setLocation(param.getDestinationOffset());
	    }
	
	    // Now clip any negative destination offsets, i.e. clip
	    // to the top and left of the destination image
	    if (destRegion.x < 0) {
	        int delta = -destRegion.x * periodX;
	        srcRegion.x += delta;
	        srcRegion.width -= delta;
	        destRegion.x = 0;
	    }
	    if (destRegion.y < 0) {
	        int delta = -destRegion.y * periodY;
	        srcRegion.y += delta;
	        srcRegion.height -= delta;
	        destRegion.y = 0;
	    }
	
	    // Now clip the destination Region to the subsampled width and height
	    int subsampledWidth = (srcRegion.width + periodX - 1) / periodX;
	    int subsampledHeight = (srcRegion.height + periodY - 1) / periodY;
	    destRegion.width = subsampledWidth;
	    destRegion.height = subsampledHeight;
	
	    // Now clip that to right and bottom of the destination image,
	    // if there is one, taking subsampling into account
	    if (image != null) {
	        Rectangle destImageRect = new Rectangle(0, 0, image.getWidth(),
	                image.getHeight());
	        destRegion.setBounds(destRegion.intersection(destImageRect));
	        if (destRegion.isEmpty()) {
	            throw new IllegalArgumentException("Empty destination region!");
	        }
	
	        int deltaX = destRegion.x + subsampledWidth - image.getWidth();
	        if (deltaX > 0) {
	            srcRegion.width -= deltaX * periodX;
	        }
	        int deltaY = destRegion.y + subsampledHeight - image.getHeight();
	        if (deltaY > 0) {
	            srcRegion.height -= deltaY * periodY;
	        }
	    }
	    if (srcRegion.isEmpty() || destRegion.isEmpty()) {
	        throw new IllegalArgumentException("Empty region!");
	    }
	}

	/**
	 * Utility method returning a proper <code>ColorModel</code> given an
	 * input <code>SampleModel</code>
	 * 
	 * @param sm
	 *                The <code>SampleModel</code> for which we need to create
	 *                a compatible <code>ColorModel</code>.
	 * 
	 * @return the created <code>ColorModel</code>
	 */
	public static ColorModel getCompatibleColorModel(final SampleModel sm) {
	    final int nBands = sm.getNumBands();
	    final int bufferType = sm.getDataType();
	    ColorModel cm = null;
	    ColorSpace cs = null;
	    if (nBands > 1) {
	        // Number of Bands > 1.
	        // ImageUtil.createColorModel provides to Creates a
	        // ColorModel that may be used with the specified
	        // SampleModel
	        cm = ImageUtil.createColorModel(sm);
	
	    } else if ((bufferType == DataBuffer.TYPE_BYTE)
	            || (bufferType == DataBuffer.TYPE_USHORT)
	            || (bufferType == DataBuffer.TYPE_INT)
	            || (bufferType == DataBuffer.TYPE_FLOAT)
	            || (bufferType == DataBuffer.TYPE_DOUBLE)) {
	
	        // Just one band. Using the built-in Gray Scale Color Space
	        cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
	        cm = RasterFactory.createComponentColorModel(bufferType, // dataType
	                cs, // color space
	                false, // has alpha
	                false, // is alphaPremultiplied
	                Transparency.OPAQUE); // transparency
	    } else {
	        if (bufferType == DataBuffer.TYPE_SHORT) {
	            // Just one band. Using the built-in Gray Scale Color
	            // Space
	            cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
	            cm = new ComponentColorModel(cs, false, false,
	                    Transparency.OPAQUE, DataBuffer.TYPE_SHORT);
	        }
	    }
	    return cm;
	}

}
