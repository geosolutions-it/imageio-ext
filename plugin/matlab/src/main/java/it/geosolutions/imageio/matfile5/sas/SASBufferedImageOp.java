/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
 *    (C) 2007 - 2009, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.matfile5.sas;

import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.WritableRaster;

import javax.media.jai.RasterFactory;

/**
 * Custom {@link BufferedImageOp} that we use on SAS tiles to perform most of the operations we need in place!
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
class SASBufferedImageOp implements BufferedImageOp, RasterOp {

	private boolean computeLog;
	private RenderingHints hints = null;

	/**
	 * 
	 */
	public SASBufferedImageOp(final boolean computeLog,
            			      final RenderingHints hints) {
		this.computeLog=computeLog;
	    this.hints = hints;
	}

	/* (non-Javadoc)
	 * @see java.awt.image.BufferedImageOp#createCompatibleDestImage(java.awt.image.BufferedImage, java.awt.image.ColorModel)
	 */
	public BufferedImage createCompatibleDestImage(BufferedImage src,
			ColorModel dstCM) {
	    if (dstCM == null) dstCM = src.getColorModel();
	    WritableRaster wr = src.getRaster().createCompatibleWritableRaster();
	    return new BufferedImage(dstCM, wr, src.isAlphaPremultiplied(), null);
	}

	/* (non-Javadoc)
	 * @see java.awt.image.BufferedImageOp#filter(java.awt.image.BufferedImage, java.awt.image.BufferedImage)
	 */
	public BufferedImage filter(BufferedImage src, BufferedImage dst) {
	    // Required sanity checks
	    if (src.getSampleModel().getNumBands() != 2)
	      throw new IllegalArgumentException();
	    // create destination image if needed
	    if (dst == null) {
	    	final PixelInterleavedSampleModel sampleModel = 
	    		new PixelInterleavedSampleModel(src.getSampleModel().getDataType(), 
	    				src.getWidth(), src.getHeight(), 1, src.getWidth(), new int[] { 0 });
	    	final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
                final ComponentColorModel colorModel = RasterFactory.createComponentColorModel(DataBuffer.TYPE_DOUBLE, // dataType
                        cs, // color space
                        false, // has alpha
                        false, // is alphaPremultiplied
                        Transparency.OPAQUE); // transparency
                final WritableRaster raster = Raster.createWritableRaster(sampleModel,null);
                dst = new BufferedImage(colorModel,raster,false,null);
	    } else if (dst.getSampleModel().getNumBands() != 1)
			      throw new IllegalArgumentException();

	    WritableRaster wsrc = src.getRaster();
	    WritableRaster wdst = dst.getRaster();
	    filter(wsrc, wdst);
	    return dst;
	}

	/* (non-Javadoc)
	 * @see java.awt.image.BufferedImageOp#getBounds2D(java.awt.image.BufferedImage)
	 */
	public Rectangle2D getBounds2D(BufferedImage src) {
		return src.getData().getBounds();
	}

	/* (non-Javadoc)
	 * @see java.awt.image.BufferedImageOp#getPoint2D(java.awt.geom.Point2D, java.awt.geom.Point2D)
	 */
	public Point2D getPoint2D(Point2D src, Point2D dst) {
	    if (dst == null) 
	    	dst = (Point2D) src.clone();
	    else 
	    	dst.setLocation(src);
	    return dst;
	}

	/* (non-Javadoc)
	 * @see java.awt.image.BufferedImageOp#getRenderingHints()
	 */
	public RenderingHints getRenderingHints() {
		return hints;
	}

	/* (non-Javadoc)
	 * @see java.awt.image.RasterOp#createCompatibleDestRaster(java.awt.image.Raster)
	 */
	public WritableRaster createCompatibleDestRaster(Raster src) {		
		return src.createCompatibleWritableRaster();
	}

	/* (non-Javadoc)
	 * @see java.awt.image.RasterOp#filter(java.awt.image.Raster, java.awt.image.WritableRaster)
	 */
	public WritableRaster filter(Raster src, WritableRaster dest) {
	    if (dest == null) dest = src.createCompatibleWritableRaster();

	    // Required sanity checks
	    if (src.getNumBands() != 2)
	      throw new IllegalArgumentException();
	    if (dest.getNumBands() != 1)
		      throw new IllegalArgumentException();

	    double[] pixel = new double[2];
	    
	    final int minx=src.getMinX();
	    final int miny= src.getMinX();
	    final int maxx=src.getWidth()+minx;
	    final int maxy= src.getHeight()+miny;
	    for (int y = miny; y < maxy; y++)
	      for (int x = minx; x < maxx; x++)
	        {
	    	  // get the values
	          src.getPixel(x, y, pixel);
	          
	          // absolute value
	          final double magnitude=Math.sqrt(Math.pow(pixel[0],2)+Math.pow(pixel[1],2));
	          
	          // log?
	          final double value=computeLog?20*Math.log10(magnitude):magnitude;
	          dest.setSample(x, y, 0, value);
	        }
	    return dest;

	}

	/* (non-Javadoc)
	 * @see java.awt.image.RasterOp#getBounds2D(java.awt.image.Raster)
	 */
	public Rectangle2D getBounds2D(Raster arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
