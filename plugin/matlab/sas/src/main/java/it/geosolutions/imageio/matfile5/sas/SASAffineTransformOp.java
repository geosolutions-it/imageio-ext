package it.geosolutions.imageio.matfile5.sas;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.RasterOp;
import java.awt.image.WritableRaster;

import javax.media.jai.RasterFactory;

class SASAffineTransformOp implements BufferedImageOp, RasterOp {

    AffineTransform xform;
    private RenderingHints hints = null;
    
    
    public SASAffineTransformOp(AffineTransform xform,
            final RenderingHints hints) {
        this.hints = hints;
        this.xform = (AffineTransform)xform.clone();
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
    
    public Point2D mapSourcePoint(Point2D sourcePt) {
        if (sourcePt == null) {
            throw new IllegalArgumentException();
        }

        sourcePt.setLocation(sourcePt.getX() + 0.5, sourcePt.getY() + 0.5);

        Point2D dpt = xform.transform(sourcePt, null);
        dpt.setLocation(dpt.getX() - 0.5, dpt.getY() - 0.5);

        return dpt;
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
        
        final int width = src.getWidth();
        final int height = src.getHeight();
        final double[] pixel = new double[2];
        for (int i=0;i<height;i++){
            for (int j=0;j<width;j++){
                src.getDataElements(j, i, pixel);
                Point2D destPt = mapSourcePoint(new Point2D.Float(j, i));
                dst.setDataElements((int)destPt.getX(), (int)destPt.getY(), pixel);
            }
        }
        return dst;
    }

}
