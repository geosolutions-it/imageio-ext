/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *        https://github.com/geosolutions-it/imageio-ext
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
package it.geosolutions.imageio.utilities;

import org.eclipse.imagen.ImageN;
import org.eclipse.imagen.NotAColorSpace;
import org.eclipse.imagen.OperationRegistry;
import org.eclipse.imagen.PlanarImage;
import org.eclipse.imagen.ROI;
import org.eclipse.imagen.RasterFactory;
import org.eclipse.imagen.RenderedOp;
import org.eclipse.imagen.media.imageread.ImageReadDescriptor;
import org.eclipse.imagen.media.viewer.RenderedImageBrowser;
import org.eclipse.imagen.registry.RIFRegistry;
import org.eclipse.imagen.registry.RenderedRegistryMode;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Simple class containing commonly used utility methods.
 */
@SuppressWarnings("deprecation")
public class ImageIOUtilities {

    private static boolean SKIP_EXTERNAL_FILES_LOOKUP = Boolean.getBoolean("it.geosolutions.skip.external.files.lookup");

    static final int MAX_SUBSAMPLING_FACTOR = Integer.MAX_VALUE;
	static final int MAX_LEVELS = 31;
	/**
	 * An array of strings containing only white spaces. Strings' lengths are
	 * equal to their index + 1 in the {@code spacesFactory} array. For example,
	 * {@code spacesFactory[4]} contains a string of length 5. Strings are
	 * constructed only when first needed.
	 */
	static final String[] spacesFactory = new String[20];

	private ImageIOUtilities() {

    }

    private static final int DEFAULT_ROI = -999;

    /**
     * Creates a <code>ColorModel</code> that may be used with the
     * specified <code>SampleModel</code>.  If a suitable
     * <code>ColorModel</code> cannot be found, this method returns
     * <code>null</code>.
     *
     * <p> Suitable <code>ColorModel</code>s are guaranteed to exist
     * for all instances of <code>ComponentSampleModel</code>.
     * For 1- and 3- banded <code>SampleModel</code>s, the returned
     * <code>ColorModel</code> will be opaque.  For 2- and 4-banded
     * <code>SampleModel</code>s, the output will use alpha transparency
     * which is not premultiplied.  1- and 2-banded data will use a
     * grayscale <code>ColorSpace</code>, and 3- and 4-banded data a sRGB
     * <code>ColorSpace</code>. Data with 5 or more bands will have a
     * <code>NotAColorSpace</code>.</p>
     *
     * <p>An instance of <code>DirectColorModel</code> will be created for
     * instances of <code>SinglePixelPackedSampleModel</code> with no more
     * than 4 bands.</p>
     *
     * <p>An instance of <code>IndexColorModel</code> will be created for
     * instances of <code>MultiPixelPackedSampleModel</code>. The colormap
     * will be a grayscale ramp with <code>1&nbsp;<<&nbsp;numberOfBits</code>
     * entries ranging from zero to at most 255.</p>
     *
     * @return An instance of <code>ColorModel</code> that is suitable for
     *         the supplied <code>SampleModel</code>, or <code>null</code>.
     *
     * @throws IllegalArgumentException  If <code>sampleModel</code> is
     *         <code>null</code>.
     */
     public static final ColorModel createColorModel(SampleModel sampleModel) {
         // Check the parameter.
         if (sampleModel == null) {
                 throw new IllegalArgumentException("sampleModel == null!");
         }
     
         // Get the data type.
         int dataType = sampleModel.getDataType();
         // Check the data type
         switch (dataType) {
            case DataBuffer.TYPE_BYTE:
            case DataBuffer.TYPE_USHORT:
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_INT:
            case DataBuffer.TYPE_FLOAT:
            case DataBuffer.TYPE_DOUBLE:
            	break;
            default:
             // Return null for other types.
            return null;
         }
     
         // The return variable.
         ColorModel colorModel = null;
     
         // Get the sample size.
         int[] sampleSize = sampleModel.getSampleSize();
     
         // Create a Component ColorModel.
         if (sampleModel instanceof  ComponentSampleModel) {
                 // Get the number of bands.
             int numBands = sampleModel.getNumBands();
     
             // Determine the color space.
             ColorSpace colorSpace = null;
             if (numBands <= 2) {
                 colorSpace = ColorSpace.getInstance(ColorSpace.CS_GRAY);
             } else if (numBands <= 4) {
                 colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
             } else {
                 colorSpace = new NotAColorSpace(numBands);
             }
     
             boolean hasAlpha = (numBands == 2) || (numBands == 4);
             boolean isAlphaPremultiplied = false;
             int transparency = hasAlpha ? Transparency.TRANSLUCENT : Transparency.OPAQUE;
             if (dataType == DataBuffer.TYPE_SHORT)
            	 colorModel = new ComponentColorModel(colorSpace, sampleSize, hasAlpha, isAlphaPremultiplied, transparency, dataType);
             else
            	 colorModel = RasterFactory.createComponentColorModel(dataType, colorSpace, hasAlpha,isAlphaPremultiplied, transparency);
         } else if (sampleModel.getNumBands() <= 4 && sampleModel instanceof SinglePixelPackedSampleModel) {
                 SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel) sampleModel;
             int[] bitMasks = sppsm.getBitMasks();
             int rmask = 0;
             int gmask = 0;
             int bmask = 0;
             int amask = 0;
  
             int numBands = bitMasks.length;
             if (numBands <= 2) {
                 rmask = gmask = bmask = bitMasks[0];
                 if (numBands == 2) {
                         amask = bitMasks[1];
                 }
             } else {
                 rmask = bitMasks[0];
                 gmask = bitMasks[1];
                 bmask = bitMasks[2];
                 if (numBands == 4) {
                         amask = bitMasks[3];
                 }
             }
     
             int bits = 0;
             for (int i = 0; i < sampleSize.length; i++) {
                 bits += sampleSize[i];
             }

             return new DirectColorModel(bits, rmask, gmask, bmask, amask);
  
         } else if (sampleModel instanceof  MultiPixelPackedSampleModel) {
                 // Load the colormap with a ramp.
             int bitsPerSample = sampleSize[0];
             int numEntries = 1 << bitsPerSample;
             byte[] map = new byte[numEntries];
             for (int i = 0; i < numEntries; i++) {
                 map[i] = (byte) (i * 255 / (numEntries - 1));
             }
                 colorModel = new IndexColorModel(bitsPerSample, numEntries, map, map, map);
         }
                     
