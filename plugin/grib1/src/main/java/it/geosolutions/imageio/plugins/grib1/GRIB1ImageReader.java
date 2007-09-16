package it.geosolutions.imageio.plugins.grib1;

import it.geosolutions.imageio.plugins.AbstractImageReader;
import it.geosolutions.imageio.plugins.SliceDescriptor;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.RasterFactory;

import net.sourceforge.jgrib.GribCollection;
import net.sourceforge.jgrib.GribFile;
import net.sourceforge.jgrib.GribRecord;
import net.sourceforge.jgrib.GribRecordBDS;
import net.sourceforge.jgrib.GribRecordGDS;
import net.sourceforge.jgrib.GribRecordPDS;
import net.sourceforge.jgrib.NoValidGribException;
import net.sourceforge.jgrib.NotSupportedException;
import net.sourceforge.jgrib.tables.GribPDSParameter;

/**
 * {@link GRIB1ImageReader} is a {@link ImageReader} able to create
 * {@link RenderedImage} from GRIB1 sources.
 * 
 * @author Daniele Romagnoli
 */
public class GRIB1ImageReader extends AbstractImageReader {

	private boolean isSingleFile = true;

	private GribFile gribFile = null;

	private GribCollection gribCollection = null;

	// private Map indexToGribSourcesMapping = Collections
	// .synchronizedMap(new HashMap(10));

	// private List imagesList;

	private static ColorModel colorModel = RasterFactory
			.createComponentColorModel(DataBuffer.TYPE_FLOAT, // dataType
					ColorSpace.getInstance(ColorSpace.CS_GRAY), // color space
					false, // has alpha
					false, // is alphaPremultiplied
					Transparency.OPAQUE); // transparency

	protected Map gribRecordsMap = Collections.synchronizedMap(new HashMap(
			GribFile.DEF_RECORDS));

	/**
	 * A class wrapping a GribRecord and its basic properties and structures
	 */
	private class GribRecordWrapper {

		private GribRecordPDS pds;

		private GribRecordBDS bds;

		private GribRecordGDS gds;

		private GribRecord record;

		private int width;

		private int height;

		private SliceDescriptor sliceDescriptor;

		private SampleModel sampleModel;

		public GribRecordWrapper(int recordIndex) {
			record = gribFile.getRecord(recordIndex + 1);
			bds = record.getBDS();
			pds = record.getPDS();
			gds = record.getGDS();
			width = gds.getGridNX();
			height = gds.getGridNY();
			sampleModel = new BandedSampleModel(DataBuffer.TYPE_FLOAT, width,
					height, 1);
			GribPDSParameter param = pds.getParameter();

			final String paramID = GRIB1Utilities.buildParamID(pds
					.getParameter(), pds.getParamTable());
			sliceDescriptor = new GRIB1SliceDescriptor(param.getName(), pds
					.getLevel(), GRIB1Utilities.buildTime(pds).toString(), 0,
					paramID);
		}

		public int getHeight() {
			return height;
		}

		public int getWidth() {
			return width;
		}

		public SampleModel getSampleModel() {
			return sampleModel;
		}

		public GribRecordBDS getBds() {
			return bds;
		}

		public GribRecordPDS getPds() {
			return pds;
		}

		public SliceDescriptor getSliceDescriptor() {
			return sliceDescriptor;
		}
	}

	private int numImages = -1;

	public void setInput(Object input, boolean seekForwardOnly,
			boolean ignoreMetadata) {
		try {

			// TODO: Check this
			if (input instanceof URI) {
				input = ((URI) input).toURL();
			}
			if (gribFile == null) {

				if (input instanceof File) {
					if (((File) input).isDirectory())
						gribCollection = new GribCollection((File) input);
					else
						gribFile = new GribFile(ImageIO
								.createImageInputStream((File) input), null);
				} else if (input instanceof String) {
					File file = new File((String) input);
					if (file.isDirectory())
						gribCollection = new GribCollection(file);
					else
						gribFile = new GribFile(ImageIO
								.createImageInputStream(file), null);
				} else if (input instanceof URL) {
					gribFile = new GribFile((URL) input, null);
				} else if (input instanceof ImageInputStream) {
					gribFile = new GribFile((ImageInputStream) input, null);

				} else if (input instanceof GribFile) {
					this.gribFile = (GribFile) input;
				}
				if (gribFile != null)
					gribFile.parseGribFile();
			}

		} catch (IOException e) {
			throw new IllegalArgumentException("Not a Valid Input", e);
		} catch (NotSupportedException e) {
			throw new RuntimeException(
					"Error occurred during grib file parsing", e);
		} catch (NoValidGribException e) {
			throw new IllegalArgumentException(
					"Error occurred during grib file parsing", e);
		}
		initialize();
	}

	private void initialize() {
		isSingleFile = gribFile != null ? true : false;
		if (isSingleFile)
			numImages = gribFile.getRecordCount();
		else {
			Iterator gribFilesIt = gribCollection.getGribIterator();
			numImages = 0;
			while (gribFilesIt.hasNext()) {
				GribFile gribFile = (GribFile) gribFilesIt.next();
				numImages += gribFile.getRecordCount();
			}
		}
	}


	private void checkImageIndex(final int imageIndex) {
		if (imageIndex < 0 || imageIndex >= numImages) {
			throw new IndexOutOfBoundsException(
					"Invalid imageIndex. It should "
							+ (numImages > 0 ? ("belong the range [0," + (numImages - 1))
									: "be 0"));
		}
	}

