package it.geosolutions.imageio.plugins;

/**
 * Interface defining mapping methods between global indexing and flat indexing.
 * Given a global Index, it allows to retrieve a proper Flat Reader as well as
 * the imageIndex needed to access a specific image from that Flat Reader.
 * 
 * @author Daniele Romagnoli
 */
public interface GlobalToFlatMapper {

	/** given a globalIndex, returns a reader-relative index */
	public int getFlatIndex(int globalIndex);

	/**
	 * given a <code>globalIndex</code>, returns a proper Flat Reader (an
	 * <code>AbstractImageReader</code> instance)
	 */
	public AbstractImageReader getFlatReader(int globalIndex);

}
