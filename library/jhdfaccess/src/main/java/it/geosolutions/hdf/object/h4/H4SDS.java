/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *	  https://imageio-ext.dev.java.net/
 *    (C) 2007, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.hdf.object.h4;

import it.geosolutions.hdf.object.AbstractHObject;
import it.geosolutions.hdf.object.IHObject;

import java.util.ArrayList;
import java.util.List;

import ncsa.hdf.hdflib.HDFChunkInfo;
import ncsa.hdf.hdflib.HDFCompInfo;
import ncsa.hdf.hdflib.HDFConstants;
import ncsa.hdf.hdflib.HDFException;
import ncsa.hdf.hdflib.HDFLibrary;

/**
 * Class representing a HDF Scientific Dataset.
 * 
 * @author Daniele Romagnoli
 */
public class H4SDS extends H4Variable implements IH4ReferencedObject, IHObject {

	/** predefined attributes */
	public static String PREDEF_ATTR_LONG_NAME = "long_name";

	public static String PREDEF_ATTR_UNITS = "units";

	public static String PREDEF_ATTR_FORMAT = "format";

	public static String PREDEF_ATTR_CALIBRATED_NT = "calibrated_nt";

	public static String PREDEF_ATTR_SCALE_FACTOR = "scale_factor";

	public static String PREDEF_ATTR_SCALE_FACTOR_ERR = "scale_factor_err";

	public static String PREDEF_ATTR_ADD_OFFSET = "add_offset";

	public static String PREDEF_ATTR_ADD_OFFSET_ERR = "add_offset_err";

	public static String PREDEF_ATTR_FILL_VALUE = "_FillValue";

	public static String PREDEF_ATTR_COORDINATE_SYSTEM = "cordsys";

	public static String PREDEF_ATTR_VALID_RANGE_MIN = "valid_min";

	public static String PREDEF_ATTR_VALID_RANGE_MAX = "valid_max";

	public static String PREDEF_ATTR_VALID_RANGE = "valid_range";

	private int[] mutex = new int[] { 1 };

	/**
	 * The index of this SDS within the source File.
	 * 
	 * @uml.property name="index"
	 */
	private int index;

	/**
	 * The dimension sizes of this SDS.
	 * 
	 * @uml.property name="dimSizes"
	 */
	private int[] dimSizes;

	/**
	 * The chunk sizes of this SDS.
	 * 
	 * @uml.property name="chunkSizes"
	 */
	private int[] chunkSizes;

	/**
	 * The rank of this SDS
	 * 
	 * @uml.property name="rank"
	 */
	private int rank = -1;

	/**
	 * the reference of this SDS
	 * 
	 * @uml.property name="reference"
	 */
	private H4ReferencedObject reference = null;

	/**
	 * The list of data object label annotations related to this SDS. <br>
	 */
	private List labelAnnotations = null;

	/**
	 * The number of data object label annotations related to this SDS. <br>
	 * 
	 * @uml.property name="nLabels"
	 */
	private int nLabels = -1;

	/**
	 * The list of data object description annotations related to this SDS. <br>
	 */
	private List descAnnotations = null;

	/**
	 * The number of data object description annotations related to this SDS.
	 * <br>
	 * 
	 * @uml.property name="nDescriptions"
	 */
	private int nDescriptions = -1;

	// /**
	// * the number of 2D datasets contained within this SDS.<BR>
	// * As an instance, a 3D SDS, having size 5*800*600 has datasetSize=5 since
	// * we can access to 5 different 2D-datasets
	// *
	// * @uml.property name="datasetSize"
	// */
	// private int datasetSize;

	/**
	 * the datatype of this sds
	 * 
	 * @uml.property name="datatype"
	 */
	private int datatype = HDFConstants.FAIL;

	/**
	 * The list of dimensions related to this SDS
	 * 
	 * @uml.property name="dimensions"
	 * @uml.associationEnd inverse="h4sds:it.geosolutions.hdf.object.h4.H4Dimension"
	 */
	private List dimensions;

	/**
	 * A boolean flag which tells whether the SDS has been opened.
	 * 
	 * @uml.property name="isOpened"
	 */
	private boolean isOpened;