	public void setInput(Object input, boolean seekForwardOnly) {
		this.setInput(input);
	}

	public void setInput(Object input) {
		this.setInput(input, true, true);
	}

	protected GRIB1ImageReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
	}

	public int getHeight(int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		return getGribRecordWrapper(imageIndex).getHeight();
	}

	public Iterator getImageTypes(int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		final List l = new java.util.ArrayList(5);
		GribRecordWrapper gw = getGribRecordWrapper(imageIndex);
		ImageTypeSpecifier imageType = new ImageTypeSpecifier(colorModel, gw
				.getSampleModel());
		l.add(imageType);
		return l.iterator();
	}

	/**
	 * Returns a {@link GribRecordWrapper} given a specified imageIndex.
	 * 
	 * @param imageIndex
	 * @return
	 */
	private GribRecordWrapper getGribRecordWrapper(int imageIndex) {
		// TODO: add more logic
		GribRecordWrapper wrapper;
		if (!gribRecordsMap.containsKey(Integer.valueOf(imageIndex))) {
			wrapper = new GribRecordWrapper(imageIndex);
			gribRecordsMap.put(Integer.valueOf(imageIndex), wrapper);
		} else
			wrapper = (GribRecordWrapper) gribRecordsMap.get(Integer
					.valueOf(imageIndex));
		return wrapper;
	}

	public int getNumImages(boolean arg0) throws IOException {
		return numImages;
	}

	public int getWidth(int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		return getGribRecordWrapper(imageIndex).getWidth();
	}

	private WritableRaster readBDSRaster(GribRecordWrapper item,
			ImageReadParam param) throws IOException, NoValidGribException {

		// ////////////////////////////////////////////////////////////////////
		//
		// -------------------------------------------------------------------
		// Raster Creation >>> Step 1: Initialization
		// -------------------------------------------------------------------
		//
		// ////////////////////////////////////////////////////////////////////

		final int width = item.getWidth();
		final int height = item.getHeight();
		GribRecordBDS bds = item.getBds();

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
			// When you do subsampling or source subsetting it might happen that
			// the given source region in the read param is uncorrect, which
			// means it can be or a bit larger than the original file or can
			// begin a bit before original limits.
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
			// Assuming Source Region Dimension equal to Source Image Dimension
			dstWidth = width;
			dstHeight = height;
			srcRegionXOffset = srcRegionYOffset = 0;
			srcRegionWidth = width;
			srcRegionHeight = height;
		}

		// SubSampling variables initialization
		xSubsamplingFactor = param.getSourceXSubsampling();
		ySubsamplingFactor = param.getSourceYSubsampling();
		dstWidth = ((dstWidth - 1) / xSubsamplingFactor) + 1;
		dstHeight = ((dstHeight - 1) / ySubsamplingFactor) + 1;

		// ////////////////////////////////////////////////////////////////////
		//
		// -------------------------------------------------------------------
		// Raster Creation >>> Step 2: Reading the required region
		// -------------------------------------------------------------------
		//
		// ////////////////////////////////////////////////////////////////////

		Rectangle roi = srcRegion != null ? srcRegion : new Rectangle(
				srcRegionXOffset, srcRegionYOffset, srcRegionWidth,
				srcRegionHeight);
		
		//Translating the child element to the proper location.
		WritableRaster translatedRaster = bds.getValues(roi)
				.createWritableTranslatedChild(0, 0);
		
		// ////////////////////////////////////////////////////////////////////
		//
		// -------------------------------------------------------------------
		// Raster Creation >>> Step 3: Performing optional subSampling
		// -------------------------------------------------------------------
		//
		// ////////////////////////////////////////////////////////////////////

		// TODO: use some more optimized JAI operation to do subSampling
		WritableRaster destRaster = Raster.createWritableRaster(
				translatedRaster.getSampleModel().createCompatibleSampleModel(
						dstWidth, dstHeight), new Point(0, 0));
		
		final int origRasterWidth = translatedRaster.getWidth();
		final int origRasterHeight = translatedRaster.getHeight();
		float data[] = null;
		for (int i = 0; i < origRasterHeight; i += ySubsamplingFactor)
			for (int j = 0; j < origRasterWidth; j += xSubsamplingFactor) {
				data = translatedRaster.getPixel(j, i, data);
				destRaster.setPixel(j / xSubsamplingFactor, i
						/ ySubsamplingFactor, data);
			}

		return destRaster;
	}

	public BufferedImage read(final int imageIndex, ImageReadParam param)
			throws IOException {
		BufferedImage image = null;

		try {

			WritableRaster raster = readBDSRaster(getGribRecordWrapper(imageIndex), param);
			image = new BufferedImage(colorModel, raster, false, null);
		} catch (NoSuchElementException e) {
			// XXX
		} catch (IndexOutOfBoundsException e) {
			// XXX
		} catch (NoValidGribException e) {
			// XXX
		}
		return image;
	}

	public IIOMetadata getStreamMetadata() {
		if (isSingleFile)
			return new GRIB1BasicStreamMetadata(gribFile, numImages);
		else
			return new GRIB1BasicStreamMetadata(gribCollection, numImages);
	}

	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		return new GRIB1ImageMetadata(imageIndex, gribFile);
	}

	public SliceDescriptor getSliceDescriptor(int imageIndex)
			throws IOException {
		checkImageIndex(imageIndex);
		GribRecordWrapper gw = getGribRecordWrapper(imageIndex);
		return gw.getSliceDescriptor();
	}

}
