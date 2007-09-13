package it.geosolutions.imageio.plugins.grib1;

import it.geosolutions.imageio.plugins.AbstractFlatReaderMapper;
import it.geosolutions.imageio.plugins.AbstractImageReader;
import it.geosolutions.imageio.plugins.SliceDescriptor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import net.sourceforge.jgrib.tables.GribPDSParamTable;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * Specific class used to implement a mapping between globalIndexes and
 * {@link GRIB1ImageReader}s
 * 
 * @author Daniele Romagnoli
 */

public class GRIB1FlatReaderMapper extends AbstractFlatReaderMapper {
	// TODO: Add synchronization

	// Parameter Table
	protected GribPDSParamTable paramTable = null;

	protected Map sliceDescriptorStringToIndexMap;

	protected Map relatedIndexesMap = Collections
			.synchronizedMap(new TreeMap());

	public void initialize(Node node) {
		if (node != null) {
			// TODO: Add Synchronization
			// change from 1.4 to 1.5 map settings

			// Locating the numImages Node
			Node imagesNode = node.getFirstChild().getFirstChild()
					.getNextSibling().getNextSibling();

			// TODO: optimize this
			int numImages = Integer.parseInt(imagesNode.getNodeValue());
			indexMap = Collections.synchronizedMap(new HashMap(numImages));
			uriToReaderMap = Collections.synchronizedMap(new HashMap(10));

			indexToSliceDescriptorMap = Collections
					.synchronizedMap(new TreeMap());
			sliceDescriptorStringToIndexMap = Collections
					.synchronizedMap(new HashMap());
			imagesNode = imagesNode.getNextSibling();
			Node imageNode = imagesNode.getFirstChild();
			int globalIndex = 0;

			while (imageNode != null) {
				Node imageIndexNode = imageNode.getFirstChild();
				Node srcUriNode = imageIndexNode.getNextSibling();
				try {
					final int imageIndex = Integer.parseInt(imageIndexNode
							.getNodeValue());
					final URI uri = new URI(srcUriNode.getNodeValue());
					indexMap.put(Integer.valueOf(globalIndex),
							new FlatReaderProperties(uri, imageIndex));
					GRIB1ImageReader flatReader;
					if (uriToReaderMap.containsKey(uri)) {
						flatReader = (GRIB1ImageReader) uriToReaderMap.get(uri);
					} else {
						flatReader = new GRIB1ImageReader(
								new GRIB1ImageReaderSpi());
						flatReader.setInput(uri);
						uriToReaderMap.put(uri, flatReader);
					}
					// TODO: trivial implementation
					SliceDescriptor sd = flatReader
							.getSliceDescriptor(imageIndex);

					// Initialize paramTable
					if (paramTable == null)
						paramTable = GRIB1Utilities
								.getParameterTable(((GRIB1SliceDescriptor) sd)
										.getParameterID());
					sd.setGlobalIndex(globalIndex);
					indexToSliceDescriptorMap.put(Integer.valueOf(globalIndex),
							sd);
					sliceDescriptorStringToIndexMap.put(sd.toString(), Integer
							.valueOf(globalIndex));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
				} catch (DOMException e) {
					// TODO Auto-generated catch block
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
				globalIndex++;
				imageNode = imageNode.getNextSibling();
			}
		}
	}

	/**
	 * @see {@link GlobalToFlatMapper#getFlatReader(int globalIndex)}
	 */
	public AbstractImageReader getFlatReader(int globalIndex) {
		FlatReaderProperties property = (FlatReaderProperties) indexMap
				.get(Integer.valueOf(globalIndex));
		AbstractImageReader flatReader = null;
		if (property != null) {
			URI srcURI = property.getSrcURI();
			if (uriToReaderMap.containsKey(srcURI)) {
				flatReader = (AbstractImageReader) uriToReaderMap.get(srcURI);
			} else {
				flatReader = new GRIB1ImageReader(new GRIB1ImageReaderSpi());
				flatReader.setInput(srcURI);
				uriToReaderMap.put(srcURI, flatReader);
			}
		}
		return flatReader;
	}

	/**
	 * Returns a real imageIndex (a Flat Index) given a globalIndex
	 */
	public int getFlatIndex(int globalIndex) {
		FlatReaderProperties property = (FlatReaderProperties) indexMap
				.get(Integer.valueOf(globalIndex));
		if (property != null)
			return property.getBaseIndex();
		else
			return -1;
	}

	public Map getSliceDescriptorStringToIndexMap() {
		return sliceDescriptorStringToIndexMap;
	}

	protected GribPDSParamTable getParamTable() {
		return paramTable;
	}

}