	/**
	 * The {@link H4SDSCollection} to which this {@link H4SDS} belongs.
	 * 
	 * @uml.property name="h4SDSCollectionOwner"
	 * @uml.associationEnd inverse="sdsList:it.geosolutions.hdf.object.h4.H4SDSCollection"
	 */
	private H4SDSCollection h4SDSCollectionOwner = null;

	// ////////////////////////////////////////////////////////////////////////
	//
	// SET of Getters
	// 
	// ////////////////////////////////////////////////////////////////////////
	/**
	 * Getter of the property <code>dimSizes</code>
	 * 
	 * @return the dimension sizes of this SDS.
	 * @uml.property name="dimSizes"
	 */
	public int[] getDimSizes() {
		return dimSizes;
	}

	/**
	 * Getter of the property <code>chunkSizes</code>
	 * 
	 * @return the chunk sizes of this SDS.
	 * @uml.property name="chunkSizes"
	 */
	public int[] getChunkSizes() {
		return chunkSizes;
	}

	/**
	 * @return the reference of this SDS.
	 * @uml.property name="reference"
	 */
	public int getReference() {
		return reference.getReference();
	}

	/**
	 * Getter of the property <code>rank</code>
	 * 
	 * @return the rank of this SDS
	 * @uml.property name="rank"
	 */
	public int getRank() {
		return rank;
	}

	/**
	 * Getter of the property <code>index</code>
	 * 
	 * @return the index of this SDS within the source File
	 * @uml.property name="index"
	 */
	public int getIndex() {
		return index;
	}

	// /**
	// * Getter of the property <code>datasetSize</code>
	// *
	// * @return the number of 2D datasets contained within this SDS
	// * @uml.property name="datasetSize"
	// */
	// public int getDatasetSize() {
	// return datasetSize;
	// }

	/**
	 * Getter of the property <code>datatype</code>
	 * 
	 * @return the datatype of this sds
	 * @uml.property name="datatype"
	 */
	public int getDatatype() {
		return datatype;
	}

	/**
	 * Getter of the property <code>nDescriptions</code>
	 * 
	 * @return number of data object description annotations related to this
	 *         SDS.
	 * @uml.property name="nDescriptions"
	 */
	public int getNDescriptions() {
		synchronized (mutex) {
			if (nDescriptions == -1)
				try {
					getAnnotations(HDFConstants.AN_DATA_DESC);
				} catch (HDFException e) {
					nDescriptions = 0;
				}
			return nDescriptions;
		}
	}

	/**
	 * Getter of the property <code>nLabels</code>
	 * 
	 * @return the number of data object label annotations related to this SDS.
	 * @uml.property name="nLabels"
	 */
	public int getNLabels() {
		synchronized (mutex) {
			if (nLabels == -1)
				try {
					getAnnotations(HDFConstants.AN_DATA_LABEL);
				} catch (HDFException e) {
					nLabels = 0;
				}
			return nLabels;
		}
	}

	/**
	 * Getter of the property <code>h4SDSCollectionOwner</code>
	 * 
	 * @return the {@link H4SDSCollection} to which this {@link H4SDS} belongs.
	 * @uml.property name="h4SDSCollectionOwner"
	 */
	public H4SDSCollection getH4SDSCollectionOwner() {
		return h4SDSCollectionOwner;
	}

	// /**
	// * Main Constructor which builds a new <code>H4SDS</code> given its index
	// * within the HDF source.<BR>
	// *
	// * It is worth to point out that we need to check if the just built
	// * <code>H4SDS</code> is a dimension scale using the proper method
	// * {@link H4SDS#isDimensionScale()}. If it is not a dimension scale, then
	// * we need to initialize the <code>H4SDS</code> by calling the
	// * {@link H4SDS#init()} method.
	// *
	// * @param h4SdsCollection
	// * the parent collection
	// * @param sdsIndex
	// * the index of the SDS within the HDF source
	// *
	// */
	// public H4SDS(H4SDSCollection h4SdsCollection, final int sdsIndex) {
	// h4SDSCollectionOwner = h4SdsCollection;
	// final int interfaceID = h4SDSCollectionOwner.getIdentifier();
	// try {
	// index = sdsIndex;
	// identifier = HDFLibrary.SDselect(interfaceID, sdsIndex);
	// isOpened = true;
	// if (identifier == HDFConstants.FAIL) {
	// throw new RuntimeException(
	// "Error while creating a new H4SDS: invalid identifier");
	// }
	//
	// } catch (HDFException e) {
	// throw new RuntimeException("Error while creating a new H4SDS", e);
	// }
	// }

