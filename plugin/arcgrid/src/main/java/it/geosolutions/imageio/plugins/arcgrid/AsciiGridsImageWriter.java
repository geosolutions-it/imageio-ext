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

import it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageMetadata.RasterSpaceType;
import it.geosolutions.imageio.plugins.arcgrid.raster.AsciiGridRaster;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.ImageOutputStreamImpl;
import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Class used for writing ASCII ArcGrid Format and ASCII GRASS Grid Format
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions.
 */
public final class AsciiGridsImageWriter extends ImageWriter {
	
	private static final Logger LOGGER = Logger.getLogger(AsciiGridsImageWriter.class.toString());

	/** <code>true</code> if there are some listeners attached to this writer */
	private boolean hasListeners;

	public static final double EPS = 1E-3;

	/** The {@link AsciiGridsImageMetadata} associated to this writer. */
	private AsciiGridsImageMetadata imageMetadata = null;

	/** The <code>ImageOutputStream</code> associated to this reader. */
	private ImageOutputStream imageOutputStream;

	/** The {@link AsciiGridRaster} to write rasters to an ascii grid file. */
	private AsciiGridRaster rasterWriter = null;

	/** The input source RenderedImage */
	private PlanarImage inputRenderedImage;

	/** Tells me  if the related file is GRASS or ESRI*/
	private AsciiGridRaster.AsciiGridRasterType rasterType;

	/** The number of columns of the raster */
	private int nColumns;

	/** The number of rows of the raster */
	private int nRows;

	/** The size of a single cell of the grid along X */
	private double cellsizeX;

	/** The size of a single cell of the grid along Y */
	private double cellsizeY;

	/**
	 * The <code>String</code> representing the rasterSpaceType which is one
	 * of "PixelIsPoint" / "PixelIsArea"
	 */
	private RasterSpaceType rasterSpaceType;

	/**
	 * The <code>String</code> to be written when a noData value is
	 * encountered during pixel writing
	 */
	private String noDataValueString;

	/** x coordinate of the grid origin (the lower left corner) */
	private double xll;

	/** y coordinate of the grid origin (the lower left corner) */
	private double yll;

	public ImageWriteParam getDefaultWriteParam() {
		return new AsciiGridsImageWriteParam(getLocale());
	}

	/**
	 * Constructor.
	 * 
	 * It builts up an {@link AsciiGridsImageWriter} by providing an
	 * {@link ImageWriterSpi} as input
	 * 
	 * @param originatingProvider
	 *            the originating service provider interface
	 */
	public AsciiGridsImageWriter(ImageWriterSpi originatingProvider) {
		super(originatingProvider);
	}

	/**
	 * Sets the output for this {@link AsciiGridsImageWriter}.
	 */
	public void setOutput(Object output) {
		super.setOutput(output); // validates output
		if (output != null) {
			if (!(output instanceof ImageOutputStream)) {
				throw new IllegalArgumentException(
						"Not a valid type of Output ");
			}
			imageOutputStream = (ImageOutputStreamImpl) output;

		} else {
			imageOutputStream = null;
//			throw new IllegalArgumentException("Not a valid type of Output ");
		}
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.info("Setting Output");
	}

	/**
	 * Writes the image to file. (First, it writes the Header, than all data
	 * values)
	 * 
	 * @see javax.imageio.ImageWriter#write(javax.imageio.metadata.IIOMetadata,
	 *      javax.imageio.IIOImage, javax.imageio.ImageWriteParam)
	 */
	public void write(IIOMetadata streamMetadata, IIOImage image,
			ImageWriteParam param) throws IOException {

		hasListeners = (this.progressListeners != null && (!(this.progressListeners
				.isEmpty()))) ? true : false;

		if (hasListeners) {
			clearAbortRequest();
			// Broadcast the start of the image write operation
			processImageStarted(0);
		}

		// Getting the source
		inputRenderedImage = PlanarImage.wrapRenderedImage(image
				.getRenderedImage());

		// Getting metadata to write the file header.
		imageMetadata = (AsciiGridsImageMetadata) image.getMetadata();
		// TODO: METADATA MANAGEMENT IF NO METADATA PROVIDED

		final Node root = imageMetadata
				.getAsTree(AsciiGridsImageMetadata.nativeMetadataFormatName);

		// retrieving from metadata, fields to be written in the header
		retrieveMetadata(root);

		// Writing out the Header
		writeHeader();

		// writing the raster
		writeRaster();

		// flush the data written out
		imageOutputStream.flush();

		if (hasListeners) {
			// Checking the status of the write operation (aborted/completed)
			if (rasterWriter.isAborting())
				processWriteAborted();
			else
				processImageComplete();
		}

	}

