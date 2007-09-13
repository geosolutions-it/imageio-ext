package it.geosolutions.imageio.plugins;

import net.sourceforge.jgrib.tables.GribPDSLevel;

//TODO improve this. add Interface returning vertical Domain and temporal domain
public class SliceDescriptor {

	public SliceDescriptor(final String parameterName, final Object zetaLevel, final String temporalDomain,final int overviewLevel){
				
		//TODO: support several types of level (as an instance, some Grib levels are Strings (as an instance, SFC)
		this.zetaLevel = zetaLevel;
		this.temporalDomain = temporalDomain;
		this.overviewLevel = overviewLevel;
		this.parameterName = parameterName;
	}
	
	protected int globalIndex = -1;
	
	//TODO: how zeta need to be handled? (Height, pressure, named levels like "sea surface level" instead of numeric value)
	protected Object zetaLevel;
	
	protected String temporalDomain;
	
	protected int overviewLevel;
	
	protected String parameterName;
	
	//Understanding band components
	protected String bandComponent;

	public int getGlobalIndex() {
		return globalIndex;
	}

	public void setGlobalIndex(int globalIndex) {
		this.globalIndex = globalIndex;
	}

	public String getTemporalDomain() {
		return temporalDomain;
	}

	public int getOverviewLevel() {
		return overviewLevel;
	}

	public String getParameterName() {
		return parameterName;
	}

	//TODO: Change this
	public float getZetaLevel() {
		return ((GribPDSLevel)zetaLevel).getValue1();
	}
	
	
}