	private H4SDS(H4SDSCollection h4SdsCollection, int index, int identifier)
			throws HDFException {
		h4SDSCollectionOwner = h4SdsCollection;
		this.index = index;
		this.identifier = identifier;
		isOpened = true;
	}

	/**
	 * Attempts to build a new {@link H4SDS} given its index within the file. A
	 * new {@link H4SDS} is returned only if the underlying SDS does not
	 * represents a dimension scale. Otherwise, <code>null</code> will
	 * returned.
	 * 
	 * @param h4SDSCollection
	 *            the collection owner.
	 * @param index
	 *            the index of the required SDS within the file
	 * @return a new {@link H4SDS} if the underlying SDS does not represents a
	 *         dimension scale. <code>null</code> otherwise.
	 * @throws HDFException
	 */
	protected static H4SDS buildH4SDS(H4SDSCollection h4SDSCollection,
			final int index) throws HDFException {
		H4SDS sds = null;
		final int interfaceID = h4SDSCollection.getIdentifier();
		try {
			final int identifier = HDFLibrary.SDselect(interfaceID, index);
			if (identifier != HDFConstants.FAIL) {
				if (!HDFLibrary.SDiscoordvar(identifier)) {
					sds = new H4SDS(h4SDSCollection, index, identifier);
				} else
					HDFLibrary.SDendaccess(identifier);
			}

		} catch (HDFException e) {
			throw new RuntimeException("Error while creating a new H4SDS", e);
		}
		return sds;
	}

	/**
	 * Open this <code>H4SDS</code><BR>
	 * it is worth to point out that the identifier of this sds is always the
	 * same when referring to the same SDS Interface ID.
	 */
	public void open() {
		synchronized (mutex) {
			if (!isOpened) {
				if (identifier != HDFConstants.FAIL) {
					try {
						final int newIdentifier = HDFLibrary.SDselect(
								h4SDSCollectionOwner.getIdentifier(), index);
						if (newIdentifier != identifier) {
							// XXX: print some error
						}
						isOpened = true;
						init();
					} catch (HDFException e) {
						throw new RuntimeException(
								"Error while opening the H4SDS", e);
					}
				}
			}
		}
	}

	/**
	 * Initializes the {@link H4SDS} fields, such as dimension sizes, rank,
	 * reference and so on.
	 * 
	 * @throws HDFException
	 */
	private void init() throws HDFException {
		// checks if already initalized
		if (rank != -1)
			return;

		// //
		//
		// get basic info like name, size etc... and print it out
		//
		// //
		final int sdInfo[] = { 0, 0, 0 };
		final int datasetDimSizes[] = new int[HDFConstants.MAX_VAR_DIMS];
		String datasetName[] = { "" };
		if (HDFLibrary.SDgetinfo(identifier, datasetName, datasetDimSizes,
				sdInfo)) {

			reference = new H4ReferencedObject(HDFLibrary.SDidtoref(identifier));
			name = datasetName[0];
			rank = sdInfo[0];
			dimSizes = new int[rank];
			// datasetSize = 1;
			for (int j = 0; j < rank; j++) {
				dimSizes[j] = datasetDimSizes[j];
				// when rank > 2, X and Y are the last 2 coordinates. As an
				// instance, for a 3D subdatasets, 3rd dimension has index 0.
				// if (j < rank - 2)
				// datasetSize *= datasetDimSizes[j];
			}
			sdInfo[1] = sdInfo[1] & (~HDFConstants.DFNT_LITEND);
			datatype = sdInfo[1];
			numAttributes = sdInfo[2];
			initDecorated();
			dimensions = new ArrayList(rank);

			HDFChunkInfo chunkInfo = new HDFChunkInfo();
			final int[] cflag = { HDFConstants.HDF_NONE };
			boolean status = HDFLibrary.SDgetchunkinfo(identifier, chunkInfo,
					cflag);
			if (status) {
				if (cflag[0] == HDFConstants.HDF_NONE)
					chunkSizes = null;
				else {
					chunkSizes = new int[rank];
					for (int k = 0; k < rank; k++)
						chunkSizes[k] = chunkInfo.chunk_lengths[k];
				}
			}
		} else {
			// XXX
		}
	}

