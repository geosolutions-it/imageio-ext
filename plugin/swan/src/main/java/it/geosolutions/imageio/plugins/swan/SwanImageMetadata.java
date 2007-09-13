/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
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
package it.geosolutions.imageio.plugins.swan;

import it.geoSolutions.jiioExt.swan.SwanHeaderDocument.SwanHeader.Datasets.Dataset;
import it.geosolutions.imageio.plugins.swan.raster.SwanRaster;
import it.geosolutions.imageio.plugins.swan.utility.UomConverter;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.units.Unit;

import org.w3c.dom.Node;


/**
 * This class represents metadata associated with images and streams.
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini(Simboss)
 */
public final class SwanImageMetadata extends SwanBaseMetadata {
	public static final String nativeMetadataFormatName = "it.geosolutions.imageio.plugins.swan.SwanImageMetadata_1.0";

	public static final String[] metadataFormatNames = { nativeMetadataFormatName };

	private double noData = Double.NaN;

	private String shortName = null;
	
	private String longName = null;
	
	private Unit uom = null;

	public SwanImageMetadata(SwanRaster raster,final int imageIndex) {
		this();
		initializeFromRaster(raster, imageIndex);
	}

	/**
	 * Initialize Metadata from a raster
	 * @param raster		
	 * 			the <code>SwanRaster</code> from which retrieve data
	 * @param imageIndex
	 * 			the imageIndex relying the required subdataset
	 */
	private void initializeFromRaster(SwanRaster raster, int imageIndex) {
		super.initializeFromRaster(raster);
		
		//Getting the number of different quantities
		int nDatasets = raster.getNDatasets();
		int index = imageIndex % nDatasets;
		
		//retrieving the dataset related to the specified index
		Dataset dataset = raster.getDatasets().getDatasetArray(index);
		
		//getting dataset properties
		shortName = dataset.getShortName();
		if (shortName==null)
			shortName="";
		longName = dataset.getLongName();
		if (longName ==null)
			longName="";
		noData=dataset.getNoDataValue();
		uom=UomConverter.getUnit(dataset.getUnitOfMeasure());
	}

	public SwanImageMetadata() {
		super(false, nativeMetadataFormatName,
				"it.geosolutions.imageio.plugins.swan.SwanImageMetadataFormat",
				null, null);
	}
	/**
	 * returns the image metadata in a tree corresponding to the provided
	 * formatName
	 * 
	 * @param formatName
	 *            The format Name
	 * 
	 * @return 
	 * 
	 * @throws IllegalArgumentException
	 *             if the formatName is not one of the supported format names
	 */
	public Node getAsTree(String formatName) {
		if (formatName.equals(nativeMetadataFormatName)) {
			return getNativeTree();
		} else if (formatName
				.equals(IIOMetadataFormatImpl.standardMetadataFormatName)) {
			return getStandardTree();
		} else {
			throw new IllegalArgumentException("Not a recognized format!");
		}
	}

	/**
	 * @see javax.imageio.metadata.IIOMetadata#isReadOnly()
	 */
	public boolean isReadOnly() {
		return false;
	}

	/**
	 * @see javax.imageio.metadata.IIOMetadata#mergeTree(java.lang.String,
	 *      org.w3c.dom.Node)
	 */
	public void mergeTree(String formatName, Node root)
			throws IIOInvalidTreeException {

	}

	/**
	 * @see javax.imageio.metadata.IIOMetadata#reset()
	 */
	public void reset() {
		super.reset();
		shortName="";
		longName="";
		uom=null;
		noData = Double.NaN;
	}

	/**
	 * IIOMetadataFormat objects are meant to describe the structure of metadata
	 * returned from the getAsTree method.
	 * 
	 * @param formatName
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws IllegalArgumentException
	 *             DOCUMENT ME!
	 */
	public IIOMetadataFormat getMetadataFormat(String formatName) {
		if (formatName.equals(nativeMetadataFormatName)) {
			return new SwanImageMetadataFormat();
		}

		throw new IllegalArgumentException("Not a recognized format!");
	}

	/**
	 * @return the root of the Tree containing Metadata in NativeFormat
	 */
	private Node getNativeTree() {
		final IIOMetadataNode root = new IIOMetadataNode(
				nativeMetadataFormatName);

		/**
		 * Setting Dataset Properties
		 */

		IIOMetadataNode datasetNode = new IIOMetadataNode("dataset");
		datasetNode.setAttribute("shortName", shortName);
		datasetNode.setAttribute("longName", longName);
		datasetNode.setAttribute("unitOfMeasure", uom.toString() );
		datasetNode.setAttribute("noDataValue", Double.toString(noData));
		root.appendChild(datasetNode);

		/**
		 * Setting Raster Properties
		 */

		IIOMetadataNode rasterNode = new IIOMetadataNode("raster");
		rasterNode.setAttribute("nColumns", Integer.toString(nCols));
		rasterNode.setAttribute("nRows", Integer.toString(nRows));
		rasterNode.setAttribute("precision", Integer.toString(decimalDigits));
		root.appendChild(rasterNode);
		
		/**
		 * Setting Envelope Properties
		 */

		IIOMetadataNode envelopeNode = new IIOMetadataNode("envelope");
		envelopeNode.setAttribute("xll", Double.toString(xll));
		envelopeNode.setAttribute("yll", Double.toString(yll));
		envelopeNode.setAttribute("xur", Double.toString(xur));
		envelopeNode.setAttribute("yur", Double.toString(yur));
		envelopeNode.setAttribute("rasterSpace", rasterSpace);
		root.appendChild(envelopeNode);
		return root;
	}

}
