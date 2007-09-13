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

import it.geosolutions.imageio.plugins.swan.raster.SwanRaster;

import java.util.GregorianCalendar;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.units.Unit;

import org.joda.time.DateTime;
import org.w3c.dom.Node;

/**
 * This class represents metadata associated with images and streams.
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini(Simboss)
 */
public final class SwanStreamMetadata extends SwanBaseMetadata {
	public static final String nativeMetadataFormatName = "it.geosolutions.imageio.plugins.swan.SwanStreamMetadata_1.0";

	public static final String[] metadataFormatNames = { nativeMetadataFormatName };

	protected int nDatasets;

	protected int nTau;
	
	private int tauTime;

	private Unit tauUom;

	private String datasetNames[] = null;
	
	private String zone;

	private GregorianCalendar baseTime;

	public SwanStreamMetadata(SwanRaster raster) {
		this();
		initializeFromRaster(raster);

		// Setting stream properties
		nDatasets = raster.getNDatasets();
		nTau = raster.getNTaus();
		tauUom = raster.getTauUom();
		tauTime = raster.getTauTime();
		datasetNames = raster.getDatasetNames();
		baseTime = raster.getBaseTime();
		zone = raster.getZone();
	}

	public SwanStreamMetadata() {
		super(false, nativeMetadataFormatName,
				"it.geosolutions.imageio.plugins.swan.SwanStreamMetadataFormat",
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
		nDatasets = -1;
		nTau = -1;
		tauTime = -1;
		tauUom = null;
		datasetNames = null;
		baseTime = null;
		zone = null;
	}

	public IIOMetadataFormat getMetadataFormat(String formatName) {
		if (formatName.equals(nativeMetadataFormatName)) {
			return new SwanStreamMetadataFormat();
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
		 * Setting general Properties
		 */
		IIOMetadataNode generalNode = new IIOMetadataNode("general");
		generalNode.setAttribute("datasetNumber", Integer.toString(nDatasets));
		generalNode.setAttribute("tauNumber", Integer.toString(nTau));
		
		DateTime dt = new DateTime(baseTime);
		final String baseTimeString = dt.toString(SwanRaster.dtf);
		generalNode.setAttribute("baseTime", baseTimeString);
		generalNode.setAttribute("zone", zone);
		
		/**
		 * Setting Envelope Properties
		 */

		IIOMetadataNode envelopeNode = new IIOMetadataNode("envelope");
		envelopeNode.setAttribute("xll", Double.toString(xll));
		envelopeNode.setAttribute("yll", Double.toString(yll));
		envelopeNode.setAttribute("xur", Double.toString(xur));
		envelopeNode.setAttribute("yur", Double.toString(yur));
		envelopeNode.setAttribute("rasterSpace", rasterSpace);
		generalNode.appendChild(envelopeNode);

		// datasets
		IIOMetadataNode datasetNamesNode = new IIOMetadataNode("datasetNames");
		final int datasets = nDatasets;

		for (int i = 0; i < datasets; i++) {
			IIOMetadataNode datasetNode = new IIOMetadataNode("dataset");
			datasetNode.setAttribute("name", datasetNames[i]);
			datasetNamesNode.appendChild(datasetNode);
		}
		generalNode.appendChild(datasetNamesNode);

		// tau
		IIOMetadataNode tauNode = new IIOMetadataNode("tau");
		tauNode.setAttribute("time", Integer.toString(tauTime));
		tauNode.setAttribute("unitOfMeasure", tauUom.toString());
		generalNode.appendChild(tauNode);

		root.appendChild(generalNode);

		/**
		 * Setting raster Properties
		 */
		IIOMetadataNode rasterNode = new IIOMetadataNode("raster");
		rasterNode.setAttribute("nColumns", Integer.toString(nCols));
		rasterNode.setAttribute("nRows", Integer.toString(nRows));
		rasterNode.setAttribute("precision", Integer.toString(decimalDigits));

		root.appendChild(rasterNode);

		return root;
	}
}
