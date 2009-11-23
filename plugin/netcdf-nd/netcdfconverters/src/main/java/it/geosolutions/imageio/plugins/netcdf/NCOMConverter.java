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
import java.util.List;
import java.util.logging.Level;

import javax.media.jai.JAI;

import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayShort;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * @author Daniele Romagnoli
 */
public class NCOMConverter {

    private final static ArrayList<String> VARIABLES = new ArrayList<String>(14);
    static {
        VARIABLES.add("surf_atm_press");
        VARIABLES.add("surf_temp_flux");
        VARIABLES.add("surf_salt_flux");
        VARIABLES.add("surf_solar_flux");
        VARIABLES.add("surf_roughness");
        VARIABLES.add("surf_wnd_stress_grid_x");
        VARIABLES.add("surf_wnd_stress_grid_y");
        VARIABLES.add("surf_el");
        VARIABLES.add("water_u");
        VARIABLES.add("water_v");
        VARIABLES.add("water_w");
        VARIABLES.add("water_temp");
        VARIABLES.add("salinity");
        NUMVARS = VARIABLES.size();
    }

    private final static int NUMVARS;

    private static final String fileNameIn = "E:\\Work\\data\\rixen\\lsvc08\\NRL\\NCOM\\20080929\\temp_m08_nest0_20080929.nc";
    private static final String fileNameOut = "E:/work/data/rixen/converted/converted_temp_m08_nest0_20080929.nc";

    
    public NCOMConverter() {
        ;
    }

