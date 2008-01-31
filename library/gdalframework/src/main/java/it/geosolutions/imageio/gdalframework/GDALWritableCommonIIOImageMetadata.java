package it.geosolutions.imageio.gdalframework;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.metadata.IIOInvalidTreeException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

	public GDALWritableCommonIIOImageMetadata() {
		this(DEFAULT_DATASET_NAME);
	}

	public GDALWritableCommonIIOImageMetadata(final String datasetName) {
		super(null, datasetName, false);
		gdalMetadataMap = Collections.synchronizedMap(new HashMap(2));
	}

	public void setProjection(String projection) {
		this.projection = projection;
	}

	public void setGeoTransformation(double[] geoTransformation) {
		this.geoTransformation = (double[]) geoTransformation.clone();
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public void setDatasetDescription(String datasetDescription) {
		this.datasetDescription = datasetDescription;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	public void setHeight(final int height) {
		this.height = height;
	}

	public void setTileHeight(final int tileHeight) {
		this.tileHeight = tileHeight;
	}

	public void setTileWidth(final int tileWidth) {
		this.tileWidth = tileWidth;
	}

	public void setBandsNumber(final int nBands) {
		this.numBands = nBands;
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
		if (!domain.equals(GDALUtilities.GDALMetadataDomain.DEFAULT)
				|| !domain
						.equals(GDALUtilities.GDALMetadataDomain.IMAGESTRUCTURE)
				|| !domain
						.startsWith(GDALUtilities.GDALMetadataDomain.XML_PREFIX))
			throw new IllegalArgumentException("Unsupported domain");
		if (domain.equals(GDALUtilities.GDALMetadataDomain.DEFAULT))
			domain = GDALUtilities.GDALMetadataDomain.DEFAULT_KEY_MAP;
		gdalMetadataMap.put(domain, metadataNameValuePairs);
	}
}
