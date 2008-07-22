/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *        https://imageio-ext.dev.java.net/
 *    (C) 2007, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.utilities;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.ROI;
import javax.media.jai.RenderedOp;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.sun.media.imageioimpl.common.ImageUtil;
import com.sun.media.jai.codecimpl.util.RasterFactory;

/**
 * Simple class containing commonly used utility methods.
 */
public class ImageIOUtilities {

    private static final int DEFAULT_ROI = -999;

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

    private ImageIOUtilities() {

    }

    /**
     * Given a root node, print the values/attributes tree using the System Out
     * 
     * @TODO change it using Logger
     * @param root
     *                the root node to be printed.
     */
    public static void displayImageIOMetadata(Node root) {
        displayMetadata(root, 0);
    }

    static void indent(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("  ");
        }
    }

    static void displayMetadata(Node node, int level) {
        indent(level); // emit open tag
        System.out.print("<" + node.getNodeName());
        NamedNodeMap map = node.getAttributes();
        if (map != null) { // print attribute values
            int length = map.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = map.item(i);
                String attrValue = attr.getNodeValue();
                if (attrValue == null)
                    attrValue = "";
                System.out.print(" " + attr.getNodeName() + "=\""
                        + attrValue + "\"");
            }
        }
        System.out.print(">"); // close current tag
        String nodeValue = node.getNodeValue();
        if (nodeValue != null)
            System.out.println(" " + nodeValue);
        else
            System.out.println("");

        Node child = node.getFirstChild();
        if (child != null) {
            while (child != null) { // emit child tags recursively
                displayMetadata(child, level + 1);
                child = child.getNextSibling();
            }
            indent(level); // emit close tag
            System.out.println("</" + node.getNodeName() + ">");
        } else {
            // System.out.println("/>");
        }
    }

    /**
     * Visualize the image, after rescaling its values, given a threshold for
     * the ROI. This is useful to rescale an image coming from a source which
     * may contain noDataValues. The ROI threshold allows to set a minimal value
     * to be included in the computation of the future JAI extrema operation
     * used before the JAI rescale operation.
     * 
     * @param image
     *                RenderedImage to visualize
     * @param title
     *                title for the frame
     * @param roiThreshold
     *                the threshold for the inner ROI
     */
    private static void visualizeRescaled(RenderedImage image, String title,
            int roiThreshold) {

        ROI roi = new ROI(image, roiThreshold);
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(image); // The source image
        if (roi != null)
            pb.add(roi); // The region of the image to scan

        // Perform the extrema operation on the source image
        RenderedOp op = JAI.create("extrema", pb);

        // Retrieve both the maximum and minimum pixel value
        double[][] extrema = (double[][]) op.getProperty("extrema");

        final double[] scale = new double[] { (255) / (extrema[1][0] - extrema[0][0]) };
        final double[] offset = new double[] { ((255) * extrema[0][0])
                / (extrema[0][0] - extrema[1][0]) };

        // Preparing to rescaling values
        ParameterBlock pbRescale = new ParameterBlock();
        pbRescale.add(scale);
        pbRescale.add(offset);
        pbRescale.addSource(image);
        RenderedOp rescaledImage = JAI.create("Rescale", pbRescale);

        ParameterBlock pbConvert = new ParameterBlock();
        pbConvert.addSource(rescaledImage);
        pbConvert.add(DataBuffer.TYPE_BYTE);
        RenderedOp destImage = JAI.create("format", pbConvert);
        visualize(destImage, title);
    }

    /**
     * base method used to simply visualize RenderedImage without further
     * information
     * 
     * @param ri
     *                RenderedImage to visualize
     */
    public static void visualize(final RenderedImage ri) {
        visualize(ri, "");
    }

    /**
     * base method used to simply visualize RenderedImage
     * 
     * @param ri
     *                RenderedImage to visualize
     * @param title
     *                title for the frame (usually the image filename)
     */
    public static void visualize(final RenderedImage ri, String title) {
        final JFrame frame = new JFrame(title);
        frame.getContentPane().add(new ScrollingImagePanel(ri, 1024, 768));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.pack();
                frame.setSize(1024, 768);
                frame.setVisible(true);
            }
        });
    }

    /**
     * base method used to visualize RenderedImage by specifying the frame title
     * as well as the request for rescaling.
     * 
     * @param ri
     *                RenderedImage to visualize
     * @param title
     *                title for the frame (usually the image filename)
     * @param rescale
     *                if <code>true</code> the RenderedImage will be rescaled
     *                using a default value for the ROI.
     */
    public static void visualize(final RenderedImage ri, String title,
            final boolean rescale) {
        visualize(ri, title, rescale, DEFAULT_ROI);
    }

    /**
     * base method used to visualize RenderedImage by specifying the frame title
     * as well as the request for rescaling.
     * 
     * @param ri
     *                RenderedImage to visualize
     * @param title
     *                title for the frame (usually the image filename)
     * @param rescale
     *                if <code>true</code> the RenderedImage will be rescaled
     *                using a default value for the ROI.
     * @param roiThreshold
     *                the threshold for the inner ROI. It represent the minimum
     *                value of the pixels representing the region of interest.
     */
    public static void visualize(RenderedImage ri, String title,
            boolean rescale, int roiThreshold) {
        if (rescale == true)
            visualizeRescaled(ri, title, roiThreshold);
        else
            visualize(ri, title);
    }
}
