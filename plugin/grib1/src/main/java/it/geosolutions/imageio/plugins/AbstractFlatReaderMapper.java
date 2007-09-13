package it.geosolutions.imageio.plugins;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * Abstract class defining a Mapping between Global indexes and Flat structures.
 * 
 * @author Daniele Romagnoli
 */
public abstract class AbstractFlatReaderMapper implements GlobalToFlatMapper{

	protected class FlatReaderProperties {
		private URI srcURI;

		private int baseIndex;
		
		public FlatReaderProperties(URI srcURI, int baseIndex){
			this.srcURI= srcURI;
			this.baseIndex = baseIndex;
		}

		public int getBaseIndex() {
			return baseIndex;
		}

		public URI getSrcURI() {
			return srcURI;
		}
	}

	/**
	 * mapping betweens globalIndex and FlatReaderProperties instances.
	 */
	protected Map indexMap=null;
	
	/**
	 * mapping betweens globalIndex and SliceDescriptors.
	 */
	protected Map indexToSliceDescriptorMap;

	/**
	 * Use a FlatReader for each URI. This map contains a mapping betweens source
	 * URI and readers.
	 */
	protected Map uriToReaderMap;
	
	public abstract void initialize(Node node);
	
	public Collection getDescriptors(){
		return indexToSliceDescriptorMap.values();
	}

	public Map getIndexToSliceDescriptorMap() {
		return indexToSliceDescriptorMap;
	}

}
