package it.geosolutions.imageio.plugins.slices2D;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import com.sun.media.imageioimpl.common.ImageUtil;
import com.sun.media.jai.codecimpl.util.RasterFactory;

/**
 * Base class which need to be extended by any specific format's ImageReader to
 * work with 2D datasets.
 * 
 * @author Romagnoli Daniele
 */
public abstract class SliceImageReader extends ImageReader implements
		IndexManager {
	private static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.plugins.slices2D");

	protected boolean isInitialized = false;

	/**
	 * The originating <code>File</code>
	 */
	protected File originatingFile = null;

	protected SliceImageReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
	}

	/**
	 * Implements this method to allow structure initialization.
	 */
	protected abstract void initialize() throws IOException;

	public abstract int getHeight(int imageIndex) throws IOException;

	public abstract int getWidth(int imageIndex) throws IOException;

	/**
	 * Specific implementation of the read Operation. Any format's reader need
	 * to implement its own version of the read operation.
	 */
	public abstract BufferedImage read(int imageIndex, ImageReadParam param)
			throws IOException;

	/**
	 * return imageMetadata related to a specific product or subdataset.
	 */
	public abstract IIOMetadata getImageMetadata(int imageIndex)
			throws IOException;

	/**
	 * return streamMetadata related the whole source.
	 */
	public abstract IIOMetadata getStreamMetadata() throws IOException;

	public abstract Iterator getImageTypes(int imageIndex) throws IOException;

	/**
	 * Utilty method returning a proper <code>ColorModel</code> given an input
	 * <code>SampleModel</code>
	 * 
	 * @param sm
	 * 		The <code>SampleModel</code> for which we need to create a compatible
	 * 		<code>ColorModel</code>.
	 * 
	 * @return
	 * 		the created <code>ColorModel</code> 
	 */
	protected ColorModel retrieveColorModel(final SampleModel sm) {
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
			if (cm == null)
				LOGGER.info("There are no ColorModels found");

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
