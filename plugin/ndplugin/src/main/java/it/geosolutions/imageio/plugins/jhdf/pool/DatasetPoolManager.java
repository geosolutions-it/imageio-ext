package it.geosolutions.imageio.plugins.jhdf.pool;

import it.geosolutions.imageio.plugins.jhdf.pool.DatasetPool.DatasetCopy;

import java.io.File;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * This class provides to manage different dataset pools (A dataset pool for
 * each (sub)dataset contained within the originating source. An ImageReader
 * should ask t
 * 
 * 
 * @author Romagnoli Daniele
 * @author Giannecchini Simone
 * 
 */

public class DatasetPoolManager {

	private static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.plugins.jhdf.pool");

	/** Hashmap containing couples (key,DatasetPool). */
	// protected Map pooledDatasetsMap = Collections.synchronizedMap(new
	// HashMap(
	// 10));
	/** Map to associate keys to index */
	// protected TreeMap indexToKeyMap = new TreeMap();
	protected TreeMap indexToPoolMap = new TreeMap();

	/**
	 * The number of pooled Datasets. If a HDF file contains 4 subdatasets, this *
	 * variable may assume as value, atmost 4. The number of copies of the same
	 * dataset within a pool is not related with this value.
	 */
	private int differentPooledDatasets;

	/** The number of subdatasets contained within the source file */
	private int subdatasetNumber;

	private File originatingFile;

	// public DatasetCopy getDatasetCopy(final int datasetIndex) {
	// return getDatasetCopy(getKeyFromIndex(datasetIndex));
	// }
	//
	// // TODO: I need to set a proper type for the search key within the
	// hashmap.
	// // Maybe "String" is not the proper one.
	// private DatasetCopy getDatasetCopy(final String key) {
	// DatasetCopy dsc = null;
	// synchronized (pooledDatasetsMap) {
	// if (pooledDatasetsMap == null
	// || key==null || !pooledDatasetsMap.containsKey(key)){
	// // populate the pool
	// ;
	//				
	// }
	//				
	//
	// if (pooledDatasetsMap.containsKey(key)) {
	// DatasetPool pool = (DatasetPool) pooledDatasetsMap.get(key);
	// synchronized (pool) {
	// if (pool.hasAvailableDatasets()) {
	// dsc = pool.getDatasetCopy();
	// }
	// }
	// }
	// }
	// return dsc;
	// }
	//
	// public void getBackDatasetCopy(final int datasetIndex,
	// final int copyID, final boolean hasBeenModified) {
	// getBackDatasetCopy(getKeyFromIndex(datasetIndex), copyID,
	// hasBeenModified);
	// }
	//
	// private void getBackDatasetCopy(final String key, final int copyID,
	// final boolean hasBeenModified) {
	// synchronized (pooledDatasetsMap) {
	// if (pooledDatasetsMap.containsKey(key)) {
	// DatasetPool pool = (DatasetPool) pooledDatasetsMap.get(key);
	// synchronized (pool) {
	// pool.getBackDatasetCopy(copyID, hasBeenModified);
	// }
	// }
	// }
	// }
	//
	// /**
	// * A simple method providing to retrieve the key which is related to a
	// * dataset from dataset index.
	// *
	// * @param datasetIndex
	// * @return
	// */
	// protected String getKeyFromIndex(final int datasetIndex) {
	// String key = null;
	// synchronized (indexToKeyMap) {
	// if (indexToKeyMap != null && !indexToKeyMap.isEmpty()
	// && indexToKeyMap.containsKey(datasetIndex))
	// key = (String) indexToKeyMap.get(datasetIndex);
	// }
	// return key;
	// }

	public DatasetCopy getDatasetCopy(int imageIndex) {
		DatasetCopy dsc = null;
		System.out.print("Entering synchronized map-->");
		synchronized (indexToPoolMap) {
			// TODO: REMOVE this MESSAGE
			System.out.print("ENTERED... ->");
			System.out.print("MANAGER: getting DatasetCopy ");
			if (indexToPoolMap == null
					|| !indexToPoolMap.containsKey(Integer.valueOf(imageIndex))) {
				// populate the pool
				indexToPoolMap.put(Integer.valueOf(imageIndex),
						new DatasetPool(imageIndex, originatingFile));
				differentPooledDatasets++;
			}
			if (indexToPoolMap.containsKey(Integer.valueOf(imageIndex))) {
				DatasetPool pool = (DatasetPool) indexToPoolMap.get(Integer
						.valueOf(imageIndex));
//				synchronized (pool) {
					dsc = pool.getDatasetCopy();
//				}
			}
		}
		// TODO: REMOVE this MESSAGE
		System.out.print(dsc.getCopyID() + "\n");
		return dsc;
	}

	public void getBackDatasetCopy(final int key, final int copyID,
			final boolean hasBeenModified) {
		synchronized (indexToPoolMap) {
			// TODO: REMOVE this MESSAGE
			System.out.print("MANAGER: getting back DatasetCopy "
					+ String.valueOf(copyID) + "\n");
			if (indexToPoolMap.containsKey(Integer.valueOf(key))) {
				DatasetPool pool = (DatasetPool) indexToPoolMap.get(Integer
						.valueOf(key));
//				synchronized (pool) {
					pool.getBackDatasetCopy(copyID, hasBeenModified);
//				}
			}
		}
	}

	public File getOriginatingFile() {
		return originatingFile;
	}

	public void setOriginatingFile(File originatingFile) {
		this.originatingFile = originatingFile;
	}
}
