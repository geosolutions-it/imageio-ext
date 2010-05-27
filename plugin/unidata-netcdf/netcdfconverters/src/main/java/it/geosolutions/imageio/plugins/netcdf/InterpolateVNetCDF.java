/*
 *    JImageIO-extension - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    (C) 2007, GeoSolutions
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

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.imageio.stream.FileImageInputStream;
import javax.media.jai.RasterFactory;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;
import javax.vecmath.GMatrix;

import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * TODO Description here ...
 * 
 * @author Alessio Fabiani
 */
public class InterpolateVNetCDF {
    private float[] xminTV = new float[]{Float.NaN, Float.NaN};

    private float[] yminTV = new float[]{Float.NaN, Float.NaN};

    private float[] xmaxTV = new float[]{Float.NaN, Float.NaN};

    private float[] ymaxTV = new float[]{Float.NaN, Float.NaN};

    private float[] xperiodTV = new float[]{Float.NaN, Float.NaN};

    private float[] yperiodTV = new float[]{Float.NaN, Float.NaN};

    private float[] zmaxTV = new float[]{Float.NaN, Float.NaN};

    private float[] zminTV = new float[]{Float.NaN, Float.NaN};

    private float[] zperiodTV = new float[]{Float.NaN, Float.NaN};
    
    private final static int T=0;
    private final static int V=1;

//    private float xminV = Float.NaN;
//
//    private float yminV = Float.NaN;
//
//    private float xmaxV = Float.NaN;
//
//    private float ymaxV = Float.NaN;
//
//    private float xperiodV = Float.NaN;
//
//    private float yperiodV = Float.NaN;
//
//    private float zmaxV = Float.NaN;
//
//    private float zminV = Float.NaN;
//
//    private float zperiodV;

    private final static ArrayList<String> variablesT = new ArrayList<String>(
            12);

    private final static ArrayList<String> variablesV = new ArrayList<String>(4);
    static {
//        variablesT.add("CHL");
//        variablesT.add("NH4");
//        variablesT.add("CELLNH4");
//        variablesT.add("NH4pr");
//        variablesT.add("NO3");
//        variablesT.add("CELLNO3");
//        variablesT.add("NO3pr");
//        variablesT.add("salt");
//        variablesT.add("detritus");
//        variablesT.add("temp");
//        variablesT.add("zgrphy");
//        variablesT.add("zoo");

        NUMVARS_T = variablesT.size();
//        variablesV.add("vclin");
        variablesV.add("vtot");
        NUMVARS_V = variablesV.size();

    }

    final static int NUMVARS_T;

    final static int NUMVARS_V;

    final static String LATITUDE = "latitude";

    final static String LONGITUDE = "longitude";

    final static String DEPTH = "depth";

    final static String UNITS = "units";

    private static final boolean APPLY_MASK = true;

    public final static String fileNameOut = "D:/tmp/netcdf/pe_out_intT.nc";

    public final static String fileNameOutV = "D:/tmp/netcdf/pe_out_intV.nc";

    public final static String fileNameIn = "D:/tmp/netcdf/pe_out.nc";

    public static void main(String[] args) {
        InterpolateVNetCDF o = new InterpolateVNetCDF();
        o.run();
    }

