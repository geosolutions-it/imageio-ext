/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.plugins.arcgrid;

import it.geosolutions.imageio.plugins.arcgrid.raster.AsciiGridRaster;
import it.geosolutions.imageio.plugins.arcgrid.raster.AsciiGridRaster.AsciiGridRasterType;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;

import org.w3c.dom.Node;

/**
 * This class represents metadata associated with images and streams.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions. 
 */
public final class AsciiGridsImageMetadata extends IIOMetadata {
	/**
	 * Two available values to define raster space as defined in GeoTiff
	 * specifications. <BR>
	 * 
	 * @see <a
	 *      href="http://www.remotesensing.org/geotiff/spec/geotiff2.5.html#2.5.2.2">GeoTiff
	 *      specifications: RasterSpace</a>
	 */
	public enum RasterSpaceType{
		PixelIsPoint,
		PixelIsArea,
		Undefined;
		
		public RasterSpaceType getDefault(){
			return PixelIsArea;
		}
	}
	

	/** the native metadata format name */
	public static final String nativeMetadataFormatName = "it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageMetadata_1.0";

	/**
	 * the list of supported metadata format names. In this case, only the
	 * native metadata format is supported.
	 */
	public static final String[] metadataFormatNames = { nativeMetadataFormatName };


	/** the value used to represent noData for an element of the raster */
	private double noData = Double.NaN;

	/** The size of a single cell of the grid along X */
	private double cellSizeX;

	/** The size of a single cell of the grid along Y */
	private double cellSizeY;

	/** The number of columns of the raster */
	private int nCols;

	/** The number of rows of the raster */
	private int nRows;

	/** x coordinate of the grid origin (the lower left corner) */
	private double lowerLeftX = 0.0;

	/** y coordinate of the grid origin (the lower left corner) */
	private double lowerLeftY = 0.0;

	/**
	 * the rasterSpace Type which is one of the values contained in
	 * <code>rasterSpaceTypes</code> array
	 */
	private RasterSpaceType rasterSpaceType = null;

	private AsciiGridRasterType rasterFileType;

	/**
	 * A constructor which uses an input {@link AsciiGridRaster} to initialize
	 * metadata fields
	 * 
	 * @param raster
	 *            input {@link AsciiGridRaster} used to retrieve properties to
	 *            set inner fields
	 */
	public AsciiGridsImageMetadata(AsciiGridRaster raster) {
		this();
		inizializeFromRaster(raster);
	}

	/**
	 * Default constructor
	 */
	public AsciiGridsImageMetadata() {
		super(
				false,
				nativeMetadataFormatName,
				"it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageMetadataFormat",
				null, null);
	}

	/**
	 * A special constructor which uses parameters provided by the client, to
	 * set inner fields
	 * 
	 * @param cols
	 *            the number of columns
	 * @param rows
	 *            the number of rows
	 * @param cellsizeX
	 *            the x size of the grid cell
	 * @param cellsizeY
	 *            the y size of the grid cell
	 * @param xll
	 *            the xllCellCoordinate of the Bounding Box
	 * @param yll
	 *            the yllCellCoordinate of the Bounding Box
	 * @param isCorner
	 *            true if xll represents the xllCorner
	 * @param grass
	 *            true if the Ascii grid is Grass
	 * @param inNoData
	 *            the value associated to noData grid values
	 */
	public AsciiGridsImageMetadata(int cols, int rows, double cellsizeX,
			double cellsizeY, double xll, double yll, boolean isCorner,boolean grass,double inNoData) {
		this();
		rasterFileType=grass?AsciiGridRasterType.GRASS:AsciiGridRasterType.ESRI;
		nCols = cols;
		nRows = rows;
		lowerLeftX = xll;
		lowerLeftY = yll;
		rasterSpaceType = isCorner ? RasterSpaceType.PixelIsArea:  RasterSpaceType.PixelIsPoint;
		cellSizeX = cellsizeX;
		cellSizeY = cellsizeY;
		noData = inNoData;

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
		cellSizeX = cellSizeY = lowerLeftX = lowerLeftY = -1;
		nCols = nRows = -1;
		rasterSpaceType = RasterSpaceType.Undefined;
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
		if (formatName.equals(nativeMetadataFormatName))
			return new AsciiGridsImageMetadataFormat();

		throw new IllegalArgumentException("Not a recognized format!");
	}

	/**
	 * This method uses access methods of the inputRaster to determine values
	 * needed for metadata initialization
	 * 
	 * @param inputRaster
	 *            the input {@link AsciiGridRaster} used to initialize fields.
	 */
	private void inizializeFromRaster(AsciiGridRaster inputRaster) {
		if (inputRaster != null) {
			nRows = inputRaster.getNRows();
			nCols = inputRaster.getNCols();
			noData = inputRaster.getNoData();

			cellSizeX = inputRaster.getCellSizeX();
			cellSizeY = inputRaster.getCellSizeY(); 

			if (inputRaster.isCorner()) {
				rasterSpaceType = RasterSpaceType.PixelIsArea;
			} else
				rasterSpaceType = RasterSpaceType.PixelIsPoint;

			lowerLeftX = inputRaster.getXllCellCoordinate();
			lowerLeftY = inputRaster.getYllCellCoordinate();

			rasterFileType=inputRaster.getRasterType();
			
		}else
			throw new NullPointerException("Null inputRaster provided.");
	}

	/**
	 * Standard tree node methods
	 */
	protected IIOMetadataNode getStandardChromaNode() {
		IIOMetadataNode node = new IIOMetadataNode("Chroma");

		IIOMetadataNode subNode = new IIOMetadataNode("ColorSpaceType");
		String colorSpaceType = "GRAY";
		subNode.setAttribute("name", colorSpaceType);
		node.appendChild(subNode);

		subNode = new IIOMetadataNode("NumChannels");

		String numChannels = "1";
		subNode.setAttribute("value", numChannels);
		node.appendChild(subNode);

		return node;
	}

	protected IIOMetadataNode getStandardCompressionNode() {
		IIOMetadataNode node = new IIOMetadataNode("Compression");

		// CompressionTypeName
		IIOMetadataNode subNode = new IIOMetadataNode("Lossless");
		subNode.setAttribute("value", "TRUE");
		node.appendChild(subNode);

		return node;
	}

	/**
	 * @return the root of the Tree containing Metadata in NativeFormat
	 */
	private Node getNativeTree() {
		final IIOMetadataNode root = new IIOMetadataNode(
				nativeMetadataFormatName);

		// //
		// Setting Format Properties
		// //
		IIOMetadataNode node = new IIOMetadataNode("formatDescriptor");
		node.setAttribute("GRASS", Boolean.toString(rasterFileType.equals(AsciiGridRasterType.GRASS)));
		root.appendChild(node);

		// //
		// Setting Grid Properties
		// //
		node = new IIOMetadataNode("gridDescriptor");
		node.setAttribute("nColumns", Integer.toString(nCols));
		node.setAttribute("nRows", Integer.toString(nRows));
		node.setAttribute("rasterSpaceType", rasterSpaceType.toString());
		node.setAttribute("noDataValue", Double.toString(noData));

		root.appendChild(node);

		// //
		// Setting Envelope Properties
		// //
		node = new IIOMetadataNode("envelopeDescriptor");
		node.setAttribute("cellsizeX", Double.toString(cellSizeX));
		node.setAttribute("cellsizeY", Double.toString(cellSizeY));
		node.setAttribute("xll", Double.toString(lowerLeftX));
		node.setAttribute("yll", Double.toString(lowerLeftY));
		root.appendChild(node);

		return root;
	}
}
