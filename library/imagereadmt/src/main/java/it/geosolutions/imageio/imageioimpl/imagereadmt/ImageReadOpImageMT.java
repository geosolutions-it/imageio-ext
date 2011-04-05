/*
 * $RCSfile: ImageReadOpImageMT.java,v $
 *
 * 
 * Copyright (c) 2005 Sun Microsystems, Inc. All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 
 * 
 * - Redistribution of source code must retain the above copyright 
 *   notice, this  list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in 
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL 
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR 
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES. 
 * 
 * You acknowledge that this software is not designed or intended for 
 * use in the design, construction, operation or maintenance of any 
 * nuclear facility. 
 *
 * $Revision: 1.2 $
 * $Date: 2006/07/14 21:43:57 $
 * $State: Exp $
 */
/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
 *    All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of GeoSolutions nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY GeoSolutions ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GeoSolutions BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package it.geosolutions.imageio.imageioimpl.imagereadmt;

import it.geosolutions.imageio.imageioimpl.EnhancedImageReadParam;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.OpImage;

import com.sun.media.jai.operator.ImageReadDescriptor;

/**
 * ------------------------------ NOTE ------------------------------  
 * Class based on SUN's JAI-ImageIO 
 * com.sun.media.jai.imageioimpl.ImageReadOpImage class.
 * ------------------------------------------------------------------  
 * 
 * Implementation of the <code>OpImage</code> of the "ImageRead" operation.
 * 
 * @author Simone Giannecchini, GeoSolutions
 */
final class ImageReadOpImageMT extends OpImage {

	/**
	 * The <code>ImageReadParam</code> used in reading the image.
	 */
	private EnhancedImageReadParam param;

	/**
	 * The <code>ImageReader</code> used to read the image.
	 */
	private ImageReader reader;

	/**
	 * The index of the image to be read.
	 */
	private int imageIndex;

	/**
	 * Whether thumbnails are to be read.
	 */
	private boolean readThumbnails;

	/**
	 * Whether stream metadata have been be read.
	 */
	private boolean streamMetadataRead = false;

	/**
	 * Whether image metadata have been be read.
	 */
	private boolean imageMetadataRead = false;

	/**
	 * A stream to be closed when the instance is disposed; may be null.
	 */
	private ImageInputStream streamToClose;

	/**
	 * Destination to source X scale factor.
	 */
	private int scaleX;

	/**
	 * Destination to source Y scale factor.
	 */
	private int scaleY;

	/**
	 * Destination to source X translation factor.
	 */
	private int transX;

	/**
	 * Destination to source Y translation factor.
	 */
	private int transY;

	private boolean readerProvided = false;

