package it.geosolutions.imageio.plugins.grib1;

import it.geosolutions.imageio.plugins.BasicStreamMetadata;
import it.geosolutions.imageio.plugins.Utilities;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import javax.imageio.metadata.IIOMetadataNode;

import net.sourceforge.jgrib.GribCollection;
import net.sourceforge.jgrib.GribFile;

public class GRIB1BasicStreamMetadata extends BasicStreamMetadata {

	private Object gribSource;

	private GRIB1BasicStreamMetadata(final int numImages) {
		super(GRIB1ImageReaderSpi.formatNames[0], "GRIB1 raster format",
				numImages);
		this.numImages = numImages;
	}

	public GRIB1BasicStreamMetadata(List imagesList) {
		this(imagesList.size());
		gribSource = imagesList;
	}

	public GRIB1BasicStreamMetadata(GribFile gribFile, final int numImages) {
		this(numImages);
		gribSource = gribFile;
	}

	public GRIB1BasicStreamMetadata(GribCollection gribCollection, final int numImages) {
		this(numImages);
		gribSource = gribCollection;
	}

	protected IIOMetadataNode buildImagesNode(IIOMetadataNode imagesNode) {
		URI uri = null;
		if (gribSource instanceof GribFile) {
			GribFile gribFile = (GribFile) gribSource;
			Object source = gribFile.getOriginatingSource();

			uri = Utilities.getURIFromSource(source);
			imagesNode = appendImagesNode(uri.toString(), (int) numImages,
					imagesNode);

		} else if (gribSource instanceof GribCollection) {
			Iterator gribFilesIt = ((GribCollection) gribSource).getGribIterator();
			int images = 0;
			while (gribFilesIt.hasNext()) {
				GribFile gribFile = (GribFile) gribFilesIt.next();
				final Object source = gribFile.getOriginatingSource();
				images = gribFile.getRecordCount();
				uri = Utilities.getURIFromSource(source);

				imagesNode = appendImagesNode(uri.toString(), images,
						imagesNode);
			}
//		} else if (gribSource instanceof List) {
//			List list = (List) gribSource;
//			Iterator listIterator = list.iterator();
//			while (listIterator.hasNext()) {
//				IndexToRecordMapper mapper = (IndexToRecordMapper) listIterator
//						.next();
//
//				IIOMetadataNode imageNode = new IIOMetadataNode("Image");
//
//				IIOMetadataNode imageIndexNode = new IIOMetadataNode("imageIndex");
//				imageIndexNode.setNodeValue(Integer.toString(mapper.getImageIndex()));
//				imageNode.appendChild(imageIndexNode);
//
//				IIOMetadataNode uriNode = new IIOMetadataNode("srcURI");
//
//				// TODO: Build Uri from gribFile
//				uriNode.setNodeValue(mapper.getUriString());
//				imageNode.appendChild(uriNode);
//
//				imagesNode.appendChild(imageNode);
//			}

		}
		return imagesNode;
	}

	private IIOMetadataNode appendImagesNode(String uriString, int nImages,
			IIOMetadataNode imagesNode) {
		for (int i = 0; i < nImages; i++) {

			IIOMetadataNode imageNode = new IIOMetadataNode("Image");

			IIOMetadataNode imageIndexNode = new IIOMetadataNode("imageIndex");
			imageIndexNode.setNodeValue(Integer.toString(i));
			imageNode.appendChild(imageIndexNode);

			IIOMetadataNode uriNode = new IIOMetadataNode("srcURI");

			// TODO: Build Uri from gribFile
			uriNode.setNodeValue(uriString);
			imageNode.appendChild(uriNode);

			imagesNode.appendChild(imageNode);
		}
		return imagesNode;
	}
}
