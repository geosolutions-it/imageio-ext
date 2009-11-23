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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.JAI;
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
 * 
 * @author Alessio Fabiani
 * 
 */
public class HOPSConverter{
    
    private float xmin = Float.NaN;

    private float ymin = Float.NaN;

    private float xmax = Float.NaN;

    private float ymax = Float.NaN;

    private float periodX = Float.NaN;

    private float periodY = Float.NaN;

    private final static ArrayList<String> variables = new ArrayList<String>(4);
    static {
        variables.add("temp");
        variables.add("salt");
        variables.add("u");
        variables.add("v");
        NUMVARS = variables.size();
    }

    final static int NUMVARS;

    final static String UNITS = "units";

    private static final boolean APPLY_MASK = false;

    private boolean writeToOut;
    
    private static final String fileName = "C:/work/data/";

    private static File inputFile = new File(fileName);
    
    private final static Logger LOGGER = Logger.getLogger("it.geosolutions.processing.node.worker.netcdf.hops");

    public HOPSConverter() {
        ;
    }

    public static void main(String[] args) throws IOException {
        HOPSConverter converter = new HOPSConverter();
        converter.runTest();

    }
    
    public void runTest() throws IOException {
        try {
            final NetcdfFile ncFileIn = NetcdfFile.open(inputFile.getAbsolutePath());
            final File outDir = /*createTodayDirectory(this.outDir)*/ new File("C:/work/data/testWritten/");
            // keep original name
            final File outputFile = File.createTempFile(inputFile.getName(), ".tmp");
            final NetcdfFileWriteable ncFileOut = NetcdfFileWriteable.createNew(outputFile.getAbsolutePath());

            // input dimensions
            Dimension timeDim0 = ncFileIn.findDimension("time");
            Dimension latDim0 = ncFileIn.findDimension("lat");
            Dimension lonDim0 = ncFileIn.findDimension("lon");

            // input variables
            Variable time_0 = ncFileIn.findVariable("time");
            Array time_0_Data = time_0.read();
            Index time_0_Index = time_0_Data.getIndex();

            Variable lon_0 = ncFileIn.findVariable("lon");
            final int nLon = lon_0.getDimension(0).getLength();

            Variable lat_0 = ncFileIn.findVariable("lat");
            final int nLat = lat_0.getDimension(0).getLength();

            Array lat_0_Data = lat_0.read();
            Index lat_0_Index = lat_0_Data.getIndex();

            Array lon_0_Data = lon_0.read();
            Index lon_0_Index = lon_0_Data.getIndex();

            Variable z_0 = ncFileIn.findVariable("z"); // Depth
            final int nLevels = z_0.getDimension(0).getLength();
            Array z_0_Data = z_0.read();
            Index z_0_Index = z_0_Data.getIndex();

            Dimension timeDim = ncFileOut.addDimension("time", timeDim0.getLength());
            Dimension latDim = ncFileOut.addDimension(NetCDFUtilities.LAT, latDim0.getLength());
            Dimension lonDim = ncFileOut.addDimension(NetCDFUtilities.LON, lonDim0.getLength());
            Dimension depthDim = ncFileOut.addDimension(NetCDFUtilities.DEPTH, nLevels);

            // writing file

            computeMatrixExtremes(lat_0_Data, lon_0_Data, lonDim0.getLength(), latDim0.getLength(), lat_0_Index, lon_0_Index);

            NetCDFConverterUtilities.copyGlobalAttributes(ncFileOut, ncFileIn.getGlobalAttributes());

            // //
            //
            // Time requires a special Management
            //
            // //
            
            // time Variable
            ncFileOut.addVariable("time", DataType.FLOAT, new Dimension[] { timeDim });
            
            // ncFileOut.addVariableAttribute("Time", "long_name", "Time");
            final float referenceTime = setTimeVariableAttributes(time_0, ncFileOut);

            // lat Variable
            ArrayFloat lat_1_Data = new ArrayFloat(new int[] { latDim0.getLength() });
            Index lat_1_Index = lat_1_Data.getIndex();
            ncFileOut.addVariable(NetCDFUtilities.LAT, DataType.FLOAT,
                    new Dimension[] { latDim });

            ncFileOut.addVariableAttribute(NetCDFUtilities.LAT, "long_name", NetCDFUtilities.LATITUDE);
            ncFileOut.addVariableAttribute(NetCDFUtilities.LAT, UNITS, lat_0.getUnitsString());
            for (int yPos = 0; yPos < latDim0.getLength(); yPos++) {
                lat_1_Data
                        .setFloat(lat_1_Index.set(yPos),
                         new Float(
                                 this.ymax
                                 - (new Float(yPos)
                                 .floatValue() * this.periodY))
                                 .floatValue());
//                                new Float(
//                                        this.ymin
//                                                + (new Float(yPos)
//                                                        .floatValue() * this.periodY))
//                                        .floatValue());
            }

            // lon Variable
            ArrayFloat lon_1_Data = new ArrayFloat(new int[] { lonDim0
                    .getLength() });
            Index lon_1_Index = lon_1_Data.getIndex();
            ncFileOut.addVariable(NetCDFUtilities.LON, DataType.FLOAT,
                    new Dimension[] { lonDim });
            ncFileOut.addVariableAttribute(NetCDFUtilities.LON, "long_name", NetCDFUtilities.LONGITUDE);
            ncFileOut.addVariableAttribute(NetCDFUtilities.LON, UNITS, lon_0.getUnitsString());
            for (int xPos = 0; xPos < lonDim0.getLength(); xPos++) {
                lon_1_Data
                        .setFloat(
                                lon_1_Index.set(xPos),
                                new Float(
                                        this.xmin
                                                + (new Float(xPos)
                                                        .floatValue() * this.periodX))
                                        .floatValue());
            }

            // depth level Variable
            ArrayFloat depthlevelDim_1_Data = new ArrayFloat(new int[] { depthDim
                    .getLength() });
            Index depthlevelDim_1_Index = depthlevelDim_1_Data.getIndex();
            ncFileOut.addVariable(NetCDFUtilities.DEPTH, DataType.FLOAT,
                    new Dimension[] { depthDim });
            ncFileOut.addVariableAttribute(NetCDFUtilities.DEPTH, "long_name", NetCDFUtilities.DEPTH);
            ncFileOut.addVariableAttribute(NetCDFUtilities.DEPTH, UNITS, z_0.getUnitsString());
            ncFileOut.addVariableAttribute(NetCDFUtilities.DEPTH, "positive", "up");
            for (int wPos = 0; wPos < depthDim.getLength(); wPos++) {
                depthlevelDim_1_Data.setFloat(depthlevelDim_1_Index.set(wPos),
                        z_0_Data.getFloat(depthlevelDim_1_Index));
            }

            // {} Variables
            for (int i = 0; i < NUMVARS; i++) {
                String varName = (String) variables.get(i);
                Variable var = ncFileIn.findVariable(varName);
                ncFileOut.addVariable(varName, var.getDataType(),
                        new Dimension[] { timeDim, depthDim, latDim, lonDim });
                NetCDFConverterUtilities.setVariableAttributes(var, ncFileOut,
                        new String[] { "positions" });

            }
            // writing bin data ...

            ncFileOut.create();
            
            ArrayFloat timeData = new ArrayFloat(new int[] {
                    timeDim.getLength()});
            Index timeIndex = timeData.getIndex();
            for (int t = 0; t < timeDim.getLength(); t++) {
                float julianTime = time_0_Data.getFloat(time_0_Index.set(t))-referenceTime;
                timeData.setFloat(timeIndex.set(t),julianTime);
            }
            
            ncFileOut.write("time", timeData);
            Variable timeVar = ncFileOut.findVariable("time");
            timeDim.addCoordinateVariable(timeVar);
            ncFileOut.write(NetCDFUtilities.LAT, lat_1_Data);
            ncFileOut.write(NetCDFUtilities.LON, lon_1_Data);
            
            Variable depthVar = ncFileOut.findVariable("depth");
            depthDim.addCoordinateVariable(depthVar);
            ncFileOut.write(NetCDFUtilities.DEPTH, depthlevelDim_1_Data);

            // TODO: AutoApply MASK?

            ArrayFloat maskMatrix = new ArrayFloat.D2(latDim.getLength(),
                    lonDim.getLength());
            Index maskIma = maskMatrix.getIndex();
            if (APPLY_MASK) {
                Variable mask = ncFileIn.findVariable("mask");
                Array maskData = mask.read();
                Index maskIndex = maskData.getIndex();
                ArrayFloat tempData = new ArrayFloat(new int[] {
                        latDim0.getLength(), lonDim0.getLength() });
                Index tempIndex = tempData.getIndex();
                for (int yPos = 0; yPos < latDim0.getLength(); yPos++) {
                    for (int xPos = 0; xPos < lonDim0.getLength(); xPos++) {
                        tempData.setFloat(tempIndex.set(yPos, xPos),
                                maskData.getFloat(maskIndex.set(yPos, xPos)));
                    }
                }

                WritableRaster outData = Resampler(lat_0_Data, lon_0_Data, lonDim0.getLength(),
                                latDim0.getLength(), 2, tempData, -1);
                for (int j = 0; j < latDim0.getLength(); j++) {
                    for (int k = 0; k < lonDim0.getLength(); k++) {
                        float sample = outData.getSampleFloat(k, j, 0);
                        maskMatrix.setFloat(maskIma.set(j, k), sample);
                    }
                }
            }

            for (int i = 0; i < NUMVARS; i++) {
                String varName = (String) variables.get(i);
                Variable var = ncFileIn.findVariable(varName);

                Array originalVarData = var.read();
                Index varIndex = originalVarData.getIndex();
                Attribute fv = var.findAttribute("_FillValue");
                float fillValue = Float.NaN;
                if (fv != null) {
                    fillValue = (fv.getNumericValue()).floatValue();
                }

                ArrayFloat T_tmp_Data = new ArrayFloat(new int[] {
                        latDim0.getLength(), lonDim0.getLength() });
                Index T_tmp_Index = T_tmp_Data.getIndex();

                ArrayFloat Tmatrix = new ArrayFloat.D4(timeDim.getLength(),
                        depthDim.getLength(), latDim.getLength(), lonDim
                                .getLength());
                Index Tima = Tmatrix.getIndex();

                for (int tPos = 0; tPos < timeDim0.getLength(); tPos++) {
                    for (int levelPos = 0; levelPos < depthDim.getLength(); levelPos++) {
                        for (int yPos = 0; yPos < latDim0.getLength(); yPos++) {
                            for (int xPos = 0; xPos < lonDim0.getLength(); xPos++) {
                                T_tmp_Data.setFloat(
                                        T_tmp_Index.set(yPos, xPos),
                                        originalVarData.getFloat(varIndex.set(
                                                tPos, yPos, xPos, levelPos)));
                            }
                        }

                        WritableRaster outData = Resampler(lat_0_Data,
                                lon_0_Data, lonDim0.getLength(), latDim0
                                        .getLength(), 2, T_tmp_Data,fillValue);
                        for (int j = 0; j < latDim0.getLength(); j++) {
                            for (int k = 0; k < lonDim0.getLength(); k++) {
                                float sample = outData.getSampleFloat(k, j, 0);
                                if (APPLY_MASK) {
                                    float maskValue = maskMatrix
                                            .getFloat(maskIma.set(j, k));
                                    if (maskValue == 0)
                                        sample = fillValue;
                                }
                                Tmatrix.setFloat(
                                        Tima.set(tPos, levelPos, j, k), sample);
                            }
                        }

                    }
                }
                ncFileOut.write(varName, Tmatrix);
            }
            ncFileOut.close();
            
            outputFile.renameTo(new File(outDir + File.separator + inputFile.getName()));
        } catch (Exception e) {
            // something bad happened
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            JAI.getDefaultInstance().getTileCache().flush();
        }
    }

