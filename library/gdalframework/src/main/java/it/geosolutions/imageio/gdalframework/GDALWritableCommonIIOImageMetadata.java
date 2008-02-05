package it.geosolutions.imageio.gdalframework;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class extending {@link GDALCommonIIOImageMetadata} in order to provide write
 * capabilities to the metadata instance. It is worth to point out that this
 * class doesn't work on an underlying dataset. It simply allows to define a
 * {@link GDALImageWriter}'s understandable metadata object.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class GDALWritableCommonIIOImageMetadata extends
		GDALCommonIIOImageMetadata {

	public static final String nativeMetadataFormatName = "it.geosolutions.imageio.gdalframework.writablecommonImageMetadata_1.0";

	private static final String DEFAULT_DATASET_NAME = "dummydataset";

	/**
	 * Default constructor of <code>GDALWritableCommonIIOImageMetadata</code>.
	 */
	public GDALWritableCommonIIOImageMetadata() {
		this(DEFAULT_DATASET_NAME);
	}

	/**
	 * Constructor of <code>GDALWritableCommonIIOImageMetadata</code>.
	 */
	public GDALWritableCommonIIOImageMetadata(final String datasetName) {
		super(null, datasetName, false);
		gdalMetadataMap = Collections.synchronizedMap(new HashMap(2));
	}

	/**
	 * Set the projection for this object
	 * 
	 * @param projection
	 *            a <code>String</code> specifying the projection expressed as
	 *            a WKT.
	 */
	public void setProjection(final String projection) {
		this.projection = projection;
	}

	/**
	 * Set the geoTransformation for this object
	 * 
	 * @param geoTransformation
	 *            an array containing the 6 coefficients defining the
	 *            geoTransformation
	 */
	public void setGeoTransformation(double[] geoTransformation) {
		if (geoTransformation != null) {
			if (geoTransformation.length != 6)
				throw new IllegalArgumentException(
						"The specified geoTransformation is invalid.\n"
								+ "A valid geoTransformation is composed of 6"
								+ " coefficients while the specified array has"
								+ "length " + geoTransformation.length);
			this.geoTransformation = (double[]) geoTransformation.clone();
		}
	}

	/**
	 * Set the dataset name for this object
	 * 
	 * @param datasetName
	 *            the name to be associated to this object
	 */
	public void setDatasetName(final String datasetName) {
		this.datasetName = datasetName;
	}

	/**
	 * Set the description for this object
	 * 
	 * @param datasetDescription
	 *            the description to be associated to this object
	 */
	public void setDatasetDescription(String datasetDescription) {
		this.datasetDescription = datasetDescription;
	}

	/**
	 * Set the width for this object
	 * 
	 * @param width
	 *            the width to be associated to this object
	 */
	public void setWidth(final int width) {
		this.width = width;
	}

	/**
	 * Set the height for this object
	 * 
	 * @param height
	 *            the height to be associated to this object
	 */
	public void setHeight(final int height) {
		this.height = height;
	}

	/**
	 * Set the tile height for this object
	 * 
	 * @param tileHeight
	 *            the tile height to be associated to this object
	 */
	public void setTileHeight(final int tileHeight) {
		this.tileHeight = tileHeight;
	}

	/**
	 * Set the tile width for this object
	 * 
	 * @param tileWidth
	 *            the tile width to be associated to this object
	 */
	public void setTileWidth(final int tileWidth) {
		this.tileWidth = tileWidth;
	}

	/**
	 * Set the number of bands for this object
	 * 
	 * @param numBands
	 *            the number of bands to be associated to this object
	 */
	public void setNumBands(final int numBands) {
		this.numBands = numBands;
	}

	/**
	 * Set the metadata for a specific domain.
	 * 
	 * @param metadataNameValuePairs
	 *            a <code>Map</code> containing name-value pairs where each
	 *            pair represents a metadata element.
	 * @param domain
	 *            the domain where the metadata need to be stored.
	 * @see GDALUtilities.GDALMetadataDomain <BR>
	 *      TODO: future version could check for already existent key or provide
	 *      a step-to-step single metadata item setting
	 * @throws IllegalArgumentException
	 *             in case the specified domain is unsupported.
	 */
	public synchronized void setGdalMetadataDomain(Map metadataNameValuePairs,
			String domain) {
		if (domain == null
				|| domain.length() > 0
				&& (!domain.equals(GDALUtilities.GDALMetadataDomain.DEFAULT)
						|| !domain
								.equals(GDALUtilities.GDALMetadataDomain.IMAGESTRUCTURE) || !domain
						.startsWith(GDALUtilities.GDALMetadataDomain.XML_PREFIX)))
			throw new IllegalArgumentException("Unsupported domain");
		if (domain.equals(GDALUtilities.GDALMetadataDomain.DEFAULT)
				|| domain.length() == 0)
			domain = GDALUtilities.GDALMetadataDomain.DEFAULT_KEY_MAP;
		gdalMetadataMap.put(domain, metadataNameValuePairs);
	}
}
