package it.geosolutions.imageio.gdalframework;

import java.awt.Dimension;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SampleModel;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.imageio.metadata.IIOMetadata;
import javax.media.jai.RasterFactory;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.gdalconst.gdalconstConstants;

import com.sun.media.imageioimpl.common.ImageUtil;

public class GDALDatasetWrapper {

	/** The LOGGER for this class. */
	private static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.gdalframework");
	/**
	 * the name of the driver which has opened the dataset held by this wrapper.
	 */
	private String driverName;

	/**
	 * The description of the driver which has opened the dataset held by this
	 * wrapper.
	 */
	private String driverDescription;

	/** The dataset name */
	private String datasetName;

	/** The dataset description */
	private String datasetDescription;

	/** The grid to world transformation. */
	private double[] geoTransformation = new double[6];

	/** The data set projection. */
	private String projection;

	/** The number of Ground Control Points */
	private int gcpNumber;

	/** The GCP's Projection */
	private String gcpProjection;

	/**
	 * The list of Ground Control Points. <BR>
	 * Any Ground Control Point has the following fields:<BR>
	 * <UL>
	 * <LI>ID: Unique Identifier</LI>
	 * <LI>Info: Informational message/description</LI>
	 * <LI>x: GCPPixel -----> Pixel (x) location of GCP on Raster</LI>
	 * <LI>y: GCPLine ------> Line(y) location of GCP on Raster</LI>
	 * <LI>lon: GCPX -------> X position of GCP in Georeferenced Space</LI>
	 * <LI>lat: GCPY -------> Y position of GCP in Georeferenced Space</LI>
	 * <LI>elevation: GCPZ -> elevation of GCP in Georeferenced Space</LI>
	 * </UL>
	 */
	private List gcps;

	/** The raster width */
	private int width;

	/** The raster height */
	private int height;

	/** The raster tile height */
	private int tileHeight;

	/** The raster tile width */
	private int tileWidth;

	/** The <code>ColorModel</code> used for the dataset */
	private ColorModel colorModel;

	/** The <code>SampleModel</code> used for the dataset */
	private SampleModel sampleModel;

	// ////////////////////////////////////////////////
	// 
	// Band Properties
	//
	// ////////////////////////////////////////////////

	/** Number of bands */
	private int bandsNumber;

	/** Array to store the maximum value for each band */
	private Double[] maximums;

	/** Array to store the minimum value for each band */
	private Double[] minimums;

	/** Array to store the noData value for each band */
	private Double[] noDataValues;

	/** Array to store the scale value for each band */
	private Double[] scales;

	/** Array to store the offset value for each band */
	private Double[] offsets;

	/** Array to store the number of numOverviews for each band */
	private int[] numOverviews;

	/** Array to store the color interpretation for each band */
	private int[] colorInterpretations;

	private IIOMetadata iioMetadata;

	/**
	 * <code>GDALDatasetWrapper</code> constructor. Firstly, it provides to
	 * open a dataset from the specified input dataset name. Then, it call the
	 * constructor which initializes all fields with dataset properties, such as
	 * raster size, raster tiling properties, projection, and more.
	 * 
	 * @param sDatasetName
	 *            The name (usually a File path or a subdataset name when the
	 *            format supports subdatasets) of the dataset we want to open.
	 */
	public GDALDatasetWrapper(String sDatasetName) {
		this(GDALUtilities.acquireDataSet(sDatasetName, gdalconst.GA_ReadOnly),
				sDatasetName);
	}
	
	protected GDALDatasetWrapper() {
		
	}

	/**
	 * <code>GDALDatasetWrapper</code> constructor.
	 * 
	 * @param dataset
	 *            the input <code>Dataset</code> on which build the wrapper.
	 * @param name
	 *            the name to be set for the dataset contained on this wrapper.
	 * @param initializationRequired
	 *            specify if initializing wrapper members is required or not.
	 */
	public GDALDatasetWrapper(Dataset dataset, String name,
			final boolean initializationRequired) {
		datasetName = name;
		datasetDescription = dataset.GetDescription();
		driverDescription = dataset.GetDriver().GetDescription();
		driverName = dataset.GetDriver().getShortName();
		if (initializationRequired)
			setMembers(dataset);
		setGeoreferencingInfo(dataset);
		// clean up data set in order to avoid keeping them around for a lot
		// of time.
		GDALUtilities.closeDataSet(dataset);
	}