	/**
	 * Initialize all required fields which will be written to the header.
	 * 
	 * @param root
	 *            The root node containing metadata
	 * @throws IOException
	 */
	private void retrieveMetadata(Node root) throws IOException {

		// //
		//
		// Grass
		//
		// //
		final Node formatDescriptorNode = root.getFirstChild();
		if(Boolean.valueOf(formatDescriptorNode.getAttributes().getNamedItem("GRASS").getNodeValue()).booleanValue()) 
					rasterType = AsciiGridRaster.AsciiGridRasterType.GRASS;
		else
					rasterType = AsciiGridRaster.AsciiGridRasterType.ESRI;

		// //
		//
		// Grid description
		//
		// //
		
		final Node gridDescriptorNode = formatDescriptorNode.getNextSibling();
		NamedNodeMap attributes = gridDescriptorNode.getAttributes();
		nColumns = Integer.parseInt(attributes.getNamedItem("nColumns").getNodeValue());
		nRows = Integer.parseInt(attributes.getNamedItem("nRows").getNodeValue());
		rasterSpaceType = RasterSpaceType.valueOf(attributes
				.getNamedItem("rasterSpaceType").getNodeValue());
		noDataValueString = null;
		
		Node dummyNode = attributes.getNamedItem("noDataValue");
		if (dummyNode != null) {
			noDataValueString = dummyNode.getNodeValue();
		}

		// //
		//
		// Spatial dimensions
		//
		// //
		final Node envelopDescriptorNode = gridDescriptorNode.getNextSibling();
		cellsizeX = Double.parseDouble(envelopDescriptorNode.getAttributes()
				.getNamedItem("cellsizeX").getNodeValue());
		cellsizeY = Double.parseDouble(envelopDescriptorNode.getAttributes()
				.getNamedItem("cellsizeY").getNodeValue());
		xll = Double.parseDouble(envelopDescriptorNode.getAttributes()
				.getNamedItem("xll").getNodeValue());
		yll = Double.parseDouble(envelopDescriptorNode.getAttributes()
				.getNamedItem("yll").getNodeValue());

		// //
		//
		// Checking if the dimensions of the current image are different from
		// the original dimensions as provided by the metadata. This might
		// happen if we scale on reading or if we make a mistake when providing
		// the metadata.
		//
		// As an alternative for ImageReadOp images with source subsampling we
		// could look for the image read params.
		//
		// //
		final int actualWidth = this.inputRenderedImage.getWidth();
		final int actualHeight = this.inputRenderedImage.getHeight();
		cellsizeX *= nColumns / actualWidth;
		cellsizeY *= nRows / actualHeight;
		if (rasterType.equals(AsciiGridRaster.AsciiGridRasterType.GRASS)){
			// If ArcGrid (No GRASS) check to have square cell Size
			if (resolutionCheck(cellsizeX, cellsizeX, EPS))
				throw new IOException(
						"The provided metadata are illegal!CellSizeX!=CellSizeY.");
		}

	}

	/**
	 * Simple check for having squre pixels.
	 * 
	 * @param cellsizeX 
	 * @param cellsizeY
	 * @param eps tolerance for the check.
	 * @return <code>true</code> if pixels are square (or almost square),
	 *         <code>false</code> otherwise.
	 */
	public static boolean resolutionCheck(double cellsizeX, double cellsizeY,
			double eps) {
		return (Math.abs(cellsizeX - cellsizeY)/Math.min(cellsizeX, cellsizeY)) > eps;
	}