    private void run() {
        try {
            NetcdfFile ncFileIn = NetcdfFile.open(fileNameIn);
            ncFileIn.writeCDL(System.out, true);
            NetcdfFileWriteable ncFileOut = NetcdfFileWriteable
                    .createNew(fileNameOut);
            NetcdfFileWriteable ncFileOutV = NetcdfFileWriteable
                    .createNew(fileNameOutV);

            File iniFile = new File("D:/tmp/netcdf/pi_ini.in");

            // //
            //
            // Determining Initialization Time
            //
            // //
            float julianDay = Float.NaN;
            if (iniFile.exists()) {
                FileImageInputStream fiis = new FileImageInputStream(iniFile);
                boolean goOn = true;

                while (goOn) {
                    try {
                        String line = fiis.readLine();
                        if (line.contains("TSTART")) {
                            final int startIndex = line.indexOf("TSTART");
                            String time = line.substring(0, startIndex).trim();
                            julianDay = Float.parseFloat(time);
                            julianDay += 2440000;
                            break;
                        }
                    } catch (IOException ioe) {
                        goOn = false;
                    }
                }
            }
            GregorianCalendar calendar;
            if (!Float.isNaN(julianDay)) {
                calendar = NetCDFConverterUtilities.fromJulian(julianDay);
            } else
                calendar = new GregorianCalendar();

            // input dimensions
            Dimension timeDim0 = ncFileIn.findDimension("time");
            Dimension tLatDim0 = ncFileIn.findDimension("tlat");
            Dimension tLonDim0 = ncFileIn.findDimension("tlon");
            Dimension vLatDim0 = ncFileIn.findDimension("vlat");
            Dimension vLonDim0 = ncFileIn.findDimension("vlon");

            // input variablesT
            Variable time_0 = ncFileIn.findVariable("time");
            Array time_0_Data = time_0.read();
            Index time_0_Index = time_0_Data.getIndex();

            Variable tGrid3 = ncFileIn.findVariable("tgrid3");
            Variable vGrid3 = ncFileIn.findVariable("vgrid3");

            // //
            //
            // Handling tracers-related objects
            //
            // //
            final int nTLat = tGrid3.getDimension(0).getLength();
            final int nTLon = tGrid3.getDimension(1).getLength();
            final int nTLevels = tGrid3.getDimension(2).getLength();
            final String tUnits = ((Attribute) tGrid3.findAttribute(UNITS))
                    .getStringValue();

            final String tUnit[] = tUnits.split(",");

            // //
            //
            // Getting tracers grid lat/lon/depth
            //
            // //
            Array tLatOriginalData = tGrid3.read(
                    "0:" + (nTLat - 1) + ":1, 0:0:1, 0:0:1, 1:1:1").reduce();
            Index tLatOriginalIndex = tLatOriginalData.getIndex();

            Array tLonOriginalData = tGrid3.read(
                    "0:0:1, 0:" + (nTLon - 1) + ":1, 0:0:1, 0:0:1").reduce();
            Index tLonOriginalIndex = tLonOriginalData.getIndex();

            Array tLevOriginalData = tGrid3.read(
                    "0:" + (nTLat - 1) + ":1, 0:" + (nTLon - 1) + ":1, 0:"
                            + (nTLevels - 1) + ":1, 2:2:1").reduce(); // Depth
            Index tLevOriginalIndex = tLevOriginalData.getIndex();

            // //
            //
            // Handling velocity-related objects
            //
            // //
            final int nVLat = vGrid3.getDimension(0).getLength();
            final int nVLon = vGrid3.getDimension(1).getLength();
            final int nVLevels = vGrid3.getDimension(2).getLength();
            final String vUnits = ((Attribute) vGrid3.findAttribute(UNITS))
                    .getStringValue();
            final String vUnit[] = tUnits.split(",");

            // //
            //
            // Getting velocity grid lat/lon/depth
            //
            // //
            Array vLatOriginalData = vGrid3.read(
                    "0:" + (nVLat - 1) + ":1, 0:0:1, 0:0:1, 1:1:1").reduce();
            Index vLatOriginalIndex = vLatOriginalData.getIndex();

            Array vLonOriginalData = vGrid3.read(
                    "0:0:1, 0:" + (nVLon - 1) + ":1, 0:0:1, 0:0:1").reduce();
            Index vLonOriginalIndex = vLonOriginalData.getIndex();

            Array vLevOriginalData = vGrid3.read(
                    "0:" + (nVLat - 1) + ":1, 0:" + (nVLon - 1) + ":1, 0:"
                            + (nVLevels - 1) + ":1, 2:2:1").reduce(); // Depth
            Index vLevOriginalIndex = vLevOriginalData.getIndex();

            // //
            //
            // Adding dimensions to the first dataset
            //
            // //
            Dimension timeDimT = ncFileOut.addDimension("time", timeDim0
                    .getLength());
            Dimension latDimT = ncFileOut.addDimension(NetCDFUtilities.LAT,
                    tLatDim0.getLength());
            Dimension lonDimT = ncFileOut.addDimension(NetCDFUtilities.LON,
                    tLonDim0.getLength());
            Dimension depthDimT = ncFileOut.addDimension(NetCDFUtilities.DEPTH,
                    nTLevels);

            // //
            //
            // Adding dimensions to the second dataset
            //
            // //
            Dimension timeDimV = ncFileOutV.addDimension("time", timeDim0
                    .getLength());
            Dimension latDimV = ncFileOutV.addDimension(NetCDFUtilities.LAT,
                    vLatDim0.getLength());
            Dimension lonDimV = ncFileOutV.addDimension(NetCDFUtilities.LON,
                    vLonDim0.getLength());
            Dimension depthDimV = ncFileOutV.addDimension(
                    NetCDFUtilities.DEPTH, nVLevels);

            // //
            //
            // Computing TGrid extremes
            //
            // //
            computeMatrixExtremes(tLatOriginalData, tLonOriginalData,
                    tLevOriginalData, tLonDim0.getLength(), tLatDim0
                            .getLength(), depthDimT.getLength(),
                    tLatOriginalIndex, tLonOriginalIndex, tLevOriginalIndex, T);

            // //
            //
            // Computing VGrid extremes
            //
            // //
            computeMatrixExtremes(vLatOriginalData, vLonOriginalData,
                    vLevOriginalData, vLonDim0.getLength(), vLatDim0
                            .getLength(), depthDimV.getLength(),
                    vLatOriginalIndex, vLonOriginalIndex, vLevOriginalIndex, V);

            // //
            //
            // Setting global attributes
            //
            // //
            List globalAttributes = ncFileIn.getGlobalAttributes();
            copyGlobalAttributes(ncFileOut, globalAttributes);
            copyGlobalAttributes(ncFileOutV, globalAttributes);

            // //
            //
            // Adding coord vars to the output files
            //
            // //

            // time variable
            ncFileOut.addVariable("time", DataType.FLOAT,
                    new Dimension[] { timeDimT });

            ncFileOutV.addVariable("time", DataType.FLOAT,
                    new Dimension[] { timeDimV });

            setTimeVariableAttributes(time_0, ncFileOut, calendar);
            setTimeVariableAttributes(time_0, ncFileOutV, calendar);

            // ////////////////////////////////////////////////////////////////
            //
            // Adding coord vars to the first dataset
            //
            // ////////////////////////////////////////////////////////////////
            // lat Variable
            ArrayFloat tLatData = new ArrayFloat(new int[] { tLatDim0
                    .getLength() });
            Index tLatIndex = tLatData.getIndex();
            ncFileOut.addVariable(NetCDFUtilities.LAT, DataType.FLOAT,
                    new Dimension[] { latDimT });
            ncFileOut.addVariableAttribute(NetCDFUtilities.LAT, "long_name",
                    LATITUDE);
            ncFileOut.addVariableAttribute(NetCDFUtilities.LAT, UNITS, tUnit[1]
                    .trim());
            for (int yPos = 0; yPos < tLatDim0.getLength(); yPos++) {
                tLatData.setFloat(tLatIndex.set(yPos), new Float(this.ymaxTV[T]
                        - (new Float(yPos).floatValue() * this.yperiodTV[T]))
                        .floatValue());
            }

            // lon Variable
            ArrayFloat tLonData = new ArrayFloat(new int[] { tLonDim0
                    .getLength() });
            Index tLonIndex = tLonData.getIndex();
            ncFileOut.addVariable(NetCDFUtilities.LON, DataType.FLOAT,
                    new Dimension[] { lonDimT });
            ncFileOut.addVariableAttribute(NetCDFUtilities.LON, "long_name",
                    LONGITUDE);
            ncFileOut.addVariableAttribute(NetCDFUtilities.LON, UNITS, tUnit[0]
                    .trim());
            for (int xPos = 0; xPos < tLonDim0.getLength(); xPos++) {
                tLonData.setFloat(tLonIndex.set(xPos), new Float(this.xminTV[T]
                        + (new Float(xPos).floatValue() * this.xperiodTV[T]))
                        .floatValue());
            }

            // depth level Variable
            ArrayFloat tDepthData = new ArrayFloat(new int[] { depthDimT
                    .getLength() });
            Index tDepthIndex = tDepthData.getIndex();
            ncFileOut.addVariable(NetCDFUtilities.DEPTH, DataType.FLOAT,
                    new Dimension[] { depthDimT });
            ncFileOut.addVariableAttribute(DEPTH, "long_name", DEPTH);
            ncFileOut.addVariableAttribute(DEPTH, UNITS, tUnit[2].trim());
            for (int zPos = 0; zPos < depthDimT.getLength(); zPos++) {
                tDepthData.setFloat(tDepthIndex.set(zPos), new Float(this.zmaxTV[T]
                        - (new Float(zPos).floatValue() * this.zperiodTV[T]))
                        .floatValue());
            }

            // ////////////////////////////////////////////////////////////////
            //
            // Adding coord vars to the second dataset
            //
            // ////////////////////////////////////////////////////////////////
            // lat Variable
            ArrayFloat vLatData = new ArrayFloat(new int[] { tLatDim0
                    .getLength() });
            Index vLatIndex = vLatData.getIndex();
            ncFileOutV.addVariable(NetCDFUtilities.LAT, DataType.FLOAT,
                    new Dimension[] { latDimV });
            ncFileOutV.addVariableAttribute(NetCDFUtilities.LAT, "long_name",
                    LATITUDE);
            ncFileOutV.addVariableAttribute(NetCDFUtilities.LAT, UNITS,
                    vUnit[1].trim());
            for (int yPos = 0; yPos < vLatDim0.getLength(); yPos++) {
                vLatData.setFloat(vLatIndex.set(yPos), new Float(this.ymaxTV[V]
                        - (new Float(yPos).floatValue() * this.yperiodTV[V]))
                        .floatValue());
            }

            // lon Variable
            ArrayFloat vLonData = new ArrayFloat(new int[] { tLonDim0
                    .getLength() });
            Index vLonIndex = vLonData.getIndex();
            ncFileOutV.addVariable(NetCDFUtilities.LON, DataType.FLOAT,
                    new Dimension[] { lonDimV });
            ncFileOutV.addVariableAttribute(NetCDFUtilities.LON, "long_name",
                    LONGITUDE);
            ncFileOutV.addVariableAttribute(NetCDFUtilities.LON, UNITS,
                    vUnit[0].trim());
            for (int xPos = 0; xPos < vLonDim0.getLength(); xPos++) {
                vLonData.setFloat(vLonIndex.set(xPos), new Float(this.xminTV[V]
                        + (new Float(xPos).floatValue() * this.xperiodTV[V]))
                        .floatValue());
            }

            // depth level Variable
            ArrayFloat vDepthData = new ArrayFloat(new int[] { depthDimT
                    .getLength() });
            Index vDepthIndex = vDepthData.getIndex();
            ncFileOutV.addVariable(NetCDFUtilities.DEPTH, DataType.FLOAT,
                    new Dimension[] { depthDimV });
            ncFileOutV.addVariableAttribute(DEPTH, "long_name", DEPTH);
            ncFileOutV.addVariableAttribute(DEPTH, UNITS, vUnit[2].trim());
            for (int zPos = 0; zPos < depthDimV.getLength(); zPos++) {
                vDepthData.setFloat(vDepthIndex.set(zPos), new Float(this.zmaxTV[V]
                        - (new Float(zPos).floatValue() * this.zperiodTV[V]))
                        .floatValue());
            }

            // Tracers related Variables
            for (int i = 0; i < NUMVARS_T; i++) {
                String varName = variablesT.get(i);
                Variable var = ncFileIn.findVariable(varName);
                ncFileOut
                        .addVariable(varName, var.getDataType(),
                                new Dimension[] { timeDimT, depthDimT, latDimT,
                                        lonDimT });
                setVariableAttributes(var, ncFileOut,
                        new String[] { "positions" });
            }

            // Velocity related Variables
            for (int i = 0; i < NUMVARS_V; i++) {
                String varName = variablesV.get(i);
                Variable var = ncFileIn.findVariable(varName);
                ncFileOutV
                        .addVariable(varName + "0", var.getDataType(),
                                new Dimension[] { timeDimV, depthDimV, latDimV,
                                        lonDimV });
                ncFileOutV
                        .addVariable(varName + "1", var.getDataType(),
                                new Dimension[] { timeDimV, depthDimV, latDimV,
                                        lonDimV });

                setVariableAttributes(var, ncFileOutV,
                        new String[] { "positions" },0);
                setVariableAttributes(var, ncFileOutV,
                        new String[] { "positions" },1);
            }

            // writing bin data ...

            ncFileOut.create();
            ncFileOutV.create();

            ArrayFloat timeData = new ArrayFloat(new int[] { timeDimT
                    .getLength() });
            Index timeIndex = timeData.getIndex();
            for (int t = 0; t < timeDimT.getLength(); t++) {
                float seconds = time_0_Data.getFloat(time_0_Index.set(t));
                timeData.setFloat(timeIndex.set(t), seconds);
            }

            // //
            //
            // Writing coordVars for the first dataset
            //
            // //
            ncFileOut.write("time", timeData);
            Variable timeVar = ncFileOut.findVariable("time");
            timeDimT.addCoordinateVariable(timeVar);
            Variable depthVarT = ncFileOut.findVariable("depth");
            depthDimT.addCoordinateVariable(depthVarT);
            ncFileOut.write(NetCDFUtilities.DEPTH, vDepthData);
            ncFileOut.write(NetCDFUtilities.LAT, tLatData);
            ncFileOut.write(NetCDFUtilities.LON, tLonData);

            // //
            //
            // Writing coordVars for the second dataset
            //
            // //
            ncFileOutV.write("time", timeData);
            Variable timeVarV = ncFileOutV.findVariable("time");
            timeDimV.addCoordinateVariable(timeVarV);
            Variable depthVarV = ncFileOut.findVariable("depth");
            depthDimV.addCoordinateVariable(depthVarV);
            ncFileOutV.write(NetCDFUtilities.DEPTH, vDepthData);
            ncFileOutV.write(NetCDFUtilities.LAT, vLatData);
            ncFileOutV.write(NetCDFUtilities.LON, vLonData);

            // TODO: AutoApply MASK?
            ArrayFloat maskMatrixT = new ArrayFloat.D2(latDimT.getLength(),
                    lonDimT.getLength());
            Index maskImaT = maskMatrixT.getIndex();
            if (APPLY_MASK) {
                Variable mask = ncFileIn.findVariable("landt");
                Array maskData = mask.read();
                Index maskIndex = maskData.getIndex();
                ArrayFloat tempData = new ArrayFloat(new int[] {
                        tLatDim0.getLength(), tLonDim0.getLength() });
                Index tempIndex = tempData.getIndex();
                for (int yPos = 0; yPos < tLatDim0.getLength(); yPos++) {
                    for (int xPos = 0; xPos < tLonDim0.getLength(); xPos++) {
                        tempData.setFloat(tempIndex.set(yPos, xPos), maskData
                                .getFloat(maskIndex.set(yPos, xPos)));
                    }
                }

                WritableRaster outData = this.Resampler(tLatOriginalData,
                        tLonOriginalData, tLonDim0.getLength(), tLatDim0
                                .getLength(), 2, tempData, -1, T);
                for (int j = 0; j < tLatDim0.getLength(); j++) {
                    for (int k = 0; k < tLonDim0.getLength(); k++) {
                        float sample = outData.getSampleFloat(k, j, 0);
                        maskMatrixT.setFloat(maskImaT.set(j, k), sample);
                    }
                }
            }

            ArrayFloat maskMatrixV = new ArrayFloat.D2(latDimT.getLength(),
                    lonDimT.getLength());
            Index maskImaV = maskMatrixV.getIndex();
            if (APPLY_MASK) {
                Variable mask = ncFileIn.findVariable("landv");
                Array maskData = mask.read();
                Index maskIndex = maskData.getIndex();
                ArrayFloat tempData = new ArrayFloat(new int[] {
                        vLatDim0.getLength(), vLonDim0.getLength() });
                Index tempIndex = tempData.getIndex();
                for (int yPos = 0; yPos < vLatDim0.getLength(); yPos++) {
                    for (int xPos = 0; xPos < vLonDim0.getLength(); xPos++) {
                        tempData.setFloat(tempIndex.set(yPos, xPos), maskData
                                .getFloat(maskIndex.set(yPos, xPos)));
                    }
                }

                WritableRaster outData = this.Resampler(vLatOriginalData,
                        vLonOriginalData, vLonDim0.getLength(), vLatDim0
                                .getLength(), 2, tempData, -1, V);
                for (int j = 0; j < vLatDim0.getLength(); j++) {
                    for (int k = 0; k < vLonDim0.getLength(); k++) {
                        float sample = outData.getSampleFloat(k, j, 0);
                        maskMatrixV.setFloat(maskImaV.set(j, k), sample);
                    }
                }
            }

            // //
            //
            // Writing Tracers related Variables
            //
            // //
            System.out.print("T Process complete...0.0%");
            for (int i = 0; i < NUMVARS_T; i++) {
                String varName = variablesT.get(i);
                Variable var = ncFileIn.findVariable(varName);

                Array originalVarData = var.read();
                Index varIndex = originalVarData.getIndex();
                Attribute fv = var.findAttribute("_FillValue");
                float fillValue = Float.NaN;
                if (fv != null) {
                    fillValue = (fv.getNumericValue()).floatValue();
                }

                ArrayFloat tempData = new ArrayFloat(new int[] {
                        tLatDim0.getLength(), tLonDim0.getLength() });
                Index tempIndex = tempData.getIndex();

                ArrayFloat tMatrix = new ArrayFloat.D4(timeDimT.getLength(),
                        depthDimT.getLength(), latDimT.getLength(), lonDimT
                                .getLength());
                Index tIma = tMatrix.getIndex();

                for (int tPos = 0; tPos < timeDim0.getLength(); tPos++) {
                    for (int levelPos = 0; levelPos < depthDimT.getLength(); levelPos++) {
                        for (int yPos = 0; yPos < tLatDim0.getLength(); yPos++) {
                            for (int xPos = 0; xPos < tLonDim0.getLength(); xPos++) {
                                // Vertical Interpolation (nearest neighbor)

                                final double targetZ = new Double(
                                        this.zmaxTV[T]
                                                - (new Double(levelPos)
                                                        .doubleValue() * this.zperiodTV[T]))
                                        .doubleValue();

                                int targetOutlev = 0;
                                double distance = Double.POSITIVE_INFINITY;
                                for (int ol = 0; ol < depthDimT.getLength(); ol++) {
                                    final double designatedZ = tLevOriginalData
                                            .getDouble(tLevOriginalIndex.set(
                                                    yPos, xPos, ol));
                                    if (Math.abs(designatedZ - targetZ) < distance) {
                                        distance = Math.abs(designatedZ
                                                - targetZ);
                                        targetOutlev = ol;
                                    }
                                }

                                tempData.setFloat(tempIndex.set(yPos, xPos),
                                        originalVarData
                                                .getFloat(varIndex.set(tPos,
                                                        yPos, xPos,
                                                        targetOutlev)));
                            }
                        }

                        WritableRaster outData = this.Resampler(
                                tLatOriginalData, tLonOriginalData, tLonDim0
                                        .getLength(), tLatDim0.getLength(), 2,
                                tempData, fillValue, T);
                        for (int j = 0; j < tLatDim0.getLength(); j++) {
                            for (int k = 0; k < tLonDim0.getLength(); k++) {
                                float sample = outData.getSampleFloat(k, j, 0);
                                if (APPLY_MASK) {
                                    float maskValue = maskMatrixV
                                            .getFloat(maskImaV.set(j, k));
                                    if (maskValue == 0)
                                        sample = fillValue;
                                }
                                tMatrix.setFloat(
                                        tIma.set(tPos, levelPos, j, k), sample);
                            }
                        }

                    }
                }
                ncFileOut.write(varName, tMatrix);
                System.out.print("..."
                        + new Double(
                                (new Double(i + 1).doubleValue() / new Double(
                                        NUMVARS_T).doubleValue()) * 100.0)
                                .floatValue() + "%");
            }

            // //
            //
            // Writing Velocity related Variables
            //
            // //
            System.out.print("V Process complete...0.0%");
            for (int i = 0; i < NUMVARS_V; i++) {
                String varName = variablesV.get(i);
                Variable var = ncFileIn.findVariable(varName);

                Array originalVarData = var.read();
                Index varIndex = originalVarData.getIndex();
                Attribute fv = var.findAttribute("_FillValue");
                float fillValue = Float.NaN;
                if (fv != null) {
                    fillValue = (fv.getNumericValue()).floatValue();
                }

                ArrayFloat tempData0 = new ArrayFloat(new int[] {
                        vLatDim0.getLength(), vLonDim0.getLength() });
                Index tempIndex0 = tempData0.getIndex();
                
                ArrayFloat tempData1 = new ArrayFloat(new int[] {
                        vLatDim0.getLength(), vLonDim0.getLength() });
                Index tempIndex1 = tempData1.getIndex();

                ArrayFloat vMatrix0 = new ArrayFloat.D4(timeDimV.getLength(),
                        depthDimV.getLength(), latDimV.getLength(), lonDimV
                                .getLength());
                Index vIma0 = vMatrix0.getIndex();
                
                ArrayFloat vMatrix1 = new ArrayFloat.D4(timeDimV.getLength(),
                        depthDimV.getLength(), latDimV.getLength(), lonDimV
                                .getLength());
                Index vIma1 = vMatrix1.getIndex();

                for (int tPos = 0; tPos < timeDim0.getLength(); tPos++) {
                    for (int levelPos = 0; levelPos < depthDimV.getLength(); levelPos++) {
                        for (int yPos = 0; yPos < vLatDim0.getLength(); yPos++) {
                            for (int xPos = 0; xPos < vLonDim0.getLength(); xPos++) {
                                // Vertical Interpolation (nearest neighbor)

                                final double targetZ = new Double(
                                        this.zmaxTV[V]
                                                - (new Double(levelPos)
                                                        .doubleValue() * this.zperiodTV[V]))
                                        .doubleValue();

                                int targetOutlev = 0;
                                double distance = Double.POSITIVE_INFINITY;
                                for (int ol = 0; ol < depthDimV.getLength(); ol++) {
                                    final double designatedZ = vLevOriginalData
                                            .getDouble(vLevOriginalIndex.set(
                                                    yPos, xPos, ol));
                                    if (Math.abs(designatedZ - targetZ) < distance) {
                                        distance = Math.abs(designatedZ
                                                - targetZ);
                                        targetOutlev = ol;
                                    }
                                }

                                tempData0.setFloat(tempIndex0.set(yPos, xPos),
                                        originalVarData
                                                .getFloat(varIndex.set(tPos,
                                                        yPos, xPos,
                                                        targetOutlev,0)));
                                
                                tempData1.setFloat(tempIndex1.set(yPos, xPos),
                                        originalVarData
                                                .getFloat(varIndex.set(tPos,
                                                        yPos, xPos,
                                                        targetOutlev,1)));
                            }
                        }

                        WritableRaster outData0 = this.Resampler(
                                vLatOriginalData, vLonOriginalData, vLonDim0
                                        .getLength(), vLatDim0.getLength(), 2,
                                tempData0, fillValue, V);
                        for (int j = 0; j < vLatDim0.getLength(); j++) {
                            for (int k = 0; k < vLonDim0.getLength(); k++) {
                                float sample = outData0.getSampleFloat(k, j, 0);
                                if (APPLY_MASK) {
                                    float maskValue = maskMatrixV
                                            .getFloat(maskImaV.set(j, k));
                                    if (maskValue == 0)
                                        sample = fillValue;
                                }
                                vMatrix0.setFloat(
                                        vIma0.set(tPos, levelPos, j, k), sample);
                            }
                        }
                        
                        WritableRaster outData1 = this.Resampler(
                                vLatOriginalData, vLonOriginalData, vLonDim0
                                        .getLength(), vLatDim0.getLength(), 2,
                                tempData1, fillValue, V);
                        for (int j = 0; j < vLatDim0.getLength(); j++) {
                            for (int k = 0; k < vLonDim0.getLength(); k++) {
                                float sample = outData1.getSampleFloat(k, j, 0);
                                if (APPLY_MASK) {
                                    float maskValue = maskMatrixV
                                            .getFloat(maskImaV.set(j, k));
                                    if (maskValue == 0)
                                        sample = fillValue;
                                }
                                vMatrix1.setFloat(
                                        vIma1.set(tPos, levelPos, j, k), sample);
                            }
                        }

                    }
                }
                ncFileOutV.write(varName+"0", vMatrix0);
                ncFileOutV.write(varName+"1", vMatrix1);
                System.out.print("..."
                        + new Double(
                                (new Double(i + 1).doubleValue() / new Double(
                                        NUMVARS_V).doubleValue()) * 100.0)
                                .floatValue() + "%");
            }

            ncFileOut.close();
            ncFileOutV.close();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private WritableRaster Resampler(final Array latData, final Array lonData,
            final int imageWidth, final int imageHeight, final int polyDegree,
            final Array data, final float fillValue, final int tv) {
        final Index latIndex = latData.getIndex();
        final Index lonIndex = lonData.getIndex();

        final int numCoeffs = (polyDegree + 1) * (polyDegree + 2) / 2;

        final int XOFFSET = 0;
        final int YOFFSET = 1;

        final int stepX = 2;
        final int stepY = 2;

        int numNeededPoints = 0;
        for (int xi = 0; xi < imageWidth; xi += stepX) {
            for (int yi = 0; yi < imageHeight; yi += stepY) {
                numNeededPoints++;
            }
        }
            computeMatrixExtremes(latData, lonData, imageWidth, imageHeight,
                    latIndex, lonIndex,tv);

        float[] destCoords = new float[2 * numNeededPoints];
        float[] srcCoords = new float[2 * numNeededPoints];

        /*
         * Copy source and destination coordinates into float arrays. The
         * destination coordinates are scaled in order to gets values similar to
         * source coordinates (values will be identical if all "real world"
         * coordinates are grid indices multiplied by a constant).
         */
        final float xmin = xminTV[tv];
        final float xperiod = xperiodTV[tv];
        final float ymax = ymaxTV[tv];
        final float yperiod = yperiodTV[tv];
        
        int offset = 0;
        for (int yi = 0; yi < imageHeight; yi += stepY) {
            for (int xi = 0; xi < imageWidth; xi += stepX) {
                srcCoords[offset] = xi;
                srcCoords[offset + 1] = yi;

                destCoords[offset] = (float) ((lonData.getFloat(lonIndex
                        .set(xi)) - xmin) / xperiod);
                destCoords[offset + 1] = (float) ((ymax - latData
                        .getFloat(latIndex.set(yi))) / yperiod);
                // destCoords[offset + 1] = ((latData.getFloat(latIndex.set(yi))
                // - this.ymin) / this.periodY);
                offset += 2;
            }
        }

        GMatrix A = new GMatrix(numNeededPoints, numCoeffs);

        for (int coord = 0; coord < numNeededPoints; coord++) {
            int var = 0;
            for (int i = 0; i <= polyDegree; i++) {
                for (int j = 0; j <= i; j++) {
                    double value = Math.pow(destCoords[2 * coord + XOFFSET],
                            (double) (i - j))
                            * Math.pow(destCoords[2 * coord + YOFFSET],
                                    (double) j);
                    A.setElement(coord, var++, value);
                }
            }
        }

        GMatrix AtAi = new GMatrix(numCoeffs, numCoeffs);
        GMatrix Ap = new GMatrix(numCoeffs, numNeededPoints);

        AtAi.mulTransposeLeft(A, A);
        AtAi.invert();
        Ap.mulTransposeRight(AtAi, A);

        GMatrix xVector = new GMatrix(numNeededPoints, 1);
        GMatrix yVector = new GMatrix(numNeededPoints, 1);

        for (int idx = 0; idx < numNeededPoints; idx++) {
            xVector.setElement(idx, 0, srcCoords[2 * idx + XOFFSET]);
            yVector.setElement(idx, 0, srcCoords[2 * idx + YOFFSET]);
        }

        GMatrix xCoeffsG = new GMatrix(numCoeffs, 1);
        GMatrix yCoeffsG = new GMatrix(numCoeffs, 1);

        xCoeffsG.mul(Ap, xVector);
        yCoeffsG.mul(Ap, yVector);

        float[] xCoeffs = new float[numCoeffs];
        float[] yCoeffs = new float[numCoeffs];

        for (int ii = 0; ii < numCoeffs; ii++) {
            xCoeffs[ii] = new Double(xCoeffsG.getElement(ii, 0)).floatValue();
            yCoeffs[ii] = new Double(yCoeffsG.getElement(ii, 0)).floatValue();
        }

        WritableRaster outDataCube;
        WritableRandomIter iteratorDataCube;
        SampleModel outSampleModel = RasterFactory.createBandedSampleModel(
                DataBuffer.TYPE_FLOAT, // data type
                imageWidth, // width
                imageHeight, // height
                1); // num bands
        outDataCube = Raster.createWritableRaster(outSampleModel, null);
        iteratorDataCube = RandomIterFactory.createWritable(outDataCube, null);
        // Transfering data in the WritableRaster structure
        Index indexInputVar = data.getIndex();

        for (int jj = 0; jj < outDataCube.getNumBands(); jj++) {
            for (int kk = 0; kk < outDataCube.getWidth(); kk++) {
                for (int ll = 0; ll < outDataCube.getHeight(); ll++) {
                    iteratorDataCube.setSample(kk, ll, jj, data
                            .getFloat(indexInputVar.set(ll, kk)));
                }
            }
        }

        WritableRaster target = RasterFactory.createWritableRaster(
                outSampleModel, null);

        for (int bi = 0; bi < outDataCube.getNumBands(); bi++) {
            for (int yi = 0; yi < imageHeight; yi++) {
                for (int xi = 0; xi < imageWidth; xi++) {
                    float[] dstCoords = new float[2];
                    GMatrix regressionVec = new GMatrix(numCoeffs, 1);
                    int var = 0;
                    for (int i = 0; i <= polyDegree; i++) {
                        for (int j = 0; j <= i; j++) {
                            double value = Math.pow(xi, (double) (i - j))
                                    * Math.pow(yi, (double) j);
                            regressionVec.setElement(var++, 0, value);
                        }
                    }

                    GMatrix xG = new GMatrix(1, 1);
                    GMatrix yG = new GMatrix(1, 1);

                    xG.mulTransposeLeft(regressionVec, xCoeffsG);
                    yG.mulTransposeLeft(regressionVec, yCoeffsG);

                    int X = (int) Math.round(xG.getElement(0, 0));
                    int Y = (int) Math.round(yG.getElement(0, 0));

                    if (X >= 0 && Y >= 0 && X < imageWidth && Y < imageHeight) {
                        target.setSample(xi, yi, bi, outDataCube
                                .getSampleFloat(X, Y, bi));
                    } else {
                        // TODO: Change with fillvalue
                        // target.setSample(xi, yi, bi, Float.NaN);
                        target.setSample(xi, yi, bi, fillValue);
                    }
                }
            }
        }

        return target;
    }

    /**
     * 
     * @param lat_0_Data
     * @param lon_0_Data
     * @param data
     * @param length
     * @param length2
     * @param length3
     * @param lat_0_Index
     * @param lon_0_Index
     * @param index
     */
    private void computeMatrixExtremes(Array lat_0_Data, Array lon_0_Data,
            Array z_0_data, int imageWidth, int imageHeight, int depth,
            Index lat_0_Index, Index lon_0_Index, Index z_0_index, int tv) {
        computeMatrixExtremes(lat_0_Data, lon_0_Data, imageWidth, imageHeight,
                lat_0_Index, lon_0_Index, tv);

        if (Float.isNaN(this.zminTV[tv]) || Float.isNaN(this.zmaxTV[tv])) {
            this.zminTV[tv] = Float.POSITIVE_INFINITY;
            this.zmaxTV[tv] = Float.NEGATIVE_INFINITY;

            for (int yi = 0; yi < imageHeight; yi++) {
                for (int xi = 0; xi < imageWidth; xi++) {
                    for (int zi = 0; zi < depth; zi++) {
                        float z = z_0_data.getFloat(z_0_index.set(yi, xi, zi));
                        if (z < this.zminTV[tv])
                            this.zminTV[tv] = z;
                        if (z > this.zmaxTV[tv])
                            this.zmaxTV[tv] = z;
                    }
                }
            }
        }

        final float rangeZ = this.zmaxTV[tv] - this.zminTV[tv];
        this.zperiodTV[tv] = rangeZ / (depth - 1);
        System.out.println(this.zminTV[tv] + ":" + this.zmaxTV[tv] + " / "
                + this.zperiodTV[tv]);
    }

    /**
     * @param latData
     * @param lonData
     * @param imageWidth
     * @param imageHeight
     * @param latIndex
     * @param lonIndex
     */
    private void computeMatrixExtremes(final Array latData,
            final Array lonData, final int imageWidth, final int imageHeight,
            final Index latIndex, final Index lonIndex, final int tv) {
        if (Float.isNaN(this.xminTV[tv]) || Float.isNaN(this.yminTV[tv])
                || Float.isNaN(this.xmaxTV[tv]) || Float.isNaN(this.ymaxTV[tv])
                || Float.isNaN(this.xperiodTV[tv]) || Float.isNaN(this.yperiodTV[tv])) {
            this.xminTV[tv] = Float.POSITIVE_INFINITY;
            this.yminTV[tv] = Float.POSITIVE_INFINITY;
            this.xmaxTV[tv] = Float.NEGATIVE_INFINITY;
            this.ymaxTV[tv] = Float.NEGATIVE_INFINITY;

            for (int yi = 0; yi < imageHeight; yi++) {
                for (int xi = 0; xi < imageWidth; xi++) {
                    float x = lonData.getFloat(lonIndex.set(xi));
                    float y = latData.getFloat(latIndex.set(yi));
                    if (x < this.xminTV[tv])
                        this.xminTV[tv] = x;
                    if (x > this.xmaxTV[tv])
                        this.xmaxTV[tv] = x;
                    if (y < this.yminTV[tv])
                        this.yminTV[tv] = y;
                    if (y > this.ymaxTV[tv])
                        this.ymaxTV[tv] = y;
                }
            }

            final float rangeX = this.xmaxTV[tv] - this.xminTV[tv];
            final float rangeY = this.ymaxTV[tv] - this.yminTV[tv];
            this.xperiodTV[tv] = rangeX / (imageWidth - 1);
            this.yperiodTV[tv] = rangeY / (imageHeight - 1);

            System.out.println(this.xminTV[tv] + ":" + this.yminTV[tv] + " - "
                    + this.xmaxTV[tv] + ":" + this.ymaxTV[tv] + " / " + this.xperiodTV[tv]
                    + ":" + this.yperiodTV[tv]);
        }
    }

    private static void setVariableAttributes(Variable variable,
            NetcdfFileWriteable writableFile, String[] exceptions) {
        setVariableAttributes(variable, writableFile, exceptions, -1);
    }
    
    private static void setVariableAttributes(Variable variable,
            NetcdfFileWriteable writableFile, String[] exceptions, final int suffix) {
        List<Attribute> attributes = variable.getAttributes();
        String name = variable.getName();
        if (suffix!=-1)
            name = name + suffix;
        if (attributes != null) {
            for (Attribute att : attributes) {
                final String attribName = att.getName();
                boolean skip = false;
                if (exceptions != null)
                    for (int i = 0; i < exceptions.length; i++)
                        if (exceptions[i].equalsIgnoreCase(attribName)) {
                            skip = true;
                            break;
                        }
                if (skip)
                    continue;
                if (att.isArray())
                    writableFile.addVariableAttribute(name, attribName, att
                            .getValues());
                else if (att.isString())
                    writableFile.addVariableAttribute(name, attribName, att
                            .getStringValue());
                else
                    writableFile.addVariableAttribute(name, attribName, att
                            .getNumericValue());
            }
        }
    }

    private static void setVariableAttributes(Variable variable,
            NetcdfFileWriteable writableFile) {
        setVariableAttributes(variable, writableFile, null);
    }

    private static void copyGlobalAttributes(NetcdfFileWriteable writableFile,
            List<Attribute> attributes) {
        if (!attributes.isEmpty()) {
            for (final Attribute attrib : attributes) {
                if (attrib.isArray())
                    writableFile.addGlobalAttribute(attrib.getName(), attrib
                            .getValues());
                else if (attrib.isString())
                    writableFile.addGlobalAttribute(attrib.getName(), attrib
                            .getStringValue());
                else
                    writableFile.addGlobalAttribute(attrib.getName(), attrib
                            .getNumericValue());
            }
        }
    }

    private void setTimeVariableAttributes(Variable time_0,
            NetcdfFileWriteable ncFileOut, GregorianCalendar calendar)
            throws IOException {
        final String name = time_0.getName();
        final String year = Integer.toString(calendar.get(Calendar.YEAR));
        final String month = Integer
                .toString((calendar.get(Calendar.MONTH) + 1));
        final String day = Integer
                .toString(calendar.get(Calendar.DAY_OF_MONTH));
        String hour = Integer.toString(calendar.get(Calendar.HOUR));
        if (hour.equalsIgnoreCase("0"))
            hour += "0";

        String minute = Integer.toString(calendar.get(Calendar.MINUTE));
        if (minute.equalsIgnoreCase("0"))
            minute += "0";

        String second = Integer.toString(calendar.get(Calendar.SECOND));
        if (second.equalsIgnoreCase("0"))
            second += "0";
        final String millisecond = Integer.toString(calendar
                .get(Calendar.MILLISECOND));

        final StringBuffer sbTime = new StringBuffer(year).append("-").append(
                month).append("-").append(day).append(" ").append(hour).append(
                ":").append(minute).append(":").append(second).append(".")
                .append(millisecond);
        ncFileOut.addVariableAttribute(name, "units", "seconds since "
                + sbTime.toString());
        ncFileOut.addVariableAttribute(name, "long_name", "time");
    }
}