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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * TODO Description here ...
 * 
 * @author Daniele Romagnoli
 * 
 * @version $Id: INGVConverter.java 916 2009-06-30 13:06:58Z dany111 $
 */
public class INGVConverter {

    private static String[] depthNames = new String[] { "depth", "deptht",
            "depthu", "depthv", "depthw" };

    public final String NAV_LON = "nav_lon";

    public final String NAV_LAT = "nav_lat";

    private final static ArrayList<String> VARIABLES = new ArrayList<String>(7);
    static {
        VARIABLES.add("sohefldo");
        VARIABLES.add("soshfldo");
        VARIABLES.add("sossheig");
        VARIABLES.add("sowaflup");
        VARIABLES.add("vosaline");
        VARIABLES.add("votemper");
        VARIABLES.add("sozotaux");
        VARIABLES.add("vozocrtx");
        VARIABLES.add("vomecrty");
        VARIABLES.add("sometauy");
        NUMVARS = VARIABLES.size();
    }

    private static final String fileNameIn = "E:\\Work\\data\\rixen\\lsvc08\\UNIBO\\HOPS2\\20080929\\HOPS1km_H_080929_T.nc";

    private static final String fileNameOut = "E:/work/data/rixen/converted/converted_HOPS1km_H_080929_T.nc";

    final static int NUMVARS;

    public static void main(String[] args) throws IOException {
        INGVConverter converter = new INGVConverter();
        String fileNameIn = INGVConverter.fileNameIn;
        String fileNameOut = INGVConverter.fileNameOut;
        converter.run(fileNameIn, fileNameOut);
    }

