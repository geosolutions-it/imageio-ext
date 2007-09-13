package it.geosolutions.imageio.plugins;

import java.util.List;
import java.util.TreeMap;

import javax.imageio.metadata.IIOMetadata;

/**
 * Main abstract class defining an Abstract SpatioTemporal Reader (the "Smart"
 * one). Such a reader allows to gain access to several 2D slices of a coverage,
 * given a set of parameters specifying time, zeta and overview levels.<BR>
 * 
 * It will use a proper set of Flat Readers to get the required data.
 * 
 * @author Daniele Romagnoli
 */
public abstract class AbstractSpatioTemporalReader implements SpatioTemporalIndexMapper{

	public abstract void setInput(Object input);

	/**
	 * Implements this method to define a mapping between global Image indexes
	 * and Flat readers (and flat indexes).
	 */
	public abstract void initializeFlatMapping();

	public abstract IIOMetadata getCoverageMetadata();

	public abstract IIOMetadata getStreamMetadata();

	/** the entry point. */
	protected Object entryPoint;

	final protected TreeMap paramsList = new TreeMap();
	
	/**
	 * An instance of an {@link AbstractFlatReaderMapper} which will allow to
	 * get access to 2D slices by means of proper Flat readers.
	 */
	protected AbstractFlatReaderMapper readerMapper = null;
	
	public abstract List read (String coverageName, String iso8601times[], float zeta[], int[] overviewLevels, int bands[]);
	
	public abstract List read (String coverageName, String iso8601times[], float zeta[], int[] overviewLevels, String bands[]);
}
