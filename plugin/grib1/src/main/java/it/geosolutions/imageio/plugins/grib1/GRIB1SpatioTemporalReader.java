package it.geosolutions.imageio.plugins.grib1;

import it.geosolutions.imageio.plugins.AbstractFlatReaderMapper;
import it.geosolutions.imageio.plugins.AbstractSpatioTemporalReader;
import it.geosolutions.imageio.plugins.SliceDescriptor;
import it.geosolutions.imageio.plugins.SpatioTemporalIndexMapper;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.imageio.metadata.IIOMetadata;

import net.sourceforge.jgrib.tables.GribPDSParamTable;

import org.w3c.dom.Node;

public class GRIB1SpatioTemporalReader extends AbstractSpatioTemporalReader {

	protected AbstractFlatReaderMapper getMapper() {
		return readerMapper;
	}

	public void setInput(Object input) {
		entryPoint = input;
		initializeFlatMapping();
	}

	/**
	 * Initialize Mapping betweens globalImageIndexes and FlatReaders
	 */
	public void initializeFlatMapping() {
		GRIB1ImageReader discoveryReader = new GRIB1ImageReader(
				new GRIB1ImageReaderSpi());
		discoveryReader.setInput(entryPoint);
		Node node = discoveryReader.getStreamMetadata().getAsTree(
				GRIB1BasicStreamMetadata.nativeMetadataFormatName);
		readerMapper = new GRIB1FlatReaderMapper();
		readerMapper.initialize(node);
		initFilters(readerMapper.getDescriptors());
	}

	/**
	 * Given a <code>Collection</code> of {@link GRIB1SliceDescriptor}
	 * initialize a hierarchical mapping by this way: A map of parameters, where
	 * each object of this map is a map of times where each object is a map of
	 * zeta levels, where each map contains overviews
	 * 
	 * @param descriptors
	 */
	private void initFilters(final Collection descriptors) {
		// TODO Create comparators

		for (Iterator i = descriptors.iterator(); i.hasNext();) {
			SliceDescriptor desc = (SliceDescriptor) i.next();
			if (!paramsList.containsKey(desc.getParameterName())) {
				// Initializing the params tree-map
				final TreeMap timesList = new TreeMap();
				paramsList.put(desc.getParameterName(), timesList);
			}

			final TreeMap timesList = (TreeMap) paramsList.get(desc
					.getParameterName());
			if (!timesList.containsKey(desc.getTemporalDomain())) {
				// Initializing the times tree-map
				final TreeMap zList = new TreeMap();
				timesList.put(desc.getTemporalDomain(), zList);
			}

			final TreeMap zList = (TreeMap) timesList.get(desc
					.getTemporalDomain());
			if (!zList.containsKey(Float.valueOf(desc.getZetaLevel()))) {
				// Initializing the z-Levels tree-map
				final TreeMap resList = new TreeMap();
				zList.put(Float.valueOf(desc.getZetaLevel()), resList);
			}

			final TreeMap resList = (TreeMap) zList.get(Float.valueOf(desc
					.getZetaLevel()));
			if (!resList.containsKey(Integer.valueOf(desc.getOverviewLevel()))) {
				resList.put(Integer.valueOf(desc.getOverviewLevel()),
						new Integer(desc.getGlobalIndex()));
			}
		}
	}

	/**
	 * Read a single image given the related global image index and return it as
	 * a <code>BufferedImage</code>.
	 * 
	 * @param globalIndex
	 *            the index identifying the requested image.
	 * @return the requested image as a <code>BufferedImage</code>.
	 * 
	 * @throws IOException
	 */
	private BufferedImage read(int globalIndex) throws IOException {
		GRIB1ImageReader reader = (GRIB1ImageReader) readerMapper
				.getFlatReader(globalIndex);
		return reader.read(readerMapper.getFlatIndex(globalIndex));
	}

	/**
	 * Builds a <code>List</code> containing Images coming from read
	 * operations performed given a set of specified globalIndexes.
	 * 
	 * @param globalIndexes
	 * @return
	 */
	private List read(int globalIndexes[]) {
		final int size = globalIndexes.length;
		List imagesList = new ArrayList(size);

		for (int i = 0; i < size; i++) {
			try {
				imagesList.add(read(globalIndexes[i]));
			} catch (IOException e) {
				// XXX
			}
		}
		return imagesList;
	}

	public IIOMetadata getCoverageMetadata() {
		return new GRIB1CoverageMetadata(this);
	}