    private void run(String fileNameIn, String fileNameOut) {
        try {
            final File fileIn = new File(fileNameIn);
            final NetcdfFile ncFileIn = NetcdfFile.open(fileNameIn);
            final File fileOut = new File(fileNameOut);
            // keep original name
            final File outputFile = File.createTempFile(fileIn.getName(),
                    ".tmp");
            final NetcdfFileWriteable ncFileOut = NetcdfFileWriteable
                    .createNew(outputFile.getAbsolutePath());

            boolean hasDepth = false;
            // input dimensions
            String timeName = "time_counter";
            Dimension timeOriginalDim = ncFileIn.findDimension(timeName);
            if (timeOriginalDim == null) {
                timeOriginalDim = ncFileIn.findDimension("time");
                timeName = "time";
            }
            final Dimension yDim = ncFileIn.findDimension("y");
            final Dimension xDim = ncFileIn.findDimension("x");

            // input VARIABLES
            final Variable timeOriginalVar = ncFileIn.findVariable(timeName);
            final Array timeOriginalData = timeOriginalVar.read();
            final DataType timeDataType = timeOriginalVar.getDataType();

            final Variable navLat = ncFileIn.findVariable(NAV_LAT);
            final DataType navLatDataType = navLat.getDataType();

            final Variable navLon = ncFileIn.findVariable(NAV_LON);
            final DataType navLonDataType = navLon.getDataType();

            final int nLat = yDim.getLength();
            final int nLon = xDim.getLength();
            final int nTimes = timeOriginalDim.getLength();

            final Array latOriginalData = navLat.read("0:" + (nLat - 1) + ":1, 0:0:1")
                    .reduce();

            final Array lonOriginalData = navLon.read("0:0:1, 0:" + (nLon - 1) + ":1")
                    .reduce();

            // //
            //
            // Depth related vars
            //
            // //
            Array depthOriginalData = null;
            DataType depthDataType = null;
            int nDepths = 0;
            Array depthDestData = null;
            Dimension depthDim = null;
            String depthName = "depth";

            Variable depthOriginalVar = null;
            int dName = 0;
            while (depthOriginalVar == null) {
                if (dName == depthNames.length)
                    break;
                String name = depthNames[dName++];
                depthOriginalVar = ncFileIn.findVariable(name); // Depth
            }
            if (depthOriginalVar != null) {
                depthName = depthNames[dName - 1];
                nDepths = depthOriginalVar.getDimension(0).getLength();
                depthOriginalData = depthOriginalVar.read();
                hasDepth = true;
            }

            Dimension timeDim = ncFileOut.addDimension("time", nTimes);
            Dimension latDim = ncFileOut
                    .addDimension(NetCDFUtilities.LAT, nLat);
            Dimension lonDim = ncFileOut
                    .addDimension(NetCDFUtilities.LON, nLon);
            if (hasDepth)
                depthDim = ncFileOut.addDimension(NetCDFUtilities.DEPTH,
                        nDepths);

            // writing file

            NetCDFConverterUtilities.copyGlobalAttributes(ncFileOut, ncFileIn
                    .getGlobalAttributes());

            // //
            //
            // Time requires a special Management
            //
            // //

            // time Variable
            Variable timeVar = ncFileOut.addVariable(NetCDFUtilities.TIME,
                    timeDataType, new Dimension[] { timeDim });
            NetCDFConverterUtilities.setVariableAttributes(timeOriginalVar, ncFileOut,
                    NetCDFUtilities.TIME);

            // Dimensions
            ncFileOut.addVariable(NetCDFUtilities.LAT, navLatDataType,
                    new Dimension[] { latDim });
            NetCDFConverterUtilities.setVariableAttributes(navLat, ncFileOut,
                    NetCDFUtilities.LAT);

            ncFileOut.addVariable(NetCDFUtilities.LON, navLonDataType,
                    new Dimension[] { lonDim });
            NetCDFConverterUtilities.setVariableAttributes(navLon, ncFileOut,
                    NetCDFUtilities.LON);

            Array lat1Data = NetCDFConverterUtilities.getArray(nLat,
                    navLatDataType);
            NetCDFConverterUtilities.setData1D(latOriginalData, lat1Data,
                    navLatDataType, nLat, true);

            // lon Variable
            Array lon1Data = NetCDFConverterUtilities.getArray(nLon,
                    navLonDataType);
            NetCDFConverterUtilities.setData1D(lonOriginalData, lon1Data,
                    navLonDataType, nLon, false);

            if (hasDepth) {
                depthDataType = depthOriginalVar.getDataType();
                ncFileOut.addVariable(NetCDFUtilities.DEPTH, depthDataType,
                        new Dimension[] { depthDim });
                NetCDFConverterUtilities.setVariableAttributes(depthOriginalVar,
                        ncFileOut, NetCDFUtilities.DEPTH);
            }

            if (hasDepth) {
                // depth level Variable
                depthDestData = NetCDFConverterUtilities.getArray(nDepths,
                        depthDataType);
                NetCDFConverterUtilities.setData1D(depthOriginalData, depthDestData,
                        depthDataType, nDepths, false);
            }

            // {} Variables
            final ArrayList<String> variables = new ArrayList<String>(5);
            final HashMap<String, String> updatingValidRange = new HashMap<String, String>(
                    5);
            final HashMap<String, String> updatingFilLValue = new HashMap<String, String>(
                    5);
            int numVars = 0;

            for (int i = 0; i < NUMVARS; i++) {
                String varName = (String) VARIABLES.get(i);
                Variable var = ncFileIn.findVariable(varName);
                if (var != null) {
                    variables.add(varName);
                    boolean hasLocalDepth = NetCDFConverterUtilities.hasThisDimension(var, depthName);
                    if (hasDepth && hasLocalDepth)
                        ncFileOut.addVariable(varName, var.getDataType(),
                                new Dimension[] { timeDim, depthDim, latDim,
                                        lonDim });
                    else
                        ncFileOut.addVariable(varName, var.getDataType(),
                                new Dimension[] { timeDim, latDim, lonDim });
                    
                    // //
                    //
                    // Check for updating valid range
                    //
                    // //
                    
                    boolean hasMinMax = false;
                    Attribute validMax  = var
                            .findAttribute(NetCDFUtilities.DatasetAttribs.VALID_MAX);
                    Attribute validMin  = var
                            .findAttribute(NetCDFUtilities.DatasetAttribs.VALID_MIN);
                    Attribute fillValue = var.findAttribute(NetCDFUtilities.DatasetAttribs.FILL_VALUE);
                    boolean hasMissingValue = false;
                    boolean hasFillValue = true;
                    if (fillValue == null){
                        hasFillValue = false;
                        fillValue = var.findAttribute(NetCDFUtilities.DatasetAttribs.MISSING_VALUE);
                        if (fillValue!=null)
                            hasMissingValue = true;
                    }
                    Attribute validRange = var.findAttribute(NetCDFUtilities.DatasetAttribs.VALID_RANGE);
                    boolean hasValidRange = false;
                    boolean rewriteAttribute = false;
                    
                    if (validMin != null && validMax != null && fillValue!=null){
                        rewriteAttribute = !NetCDFConverterUtilities.isFillValueOutsideValidRange(validMax,validMin,fillValue,var.getDataType());
                        hasMinMax = true;
                    }
                    else if (validRange != null && fillValue!=null){
                        rewriteAttribute = !NetCDFConverterUtilities.isFillValueOutsideValidRange(validRange,fillValue,var.getDataType());
                        hasValidRange = true;
                    }
                    else {
                        rewriteAttribute = true;
                    }
                    if (rewriteAttribute) {
                        updatingValidRange.put(varName, "");
                        DataType varDatatype = var.getDataType();
                        Array range = NetCDFConverterUtilities
                                .getRangeArray(varDatatype);
                        ncFileOut.addVariableAttribute(varName,
                                NetCDFUtilities.DatasetAttribs.VALID_RANGE, range);
                        if (hasMissingValue && !hasFillValue){
                            updatingFilLValue.put(varName, "");
                            Number fillVal = NetCDFConverterUtilities.getNumber(varDatatype);
                            ncFileOut.addVariableAttribute(varName,
                                    NetCDFUtilities.DatasetAttribs.FILL_VALUE, fillVal);
                        }
                            
                    }
                    String[] exceptions=null;
                    if (hasMinMax){
                        if (hasMissingValue)
                            exceptions=new String[]{NetCDFUtilities.DatasetAttribs.VALID_MAX,NetCDFUtilities.DatasetAttribs.VALID_MIN, NetCDFUtilities.DatasetAttribs.MISSING_VALUE};
                        else
                            exceptions=new String[]{NetCDFUtilities.DatasetAttribs.VALID_MAX,NetCDFUtilities.DatasetAttribs.VALID_MIN};
                        
                    }
                        else if (hasValidRange){
                            if (hasMissingValue)
                                exceptions=new String[]{NetCDFUtilities.DatasetAttribs.VALID_RANGE, NetCDFUtilities.DatasetAttribs.MISSING_VALUE};
                            else
                                exceptions=new String[]{NetCDFUtilities.DatasetAttribs.VALID_RANGE};
                        }
                        
                    else if (hasMissingValue)
                        exceptions = new String[] { NetCDFUtilities.DatasetAttribs.MISSING_VALUE};
                    NetCDFConverterUtilities.setVariableAttributes(var,
                            ncFileOut, exceptions);
                    numVars++;
                }
            }

            // writing bin data ...

            ncFileOut.create();

            Array timeData = NetCDFConverterUtilities.getArray(nTimes,
                    timeDataType);
            NetCDFConverterUtilities.setData1D(timeOriginalData, timeData,
                    timeDataType, nTimes, false);

            ncFileOut.write("time", timeData);
            timeVar = ncFileOut.findVariable("time");
            timeDim.addCoordinateVariable(timeVar);
            
            ncFileOut.write(NetCDFUtilities.LAT, lat1Data);
            ncFileOut.write(NetCDFUtilities.LON, lon1Data);

            if (hasDepth) {
                Variable depthVar = ncFileOut.findVariable("depth");
                depthDim.addCoordinateVariable(depthVar);
                ncFileOut.write(NetCDFUtilities.DEPTH, depthDestData);
            }

            for (int i = 0; i < numVars; i++) {
                String varName = (String) variables.get(i);
                Variable var = ncFileIn.findVariable(varName);
                final boolean hasLocalDepth = NetCDFConverterUtilities
                        .hasThisDimension(var, depthName);

                Array originalVarArray = var.read();
                DataType varDataType = var.getDataType();
                Array destArray = null;
                int[] dimensions = null;
                final boolean setDepth = hasDepth && hasLocalDepth;
                if (setDepth) {
                    dimensions = new int[] { timeDim.getLength(),
                            depthDim.getLength(), latDim.getLength(),
                            lonDim.getLength() };

                } else {
                    dimensions = new int[] { timeDim.getLength(),
                            latDim.getLength(), lonDim.getLength() };
                }
                destArray = NetCDFConverterUtilities.getArray(dimensions,
                        varDataType);

                boolean findNewRange = updatingValidRange.containsKey(varName);
                boolean updateFillValue = updatingFilLValue.containsKey(varName);
                
                final int[] loopLengths;
                if (setDepth)
                    loopLengths = new int[] { nTimes, nDepths, nLat, nLon };
                else
                    loopLengths = new int[] { nTimes, nLat, nLon };
                NetCDFConverterUtilities.writeData(ncFileOut, varName, var,
                        originalVarArray, destArray, findNewRange, updateFillValue, loopLengths,
                        true);
            }

            ncFileOut.close();
            outputFile.renameTo(fileOut);
        } catch (Exception e) {
            // something bad happened
            if (NetCDFConverterUtilities.LOGGER.isLoggable(Level.INFO))
                NetCDFConverterUtilities.LOGGER.log(Level.INFO, e
                        .getLocalizedMessage(), e);
        }
    }

}