	/**
	 * Derive the image layout based on the user-supplied layout, reading
	 * parameters, and image index.
	 */
	private static ImageLayout layoutHelper(ImageLayout il,
			ImageReadParam param, ImageReader reader, int imageIndex)
			throws IOException {
		ImageLayout layout = (il == null) ? new ImageLayout()
				: (ImageLayout) il.clone();

		// --- Determine the image type. ---

		// If not provided in the original layout, set the SampleModel
		// and ColorModel from the ImageReadParam, if supplied.
		if (!layout.isValid(ImageLayout.SAMPLE_MODEL_MASK)
				&& !layout.isValid(ImageLayout.COLOR_MODEL_MASK)) {
			// If an ImageReadParam has been supplied and has its
			// destinationType set then use it. Otherwise default to
			// the raw image type.
			ImageTypeSpecifier imageType = (param != null && param
					.getDestinationType() != null) ? param.getDestinationType()
					: reader.getRawImageType(imageIndex);

			// XXX The following block of code should not be necessary
			// but for J2SE 1.4.0 FCS ImageReader.getRawImageType(0)
			// returns null for earth.jpg, Bas-noir.jpg, etc.
			if (imageType == null) {
				Iterator imageTypes = reader.getImageTypes(imageIndex);
				while (imageType == null && imageTypes.hasNext()) {
					imageType = (ImageTypeSpecifier) imageTypes.next();
				}
			}

			// XXX Should an exception be thrown if imageType is null?
			if (imageType != null) {
				// Set the SampleModel and ColorModel.
				layout.setSampleModel(imageType.getSampleModel());
				layout.setColorModel(imageType.getColorModel());
			}
		}

		// --- Set up the destination bounds. ---

		// Calculate the computable destination bounds.
		Dimension sourceSize = getSourceSize(param, reader, imageIndex);
		Rectangle srcRegion = new Rectangle();
		Rectangle destRegion = new Rectangle();
		computeRegions(param, sourceSize.width, sourceSize.height, layout
				.getMinX(null), // valid value or 0
				layout.getMinY(null), // valid value or 0
				false, srcRegion, destRegion);

		if (!destRegion.isEmpty()) {
			// Backup layout image bounds with computable bounds.
			if (!layout.isValid(ImageLayout.WIDTH_MASK)) {
				layout.setWidth(destRegion.width);
			}
			if (!layout.isValid(ImageLayout.HEIGHT_MASK)) {
				layout.setHeight(destRegion.height);
			}
			if (!layout.isValid(ImageLayout.MIN_X_MASK)) {
				layout.setMinX(destRegion.x);
			}
			if (!layout.isValid(ImageLayout.MIN_Y_MASK)) {
				layout.setMinY(destRegion.y);
			}

			// Ensure the layout bounds intersect computable bounds.
			Rectangle destBounds = new Rectangle(layout.getMinX(null), layout
					.getMinY(null), layout.getWidth(null), layout
					.getHeight(null));
			if (destRegion.intersection(destBounds).isEmpty()) {
				throw new IllegalArgumentException(I18N
						.getString("ImageReadOpImage0"));
			}
		}

		// --- Set up the tile grid. ---

		if (!layout.isValid(ImageLayout.TILE_GRID_X_OFFSET_MASK)) {
			layout.setTileGridXOffset(reader.getTileGridXOffset(imageIndex));
		}
		if (!layout.isValid(ImageLayout.TILE_GRID_Y_OFFSET_MASK)) {
			layout.setTileGridYOffset(reader.getTileGridYOffset(imageIndex));
		}
		if (!layout.isValid(ImageLayout.TILE_WIDTH_MASK)) {
			layout.setTileWidth(reader.getTileWidth(imageIndex));
		}
		if (!layout.isValid(ImageLayout.TILE_HEIGHT_MASK)) {
			layout.setTileHeight(reader.getTileHeight(imageIndex));
		}

		return layout;
	}

	/**
	 * Returns whether an <code>ImageTypeSpecifier</code> may be used to read
	 * in the image at a specified index.
	 * 
	 * XXX
	 */
	private static boolean isCompatibleType(ImageTypeSpecifier imageType,
			ImageReader reader, int imageIndex) throws IOException {
		Iterator imageTypes = reader.getImageTypes(imageIndex);

		boolean foundIt = false;
		while (imageTypes.hasNext()) {
			ImageTypeSpecifier type = (ImageTypeSpecifier) imageTypes.next();
			if (type.equals(imageType)) {
				foundIt = true;
				break;
			}
		}

		return foundIt;
	}

	/**
	 * Returns the source region to be read. If the sourceRenderSize is being
	 * used it is returned; otherwise the raw source dimensions are returned.
	 * 
	 * XXX
	 */
	private static Dimension getSourceSize(ImageReadParam param,
			ImageReader reader, int imageIndex) throws IOException {
		Dimension sourceSize = null;
		if (param != null && param.canSetSourceRenderSize()) {
			sourceSize = param.getSourceRenderSize();
		}
		if (sourceSize == null) {
			sourceSize = new Dimension(reader.getWidth(imageIndex), reader
					.getHeight(imageIndex));
		}
		return sourceSize;
	}

	/**
	 * XXX
	 */
	// Code copied from ImageReader.java
	private static Rectangle getSourceRegion(ImageReadParam param,
			int srcWidth, int srcHeight) {
		Rectangle sourceRegion = new Rectangle(0, 0, srcWidth, srcHeight);
		if (param != null) {
			Rectangle region = param.getSourceRegion();
			if (region != null) {
				sourceRegion = sourceRegion.intersection(region);
			}

			int subsampleXOffset = param.getSubsamplingXOffset();
			int subsampleYOffset = param.getSubsamplingYOffset();
			sourceRegion.x += subsampleXOffset;
			sourceRegion.y += subsampleYOffset;
			sourceRegion.width -= subsampleXOffset;
			sourceRegion.height -= subsampleYOffset;
		}

		return sourceRegion;
	}

