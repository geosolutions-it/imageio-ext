package it.geosolutions.imageio.plugins.jhdf.aps;

import it.geosolutions.imageio.plugins.jhdf.BaseHDFImageReader;
import it.geosolutions.imageio.plugins.jhdf.SubDatasetInfo;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.ScalarDS;

/**
 * Specific Implementation of the <code>BaseHDFImageReader</code> needed to
 * work on HDF produced by the Navy's APS (Automated Processing System)
 * 
 * @author Romagnoli Daniele
 */
public class APSImageReader extends BaseHDFImageReader {

	public APSImageReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
	}

	/** The Products Dataset List contained within the APS File */
	private String[] productList;

	private APSImageMetadata imageMetadata;

	private APSStreamMetadata streamMetadata;

	private void checkImageIndex(int imageIndex) {
		// TODO: Implements the imageIndex coherency check

		// if (imageIndex < 0
		// || (!hasSubDatasets && imageIndex > 0)
		// || (hasSubDatasets && ((nSubdatasets == 0 && imageIndex > 0) ||
		// (nSubdatasets != 0 && (imageIndex > nSubdatasets))))) {
		//
		// // The specified imageIndex is not valid.
		// // Retrieving the valid image index range.
		// final int validImageIndex = hasSubDatasets ? nSubdatasets
		// : 0;
		// StringBuffer sb = new StringBuffer(
		// "Illegal imageIndex specified = ").append(imageIndex)
		// .append(", while the valid imageIndex");
		// if (validImageIndex > 0)
		// // There are N Subdatasets.
		// sb.append(" range should be (0,").append(validImageIndex - 1)
		// .append(")!!");
		// else
		// // Only the imageIndex 0 is valid.
		// sb.append(" should be only 0!");
		// throw new IndexOutOfBoundsException(sb.toString());
		// }
	}

	/**
	 * Retrieve APS main information.
	 * 
	 * @param root
	 * 
	 * @throws Exception
	 */
	protected void initializeProfile() throws Exception {
		// Getting the Member List from the provided root
		final List membersList = ((Group) root).getMemberList();
		final Iterator metadataIt = root.getMetadata().iterator();

		int subdatasetsNum = 0;
		while (metadataIt.hasNext()) {
			// get the attribute
			final Attribute attrib = (Attribute) metadataIt.next();
			final String attribName = attrib.getName();
			// Checking if the attribute is related to the products list
			if (attribName.equalsIgnoreCase("prodList")) {
				Object valuesList = attrib.getValue();
				final String[] values = (String[]) valuesList;
				String products[] = values[0].split(",");
				productList = refineProductList(products);
				subdatasetsNum = productList.length;
				break;
			}
		}
		final int listSize = membersList.size();

		subDatasetsMap = new LinkedHashMap(subdatasetsNum);
		sourceStructure = new SourceStructure(subdatasetsNum);

		// Scanning all the datasets
		for (int i = 0; i < listSize; i++) {
			final HObject member = (HObject) membersList.get(i);
			if (member instanceof ScalarDS) {
				final String name = member.getName();
				for (int j = 0; j < subdatasetsNum; j++) {

					// Checking if the actual dataset is a product.
					if (name.equals(productList[j])) {
						// Updating the subDatasetsMap map
						subDatasetsMap.put(name, member);

						// retrieving subDataset main properties
						// (Rank, dims, chunkSize)
						final int rank = ((Dataset) member).getRank();
						final long[] dims = ((Dataset) member).getDims();

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

						final Datatype dt = ((Dataset) member).getDatatype();
						// instantiating a SubDatasetInfo
						SubDatasetInfo dsInfo = new SubDatasetInfo(name, rank,
								subDatasetDims, subDatasetChunkSize, dt);
						sourceStructure.setSubDatasetSize(j, datasetSize);
						sourceStructure.setSubDatasetInfo(j, dsInfo);
					}
				}
			}
		}
	}

	/**
	 * Reduces the product's list by removing not interesting ones. As an
	 * instance the dataset containing l2_flags will be not presented.
	 * 
	 * @param products
	 *            The originating <code>String</code> array containing the
	 *            list of products to be checked.
	 * @return A <code>String</code> array containing a refined list of
	 *         products
	 */
	private String[] refineProductList(String[] products) {
		final int inputProducts = products.length;
		int j = 0;
		final boolean[] accepted = new boolean[inputProducts];

		for (int i = 0; i < inputProducts; i++)
			if (isAcceptedItem(products[i])) {
				accepted[i] = true;
				j++;
			} else
				accepted[i] = false;
		if (j == inputProducts)
			return products;
		final String[] returnedProductsList = new String[j];
		j = 0;
		for (int i = 0; i < inputProducts; i++) {
			if (accepted[i])
				returnedProductsList[j++] = products[i];
		}
		return returnedProductsList;
	}

	protected boolean isAcceptedItem(String productName) {
		// if (attribName.endsWith("_flags"))
		// return false;
		if (APSProperties.apsProducts.getHDFProduct(productName) != null)
			return true;
		return false;
	}

	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
		checkImageIndex(imageIndex);
		SubDatasetInfo sdInfo = sourceStructure
				.getSubDatasetInfo(retrieveSubDatasetIndex(imageIndex));
		if (imageMetadata == null)
			imageMetadata = new APSImageMetadata(sdInfo);
		return imageMetadata;
	}

	public int getNumImages(boolean allowSearch) throws IOException {
		return sourceStructure.getNSubdatasets();
	}

	public IIOMetadata getStreamMetadata() throws IOException {
		if (streamMetadata == null)
			streamMetadata = new APSStreamMetadata(root);
		return streamMetadata;
	}

	public void dispose() {
		super.dispose();
		synchronized (mutex) {
			final Set set = subDatasetsMap.keySet();
			final Iterator setIt = set.iterator();

			// Cleaning HashMap
			while (setIt.hasNext()) {
				Dataset ds = (Dataset) subDatasetsMap.get(setIt.next());
				// TODO:Restore original properties?
				// TODO: Close datasets
			}
			subDatasetsMap.clear();
		}
	}

	public void reset() {
		super.reset();
		streamMetadata = null;
		imageMetadata = null;
		productList = null;
	}

	protected int getBandNumberFromProduct(String productName) {
		return APSProperties.apsProducts.getHDFProduct(productName).getNBands();
	}

}
