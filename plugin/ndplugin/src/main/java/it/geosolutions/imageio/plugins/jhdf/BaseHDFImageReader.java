package it.geosolutions.imageio.plugins.jhdf;

import it.geosolutions.imageio.plugins.slices2D.SliceImageReader;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;

import java.awt.Rectangle;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.ImageReaderSpi;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.HObject;

public abstract class BaseHDFImageReader extends SliceImageReader {

	protected LinkedHashMap subDatasetsMap;

	protected final int[] mutex = new int[] { 0 };
	
	/**
	 * returns a subDataset given a datasetIndex
	 * 
	 * @param imageIndex
	 *            The index of the required SubDataset
	 * @return the required SubDataset
	 */
	protected Dataset getDataset(int datasetIndex) {
		synchronized (mutex) {
			Set set = subDatasetsMap.keySet();
			Iterator it = set.iterator();
			for (int j = 0; j < datasetIndex; j++)
				it.next();
			return (Dataset) subDatasetsMap.get((String) it.next());
		}
	}
	
	/**
	 * Given a specified datasetIndex, returns the proper subDataset.
	 * 
	 * @param datasetIndex
	 *            an index specifying required coverage(subDataset).
	 * 
	 */
	protected Dataset retrieveDataset(int datasetIndex) {
		// TODO: implement checkImageIndex
//		checkImageIndex(datasetIndex);
		return getDataset(datasetIndex);

	}

	// TODO: should be moved in the aboveLayer?

	/**
	 * Class used to store basic source structure properties such as number of
	 * subdatasets and basic properties of each sub dataset, such as, rank,
	 * dimensions size, chunk size.
	 */
	protected class SourceStructure {

		/** the number of subdatasets contained within the data source */
		protected int nSubdatasets;

		/** a list of {@link SubDatasetInfo} instances */
		protected ArrayList subDatasetInfo;

		/**
		 * Redundant field. Allows index management without scanning all
		 * <code>SubDatasetInfo</code>
		 */
		protected ArrayList subDatasetSizes;

		/**
		 * A-priori Constructor. Use this form when you know the exact
		 * subdataset number prior to instantiate the
		 * <code>SourceStructure</code>
		 * 
		 * @param subdatasetsNum
		 */
		public SourceStructure(int subdatasetsNum) {
			nSubdatasets = subdatasetsNum;
			subDatasetInfo = new ArrayList(subdatasetsNum);
			// new SubDatasetInfo[subdatasetsNum];
			subDatasetSizes = new ArrayList(subdatasetsNum);
		}

		/**
		 * Constructor which need to be used when you dont know the exact
		 * subdataset number a priori. After all <code>SubDatasetInfo</code>'s
		 * has been added, you need to call the <code>init</code> method.
		 * 
		 */

		public SourceStructure() {
			nSubdatasets = 0;
			subDatasetInfo = new ArrayList(10);
			subDatasetSizes = new ArrayList(10);
		}

		public long getSubDatasetSize(int index) {
			return ((Long) subDatasetSizes.get(index)).longValue();
			// return subDatasetSize[0];
		}

		public void setSubDatasetSize(int index, long size) {
			subDatasetSizes.add(index, new Long(size));
		}

		public int getNSubdatasets() {
			return nSubdatasets;
		}

		public void setNSubdatasets(int subdatasets) {
			nSubdatasets = subdatasets;
		}

		public long[] getSubDatasetSizes() {
			final Object[] objArray = subDatasetSizes.toArray();
			final int length = objArray.length;
			final long[] array = new long[length];
			for (int i = 0; i < length; i++)
				array[i] = ((Long) (objArray[i])).longValue();
			return array;
		}

		public void setSubDatasetInfo(int subDatasetIndex, SubDatasetInfo dsInfo) {
			// subDatasetInfo[j] = dsInfo;
			subDatasetInfo.add(subDatasetIndex, dsInfo);
		}

		public void addSubDatasetProperties(SubDatasetInfo dsInfo,
				long subDatasetSize) {
			// subDatasetInfo[j] = dsInfo;
			subDatasetInfo.add(nSubdatasets, dsInfo);
			subDatasetSizes.add(nSubdatasets++, new Long(subDatasetSize));
		}

