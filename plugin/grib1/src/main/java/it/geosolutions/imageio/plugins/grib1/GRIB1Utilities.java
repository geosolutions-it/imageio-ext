package it.geosolutions.imageio.plugins.grib1;

import java.util.Calendar;

import net.sourceforge.jgrib.GribRecord;
import net.sourceforge.jgrib.GribRecordGDS;
import net.sourceforge.jgrib.GribRecordPDS;
import net.sourceforge.jgrib.tables.GribPDSParamTable;
import net.sourceforge.jgrib.tables.GribPDSParameter;

import org.joda.time.Instant;
import org.joda.time.Interval;

/**
 * Set of utility methods to handle all problematics involved with table
 * versions, parameter numbers, multicomponents
 * 
 * @author Daniele Romagnoli
 */
public class GRIB1Utilities {

	public final static int SINGLE_COMPONENT_PARAM = -1;

	/**
	 * Simple method returning the bands number for the global parameter having
	 * <code>paramNum</code> as first band parameter. As an instance, the WIND
	 * parameter has 2 components (bands), U-components with paramNum = 33 and
	 * V-components with paramNum = 34. To know the number of bands of the WIND
	 * parameter, I need to query this method by specifying 33 as
	 * firstBandParamNum.
	 * 
	 * @param tableVersion
	 * @param firstBandParamNum
	 * @return the number of bands for the parameter having
	 *         <code>firstBandParamNum</code> as paramNum of its first band.
	 */
	public static int getBandsNumberFromFirstParamNum(final int tableVersion,
			final int firstBandParamNum) {

		if (firstBandParamNum >= 0 && firstBandParamNum < 128) {
			// NOTE: paramNum belonging [0,127] always refer to the same
			// parameter, whatever tableVersion value is set.

			switch (firstBandParamNum) {
			case 33:
			case 45:
			case 95:
			case 93:
			case 124:
				return 2;
			}
		}

		switch (tableVersion) {
		case 2:
			switch (firstBandParamNum) {
			// case 33:
			// case 45:
			// case 95:
			// case 93:
			// case 124:
			case 160:
			case 162:
			case 181:
			case 183:
			case 196:
				// TODO add more cases
				return 2;
			default:
				return 1;
			}
		default:
			return 1;
		}
	}

	/**
	 * Given a tableVersion number and a param number, returns -1 if this couple
	 * represents a single banded parameter, N if it represents the (N-1)TH band
	 * of a multibands parameters. <BR>
	 * <BR>
	 * 
	 * @see <a
	 *      href="http://www.nco.ncep.noaa.gov/pmb/docs/on388/table2.html">ON388 -
	 *      TABLE2 - Parameters & Units (PDS Octet 9)</a> for information about
	 *      parameter table and table versions.
	 * 
	 * @param tableVersion
	 *            the table version of this parameter.
	 * @param paramNum
	 *            the paramNum of this parameter.
	 * @return -1 if this couple represents a single banded parameter, N if it
	 *         represents the (N-1)TH band of a multibands parameters.
	 */
	public static int checkMultiBandsParam(final int tableVersion,
			final int paramNum) {

		if (paramNum >= 0 && paramNum < 128) {
			// NOTE: paramNum belonging [0,127] always refer to the same
			// parameter, whatever tableVersion value is set.

			switch (paramNum) {
			case 33:
			case 45:
			case 95:
			case 93:
			case 124:
				// TODO add more cases
				return 0;
			case 34:
			case 46:
			case 96:
			case 94:
				return 1;
			}
		}

		switch (tableVersion) {
		case 2:
			switch (paramNum) {
			// case 33:
			// case 45:
			// case 95:
			// case 93:
			case 124:
			case 160:
			case 162:
			case 181:
			case 183:
			case 196:
				// TODO add more cases
				return 0;
				// case 34:
				// case 46:
				// case 96:
				// case 94:
			case 125:
			case 161:
			case 163:
			case 182:
			case 184:
			case 197:
				return 1;
			default:
				return SINGLE_COMPONENT_PARAM;
			}
		case 128:
			switch (paramNum) {
			// TODO: Add more cases
			case 190:
			case 203:
				return 0;
			case 191:
			case 204:
				return 1;
			default:
				return SINGLE_COMPONENT_PARAM;
			}

		case 129:
			switch (paramNum) {
			// TODO: Add more cases
			case 190:
				return 0;
			case 191:
				return 1;
			default:
				return SINGLE_COMPONENT_PARAM;
			}
		default:
			return SINGLE_COMPONENT_PARAM;
		}
	}

	/**
	 * Returns a <code>String</code> containing main properties
	 * 
	 * @param firstBandParamNum
	 * @param gr
	 * @return
	 */
	public static String buildKey(int firstBandParamNum, GribRecord gr) {
		GribRecordPDS pds = gr.getPDS();
		GribRecordGDS gds = gr.getGDS();
		String pdsHeader = pds.headerToString();
		pdsHeader = pdsHeader.substring(pdsHeader.indexOf("center:"));
		pdsHeader = pdsHeader.substring(0, pdsHeader
				.indexOf(" (dd.mm.yyyy hh:mm)"));
		final String levelString = (new StringBuffer(pds.getLevelName())
				.append("_").append(pds.getLevelDesc()).append("_").append(
						pds.getLevelUnits()).append("_").append(
						Float.toString(pds.getLevelValue())).append("_")
				.append(Float.toString(pds.getLevelValue2()))).toString();
		final StringBuffer sb = new StringBuffer(Integer
				.toString(firstBandParamNum)).append("_").append(pdsHeader)
				.append("_").append(levelString).append("_").append(
						gds.toString());
		return sb.toString();
	}

