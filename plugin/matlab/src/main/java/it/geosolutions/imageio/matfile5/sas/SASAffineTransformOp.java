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

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.RasterOp;
import java.awt.image.WritableRaster;

class SASAffineTransformOp implements BufferedImageOp, RasterOp {

    AffineTransform xform;
    AffineTransform inv_xform;
    private RenderingHints hints = null;
    
    
    public SASAffineTransformOp(AffineTransform xform,
            final RenderingHints hints) {
        this.hints = hints;
        this.xform = (AffineTransform)xform.clone();
        try {
            inv_xform = xform.createInverse();
        } catch (NoninvertibleTransformException e) {
            
        }
    }
    
    
    public BufferedImage filter (BufferedImage src, BufferedImage dst){
          // create destination image if needed
          if (dst == null) {
                  dst = createCompatibleDestImage(src, null);
          }
          WritableRaster wsrc = src.getRaster();
          WritableRaster wdst = dst.getRaster();
          filter(wsrc, wdst);
          return dst;
    }
    
    public Point2D mapSourcePoint(Point2D sourcePt, Point2D destPt) {
        if (sourcePt == null) {
            throw new IllegalArgumentException();
        }

        sourcePt.setLocation(sourcePt.getX() + 0.5, sourcePt.getY() + 0.5);
        Point2D dpt = xform.transform(sourcePt, destPt);
        sourcePt.setLocation(sourcePt.getX() - 0.5, sourcePt.getY() - 0.5);
        dpt.setLocation(dpt.getX() - 0.5, dpt.getY() - 0.5);

        return dpt;
    }
    
    /**
     * Computes the source point corresponding to the supplied point.
     *
     * @param destPt the position in destination image coordinates
     * to map to source image coordinates.
     *
     * @return a <code>Point2D</code> of the same class as
     * <code>destPt</code>.
     *
     * @throws IllegalArgumentException if <code>destPt</code> is
     * <code>null</code>.
     *
     * @since JAI 1.1.2
     */
    public Point2D mapDestPoint(Point2D destPt, Point2D srcPt) {
        if (destPt == null) {
            throw new IllegalArgumentException();
        }

        destPt.setLocation(destPt.getX() + 0.5, destPt.getY() + 0.5);

        Point2D sourcePt = inv_xform.transform(destPt, srcPt);
        destPt.setLocation(destPt.getX() - 0.5, destPt.getY() - 0.5);
        sourcePt.setLocation(sourcePt.getX() - 0.5, sourcePt.getY() - 0.5);

        return sourcePt;
    }

    
    public BufferedImage createCompatibleDestImage (BufferedImage src,
            ColorModel destCM, final Rectangle dstRegion) {
    	BufferedImage image;
        Rectangle r = getBounds2D(src).getBounds();

        // If r.x (or r.y) is < 0, then we want to only create an image 
        // that is in the positive range.
        // If r.x (or r.y) is > 0, then we need to create an image that
        // includes the translation.
        int w = r.x + r.width;
        int h = r.y + r.height;
        if (w <= 0) {
            throw new RasterFormatException("Transformed width ("+w+
                                            ") is less than or equal to 0.");
        }
        if (h <= 0) {
            throw new RasterFormatException("Transformed height ("+h+
                                            ") is less than or equal to 0.");
        }

        if (dstRegion != null){
        	w = dstRegion.width;
        	h = dstRegion.height;
        }

        if (destCM == null) {
            ColorModel cm = src.getColorModel();
                image = new BufferedImage(cm,
                          src.getRaster().createCompatibleWritableRaster(w,h),
                          cm.isAlphaPremultiplied(), null);
            
        }
        else {
            image = new BufferedImage(destCM,
                                    destCM.createCompatibleWritableRaster(w,h),
                                    destCM.isAlphaPremultiplied(), null);
        }

        return image;
    }
    
    /**
     * Creates a zeroed destination image with the correct size and number of
     * bands.  A <CODE>RasterFormatException</CODE> may be thrown if the 
     * transformed width or height is equal to 0.  
     * <p>
     * If <CODE>destCM</CODE> is null,
     * an appropriate <CODE>ColorModel</CODE> is used; this 
     * <CODE>ColorModel</CODE> may have
     * an alpha channel even if the source <CODE>ColorModel</CODE> is opaque.
     *
     * @param src  The <CODE>BufferedImage</CODE> to be transformed.
     * @param destCM  <CODE>ColorModel</CODE> of the destination.  If null,
     * an appropriate <CODE>ColorModel</CODE> is used.  
     *
     * @return The zeroed destination image.
     */
    public BufferedImage createCompatibleDestImage (BufferedImage src,
                                                    ColorModel destCM) {
        return createCompatibleDestImage(src, destCM, null);
    }
    