	/**
	 * XXX
	 */
	// clipDestRegion: whether to clip destRegion to positive coordinates.
	// Code based on method of same name in ImageReader.java
	private static void computeRegions(ImageReadParam param, int srcWidth,
			int srcHeight, int destMinX, int destMinY, boolean clipDestRegion,
			Rectangle srcRegion, Rectangle destRegion) {
		if (srcRegion == null) {
			throw new IllegalArgumentException("srcRegion == null");
		}
		if (destRegion == null) {
			throw new IllegalArgumentException("destRegion == null");
		}

		// Start with the entire source image
		srcRegion.setBounds(0, 0, srcWidth, srcHeight);

		// Destination also starts with source image, as that is the
		// maximum extent if there is no subsampling
		destRegion.setBounds(destMinX, destMinY, srcWidth, srcHeight);

		// Clip that to the param region, if there is one
		int periodX = 1;
		int periodY = 1;
		int gridX = 0;
		int gridY = 0;
		if (param != null) {
			Rectangle paramSrcRegion = param.getSourceRegion();
			if (paramSrcRegion != null) {
				srcRegion.setBounds(srcRegion.intersection(paramSrcRegion));
			}
			periodX = param.getSourceXSubsampling();
			periodY = param.getSourceYSubsampling();
			gridX = param.getSubsamplingXOffset();
			gridY = param.getSubsamplingYOffset();
			srcRegion.translate(gridX, gridY);
			srcRegion.width -= gridX;
			srcRegion.height -= gridY;
			Point destinationOffset = param.getDestinationOffset();
			destRegion.translate(destinationOffset.x, destinationOffset.y);
		}

		if (clipDestRegion) {
			// Now clip any negative destination offsets, i.e. clip
			// to the top and left of the destination image
			if (destRegion.x < 0) {
				int delta = -destRegion.x * periodX;
				srcRegion.x += delta;
				srcRegion.width -= delta;
				destRegion.x = 0;
			}
			if (destRegion.y < 0) {
				int delta = -destRegion.y * periodY;
				srcRegion.y += delta;
				srcRegion.height -= delta;
				destRegion.y = 0;
			}
		}

		// Now clip the destination Region to the subsampled width and height
		int subsampledWidth = (srcRegion.width + periodX - 1) / periodX;
		int subsampledHeight = (srcRegion.height + periodY - 1) / periodY;
		destRegion.width = subsampledWidth;
		destRegion.height = subsampledHeight;

		if (srcRegion.isEmpty() || destRegion.isEmpty()) {
			throw new IllegalArgumentException(I18N
					.getString("ImageReadOpImage1"));
		}
	}