         return colorModel;
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
     * to be included in the computation of the future ImageN extrema operation
     * used before the ImageN rescale operation.
     * 
     * @param image
     *                RenderedImage to visualize
     * @param title
     *                title for the frame
     * @param roiThreshold
     *                the threshold for the inner ROI
     */
    static void visualizeRescaled(RenderedImage image, String title,
            int roiThreshold) {

        ROI roi = new ROI(image, roiThreshold);
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(image); // The source image
        if (roi != null)
            pb.add(roi); // The region of the image to scan

        // Perform the extrema operation on the source image
        RenderedOp op = ImageN.create("extrema", pb);

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
        RenderedOp rescaledImage = ImageN.create("Rescale", pbRescale);

        ParameterBlock pbConvert = new ParameterBlock();
        pbConvert.addSource(rescaledImage);
        pbConvert.add(DataBuffer.TYPE_BYTE);
        RenderedOp destImage = ImageN.create("format", pbConvert);
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
        RenderedImageBrowser.showChain(ri, false, true, title, true);
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
					// not Image I/O plugins
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
     * Method to return core ImageReaderSPI/ImageWriterSPI for a given formatName.
     */
    public static List<ImageReaderWriterSpi> getImageReaderWriterSPI(ServiceRegistry registry,
            ServiceRegistry.Filter filter, String formatName, boolean isReader) {

        IIORegistry iioRegistry = (IIORegistry) registry;

        final Class<? extends ImageReaderWriterSpi> spiClass = isReader ? ImageReaderSpi.class : ImageWriterSpi.class;

        final Iterator<? extends ImageReaderWriterSpi> iter = iioRegistry
                .getServiceProviders(spiClass, filter, true); // useOrdering

        String formatNames[];
        ImageReaderWriterSpi provider;

        ArrayList<ImageReaderWriterSpi> list = new ArrayList<ImageReaderWriterSpi>();
        while (iter.hasNext()) {
            provider = (ImageReaderWriterSpi) iter.next();
            {
                // Get the formatNames supported by this Spi
                formatNames = provider.getFormatNames();
                for (int i = 0; i < formatNames.length; i++) {
                    if (formatNames[i].equalsIgnoreCase(formatName)) {
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
                }
            }
        }
        if (standard!=null && custom!=null){
            if(ImageReaderSpi.class.isAssignableFrom(standard.getClass()))
                return registry.setOrdering(ImageReaderSpi.class, (ImageReaderSpi)custom,(ImageReaderSpi) standard);
            else
                return registry.setOrdering(ImageWriterSpi.class, (ImageWriterSpi)custom,(ImageWriterSpi) standard);
        }

        //we did not find them
        return false;
	}

	/**
	 * Convenience method for testing two objects for equality. One or both
	 * objects may be null.
	 */
	public static boolean equals(final Object object1, final Object object2) {
	    return (object1 == object2)
	            || (object1 != null && object1.equals(object2));
	}

	/**
	 * Returns {@code true} if the two specified objects implements exactly the
	 * same set of interfaces. Only interfaces assignable to {@code base} are
	 * compared. Declaration order doesn't matter. For example in ISO 19111,
	 * different interfaces exist for different coordinate system geometries ({@code CartesianCS},
	 * {@code PolarCS}, etc.).
	 */
	public static boolean sameInterfaces(final Class<?> object1,
	        final Class<?> object2, final Class<?> base) {
	    if (object1 == object2) {
	        return true;
	    }
	    if (object1 == null || object2 == null) {
	        return false;
	    }
	    final Class<?>[] c1 = object1.getInterfaces();
	    final Class<?>[] c2 = object2.getInterfaces();
	    /*
	     * Trim all interfaces that are not assignable to 'base' in the 'c2'
	     * array. Doing this once will avoid to redo the same test many time in
	     * the inner loops j=[0..n].
	     */
	    int n = 0;
	    for (int i = 0; i < c2.length; i++) {
	        final Class<?> c = c2[i];
	        if (base.isAssignableFrom(c)) {
	            c2[n++] = c;
	        }
	    }
	    /*
	     * For each interface assignable to 'base' in the 'c1' array, check if
	     * this interface exists also in the 'c2' array. Order doesn't matter.
	     */
	    compare: for (int i = 0; i < c1.length; i++) {
	        final Class<?> c = c1[i];
	        if (base.isAssignableFrom(c)) {
	            for (int j = 0; j < n; j++) {
	                if (c.equals(c2[j])) {
	                    System.arraycopy(c2, j + 1, c2, j, --n - j);
	                    continue compare;
	                }
	            }
	            return false; // Interface not found in 'c2'.
	        }
	    }
	    return n == 0; // If n>0, at least one interface was not found in 'c1'.
	}

	/**
	 * Returns a string of the specified length filled with white spaces. This
	 * method tries to return a pre-allocated string if possible.
	 * 
	 * @param length
	 *                The string length. Negative values are clamped to 0.
	 * @return A string of length {@code length} filled with white spaces.
	 */
	public static String spaces(int length) {
	    // No need to synchronize. In the unlikely event of two threads
	    // calling this method at the same time and the two calls creating a
	    // new string, the String.intern() call will take care of
	    // canonicalizing the strings.
	    final int last = spacesFactory.length - 1;
	    if (length < 0)
	        length = 0;
	    if (length <= last) {
	        if (spacesFactory[length] == null) {
	            if (spacesFactory[last] == null) {
	                char[] blancs = new char[last];
	                Arrays.fill(blancs, ' ');
	                spacesFactory[last] = new String(blancs).intern();
	            }
	            spacesFactory[length] = spacesFactory[last]
	                    .substring(0, length).intern();
	        }
	        return spacesFactory[length];
	    } else {
	        char[] blancs = new char[length];
	        Arrays.fill(blancs, ' ');
	        return new String(blancs);
	    }
	}

	/**
	 * Retrieves the class of the native type for the specified {@link DataBuffer} type.
	 * 
	 * <p>
	 * Spits an {@link IllegalArgumentException} in case the data type is unknown.
	 * @param dataType {@link DataBuffer} type.
	 * @return the class of the native type for the specified {@link DataBuffer} type or an {@link IllegalArgumentException}.
	 */
	public static Class<?> classForDataBufferType(final int dataType){
		switch(dataType){
		case DataBuffer.TYPE_BYTE:
			return Byte.class;
		case DataBuffer.TYPE_SHORT:case DataBuffer.TYPE_USHORT:
			return Short.class;
		case DataBuffer.TYPE_INT:
			return Integer.class;
		case DataBuffer.TYPE_FLOAT:
			return Float.class;
		case DataBuffer.TYPE_DOUBLE:
			return Double.class;
		case DataBuffer.TYPE_UNDEFINED:
		default:
			throw new IllegalArgumentException("Wrong datatype:"+dataType);
			
			
		}
	}

	/**
	 * Returns a short class name for the specified class. This method will omit
	 * the package name. For example, it will return "String" instead of
	 * "java.lang.String" for a {@link String} object. It will also name array
	 * according Java language usage, for example "double[]" instead of "[D".
	 * 
	 * @param classe
	 *                The object class (may be {@code null}).
	 * @return A short class name for the specified object.
	 * 
	 * @todo Consider replacing by {@link Class#getSimpleName} when we will be
	 *       allowed to compile for J2SE 1.5.
	 */
	public static String getShortName(Class<?> classe) {
	    if (classe == null) {
	        return "<*>";
	    }
	    int dimension = 0;
	    Class<?> el;
	    while ((el = classe.getComponentType()) != null) {
	        classe = el;
	        dimension++;
	    }
	    String name = classe.getName();
	    final int lower = name.lastIndexOf('.');
	    final int upper = name.length();
	    name = name.substring(lower + 1, upper).replace('$', '.');
	    if (dimension != 0) {
	        StringBuffer buffer = new StringBuffer(name);
	        do {
	            buffer.append("[]");
	        } while (--dimension != 0);
	        name = buffer.toString();
	    }
	    return name;
	}

    /**
     * Takes a URL and converts it to a File. The attempts to deal with 
     * Windows UNC format specific problems, specifically files located
     * on network shares and different drives.
     * 
     * If the URL.getAuthority() returns null or is empty, then only the
     * url's path property is used to construct the file. Otherwise, the
     * authority is prefixed before the path.
     * 
     * It is assumed that url.getProtocol returns "file".
     * 
     * Authority is the drive or network share the file is located on.
     * Such as "C:", "E:", "\\fooServer"
     * 
     * @param url a URL object that uses protocol "file"
     * @return a File that corresponds to the URL's location
     */
    public static File urlToFile(URL url) {
        if (!"file".equals(url.getProtocol())) {
            return null; // not a File URL
        }
        String string = url.toExternalForm();
        if (string.contains("+")) {
            // this represents an invalid URL created using either
            // file.toURL(); or
            // file.toURI().toURL() on a specific version of Java 5 on Mac
            string = string.replace("+", "%2B");
        }
        try {
            string = URLDecoder.decode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not decode the URL to UTF-8 format", e);
        }
        
        String path3;
        String simplePrefix = "file:/";
        String standardPrefix = "file://";
        String os = System.getProperty("os.name");

        if (os.toUpperCase().contains("WINDOWS") && string.startsWith(standardPrefix)) {
            // win32: host/share reference
            path3 = string.substring(standardPrefix.length() - 2);
        } else if (string.startsWith(standardPrefix)) {
            path3 = string.substring(standardPrefix.length());
        } else if (string.startsWith(simplePrefix)) {
            path3 = string.substring(simplePrefix.length() - 1);
        } else {
            String auth = url.getAuthority();
            String path2 = url.getPath().replace("%20", " ");
            if (auth != null && !auth.equals("")) {
                path3 = "//" + auth + path2;
            } else {
                path3 = path2;
            }
        }

        return new File(path3);
    }

	/**
	 * Given a pair of xSubsamplingFactor (xSSF) and ySubsamplingFactor (ySFF), 
	 * look for a subsampling factor (SSF) in case xSSF != ySSF or they are not
	 * powers of 2.
	 * In case xSSF == ySSF == 2^N, the method return 0 (No optimal subsampling factor found).
	 * 
	 * @param xSubsamplingFactor
	 * @param ySubsamplingFactor
	 * @return 
	 */
	public static int getSubSamplingFactor2(final int xSubsamplingFactor, final int ySubsamplingFactor) {
	    boolean resamplingIsRequired = false;
	    int newSubSamplingFactor = 0;
	
	    // Preliminar check: Are xSSF and ySSF different?
	    final boolean subSamplingFactorsAreDifferent = (xSubsamplingFactor != ySubsamplingFactor);
	
	    // Let be nSSF the minimum of xSSF and ySSF (They may be equals).
	    newSubSamplingFactor = (xSubsamplingFactor <= ySubsamplingFactor) ? xSubsamplingFactor
	            : ySubsamplingFactor;
	    // if nSSF is greater than the maxSupportedSubSamplingFactor
	    // (MaxSupSSF), it needs to be adjusted.
	    final boolean changedSubSamplingFactors = (newSubSamplingFactor > MAX_SUBSAMPLING_FACTOR);
	    if (newSubSamplingFactor > MAX_SUBSAMPLING_FACTOR)
	        newSubSamplingFactor = MAX_SUBSAMPLING_FACTOR;
	    final int optimalSubsampling = findOptimalSubSampling(newSubSamplingFactor);
	
	    resamplingIsRequired = subSamplingFactorsAreDifferent
	            || changedSubSamplingFactors || optimalSubsampling != newSubSamplingFactor;
	    if (!resamplingIsRequired) {
	        // xSSF and ySSF are equal and they are not greater than MaxSuppSSF
	    	newSubSamplingFactor = 0;
	    } else {
	        // xSSF and ySSF are different or they are greater than MaxSuppSFF.
	        // We need to find a new subsampling factor to load a proper region.
	        newSubSamplingFactor = optimalSubsampling;
	    }
	    return newSubSamplingFactor;
	}

	static int findOptimalSubSampling(final int newSubSamplingFactor) {
	    int optimalSubSamplingFactor = 1;
	
	    // finding the available subsampling factors from the number of
	    // resolution levels
	    for (int level = 0; level < MAX_LEVELS; level++) {
	        // double the subSamplingFactor until it is lower than the
	        // input subSamplingFactor
	        if (optimalSubSamplingFactor < newSubSamplingFactor)
	            optimalSubSamplingFactor = 1 << level;
	        // if the calculated subSamplingFactor is greater than the input
	        // subSamplingFactor, we need to step back by halving it.
	        else if (optimalSubSamplingFactor > newSubSamplingFactor) {
	            optimalSubSamplingFactor = optimalSubSamplingFactor >> 1;
	            break;
	        } else if (optimalSubSamplingFactor == newSubSamplingFactor) {
	            break;
	        }
	    }
	    return optimalSubSamplingFactor;
	}

	/**
	 * Returns a short class name for the specified object. This method will
	 * omit the package name. For example, it will return "String" instead of
	 * "java.lang.String" for a {@link String} object.
	 * 
	 * @param object
	 *                The object (may be {@code null}).
	 * @return A short class name for the specified object.
	 */
	public static String getShortClassName(final Object object) {
	    return getShortName(object != null ? object.getClass() : null);
	}

	public static String adjustAttributeName(final String attributeName){
	    if (attributeName.contains("\\")){
	        return attributeName.replace("\\", "_");
	    }
	    return attributeName;
	}

	/**
	 * Allows or disallow native acceleration for the specified operation on the given ImageN instance.
	 * By default, ImageN uses hardware accelerated methods when available. For example, it make use of
	 * MMX instructions on Intel processors. Unluckily, some native method crash the Java Virtual
	 * Machine under some circumstances. For example on ImageN, the {@code "Affine"} operation on
	 * an image with float data type, bilinear interpolation and an {@link org.eclipse.imagen.ImageLayout}
	 * rendering hint cause an exception in medialib native code. Disabling the native acceleration
	 * (i.e using the pure Java version) is a convenient workaround until Sun fix the bug.
	 * <p>
	 * <strong>Implementation note:</strong> the current implementation assumes that factories for
	 * native implementations are declared in the {@code org.eclipse.imagen.media.mlib} package, while
	 * factories for pure java implementations are declared in the {@code org.eclipse.imagen.media.opimage}
	 * package. It work for Sun's 1.1.2 implementation, but may change in future versions. If this
	 * method doesn't recognize the package, it does nothing.
	 *
	 * @param operation The operation name (e.g. {@code "Affine"}).
	 * @param allowed {@code false} to disallow native acceleration.
	 * @param jai The instance of {@link JAI} we are going to work on. This argument can be
	 *        omitted for the {@linkplain JAI#getDefaultInstance default ImageN instance}.
	 *
	 * @see <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4906854">JAI bug report 4906854</a>
	 */
	public synchronized static void setNativeAccelerationAllowed(final String operation,
	                                                             final boolean  allowed,
	                                                             final ImageN jai)
	{
	    final String product = "org.eclipse.imagen.media";
	    final OperationRegistry registry = jai.getOperationRegistry();
	
	    // TODO: Check if we can remove SuppressWarnings with a future ImageN version.
	    @SuppressWarnings("unchecked")
	    final List<RenderedImageFactory> factories = registry.getOrderedFactoryList(
	            RenderedRegistryMode.MODE_NAME, operation, product);
	    if (factories != null) {
	        RenderedImageFactory   javaFactory = null;
	        RenderedImageFactory nativeFactory = null;
	        Boolean               currentState = null;
	        for (final RenderedImageFactory factory : factories) {
	            final String pack = factory.getClass().getPackage().getName();
	            if (pack.equals("org.eclipse.imagen.media.mlib")) {
	                nativeFactory = factory;
	                if (javaFactory != null) {
	                    currentState = Boolean.FALSE;
	                }
	            }
	            if (pack.equals("org.eclipse.imagen.media.opimage")) {
	                javaFactory = factory;
	                if (nativeFactory != null) {
	                    currentState = Boolean.TRUE;
	                }
	            }
	        }
	        if (currentState!=null && currentState.booleanValue()!=allowed) {
	            RIFRegistry.unsetPreference(registry, operation, product,
	                                        allowed ? javaFactory : nativeFactory,
	                                        allowed ? nativeFactory : javaFactory);
	            RIFRegistry.setPreference(registry, operation, product,
	                                      allowed ? nativeFactory : javaFactory,
	                                      allowed ? javaFactory : nativeFactory);
	        }
	    }
	}

	/**
	 * Allows or disallow native acceleration for the specified operation on the
	 * {@linkplain JAI#getDefaultInstance default ImageN instance}. This method is
	 * a shortcut for <code>{@linkplain #setNativeAccelerationAllowed(String,boolean,JAI)
	 * setNativeAccelerationAllowed}(operation, allowed, ImageN.getDefaultInstance())</code>.
	 *
	 * @see #setNativeAccelerationAllowed(String, boolean, JAI)
	 */
	public static void setNativeAccelerationAllowed(final String operation, final boolean allowed) {
	    setNativeAccelerationAllowed(operation, allowed, ImageN.getDefaultInstance());
	}

	public final static void checkNotNull (final Object checkMe, final String message){
		if (checkMe == null){
			throw new IllegalArgumentException(message != null ? message : "The provided object was NULL");
		}
	}    
	
	
    /**
     * Allow to dispose this image, as well as the related image sources.
     * 
     * @param rOp
     *            the image to be disposed.
     */
    public static void disposeImage(RenderedImage rOp) {
        if (rOp != null) {
            if (rOp instanceof RenderedOp) {
                RenderedOp renderedOp = (RenderedOp) rOp;

                final int nSources = renderedOp.getNumSources();
                if (nSources > 0) {
                    for (int k = 0; k < nSources; k++) {
                        Object source = null;
                        try {
                            source = renderedOp.getSourceObject(k);

                        } catch (ArrayIndexOutOfBoundsException e) {
                            // Ignore
                        }
                        if (source != null) {
                            if (source instanceof RenderedOp) {
                                disposeImage((RenderedOp) source);
                            } else if (source instanceof BufferedImage) {
                                ((BufferedImage) source).flush();
                                source = null;
                            }
                        }
                    }
                } else {
                    // get the reader
                    Object imageReader = rOp.getProperty(ImageReadDescriptor.PROPERTY_NAME_IMAGE_READER);
                    if (imageReader != null && imageReader instanceof ImageReader) {
                        final ImageReader reader = (ImageReader) imageReader;
                        final ImageInputStream stream = (ImageInputStream) reader.getInput();
                        try {
                            stream.close();
                        } catch (Throwable e) {
                            // swallow this
                        }
                        try {
                            reader.dispose();
                        } catch (Throwable e) {
                            // swallow this
                        }
                    }
                }
                final Object roi = rOp.getProperty("ROI");
                if (roi != null && (roi instanceof ROI || roi instanceof RenderedImage)) {
                    ROI roiImage = (ROI) roi;
                    PlanarImage image = roiImage.getAsImage();
                    if (image != null) {
                        image.dispose();
                        image = null;
                        roiImage = null;
                    }
                }

                if (rOp instanceof PlanarImage) {
                    ((PlanarImage) rOp).dispose();
                } else if (rOp instanceof BufferedImage) {
                    ((BufferedImage) rOp).flush();
                    rOp = null;
                }
            }
        }
    }

    public static boolean isSkipExternalFilesLookup() {
        return SKIP_EXTERNAL_FILES_LOOKUP;
    }

    /**
     * Returns a {@link ImageTypeSpecifier} usable for the given number of bands, and compatible
     * wtih a base image type (will reuse the data type and sample model width/height)
     */
    public static ImageTypeSpecifier getBandSelectedType(int numBands, SampleModel sm) {
        ColorModel cmDestination =
                new ComponentColorModel(
                        new NotAColorSpace(numBands),
                        false,
                        false,
                        Transparency.OPAQUE,
                        sm.getDataType());
        SampleModel smDestination = RasterFactory.createComponentSampleModel(sm,
                sm.getDataType(), sm.getWidth(), sm.getHeight(), numBands);
        return new ImageTypeSpecifier(cmDestination, smDestination);
    }

    /**
     * Convert an object to a string. If the object is an array of primitive types (byte[], int[], short[])
     * it will convert each element to a string and concatenate them with a space.
     * If the object is null it will return an empty string.
     * For other object types it will call the toString() method.
     */
    public static String convertObjectToString(Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj instanceof byte[]) {
            byte[] bArray = (byte[]) obj;
            StringBuilder sb = new StringBuilder();
            for (byte b : bArray) {
                sb.append(b).append(" ");
            }

            return sb.toString();
        } else if (obj instanceof int[]) {
            int[] iArray = (int[]) obj;
            StringBuilder sb = new StringBuilder();
            for (int i : iArray) {
                sb.append(i).append(" ");
            }
            return sb.toString();
        } else if (obj instanceof short[]) {
            short[] sArray = (short[]) obj;
            StringBuilder sb = new StringBuilder();
            for (short value : sArray) {
                sb.append(value).append(" ");
            }
            return sb.toString();
        }

        return obj.toString();
    }

    /**
     * Checks if the provided SampleModel is a binary one (1 bit per pixel, 1 band).
     */
    public static boolean isBinary(SampleModel sm) {
        return sm instanceof MultiPixelPackedSampleModel && ((MultiPixelPackedSampleModel)sm).getPixelBitStride() == 1 && sm.getNumBands() == 1;
    }

    /**
     * Checks if the provided image is contiguous in memory.
     * This is true for binary images, or for images with a ComponentSampleModel
     * with pixelStride equal to numBands, bandOffsets equal to {0,1,2,...}
     * and all bankIndices equal to 0.
     */
    public static final boolean imageIsContiguous(RenderedImage image) {
        SampleModel sm;
        if (image instanceof BufferedImage) {
            WritableRaster ras = ((BufferedImage)image).getRaster();
            sm = ras.getSampleModel();
        } else {
            sm = image.getSampleModel();
        }

        if (sm instanceof ComponentSampleModel) {
            ComponentSampleModel csm = (ComponentSampleModel)sm;
            if (csm.getPixelStride() != csm.getNumBands()) {
                return false;
            } else {
                int[] bandOffsets = csm.getBandOffsets();

                for(int i = 0; i < bandOffsets.length; ++i) {
                    if (bandOffsets[i] != i) {
                        return false;
                    }
                }

                int[] bankIndices = csm.getBankIndices();

                for(int i = 0; i < bandOffsets.length; ++i) {
                    if (bankIndices[i] != 0) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            return isBinary(sm);
        }
    }

    /**
     * Extracts packed binary data (1 bit per pixel) from a binary Raster into a byte array.
     * The data is packed with 8 pixels per byte, with the most significant bit
     * corresponding to the leftmost pixel.
     * <p>
     * If the Raster is not binary (1 bit per pixel, 1 band) an
     * IllegalArgumentException is thrown.
     */
    public static byte[] getPackedBinaryData(Raster raster, Rectangle rect) {
        SampleModel sm = raster.getSampleModel();
        if (!isBinary(sm)) {
            throw new IllegalArgumentException("Source image is not binary");
        } else {
            int rectX = rect.x;
            int rectY = rect.y;
            int rectWidth = rect.width;
            int rectHeight = rect.height;
            DataBuffer dataBuffer = raster.getDataBuffer();
            int dx = rectX - raster.getSampleModelTranslateX();
            int dy = rectY - raster.getSampleModelTranslateY();
            MultiPixelPackedSampleModel mpp = (MultiPixelPackedSampleModel)sm;
            int lineStride = mpp.getScanlineStride();
            int eltOffset = dataBuffer.getOffset() + mpp.getOffset(dx, dy);
            int bitOffset = mpp.getBitOffset(dx);
            int numBytesPerRow = (rectWidth + 7) / 8;
            if (dataBuffer instanceof DataBufferByte && eltOffset == 0 && bitOffset == 0 && numBytesPerRow == lineStride && ((DataBufferByte)dataBuffer).getData().length == numBytesPerRow * rectHeight) {
                return ((DataBufferByte)dataBuffer).getData();
            } else {
                byte[] binaryDataArray = new byte[numBytesPerRow * rectHeight];
                int b = 0;
                if (bitOffset == 0) {
                    if (dataBuffer instanceof DataBufferByte) {
                        byte[] data = ((DataBufferByte)dataBuffer).getData();
                        int stride = numBytesPerRow;
                        int offset = 0;

                        for(int y = 0; y < rectHeight; ++y) {
                            System.arraycopy(data, eltOffset, binaryDataArray, offset, stride);
                            offset += stride;
                            eltOffset += lineStride;
                        }
                    } else if (!(dataBuffer instanceof DataBufferShort) && !(dataBuffer instanceof DataBufferUShort)) {
                        if (dataBuffer instanceof DataBufferInt) {
                            int[] data = ((DataBufferInt)dataBuffer).getData();

                            for(int y = 0; y < rectHeight; ++y) {
                                int xRemaining = rectWidth;

                                int i;
                                for(i = eltOffset; xRemaining > 24; xRemaining -= 32) {
                                    int datum = data[i++];
                                    binaryDataArray[b++] = (byte)(datum >>> 24 & 255);
                                    binaryDataArray[b++] = (byte)(datum >>> 16 & 255);
                                    binaryDataArray[b++] = (byte)(datum >>> 8 & 255);
                                    binaryDataArray[b++] = (byte)(datum & 255);
                                }

                                for(int shift = 24; xRemaining > 0; xRemaining -= 8) {
                                    binaryDataArray[b++] = (byte)(data[i] >>> shift & 255);
                                    shift -= 8;
                                }

                                eltOffset += lineStride;
                            }
                        }
                    } else {
                        short[] data = dataBuffer instanceof DataBufferShort ? ((DataBufferShort)dataBuffer).getData() : ((DataBufferUShort)dataBuffer).getData();

                        for(int y = 0; y < rectHeight; ++y) {
                            int xRemaining = rectWidth;

                            int i;
                            for(i = eltOffset; xRemaining > 8; xRemaining -= 16) {
                                short datum = data[i++];
                                binaryDataArray[b++] = (byte)(datum >>> 8 & 255);
                                binaryDataArray[b++] = (byte)(datum & 255);
                            }

                            if (xRemaining > 0) {
                                binaryDataArray[b++] = (byte)(data[i] >>> 8 & 255);
                            }

                            eltOffset += lineStride;
                        }
                    }
                } else if (dataBuffer instanceof DataBufferByte) {
                    byte[] data = ((DataBufferByte)dataBuffer).getData();
                    if ((bitOffset & 7) == 0) {
                        int stride = numBytesPerRow;
                        int offset = 0;

                        for(int y = 0; y < rectHeight; ++y) {
                            System.arraycopy(data, eltOffset, binaryDataArray, offset, stride);
                            offset += stride;
                            eltOffset += lineStride;
                        }
                    } else {
                        int leftShift = bitOffset & 7;
                        int rightShift = 8 - leftShift;

                        for(int y = 0; y < rectHeight; ++y) {
                            int i = eltOffset;

                            for(int xRemaining = rectWidth; xRemaining > 0; xRemaining -= 8) {
                                if (xRemaining > rightShift) {
                                    binaryDataArray[b++] = (byte)((data[i++] & 255) << leftShift | (data[i] & 255) >>> rightShift);
                                } else {
                                    binaryDataArray[b++] = (byte)((data[i] & 255) << leftShift);
                                }
                            }

                            eltOffset += lineStride;
                        }
                    }
                } else if (!(dataBuffer instanceof DataBufferShort) && !(dataBuffer instanceof DataBufferUShort)) {
                    if (dataBuffer instanceof DataBufferInt) {
                        int[] data = ((DataBufferInt)dataBuffer).getData();

                        for(int y = 0; y < rectHeight; ++y) {
                            int bOffset = bitOffset;

                            for(int x = 0; x < rectWidth; bOffset += 8) {
                                int i = eltOffset + bOffset / 32;
                                int mod = bOffset % 32;
                                int left = data[i];
                                if (mod <= 24) {
                                    binaryDataArray[b++] = (byte)(left >>> 24 - mod);
                                } else {
                                    int delta = mod - 24;
                                    int right = data[i + 1];
                                    binaryDataArray[b++] = (byte)(left << delta | right >>> 32 - delta);
                                }

                                x += 8;
                            }

                            eltOffset += lineStride;
                        }
                    }
                } else {
                    short[] data = dataBuffer instanceof DataBufferShort ? ((DataBufferShort)dataBuffer).getData() : ((DataBufferUShort)dataBuffer).getData();

                    for(int y = 0; y < rectHeight; ++y) {
                        int bOffset = bitOffset;

                        for(int x = 0; x < rectWidth; bOffset += 8) {
                            int i = eltOffset + bOffset / 16;
                            int mod = bOffset % 16;
                            int left = data[i] & '\uffff';
                            if (mod <= 8) {
                                binaryDataArray[b++] = (byte)(left >>> 8 - mod);
                            } else {
                                int delta = mod - 8;
                                int right = data[i + 1] & '\uffff';
                                binaryDataArray[b++] = (byte)(left << delta | right >>> 16 - delta);
                            }

                            x += 8;
                        }

                        eltOffset += lineStride;
                    }
                }

                return binaryDataArray;
            }
        }
    }

    /**
     * Sets packed binary data (1 bit per pixel) into a binary WritableRaster from a byte array.
     * The data is packed with 8 pixels per byte, with the most significant bit
     * corresponding to the leftmost pixel.
     * <p>
     * If the Raster is not binary (1 bit per pixel, 1 band) an
     * IllegalArgumentException is thrown.
     */
    public static void setPackedBinaryData(byte[] binaryDataArray, WritableRaster raster, Rectangle rect) {
        SampleModel sm = raster.getSampleModel();
        if (!isBinary(sm)) {
            throw new IllegalArgumentException("Target raster is not binary");
        } else {
            int rectX = rect.x;
            int rectY = rect.y;
            int rectWidth = rect.width;
            int rectHeight = rect.height;
            DataBuffer dataBuffer = raster.getDataBuffer();
            int dx = rectX - raster.getSampleModelTranslateX();
            int dy = rectY - raster.getSampleModelTranslateY();
            MultiPixelPackedSampleModel mpp = (MultiPixelPackedSampleModel)sm;
            int lineStride = mpp.getScanlineStride();
            int eltOffset = dataBuffer.getOffset() + mpp.getOffset(dx, dy);
            int bitOffset = mpp.getBitOffset(dx);
            int b = 0;
            if (bitOffset == 0) {
                if (dataBuffer instanceof DataBufferByte) {
                    byte[] data = ((DataBufferByte)dataBuffer).getData();
                    if (data == binaryDataArray) {
                        return;
                    }

                    int stride = (rectWidth + 7) / 8;
                    int offset = 0;

                    for(int y = 0; y < rectHeight; ++y) {
                        System.arraycopy(binaryDataArray, offset, data, eltOffset, stride);
                        offset += stride;
                        eltOffset += lineStride;
                    }
                } else if (!(dataBuffer instanceof DataBufferShort) && !(dataBuffer instanceof DataBufferUShort)) {
                    if (dataBuffer instanceof DataBufferInt) {
                        int[] data = ((DataBufferInt)dataBuffer).getData();

                        for(int y = 0; y < rectHeight; ++y) {
                            int xRemaining = rectWidth;

                            int i;
                            for(i = eltOffset; xRemaining > 24; xRemaining -= 32) {
                                data[i++] = (binaryDataArray[b++] & 255) << 24 | (binaryDataArray[b++] & 255) << 16 | (binaryDataArray[b++] & 255) << 8 | binaryDataArray[b++] & 255;
                            }

                            for(int shift = 24; xRemaining > 0; xRemaining -= 8) {
                                data[i] |= (binaryDataArray[b++] & 255) << shift;
                                shift -= 8;
                            }

                            eltOffset += lineStride;
                        }
                    }
                } else {
                    short[] data = dataBuffer instanceof DataBufferShort ? ((DataBufferShort)dataBuffer).getData() : ((DataBufferUShort)dataBuffer).getData();

                    for(int y = 0; y < rectHeight; ++y) {
                        int xRemaining = rectWidth;

                        int i;
                        for(i = eltOffset; xRemaining > 8; xRemaining -= 16) {
                            data[i++] = (short)((binaryDataArray[b++] & 255) << 8 | binaryDataArray[b++] & 255);
                        }

                        if (xRemaining > 0) {
                            data[i++] = (short)((binaryDataArray[b++] & 255) << 8);
                        }

                        eltOffset += lineStride;
                    }
                }
            } else {
                int stride = (rectWidth + 7) / 8;
                int offset = 0;
                if (dataBuffer instanceof DataBufferByte) {
                    byte[] data = ((DataBufferByte)dataBuffer).getData();
                    if ((bitOffset & 7) == 0) {
                        for(int y = 0; y < rectHeight; ++y) {
                            System.arraycopy(binaryDataArray, offset, data, eltOffset, stride);
                            offset += stride;
                            eltOffset += lineStride;
                        }
                    } else {
                        int rightShift = bitOffset & 7;
                        int leftShift = 8 - rightShift;
                        int leftShift8 = 8 + leftShift;
                        int mask = (byte)(255 << leftShift);
                        int mask1 = (byte)(~mask);

                        for(int y = 0; y < rectHeight; ++y) {
                            int i = eltOffset;

                            for(int xRemaining = rectWidth; xRemaining > 0; xRemaining -= 8) {
                                byte datum = binaryDataArray[b++];
                                if (xRemaining > leftShift8) {
                                    data[i] = (byte)(data[i] & mask | (datum & 255) >>> rightShift);
                                    ++i;
                                    data[i] = (byte)((datum & 255) << leftShift);
                                } else if (xRemaining > leftShift) {
                                    data[i] = (byte)(data[i] & mask | (datum & 255) >>> rightShift);
                                    ++i;
                                    data[i] = (byte)(data[i] & mask1 | (datum & 255) << leftShift);
                                } else {
                                    int remainMask = (1 << leftShift - xRemaining) - 1;
                                    data[i] = (byte)(data[i] & (mask | remainMask) | (datum & 255) >>> rightShift & ~remainMask);
                                }
                            }

                            eltOffset += lineStride;
                        }
                    }
                } else if (!(dataBuffer instanceof DataBufferShort) && !(dataBuffer instanceof DataBufferUShort)) {
                    if (dataBuffer instanceof DataBufferInt) {
                        int[] data = ((DataBufferInt)dataBuffer).getData();
                        int rightShift = bitOffset & 7;
                        int leftShift = 8 - rightShift;
                        int leftShift32 = 32 + leftShift;
                        int mask = -1 << leftShift;
                        int mask1 = ~mask;

                        for(int y = 0; y < rectHeight; ++y) {
                            int bOffset = bitOffset;
                            int xRemaining = rectWidth;

                            for(int x = 0; x < rectWidth; xRemaining -= 8) {
                                int i = eltOffset + (bOffset >> 5);
                                int mod = bOffset & 31;
                                int datum = binaryDataArray[b++] & 255;
                                if (mod <= 24) {
                                    int shift = 24 - mod;
                                    if (xRemaining < 8) {
                                        datum &= 255 << 8 - xRemaining;
                                    }

                                    data[i] = data[i] & ~(255 << shift) | datum << shift;
                                } else if (xRemaining > leftShift32) {
                                    data[i] = data[i] & mask | datum >>> rightShift;
                                    ++i;
                                    data[i] = datum << leftShift;
                                } else if (xRemaining > leftShift) {
                                    data[i] = data[i] & mask | datum >>> rightShift;
                                    ++i;
                                    data[i] = data[i] & mask1 | datum << leftShift;
                                } else {
                                    int remainMask = (1 << leftShift - xRemaining) - 1;
                                    data[i] = data[i] & (mask | remainMask) | datum >>> rightShift & ~remainMask;
                                }

                                x += 8;
                                bOffset += 8;
                            }

                            eltOffset += lineStride;
                        }
                    }
                } else {
                    short[] data = dataBuffer instanceof DataBufferShort ? ((DataBufferShort)dataBuffer).getData() : ((DataBufferUShort)dataBuffer).getData();
                    int rightShift = bitOffset & 7;
                    int leftShift = 8 - rightShift;
                    int leftShift16 = 16 + leftShift;
                    int mask = (short)(~(255 << leftShift));
                    int mask1 = (short)('\uffff' << leftShift);
                    int mask2 = (short)(~mask1);

                    for(int y = 0; y < rectHeight; ++y) {
                        int bOffset = bitOffset;
                        int xRemaining = rectWidth;

                        for(int x = 0; x < rectWidth; xRemaining -= 8) {
                            int i = eltOffset + (bOffset >> 4);
                            int mod = bOffset & 15;
                            int datum = binaryDataArray[b++] & 255;
                            if (mod <= 8) {
                                if (xRemaining < 8) {
                                    datum &= 255 << 8 - xRemaining;
                                }

                                data[i] = (short)(data[i] & mask | datum << leftShift);
                            } else if (xRemaining > leftShift16) {
                                data[i] = (short)(data[i] & mask1 | datum >>> rightShift & '\uffff');
                                ++i;
                                data[i] = (short)(datum << leftShift & '\uffff');
                            } else if (xRemaining > leftShift) {
                                data[i] = (short)(data[i] & mask1 | datum >>> rightShift & '\uffff');
                                ++i;
                                data[i] = (short)(data[i] & mask2 | datum << leftShift & '\uffff');
                            } else {
                                int remainMask = (1 << leftShift - xRemaining) - 1;
                                data[i] = (short)(data[i] & (mask1 | remainMask) | datum >>> rightShift & '\uffff' & ~remainMask);
                            }

                            x += 8;
                            bOffset += 8;
                        }

                        eltOffset += lineStride;
                    }
                }
            }

        }
    }

    /**
     * Returns an ImageTypeSpecifier to be used as the destination type for an image read operation.
     * <p>
     * If the provided ImageReadParam contains a destination type, it is checked against the provided
     * imageTypes iterator. If it is compatible with one of the types in the iterator,
     * it is returned, otherwise an IIOException is thrown.
     * <p>
     *     If the ImageReadParam does not contain a destination type, the first type
     *     from the imageTypes iterator is returned.
     * <p>
     * If the imageTypes iterator is null or empty, an IllegalArgumentException is thrown.
     * <p>
     *
     * @param param
     * @param imageTypes
     * @return
     * @throws IIOException
     */
    public static final ImageTypeSpecifier getDestinationType(ImageReadParam param, Iterator<ImageTypeSpecifier> imageTypes) throws IIOException {
        if (imageTypes != null && imageTypes.hasNext()) {
            ImageTypeSpecifier imageType = null;
            if (param != null) {
                imageType = param.getDestinationType();
            }

            if (imageType == null) {
                Object o = imageTypes.next();
                if (!(o instanceof ImageTypeSpecifier)) {
                    throw new IllegalArgumentException("Non-ImageTypeSpecifier retrieved from imageTypes!");
                }

                imageType = (ImageTypeSpecifier)o;
            } else {
                boolean foundIt = false;

                while(imageTypes.hasNext()) {
                    ImageTypeSpecifier type = (ImageTypeSpecifier)imageTypes.next();
                    if (type.equals(imageType)) {
                        foundIt = true;
                        break;
                    }
                }

                if (!foundIt) {
                    throw new IIOException("Destination type from ImageReadParam does not match!");
                }
            }

            return imageType;
        } else {
            throw new IllegalArgumentException("imageTypes null or empty!");
        }
    }

    /**
     * Checks if the provided ColorSpace is a non-standard ICC based color space.
     * Non-standard means that it is not sRGB, Gray, YCbCr or CMYK.
     * <p>
     * This method can be used to check if color conversion should be applied
     * when converting an image to sRGB for display purposes.
     */
    public static boolean isNonStandardICCColorSpace(ColorSpace cs) {
        boolean retval = false;

        try {
            retval = cs instanceof ICC_ColorSpace && !cs.isCS_sRGB() && !cs.equals(ColorSpace.getInstance(1004)) && !cs.equals(ColorSpace.getInstance(1003)) && !cs.equals(ColorSpace.getInstance(1001)) && !cs.equals(ColorSpace.getInstance(1002));
        } catch (IllegalArgumentException var3) {
        }

        return retval;
    }

    /**
     * Checks if the provided ImageWriter can encode images of the provided type.
     * If the type is null, or if the writer's originating provider is null,
     * no exception is thrown.
     * <p>
     * If the writer cannot encode images of the provided type, an IIOException
     * is thrown.
     **/
    public static void canEncodeImage(ImageWriter writer, ImageTypeSpecifier type) throws IIOException {
        ImageWriterSpi spi = writer.getOriginatingProvider();
        if (type != null && spi != null && !spi.canEncodeImage(type)) {
            throw new IIOException(String.format("Writer %s cannot encode images of type %s", spi.getDescription(Locale.getDefault()), type));
        }
    }

    /**
     * Returns the size in bytes of a tile for the provided SampleModel.
     * <p>
     * The size is computed as scanlineStride * height * elementSizeInBytes,
     * plus any band or pixel offsets.
     * <p>
     * If the SampleModel is null, 0 is returned.
     */
    public static long getTileSize(SampleModel sm) {
        int elementSize = DataBuffer.getDataTypeSize(sm.getDataType());

        if (sm instanceof MultiPixelPackedSampleModel) {
            MultiPixelPackedSampleModel mppsm =
                    (MultiPixelPackedSampleModel)sm;
            return (mppsm.getScanlineStride() * mppsm.getHeight() +
                    (mppsm.getDataBitOffset() + elementSize -1) / elementSize) *
                    ((elementSize + 7) / 8);
        } else if (sm instanceof ComponentSampleModel) {
            ComponentSampleModel csm = (ComponentSampleModel)sm;
            int[] bandOffsets = csm.getBandOffsets();
            int maxBandOff = bandOffsets[0];
            for (int i=1; i<bandOffsets.length; i++)
                maxBandOff = Math.max(maxBandOff, bandOffsets[i]);

            long size = 0;
            int pixelStride = csm.getPixelStride();
            int scanlineStride = csm.getScanlineStride();
            if (maxBandOff >= 0)
                size += maxBandOff + 1;
            if (pixelStride > 0)
                size += pixelStride * (sm.getWidth() - 1);
            if (scanlineStride > 0)
                size += scanlineStride * (sm.getHeight() - 1);

            int[] bankIndices = csm.getBankIndices();
            maxBandOff = bankIndices[0];
            for (int i=1; i<bankIndices.length; i++)
                maxBandOff = Math.max(maxBandOff, bankIndices[i]);
            return size * (maxBandOff + 1) * ((elementSize + 7) / 8);
        } else if (sm instanceof SinglePixelPackedSampleModel) {
            SinglePixelPackedSampleModel sppsm =
                    (SinglePixelPackedSampleModel)sm;
            long size = sppsm.getScanlineStride() * (sppsm.getHeight() - 1) +
                    sppsm.getWidth();
            return size * ((elementSize + 7) / 8);
        }

        return 0;
    }

    /**
     * Returns the size in bytes of a tile for the provided SampleModel.
     * <p>
     * The size is computed as width * height * numBands * elementSizeInBytes
     * <p>
     * If the SampleModel is null, 0 is returned.
     */
    public static long getBandSize(SampleModel sm) {
        int elementSize = DataBuffer.getDataTypeSize(sm.getDataType());

        if (sm instanceof ComponentSampleModel) {
            ComponentSampleModel csm = (ComponentSampleModel)sm;
            int pixelStride = csm.getPixelStride();
            int scanlineStride = csm.getScanlineStride();
            long size = Math.min(pixelStride, scanlineStride);

            if (pixelStride > 0)
                size += pixelStride * (sm.getWidth() - 1);
            if (scanlineStride > 0)
                size += scanlineStride * (sm.getHeight() - 1);
            return size * ((elementSize + 7) / 8);
        } else
            return getTileSize(sm);
    }

}
