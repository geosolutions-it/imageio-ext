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
package it.geosolutions.imageio.plugins.netcdf;

import java.awt.image.SampleModel;

import ucar.nc2.Variable;

public abstract class BaseVariableWrapper {

    private Variable variable;

    private String name;

    private int width;

    private int height;

    private int tileHeight;

	private int tileWidth;

    private int rank;
    
    private int numBands;
    
	private SampleModel sampleModel;

    public void setSampleModel(SampleModel sampleModel) {
		this.sampleModel = sampleModel;
	}

    public BaseVariableWrapper(Variable variable) {
        this.variable = variable;
        rank = variable.getRank();
        width = variable.getDimension(rank - NetCDFUtilities.X_DIMENSION).getLength();
        height = variable.getDimension(rank - NetCDFUtilities.Y_DIMENSION).getLength();
        numBands = rank>2?variable.getDimension(2).getLength():1;
        tileHeight = height;
        tileWidth = width;
        name = variable.getName();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public SampleModel getSampleModel() {
        return sampleModel;
    }

    public int getNumBands() {
		return numBands;
	}

    
    public int getTileHeight() {
        return tileHeight;
    }

    public int getTileWidth() {
        return tileWidth;
    }
    
    public void setTileHeight(int tileHeight) {
		this.tileHeight = tileHeight;
	}

	public void setTileWidth(int tileWidth) {
		this.tileWidth = tileWidth;
	}

    public Variable getVariable() {
        return variable;
    }

    public int getRank() {
        return rank;
    }

    public String getName() {
        return name;
    }
}
