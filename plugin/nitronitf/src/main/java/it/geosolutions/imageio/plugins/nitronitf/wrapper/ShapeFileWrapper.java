/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2012, GeoSolutions
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
package it.geosolutions.imageio.plugins.nitronitf.wrapper;

public class ShapeFileWrapper {

    public ShapeFileWrapper(byte[] shp, byte[] shx, byte[] dbf, int shpLength, int shxLength,
            int dbfLength) {
        super();
        this.shp = shp;
        this.shx = shx;
        this.dbf = dbf;
        this.shpLength = shpLength;
        this.shxLength = shxLength;
        this.dbfLength = dbfLength;
    }

    byte[] shp;

    byte[] shx;

    byte[] dbf;

    int shpLength;

    int shxLength;

    int dbfLength;

    public byte[] getShp() {
        return shp;
    }

    public void setShp(byte[] shp) {
        this.shp = shp;
    }

    public byte[] getShx() {
        return shx;
    }

    public void setShx(byte[] shx) {
        this.shx = shx;
    }

    public byte[] getDbf() {
        return dbf;
    }

    public void setDbf(byte[] dbf) {
        this.dbf = dbf;
    }

    public int getShpLength() {
        return shpLength;
    }

    public void setShpLength(int shpLength) {
        this.shpLength = shpLength;
    }

    public int getShxLength() {
        return shxLength;
    }

    public void setShxLength(int shxLength) {
        this.shxLength = shxLength;
    }

    public int getDbfLength() {
        return dbfLength;
    }

    public void setDbfLength(int dbfLength) {
        this.dbfLength = dbfLength;
    }

}