	public IIOMetadata getStreamMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see {@link SpatioTemporalIndexMapper#getGlobalIndexes(String, String[], float[], int[], String[])}
	 */
	public int[] getGlobalIndexes(String coverageName, String[] iso8601Times,
			float[] zeta, int[] overviewLevels, String[] bands) {
		String paramNames[];
		final boolean multiComponents = GRIB1Utilities
				.isMultiComponents(coverageName);

		final GribPDSParamTable table = ((GRIB1FlatReaderMapper) readerMapper)
				.getParamTable();

		if (multiComponents) {
			if (bands == null) {
				paramNames = GRIB1Utilities.getParamNames(coverageName, table);
			} else {
				// TODO: add some coherency checks
				final int selectedBands = bands.length;
				paramNames = new String[selectedBands];
				String[] tempParamNames = GRIB1Utilities.getParamNames(
						coverageName, table);
				final int availableBands = tempParamNames.length;
				for (int i = 0; i < availableBands; i++)
					for (int j = 0; j < selectedBands; j++)
						if (bands[j].equalsIgnoreCase(tempParamNames[i]))
							paramNames[j] = tempParamNames[i];
			}
		} else
			paramNames = new String[] { coverageName };
		return getGlobalIndexes(paramNames, iso8601Times, zeta, overviewLevels);
	}

	public int[] getGlobalIndexes(String coverageName, String[] iso8601Times,
			float[] zeta, int[] overviewLevels, int[] bands) {

		final GribPDSParamTable table = ((GRIB1FlatReaderMapper) readerMapper)
				.getParamTable();

		String paramNames[];
		final boolean multiComponents = GRIB1Utilities
				.isMultiComponents(coverageName);
		if (multiComponents) {
			if (bands == null)
				paramNames = GRIB1Utilities.getParamNames(coverageName, table);
			else {
				// TODO: add some coherency checks
				final int selectedBands = bands.length;
				paramNames = new String[selectedBands];
				String[] tempParamNames = GRIB1Utilities.getParamNames(
						coverageName, table);
				for (int i = 0; i < selectedBands; i++) {
					paramNames[i] = tempParamNames[bands[i]];
				}
			}
		} else
			paramNames = new String[] { coverageName };
		return getGlobalIndexes(paramNames, iso8601Times, zeta, overviewLevels);
	}

	private int[] getGlobalIndexes(String[] paramNames, String[] iso8601Times,
			float[] zeta, int[] overviewLevels) {

		Collection globalIndexes = new ArrayList();
		final int paramNumbers = paramNames.length;

		// ////////////////////////////////////////////////////////////////////
		// 
		// 1ST level selection: parameter Name
		//
		// ////////////////////////////////////////////////////////////////////
		for (int i = 0; i < paramNumbers; i++) {
			TreeMap timeLevelsMap = (TreeMap) paramsList.get(paramNames[i]);
			final int nTimes;
			if (timeLevelsMap == null) {
				continue;
			}
			if (iso8601Times != null) {
				nTimes = iso8601Times.length;
			} else {
				nTimes = timeLevelsMap.size();
				iso8601Times = new String[nTimes];
				Iterator it = timeLevelsMap.keySet().iterator();
				int item = 0;
				while (it.hasNext()) {
					iso8601Times[item] = (String) it.next();
					item++;
				}
			}

			// ////////////////////////////////////////////////////////////////
			// 
			// 2ND level selection: Time slices
			//
			// ////////////////////////////////////////////////////////////////
			for (int t = 0; t < nTimes; t++) {
				TreeMap zlevelsMap = (TreeMap) timeLevelsMap
						.get((String) iso8601Times[t]);
				final int nZeta;
				if (zlevelsMap == null) {
					continue;
				}
				if (zeta != null) {
					nZeta = zeta.length;
				} else {
					nZeta = zlevelsMap.size();
					zeta = new float[nZeta];
					Iterator it = zlevelsMap.keySet().iterator();
					int item = 0;
					while (it.hasNext()) {
						zeta[item] = ((Float) it.next()).floatValue();
						item++;
					}
				}

				// ////////////////////////////////////////////////////////////
				// 
				// 3RD level selection: Z-level selection
				//
				// ////////////////////////////////////////////////////////////
				for (int z = 0; z < nZeta; z++) {
					TreeMap overviewLevelMap = (TreeMap) zlevelsMap.get(Float
							.valueOf(zeta[z]));
					final int nOverviews;
					if (overviewLevelMap == null) {
						continue;
					}
					if (overviewLevels != null) {
						nOverviews = overviewLevels.length;
					} else {
						nOverviews = overviewLevelMap.size();
						overviewLevels = new int[nOverviews];
						Iterator it = overviewLevelMap.keySet().iterator();
						int item = 0;
						while (it.hasNext()) {
							overviewLevels[item] = ((Integer) it.next())
									.intValue();
							item++;
						}
					}

					// ////////////////////////////////////////////////////////////
					// 
					// Final level selection: Overviews selection
					//
					// ////////////////////////////////////////////////////////////
					for (int l = 0; l < nOverviews; l++) {
						Integer globIndex = (Integer) overviewLevelMap
								.get(Integer.valueOf(overviewLevels[l]));
						globalIndexes.add(globIndex);
					}
				}
			}
		}
		
		//Converting Integers in int
		//TODO: When moving to 1.5 change this section
		Object[] integers = globalIndexes.toArray();
		final int nIndexes = integers.length;
		final int[] ints = new int[nIndexes];
		for (int i = 0; i < nIndexes; i++) {
			ints[i] = ((Integer) integers[i]).intValue();
		}
		return ints;
	}

	/**
	 * @see {@link AbstractSpatioTemporalReader#read(String, String[], float[], int[], int[])}
	 */
	public List read(String coverageName, String[] iso8601times, float[] zeta,
			int[] overviewLevels, int[] bands) {
		return read(getGlobalIndexes(coverageName, iso8601times, zeta,
				overviewLevels, bands));
	}

	/**
	 * @see {@link AbstractSpatioTemporalReader#read(String, String[], float[], int[], String[])}
	 */
	public List read(String coverageName, String[] iso8601times, float[] zeta,
			int[] overviewLevels, String[] bands) {
		return read(getGlobalIndexes(coverageName, iso8601times, zeta,
				overviewLevels, bands));
	}
}