    /**
     * Disposing all the of objects created along the path and interrupting
     * Thread.
     */
    protected synchronized void dispose() {
        LOGGER.info("Disposing NetCDFHOPSProcessingNode...");
        LOGGER.info("Disposing NetCDFHOPSProcessingNode... Done!");
    }

   
    
    // ////////////////////////////////////////////////////////////////////////
    //
    // HELPERS !!!
    //
    // ////////////////////////////////////////////////////////////////////////
    private float setTimeVariableAttributes(Variable time_0,
            NetcdfFileWriteable ncFileOut) throws IOException {
        Array time_0_Data = time_0.read();
        Index time_0_Index = time_0_Data.getIndex();
        
        final String name = time_0.getName();
        final float referenceTime = time_0_Data.getFloat(time_0_Index.set(0));
        float fTime = referenceTime;
        Attribute offset = time_0.findAttribute("add_offset");
        if (offset!=null)
            fTime += offset.getNumericValue().floatValue();
        
        GregorianCalendar calendar = null;
        if(time_0.getDescription().toLowerCase().contains("modified julian")) {
            calendar = NetCDFConverterUtilities.fromModifiedJulian(fTime, time_0.getDescription(), time_0.getUnitsString());
        } else {
            calendar = NetCDFConverterUtilities.fromJulian(fTime);
        }
        
        final String year = Integer.toString(calendar.get(Calendar.YEAR));
        final String month = Integer.toString((calendar.get(Calendar.MONTH)+1));
        final String day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
        String hour = Integer.toString(calendar.get(Calendar.HOUR));
        if (hour.equalsIgnoreCase("0"))
            hour+="0";
        
        String minute = Integer.toString(calendar.get(Calendar.MINUTE));
        if (minute.equalsIgnoreCase("0"))
            minute+="0";
        
        String second = Integer.toString(calendar.get(Calendar.SECOND));
        if (second.equalsIgnoreCase("0"))
            second+="0";
        final String millisecond = Integer.toString(calendar.get(Calendar.MILLISECOND));
        
        final StringBuffer sbTime = new StringBuffer(year).append("-").append(month).append("-").append(day).
        append(" ").append(hour).append(":").append(minute).append(":").append(second).append(".").append(millisecond);
        ncFileOut.addVariableAttribute(name, "units", "days since " + sbTime.toString());
        ncFileOut.addVariableAttribute(name, "long_name", "time");
        return referenceTime;
    }