	/**
	 * XXX NB: This class may reset the following fields of the ImageReadParam
	 * destinationOffset destinationType sourceRegion
	 * 
	 * @param readerProvided
	 */
	ImageReadOpImageMT(ImageLayout layout, Map configuration,
			ImageReadParam param, ImageReader reader, int imageIndex,
			boolean readThumbnails, ImageInputStream streamToClose,
			boolean readerProvided) throws IOException {
		super(null, layoutHelper(layout, param, reader, imageIndex),
				configuration, false);

		this.readerProvided = readerProvided;
		// Revise parameter 'param' as needed.
		if (param == null) {
			// Get the ImageReadParam from the ImageReader.
			this.param = (EnhancedImageReadParam) reader.getDefaultReadParam();
		} else if (param instanceof EnhancedImageReadParam) {
			try {
				this.param = (EnhancedImageReadParam) ((EnhancedImageReadParam) param).clone();
			} catch (CloneNotSupportedException e) {
				final IOException ioe = new IOException();
				ioe.initCause(e);
				throw ioe;
			}
		} else

			throw new IllegalArgumentException();

		// Revise parameter 'readThumbnails' as needed.
		if (readThumbnails && !reader.hasThumbnails(imageIndex)) {
			// Unset thumbnail flag if not supported by ImageReader.
			readThumbnails = false;
		}

		// Set instance variables from (possibly revised) parameters.
		this.reader = reader;
		this.imageIndex = imageIndex;
		this.readThumbnails = readThumbnails;
		this.streamToClose = streamToClose;

		// If an ImageTypeSpecifier is specified in the ImageReadParam
		// but it is incompatible with the ImageReader, then attempt to
		// replace it with a compatible one derived from this image.
		if (param.getDestinationType() != null
				&& !isCompatibleType(param.getDestinationType(), reader,
						imageIndex) && sampleModel != null
				&& colorModel != null) {
			ImageTypeSpecifier newImageType = new ImageTypeSpecifier(
					colorModel, sampleModel);
			if (isCompatibleType(newImageType, reader, imageIndex)) {
				param.setDestinationType(newImageType);
			}
		}

		// --- Compute the destination to source mapping coefficients. ---

		Dimension sourceSize = getSourceSize(param, reader, imageIndex);

		Rectangle srcRegion = getSourceRegion(param, sourceSize.width,
				sourceSize.height);

		Point destinationOffset = this.param.getDestinationOffset();

		this.scaleX = this.param.getSourceXSubsampling();
		this.scaleY = this.param.getSourceYSubsampling();
		this.transX = srcRegion.x + this.param.getSubsamplingXOffset()
				- this.param.getSourceXSubsampling()
				* (minX + destinationOffset.x);
		this.transY = srcRegion.y + this.param.getSubsamplingYOffset()
				- this.param.getSourceYSubsampling()
				* (minY + destinationOffset.y);

		// Replace the original destination offset with (0,0) as the
		// destination-to-source mapping assimilates this value.
		this.param.setDestinationOffset(new Point());
		// XXX Need to unset other ImageReadParam settings either here
		// or in computeTile(). Examine this issue taking into account
		// synchronization.

		// Set the ImageReadParam property.
		setProperty(ImageReadDescriptor.PROPERTY_NAME_IMAGE_READ_PARAM, param);

		// Set the ImageReader property.
		setProperty(ImageReadDescriptor.PROPERTY_NAME_IMAGE_READER, reader);

		// If metadata are being read, set the value of the metadata
		// properties to UndefinedProperty so that the property
		// names will appear in the array of property names. The actual
		// values will be retrieved when getProperty() is invoked.
		if (!reader.isIgnoringMetadata()) {
			// Get the service provider interface, if any.
			ImageReaderSpi provider = reader.getOriginatingProvider();

			// Stream metadata.
			if (provider == null
					|| provider.isStandardStreamMetadataFormatSupported()
					|| provider.getNativeStreamMetadataFormatName() != null) {
				// Assume an ImageReader with a null provider supports
				// stream metadata.
				setProperty(ImageReadDescriptor.PROPERTY_NAME_METADATA_STREAM,
						java.awt.Image.UndefinedProperty);
			} else {
				// Provider supports neither standard nor native stream
				// metadata so set flag to suppress later reading attempt.
				streamMetadataRead = true;
			}

			// Image metadata.
			if (provider == null
					|| provider.isStandardImageMetadataFormatSupported()
					|| provider.getNativeImageMetadataFormatName() != null) {
				// Assume an ImageReader with a null provider supports
				// image metadata.
				setProperty(ImageReadDescriptor.PROPERTY_NAME_METADATA_IMAGE,
						java.awt.Image.UndefinedProperty);
			} else {
				// Provider supports neither standard nor native image
				// metadata so set flag to suppress later reading attempt.
				imageMetadataRead = true;
			}
		}

		// If thumbnail read flag is set, set the value of the thumbnail
		// property to UndefinedProperty so that the thumbnail property
		// name will appear in the array of property names. The actual
		// value will be retrieved when getProperty() is invoked.
		if (readThumbnails && reader.readerSupportsThumbnails()) {
			setProperty(ImageReadDescriptor.PROPERTY_NAME_THUMBNAILS,
					java.awt.Image.UndefinedProperty);
		}
	}