	/**
	 * Write the raster to file.
	 * 
	 * @throws IOException
	 */
	private void writeRaster() throws IOException {
		// we need to cobble rasters of the same row together in order to
		// respect the way our writer works.

		final RectIter iterator = RectIterFactory.create(inputRenderedImage,
				null);
		// writing
		final Double noDataDouble = new Double(rasterWriter.getNoData());
		final String noDataMarker = rasterWriter.getNoDataMarker();
		rasterWriter.writeRaster(iterator, noDataDouble, noDataMarker);
	}

	/**
	 * Instantiates a proper {@link AsciiGridRaster} (ArcGrid/GRASS) and write
	 * the Header.
	 * 
	 * @throws IOException
	 */
	private void writeHeader() throws IOException {
		rasterWriter = rasterType.createAsciiGridRaster(imageOutputStream, this);
		

		rasterWriter.writeHeader(Integer.toString(nColumns), Integer
				.toString(nRows), Double.toString(xll), Double.toString(yll),
				Double.toString(cellsizeX), Double.toString(cellsizeY),
				rasterSpaceType.toString(), noDataValueString);
	}

	/**
	 * @see javax.imageio.ImageWriter#getDefaultImageMetadata(javax.imageio.ImageTypeSpecifier,
	 *      javax.imageio.ImageWriteParam)
	 */
	public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier its,
			ImageWriteParam param) {
		return null;
	}

	/**
	 * @see javax.imageio.ImageWriter#getDefaultIStreamMetadata(javax.imageio.ImageWriteParam)
	 */
	public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
		return null;
	}

	/**
	 * @see javax.imageio.ImageWriter#convertStreamMetadata(javax.imageio.metadata.IIOMetadata,
	 *      javax.imageio.ImageWriteParam)
	 */
	public IIOMetadata convertStreamMetadata(IIOMetadata md,
			ImageWriteParam param) {
		return null;
	}

	/**
	 * @see javax.imageio.ImageWriter#convertImageMetadata(javax.imageio.metadata.IIOMetadata,
	 *      javax.imageio.ImageTypeSpecifier, javax.imageio.ImageWriteParam)
	 */
	public IIOMetadata convertImageMetadata(IIOMetadata md,
			ImageTypeSpecifier its, ImageWriteParam param) {
		return md;
	}

	final class AsciiGridsImageWriteParam extends ImageWriteParam {

		AsciiGridsImageWriteParam(Locale locale) {
			super(locale);
			compressionMode = MODE_DISABLED;
			canWriteCompressed = true;
		}

		public void setCompressionMode(int mode) {
			if (mode == MODE_EXPLICIT || mode == MODE_COPY_FROM_METADATA) {
				throw new UnsupportedOperationException(
						"mode == MODE_EXPLICIT || mode == MODE_COPY_FROM_METADATA");
			}

			super.setCompressionMode(mode); // This sets the instance variable.
		}

		public void unsetCompression() {
			super.unsetCompression(); // Performs checks.
		}
	}

	/**
	 * Cleans this {@link AsciiGridsImageWriter}.
	 */
	public void dispose() {
		if (imageOutputStream != null)
			try {
				imageOutputStream.flush();
				imageOutputStream.close();
			} catch (IOException ioe) {

			}
		imageOutputStream = null;
		super.dispose();
	}

	public synchronized void abort() {
		// super.abort();
		if (rasterWriter != null)
			rasterWriter.abort();
	}

	protected synchronized boolean abortRequested() {
		return rasterWriter.isAborting();
		// return super.abortRequested();
	}

	protected synchronized void clearAbortRequest() {
		// super.clearAbortRequest();
		if (rasterWriter != null)
			rasterWriter.clearAbort();
	}

	public void processImageProgress(float percentageDone) {
		super.processImageProgress(percentageDone);
	}

	public int getNColumns() {
		return nColumns;
	}

	public int getNRows() {
		return nRows;
	}

	public boolean isHasListeners() {
		return hasListeners;
	}

	public void reset() {
		super.reset();
		imageOutputStream=null;
		imageMetadata=null;
		rasterWriter=null;
		inputRenderedImage=null;
	}

}