	/**
	 * Given a paramID String, returns tableVersionNumber and ParameterNumber
	 * 
	 * @param paramID
	 * @return
	 */
	public static int[] getParam(final String paramID) {
		String paramDesc[] = paramID.split(":");
		int[] params = new int[] { Integer.parseInt(paramDesc[2]),
				Integer.parseInt(paramDesc[3]) };
		return params;
	}

	public static int checkMultiBandsParam(String paramIDstring) {
		return checkMultiBandsParam(getParamDescriptor(paramIDstring));
	}

	public static int checkMultiBandsParam(int[] param) {
		return checkMultiBandsParam(param[2], param[3]);
	}

	public static String getMainParameterDescription(final int tableVersion,
			final int paramNum) {
		// TODO: leverage on tables

		if (paramNum >= 0 && paramNum < 128) {
			// NOTE: paramNum belonging [0,127] always refer to the same
			// parameter, whatever tableVersion value is set.

			switch (paramNum) {
			case 34:
			case 33:
				return "Wind Velocity";
			}
		}
		return "";
	}

	public static String getMainParameterName(final int tableVersion,
			final int paramNum) {

		// TODO: leverage on GribPDSParamTable
		// TODO: Refactor this method using tables
		// TODO: Handle all cases.
		// <a href="http://www.nco.ncep.noaa.gov/pmb/docs/on388/table2.html">
		// ON388 - TABLE2 - Parameters & Units (PDS Octet 9)</a> for
		// information about parameter table and table versions.

		if (paramNum >= 0 && paramNum < 128) {
			// NOTE: paramNum belonging [0,127] always refer to the same
			// parameter,
			// whatever tableVersion value is set.

			switch (paramNum) {
			case 34:
			case 33:
				return "WIND";
			}
		}

		switch (tableVersion) {
		case 2:
			switch (paramNum) {
			// case 34:
			// case 33:
			// return "WIND";
			// TODO add more cases
			}
		case 128:
			switch (paramNum) {
			case 190:
			case 203:
				return "PROPERPARAM";
			}

		case 129:
			switch (paramNum) {
			case 190:
				return "PROPERPARAM";
			}
		default:
			return "PROPERPARAM";
		}
	}

	public static String buildTime(GribRecordPDS pds) {
		// TODO: add baseTime management
		Object pdsTime = null;
		int p2 = pds.getP2();
		Calendar forecastTime1 = pds.getGMTForecastTime();
		if (p2 != 0) {
			Calendar forecastTime2 = pds.getGMTForecastTime2();
			if (forecastTime1.compareTo(forecastTime2) == 0) {
				pdsTime = new Instant(forecastTime1);
			} else {
				pdsTime = new Interval(forecastTime1.getTimeInMillis(),
						forecastTime2.getTimeInMillis());
			}
		} else
			pdsTime = new Instant(forecastTime1);
		return pdsTime.toString();
	}

	public static boolean isMultiComponents(String coverageName) {
		if (coverageName.equalsIgnoreCase("WIND"))
			return true;
		// TODO: Add all cases
		return false;
	}

	public static String[] getParamNames(String coverageName,
			GribPDSParamTable table) {
//		 TODO: Provide a full set cases
		String[] paramNames = null;
		if (coverageName.equalsIgnoreCase("WIND")) {
			paramNames = new String[2];
			paramNames[0] = table.getParameter(33).getName();
			paramNames[1] = table.getParameter(34).getName();
		}
		return paramNames;
	}

	public static String getAxisDescription(final int tableVersion,
			final int paramNum) {
		// TODO: Provide a full set case (also using tables).
		return "WIND";
	}

	/**
	 * return a parameterID String given the parameterTable and the parameter.
	 */
	public static String buildParamID(GribPDSParameter parameter,
			GribPDSParamTable table) {
		return new StringBuffer(Integer.toString(table.getCenter_id())).append(
				":").append(Integer.toString(table.getSubcenter_id())).append(
				":").append(Integer.toString(table.getVersionNumber())).append(
				":").append(Integer.toString(parameter.getNumber())).toString();
	}

	public static GribPDSParamTable getParameterTable(
			String parameterTableString) {
		String[] paramTable = parameterTableString.split(":");
		return GribPDSParamTable.getParameterTable(Integer
				.parseInt(paramTable[0]), Integer.parseInt(paramTable[1]),
				Integer.parseInt(paramTable[2]));
	}

	/**
	 * Given a parameterID String, return an integer list
	 */
	public static int[] getParamDescriptor(String parameterID) {
		String paramDesc[] = parameterID.split(":");
		int[] params = new int[] { Integer.parseInt(paramDesc[0]),
				Integer.parseInt(paramDesc[1]), Integer.parseInt(paramDesc[2]),
				Integer.parseInt(paramDesc[3]) };
		return params;
	}

	public static String buildDescription(GribPDSParameter param) {
		final String unitString = param.getUnit();
		final String parameterDescription = getDescription(param);
		return new StringBuffer(parameterDescription).append(" ").append(
				unitString).toString();
	}

	private static String getDescription(GribPDSParameter param) {
		// TODO: handle several cases
		String paramDesc = param.getDescription();
		if (paramDesc.contains("U-Component")) {
			paramDesc = paramDesc
					.substring(0, paramDesc.indexOf("U-Component"));
		} else if (paramDesc.contains("V-Component")) {
			paramDesc = paramDesc
					.substring(0, paramDesc.indexOf("V-Component"));
		}
		return paramDesc;
	}
}
