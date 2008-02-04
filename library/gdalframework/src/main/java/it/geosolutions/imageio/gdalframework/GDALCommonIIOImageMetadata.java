/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *	  https://imageio-ext.dev.java.net/
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
package it.geosolutions.imageio.gdalframework;

import java.awt.Dimension;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SampleModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.media.jai.RasterFactory;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.GCP;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.gdalconst.gdalconstConstants;
import org.w3c.dom.Node;

import com.sun.media.imageioimpl.common.ImageUtil;

/**
 * Class needed to store all available information of a GDAL Dataset with the
 * add of additional information. For convenience and future re-use this class
 * also represents an {@link IIOMetadata}. A wide set of getters method allow
 * to retrieve several information directly from the metadata instance, without
 * need of getting the XML DOM nodes tree.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class GDALCommonIIOImageMetadata extends IIOMetadata implements
		Cloneable {

	// protected Object clone() throws CloneNotSupportedException {
	// GDALCommonIIOImageMetadata metadata = new GDALCommonIIOImageMetadata();
	// metadata.driverName = this.driverName;
	// metadata.driverDescription = this.driverDescription;
	// metadata.datasetName = this.datasetName;
	// metadata.datasetDescription = this.datasetDescription;
	// metadata.projection = this.projection;
	// metadata.gcpNumber = this.gcpNumber;
	// metadata.gcpProjection = this.gcpProjection;
	// metadata.geoTransformation = (double[]) this.geoTransformation.clone();
	// if (this.gdalMetadataMap != null) {
	// Map inputMap = this.gdalMetadataMap;
	// Map map = Collections.synchronizedMap(new HashMap(inputMap.size()));
	// final Iterator outKeys = inputMap.keySet().iterator();
	// while (outKeys.hasNext()) {
	// final String key = (String) outKeys.next();
	// final Map valuesMap = (Map) inputMap.get(key);
	// final Iterator inKeys = valuesMap.keySet().iterator();
	// final Map innerMap = new HashMap(valuesMap.size());
	// while (inKeys.hasNext()) {
	// final String ikey = (String) inKeys.next();
	// final String value = (String) valuesMap.get(ikey);
	// innerMap.put(ikey, value);
	// }
	// map.put(key, innerMap);
	// }
	// metadata.gdalMetadataMap = map;
	// }
	// // TODO: Need to clone GCPs ... but actually JVM crashes when getting
	// // GCPs
	// metadata.width = this.width;
	// metadata.height = this.height;
	// metadata.tileHeight = this.tileHeight;
	// metadata.tileWidth = this.tileWidth;
	//
	// // TODO: clone sampleModel and color Model
	// // metadata.colorModel = new ColorModel()
	// // metadata.sampleModel= new SampleModel()
	// metadata.numBands = this.numBands;
	//
	// metadata.maximums = (Double[]) this.maximums.clone();
	// metadata.minimums = (Double[]) this.minimums.clone();
	// metadata.noDataValues = (Double[]) this.noDataValues.clone();
	// metadata.scales = (Double[]) this.scales.clone();
	// metadata.offsets = (Double[]) this.offsets.clone();
	//
	// metadata.numOverviews = (int[]) this.numOverviews.clone();
	// metadata.colorInterpretations = (int[]) this.colorInterpretations
	// .clone();
	// return metadata;
	// }

	/** The LOGGER for this class. */
	private static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.gdalframework");

	public static final String nativeMetadataFormatName = "it.geosolutions.imageio.gdalframework.commonImageMetadata_1.0";

	public static final String nativeMetadataFormatClassName = "it.geosolutions.imageio.gdalframework.GDALCommonIIOImageMetadataFormat";

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
	protected String datasetName;

	/** The dataset description */
	protected String datasetDescription;

	/** The data set projection. */
	protected String projection;

	/** The number of Ground Control Points */
	private int gcpNumber;

	/** The GCP's Projection */
	private String gcpProjection;

	/** The grid to world transformation. */
	protected double[] geoTransformation = new double[6];

	/**
	 * A map containing an HashMap for each domain if available (the Default
	 * domain, the ImageStructure domain, as well as any xml prefixed domain)
	 */
	protected Map gdalMetadataMap;

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
	protected int width;

	/** The raster height */
	protected int height;

	/** The raster tile height */
	protected int tileHeight;

	/** The raster tile width */
	protected int tileWidth;

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
	protected int numBands;

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

	/**
	 * <code>GDALCommonIIOImageMetadata</code> constructor. Firstly, it
	 * provides to open a dataset from the specified input dataset name. Then,
	 * it call the constructor which initializes all fields with dataset
	 * properties, such as raster size, raster tiling properties, projection,
	 * and more.
	 * 
	 * @param sDatasetName
	 *            The name (usually a File path or a subdataset name when the
	 *            format supports subdatasets) of the dataset we want to open.
	 */
	public GDALCommonIIOImageMetadata(String sDatasetName) {
		this(sDatasetName, nativeMetadataFormatName,
				nativeMetadataFormatClassName);
	}

	/**
	 * <code>GDALCommonIIOImageMetadata</code> constructor. Firstly, it
	 * provides to open a dataset from the specified input dataset name. Then,
	 * it call the constructor which initializes all fields with dataset
	 * properties, such as raster size, raster tiling properties, projection,
	 * and more.
	 * 
	 * @param sDatasetName
	 *            The name (usually a File path or a subdataset name when the
	 *            format supports subdatasets) of the dataset we want to open.
	 * @param formatName
	 *            the name of the native metadata format
	 * @param formatClassName
	 *            the name of the class of the native metadata format
	 */
	public GDALCommonIIOImageMetadata(String sDatasetName, String formatName,
			String formatClassName) {
		this(GDALUtilities.acquireDataSet(sDatasetName, gdalconst.GA_ReadOnly),
				sDatasetName, formatName, formatClassName);
	}

	/**
	 * <code>GDALCommonIIOImageMetadata</code> constructor.
	 * 
	 * @param dataset
	 *            the input <code>Dataset</code> on which build the wrapper.
	 * @param name
	 *            the name to be set for the dataset contained on this wrapper.
	 * @param initializationRequired
	 *            specify if initializing wrapper members is required or not.
	 * @param formatName
	 *            the name of the native metadata format
	 * @param formatClassName
	 *            the name of the class of the native metadata format
	 */
	public GDALCommonIIOImageMetadata(Dataset dataset, String name,
			final boolean initializationRequired, final String formatName,
			final String formatClassName) {
		super(false, formatName, formatClassName, null, null);
		datasetName = name;
		if (dataset == null)
			return;
		datasetDescription = dataset.GetDescription();
		driverDescription = dataset.GetDriver().GetDescription();
		driverName = dataset.GetDriver().getShortName();
		gdalMetadataMap = Collections.synchronizedMap(new HashMap(2));

		// //
		//
		// Getting Metadata from Default domain and Image_structure domain
		//
		// //
		Map defMap = dataset
				.GetMetadata_Dict(GDALUtilities.GDALMetadataDomain.DEFAULT);
		if (defMap != null && defMap.size() > 0)
			gdalMetadataMap.put(
					GDALUtilities.GDALMetadataDomain.DEFAULT_KEY_MAP, defMap);

		Map imageStMap = dataset
				.GetMetadata_Dict(GDALUtilities.GDALMetadataDomain.IMAGESTRUCTURE);
		if (imageStMap != null && imageStMap.size() > 0)
			gdalMetadataMap
					.put(GDALUtilities.GDALMetadataDomain.IMAGESTRUCTURE,
							imageStMap);

		// //
		//
		// Initializing member if needed
		//
		// //
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
	 * @param formatName
	 *            the name of the native metadata format
	 * @param formatClassName
	 *            the name of the class of the native metadata format
	 */
	public GDALCommonIIOImageMetadata(Dataset dataset, String name,
			final String formatName, final String formatClassName) {
		this(dataset, name, true, formatName, formatClassName);
	}

	/**
	 * Constructor which initializes fields by retrieving properties such as
	 * raster size, raster tiling properties, projection, and more from a given
	 * input <code>Dataset</code> if not null.
	 * 
	 * @param dataset
	 *            the <code>Dataset</code> used to initialize all
	 *            {@link GDALDatasetWrapper}'s fields.
	 * @param name
	 *            the dataset name
	 * 
	 */
	public GDALCommonIIOImageMetadata(Dataset dataset, String name,
			final boolean initializationRequired) {
		this(dataset, name, initializationRequired, nativeMetadataFormatName,
				nativeMetadataFormatClassName);
	}

	private GDALCommonIIOImageMetadata() {
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
		numBands = dataset.getRasterCount();
		if (numBands <= 0)
			return false;
		// final int xsize = dataset.getRasterXSize();
		// final int ysize = dataset.getRasterYSize();

		// If the image is very big, its size expressed as the number of
		// bytes needed to store pixels, may be a negative number
		final int tileSize = tileWidth
				* tileHeight
				* numBands
				* (gdal.GetDataTypeSize(dataset.GetRasterBand(1).getDataType()) / 8);

		// bands variables
		final int[] banks = new int[numBands];
		final int[] offsetsR = new int[numBands];
		noDataValues = new Double[numBands];
		scales = new Double[numBands];
		offsets = new Double[numBands];
		minimums = new Double[numBands];
		maximums = new Double[numBands];
		numOverviews = new int[numBands];
		colorInterpretations = new int[numBands];
		int buf_type = 0;

		Band pBand = null;

		// scanning bands
		final Double tempD[] = new Double[1];
		final int bandsOffset[] = new int[numBands];
		for (int band = 0; band < numBands; band++) {
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
		//
		// //
		if (tileSize < 0)
			sampleModel = new BandedSampleModel(buffer_type, tileWidth,
					tileHeight, tileWidth, banks, offsetsR);
		else
			sampleModel = new PixelInterleavedSampleModel(buffer_type,
					tileWidth, tileHeight, numBands, tileWidth * numBands,
					bandsOffset);

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
			if (numBands > 1) {
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

	private Node createCommonNativeTree() {
		// Create root node
		final IIOMetadataNode root = new IIOMetadataNode(
				nativeMetadataFormatName);

		// ////////////////////////////////////////////////////////////////////
		//
		// DatasetDescriptor
		//
		// ////////////////////////////////////////////////////////////////////
		IIOMetadataNode node = new IIOMetadataNode("DatasetDescriptor");
		node.setAttribute("name", datasetName);
		node.setAttribute("description", datasetDescription);
		node.setAttribute("driverName", driverName);
		node.setAttribute("driverDescription", driverDescription);
		node.setAttribute("projection", projection);
		node.setAttribute("numGCPs", Integer.toString(gcpNumber));
		node.setAttribute("gcpProjection", gcpProjection);
		root.appendChild(node);

		// ////////////////////////////////////////////////////////////////////
		//
		// RasterDimensions
		//
		// ////////////////////////////////////////////////////////////////////
		node = new IIOMetadataNode("RasterDimensions");
		node.setAttribute("width", Integer.toString(width));
		node.setAttribute("height", Integer.toString(height));
		node.setAttribute("tileWidth", Integer.toString(tileWidth));
		node.setAttribute("tileHeight", Integer.toString(tileHeight));
		node.setAttribute("numBands", Integer.toString(numBands));
		root.appendChild(node);

		// ////////////////////////////////////////////////////////////////////
		//
		// GeoTransform
		//
		// ////////////////////////////////////////////////////////////////////
		node = new IIOMetadataNode("GeoTransform");
		final boolean hasgeoTransform = geoTransformation != null
				&& geoTransformation.length > 0;
		node.setAttribute("m0", hasgeoTransform ? Double
				.toString(geoTransformation[0]) : null);
		node.setAttribute("m1", hasgeoTransform ? Double
				.toString(geoTransformation[1]) : null);
		node.setAttribute("m2", hasgeoTransform ? Double
				.toString(geoTransformation[2]) : null);
		node.setAttribute("m3", hasgeoTransform ? Double
				.toString(geoTransformation[3]) : null);
		node.setAttribute("m4", hasgeoTransform ? Double
				.toString(geoTransformation[4]) : null);
		node.setAttribute("m5", hasgeoTransform ? Double
				.toString(geoTransformation[5]) : null);
		root.appendChild(node);

		// ////////////////////////////////////////////////////////////////////
		//
		// GCPS
		//
		// ////////////////////////////////////////////////////////////////////
		if (gcpNumber > 0) {
			IIOMetadataNode nodeGCPs = new IIOMetadataNode("GCPS");
			final List gcps = getGcps();
			final Iterator it = gcps.iterator();
			while (it.hasNext()) {
				node = new IIOMetadataNode("GCP");
				final GCP gcp = (GCP) it.next();
				node.setAttribute("x", Double.toString(gcp.getGCPPixel()));
				node.setAttribute("y", Double.toString(gcp.getGCPLine()));
				node.setAttribute("id", gcp.getId());
				node.setAttribute("info", gcp.getInfo());
				node.setAttribute("lon", Double.toString(gcp.getGCPX()));
				node.setAttribute("lat", Double.toString(gcp.getGCPY()));
				node.setAttribute("elevation", Double.toString(gcp.getGCPZ()));
				nodeGCPs.appendChild(node);
			}
			root.appendChild(nodeGCPs);
		}

		// ////////////////////////////////////////////////////////////////////
		//
		// BandsInfo
		//
		// ////////////////////////////////////////////////////////////////////
		IIOMetadataNode bandsNode = new IIOMetadataNode("BandsInfo");

		// //
		//
		// BandsInfo -> BandInfo
		//
		// //
		for (int i = 0; i < numBands; i++) {
			node = new IIOMetadataNode("BandInfo");
			node.setAttribute("index", Integer.toString(i));
			node.setAttribute("colorInterpretation",
					colorInterpretations != null
							& colorInterpretations.length > i ? Integer
							.toBinaryString(colorInterpretations[i]) : "");
			node.setAttribute("noData",
					noDataValues != null && noDataValues.length > i
							&& noDataValues[i] != null ? noDataValues[i]
							.toString() : null);
			node.setAttribute("maximum", maximums != null
					&& maximums.length > i && maximums[i] != null ? maximums[i]
					.toString() : null);
			node.setAttribute("minimum", minimums != null
					&& minimums.length > i && minimums[i] != null ? minimums[i]
					.toString() : null);
			node.setAttribute("scale", scales != null && scales.length > i
					&& scales[i] != null ? scales[i].toString() : null);
			node.setAttribute("offset", offsets != null && offsets.length > i
					&& offsets[i] != null ? offsets[i].toString() : null);
			node.setAttribute("numOverviews", numOverviews != null
					&& numOverviews.length > i ? Integer
					.toString(numOverviews[i]) : null);
			bandsNode.appendChild(node);
		}

		// ////////////////////////////////////////////////////////////////////
		//
		// BandsInfo -> BandInfo -> ColorTable
		//
		// ////////////////////////////////////////////////////////////////////
		if (colorModel instanceof IndexColorModel) {
			final IndexColorModel icm = (IndexColorModel) colorModel;
			final int mapSize = icm.getMapSize();
			IIOMetadataNode node1 = new IIOMetadataNode("ColorTable");
			node1.setAttribute("sizeOfLocalColorTable", Integer
					.toString(mapSize));
			final byte rgb[][] = new byte[3][mapSize];
			icm.getReds(rgb[0]);
			icm.getReds(rgb[1]);
			icm.getReds(rgb[2]);
			for (int i = 0; i < mapSize; i++) {
				IIOMetadataNode nodeEntry = new IIOMetadataNode(
						"ColorTableEntry");
				nodeEntry.setAttribute("index", Integer.toString(i));
				nodeEntry.setAttribute("red", Byte.toString(rgb[0][i]));
				nodeEntry.setAttribute("green", Byte.toString(rgb[1][i]));
				nodeEntry.setAttribute("blue", Byte.toString(rgb[2][i]));
				nodeEntry.setAttribute("alpha", Byte.toString(rgb[3][i]));
				node1.appendChild(nodeEntry);
			}
			node.appendChild(node1);
		}
		root.appendChild(bandsNode);
		return root;
	}

	public Node getAsTree(String formatName) {
		if (nativeMetadataFormatName.equalsIgnoreCase(formatName))
			return createCommonNativeTree();
		throw new UnsupportedOperationException(formatName
				+ " is not a supported format name");
	}

	public boolean isReadOnly() {
		return true;
	}

	public void mergeTree(String formatName, Node root)
			throws IIOInvalidTreeException {
		throw new UnsupportedOperationException(
				"mergeTree operation is not allowed");
	}

	public void reset() {
		throw new UnsupportedOperationException(
				"reset operation is not allowed");
	}

	// ////////////////////////////////////////////////////////////////////////
	//
	// Dataset and Driver Properties
	// 
	// ////////////////////////////////////////////////////////////////////////
	/**
	 * Return the name of the dataset which is the source for this
	 * <code>IIOMetadata</code>
	 */
	public String getDatasetName() {
		return datasetName;
	}

	/**
	 * Return the description of the dataset which is the source for this
	 * <code>IIOMetadata</code>
	 */
	public String getDescription() {
		return datasetDescription;
	}

	/**
	 * Return the name of the GDAL driver used to open the source dataset for
	 * this <code>IIOMetadata</code>
	 */
	public String getDriverName() {
		return driverName;
	}

	/**
	 * Return the description of the GDAL driver used to open the source dataset
	 * for this <code>IIOMetadata</code>
	 */
	public String getDriverDescription() {
		return driverDescription;
	}

	// ////////////////////////////////////////////////////////////////////////
	//
	// Raster Properties
	// 
	// ////////////////////////////////////////////////////////////////////////
	/**
	 * Return the number of bands of the dataset which is the source for this
	 * <code>IIOMetadata</code>
	 */
	public int getNumBands() {
		return numBands;
	}

	/** Return the width of the image */
	public int getWidth() {
		return width;
	}

	/** Return the height of the image */
	public int getHeight() {
		return height;
	}

	/** Return the tile height of the image */
	public int getTileHeight() {
		return tileHeight;
	}

	/** Return the tile width of the image */
	public int getTileWidth() {
		return tileWidth;
	}

	/**
	 * return the <code>ColorModel</code> for the dataset held by this object.
	 */
	public final ColorModel getColorModel() {
		return colorModel;
	}

	/**
	 * return the <code>SampleModel</code> for the dataset held by this
	 * object.
	 */
	public final SampleModel getSampleModel() {
		return sampleModel;
	}

	// ////////////////////////////////////////////////////////////////////////
	//
	// Referencing
	// 
	// ////////////////////////////////////////////////////////////////////////
	/** Return the projection */
	public String getProjection() {
		return projection;
	}

	/** Return the grid to world transformation of the image */
	public double[] getGeoTransformation() {
		return (double[]) geoTransformation.clone();
	}

	/** Return the number of Ground Control Points */
	public int getGcpNumber() {
		return gcpNumber;
	}

	/** return the Ground Control Point's projection */
	public String getGcpProjection() {
		return gcpProjection;
	}

	/** return the Ground Control Points */
	public synchronized final List getGcps() {
		// TODO: actually the Java bindings do not work properly when getting
		// GCPs (the JVM crash). Uncomment the following code when the method
		// getting GCPs works fine
		//
		//
		// if (gcps == null) {
		// gcps = new Vector(gcpNumber);
		// final Dataset ds = GDALUtilities.acquireDataSet(datasetName,
		// gdalconst.GA_ReadOnly);
		// ds.GetGCPs((Vector) gcps);
		// GDALUtilities.closeDataSet(ds);
		// }
		// return Collections.unmodifiableList(gcps);
		return null;
	}

	// ////////////////////////////////////////////////////////////////////////
	//
	// Bands Properties
	// 
	// ////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the number of overviews for the specified band
	 * 
	 * @param bandIndex
	 *            the index of the required band
	 */
	public int getNumOverviews(final int bandIndex) {
		checkBandIndex(bandIndex);
		return numOverviews[bandIndex];
	}

	/**
	 * Returns the maximum value for the specified band
	 * 
	 * @param bandIndex
	 *            the index of the required band
	 * @throws IllegalArgumentException
	 *             in case the specified band number is out of range or maximum
	 *             value has not been found
	 */
	public double getMaximum(final int bandIndex)
			throws IllegalArgumentException {
		checkBandIndex(bandIndex);
		if (maximums != null) {
			Double maximum = maximums[bandIndex];
			if (maximum != null)
				return maximum.doubleValue();
		}
		throw new IllegalArgumentException(
				"no maximum value available for the specified band "
						+ bandIndex);
	}

	/**
	 * Returns the minimum value for the specified band
	 * 
	 * @param bandIndex
	 *            the index of the required band
	 * @throws IllegalArgumentException
	 *             in case the specified band number is out of range or minimum
	 *             value has not been found
	 */
	public double getMinimum(final int bandIndex)
			throws IllegalArgumentException {
		checkBandIndex(bandIndex);
		if (minimums != null) {
			Double minimum = minimums[bandIndex];
			if (minimum != null)
				return minimum.doubleValue();
		}
		throw new IllegalArgumentException(
				"no minimum value available for the specified band "
						+ bandIndex);
	}

	/**
	 * Returns the scale value for the specified band
	 * 
	 * @param bandIndex
	 *            the index of the required band
	 * @throws IllegalArgumentException
	 *             in case the specified band number is out of range or scale
	 *             value has not been found
	 */
	public double getScale(final int bandIndex) throws IllegalArgumentException {
		checkBandIndex(bandIndex);
		if (scales != null) {
			Double scale = scales[bandIndex];
			if (scale != null)
				return scale.doubleValue();
		}
		throw new IllegalArgumentException(
				"no scale value available for the specified band " + bandIndex);
	}

	/**
	 * Returns the offset value for the specified band
	 * 
	 * @param bandIndex
	 *            the index of the required band
	 * @throws IllegalArgumentException
	 *             in case the specified band number is out of range or offset
	 *             value has not been found
	 */
	public double getOffset(final int bandIndex)
			throws IllegalArgumentException {
		checkBandIndex(bandIndex);
		if (offsets != null) {
			Double offset = offsets[bandIndex];
			if (offset != null)
				return offset.doubleValue();
		}
		throw new IllegalArgumentException(
				"no Offset value available for the specified band " + bandIndex);
	}

	/**
	 * Returns the noDataValue value for the specified band
	 * 
	 * @param bandIndex
	 *            the index of the required band
	 * @throws IllegalArgumentException
	 *             in case the specified band number is out of range or
	 *             noDataValue has not been found
	 */
	public double getNoDataValue(final int bandIndex)
			throws IllegalArgumentException {
		checkBandIndex(bandIndex);
		if (noDataValues != null) {
			Double noDataValue = noDataValues[bandIndex];
			if (noDataValue != null)
				return noDataValue.doubleValue();
		}
		throw new IllegalArgumentException(
				"no noDataValue available for the specified band " + bandIndex);
	}

	/**
	 * Check the validity of the specified band index. Band indexes are in the
	 * range [0, numBands -1 ]
	 * 
	 * @param bandIndex
	 *            the band index to be validated.
	 * @throws IllegalArgumentException
	 *             in case the specified band index isn't in the valid range
	 */
	private void checkBandIndex(final int bandIndex)
			throws IllegalArgumentException {
		if (bandIndex < 0 || bandIndex > numBands) {
			final StringBuffer sb = new StringBuffer("Specified band index (")
					.append(bandIndex).append(
							") is out of range. It should be in the range [0,")
					.append(numBands - 1).append("]");
			throw new IllegalArgumentException(sb.toString());
		}
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

	/**
	 * Returns a Map representing metadata elements (key,value) for a specific
	 * domain of GDAL metadata.
	 * 
	 * @param metadataDomain
	 *            the requested GDAL metadata domain.
	 * 
	 * @see GDALUtilities.GDALMetadataDomain
	 * @return the metadata mapping for the specified domain or
	 *         <code>null</code> in case no metadata is available for the
	 *         domain or the specified domain is unsupported.
	 */
	protected Map getGdalMetadataDomain(final String metadataDomain) {
		if (metadataDomain
				.equalsIgnoreCase(GDALUtilities.GDALMetadataDomain.DEFAULT)) {
			if (gdalMetadataMap
					.containsKey(GDALUtilities.GDALMetadataDomain.DEFAULT_KEY_MAP))
				return (Map) gdalMetadataMap
						.get(GDALUtilities.GDALMetadataDomain.DEFAULT_KEY_MAP);
		} else if (metadataDomain
				.equalsIgnoreCase(GDALUtilities.GDALMetadataDomain.IMAGESTRUCTURE)
				|| metadataDomain
						.startsWith(GDALUtilities.GDALMetadataDomain.XML_PREFIX)) {
			if (gdalMetadataMap.containsKey(metadataDomain))
				return (Map) gdalMetadataMap.get(metadataDomain);
		}
		return null;
	}

	/**
	 * Return all the available metadata domains.
	 * 
	 * @return a list of <code>String</code>s representing metadata domains
	 *         defined for the dataset on which this instance is based.
	 */
	protected List getGdalMetadataDomainsList() {
		final Set keys = gdalMetadataMap.keySet();
		List list = null;
		// //
		// 
		// Since the GDAL default metadata domain is an empty String (which
		// can't be used as a key of a map), I need a minor tuning leveraging
		// on a valid String (see
		// GDALUtilities.GDALMetadataDomain.DEFAULT_KEY_MAP)
		//
		// //
		if (keys != null) {
			final Iterator keysIt = keys.iterator();
			list = new ArrayList(keys.size());
			while (keysIt.hasNext()) {
				final String key = (String) keysIt.next();
				if (key
						.equals(GDALUtilities.GDALMetadataDomain.DEFAULT_KEY_MAP))
					list.add(GDALUtilities.GDALMetadataDomain.DEFAULT);
				else
					list.add(key);
			}
		}
		return list;
	}
}