	/**
	 * Constructor which initializes fields by retrieving properties such as
	 * raster size, raster tiling properties, projection, and more from a given
	 * input <code>Dataset</code>.
	 * 
	 * @param dataset
	 *            the <code>Dataset</code> used to initialize all
	 *            {@link GDALDatasetWrapper}'s fields.
	 * @param name
	 *            the dataset name
	 */
	public GDALDatasetWrapper(Dataset dataset, String name) {
		this(dataset, name, true);

	}

	/**
	 * A kind of initialization method which provides to set all fields of the
	 * {@link GDALDatasetWrapper}
	 * 
	 * @param dataset
	 *            the <code>Dataset</code> which will be used for the
	 *            initialization
	 * @return <code>true</code> if the initialization was successfully
	 *         completed. <code>false</code> if some field wasn't properly
	 *         initialized
	 */
	private boolean setMembers(Dataset dataset) {
		// Retrieving raster properties
		width = dataset.getRasterXSize();
		height = dataset.getRasterYSize();

		// Retrieving block size
		final int[] xBlockSize = new int[1];
		final int[] yBlockSize = new int[1];

		// Remember: RasterBand numeration starts from 1
		dataset.GetRasterBand(1).GetBlockSize(xBlockSize, yBlockSize);
		tileHeight = yBlockSize[0];
		tileWidth = xBlockSize[0];
		if (((long) tileHeight) * ((long) tileWidth) > Integer.MAX_VALUE)
			performTileSizeTuning(dataset);

		// /////////////////////////////////////////////////////////////////
		//
		// Getting dataset main properties
		//
		// /////////////////////////////////////////////////////////////////
		bandsNumber = dataset.getRasterCount();
		if (bandsNumber <= 0)
			return false;
		// final int xsize = dataset.getRasterXSize();
		// final int ysize = dataset.getRasterYSize();

		// If the image is very big, its size expressed as the number of
		// bytes needed to store pixels, may be a negative number
		final int tileSize = tileWidth
				* tileHeight
				* bandsNumber
				* (gdal.GetDataTypeSize(dataset.GetRasterBand(1).getDataType()) / 8);

		// bands variables
		final int[] banks = new int[bandsNumber];
		final int[] offsetsR = new int[bandsNumber];
		noDataValues = new Double[bandsNumber];
		scales = new Double[bandsNumber];
		offsets = new Double[bandsNumber];
		minimums = new Double[bandsNumber];
		maximums = new Double[bandsNumber];
		numOverviews = new int[bandsNumber];
		colorInterpretations = new int[bandsNumber];
		int buf_type = 0;

		Band pBand = null;

		// scanning bands
		final Double tempD[] = new Double[1];
		final int bandsOffset[] = new int[bandsNumber];
		for (int band = 0; band < bandsNumber; band++) {
			/* Bands are not 0-base indexed, so we must add 1 */
			pBand = dataset.GetRasterBand(band + 1);
			buf_type = pBand.getDataType();
			banks[band] = band;
			offsetsR[band] = 0;
			pBand.GetNoDataValue(tempD);
			noDataValues[band] = tempD[0];
			pBand.GetOffset(tempD);
			offsets[band] = tempD[0];
			pBand.GetScale(tempD);
			scales[band] = tempD[0];
			pBand.GetMinimum(tempD);
			minimums[band] = tempD[0];
			pBand.GetMaximum(tempD);
			maximums[band] = tempD[0];
			colorInterpretations[band] = pBand.GetRasterColorInterpretation();
			numOverviews[band] = pBand.GetOverviewCount();
			bandsOffset[band] = band;
		}

		// /////////////////////////////////////////////////////////////////
		//
		// Variable used to specify the data type for the storing samples
		// of the SampleModel
		//
		// /////////////////////////////////////////////////////////////////
		int buffer_type = 0;
		if (buf_type == gdalconstConstants.GDT_Byte)
			buffer_type = DataBuffer.TYPE_BYTE;
		else if (buf_type == gdalconstConstants.GDT_UInt16)
			buffer_type = DataBuffer.TYPE_USHORT;
		else if (buf_type == gdalconstConstants.GDT_Int16)
			buffer_type = DataBuffer.TYPE_SHORT;
		else if ((buf_type == gdalconstConstants.GDT_Int32)
				|| (buf_type == gdalconstConstants.GDT_UInt32))
			buffer_type = DataBuffer.TYPE_INT;
		else if (buf_type == gdalconstConstants.GDT_Float32)
			buffer_type = DataBuffer.TYPE_FLOAT;
		else if (buf_type == gdalconstConstants.GDT_Float64)
			buffer_type = DataBuffer.TYPE_DOUBLE;
		else
			return false;

		// //
		//
		// Setting the Sample Model
		//
		// Here you have a nice trick. If you check the SampleMOdel class
		// you'll see that there is an actual limitation on the width and
		// height of an image that we can create that is it the product
		// width*height cannot be bigger than the maximum integer.
		//
		// Well a way to pass beyond that is to use TileWidth and TileHeight
		// instead of the real width and height when creating the sample
		// model. It will work!
		// //
		if (tileSize < 0)
			sampleModel = new BandedSampleModel(buffer_type, tileWidth,
					tileHeight, tileWidth, banks, offsetsR);
		else
			sampleModel = new PixelInterleavedSampleModel(buffer_type,
					tileWidth, tileHeight, bandsNumber,
					tileWidth * bandsNumber, bandsOffset);

		// //
		//
		// Setting the Color Model
		//
		// //
		if (pBand.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex) {
			colorModel = pBand.GetRasterColorTable().getIndexColorModel(
					gdal.GetDataTypeSize(buf_type));
		} else {
			ColorSpace cs = null;
			if (bandsNumber > 1) {
				// /////////////////////////////////////////////////////////////////
				//
				// Number of Bands > 1.
				// ImageUtil.createColorModel provides to Creates a
				// ColorModel that may be used with the specified
				// SampleModel
				//
				// /////////////////////////////////////////////////////////////////
				colorModel = ImageUtil.createColorModel(sampleModel);
				if (colorModel == null) {
					LOGGER.severe("No ColorModels found");
					return false;
				}

			} else if ((buffer_type == DataBuffer.TYPE_BYTE)
					|| (buffer_type == DataBuffer.TYPE_USHORT)
					|| (buffer_type == DataBuffer.TYPE_INT)
					|| (buffer_type == DataBuffer.TYPE_FLOAT)
					|| (buffer_type == DataBuffer.TYPE_DOUBLE)) {

				// Just one band. Using the built-in Gray Scale Color Space
				cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
				colorModel = RasterFactory.createComponentColorModel(
						buffer_type, // dataType
						cs, // color space
						false, // has alpha
						false, // is alphaPremultiplied
						Transparency.OPAQUE); // transparency
			} else {
				if (buffer_type == DataBuffer.TYPE_SHORT) {
					// Just one band. Using the built-in Gray Scale Color
					// Space
					cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
					colorModel = new ComponentColorModel(cs, false, false,
							Transparency.OPAQUE, DataBuffer.TYPE_SHORT);
				}
			}
		}
		if (colorModel == null || sampleModel == null)
			return false;
		return true;
	}

