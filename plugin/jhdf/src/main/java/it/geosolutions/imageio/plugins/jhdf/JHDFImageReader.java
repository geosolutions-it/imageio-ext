package it.geosolutions.imageio.plugins.jhdf;

import it.geosolutions.imageio.plugins.jhdf.pool.DatasetPool.DatasetCopy;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;

import com.sun.media.imageioimpl.common.ImageUtil;
import com.sun.media.jai.codecimpl.util.RasterFactory;

/**
 * WORK IN PROGRESS - NOT YET COMPLETED - ACTUALLY ABANDONED
 */

public class JHDFImageReader extends ImageReader {
	private static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.plugins.jhdf.");

	/** The originating provider. It is used to retrieve the DatasetPoolHandler */
	protected JHDFImageReaderSpi spi;

	private boolean isInitialized = false;

	private File originatingFile = null;

	private ImageTypeSpecifier imageType;

	protected JHDFImageReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
		spi = (JHDFImageReaderSpi) originatingProvider;
	}

	public int getWidth(final int imageIndex) throws IOException {
		if (!isInitialized)
			initialize();
		DatasetCopy item = spi.dsPoolManager.getDatasetCopy(imageIndex);
		final int width = item.getDataset().getWidth();
		spi.dsPoolManager.getBackDatasetCopy(imageIndex, item.getCopyID(),
				false);
		return width;

	}

	public int getHeight(final int imageIndex) throws IOException {
		if (!isInitialized)
			initialize();
		DatasetCopy item = spi.dsPoolManager.getDatasetCopy(imageIndex);
		final int height = item.getDataset().getHeight();
		spi.dsPoolManager.getBackDatasetCopy(imageIndex, item.getCopyID(),
				false);
		return height;
	}

	public IIOMetadata getImageMetadata(final int imageIndex)
			throws IOException {
		return null;
	}

	public int getNumImages(boolean arg0) throws IOException {
		return 0;
	}

	public IIOMetadata getStreamMetadata() throws IOException {
		return null;
	}

	public BufferedImage read(final int imageIndex, ImageReadParam param)
			throws IOException {

		BufferedImage bimage = null;
		// TODO: REMOVE this MESSAGE
		System.out.print("read->");
		System.out.print(Integer.toString(param.getSourceRegion().x) + " "
				+ Integer.toString(param.getSourceRegion().y) + "\n");
		final DatasetCopy dsc = spi.dsPoolManager.getDatasetCopy(imageIndex);
		final int pooledCopy = dsc.getCopyID();

		// TODO: REMOVE this MESSAGE
		System.out.print(Integer.toString(param.getSourceRegion().x) + " "
				+ Integer.toString(param.getSourceRegion().y)
				+ " Obtained copy=" + pooledCopy + "\n");
		if (dsc != null) {
			final Dataset dataset = dsc.getDataset();
			dataset.init();

			if (!isInitialized)
				initialize();
			dataset.init();
			final int width = dataset.getWidth();
			final int height = dataset.getHeight();

			if (param == null)
				param = getDefaultReadParam();

			int dstWidth = -1;
			int dstHeight = -1;
			int srcRegionWidth = -1;
			int srcRegionHeight = -1;
			int srcRegionXOffset = -1;
			int srcRegionYOffset = -1;
			int xSubsamplingFactor = -1;
			int ySubsamplingFactor = -1;

			// //
			//
			// Retrieving Information about Source Region and doing
			// additional intialization operations.
			//
			// //
			Rectangle srcRegion = param.getSourceRegion();
			if (srcRegion != null) {
				srcRegionWidth = (int) srcRegion.getWidth();
				srcRegionHeight = (int) srcRegion.getHeight();
				srcRegionXOffset = (int) srcRegion.getX();
				srcRegionYOffset = (int) srcRegion.getY();

				// //
				//
				// Minimum correction for wrong source regions
				//
				// When you do subsampling or source subsetting it might
				// happen that the given source region in the read param is
				// uncorrect, which means it can be or a bit larger than the
				// original file or can begin a bit before original limits.
				//
				// We got to be prepared to handle such case in order to avoid
				// generating ArrayIndexOutOfBoundsException later in the code.
				//
				// //

				if (srcRegionXOffset < 0)
					srcRegionXOffset = 0;
				if (srcRegionYOffset < 0)
					srcRegionYOffset = 0;
				if ((srcRegionXOffset + srcRegionWidth) > width) {
					srcRegionWidth = width - srcRegionXOffset;
				}
				// initializing destWidth
				dstWidth = srcRegionWidth;

				if ((srcRegionYOffset + srcRegionHeight) > height) {
					srcRegionHeight = height - srcRegionYOffset;
				}
				// initializing dstHeight
				dstHeight = srcRegionHeight;

			} else {
				// Source Region not specified.
				// Assuming Source Region Dimension equal to Source Image
				// Dimension
				dstWidth = width;
				dstHeight = height;
				srcRegionXOffset = srcRegionYOffset = 0;
				srcRegionWidth = width;
				srcRegionHeight = height;
			}

			// SubSampling variables initialization
			xSubsamplingFactor = param.getSourceXSubsampling();
			ySubsamplingFactor = param.getSourceYSubsampling();

			// ////
			//
			// Updating the destination size in compliance with
			// the subSampling parameters
			//
			// ////

			dstWidth = ((dstWidth - 1) / xSubsamplingFactor) + 1;
			dstHeight = ((dstHeight - 1) / ySubsamplingFactor) + 1;

			final long[] start = dataset.getStartDims();
			final long[] stride = dataset.getStride();
			final long[] sizes = dataset.getSelectedDims();

			start[0] = srcRegionYOffset;
			start[1] = srcRegionXOffset;
			sizes[0] = dstHeight;
			sizes[1] = dstWidth;
			stride[0] = ySubsamplingFactor;
			stride[1] = xSubsamplingFactor;

			final Datatype dt = dataset.getDatatype();
			final int dataTypeClass = dt.getDatatypeClass();
			final int dataTypeSize = dt.getDatatypeSize();
			final boolean isUnsigned = dt.isUnsigned();

			final int nBands = 1;

			// bands variables
			final int[] banks = new int[nBands];
			final int[] offsets = new int[nBands];
			for (int band = 0; band < nBands; band++) {
				/* Bands are not 0-base indexed, so we must add 1 */
				banks[band] = band;
				offsets[band] = 0;
			}

			// Variable used to specify the data type for the storing samples
			// of the SampleModel
			int buffer_type = 0;
			if (dataTypeClass == Datatype.CLASS_INTEGER) {
				if (dataTypeSize == 1)
					buffer_type = DataBuffer.TYPE_BYTE;
				else if (dataTypeSize == 2) {
					if (isUnsigned)
						buffer_type = DataBuffer.TYPE_USHORT;
					else
						buffer_type = DataBuffer.TYPE_SHORT;
				} else if (dataTypeSize == 4)
					buffer_type = DataBuffer.TYPE_INT;
			} else if (dataTypeClass == Datatype.CLASS_FLOAT)
				if (dataTypeSize == 4)
					buffer_type = DataBuffer.TYPE_FLOAT;

			SampleModel sm = new BandedSampleModel(buffer_type, dstWidth,
					dstHeight, dstWidth, banks, offsets);
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

			} else if ((buffer_type == DataBuffer.TYPE_BYTE)
					|| (buffer_type == DataBuffer.TYPE_USHORT)
					|| (buffer_type == DataBuffer.TYPE_INT)
					|| (buffer_type == DataBuffer.TYPE_FLOAT)
					|| (buffer_type == DataBuffer.TYPE_DOUBLE)) {

				// Just one band. Using the built-in Gray Scale Color Space
				cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
				cm = RasterFactory.createComponentColorModel(buffer_type, // dataType
						cs, // color space
						false, // has alpha
						false, // is alphaPremultiplied
						Transparency.OPAQUE); // transparency
			} else {
				if (buffer_type == DataBuffer.TYPE_SHORT) {
					// Just one band. Using the built-in Gray Scale Color
					// Space
					cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
					cm = new ComponentColorModel(cs, false, false,
							Transparency.OPAQUE, DataBuffer.TYPE_SHORT);
				}
			}

			WritableRaster wr = null;
			final Object data;
			try {
				data = dataset.read();
				final int size = dstWidth * dstHeight;

				// DataBuffer db = new
				// DataBufferInt((int[])data,dstHeight*dstWidth);

				DataBuffer db = null;

				switch (buffer_type) {
				case DataBuffer.TYPE_BYTE:
					db = new DataBufferByte((byte[]) data, size);
					break;
				case DataBuffer.TYPE_SHORT:
				case DataBuffer.TYPE_USHORT:
					db = new DataBufferShort((short[]) data, size);
					break;
				case DataBuffer.TYPE_INT:
					db = new DataBufferInt((int[]) data, size);
					break;
				case DataBuffer.TYPE_FLOAT:
					db = new DataBufferFloat((float[]) data, size);
					break;
				}

				wr = Raster.createWritableRaster(sm, db, null);
				bimage = new BufferedImage(cm, wr, false, null);

			} catch (OutOfMemoryError e) {
				// TODO Auto-generated catch block
			} catch (Exception e) {
				// TODO Auto-generated catch block
			}
			System.out.print("<---read ");
			System.out.print(Integer.toString(param.getSourceRegion().x) + " "
					+ Integer.toString(param.getSourceRegion().y) + "\n");
			spi.dsPoolManager.getBackDatasetCopy(imageIndex, pooledCopy, true);
		}

		return bimage;
	}

	public void setInput(Object input, boolean seekForwardOnly,
			boolean ignoreMetadata) {
		this.setInput(input);
	}

	public void setInput(Object input, boolean seekForwardOnly) {
		this.setInput(input, seekForwardOnly);
	}

	public void setInput(Object input) {

		File file = null;

		// ////////////////////////////////////////////////////////////////////
		//
		// Reset the state of this reader
		//
		// Prior to set a new input, I need to do a pre-emptive reset in order
		// to clear any value-object related to the previous input.
		// ////////////////////////////////////////////////////////////////////
		if (input instanceof File)
			file = (File) input;

		if (input instanceof FileImageInputStreamExtImpl)
			file = ((FileImageInputStreamExtImpl) input).getFile();

		// reading information
		initialize(file);

	}

	public Iterator getImageTypes(int imageIndex) throws IOException {
//		if (!isInitialized)
//			initialize();
//		final List l = new java.util.ArrayList(5);
//		System.out.print("getImagesTypes->");
//		DatasetCopy item = spi.dsPoolManager.getDatasetCopy(imageIndex);
//		Dataset dataset = item.getDataset();
//		dataset.init();
//
//		final Datatype dt = dataset.getDatatype();
//		final int width = dataset.getWidth();
//		final int height = dataset.getHeight();
//		final int dataTypeClass = dt.getDatatypeClass();
//		final int dataTypeSize = dt.getDatatypeSize();
//		final boolean isUnsigned = dt.isUnsigned();
//		ColorModel cm = null;
//		SampleModel sm = null;
//		if (dataTypeClass == Datatype.CLASS_INTEGER) {
//			if (dataTypeSize == 1) {
//				cm = RasterFactory.createComponentColorModel(
//						DataBuffer.TYPE_BYTE, ColorSpace
//								.getInstance(ColorSpace.CS_GRAY), false, false,
//						Transparency.OPAQUE);
//				sm = cm.createCompatibleSampleModel(width, height);
//
//			} else if (dataTypeSize == 2 && !isUnsigned) {
//				// XXX I am forcing to USHORT for testing purposes
//				cm = new ComponentColorModel(ColorSpace
//						.getInstance(ColorSpace.CS_GRAY), false, false,
//						Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
//				sm = cm.createCompatibleSampleModel(width, height);
//			} else if (dataTypeSize == 4) {
//				cm = new ComponentColorModel(ColorSpace
//						.getInstance(ColorSpace.CS_GRAY), false, false,
//						Transparency.OPAQUE, DataBuffer.TYPE_INT);
//				sm = cm.createCompatibleSampleModel(width, height);
//			}
//		} else if (dataTypeClass == Datatype.CLASS_FLOAT) {
//
//			cm = RasterFactory.createComponentColorModel(DataBuffer.TYPE_FLOAT,
//					ColorSpace.getInstance(ColorSpace.CS_GRAY), false, false,
//					Transparency.OPAQUE);
//			sm = cm.createCompatibleSampleModel(width, height);
//
//		}
//
//		imageType = new ImageTypeSpecifier(cm, sm);
//		l.add(imageType);
//		spi.dsPoolManager.getBackDatasetCopy(imageIndex, item.getCopyID(),
//				false);
//		System.out.print("<---getImagesTypes\n");
//		return l.iterator();
		
		
		
		
		final List l = new java.util.ArrayList(5);
		if (!isInitialized)
			initialize();

		DatasetCopy item = spi.dsPoolManager.getDatasetCopy(imageIndex);
		Dataset dataset = item.getDataset();
		dataset.init();

		final Datatype dt = dataset.getDatatype();

		final int nRank = dataset.getRank();
		System.out.println(nRank);
		final int dataTypeClass = dt.getDatatypeClass();
		final int dataTypeSize = dt.getDatatypeSize();
		final int width = dataset.getWidth();
		final int height = dataset.getHeight();
		final boolean isUnsigned = dt.isUnsigned();

		final int nBands = 1;

		// bands variables
		final int[] banks = new int[nBands];
		final int[] offsets = new int[nBands];
		for (int band = 0; band < nBands; band++) {
			/* Bands are not 0-base indexed, so we must add 1 */
			banks[band] = band;
			offsets[band] = 0;
		}

		// Variable used to specify the data type for the storing samples
		// of the SampleModel
		int buffer_type = 0;
		if (dataTypeClass == Datatype.CLASS_INTEGER) {
			if (dataTypeSize == 1)
				buffer_type = DataBuffer.TYPE_BYTE;
			else if (dataTypeSize == 2) {
				if (isUnsigned)
					buffer_type = DataBuffer.TYPE_USHORT;
				else
					buffer_type = DataBuffer.TYPE_SHORT;
			} else if (dataTypeSize == 4)
				buffer_type = DataBuffer.TYPE_INT;
		} else if (dataTypeClass == Datatype.CLASS_FLOAT)
			if (dataTypeSize == 4)
				buffer_type = DataBuffer.TYPE_FLOAT;

		SampleModel sm = new BandedSampleModel(buffer_type, width, height,
				width, banks, offsets);
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

		} else if ((buffer_type == DataBuffer.TYPE_BYTE)
				|| (buffer_type == DataBuffer.TYPE_USHORT)
				|| (buffer_type == DataBuffer.TYPE_INT)
				|| (buffer_type == DataBuffer.TYPE_FLOAT)
				|| (buffer_type == DataBuffer.TYPE_DOUBLE)) {

			// Just one band. Using the built-in Gray Scale Color Space
			cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
			cm = RasterFactory.createComponentColorModel(buffer_type, // dataType
					cs, // color space
					false, // has alpha
					false, // is alphaPremultiplied
					Transparency.OPAQUE); // transparency
		} else {
			if (buffer_type == DataBuffer.TYPE_SHORT) {
				// Just one band. Using the built-in Gray Scale Color
				// Space
				cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
				cm = new ComponentColorModel(cs, false, false,
						Transparency.OPAQUE, DataBuffer.TYPE_SHORT);
			}
		}
		
		spi.dsPoolManager.getBackDatasetCopy(imageIndex, item.getCopyID(),
				false);
		imageType = new ImageTypeSpecifier(cm, sm);
		l.add(imageType);
		return l.iterator();

	}

	private void initialize(File file) {
		if (!isInitialized) {
			originatingFile = file;
			spi.dsPoolManager.setOriginatingFile(originatingFile);
			isInitialized = true;
		}
	}

	private void initialize() {
		initialize(originatingFile);
	}

	public int getTileHeight(int imageIndex) throws IOException {
		return 256;
	}

	public int getTileWidth(int imageIndex) throws IOException {
		return 256;
	}

	public void dispose() {
		// TODO: NEED TO BE IMPLEMENTED
		super.dispose();
	}

	public void reset() {
		// TODO: NEED TO BE IMPLEMENTED
		super.reset();
	}

}
