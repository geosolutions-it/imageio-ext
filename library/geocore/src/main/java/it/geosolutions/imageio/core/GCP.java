/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.core;


/**
 * Class that holds information about a ground control point.
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public class GCP {

	private double easting;
	
	private double northing;
	
	private double elevation;
	
	private int row;
	
	private int column;
	
	private String id;
	
	private String description;

	/**
	 * 
	 */
	public GCP() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Easting of this gcp.
	 * 
	 * @return the easting of this gcp.
	 */
	public double getEasting() {
		return easting;
	}

	/**
	 * @param easting the easting to set
	 */
	public void setEasting(double easting) {
		this.easting = easting;
	}

	/**
	 * Northing of this gcp.
	 * 
	 * @return the northing of this gcp
	 */
	public double getNorthing() {
		return northing;
	}

	/**
	 * @param northing the northing to set
	 */
	public void setNorthing(double northing) {
		this.northing = northing;
	}

	/**
	 * The elevation of this GCP.
	 * 
	 * @return the elevation of thic gcp.
	 */
	public double getElevation() {
		return elevation;
	}

	/**
	 * @param elevation the elevation to set
	 */
	public void setElevation(double elevation) {
		this.elevation = elevation;
	}

	/**
	 * Line (y) location of GCP on raster. 
	 * 
	 * @return the row location of this GCP on the raster.
	 */
	public int getRow() {
		return row;
	}

	/**
	 * @param row the row to set
	 */
	public void setRow(int row) {
		this.row = row;
	}

	/**
	 * Pixel (x) location of GCP on raster. 
	 * 
	 * @return the column location of this gcp on the raster.
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * @param column the column to set
	 */
	public void setColumn(int column) {
		this.column = column;
	}

	/**
	 * Unique identifier, often numeric. 
	 * 
	 * @return the id for this GCP
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Informational message or "". 
	 * 
	 * @return the description of this GCP.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + column;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		long temp;
		temp = Double.doubleToLongBits(easting);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(elevation);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		temp = Double.doubleToLongBits(northing);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + row;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GCP other = (GCP) obj;
		if (column != other.column)
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (Double.doubleToLongBits(easting) != Double
				.doubleToLongBits(other.easting))
			return false;
		if (Double.doubleToLongBits(elevation) != Double
				.doubleToLongBits(other.elevation))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (Double.doubleToLongBits(northing) != Double
				.doubleToLongBits(other.northing))
			return false;
		if (row != other.row)
			return false;
		return true;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		final GCP newGCP= new GCP();
		newGCP.setColumn(this.getColumn());
		newGCP.setDescription(this.getDescription());
		newGCP.setEasting(this.getEasting());
		newGCP.setElevation(this.getElevation());
		newGCP.setId(this.getId());
		newGCP.setNorthing(this.getNorthing());
		newGCP.setRow(this.getRow());
		return newGCP;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	    StringBuilder result = new StringBuilder();
	    String NEW_LINE = System.getProperty("line.separator");
	    result.append(this.getClass().getName()).append(" Object {").append(NEW_LINE);
	    result.append(" id: " ).append(id).append(NEW_LINE);
	    result.append(" description: " ).append(description).append(NEW_LINE);
	    result.append(" easting: " ).append(easting).append(NEW_LINE);
	    result.append(" northing: " ).append(northing).append(NEW_LINE);
	    result.append(" elevation: " ).append(elevation).append(NEW_LINE);
	    result.append(" column: " ).append(column).append(NEW_LINE);
	    result.append(" row: " ).append(row).append(NEW_LINE);
	    result.append("}");
	    return result.toString();

	}
}