	/**
	 * Given an input <code>Dataset</code>, sets georeferencing information
	 * 
	 * @param dataset
	 *            a <code>Dataset</code> from where to retrieve all
	 *            georeferencing information available
	 */
	private void setGeoreferencingInfo(Dataset dataset) {
		// Setting CRS's related information
		dataset.GetGeoTransform(geoTransformation);
		projection = dataset.GetProjection();
		gcpProjection = dataset.GetGCPProjection();
		gcpNumber = dataset.GetGCPCount();
	}

	/**
	 * Provides to increase data access performances. In many cases, the block
	 * size for a raster band is a single line of N pixels, where N is the width
	 * of the raster.
	 * 
	 * The Java Advanced Imaging allows to load and manipulate data only when
	 * they are needed (The Deferred Execution Model). This is done by working
	 * only on tiles containing the required data.
	 * 
	 * However, reading a big image composed of tiles having a size of Nx1 (The
	 * most commonly used block size) would be not optimized because for each
	 * tile, a read operation is computed (very small tiles -> very high read
	 * operations number)
	 * 
	 * In order to optimize data access operations, it would be better to make
	 * tiles a little bit greater than just a single line of pixels.
	 * 
	 * @param dataset
	 */
	private void performTileSizeTuning(Dataset dataset) {
		final int width = dataset.getRasterXSize();
		final int height = dataset.getRasterYSize();
		final Dimension imageSize = new Dimension(width, height);
		final Dimension tileSize = GDALUtilities.toTileSize(imageSize);
		tileHeight = tileSize.height;
		tileWidth = tileSize.width;
	}

	// ////////////////////////////////////////////////////////////////////
	// Simple Set of getters
	// ////////////////////////////////////////////////////////////////////

