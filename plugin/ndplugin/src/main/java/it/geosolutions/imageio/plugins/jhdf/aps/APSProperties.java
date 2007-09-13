package it.geosolutions.imageio.plugins.jhdf.aps;

import it.geosolutions.imageio.plugins.jhdf.HDFProducts;


public abstract class APSProperties {
	
	public static class APSProducts extends HDFProducts{
		public APSProducts(){
			super(3);
			HDFProduct sst = new HDFProduct("sst",1);
			setHDFProduct(0, sst);
			
			HDFProduct chl_oc3 = new HDFProduct("chl_oc3",1);
			setHDFProduct(1, chl_oc3);
			
			HDFProduct k_490 = new HDFProduct("K_490",1);
			setHDFProduct(2, k_490);
			
			//TODO: Add more APS supported products
		}
	}

	public final static APSProperties.APSProducts apsProducts = new APSProperties.APSProducts();
	
	/**
	 * -------------------------
	 * Standard: File Attributes
	 * -------------------------
	 */

	/** The name of the product */
	final static String STD_FA_FILE = "file";

	/** Always set to UNCLASSIFIED */
	final static String STD_FA_FILECLASSIFICATION = "fileClassification";

	/** Either EXPERIMENTAL or OPERATIONAL */
	final static String STD_FA_FILESTATUS = "fileStatus";

	/** One of NRL Level-3 / NRL Level-3 Mosaic / NRL Level-4 */
	final static String STD_FA_FILETITLE = "fileTitle";

	/** The version of APS Data format */
	final static String STD_FA_FILEVERSION = "fileVersion";

	/** The agency which created the file */
	final static String STD_FA_CREATEAGENCY = "createAgency";

	/** The version of the software which created the file */
	final static String STD_FA_CREATESOFTWARE = "createSoftware";

	/** The hardware/software platform the file was created on */
	final static String STD_FA_CREATEPLATFORM = "createPlatform";

	/** The date and time when the file was created */
	final static String STD_FA_CREATETIME = "createTime";

	/** The name of the user that created this file */
	final static String STD_FA_CREATEUSER = "createUser";

	final static String[] STD_FA_ATTRIB = new String[] { STD_FA_FILE,
			STD_FA_FILECLASSIFICATION, STD_FA_FILESTATUS, STD_FA_FILETITLE,
			STD_FA_FILEVERSION, STD_FA_CREATEAGENCY, STD_FA_CREATESOFTWARE,
			STD_FA_CREATEPLATFORM, STD_FA_CREATETIME, STD_FA_CREATEUSER };

	/**
	 * ------------------------- 
	 * Standard: Time Attributes
	 * -------------------------
	 */

	/** UTC start time as an ASCII string */
	final static String STD_TA_TIMESTART = "timeStart";

	/** UTC year of data start, e.g. 2007 */
	final static String STD_TA_TIMESTARTYEAR = "timeStartYear";

	/** UTC day-of-year of data start (1-366) */
	final static String STD_TA_TIMESTARTDAY = "timeStartDay";

	/** UTC milliseconds-of-day of data start (1-86400000) */
	final static String STD_TA_TIMESTARTTIME = "timeStartTime";

	/** UTC end time as an ASCII string */
	final static String STD_TA_TIMEEND = "timeEnd";

	/** UTC year of data end, e.g. 2007 */
	final static String STD_TA_TIMEENDYEAR = "timeEndYear";

	/** UTC day-of-year of data end (1-366) */
	final static String STD_TA_TIMEENDDAY = "timeEndDay";

	/** UTC milliseconds-of-day of data end (1-86400000) */
	final static String STD_TA_TIMEENDTIME = "timeEndTime";

	/**
	 * Flag indicating if data collected during day or night. May be one of Day,
	 * Night, Day/Night
	 */
	final static String STD_TA_TIMEDAYNIGHT = "timeDayNight";

