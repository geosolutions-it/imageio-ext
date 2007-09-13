package it.geosolutions.imageio.plugins.grib1;

public class GRIB1FileDescriptorsMapper {

//	// <GribFile,GribFileDescriptor> mapping
//	private SoftValueHashMap gribFileMap;
//
//	public GribFileDescriptor getGribFileDescriptor(GribFile gribFile) {
//		if (gribFileMap.containsKey(gribFile))
//			return (GribFileDescriptor) gribFileMap.get(gribFile);
//		else {
//			GribFileDescriptor gfd = new GribFileDescriptor(gribFile);
//			gribFileMap.put(gribFile, gfd);
//			return gfd;
//		}
//	}
//
//	protected class GribFileDescriptor {
//		public GribFileDescriptor(GribFile gribFile) {
//			this.gribFile = gribFile;
//			initialize();
//			
//		}
//
//		private GribFile gribFile;
//		
//		private int nRecords;
//
//		// <imageIndex, DataDescriptor> mapping
//		protected List imagesList;
//		
//
//		protected class DataDescriptor {
//			private int availableBands;
//
//			private int[] recordIndex;
//
//		}
//		
//		public void initialize(){
//			nRecords = gribFile.getRecordCount();
//			int imageIndex = 0;
//			for (int recordIndex = 0; recordIndex < nRecords; recordIndex++) {
//				GribRecord gr = gribFile.getRecord(recordIndex + 1);
//				GribRecordPDS pds = gr.getPDS();
//				int paramNum = pds.getParameter().getNumber();
//				int tableVersion = pds.getParamTable().getVersionNumber();
//
//				// Getting the number of bands of the parameter for which
//				// this paramNum represents a component
//				int bands = GRIB1Utilities.checkMultiBandsParam(tableVersion, paramNum);
//				
//				
//				
//				
////				IndexToRecordMapper irMapper;
////				if (bands != -1) {
////					// retrieving the first band param num.
////					final int firstBandParamNum = paramNum - bands;
////					final int nBands = GRIB1Utilities.getBandsNumberFromFirstParamNum(
////							tableVersion, firstBandParamNum);
////					final String key = GRIB1Utilities.buildKey(firstBandParamNum, gr);
////					if (indexToGribSourcesMapping.containsKey(key)) {
////						irMapper = (IndexToRecordMapper) indexToGribSourcesMapping
////								.get(key);
////						irMapper.setBand(bands, uri, recordIndex);
////					} else {
////						irMapper = new IndexToRecordMapper(imageIndex,
////								nBands);
////						irMapper.setBand(bands, uri, recordIndex);
////						indexToGribSourcesMapping.put(key, irMapper);
////					}
////				} else
////					irMapper = new IndexToRecordMapper(imageIndex,
////							recordIndex, uri);
////				if (irMapper.isMappingComplete()) {
////					imageIndex++;
////					imagesList.add(irMapper);
////					numImages++;
////				}
//			
//			
//			}
//			
//		}
//
//	}

}
