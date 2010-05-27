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
import java.util.List;
import java.util.logging.Level;

import javax.media.jai.JAI;

import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * @author Daniele Romagnoli
 */
public class SWANConverter {

    private static final String fileNameIn = "c:\\Work\\data\\rixen\\lscv08\\NRL/SWAN/Ligurian_Sea/2008092500.nc";
    private static final String fileNameOut = "c:/work/data/rixen/converted/converted_2008092500.nc";

    public SWANConverter() {
        ;
    }

    public static void main(String[] args) throws IOException {
        SWANConverter converter = new SWANConverter();
        String fileNameIn = SWANConverter.fileNameIn;
        String fileNameOut = SWANConverter.fileNameOut;
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

            boolean hasZeta = false;
            // input dimensions
            final Dimension timeDim0 = ncFileIn.findDimension("time");
            final int nTimes = timeDim0.getLength();
            
            final Dimension latDim0 = ncFileIn.findDimension(NetCDFUtilities.LATITUDE);
            final int nLat = latDim0.getLength();
               
            final Dimension lonDim0 = ncFileIn.findDimension(NetCDFUtilities.LONGITUDE);
            final int nLon = lonDim0.getLength();

            // input VARIABLES
            final Variable timeOriginalVar = ncFileIn.findVariable("time");
            final Array timeOriginalData = timeOriginalVar.read();
            final Index timeOriginalIndex = timeOriginalData.getIndex();
            final DataType timeDataType = timeOriginalVar.getDataType();

            final Variable lonOriginalVar = ncFileIn.findVariable(NetCDFUtilities.LONGITUDE);
            final DataType lonDataType = lonOriginalVar.getDataType();
            
            final Variable latOriginalVar = ncFileIn.findVariable(NetCDFUtilities.LATITUDE);
            final DataType latDataType = latOriginalVar.getDataType();

            final Array latOriginalData = latOriginalVar.read();
            final Array lonOriginalData = lonOriginalVar.read();

            // //
            //
            // Depth related vars
            //
            // //
            Array levelOriginalData = null;
            int nZeta = 0;
            Array zeta1Data = null;
            Dimension zDim = null;
            DataType zetaDataType = null;

            final Variable levelOriginalVar = ncFileIn.findVariable("z"); // Depth
            if (levelOriginalVar != null) {
                nZeta = levelOriginalVar.getDimension(0).getLength();
                levelOriginalData = levelOriginalVar.read();
                zetaDataType = levelOriginalVar.getDataType();
                hasZeta = true;
            }

            Dimension timeDim = ncFileOut.addDimension("time", nTimes);
            Dimension latDim = ncFileOut.addDimension(NetCDFUtilities.LAT,
                    nLat);
            
            Dimension lonDim = ncFileOut.addDimension(NetCDFUtilities.LON,
                    nLon);

            if (hasZeta)
                zDim = ncFileOut.addDimension(NetCDFUtilities.HEIGHT,
                        nZeta);

            NetCDFConverterUtilities.copyGlobalAttributes(ncFileOut, ncFileIn.getGlobalAttributes());

            // Dimensions
            Variable timeVar = ncFileOut.addVariable("time", timeDataType,
                    new Dimension[] { timeDim });
            NetCDFConverterUtilities.setVariableAttributes(timeOriginalVar, ncFileOut, new String[]{"long_name"});
            ncFileOut.addVariableAttribute("time", "long_name", "time");

            ncFileOut.addVariable(NetCDFUtilities.LAT, latDataType,
                    new Dimension[] { latDim });
            NetCDFConverterUtilities.setVariableAttributes(latOriginalVar, ncFileOut, NetCDFUtilities.LAT );

            ncFileOut.addVariable(NetCDFUtilities.LON, lonDataType,
                    new Dimension[] { lonDim });
            NetCDFConverterUtilities.setVariableAttributes(lonOriginalVar, ncFileOut, NetCDFUtilities.LON);
            
            if (hasZeta){
                ncFileOut.addVariable(NetCDFUtilities.HEIGHT, zetaDataType,
                        new Dimension[] { zDim });
                NetCDFConverterUtilities.setVariableAttributes(levelOriginalVar, ncFileOut, NetCDFUtilities.HEIGHT, new String[]{"long_name"});
                ncFileOut.addVariableAttribute(NetCDFUtilities.HEIGHT, "positive", "up");
                ncFileOut.addVariableAttribute(NetCDFUtilities.HEIGHT, "long_name", NetCDFUtilities.HEIGHT);

            }
            
            // lat Variable
            Array lat1Data = NetCDFConverterUtilities.getArray(nLat, latDataType);
            NetCDFConverterUtilities.setData1D(latOriginalData, lat1Data, latDataType, nLat, true);

            // lon Variable
            Array lon1Data = NetCDFConverterUtilities.getArray(nLon, lonDataType);
            NetCDFConverterUtilities.setData1D(lonOriginalData, lon1Data, lonDataType, nLon, false);
            
            if (hasZeta) {
                // depth level Variable
                zeta1Data = NetCDFConverterUtilities.getArray(nZeta, zetaDataType);
                NetCDFConverterUtilities.setData1D(levelOriginalData, zeta1Data, zetaDataType, nZeta, false);
            }

            // {} Variables
            final ArrayList<String> variables = new ArrayList<String>(5);
            int numVars = 0;

            List<Variable> findVariables = ncFileIn.getVariables();
            for (Variable var : findVariables) {
                if (var != null) {
                    String varName = var.getName();
                    if (varName.equalsIgnoreCase(NetCDFUtilities.LATITUDE) ||
                        varName.equalsIgnoreCase(NetCDFUtilities.LONGITUDE) ||
                        varName.equalsIgnoreCase(NetCDFUtilities.TIME) ||
                        varName.equalsIgnoreCase(NetCDFUtilities.ZETA))
                        continue;
                    variables.add(varName);
                    List<Dimension> dims = var.getDimensions();
                    boolean hasLocalZeta = false;
                    for (Dimension dim: dims){
                        if (dim.getName().equalsIgnoreCase(NetCDFUtilities.ZETA)){
                            hasLocalZeta = true;
                            break;
                        }
                    }
                    if (hasZeta && hasLocalZeta)
                    ncFileOut
                            .addVariable(varName, var.getDataType(),
                                    new Dimension[] { timeDim, zDim,
                                            latDim, lonDim });
                    else 
                        ncFileOut
                        .addVariable(varName, var.getDataType(),
                                new Dimension[] { timeDim,
                                        latDim, lonDim });
                    NetCDFConverterUtilities.setVariableAttributes(var, ncFileOut, new String[]{"missing_value"});
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

            if (hasZeta) {
                Variable heightVar = ncFileOut.findVariable(NetCDFUtilities.HEIGHT);
                zDim.addCoordinateVariable(heightVar);
                ncFileOut.write(NetCDFUtilities.HEIGHT, zeta1Data);
            }

            for (int i = 0; i < numVars; i++) {
                String varName = (String) variables.get(i);
                Variable var = ncFileIn.findVariable(varName);
                boolean hasLocalZeta = NetCDFConverterUtilities.hasThisDimension(var, NetCDFUtilities.ZETA);
                Array originalVarArray = var.read();
                DataType varDataType = var.getDataType();
                Array destArray = null;
                int[] dimensions = null;
                if (hasZeta && hasLocalZeta) {
                    dimensions = new int[] { timeDim.getLength(),
                            zDim.getLength(), latDim.getLength(),
                            lonDim.getLength() };

                } else {
                    dimensions = new int[] { timeDim.getLength(),
                            latDim.getLength(), lonDim.getLength() };
                }
                destArray = NetCDFConverterUtilities.getArray(dimensions,
                        varDataType);
                
                final boolean setZeta = hasZeta && hasLocalZeta;
                final int[] loopLengths;
                if (setZeta)
                    loopLengths = new int[]{nTimes, nZeta, nLat, nLon};
                else 
                    loopLengths = new int[]{nTimes, nLat, nLon};
                NetCDFConverterUtilities.writeData(ncFileOut, varName, var, originalVarArray, destArray, false, false, loopLengths, true);
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