package it.geosolutions.imageio.plugins.jhdf.tovs;

import it.geosolutions.imageio.plugins.jhdf.BaseHDFImageReader;
import it.geosolutions.imageio.plugins.jhdf.SubDatasetInfo;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.ScalarDS;

public class TOVSImageReader extends BaseHDFImageReader {
	private TOVSImageMetadata imageMetadata;

	public TOVSImageReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
	}

	protected int getBandNumberFromProduct(String productName) {
		return TOVSPathAProperties.tovsProducts.getHDFProduct(productName)
				.getNBands();
	}

	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
		SubDatasetInfo sdInfo = sourceStructure
				.getSubDatasetInfo(retrieveSubDatasetIndex(imageIndex));
		if (imageMetadata == null)
			imageMetadata = new TOVSImageMetadata(sdInfo);
		return imageMetadata;
	}

	public IIOMetadata getStreamMetadata() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNumImages(boolean allowSearch) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	protected boolean isAcceptedItem(String productName) {
		if (TOVSPathAProperties.tovsProducts.getHDFProduct(productName) != null)
			return true;
		return false;
	}

	/**
	 * Initializing TOVS datasets properties.
	 * 
	 * @param root
	 * 
	 * @throws Exception
	 */
	protected void initializeProfile() throws Exception {
		// Getting the Member List from the provided root
		final List membersList = ((Group) root).getMemberList();

		final int listSize = membersList.size();
		int subDatasets = 0;

		// Mutex on the subDatasetMap and sourceStructure initialization
		subDatasetsMap = new LinkedHashMap(8);
		sourceStructure = new SourceStructure();

		// Scanning all the datasets
		for (int i = 0; i < listSize; i++) {
			final HObject member = (HObject) membersList.get(i);
			if (member instanceof ScalarDS) {
				if (member.hasAttribute()) {
					List metadataList = member.getMetadata();
					Iterator metadataIt = metadataList.iterator();
					while (metadataIt.hasNext()) {
						final Attribute attrib = (Attribute) metadataIt.next();
						final String attribName = attrib.getName();
						if (attribName.equals("long_name")) {
							Object valuesList = attrib.getValue();
							final String[] values = (String[]) valuesList;
							final String name = values[0];

							// Checking if the actual dataset is accepted.
							if (isAcceptedItem(name)) {
								subDatasets++;
								// Updating the subDatasetsMap map
								subDatasetsMap.put(name, member);

								// retrieving subDataset main properties
								// (Rank, dims, chunkSize)
								final int rank = ((Dataset) member).getRank();
								final long[] dims = ((Dataset) member)
										.getDims();
								final long[] chunkSize = ((Dataset) member)
										.getChunkSize();

								final long[] subDatasetDims = new long[rank];
								final long[] subDatasetChunkSize;
								long datasetSize = 1;

								// copying values to avoid altering dataset
								// fields.
								for (int k = 0; k < rank; k++) {
									subDatasetDims[k] = dims[k];

									// when rank > 2, X and Y are the last
									// 2 coordinates. As an instance, for a
									// 3D subdatasets, 3rd dimension has
									// index 0.
									if (k < rank - 2)
										datasetSize *= dims[k];
								}
								if (chunkSize != null) {
									subDatasetChunkSize = new long[rank];
									for (int k = 0; k < rank; k++)
										subDatasetChunkSize[k] = chunkSize[k];
								} else
									subDatasetChunkSize = null;
								final Datatype dt = ((Dataset) member)
										.getDatatype();
								// instantiating a SubDatasetInfo
								SubDatasetInfo dsInfo = new SubDatasetInfo(
										name, rank, subDatasetDims,
										subDatasetChunkSize, dt);
								sourceStructure.addSubDatasetProperties(dsInfo,
										datasetSize);
							}
						}
					}
				}
			}
		}
	}
}
