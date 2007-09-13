package it.geosolutions.imageio.plugins;

/**
 * This interface allows to retrieve global indexes (which refer to 2D Slices)
 * given a specified time (or interval), z (or vertical range), overview level
 * (or a set of them)
 * 
 * @author Romagnoli Daniele
 * 
 */
public interface SpatioTemporalIndexMapper {

	/**
	 * Return the set of globalIndexes referring to all the 2D slices pertaining
	 * to specific domain identified by the input parameters.
	 * 
	 * @param coverageName
	 * @param iso8601times
	 *            an ISO8601 formatted <code>String</code>'s array
	 *            representing Time instants or Time periods.
	 * @param zeta
	 *            //TODO: discuss this parameter.
	 * 
	 * @param levels
	 *            an int array specifying the required resolution levels.
	 * @param bands
	 *            an array specifying required bands.
	 * @return an array of integer containing globalIndexes referring the
	 *         required slices.
	 */
	public int[] getGlobalIndexes(String coverageName, String iso8601times[],
			float zeta[], int[] levels, int bands[]);

	public int[] getGlobalIndexes(String coverageName, String iso8601times[],
			float zeta[], int[] levels, String bands[]);

}