	/**
	 * Returns false as ImageReaders might return Rasters via computeTile() tile
	 * that are internally cached.
	 */
	public boolean computesUniqueTiles() {
		return false;
	}

	/**
	 * XXX
	 */
	private Rectangle computeSourceRect(Rectangle destRect) {
		Rectangle sourceRect = new Rectangle();

		sourceRect.x = scaleX * destRect.x + transX;
		sourceRect.y = scaleY * destRect.y + transY;

		sourceRect.width = scaleX * (destRect.x + destRect.width) + transX
				- sourceRect.x;
		sourceRect.height = scaleY * (destRect.y + destRect.height) + transY
				- sourceRect.y;

		return sourceRect;
	}

	/**
	 * Computes a tile.
	 * 
	 * @param tileX
	 *            The X index of the tile.
	 * @param tileY
	 *            The Y index of the tile.
	 */
	public Raster computeTile(int tileX, int tileY) {
		// XXX System.out.println("Tile ("+tileX+","+tileY+")");
		// Create a new WritableRaster to represent this tile.
		Point org = new Point(tileXToX(tileX), tileYToY(tileY));
		// WritableRaster dest = Raster.createWritableRaster(sampleModel, org);
		Rectangle rect = new Rectangle(org.x, org.y, tileWidth, tileHeight);

		// Clip output rectangle to image bounds.
		// Not sure what will happen here with the bounds intersection.
		Rectangle destRect = rect.intersection(getBounds());
		// XXX Check for destRect.isEmpty()?

		/*
		 * XXX delete java.awt.geom.AffineTransform transform = new
		 * java.awt.geom.AffineTransform(scaleX, 0, 0, scaleY, transX, transY);
		 */
		Rectangle srcRect = computeSourceRect(destRect);
		WritableRaster readerTile = null;
		try {

			final ImageReadParam param = (ImageReadParam) this.param.clone();
			param.setSourceRegion(srcRect);
			param.setDestinationOffset(org);
			BufferedImage bi = reader.read(imageIndex, param);
			WritableRaster ras = bi.getRaster();
			readerTile = ras.createWritableChild(0, 0, ras.getWidth(), ras
					.getHeight(), org.x, org.y, null);

		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}

		WritableRaster tile = null;
		if (sampleModel == readerTile.getSampleModel()) {
			tile = readerTile;
		} else {
			tile = Raster.createWritableRaster(sampleModel, org);
			tile.setRect(readerTile);
		}
		return tile;
	}

	/**
	 * Throws an IllegalArgumentException since the image has no image sources.
	 * 
	 * @param sourceRect
	 *            ignored.
	 * @param sourceIndex
	 *            ignored.
	 * 
	 * @throws IllegalArgumentException
	 *             since the image has no sources.
	 */
	public Rectangle mapSourceRect(Rectangle sourceRect, int sourceIndex) {
		throw new IllegalArgumentException(I18N.getString("ImageReadOpImage2"));
	}

	/**
	 * Throws an IllegalArgumentException since the image has no image sources.
	 * 
	 * @param destRect
	 *            ignored.
	 * @param sourceIndex
	 *            ignored.
	 * 
	 * @throws IllegalArgumentException
	 *             since the image has no sources.
	 */
	public Rectangle mapDestRect(Rectangle destRect, int sourceIndex) {
		throw new IllegalArgumentException(I18N.getString("ImageReadOpImage2"));
	}