	/**
	 * return the <code>ColorModel</code> for the dataset held by this
	 * wrapper.
	 */
	public final ColorModel getColorModel() {
		return colorModel;
	}

	/**
	 * Get the Image metadata available for the dataset held by this wrapper.
	 */
	public synchronized IIOMetadata getImageIOMetadata() {
		if (iioMetadata == null) {
			iioMetadata = getIIOImageMetadata();
		}
		return iioMetadata;
	}

	/**
	 * return the name of the dataset held by this wrapper.
	 */
	public final String getDatasetName() {
		return datasetName;
	}

	/**
	 * return the <code>SampleModel</code> for the dataset held by this
	 * wrapper.
	 */
	public final SampleModel getSampleModel() {
		return sampleModel;
	}

	/**
	 * return the tile height of the raster held by this wrapper.
	 */
	public final int getTileHeight() {
		return tileHeight;
	}

	/**
	 * return the tile width of the raster held by this wrapper.
	 */
	public final int getTileWidth() {
		return tileWidth;
	}

	/**
	 * return the width of the raster held by this wrapper.
	 */
	public final int getWidth() {
		return width;
	}

	/**
	 * return the height of the raster held by this wrapper.
	 */
	public final int getHeight() {
		return height;
	}

	/**
	 * return the number of bands of the raster held by this wrapper.
	 */
	public final int getBandsNumber() {
		return bandsNumber;
	}

	/**
	 * return the grid to world transformation for the dataset held by this
	 * wrapper.
	 */
	public final double[] getGeoTransformation() {
		return geoTransformation;
	}

	/**
	 * return the number of Ground Control Points for the dataset held by this
	 * wrapper.
	 */
	public final int getGcpNumber() {
		return gcpNumber;
	}

	/**
	 * return the array storing the maximum value for each band. It may contain
	 * <code>null</code> objects
	 */
	public final Double[] getMaximums() {
		return (Double[]) maximums.clone();
	}

	/**
	 * return the array storing the minimum value for each band. It may contain
	 * <code>null</code> objects
	 */
	public final Double[] getMinimums() {
		return (Double[]) minimums.clone();
	}

	/**
	 * return the array storing the offset value for each band. It may contain
	 * <code>null</code> objects
	 */
	public final Double[] getOffsets() {
		return (Double[]) offsets.clone();
	}

	/**
	 * return the array storing the scale value for each band. It may contain
	 * <code>null</code> objects
	 */
	public final Double[] getScales() {
		return (Double[]) scales.clone();
	}

	/**
	 * return the array storing the nodata value for each band. It may contain
	 * <code>null</code> objects
	 */
	public final Double[] getNoDataValues() {
		return (Double[]) noDataValues.clone();
	}

	/**
	 * return the array storing the number of overviews value for each band
	 */
	public final int[] getNumOverviews() {
		return numOverviews;
	}

	/**
	 * return the dataset projection
	 */
	public final String getProjection() {
		return projection;
	}

	/**
	 * return the Ground Control Point's projection.
	 */
	public String getGcpProjection() {
		return gcpProjection;
	}

	/** return the driver name */
	public final String getDriverName() {
		return driverName;
	}

	/** return a list of Ground Control Points for the datase */
	public synchronized final List getGcps() {
		if (gcps == null) {
			gcps = new Vector(gcpNumber);
			final Dataset ds = GDALUtilities.acquireDataSet(datasetName,
					gdalconst.GA_ReadOnly);
			ds.GetGCPs((Vector) gcps);
			GDALUtilities.closeDataSet(ds);
		}
		return Collections.unmodifiableList(gcps);

	}

	/** return the driver description */
	public final String getDriverDescription() {
		return driverDescription;
	}

	/** return the dataset description */
	public final String getDatasetDescription() {
		return datasetDescription;
	}

	/** return the color interpretation for the dataset */
	public final int[] getColorInterpretations() {
		return colorInterpretations;
	}

	/**
	 * Provide a proper <code>IIOMetadata</code> object for the specified
	 * {@link GDALDatasetWrapper}.
	 * 
	 * Default implementation returns an instance of
	 * {@link GDALCommonIIOImageMetadata}. In case you need a subclassed
	 * metadata, extends the {@link GDALDatasetWrapper} and override the
	 * {@link GDALDatasetWrapper#getIIOImageMetadata(GDALDatasetWrapper) method.
	 */
	protected IIOMetadata getIIOImageMetadata() {
		return new GDALCommonIIOImageMetadata(this);
	}
}
