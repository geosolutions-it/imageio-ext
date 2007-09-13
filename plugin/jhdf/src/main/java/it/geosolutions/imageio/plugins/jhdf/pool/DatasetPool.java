package it.geosolutions.imageio.plugins.jhdf.pool;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;

/**
 * This class represents a pool of Dataset. Since using subsampling / subregion
 * mechanism will modify the original dataset, we need to work on a different
 * dataset everytime <code>ImageReader.read()</code> method is called.
 * Furthermore, if read operations are multithreaded (each thread will try to
 * load a tile) such a problem is more perceptible.
 * 
 * @author Romagnoli Daniele
 * @author Giannecchini Simone
 */

/**
 * WORK IN PROGRESS - NOT YET COMPLETED
 */

public class DatasetPool {

	private static final Logger LOGGER = Logger
			.getLogger("it.geosolutions.imageio.plugins.jhdf.pool");

	private static final int DEFAULT_DATASETSCOPIES_LIMIT = 10;

	private static final int DEFAULT_DATASETSCOPIES_START = 4;

	private final File sourceFile;

	/**
	 * <code>DatasetPool constructor</code>
	 * 
	 * @param imageIndex
	 *            The index of the dataset in the originating file.
	 * @param originatingFile
	 *            The originating <code>File</code>
	 * @param datasetCopies
	 *            The number of pooled copies of this dataset
	 * @param datasetLimit
	 *            The maximum number of copies allowed for this pool
	 */
	public DatasetPool(int imageIndex, final File originatingFile,
			final int datasetCopies, final int datasetLimit) {

		datasetIndex = imageIndex;
		this.datasetLimit = datasetLimit;
		sourceFile = originatingFile;

		// retrieving the name of the originating file.
		final String fileName = originatingFile.getAbsolutePath();

		// synchronized (storedCopies) {
		for (int i = 0; i < datasetCopies; i++) {
			try {

				// retrieving a FileFormat
				FileFormat ffo = FileFormat.getInstance(fileName);
				ffo = ffo.open(fileName, FileFormat.WRITE);
				Group root = (Group) ffo.get("/");

				// setting the copy ID.
				final int uniqueID = IDgenerator++;
				final List membersList = root.getMemberList();
				final int members = membersList.size();
				final int selectedIndex = members > 1 ? imageIndex + 1
						: imageIndex;

				// getting the required dataset.
				final Dataset dataset = (Dataset) membersList
						.get(selectedIndex);

				// Building a new copy of this dataset.
				final DatasetCopy dsc = new DatasetCopy(uniqueID, dataset);
				nCopies++;
				availableDatasets++;
				storedCopies.put(Integer.valueOf(uniqueID), dsc);

				// initializing the original properties of the pooled dataset.
				if (i == 0)
					originalProperties = new DatasetProperties(dsc.getDataset());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// }
	}

	public DatasetPool(int imageIndex, final File originatingFile,
			final int datasetCopies) {
		this(imageIndex, originatingFile, datasetCopies,
				DEFAULT_DATASETSCOPIES_LIMIT);
	}

	public DatasetPool(int imageIndex, File originatingFile) {
		this(imageIndex, originatingFile, DEFAULT_DATASETSCOPIES_START,
				DEFAULT_DATASETSCOPIES_LIMIT);

	}

	/** dataset not yet "on use " */
	private int availableDatasets;

	/** total number of dataset within the pool */
	private int nCopies;

	/** the max number of creable dataset */
	private int datasetLimit;

	/** the index number of this (sub)dataset within the file */
	private int datasetIndex;

	/** the list containing the real pool of datasets */
	private TreeMap storedCopies = new TreeMap();

	/**
	 * the original properties of the dataset. Since operations like subregion
	 * and subsampling will modify the dataset's properties (such as its rank,
	 * its dimension,...), I need to store somewhere these properties prior the
	 * dataset will be modified.
	 */
	private DatasetProperties originalProperties;

	private static int IDgenerator = 0;

	/**
	 * Simple class representing a Dataset copy.
	 * 
	 */
	public final class DatasetCopy {

		/** A dataset */
		private Dataset dataset;

		/**
		 * <code>true</code> if this dataset of the pool is available or it is
		 * already "on use" by some reader/writer.
		 */
		private boolean isAvailable;

		/** Represents the unique ID related to this specific copy of the dataset */
		private int copyID; // TODO:Should be long?

		public DatasetCopy(final int copyID, Dataset dataset) {
			this.copyID = copyID;
			isAvailable = true;
			// TODO: prior to call this method should I do a real copy?
			this.dataset = dataset;
		}

		public DatasetCopy(final int copyID) {
			this.copyID = copyID;
			isAvailable = true;

			final String fileName = sourceFile.getAbsolutePath();

			try {
				FileFormat ffo = FileFormat.getInstance(fileName);

				ffo = ffo.open(fileName, FileFormat.WRITE);
				Group root = (Group) ffo.get("/");

				final Dataset dataset = (Dataset) root.getMemberList().get(
						datasetIndex);
				this.dataset = dataset;

			} catch (OutOfMemoryError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		public Dataset getDataset() {
			return dataset;
		}

		public int getCopyID() {
			return copyID;
		}

		public boolean isAvailable() {
			return isAvailable;
		}

		public void setAvailable(boolean isAvailable) {
			this.isAvailable = isAvailable;
		}

		/**
		 * Restoring original properties for this dataset copy. I need to
		 * compute a copy of each value.
		 */
		private void restoreOriginalProperties() {
			long[] start = this.dataset.getStartDims();
			long[] stride = this.dataset.getStride();
			long[] dims = this.dataset.getDims();
			int[] selectedIndex = this.dataset.getSelectedIndex();
			long[] sizes = this.dataset.getSelectedDims();

			final int startLength = originalProperties.start.length;
			for (int i = 0; i < startLength; i++) {
				start[i] = originalProperties.start[i];
			}
			final int strideLength = originalProperties.stride.length;
			for (int i = 0; i < strideLength; i++) {
				stride[i] = originalProperties.stride[i];
			}
			final int dimsLength = originalProperties.dims.length;
			for (int i = 0; i < dimsLength; i++) {
				dims[i] = originalProperties.dims[i];
			}
			final int sizesLength = originalProperties.sizes.length;
			for (int i = 0; i < sizesLength; i++) {
				sizes[i] = originalProperties.sizes[i];
			}
			final int selectedIndexLength = originalProperties.selectedIndex.length;
			for (int i = 0; i < selectedIndexLength; i++) {
				selectedIndex[i] = originalProperties.selectedIndex[i];
			}

			System.out.print("init\n");
			this.dataset.init();
			
			// TODO: It is needed?
			this.dataset.clearData();

		}
	}

	/**
	 * A simple class which allows to store the properties of a dataset, prior
	 * it will be used/accessed with mechanismes which may modify the
	 * properties.
	 * 
	 */
	protected final class DatasetProperties {

		/**
		 * <coded>DatasetProperties</code> constructor. It provides to copy all
		 * the fields which may be changed during a parametrized read operation.
		 */
		public DatasetProperties(Dataset dataset) {
			long[] start = dataset.getStartDims();
			long[] stride = dataset.getStride();
			long[] dims = dataset.getDims();
			int[] selectedIndex = dataset.getSelectedIndex();
			long[] sizes = dataset.getSelectedDims();

			final int startLength = start.length;
			this.start = new long[startLength];
			for (int i = 0; i < startLength; i++) {
				this.start[i] = start[i];
			}
			final int strideLength = stride.length;
			this.stride = new long[strideLength];
			for (int i = 0; i < strideLength; i++) {
				this.stride[i] = stride[i];
			}
			final int dimsLength = dims.length;
			this.dims = new long[dimsLength];
			for (int i = 0; i < dimsLength; i++) {
				this.dims[i] = dims[i];
			}
			final int sizesLength = sizes.length;
			this.sizes = new long[sizesLength];
			for (int i = 0; i < sizesLength; i++) {
				this.sizes[i] = sizes[i];
			}
			final int selectedIndexLength = selectedIndex.length;
			this.selectedIndex = new int[selectedIndexLength];
			for (int i = 0; i < selectedIndexLength; i++) {
				this.selectedIndex[i] = selectedIndex[i];
			}
		}

		/** The starting position of the selected dataset */
		private final long[] start;

		/**
		 * The stride of the selected dataset. Essentially, the stride is the
		 * number of elements to move from the start location in each dimension.
		 */
		private final long[] stride;

		/** The current dimension sizes of the selected dataset */
		private final long[] dims;

		/** The selected dimensions for display */
		private final int[] selectedIndex;

		/** The selected dimension sizes of the selected dataset */
		private final long[] sizes;

	}

	public final boolean hasAvailableDatasets() {
		return availableDatasets > 0;
	}

	public DatasetCopy getDatasetCopy() {
		DatasetCopy dsc = null;

		if (hasAvailableDatasets()) {
			// synchronized (storedCopies) {
			final Set set = storedCopies.keySet();
			final Iterator iter = set.iterator();
			// Cleaning HashMap
			while (iter.hasNext()) {
				dsc = (DatasetCopy) storedCopies.get(iter.next());
				if (dsc.isAvailable()) {
					dsc.setAvailable(false);
					availableDatasets--;
					break;
				}
			}
			// }
		} else if (nCopies < datasetLimit) {
			// I can instantiate some other DatasetCopy's

			// synchronized (storedCopies) {
			final int uniqueID = IDgenerator++;
			dsc = new DatasetCopy(uniqueID);
			nCopies++;
			availableDatasets++;
			storedCopies.put(Integer.valueOf(uniqueID), dsc);
			// }
		}
		return dsc;
	}

	public void getBackDatasetCopy(final int copyID, boolean hasBeenModified) {
		// TODO: Add some checks
		// synchronized (storedCopies) {
		DatasetCopy dsc = (DatasetCopy) storedCopies.get(Integer
				.valueOf(copyID));
		if (hasBeenModified)
			dsc.restoreOriginalProperties();
		dsc.setAvailable(true);
		availableDatasets++;
		// }
	}

	public DatasetProperties getOriginalProperties() {
		return originalProperties;
	}
}