	/**
	 * Gets a property from the property set of this image. If the property name
	 * is not recognized, <code>java.awt.Image.UndefinedProperty</code> will
	 * be returned.
	 * 
	 * <p>
	 * This implementation first attempts to retrieve the property using the
	 * equivalent superclass method. If the returned value is not a valid
	 * property value, the requested property name is that of the image
	 * thumbnails property, the stream metadata property, or the image metadata
	 * property, and there has been no prior attempt to read the corresponding
	 * property value, then its reads the value and set the property. This
	 * implementation therefore defers reading of the image thumbnails, stream
	 * metadata, and image metadata values until the correpsonding property is
	 * actually requested.
	 * </p>
	 * 
	 * @param name
	 *            the name of the property to get, as a <code>String</code>.
	 * 
	 * @return A reference to the property <code>Object</code>, or the value
	 *         <code>java.awt.Image.UndefinedProperty</code>.
	 * 
	 * @exception IllegalArgumentException
	 *                if <code>propertyName</code> is <code>null</code>.
	 */
	public Object getProperty(String name) {
		// Attempt to get property from superclass method.
		Object property = super.getProperty(name);

		// If thumbnail property name with undefined value and thumbnails
		// are being read and an attempt to read them has not already been
		// made, then read the thumbnails and set the property.
		if ((property == null || property == java.awt.Image.UndefinedProperty)) {

			// Thumbnails
			if (readThumbnails
					&& name
							.equalsIgnoreCase(ImageReadDescriptor.PROPERTY_NAME_THUMBNAILS)) {

				// Lock the class to avoid a race condition here
				// and with computeTile().
				synchronized (reader) {
					// First re-check the flag in case another thread
					// got here first.
					if (readThumbnails) {
						try {
							// Get number of thumbnails.
							int numThumbnails = reader
									.getNumThumbnails(imageIndex);

							if (numThumbnails > 0) {
								// Read all thumbnails.
								BufferedImage[] thumbnails = new BufferedImage[numThumbnails];
								for (int i = 0; i < numThumbnails; i++) {
									thumbnails[i] = reader.readThumbnail(
											imageIndex, i);
								}

								// Set thumbnail property.
								setProperty(
										ImageReadDescriptor.PROPERTY_NAME_THUMBNAILS,
										thumbnails);

								// Update return value.
								property = thumbnails;
							}
						} catch (IOException e) {
							throw new RuntimeException(e);
						} finally {
							// If return value is somehow null set it
							// to UndefinedProperty.
							if (property == null) {
								property = java.awt.Image.UndefinedProperty;
							}

							// Unset thumbnail flag to avert subsequent
							// reading attempts in case this one failed.
							readThumbnails = false;
						}
					}
				}
			} else if (!reader.isIgnoringMetadata()
					&& ((!streamMetadataRead && name
							.equalsIgnoreCase(ImageReadDescriptor.PROPERTY_NAME_METADATA_STREAM)) || (!imageMetadataRead && name
							.equalsIgnoreCase(ImageReadDescriptor.PROPERTY_NAME_METADATA_IMAGE)))) {

				// Lock the class to avoid a race condition here
				// and with computeTile().
				synchronized (reader) {

					// Set flag to indicate stream or image metadata.
					boolean isStreamMetadata = name
							.equalsIgnoreCase(ImageReadDescriptor.PROPERTY_NAME_METADATA_STREAM);

					// Recheck the appropriate flag.
					if (!(isStreamMetadata ? streamMetadataRead
							: imageMetadataRead)) {

						// Set property name.
						String propertyName = isStreamMetadata ? ImageReadDescriptor.PROPERTY_NAME_METADATA_STREAM
								: ImageReadDescriptor.PROPERTY_NAME_METADATA_IMAGE;

						IIOMetadata metadata = null;
						try {
							// Read metadata.
							metadata = isStreamMetadata ? reader
									.getStreamMetadata() : reader
									.getImageMetadata(imageIndex);

							// Set metadata property.
							if (metadata != null) {
								setProperty(propertyName, metadata);
							}

							// Update return value.
							property = metadata;
						} catch (IOException e) {
							throw new RuntimeException(e);
						} finally {
							// If return value is somehow null set it
							// to UndefinedProperty.
							if (property == null) {
								property = java.awt.Image.UndefinedProperty;
							}

							// Set appropriate flag to avert subsequent
							// reading attempts in case this one failed.
							if (isStreamMetadata) {
								streamMetadataRead = true;
							} else {
								imageMetadataRead = true;
							}
						}
					}
				}
			}
		}

		return property;
	}

	/**
	 * Closes an <code>ImageInputStream</code> passed in, if any. Same thing
	 * for a reader.
	 */
	public void dispose() {
		if (streamToClose != null) {
			try {
				streamToClose.close();
			} catch (IOException e) {
				// Ignore it.
			}
		}
		if (!readerProvided) {

			reader.dispose();

		}

		super.dispose();
	}
}