	final static String[] STD_TA_ATTRIB = new String[] { STD_TA_TIMESTART,
			STD_TA_TIMESTARTYEAR, STD_TA_TIMESTARTDAY, STD_TA_TIMESTARTTIME,
			STD_TA_TIMEEND, STD_TA_TIMEENDYEAR, STD_TA_TIMEENDDAY,
			STD_TA_TIMEENDTIME, STD_TA_TIMEDAYNIGHT };

	/**
	 * --------------------------- 
	 * Standard: Sensor Attributes
	 * ---------------------------
	 */

	/** AVHRR/3, SeaWiFS, MODIS */
	final static String STD_SA_SENSOR = "sensor";

	/** Platform carrying sensor, like Orbview-2, NOAA-12, MODIS-AQUA */
	final static String STD_SA_SENSORPLATFORM = "sensorPlatform";

	/** Agency/Owner of Sensor */
	final static String STD_SA_SENSORAGENCY = "sensorAgency";

	/** Type of sensor: scanner, pushbroom, whiskbroom */
	final static String STD_SA_SENSORTYPE = "sensorType";

	/** Description of spectrum: visible, near-IR, thermal */
	final static String STD_SA_SENSORSPECTRUM = "sensorSpectrum";

	/** Number of Bands */
	final static String STD_SA_SENSORNUMBEROFBANDS = "sensorNumberOfBands";

	/** Units of wavelengths, like nm */
	final static String STD_SA_SENSORBANDUNITS = "sensorBandUnits";

	/** Center wavelengths */
	final static String STD_SA_SENSORBANDS = "sensorBands";

	/** Nominal width of bands */
	final static String STD_SA_SENSORBANDWIDTHS = "sensorBandWidths";

	/** Nominal Altitude of sensor */
	final static String STD_SA_SENSORNOMINALALTITUDEINKM = "sensorNominalAltitudeInKM";

	/** Distance on earth of Field of View in kilometers */
	final static String STD_SA_SENSORSCANWIDTHINKM = "sensorScanWidthInKM";

	/** Distance on earth of a single pixel in kilometers */
	final static String STD_SA_SENSORRESOLUTIONINKM = "sensorResolutionInKM";

	/** Type of platform */
	final static String STD_SA_SENSORPLATFORMTYPE = "sensorPlatformType";

	final static String[] STD_SA_ATTRIB = new String[] { STD_SA_SENSOR,
			STD_SA_SENSORPLATFORM, STD_SA_SENSORAGENCY, STD_SA_SENSORTYPE,
			STD_SA_SENSORSPECTRUM, STD_SA_SENSORNUMBEROFBANDS,
			STD_SA_SENSORBANDUNITS, STD_SA_SENSORBANDS,
			STD_SA_SENSORBANDWIDTHS, STD_SA_SENSORNOMINALALTITUDEINKM,
			STD_SA_SENSORSCANWIDTHINKM, STD_SA_SENSORRESOLUTIONINKM,
			STD_SA_SENSORPLATFORMTYPE };

	/**
	 * ----------------------------------------- 
	 * Product file: Input Parameters Attributes 
	 * -----------------------------------------
	 */

	/** Name of the calibration file used. SeaWiFS/MOS specific. */
	final static String PFA_IPA_INPUTCALIBRATIONFILE = "inputCalibrationFile";

	/** A string indicating the options used during the processing of the file */
	final static String PFA_IPA_INPUTPARAMETER = "inputParameter";

	/** The mask defined as an integer */
	final static String PFA_IPA_INPUTMASKSINT = "inputMasksInt";

	/**
	 * A comma seperated list of flags that were used as masks during
	 * processing.
	 */
	final static String PFA_IPA_INPUTMASKS = "inputMasks";

	/** A comma seperated list of products stored in this file. */
	final static String PFA_IPA_PRODLIST = "prodList";

	/** Version of processing */
	final static String PFA_IPA_PROCESSINGVERSION = "processingVersion";