	protected void finalize() throws Throwable {
		dispose();
	}

	/**
	 * close this {@link H4SDS} and its owned {@link AbstractHObject}s and
	 * dispose allocated objects.
	 */
	public void dispose() {
		synchronized (mutex) {
			if (dimensions != null) {
				final int dimSizes = dimensions.size();
				if (dimSizes != 0) {
					for (int i = 0; i < dimSizes; i++) {
						H4Dimension dim = (H4Dimension) dimensions.get(i);
						dim.dispose();
					}
				}
			}
			if (descAnnotations != null) {
				for (int i = 0; i < nDescriptions; i++) {
					H4Annotation ann = (H4Annotation) descAnnotations.get(i);
					ann.close();
				}
			}
			if (labelAnnotations != null) {
				for (int i = 0; i < nLabels; i++) {
					H4Annotation ann = (H4Annotation) labelAnnotations.get(i);
					ann.close();
				}
			}

			// Disposing objects hold by H4DecoratedObject superclass
			super.dispose();
			close();
			identifier = HDFConstants.FAIL;
		}
	}

	/**
	 * Terminate access to this SDS.
	 */
	public void close() {
		synchronized (mutex) {
			// During the H4SDSCollection initialization, all SDSs are opened,
			// initialized and immediatly closed. When a SDS is required, the
			// H4SDSCollection re-open access to the specific SDS. When a
			// H4SDSCollection is closed, it attempts to close all H4SDSs by
			// calling the close method of each of them. Only the really opened
			// H4SDSs need to be closed.
			if (isOpened) {
				try {
					if (identifier != HDFConstants.FAIL) {
						HDFLibrary.SDendaccess(identifier);
						isOpened = false;
					}
				} catch (HDFException e) {
					// XXX
				}
			}
		}
	}

	/**
	 * Read all data from the current SDS.
	 * 
	 * @return an array of the proper type containing read data
	 * @throws HDFException
	 */
	public Object read() throws HDFException{
		final int start[] = new int[rank];
		final int stride[] = new int[rank];
		final int size[] = new int[rank];
		
		for (int i=0;i<rank;i++){
			start[i]=0;
			stride[i]=1;
			size[i]=dimSizes[i];
		}
		return read(start,stride,size);
	}
	
	/**
	 * Read a required zone of data, given a set of input parameters.
	 * 
	 * @param selectedStart
	 *            int array indicating the start point of each dimension
	 * @param selectedStride
	 *            int array indicating the required stride of each dimension
	 * @param selectedSizes
	 *            int array indicating the required size along each dimension
	 * @return an array of the proper type containing read data
	 * @throws HDFException
	 */
	public Object read(final int selectedStart[], final int selectedStride[],
			final int selectedSizes[]) throws HDFException {

		Object theData = null;
		int datasize = 1;
		int[] size = new int[rank];
		int[] start = new int[rank];
		int[] stride = null;
		for (int i = 0; i < rank; i++) {
			datasize *= selectedSizes[i];
			size[i] = selectedSizes[i];
			start[i] = selectedStart[i];
		}
		if (selectedStride != null) {
			stride = new int[rank];
			for (int i = 0; i < rank; i++)
				stride[i] = selectedStride[i];
		}

		// allocate the required data array where data read will be stored.
		theData = H4DatatypeUtilities.allocateArray(datatype, datasize);
		// //
		//
		// Read operation
		//
		// //
		if (theData != null) {
			HDFLibrary.SDreaddata(identifier, start, stride, size, theData);
		}

		return theData;
	}