		public SubDatasetInfo getSubDatasetInfo(int subDatasetIndex) {
			if (subDatasetIndex <= nSubdatasets)
				// return subDatasetInfo[subDatasetIndex];
				return (SubDatasetInfo) subDatasetInfo.get(subDatasetIndex);
			else
				return null;
		}

		public void dispose(){
			nSubdatasets = 0;
			subDatasetInfo.clear();
			subDatasetSizes.clear();
		}
	}

	/**
	 * 
	 * @param itemName
	 *            The name of the product/subDataset to be checked
	 * @return <code>true</code> if the product/subdataset needs to be taken
	 *         on account.
	 */
	protected abstract boolean isAcceptedItem(final String itemName);

	/**
	 * Additional initialization for a specific HDF "Profile".
	 * Depending on the HDF data producer, the originating file has a proper
	 * data/metadata structure. For this reason, a specific initialization 
	 * should be implemented for each different HDF "Profile".
	 * As an instance, the Automated Processing System (APS) produces HDF files
	 * having a structure which differes from the HDF structure of a file 
	 * produced by TIROS Operational Vertical Sounder (TOVS).
	 * 
	 * @throws Exception
	 */
	protected abstract void initializeProfile() throws Exception;
	
	
	protected abstract int getBandNumberFromProduct(final String productName);

	/** The originating FileFormat */
	protected FileFormat fileFormat = null;

	protected ImageTypeSpecifier imageType = null;

	/**
	 * a <code>SourceStructure</code>'s instance needed to get main
	 * SubDatasets info and properties.
	 */
	protected SourceStructure sourceStructure;

	/** root of the FileFormat related to the provided input source */
	protected HObject root;

