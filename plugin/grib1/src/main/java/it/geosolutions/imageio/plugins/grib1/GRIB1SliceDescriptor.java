package it.geosolutions.imageio.plugins.grib1;

import it.geosolutions.imageio.plugins.SliceDescriptor;

public class GRIB1SliceDescriptor extends SliceDescriptor {

	// a String "parameterTableVersion:parameterNumber"
	final String parameterID;
	
	private String stringRepresentation = null;

	public GRIB1SliceDescriptor(String parameterName, Object zetaLevel,
			String temporalDomain, int overviewLevel, String parameterID) {
		super(parameterName, zetaLevel, temporalDomain, overviewLevel);
		this.parameterID = parameterID;
	}

	public String toString() {
		if (stringRepresentation == null) {
			StringBuffer sb = new StringBuffer(parameterID).append("_").append(
					temporalDomain).append("_").append(zetaLevel.toString())
					.append("_").append(Integer.toString(overviewLevel));
			stringRepresentation = sb.toString();
		}
		return stringRepresentation;
	}

	public String getParameterID() {
		return parameterID;
	}
}