	final static String[] PFA_IPA_ATTRIB = new String[] {
			PFA_IPA_INPUTCALIBRATIONFILE, PFA_IPA_INPUTPARAMETER,
			PFA_IPA_INPUTMASKSINT, PFA_IPA_INPUTMASKS, PFA_IPA_PRODLIST,
			PFA_IPA_PROCESSINGVERSION };

	/**
	 * ----------------------------------- 
	 * Product file: Navigation Attributes
	 * -----------------------------------
	 */

	/** Navigation type of data. Always set to 'mapped' */
	final static String PFA_NA_NAVTYPE = "navType";

	/** Map projection system used. Always set to NRL(USGS) */
	final static String PFA_NA_MAPPROJECTIONSYSTEM = "mapProjectionSystem";

	/**
	 * Name of the SDS included in the file that contains the map projection
	 * parameter values.
	 */
	final static String PFA_NA_MAPPROJECTION = "mapProjection";

	/** Latitude and longitude of upper left (1,1) point of each product. */
	final static String PFA_NA_MAPPEDUPERLEFT = "mappedUpperLeft";

	/** Latitude and longitude of upper right (1,n) point of each product. */
	final static String PFA_NA_MAPPEDUPPERRIGHT = "mappedUpperRight";

	/** Latitude and longitude of lower left (m,1) point of each product. */
	final static String PFA_NA_MAPPEDLOWERLEFT = "mappedLowerLeft";

	/** Latitude and longitude of lower right (m,n) point of each product. */
	final static String PFA_NA_MAPPEDLOWERRIGHT = "mappedLowerRight";

	final static String[] PFA_NA_ATTRIB = new String[] { PFA_NA_NAVTYPE,
			PFA_NA_MAPPROJECTIONSYSTEM, PFA_NA_MAPPROJECTION,
			PFA_NA_MAPPEDUPERLEFT, PFA_NA_MAPPEDUPPERRIGHT,
			PFA_NA_MAPPEDLOWERLEFT, PFA_NA_MAPPEDLOWERRIGHT };

	/**
	 * ---------------------------------------------------- 
	 * Product file: Input Geographical Coverage Attributes
	 * ----------------------------------------------------
	 */

	/**
	 * latitude and longitude of upper left (1,1) point of original input data.
	 */
	final static String PFA_IGCA_LOCALEUPPERLEFT = "localeUpperLeft";

	/**
	 * latitude and longitude of upper right (1,n) point of original input data.
	 */
	final static String PFA_IGCA_LOCALEUPPERRIGHT = "localeUpperRight";

	/**
	 * latitude and longitude of lower left (m,1) point of original input data.
	 */
	final static String PFA_IGCA_LOCALELOWERLEFT = "localeLowerLeft";

	/**
	 * latitude and longitude of lower right (m,n) point of original input data.
	 */
	final static String PFA_IGCA_LOCALELOWERRIGHT = "localeLowerRight";

	/** latitude and longitude of NorthWestern point of original input data. */
	final static String PFA_IGCA_LOCALENWCORNER = "localeNWCorner";

	/** latitude and longitude of NorthEastern point of original input data. */
	final static String PFA_IGCA_LOCALENECORNER = "localeNECorner";

	/** latitude and longitude of SouthWestern point of original input data. */
	final static String PFA_IGCA_LOCALESWCORNER = "localeSWCorner";

	/** latitude and longitude of SouthEastern point of original input data. */
	final static String PFA_IGCA_LOCALESECORNER = "localeSECorner";

	/**  */
	final static String[] PFA_IGCA_ATTRIB = new String[] {
			PFA_IGCA_LOCALEUPPERLEFT, PFA_IGCA_LOCALEUPPERRIGHT,
			PFA_IGCA_LOCALELOWERLEFT, PFA_IGCA_LOCALELOWERRIGHT,
			PFA_IGCA_LOCALENWCORNER, PFA_IGCA_LOCALENECORNER,
			PFA_IGCA_LOCALESWCORNER, PFA_IGCA_LOCALESECORNER };

