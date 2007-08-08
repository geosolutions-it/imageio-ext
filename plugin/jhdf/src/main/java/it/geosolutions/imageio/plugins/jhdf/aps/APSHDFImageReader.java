package it.geosolutions.imageio.plugins.jhdf.aps;

import it.geosolutions.imageio.plugins.jhdf.BaseHDFImageReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.ScalarDS;

/**
 * Specific Implementation of the <code>BaseHDFImageReader</code> needed to
 * work on HDF produced by the Navy's APS (Automated Processing System)
 * 
 * @author Romagnoli Daniele
 */
public class APSHDFImageReader extends BaseHDFImageReader {

	protected APSHDFImageReader(ImageReaderSpi originatingProvider) {
		super(originatingProvider);
	}

	/** The Dataset List contained within this APS File */

	private String[] productList;

	private HashMap subDatasets;

	private HObject mapProjectionHObject;

	private APSHDFImageMetadata imageMetadata;
	
	
	/**
	 * Retrieve APS main information
	 * 
	 * @param root
	 * @throws Exception
	 */
	private void initializeAPS(HObject root) throws Exception {

		// Getting the Member List from the provided root
		final List membersList = ((Group) root).getMemberList();
		final Iterator metadataIt = root.getMetadata().iterator();
		int initialized = 0;
		String mapProjectionName = "";
		while (metadataIt.hasNext()) {
			// get the attribute
			final Attribute att = (Attribute) metadataIt.next();

			// Checking if the attribute is related to the products list
			if (att.getName().equalsIgnoreCase("prodList")) {
				Object valuesList = att.getValue();
				final String[] values = (String[]) valuesList;
				final String products[] = values[0].split(",");
				productList = products;
				nSubdatasets = products.length;
				subDatasets = new HashMap(nSubdatasets);
				initialized++;
			}

			// Checking if the attribute is related to the Map Projection
			if (att.getName().equalsIgnoreCase("mapProjection")) {
				Object value = att.getValue();
				final String[] values = (String[]) value;
				mapProjectionName = values[0];
				initialized++;
			}

			if (initialized == 2)
				break;
		}

		final int listSize = membersList.size();
//		structure = new SourceStructure(nSubdatasets);
		
		// Scanning all the datasets
		for (int i = 0; i < listSize; i++) {
			final HObject member = (HObject) membersList.get(i);
			if (member instanceof ScalarDS) {
				final String name = member.getName();
				if (name.equals(mapProjectionName)) {
					mapProjectionHObject = member;
					continue;

				}
				for (int j = 0; j < nSubdatasets; j++) {
					if (name.equals(productList[j])) {
//						DatasetInfo dsInfo= new DatasetInfo(name);
//						final int dims=((Dataset)member).getRank();
//						final long dimSizes[] = ((Dataset)member).getDims();
						subDatasets.put(name, member);
						List metadata = member.getMetadata();
						int k=0;
						k++;
						
						//TODO: Need to set Bands!
//						dsInfo.setDims(dims);
//						dsInfo.setDimSizes(dimSizes);
//						structure.setDatasetInfo(j, dsInfo);
						
					}
				}
			}
		}

		if (nSubdatasets > 1)
			hasSubDatasets = true;

	}

	protected void initialize() throws IOException {
		super.initialize();

		try {
			initializeAPS(root);
		} catch (Exception e) {
			IOException ioe = new IOException(
					"Unable to Initialize data. Provided Input is not valid"
							+ e);
			ioe.initCause(e);
			throw ioe;
		}
	}
	
	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
		if (imageMetadata == null)
			imageMetadata = new APSHDFImageMetadata(retrieveDataset(imageIndex));
		return imageMetadata;
	}

	public int getNumImages(boolean allowSearch) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public IIOMetadata getStreamMetadata() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