    public static void main(String[] args) throws IOException {
        NCOMConverter converter = new NCOMConverter();
        String fileNameIn = NCOMConverter.fileNameIn;
        String fileNameOut = NCOMConverter.fileNameOut;
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
            final Dimension timeDim0 = ncFileIn.findDimension("time");
            final int nTimes = timeDim0.getLength();
            
            final Dimension latDim0 = ncFileIn.findDimension("lat");
            final int nLat = latDim0.getLength();
               
            final Dimension lonDim0 = ncFileIn.findDimension("lon");
            final int nLon = lonDim0.getLength();

            // input VARIABLES
            final Variable timeOriginalVar = ncFileIn.findVariable("time");
            final Array timeOriginalData = timeOriginalVar.read();
            final Index timeOriginalIndex = timeOriginalData.getIndex();
            final DataType timeDataType = timeOriginalVar.getDataType();

            final Variable lonOriginalVar = ncFileIn.findVariable("lon");
            final DataType lonDataType = lonOriginalVar.getDataType();
            
            final Variable latOriginalVar = ncFileIn.findVariable("lat");
            final DataType latDataType = latOriginalVar.getDataType();

            final Array latOriginalData = latOriginalVar.read();
            final Array lonOriginalData = lonOriginalVar.read();

            // //
            //
            // Depth related vars
            //
            // //
            Array depthOriginalData = null;
            int nDepths = 0;
            Array depth1Data = null;
            Dimension depthDim = null;
            DataType depthDataType = null;

            final Variable depthOriginalVar = ncFileIn.findVariable("depth"); // Depth
            if (depthOriginalVar != null) {
                nDepths = depthOriginalVar.getDimension(0).getLength();
                depthOriginalData = depthOriginalVar.read();
                depthDataType = depthOriginalVar.getDataType();
                hasDepth = true;
            }

            Dimension timeDim = ncFileOut.addDimension("time", nTimes);
            Dimension latDim = ncFileOut.addDimension(NetCDFUtilities.LAT,
                    nLat);
            
            Dimension lonDim = ncFileOut.addDimension(NetCDFUtilities.LON,
                    nLon);

            if (hasDepth)
                depthDim = ncFileOut.addDimension(NetCDFUtilities.DEPTH,
                        nDepths);

            NetCDFConverterUtilities.copyGlobalAttributes(ncFileOut, ncFileIn.getGlobalAttributes());

            // Dimensions
            Variable timeVar = ncFileOut.addVariable("time", timeDataType,
                    new Dimension[] { timeDim });
            NetCDFConverterUtilities.setVariableAttributes(timeOriginalVar, ncFileOut, new String[]{"long_name"});
            ncFileOut.addVariableAttribute("time", "long_name", "time");

            ncFileOut.addVariable(NetCDFUtilities.LAT, latDataType,
                    new Dimension[] { latDim });
            NetCDFConverterUtilities.setVariableAttributes(latOriginalVar, ncFileOut);

            ncFileOut.addVariable(NetCDFUtilities.LON, lonDataType,
                    new Dimension[] { lonDim });
            NetCDFConverterUtilities.setVariableAttributes(lonOriginalVar, ncFileOut);
            
            if (hasDepth){
                ncFileOut.addVariable(NetCDFUtilities.DEPTH, depthDataType,
                        new Dimension[] { depthDim });
                NetCDFConverterUtilities.setVariableAttributes(depthOriginalVar, ncFileOut);
            }
            
            // lat Variable
            Array lat1Data = NetCDFConverterUtilities.getArray(nLat, latDataType);
            NetCDFConverterUtilities.setData1D(latOriginalData, lat1Data, latDataType, nLat, true);

            // lon Variable
            Array lon1Data = NetCDFConverterUtilities.getArray(nLon, lonDataType);
            NetCDFConverterUtilities.setData1D(lonOriginalData, lon1Data, lonDataType, nLon, false);
            
            if (hasDepth) {
                // depth level Variable
                depth1Data = NetCDFConverterUtilities.getArray(nDepths, depthDataType);
                NetCDFConverterUtilities.setData1D(depthOriginalData, depth1Data, depthDataType, nDepths, false);
            }

            // {} Variables
            final ArrayList<String> variables = new ArrayList<String>(5);
            final HashMap<String, String> updatingValidRange = new HashMap<String, String>(
                    5);
            int numVars = 0;

            for (int i = 0; i < NUMVARS; i++) {
                String varName = (String) VARIABLES.get(i);
                Variable var = ncFileIn.findVariable(varName);
                if (var != null) {
                    variables.add(varName);
                    List<Dimension> dims = var.getDimensions();
                    boolean hasLocalDepth = false;
                    for (Dimension dim: dims){
                        if (dim.getName().equalsIgnoreCase("depth")){
                            hasLocalDepth = true;
                            break;
                        }
                    }
                    if (hasDepth && hasLocalDepth)
                    ncFileOut
                            .addVariable(varName, var.getDataType(),
                                    new Dimension[] { timeDim, depthDim,
                                            latDim, lonDim });
                    else 
                        ncFileOut
                        .addVariable(varName, var.getDataType(),
                                new Dimension[] { timeDim,
                                        latDim, lonDim });
                    Attribute validRange = var.findAttribute(NetCDFUtilities.DatasetAttribs.VALID_MAX);
                    if (validRange != null)
                        validRange = var.findAttribute(NetCDFUtilities.DatasetAttribs.VALID_MIN);
                    if (validRange == null)
                        validRange = var.findAttribute(NetCDFUtilities.DatasetAttribs.VALID_RANGE);

                    if (validRange == null) {
                        updatingValidRange.put(varName, "");
                        ArrayShort range = new ArrayShort(new int[] { 2 });
                        Index index = range.getIndex();
                        range.setShort(index.set(0), Short.MIN_VALUE);
                        range.setShort(index.set(1), Short.MAX_VALUE);
                        ncFileOut.addVariableAttribute(varName, NetCDFUtilities.DatasetAttribs.VALID_RANGE,
                                range);
                    }
                    NetCDFConverterUtilities.setVariableAttributes(var, ncFileOut);
                    numVars++;
                }
            }

            // writing bin data ...

            ncFileOut.create();

            ArrayFloat timeData = new ArrayFloat(new int[] { timeDim
                    .getLength() });
            Index timeIndex = timeData.getIndex();
            for (int t = 0; t < timeDim.getLength(); t++) {
                timeData.setFloat(timeIndex.set(t), timeOriginalData
                        .getFloat(timeOriginalIndex.set(t)));
            }

            ncFileOut.write("time", timeData);
            timeVar = ncFileOut.findVariable("time");
            timeDim.addCoordinateVariable(timeVar);
            ncFileOut.write(NetCDFUtilities.LAT, lat1Data);
            ncFileOut.write(NetCDFUtilities.LON, lon1Data);

            if (hasDepth) {
                Variable depthVar = ncFileOut.findVariable("depth");
                depthDim.addCoordinateVariable(depthVar);
                ncFileOut.write(NetCDFUtilities.DEPTH, depth1Data);
            }

            for (int i = 0; i < numVars; i++) {
                String varName = (String) variables.get(i);
                Variable var = ncFileIn.findVariable(varName);
                boolean hasLocalDepth = NetCDFConverterUtilities.hasThisDimension(var, "depth");
                Array originalVarArray = var.read();
                DataType varDataType = var.getDataType();
                Array destArray = null;
                int[] dimensions = null;
                if (hasDepth && hasLocalDepth) {
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
                final boolean setDepth = hasDepth && hasLocalDepth;
                final int[] loopLengths;
                if (setDepth)
                    loopLengths = new int[]{nTimes, nDepths, nLat, nLon};
                else 
                    loopLengths = new int[]{nTimes, nLat, nLon};
                NetCDFConverterUtilities.writeData(ncFileOut, varName, var, originalVarArray, destArray, findNewRange, false, loopLengths, true);
            }

            ncFileOut.close();
            outputFile.renameTo(fileOut);
        } catch (Exception e) {
            // something bad happened
            if (NetCDFConverterUtilities.LOGGER.isLoggable(Level.INFO))
                NetCDFConverterUtilities.LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            JAI.getDefaultInstance().getTileCache().flush();
        }
    }

}