	/**
	 * -------------------------- 
	 * Product Dataset Attributes
	 * --------------------------
	 */

	/**
	 * This string contains the version of the software which created the
	 * product.
	 */
	final static String PDSA_CREATESOFTWARE = "createSoftware";

	/** This string contains the date and time when the product was created */
	final static String PDSA_CREATETIME = "createTime";

	/**
	 * This string contains a triple describing the cpu-machine-os which created
	 * the scientific data set
	 */
	final static String PDSA_CREATEPLATFORM = "createPlatform";

	/** This is a description of the product. */
	final static String PDSA_PRODUCTNAME = "productName";

	/** This is a notation about the algorithm, usually a paper reference. */
	final static String PDSA_PRODUCTALGORITHM = "productAlgorithm";

	/** This is a description of the units of the product. */
	final static String PDSA_PRODUCTUNITS = "productUnits";

	/**
	 * This is a version number of the product used to indicate changes in the
	 * algorithm.
	 */
	final static String PDSA_PRODUCTVERSION = "productVersion";

	/**
	 * This is a type of product. For example, 'chl_oc4v4' and 'chl_oc3m' would
	 * both set this to 'chl'.
	 */
	final static String PDSA_PRODUCTTYPE = "productType";

	/**
	 * This is a space delimetered string of additional units available for this
	 * product. For example, an sst product may set this string to "Kelvin
	 * Fahrenheit"
	 */
	// final static String PDSA_ADDITIONALUNITS = "additionalUnits";
	// The test file in the test-data folder has "otherUnits" as attribute
	// instead of "additionalUnits"
	final static String PDSA_ADDITIONALUNITS = "otherUnits";

	/**
	 * This new SDS attribute will give an indication of the status this
	 * product.
	 */
	final static String PDSA_PRODUCTSTATUS = "productStatus";

	/** This is a suggested range of valid data. */
	final static String PDSA_VALIDRANGE = "validRange";

	/**
	 * This is the geophysical value which will represent invalid data for the
	 * given product.
	 */
	final static String PDSA_INVALID = "invalid";

	/** The type of scaling of the product. Currently, always Linear */
	final static String PDSA_PRODUCTSCALING = "productScaling";

	/** The slope for product scaling. */
	final static String PDSA_SCALINGSLOPE = "scalingSlope";

	/** The intercept for product scaling. */
	final static String PDSA_SCALINGINTERCEPT = "scalingIntercept";

	/**
	 * This is a suggested function to apply to convert the data in the SDS into
	 * an image. A value of 1 indicates linear scaling; a value of 2 indicates
	 * log10 scaling.
	 */
	final static String PDSA_BROWSEFUNC = "browseFunc";

	/**
	 * This is a suggested display range when converting the data in the SDS
	 * into an image. This may or may not be the same as validRange because in
	 * some cases (e.g. rrs_412), the data has been known to fall outside the
	 * range, but we wish to display the invalid data. This attribute is used by
	 * the APS program imgBrowse when creating quick-look browse images of
	 * different products.
	 */
	final static String PDSA_BROWSERANGES = "browseRange";

	/**  */
	final static String[] PDSA_ATTRIB = new String[] { PDSA_CREATESOFTWARE,
			PDSA_CREATETIME, PDSA_CREATEPLATFORM, PDSA_PRODUCTNAME,
			PDSA_PRODUCTALGORITHM, PDSA_PRODUCTUNITS, PDSA_PRODUCTVERSION,
			PDSA_PRODUCTTYPE, PDSA_ADDITIONALUNITS, PDSA_PRODUCTSTATUS,
			PDSA_VALIDRANGE, PDSA_INVALID, PDSA_PRODUCTSCALING,
			PDSA_SCALINGSLOPE, PDSA_SCALINGINTERCEPT, PDSA_BROWSEFUNC,
			PDSA_BROWSERANGES };

}
