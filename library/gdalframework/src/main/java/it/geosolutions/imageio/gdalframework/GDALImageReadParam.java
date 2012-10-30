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
package it.geosolutions.imageio.gdalframework;

import javax.imageio.ImageReadParam;

import org.gdal.gdalconst.gdalconst;

/**
 * An class which works as an adapter of {@link ImageReadParam}
 * to facilitate passing image read parameters specific to 
 * GDAL to GDAL's image reader classes.
 * 
 * @author Billy Newman, BIT Systems.
 * 
 */
public class GDALImageReadParam extends ImageReadParam {

	public enum ResampleAlgorithm {
		NEAREST_NEIGHBOUR(gdalconst.GRA_NearestNeighbour),
		BILINEAR(gdalconst.GRA_Bilinear),
		CUBIC(gdalconst.GRA_Cubic),
		CUBIC_SPLINE(gdalconst.GRA_CubicSpline);
				
		/**
		 * One of gdalconst.GRA_NearestNeighbour, gdalconst.GRA_Bilinear, gdalconst.GRA_Cubic or gdalconst.RA_CubicSpline. 
		 * Controls the sampling method used.
		 * 
		 */
		private int gdalResampleAlgorithm;
		
		private ResampleAlgorithm(int gdalResampleAlgorithm) {
			this.gdalResampleAlgorithm = gdalResampleAlgorithm;
		}
		
		public int getGDALResampleAlgorithm() {
			return gdalResampleAlgorithm;
		}
	}
	
	/**
	 * The coordinate system to convert to. 
	 * If null no change of coordinate system will take place.
	 */
	private String destinationWkt = null;
	
	/**
	 * Controls the sampling method used.
	 */
	private ResampleAlgorithm resampleAlgorithm = ResampleAlgorithm.NEAREST_NEIGHBOUR;
	
	/**
	 * Maximum error measured in input pixels that is allowed in approximating the transformation (0.0 for exact calculations)
	 */
	private double maxError = 0.0;

	/**
	 * Set the coordinate system to convert to.
	 * @param destinationWkt the well known text of the destination image
	 */
	public void setDestinationWkt(String destinationWkt) {
		this.destinationWkt = destinationWkt;
	}
	
	/**
	 * Get the coordinate system to convert to.
	 * @return the well known text of the destination image
	 */
	public String getDestinationWkt() {
		return destinationWkt;
	}
	
	/**
	 * Set the resampling algorithm for gdal to use.
	 * @param resampleAlgorithm the resampling algorithm to set.
	 * 
	 */
	public void setResampleAlgorithm(ResampleAlgorithm resampleAlgorithm) {
		this.resampleAlgorithm = resampleAlgorithm;
	}
	
	/**
	 * Get the resampling algorithm for gdal to use.
	 * @return the resampling algorithm to set.
	 * 
	 */
	public ResampleAlgorithm getResampleAlgorithm() {
		return resampleAlgorithm;
	}
	
	/**
	 * Set the maximum error.
	 * Measured in input pixels that is allowed in approximating the transformation (0.0 for exact calculations).
	 * @param maxError maximum error
	 */
	public void setMaxError(double maxError) {
		this.maxError = maxError;
	}

	/**
	 * Get the maximum error.
	 * Measured in input pixels that is allowed in approximating the transformation (0.0 for exact calculations).
	 * @return maximum error
	 */
	public double getMaxError() {
		return maxError;
	}
}