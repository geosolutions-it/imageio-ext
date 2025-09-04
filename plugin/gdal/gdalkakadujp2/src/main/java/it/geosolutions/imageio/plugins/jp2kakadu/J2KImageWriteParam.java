/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2025, GeoSolutions
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
package it.geosolutions.imageio.plugins.jp2kakadu;

import javax.imageio.ImageWriteParam;
import java.util.Locale;

public class J2KImageWriteParam extends ImageWriteParam {
    public static final String FILTER_97 = "w9x7";
    public static final String FILTER_53 = "w5x3";
    private int numDecompositionLevels = 5;
    private double encodingRate = Double.MAX_VALUE;
    private boolean lossless = true;
    private boolean componentTransformation = true;
    private String filter = "w5x3";
    private int[] codeBlockSize = new int[]{64, 64};
    private String progressionType = "layer";
    private boolean EPH = false;
    private boolean SOP = false;
    private boolean writeCodeStreamOnly = false;

    public J2KImageWriteParam(Locale locale) {
        super(locale);
        this.setDefaults();
    }

    public J2KImageWriteParam() {
        this.setDefaults();
    }

    private void setDefaults() {
        this.canOffsetTiles = true;
        this.canWriteTiles = true;
        this.canOffsetTiles = true;
        this.compressionTypes = new String[]{"JPEG2000"};
        this.canWriteCompressed = true;
        this.tilingMode = 2;
    }

    public void setNumDecompositionLevels(int numDecompositionLevels) {
        if (numDecompositionLevels >= 0 && numDecompositionLevels <= 32) {
            this.numDecompositionLevels = numDecompositionLevels;
        } else {
            throw new IllegalArgumentException("numDecompositionLevels < 0 || numDecompositionLevels > 32");
        }
    }

    public int getNumDecompositionLevels() {
        return this.numDecompositionLevels;
    }

    public void setEncodingRate(double rate) {
        this.encodingRate = rate;
        if (this.encodingRate != Double.MAX_VALUE) {
            this.lossless = false;
            this.filter = "w9x7";
        } else {
            this.lossless = true;
            this.filter = "w5x3";
        }

    }

    public double getEncodingRate() {
        return this.encodingRate;
    }

    public void setLossless(boolean lossless) {
        this.lossless = lossless;
    }

    public boolean getLossless() {
        return this.lossless;
    }

    public void setFilter(String value) {
        this.filter = value;
    }

    public String getFilter() {
        return this.filter;
    }

    public void setComponentTransformation(boolean value) {
        this.componentTransformation = value;
    }

    public boolean getComponentTransformation() {
        return this.componentTransformation;
    }

    public void setCodeBlockSize(int[] value) {
        this.codeBlockSize = value;
    }

    public int[] getCodeBlockSize() {
        return this.codeBlockSize;
    }

    public void setSOP(boolean value) {
        this.SOP = value;
    }

    public boolean getSOP() {
        return this.SOP;
    }

    public void setEPH(boolean value) {
        this.EPH = value;
    }

    public boolean getEPH() {
        return this.EPH;
    }

    public void setProgressionType(String value) {
        this.progressionType = value;
    }

    public String getProgressionType() {
        return this.progressionType;
    }

    public void setWriteCodeStreamOnly(boolean value) {
        this.writeCodeStreamOnly = value;
    }

    public boolean getWriteCodeStreamOnly() {
        return this.writeCodeStreamOnly;
    }
}