    private WritableRaster Resampler(final Array latData, final Array lonData,
            final int imageWidth, final int imageHeight, final int polyDegree,
            final Array data, final float fillValue) {
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
                latIndex, lonIndex);

        float[] destCoords = new float[2 * numNeededPoints];
        float[] srcCoords = new float[2 * numNeededPoints];

        /*
         * Copy source and destination coordinates into float arrays. The
         * destination coordinates are scaled in order to gets values similar to
         * source coordinates (values will be identical if all "real world"
         * coordinates are grid indices multiplied by a constant).
         */
        int offset = 0;
        for (int yi = 0; yi < imageHeight; yi += stepY) {
            for (int xi = 0; xi < imageWidth; xi += stepX) {
                srcCoords[offset] = xi;
                srcCoords[offset + 1] = yi;

                destCoords[offset] = (float) ((lonData.getFloat(lonIndex
                        .set(xi)) - this.xmin) / this.periodX);
                 destCoords[offset + 1] = (float) ((this.ymax - latData
                 .getFloat(latIndex.set(yi))) / this.periodY);
//                destCoords[offset + 1] = ((latData.getFloat(latIndex.set(yi)) - this.ymin) / this.periodY);
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
//                        target.setSample(xi, yi, bi, Float.NaN);
                        target.setSample(xi, yi, bi, fillValue);
                    }
                }
            }
        }

        return target;
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
            final Index latIndex, final Index lonIndex) {
        if (Float.isNaN(this.xmin) || Float.isNaN(this.ymin)
                || Float.isNaN(this.xmax) || Float.isNaN(this.ymax)
                || Float.isNaN(this.periodX) || Float.isNaN(this.periodY)) {
            this.xmin = Float.POSITIVE_INFINITY;
            this.ymin = Float.POSITIVE_INFINITY;
            this.xmax = Float.NEGATIVE_INFINITY;
            this.ymax = Float.NEGATIVE_INFINITY;

            for (int yi = 0; yi < imageHeight; yi++) {
                for (int xi = 0; xi < imageWidth; xi++) {
                    float x = lonData.getFloat(lonIndex.set(xi));
                    float y = latData.getFloat(latIndex.set(yi));
                    if (x < this.xmin)
                        this.xmin = x;
                    if (x > this.xmax)
                        this.xmax = x;
                    if (y < this.ymin)
                        this.ymin = y;
                    if (y > this.ymax)
                        this.ymax = y;
                }
            }

            final float rangeX = this.xmax - this.xmin;
            final float rangeY = this.ymax - this.ymin;
            this.periodX = rangeX / (imageWidth-1);
            this.periodY = rangeY / (imageHeight-1);

            System.out.println(this.xmin + ":" + this.ymin + " - " + this.xmax
                    + ":" + this.ymax + " / " + this.periodX + ":"
                    + this.periodY);
        }
    }

}