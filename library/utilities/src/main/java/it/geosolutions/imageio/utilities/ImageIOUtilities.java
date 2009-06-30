/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *        https://imageio-ext.dev.java.net/
 *    (C) 2007 - 2008, GeoSolutions
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.media.jai.JAI;
import javax.media.jai.ROI;
import javax.media.jai.RenderedOp;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.sun.media.imageioimpl.common.ImageUtil;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi;
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

    private static void indent(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("  ");
        }
    }

    private static void displayMetadata(Node node, int level) {
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
    
    // Method to return JDK core ImageReaderSPI/ImageWriterSPI for a 
    // given formatName.
    public static List<ImageReaderWriterSpi> getJDKImageReaderWriterSPI(ServiceRegistry registry,
			String formatName, boolean isReader) {

		IIORegistry iioRegistry = (IIORegistry) registry;

		final Class<? extends ImageReaderWriterSpi> spiClass;
		final String descPart;
		if (isReader) {
			spiClass = ImageReaderSpi.class;
			descPart = " image reader";
		} else {
			spiClass = ImageWriterSpi.class;
			descPart = " image writer";
		}

		final Iterator<? extends ImageReaderWriterSpi> iter = iioRegistry.getServiceProviders(spiClass, true); // useOrdering

		String formatNames[];
		ImageReaderWriterSpi provider;
		String desc = "standard " + formatName + descPart;
		String jiioPath = "com.sun.media.imageioimpl";
		Locale locale = Locale.getDefault();
		ArrayList<ImageReaderWriterSpi> list = new ArrayList<ImageReaderWriterSpi>();
		while (iter.hasNext()) {
			provider = (ImageReaderWriterSpi) iter.next();

			// Look for JDK core ImageWriterSpi's
			if (provider.getVendorName().startsWith("Sun Microsystems")
					&& desc.equalsIgnoreCase(provider.getDescription(locale)) &&
					// not JAI Image I/O plugins
					!provider.getPluginClassName().startsWith(jiioPath)) {

				// Get the formatNames supported by this Spi
				formatNames = provider.getFormatNames();
				for (int i = 0; i < formatNames.length; i++) {
					if (formatNames[i].equalsIgnoreCase(formatName)) {
						// Must be a JDK provided ImageReader/ImageWriter
						list.add(provider);
						break;
					}
				}
			}
		}

		return list;
	}
    
    /**
     * Replace the original provider with name originalProviderName with the provider with name 
     * customProviderName for the class providerClass and for the provided format .
     * @param providerClass the {@link Class} for the providers.
     * @param customProviderName the name of the provider we want to use as new preferred provider.
     * @param originalProviderName the name of the provider we want to deregister.
     * @param format the format for this provi
     * @return <code>true</code> if we find both of the providers and the replacement succeed, <code>false</code> otherwise.
     */
    public static boolean replaceProvider(
    		final Class<? extends ImageReaderWriterSpi> providerClass,
    		final String customProviderName,
    		final String originalProviderName,
    		final String format){
		// now we need to set the order 
		final IIORegistry registry = IIORegistry.getDefaultInstance();
		ImageReaderWriterSpi standard = null,custom = null;

        for (final Iterator<? extends ImageReaderWriterSpi> it = registry.getServiceProviders(providerClass, false); it.hasNext();) {
            final ImageReaderWriterSpi provider = it.next();
            final String providerClassName=provider.getClass().getName();
            final String[] formats = provider.getFormatNames();
            for (int i=0; i<formats.length; i++) {
                if (formats[i].equalsIgnoreCase(format)) {
                    if (providerClassName.equalsIgnoreCase(originalProviderName)) {
                    	standard = provider;
                    } else 
                    	if (providerClassName.equalsIgnoreCase(customProviderName)) {
                        custom = provider;
                    }
                    break;
                }
            }
        }
        if (standard!=null && custom!=null) 
        {
        	if(ImageReaderSpi.class.isAssignableFrom(standard.getClass()))
        		return registry.setOrdering(ImageReaderSpi.class, (ImageReaderSpi)custom,(ImageReaderSpi) standard);	
        	else
        		return registry.setOrdering(ImageWriterSpi.class, (ImageWriterSpi)custom,(ImageWriterSpi) standard);	
        }

        //we did not find them
        return false;
	}    
}