	protected BaseHDFImageReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
	}

	/**
	 * Returns the width in pixels of the given image within the input source.
	 * 
	 * @param imageIndex
	 *            the index of the image to be queried.
	 * 
	 * @return the width of the image, as an <code>int</code>.
	 */
	public int getWidth(final int imageIndex) throws IOException {
		if (!isInitialized)
			initialize();
		SubDatasetInfo sdInfo = sourceStructure
				.getSubDatasetInfo(retrieveSubDatasetIndex(imageIndex));
		return sdInfo.getWidth();
	}

	/**
	 * Returns the height in pixels of the given image within the input source.
	 * 
	 * @param imageIndex
	 *            the index of the image to be queried.
	 * 
	 * @return the height of the image, as an <code>int</code>.
	 */
	public int getHeight(final int imageIndex) throws IOException {
		if (!isInitialized)
			initialize();
		SubDatasetInfo sdInfo = sourceStructure
				.getSubDatasetInfo(retrieveSubDatasetIndex(imageIndex));
		return sdInfo.getHeight();
	}

	/**
	 * Reads the image indexed by <code>imageIndex</code> and returns it as a
	 * complete <code>BufferedImage</code>, using a supplied
	 * <code>ImageReadParam</code>.
	 * 
	 * @param imageIndex
	 *            the index of the image to be retrieved.
	 * @param param
	 *            an <code>ImageReadParam</code> used to control the reading
	 *            process, or <code>null</code>.
	 * 
	 * @return the desired portion of the image as a <code>BufferedImage</code>.
	 */
	public BufferedImage read(final int imageIndex, ImageReadParam param)
			throws IOException {

		// ////////////////////////////////////////////////////////////////////
		//
		// INITIALIZATIONS
		//
		// ////////////////////////////////////////////////////////////////////

		if (!isInitialized)
			initialize();
		final int[] slice2DindexCoordinates = getSlice2DIndexCoordinates(imageIndex);
		final int subDatasetIndex = slice2DindexCoordinates[0];
		final Dataset dataset = retrieveDataset(subDatasetIndex);

		BufferedImage bimage = null;
		dataset.init();

		SubDatasetInfo sdInfo = sourceStructure
				.getSubDatasetInfo(subDatasetIndex);

		final int rank = sdInfo.getRank();
		final int width = sdInfo.getWidth();
		final int height = sdInfo.getHeight();
		final Datatype dt = sdInfo.getDatatype();

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

		// getting dataset properties.
		final long[] start = dataset.getStartDims();
		final long[] stride = dataset.getStride();
		final long[] sizes = dataset.getSelectedDims();

		// Setting variables needed to execute read operation.
		start[rank - 2] = srcRegionYOffset;
		start[rank - 1] = srcRegionXOffset;
		sizes[rank - 2] = dstHeight;
		sizes[rank - 1] = dstWidth;
		stride[rank - 2] = ySubsamplingFactor;
		stride[rank - 1] = xSubsamplingFactor;

		if (rank > 2) {
			// Setting indexes of dimensions > 2.
			for (int i = 0; i < rank - 2; i++) {
				// TODO: Need to change indexing logic
				start[i] = slice2DindexCoordinates[i + 1];
				sizes[i] = 1;
				stride[i] = 1;
			}
		}

		final int nBands = getBandNumberFromProduct(sdInfo.getName());

		// bands variables
		final int[] banks = new int[nBands];
		final int[] offsets = new int[nBands];
		for (int band = 0; band < nBands; band++) {
			banks[band] = band;
			offsets[band] = 0;
		}

		// Setting SampleModel and ColorModel
		final int bufferType = HDFUtilities.getBufferTypeFromDataType(dt);
		SampleModel sm = new BandedSampleModel(bufferType, dstWidth, dstHeight,
				dstWidth, banks, offsets);
		ColorModel cm = retrieveColorModel(sm);

		// ////////////////////////////////////////////////////////////////////
		//
		// DATA READ
		//
		// ////////////////////////////////////////////////////////////////////

		WritableRaster wr = null;
		final Object data;
		try {
			data = dataset.read();
			final int size = dstWidth * dstHeight;
			DataBuffer dataBuffer = null;

			switch (bufferType) {
			case DataBuffer.TYPE_BYTE:
				dataBuffer = new DataBufferByte((byte[]) data, size);
				break;
			case DataBuffer.TYPE_SHORT:
			case DataBuffer.TYPE_USHORT:
				dataBuffer = new DataBufferShort((short[]) data, size);
				break;
			case DataBuffer.TYPE_INT:
				dataBuffer = new DataBufferInt((int[]) data, size);
				break;
			case DataBuffer.TYPE_FLOAT:
				dataBuffer = new DataBufferFloat((float[]) data, size);
				break;
			case DataBuffer.TYPE_DOUBLE:
				dataBuffer = new DataBufferDouble((double[]) data, size);
				break;
			}

			wr = Raster.createWritableRaster(sm, dataBuffer, null);
			bimage = new BufferedImage(cm, wr, false, null);

		} catch (Exception e) {
			RuntimeException rte = new RuntimeException(
					"Exception occurred while data Reading" + e);
			rte.initCause(e);
			throw rte;
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
		if (originatingFile != null)
			reset();
		if (input instanceof File) {
			file = (File) input;
			originatingFile = file;
		}

		if (input instanceof FileImageInputStreamExtImpl) {
			file = ((FileImageInputStreamExtImpl) input).getFile();
			originatingFile = file;
		}

		try {
			initialize();
		} catch (IOException e) {
			new RuntimeException("Not a Valid Input", e);
		}
	}

	/**
	 * Simple initialization method
	 */
	protected void initialize() throws IOException {
		synchronized (mutex) {
			if (originatingFile == null)
				throw new IOException(
						"Unable to Initialize data. Provided Input is not valid");
			final String fileName = originatingFile.getAbsolutePath();
			try {
				fileFormat = FileFormat.getInstance(fileName);
				fileFormat = fileFormat.open(fileName, FileFormat.READ);
				root = fileFormat.get("/");
				if (root != null)
					initializeProfile();

			} catch (Exception e) {
				IOException ioe = new IOException(
						"Unable to Initialize data. Provided Input is not valid"
								+ e);
				ioe.initCause(e);
				throw ioe;
			}
			isInitialized = true;
		}
	}

	/**    
     * Returns an <code>Iterator</code> containing possible image
     * types to which the given image may be decoded, in the form of
     * <code>ImageTypeSpecifiers</code>s. 
     *
     * @param imageIndex the index of the image to be
     * <code>retrieved</code>.
     *
     * @return an <code>Iterator</code> containing at least one
     * <code>ImageTypeSpecifier</code> representing suggested image
     * types for decoding the current given image.
     *
     * @exception IllegalStateException if the input source has not been set.
     * @exception IndexOutOfBoundsException if the supplied index is
     * out of bounds.
     * @exception IOException if an error occurs reading the format
     * information from the input source.
     */
	
	public Iterator getImageTypes(final int imageIndex) throws IOException {
		final List l = new java.util.ArrayList(1);
		if (!isInitialized)
			initialize();

		SubDatasetInfo sdInfo = sourceStructure
				.getSubDatasetInfo(retrieveSubDatasetIndex(imageIndex));

		final Datatype dt = sdInfo.getDatatype();
		final int width = sdInfo.getWidth();
		final int height = sdInfo.getHeight();
		final int nBands = getBandNumberFromProduct(sdInfo.getName());

		// bands variables
		final int[] banks = new int[nBands];
		final int[] offsets = new int[nBands];
		for (int band = 0; band < nBands; band++) {
			banks[band] = band;
			offsets[band] = 0;
		}

		// Variable used to specify the data type for the storing samples
		// of the SampleModel
		int bufferType = HDFUtilities.getBufferTypeFromDataType(dt);
		final SampleModel sm = new BandedSampleModel(bufferType, width, height,
				width, banks, offsets);

		final ColorModel cm = retrieveColorModel(sm);

		imageType = new ImageTypeSpecifier(cm, sm);
		l.add(imageType);
		return l.iterator();
	}

	public int getTileHeight(final int imageIndex) throws IOException {
		if (!isInitialized)
			initialize();
		final int subDatasetIndex = retrieveSubDatasetIndex(imageIndex);
		SubDatasetInfo sdInfo = sourceStructure
				.getSubDatasetInfo(subDatasetIndex);
		final long[] chunkSize = sdInfo.getChunkSize();

		// TODO: Change this behavior
		if (chunkSize != null) {
			final int rank = sdInfo.getRank();
			return (int) chunkSize[rank - 1];
		} else
			return Math.min(512, sdInfo.getHeight());
	}

	public int getTileWidth(final int imageIndex) throws IOException {
		if (!isInitialized)
			initialize();
		final int subDatasetIndex = retrieveSubDatasetIndex(imageIndex);
		SubDatasetInfo sdInfo = sourceStructure
				.getSubDatasetInfo(subDatasetIndex);
		final long[] chunkSize = sdInfo.getChunkSize();

		// TODO: Change this behavior
		if (chunkSize != null) {
			final int rank = sdInfo.getRank();
			return (int) chunkSize[rank - 2];
		} else
			return Math.min(512, sdInfo.getWidth());
	}

	public void dispose() {
		super.dispose();
		try {
			fileFormat.close();
			sourceStructure.dispose();
			sourceStructure=null;
		} catch (Exception e) {
			// TODO Nothing to do.
		}
	}

	public void reset() {
		super.setInput(null, false, false);
		root = null;
		originatingFile = null;
	}

	/**
	 * returns a proper subindex needed to access a specific 2D slice of a
	 * specified coverage/subdataset.
	 * 
	 * @param imageIndex
	 *            the specified coverage/subDataset
	 * @param selectedIndexOfEachDim
	 *            the required index of each dimension
	 * 
	 * TODO: Should I use a single long[] input parameter containing also the
	 * subdataset index?
	 */
	public int retrieveSlice2DIndex(int imageIndex, int[] selectedIndexOfEachDim) {
		int subIndexOffset = 0;
		final SubDatasetInfo sdInfo = sourceStructure
				.getSubDatasetInfo(imageIndex);
		for (int i = 0; i < imageIndex; i++)
			subIndexOffset += (sourceStructure.getSubDatasetSize(i));

		if (selectedIndexOfEachDim != null) {
			// X and Y dims are not taken in account
			final int selectedDimsLenght = selectedIndexOfEachDim.length;
			final long[] subDatasetDims = sdInfo.getDims();
			final int rank = sdInfo.getRank();

			// supposing specifying all required subDimensions.
			// as an instance, if rank=5, I need to specify 3 dimensions-index
			// TODO: maybe I can assume some default behavior.
			// as an instance, using 0 as dimension-index when not specified.
			if (selectedDimsLenght != (rank - 2)) {
				throw new IndexOutOfBoundsException(
						"The selected dims array can't be"
								+ "greater than the rank of the subDataset");
			}
			for (int i = 0; i < selectedDimsLenght; i++) {
				if (selectedIndexOfEachDim[i] > subDatasetDims[i]) {
					final StringBuffer sb = new StringBuffer();
					sb
							.append(
									"At least one of the specified indexes is greater than the max allowed index in that dimension\n")
							.append("dimension=")
							.append(i)
							.append(" index=")
							.append(selectedIndexOfEachDim[i])
							.append(
									" while the maximum index available for this dimension is ")
							.append(subDatasetDims[i]);
					throw new IndexOutOfBoundsException(sb.toString());
				}
			}
			long displacement = 0;
			if (rank > 2) {
				// The least significant dimension is used as offset
				long finalOffset = selectedIndexOfEachDim[rank - 3];
				if (rank > 3) {
					//TODO: review and test this logic with a 4D dataset.
					final long[] multipliers = new long[rank - 3];
					for (int i = 0; i < rank - 3; i++)
						multipliers[i] = subDatasetDims[i + 2];
					for (int i = 0; i < rank - 3; i++) {
						int factor = 1;
						for (int j = 0; j < rank - 3 - i; j++)
							factor *= multipliers[j];
						displacement += (factor * selectedIndexOfEachDim[i]);
					}
				}
				displacement += finalOffset;
			}
			subIndexOffset += displacement;
		}
		return (int) subIndexOffset;
	}

	/**
	 * Given a specifiedIndex as an input, returns a <code>long[]</code>
	 * having the subDataset/coverage index at the first position of the array.
	 * Then, the indexes (of the other dimensions) needed to retrieve a proper
	 * 2D Slice.
	 * 
	 * As an instance, suppose a HDF source contains a 4D SubDataset with the
	 * form (X,Y,Z,T). if returnedIndex[]={2,3,1}, the required Slice2D is
	 * available at the subDataset with index=2, timeIndex=3, zIndex=1.
	 * 
	 * TODO: Now, we are supposing order is 5thDim -> T -> Z -> (X,Y)
	 * 
	 */
	public int[] getSlice2DIndexCoordinates(int requiredSlice2DIndex) {
		final int nTotalDataset = sourceStructure.getNSubdatasets();
		final long[] subDatasetSizes = sourceStructure.getSubDatasetSizes();
		int iSubdataset = 0;
		for (; iSubdataset < nTotalDataset; iSubdataset++) {
			int subDatasetSize = (int) subDatasetSizes[iSubdataset];
			if (requiredSlice2DIndex >= subDatasetSize)
				requiredSlice2DIndex -= subDatasetSize;
			else
				break;
		}

		// Getting the SubDatasetInfo related to the specified subDataset.
		final SubDatasetInfo sdInfo = sourceStructure
				.getSubDatasetInfo(iSubdataset);
		final int rank = sdInfo.getRank();

		// index initialization
		final int[] slice2DIndexCoordinates = new int[rank - 1];// subDatasetIndex+(rank-2)
		for (int i = 0; i < rank - 1; i++)
			slice2DIndexCoordinates[i] = 0;
		slice2DIndexCoordinates[0] = iSubdataset;

		if (rank > 2) {

			if (rank > 3) {
//				TODO: review and test this logic with a 4D dataset.
				final long[] subDatasetDims = sdInfo.getDims();
				final long[] multipliers = new long[rank - 3];
				for (int i = 0; i < rank - 3; i++)
					multipliers[i] = subDatasetDims[i];

				for (int i = 0; i < rank - 3; i++) {
					int factor = 1;
					for (int j = 0; j < rank - 3 - i; j++)
						factor *= multipliers[j];
					while (requiredSlice2DIndex >= factor) {
						requiredSlice2DIndex -= factor;
						slice2DIndexCoordinates[i + 1]++;
					}
				}
			}
			slice2DIndexCoordinates[rank - 2] = requiredSlice2DIndex;
		}
		return slice2DIndexCoordinates;
	}

	// /**
	// * Given a specifiedIndex as an input, returns a <code>long[]</code>
	// * having the subDataset/coverage index at the first position of the
	// array.
	// * Then, the indexes (of the other dimensions) needed to retrieve a proper
	// * 2D Slice.
	// *
	// * As an instance, suppose a HDF source contains a 4D SubDataset with the
	// * form (X,Y,Z,T). if returnedIndex[]={2,3,1}, the required Slice2D is
	// * available at the subDataset with index=2, timeIndex=3, zIndex=1.
	// *
	// * TODO: Now, we are supposing order is 5thDim -> T -> Z -> (X,Y)
	// *
	// */
	// public int[] getSlice2DIndexCoordinates(int requiredSlice2DIndex) {
	// final int nTotalDataset = sourceStructure.getNSubdatasets();
	// final long[] subDatasetSizes = sourceStructure.getSubDatasetSizes();
	// int iSubdataset = 0;
	// for (; iSubdataset < nTotalDataset; iSubdataset++) {
	// int subDatasetSize = (int) subDatasetSizes[iSubdataset];
	// if (requiredSlice2DIndex >= subDatasetSize)
	// requiredSlice2DIndex -= subDatasetSize;
	// else
	// break;
	// }
	//
	// // Getting the SubDatasetInfo related to the specified subDataset.
	// final SubDatasetInfo sdInfo = sourceStructure
	// .getSubDatasetInfo(iSubdataset);
	// final int rank = sdInfo.getRank();
	//
	// // index initialization
	// final int[] slice2DIndexCoordinates = new int[rank - 1];//
	// subDatasetIndex+(rank-2)
	// for (int i = 0; i < rank - 1; i++)
	// slice2DIndexCoordinates[i] = 0;
	// slice2DIndexCoordinates[0] = iSubdataset;
	//
	// if (rank > 2) {
	// final long[] subDatasetDims = sdInfo.getDims();
	// if (rank > 3) {
	// final long[] multipliers = new long[rank - 3];
	// for (int i = 0; i < rank - 3; i++)
	// multipliers[i] = subDatasetDims[i + 2];
	//
	// for (int i = 0; i < rank - 3; i++) {
	// int factor = 1;
	// for (int j = 0; j < rank - 3 - i; j++)
	// factor *= multipliers[j];
	// while (requiredSlice2DIndex >= factor) {
	// requiredSlice2DIndex -= factor;
	// slice2DIndexCoordinates[i + 1]++;
	// }
	// }
	// }
	// slice2DIndexCoordinates[rank - 2] = requiredSlice2DIndex;
	// }
	// return slice2DIndexCoordinates;
	// }

	/**
	 * Given the index of a 2D image, retrieve the index of the subDataset
	 * containing that image.
	 * 
	 * @param imageIndex
	 *            the index of a 2D image
	 * @return the index of the subDataset containing that image.
	 */
	protected int retrieveSubDatasetIndex(int imageIndex) {
		final int nTotalDataset = sourceStructure.getNSubdatasets();
		final long[] subDatasetSizes = sourceStructure.getSubDatasetSizes();
		int iSubdataset = 0;
		for (; iSubdataset < nTotalDataset; iSubdataset++) {
			int subDatasetSize = (int) subDatasetSizes[iSubdataset];
			if (imageIndex >= subDatasetSize)
				imageIndex -= subDatasetSize;
			else
				break;
		}
		return iSubdataset;
	}
}
