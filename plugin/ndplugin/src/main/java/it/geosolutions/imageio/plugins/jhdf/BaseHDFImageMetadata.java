package it.geosolutions.imageio.plugins.jhdf;

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;

import ncsa.hdf.object.Dataset;

public abstract class BaseHDFImageMetadata extends IIOMetadata {

	/** The Dataset Name */
	protected String name = "";

	/** The Dataset Rank */
	protected int rank = -1;

	/** The Dataset Dims Sizes. 
	 * As an Instance, a Dataset may have (x,y,z,t) respectively (3200,2000,10,5)
	 */
	protected long dims[] = null;

	/** The Dataset Chunk Size expressed as a String.
	 * As an Instance, a 2D Dataset may have a chunk size of 512*512 
	 * In such a case, chunkSize value is "512,512".
	 * */
	protected long chunkSize[] = null;

	public BaseHDFImageMetadata(boolean standardMetadataFormatSupported,
			String nativeMetadataFormatName,
			String nativeMetadataFormatClassName,
			String[] extraMetadataFormatNames,
			String[] extraMetadataFormatClassNames) {
		super(standardMetadataFormatSupported, nativeMetadataFormatName,
				nativeMetadataFormatClassName, extraMetadataFormatNames,
				extraMetadataFormatClassNames);
	}

	/**
	 * Returns a <code>IIOMetadataNode</code> which should be common to each
	 * specific {@link BaseHDFImageMetada} extension since each HDF dataset 
	 * has a Rank, a Name, a set of dims length.
	 * 
	 * @return the <code>IIOMetadataNode</code> common to each HDF metadata.
	 */
	protected IIOMetadataNode getCommonDatasetNode() {
		//The generated Node is common to each HDF metadata structure.
		
		 /*
         * root
         *   +-- datasetProperties (Name, Rank, Dims, ChunkSize)
         */
		
		final IIOMetadataNode datasetNode = new IIOMetadataNode(
				"DatasetProperties");
		datasetNode.setAttribute("Name", name);
		datasetNode.setAttribute("Rank", Integer.toString(rank));
		
		String sDims="";
		if (dims != null) {
			final int dimsLength = dims.length;
			final StringBuffer sb = new StringBuffer();
			for (int i = 0; i < dimsLength; i++) {
				sb.append(Long.toString(dims[i]));
				if (i!=dimsLength-1)
					sb.append(",");
			}
			sDims = sb.toString().trim();
		}
		datasetNode.setAttribute("Dims", sDims);
		
		String sChunkSize="";
		if (chunkSize != null) {
			final int chunkSizeLength = chunkSize.length;
			final StringBuffer sb = new StringBuffer();
			for (int i = 0; i < chunkSizeLength; i++) {
				sb.append(Long.toString(chunkSize[i]));
				if (i!=chunkSizeLength-1)
					sb.append(",");
			}
			sChunkSize = sb.toString().trim();
		}
		
		datasetNode.setAttribute("ChunkSize", sChunkSize);
		//TODO: Should I add envelope?
		return datasetNode;
	}
	
	protected void initializeCommonDatasetProperties(SubDatasetInfo sdInfo) {
		//	 setting dims (array copy)
		final long dims[] = sdInfo.getDims();
		if (dims!=null){
			final int dimsLength=dims.length;
			this.dims=new long[dimsLength];
			for (int i=0;i<dimsLength;i++)
				this.dims[i]=dims[i];
		}
		
		// setting chunkSize (array copy)
		final long chunkSize[] = sdInfo.getChunkSize();
		if (chunkSize!=null){
			final int chunkSizeLength=chunkSize.length;
			this.chunkSize=new long[chunkSizeLength];
			for (int i=0;i<chunkSizeLength;i++)
				this.chunkSize[i]=chunkSize[i];
		}

		// setting rank
		this.rank = sdInfo.getRank();
		
		// setting name
		this.name = sdInfo.getName();
	}
	
	/**
	 * Set properties common to each HDF format (not a specific "profile"). 
	 * That is, any HDF dataset has a Rank, a Name, a set of dims length.
	 * 
	 * @param dataset
	 */
	protected void initializeCommonDatasetProperties(Dataset dataset){
		// setting dims (array copy)
		final long dims[] = dataset.getDims();
		if (dims!=null){
			final int dimsLength=dims.length;
			this.dims=new long[dimsLength];
			for (int i=0;i<dimsLength;i++)
				this.dims[i]=dims[i];
		}
		
		// setting chunkSize (array copy)
		final long chunkSize[] = dataset.getChunkSize();
		if (chunkSize!=null){
			final int chunkSizeLength=chunkSize.length;
			this.chunkSize=new long[chunkSizeLength];
			for (int i=0;i<chunkSizeLength;i++)
				this.chunkSize[i]=chunkSize[i];
		}

		// setting rank
		this.rank = dataset.getRank();
		
		// setting name
		this.name = dataset.getName();
	}
}