    public Rectangle2D getBounds2D (BufferedImage src) {
        return getBounds2D(src.getRaster());
    }
    
    public Rectangle2D getBounds2D (Raster src) {
        int w = src.getWidth();
        int h = src.getHeight();

        // Get the bounding box of the src and transform the corners
        float[] pts = {0, 0, w, 0, w, h, 0, h};
        xform.transform(pts, 0, pts, 0, 4);

        // Get the min, max of the dst
        float fmaxX = pts[0];
        float fmaxY = pts[1];
        float fminX = pts[0];
        float fminY = pts[1];
        for (int i=2; i < 8; i+=2) {
            if (pts[i] > fmaxX) {
                fmaxX = pts[i];
            }
            else if (pts[i] < fminX) {
                fminX = pts[i];
            }
            if (pts[i+1] > fmaxY) {
                fmaxY = pts[i+1];
            }
            else if (pts[i+1] < fminY) {
                fminY = pts[i+1];
            }
        }

        return new Rectangle2D.Float(fminX, fminY, fmaxX-fminX, fmaxY-fminY);
    }

    public Point2D getPoint2D(Point2D src, Point2D dst) {
        if (dst == null) 
            dst = (Point2D) src.clone();
        else 
            dst.setLocation(src);
        return dst;
    }


    public RenderingHints getRenderingHints() {
        return hints;
    }


    public WritableRaster createCompatibleDestRaster(Raster src) {
        return src.createCompatibleWritableRaster();
    }


    public WritableRaster filter(Raster src, WritableRaster dst) {
        if (dst == null) 
            dst = src.createCompatibleWritableRaster();

        // Required sanity checks
        if (src.getNumBands() != 2)
            throw new IllegalArgumentException();
        if (dst.getNumBands() != 2)
            throw new IllegalArgumentException();
        
        final int minX = src.getMinX();
        final int minY = src.getMinY();
        final int width = src.getWidth();
        final int height = src.getHeight();
        final int maxX = minX + width -1;
        final int maxY = minY + height -1;
        final int minXD = dst.getMinX();
        final int minYD = dst.getMinY();
        final int widthD = dst.getWidth();
        final int heightD = dst.getHeight();
        final int maxXD = minXD + widthD -1;
        final int maxYD = minYD + heightD -1;
        
        Object pixel = null;
        final int dataType = src.getSampleModel().getDataType();
        if (dataType == DataBuffer.TYPE_DOUBLE)
        	pixel = new double[2];
        else if (dataType == DataBuffer.TYPE_FLOAT)
        	pixel = new float[2];
        else if (dataType == DataBuffer.TYPE_INT)
        	pixel = new int[2];
        else
        	throw new IllegalArgumentException("Unsupported datatype");
        Point2D srcPt = new Point2D.Float(0, 0);
        Point2D destPt = new Point2D.Float(0, 0);
//        for (int i=minY;i<=maxY;i++){
//            for (int j=minX;j<=maxX;j++){
//                src.getDataElements(j, i, pixel);
//                srcPt.setLocation(j, i);
//                destPt = mapSourcePoint(srcPt, destPt);
//                final int nearestX = findNearest(destPt.getX(), minXD, maxXD);
//                final int nearestY = findNearest(destPt.getY(), minYD, maxYD);
//                dst.setDataElements(nearestX, nearestY, pixel);
//            }
//        }
        for (int i=minYD;i<=maxYD;i++){
            for (int j=minXD;j<=maxXD;j++){
                destPt.setLocation(j,i);

                srcPt = mapDestPoint(destPt,srcPt);
                final int nearestX = findNearest(srcPt.getX(), minX, maxX);
                final int nearestY = findNearest(srcPt.getY(), minY, maxY);
                src.getDataElements(nearestX, nearestY, pixel);
                dst.setDataElements(j, i, pixel);
            }
        }
        return dst;
    }

    private int findNearest(double a, int minA, int maxA) {
        int nearestA = (int) Math.round(a);
        if (nearestA < minA)
            nearestA = minA;
        else if (nearestA > maxA)
            nearestA = maxA;
        return nearestA;
    }

}