	/**
	 * Returns compression info related to this SDS
	 * 
	 * @return the type of compression.
	 * @throws HDFException
	 */
	public int getCompression() throws HDFException {
		final HDFCompInfo compInfo = new HDFCompInfo();
		HDFLibrary.SDgetcompress(identifier, compInfo);
		return compInfo.ctype;
	}

	/**
	 * Returns a <code>List</code> containing available dimensions for this
	 * SDS.
	 * 
	 * @return a <code>List</code> of {@link H4Dimension}s
	 */
	public List getDimensions() throws HDFException {
		synchronized (mutex) {
			if (dimensions.size() == 0) {
				for (int i = 0; i < rank; i++) {
					// Adding dimensions if needed
					H4Dimension dimension = new H4Dimension(this, i);
					dimensions.add(i, dimension);
				}
			}
			return dimensions;
		}
	}

	/**
	 * returns a specific dimension of this SDS, given its index.
	 * 
	 * @param index
	 *            the index of the required dimension
	 * @return the {@link H4Dimension} related to the specified index.
	 * @throws HDFException
	 */
	public H4Dimension getDimension(final int index) throws HDFException {
		if (index > rank || index < 0)
			throw new IndexOutOfBoundsException(
					"Specified index is not valid. It should be greater than zero and belower than rank which is "
							+ rank);
		H4Dimension dim = (H4Dimension) getDimensions().get(index);
		return dim;
	}

	/**
	 * Returns a <code>List</code> of annotations available for this SDS,
	 * given the required type of annotations.
	 * 
	 * @param annotationType
	 *            the required annotation type. One of:<BR>
	 *            <code>HDFConstants.AN_DATA_LABEL</code> for label
	 *            annotations<BR>
	 *            <code>HDFConstants.AN_DATA_DESC</code> for description
	 *            annotations
	 * @return the <code>List</code> of annotations available for this SDS
	 * @throws HDFException
	 */
	public List getAnnotations(final int annotationType) throws HDFException {
		synchronized (mutex) {

			H4AnnotationManager annotationManager = h4SDSCollectionOwner
					.getH4File().getH4AnnotationManager();
			short shortRef = (short) reference.getReference();
			switch (annotationType) {
			case HDFConstants.AN_DATA_LABEL:
				if (nLabels == -1) {
					// Searching data object label annotations related to this
					// SDS using the new TAG.
					List listLabels = annotationManager.getH4Annotations(
							HDFConstants.AN_DATA_LABEL,
							(short) HDFConstants.DFTAG_NDG, shortRef);
					if (listLabels == null) {
						// Searching data object label annotations related to
						// this SDS using the obsolete TAG.
						listLabels = annotationManager.getH4Annotations(
								HDFConstants.AN_DATA_LABEL,
								(short) HDFConstants.DFTAG_SDG, shortRef);
					}
					if (listLabels == null || listLabels.size() == 0) {
						nLabels = 0;
					} else
						nLabels = listLabels.size();
					labelAnnotations = listLabels;
				}
				return labelAnnotations;
			case HDFConstants.AN_DATA_DESC:
				if (nDescriptions == -1) {
					// Searching data object label annotations related to this
					// SDS using the new TAG.
					List listDescriptions = annotationManager.getH4Annotations(
							HDFConstants.AN_DATA_DESC,
							(short) HDFConstants.DFTAG_NDG, shortRef);
					if (listDescriptions == null) {
						// Searching data object label annotations related to
						// this SDS using the obsolete TAG.
						listDescriptions = annotationManager.getH4Annotations(
								HDFConstants.AN_DATA_DESC,
								(short) HDFConstants.DFTAG_SDG, shortRef);
					}
					if (listDescriptions == null
							|| listDescriptions.size() == 0) {
						nDescriptions = 0;
					} else
						nDescriptions = listDescriptions.size();
					descAnnotations = listDescriptions;
				}
				return descAnnotations;
			default:
				return null;
			}
		}
	}

	// /**
	// * returns <code>true</code> if this SDS is a Dimension Scale.
	// *
	// * @return <code>true</code> if this SDS is a Dimension Scale.
	// * @throws HDFException
	// */
	// protected boolean isDimensionScale() throws HDFException {
	// return HDFLibrary.SDiscoordvar(identifier);
	// }
}
