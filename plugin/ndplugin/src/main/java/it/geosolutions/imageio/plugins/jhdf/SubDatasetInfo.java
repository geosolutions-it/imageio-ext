package it.geosolutions.imageio.plugins.jhdf;

import ncsa.hdf.object.Datatype;

public class SubDatasetInfo {
		private String name;
		private int rank;
		private long [] dims;
		private long [] chunkSize;
		private Datatype datatype;
		public SubDatasetInfo(final String name, final int rank, long[] subDatasetDims, long[] subDatasetChunkSize, final Datatype datatype) {
			if (subDatasetDims.length!=rank)
				throw new RuntimeException("Wrong SubDatasetInfo initialization. subDatasetDims length != rank");
			this.name=name;
			this.dims = subDatasetDims;
			this.rank = rank; 
			this.datatype = datatype;
			this.chunkSize = subDatasetChunkSize;
		}
		public long[] getChunkSize() {
			return chunkSize;
		}
		public void setChunkSize(long[] chunkSize) {
			this.chunkSize = chunkSize;
		}
		public long[] getDims() {
			return dims;
		}
		public void setDims(long[] dims) {
			this.dims = dims;
		}
		public final String getName() {
			return name;
		}
		public void setName(final String name) {
			this.name = name;
		}
		public final int getRank() {
			return rank;
		}
		public void setRank(final int rank) {
			this.rank = rank;
		}
		public Datatype getDatatype() {
			return datatype;
		}
		public void setDatatype(final Datatype datatype) {
			this.datatype = datatype;
		}
		public int getWidth() {
			return (int)dims[rank-1];
		}
		public int getHeight() {
			return (int)dims[rank-2];
		}
}
