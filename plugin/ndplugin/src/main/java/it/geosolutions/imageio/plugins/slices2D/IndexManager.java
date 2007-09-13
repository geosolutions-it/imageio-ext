package it.geosolutions.imageio.plugins.slices2D;

public interface IndexManager {

	/**
	 * Retrieve a slice2D index, from an imageIndex and a vector of
	 * selectedDims. This method should be used to access (sub)datasets having
	 * more then 2D.
	 * 
	 * @param imageIndex
	 *            Related to a Specific Product contained within a sourceFile or
	 *            a subDataset
	 * 
	 * @param selectedIndexOfEachDim
	 *            An <code>int</code> array containing the required index in
	 *            the proper dimension. As an instance, suppose having a
	 *            products with rank=4 and dimensions = [1024,768,10,20] that is
	 *            a dataset having 1024x768 wider 2D slices, for 10 z-levels at
	 *            20 time instants. If you want to retrieve the subIndex needed
	 *            to access to the 3rd z-level of the 7th instant, you should 
	 *            specify a dimensionIndex of [2,6] (starting from zero). 
	 */
	public int retrieveSlice2DIndex(final int imageIndex, int[] selectedIndexOfEachDim);

	
	/**
	 * Given a specified index, returns a <code>int[]</code> containing indexing
	 * information such as coverageIndex, Nth dimension, (N-1)th-dimension, until
	 * (N-2)th-dimension. In case of 2D subDatasets, it simply returns the index
	 * of the coverage in the source. In case of source having a single 2D dataset,
	 * it returns 0. 
	 * 
	 * @param imageIndex
	 * 			The imageIndex to be "parsed"
	 * @return
	 * 			
	 */
	public int[] getSlice2DIndexCoordinates(final int requiredSlice2DIndex);
}
