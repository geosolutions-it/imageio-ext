package it.geosolutions.imageio.plugins.jhdf.aps;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.metadata.IIOMetadataFormatImpl;

public class APSHDFStreamMetadataFormat extends IIOMetadataFormatImpl {
	private static IIOMetadataFormat instance = null;
	
	public APSHDFStreamMetadataFormat() {
		super(APSHDFStreamMetadata.nativeMetadataFormatName,
				IIOMetadataFormatImpl.CHILD_POLICY_ALL);
		addElement("StandardAPSFileAttributes",
	            APSHDFStreamMetadata.nativeMetadataFormatName, CHILD_POLICY_ALL);
		
		addElement("FileAttributes",
				"StandardAPSFileAttributes", CHILD_POLICY_EMPTY);
        addAttribute("FileAttributes", APSAttributes.STD_FA_FILE, DATATYPE_STRING, true, null);
        addAttribute("FileAttributes", APSAttributes.STD_FA_FILECLASSIFICATION, DATATYPE_STRING, true, null);
        addAttribute("FileAttributes", APSAttributes.STD_FA_FILESTATUS, DATATYPE_STRING, true, null);
        addAttribute("FileAttributes", APSAttributes.STD_FA_FILETITLE, DATATYPE_STRING, true, null);
        addAttribute("FileAttributes", APSAttributes.STD_FA_FILEVERSION, DATATYPE_STRING, true, null);
        addAttribute("FileAttributes", APSAttributes.STD_FA_CREATEAGENCY, DATATYPE_STRING, true, null);
        addAttribute("FileAttributes", APSAttributes.STD_FA_CREATESOFTWARE, DATATYPE_STRING, true, null);
        addAttribute("FileAttributes", APSAttributes.STD_FA_CREATEPLATFORM, DATATYPE_STRING, true, null);
        addAttribute("FileAttributes", APSAttributes.STD_FA_CREATETIME, DATATYPE_STRING, true, null);
        addAttribute("FileAttributes", APSAttributes.STD_FA_CREATEUSER, DATATYPE_STRING, true, null);
                
        addElement("TimeAttributes",
				"StandardAPSFileAttributes", CHILD_POLICY_EMPTY);
        addAttribute("TimeAttributes", APSAttributes.STD_TA_TIMESTART, DATATYPE_STRING, true, null);
        addAttribute("TimeAttributes", APSAttributes.STD_TA_TIMESTARTYEAR, DATATYPE_INTEGER, true, null);
        addAttribute("TimeAttributes", APSAttributes.STD_TA_TIMESTARTDAY, DATATYPE_INTEGER, true, null);
        addAttribute("TimeAttributes", APSAttributes.STD_TA_TIMESTARTTIME, DATATYPE_INTEGER, true, null);
        addAttribute("TimeAttributes", APSAttributes.STD_TA_TIMEEND, DATATYPE_STRING, true, null);
        addAttribute("TimeAttributes", APSAttributes.STD_TA_TIMEENDYEAR, DATATYPE_INTEGER, true, null);
        addAttribute("TimeAttributes", APSAttributes.STD_TA_TIMEENDDAY, DATATYPE_INTEGER, true, null);
        addAttribute("TimeAttributes", APSAttributes.STD_TA_TIMEENDTIME, DATATYPE_INTEGER, true, null);
        addAttribute("TimeAttributes", APSAttributes.STD_TA_TIMEDAYNIGHT, DATATYPE_STRING, true, null);
            
        addElement("SensorAttributes",
            		"StandardAPSFileAttributes", CHILD_POLICY_EMPTY);
        addAttribute("SensorAttributes", APSAttributes.STD_SA_SENSOR, DATATYPE_STRING, true, null);
        addAttribute("SensorAttributes", APSAttributes.STD_SA_SENSORPLATFORM, DATATYPE_INTEGER, true, null);
        addAttribute("SensorAttributes", APSAttributes.STD_SA_SENSORAGENCY, DATATYPE_INTEGER, true, null);
        addAttribute("SensorAttributes", APSAttributes.STD_SA_SENSORTYPE, DATATYPE_INTEGER, true, null);
        addAttribute("SensorAttributes", APSAttributes.STD_SA_SENSORSPECTRUM, DATATYPE_STRING, true, null);
        addAttribute("SensorAttributes", APSAttributes.STD_SA_SENSORNUMBEROFBANDS, DATATYPE_INTEGER, true, null);
        addAttribute("SensorAttributes", APSAttributes.STD_SA_SENSORBANDUNITS, DATATYPE_INTEGER, true, null);
        addAttribute("SensorAttributes", APSAttributes.STD_SA_SENSORBANDS, DATATYPE_INTEGER, true, null);
        addAttribute("SensorAttributes", APSAttributes.STD_SA_SENSORBANDWIDTHS, DATATYPE_STRING, true, null);
        addAttribute("SensorAttributes", APSAttributes.STD_SA_SENSORNOMINALALTITUDEINKM, DATATYPE_STRING, true, null);
        addAttribute("SensorAttributes", APSAttributes.STD_SA_SENSORSCANWIDTHINKM, DATATYPE_STRING, true, null);
        addAttribute("SensorAttributes", APSAttributes.STD_SA_SENSORRESOLUTIONINKM, DATATYPE_STRING, true, null);
        addAttribute("SensorAttributes", APSAttributes.STD_SA_SENSORPLATFORMTYPE, DATATYPE_STRING, true, null);
                
        addElement("ProductFileAttributes",
        	            APSHDFStreamMetadata.nativeMetadataFormatName, CHILD_POLICY_SOME);
        addElement("InputParameterAttributes",
        		"ProductFileAttributes", CHILD_POLICY_EMPTY);
        addAttribute("InputParameterAttributes", APSAttributes.PFA_IPA_INPUTCALIBRATIONFILE, DATATYPE_STRING, false, null);
        addAttribute("InputParameterAttributes", APSAttributes.PFA_IPA_INPUTPARAMETER, DATATYPE_STRING, false, null);
        addAttribute("InputParameterAttributes", APSAttributes.PFA_IPA_INPUTMASKSINT, DATATYPE_STRING, false, null);
        addAttribute("InputParameterAttributes", APSAttributes.PFA_IPA_INPUTMASKS, DATATYPE_STRING, false, null);
        addAttribute("InputParameterAttributes", APSAttributes.PFA_IPA_PRODLIST, DATATYPE_STRING, false, null);
        addAttribute("InputParameterAttributes", APSAttributes.PFA_IPA_PROCESSINGVERSION, DATATYPE_STRING, false, null);
        
        addElement("NavigationAttributes",
        		"ProductFileAttributes", CHILD_POLICY_EMPTY);
        addAttribute("NavigationAttributes", APSAttributes.PFA_NA_NAVTYPE, DATATYPE_STRING, false, null);
        addAttribute("NavigationAttributes", APSAttributes.PFA_NA_MAPPROJECTIONSYSTEM , DATATYPE_STRING, false, null);
        addAttribute("NavigationAttributes", APSAttributes.PFA_NA_MAPPROJECTION, DATATYPE_STRING, false, null);
        addAttribute("NavigationAttributes", APSAttributes.PFA_NA_MAPPEDUPERLEFT, DATATYPE_STRING, false, null);
        addAttribute("NavigationAttributes", APSAttributes.PFA_NA_MAPPEDUPPERRIGHT, DATATYPE_STRING, false, null);
        addAttribute("NavigationAttributes", APSAttributes.PFA_NA_MAPPEDLOWERLEFT, DATATYPE_STRING, false, null);
        addAttribute("NavigationAttributes", APSAttributes.PFA_NA_MAPPEDLOWERRIGHT, DATATYPE_STRING, false, null);
        
        addElement("InputGeographicalCoverageAttributes",
        		"ProductFileAttributes", CHILD_POLICY_EMPTY);
        addAttribute("InputGeographicalCoverageAttributes", APSAttributes.PFA_IGCA_LOCALEUPPERLEFT, DATATYPE_STRING, false, null);
        addAttribute("InputGeographicalCoverageAttributes", APSAttributes.PFA_IGCA_LOCALEUPPERRIGHT, DATATYPE_STRING, false, null);
        addAttribute("InputGeographicalCoverageAttributes", APSAttributes.PFA_IGCA_LOCALELOWERLEFT, DATATYPE_STRING, false, null);
        addAttribute("InputGeographicalCoverageAttributes", APSAttributes.PFA_IGCA_LOCALELOWERRIGHT, DATATYPE_STRING, false, null);
        addAttribute("InputGeographicalCoverageAttributes", APSAttributes.PFA_IGCA_LOCALENWCORNER, DATATYPE_STRING, false, null);
        addAttribute("InputGeographicalCoverageAttributes", APSAttributes.PFA_IGCA_LOCALENECORNER, DATATYPE_STRING, false, null);
        addAttribute("InputGeographicalCoverageAttributes", APSAttributes.PFA_IGCA_LOCALESWCORNER, DATATYPE_STRING, false, null);
        addAttribute("InputGeographicalCoverageAttributes", APSAttributes.PFA_IGCA_LOCALESECORNER, DATATYPE_STRING, false, null);
        
        
        
        addElement("Projection",
        		APSHDFStreamMetadata.nativeMetadataFormatName, CHILD_POLICY_EMPTY);
        addAttribute("Projection", "Name", DATATYPE_STRING, true, null);
        addAttribute("Projection", "FullName", DATATYPE_STRING, true, null);
        addAttribute("Projection", "Code", DATATYPE_STRING, true, null);
        addAttribute("Projection", "Projection", DATATYPE_STRING, true, null);
        addAttribute("Projection", "Zone", DATATYPE_STRING, true, null);
        addAttribute("Projection", "Datum", DATATYPE_INTEGER, true, null);
        addAttribute("Projection", "param0", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "param1", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "param2", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "param3", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "param4", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "param5", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "param6", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "param7", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "param8", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "param9", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "param10", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "param11", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "param12", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "param13", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "param14", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "Width", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "Height", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "Longitude_1", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "Latitude_1", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "Pixel_1", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "Line_1", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "Longitude_2", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "Latitude_2", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "Pixel_2", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "Line_2", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "Delta", DATATYPE_FLOAT, true, null);
        addAttribute("Projection", "Aspect", DATATYPE_FLOAT, true, null);
}

	public boolean canNodeAppear(String elementName,
			ImageTypeSpecifier imageType) {
		// @todo @task TODO
		return true;
	}
}

