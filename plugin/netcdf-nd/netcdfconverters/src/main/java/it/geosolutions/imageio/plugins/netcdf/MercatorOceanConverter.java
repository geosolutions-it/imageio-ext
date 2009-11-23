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
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
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
public class MercatorOceanConverter {

    private float xmin = Float.NaN;

    private float ymin = Float.NaN;

    private float xmax = Float.NaN;

    private float ymax = Float.NaN;

    private float periodX = Float.NaN;

    private float periodY = Float.NaN;

    private final static String TIME_ORIGIN = "bulletin_date";

    private final static String FORECAST_DAY = "forecast_range";

    private final static ArrayList<String> VARIABLES = new ArrayList<String>(4);
    static {
        VARIABLES.add("temperature");
        VARIABLES.add("salinity");
        VARIABLES.add("u");
        VARIABLES.add("v");
        VARIABLES.add("kz");
        VARIABLES.add("ssh");
        VARIABLES.add("mlp");
        VARIABLES.add("taux");
        VARIABLES.add("tauy");
        VARIABLES.add("qtot");
        VARIABLES.add("emp");
        VARIABLES.add("qsr");
        VARIABLES.add("hice");
        VARIABLES.add("fice");
        VARIABLES.add("uice");
        VARIABLES.add("hice");
        NUMVARS = VARIABLES.size();
    }

    final static String UNITS = "units";

    private final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.processing.node.worker.netcdf");

    public MercatorOceanConverter() {
        ;
    }

    private static final String fileNameIn = "C:\\Work\\data\\rixen\\lsvc08\\mercator-ocean\\PSY2V3R1\\20080924\\ext-mercatorPsy2v3R1v_med_mean_20080926_R20080924.nc";

    private static final String fileNameOut = "C:/work/data/rixen/converted/re_converted_ext-mercatorPsy2v3R1v_med_mean_20080926_R20080924.nc";

    final static int NUMVARS;

    public static void main(String[] args) throws IOException {
        MercatorOceanConverter converter = new MercatorOceanConverter();
        String fileNameIn = MercatorOceanConverter.fileNameIn;
        String fileNameOut = MercatorOceanConverter.fileNameOut;
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
            // input dimensions
            final Dimension latOriginalDim = ncFileIn
                    .findDimension(NetCDFUtilities.LATITUDE);
            final Dimension lonOriginalDim = ncFileIn
                    .findDimension(NetCDFUtilities.LONGITUDE);

            // input VARIABLES
            // Variable time_0 = ncFileIn.findVariable("time");
            // Array time_0_Data = time_0.read();
            // Index time_0_Index = time_0_Data.getIndex();

            final Variable lonOriginalVar = ncFileIn
                    .findVariable(NetCDFUtilities.LONGITUDE);
            final int nLon = lonOriginalDim.getLength();

            final Variable latOriginalVar = ncFileIn
                    .findVariable(NetCDFUtilities.LATITUDE);
            final int nLat = latOriginalDim.getLength();

            final Array latOriginalData = latOriginalVar.read();
            final Index latOriginalIndex = latOriginalData.getIndex();

            final Array lonOriginalData = lonOriginalVar.read();
            final Index lonOriginalIndex = lonOriginalData.getIndex();

            final Variable depthOriginalVar = ncFileIn
                    .findVariable(NetCDFUtilities.DEPTH); // Depth
            final int nLevels = depthOriginalVar.getDimension(0).getLength();
            final Array depthOriginalData = depthOriginalVar.read();

            // Dimension timeDim = ncFileOut.addDimension("time",
            // timeDim0.getLength());
            Dimension latDim = ncFileOut
                    .addDimension(NetCDFUtilities.LAT, nLat);
            Dimension lonDim = ncFileOut
                    .addDimension(NetCDFUtilities.LON, nLon);
            Dimension depthDim = ncFileOut.addDimension(NetCDFUtilities.DEPTH,
                    nLevels);
            Dimension timeDim = ncFileOut.addDimension(NetCDFUtilities.TIME, 1);

            // writing file
            computeMatrixExtremes(latOriginalData, lonOriginalData, nLon, nLat,
                    latOriginalIndex, lonOriginalIndex);

            NetCDFConverterUtilities.copyGlobalAttributes(ncFileOut, ncFileIn
                    .getGlobalAttributes());

            // //
            //
            // Time requires a special Management
            //
            // //

            // time Variable
            Variable timeVar = ncFileOut.addVariable(NetCDFUtilities.TIME,
                    DataType.FLOAT, new Dimension[] { timeDim });
            Attribute referenceTime = ncFileIn.findGlobalAttribute(TIME_ORIGIN);
            Attribute forecastDate = ncFileIn.findGlobalAttribute(FORECAST_DAY);
            int numDay = 0;
            if (referenceTime != null && forecastDate != null) {
                numDay = setTime(ncFileOut, referenceTime, forecastDate);
            }

            // final float referenceTime = setTimeVariableAttributes(timeVar,
            // ncFileOut);

            // lat Variable
            ArrayFloat latDestData = new ArrayFloat(new int[] { latOriginalDim
                    .getLength() });
            Index latDestIndex = latDestData.getIndex();
            ncFileOut.addVariable(NetCDFUtilities.LAT, DataType.FLOAT,
                    new Dimension[] { latDim });

            // NOTE: A Strange BUG (I guess)
            // when you copy attributes from old vars to new vars, it overwrite
            // coordvars!!!

            ncFileOut.addVariableAttribute(NetCDFUtilities.LAT, "long_name",
                    NetCDFUtilities.LATITUDE);
            ncFileOut.addVariableAttribute(NetCDFUtilities.LAT, UNITS,
                    latOriginalVar.getUnitsString());
            // NetCDFConverterUtilities.setVariableAttributes(latOriginalVar,
            // ncFileOut, NetCDFUtilities.LAT,
            // new String[] { "long_name", UNITS, "step" , "axis"});
            for (int yPos = 0; yPos < latOriginalDim.getLength(); yPos++) {
                latDestData
                        .setFloat(
                                latDestIndex.set(yPos),
                                new Float(
                                        this.ymax
                                                - (new Float(yPos).floatValue() * this.periodY))
                                        .floatValue());
            }

            // lon Variable
            ArrayFloat lonDestData = new ArrayFloat(new int[] { lonOriginalDim
                    .getLength() });
            Index lonDestIndex = lonDestData.getIndex();
            ncFileOut.addVariable(NetCDFUtilities.LON, DataType.FLOAT,
                    new Dimension[] { lonDim });
            ncFileOut.addVariableAttribute(NetCDFUtilities.LON, "long_name",
                    NetCDFUtilities.LONGITUDE);
            ncFileOut.addVariableAttribute(NetCDFUtilities.LON, UNITS,
                    lonOriginalVar.getUnitsString());
            // NetCDFConverterUtilities.setVariableAttributes(lonOriginalVar,
            // ncFileOut, NetCDFUtilities.LON,
            // new String[] { "long_name", UNITS, "step", "axis"});
            for (int xPos = 0; xPos < lonOriginalDim.getLength(); xPos++) {
                lonDestData
                        .setFloat(
                                lonDestIndex.set(xPos),
                                new Float(
                                        this.xmin
                                                + (new Float(xPos).floatValue() * this.periodX))
                                        .floatValue());
            }

            // depth level Variable
            ArrayFloat depthDestData = new ArrayFloat(new int[] { depthDim
                    .getLength() });
            Index depthDestIndex = depthDestData.getIndex();
            ncFileOut.addVariable(NetCDFUtilities.DEPTH, DataType.FLOAT,
                    new Dimension[] { depthDim });
            ncFileOut.addVariableAttribute(NetCDFUtilities.DEPTH, "long_name",
                    NetCDFUtilities.DEPTH);
            ncFileOut.addVariableAttribute(NetCDFUtilities.DEPTH, UNITS,
                    depthOriginalVar.getUnitsString());
            final Attribute positiveAttrib = depthOriginalVar
                    .findAttribute("positive");
            String positiveValue = "down";
            if (positiveAttrib != null)
                positiveValue = positiveAttrib.getStringValue();

            ncFileOut.addVariableAttribute(NetCDFUtilities.DEPTH, "positive",
                    positiveValue);

            for (int wPos = 0; wPos < depthDim.getLength(); wPos++) {
                depthDestData.setFloat(depthDestIndex.set(wPos),
                        depthOriginalData.getFloat(depthDestIndex));
            }

            int numVars = 0;
            final ArrayList<String> variables = new ArrayList<String>(5);

            // {} Variables
            for (int i = 0; i < NUMVARS; i++) {
                String varName = (String) VARIABLES.get(i);
                Variable var = ncFileIn.findVariable(varName);
                if (var != null) {
                    variables.add(varName);
                    boolean hasLocalDepth = NetCDFConverterUtilities
                            .hasThisDimension(var, "depth");
                    if (hasLocalDepth)
                        ncFileOut.addVariable(varName, var.getDataType(),
                                new Dimension[] { timeDim, depthDim, latDim,
                                        lonDim });
                    else
                        ncFileOut.addVariable(varName, var.getDataType(),
                                new Dimension[] { timeDim, latDim, lonDim });

                    NetCDFConverterUtilities.setVariableAttributes(var,
                            ncFileOut, new String[] { "positions" });
                    numVars++;
                }

            }
            // writing bin data ...

            ncFileOut.create();

            ArrayFloat timeData = new ArrayFloat(new int[] { timeDim
                    .getLength() });
            Index timeIndex = timeData.getIndex();
            timeData.setFloat(timeIndex.set(0), numDay);

            ncFileOut.write(NetCDFUtilities.TIME, timeData);
            timeDim.addCoordinateVariable(timeVar);

            Variable latitudeVar = ncFileOut.findVariable(NetCDFUtilities.LAT);
            latDim.addCoordinateVariable(latitudeVar);
            ncFileOut.write(NetCDFUtilities.LAT, latDestData);

            Variable longitudeVar = ncFileOut.findVariable(NetCDFUtilities.LON);
            lonDim.addCoordinateVariable(longitudeVar);
            ncFileOut.write(NetCDFUtilities.LON, lonDestData);

            Variable depthVar = ncFileOut.findVariable(NetCDFUtilities.DEPTH);
            depthDim.addCoordinateVariable(depthVar);
            ncFileOut.write(NetCDFUtilities.DEPTH, depthDestData);

            for (int i = 0; i < numVars; i++) {
                final String varName = (String) variables.get(i);
                final Variable var = ncFileIn.findVariable(varName);
                final boolean hasLocalDepth = NetCDFConverterUtilities
                        .hasThisDimension(var, "depth");

                final Array originalData = var.read();
                Index varIndex = originalData.getIndex();
                final DataType varDataType = var.getDataType();
                final Attribute fv = var.findAttribute("_FillValue");
                float fillValue = Float.NaN;
                if (fv != null) {
                    fillValue = (fv.getNumericValue()).floatValue();
                }

                Array destArray = null;
                int[] dimensions = null;
                if (hasLocalDepth) {
                    dimensions = new int[] { timeDim.getLength(),
                            depthDim.getLength(), nLat, nLon };

                } else {
                    dimensions = new int[] { timeDim.getLength(), nLat, nLon };
                }
                destArray = NetCDFConverterUtilities.getArray(dimensions,
                        varDataType);
                Index destIndex = destArray.getIndex();

                ArrayFloat tempData = new ArrayFloat(
                        new int[] { latOriginalDim.getLength(),
                                lonOriginalDim.getLength() });
                Index tempIndex = tempData.getIndex();

                if (hasLocalDepth) {
                    for (int levelPos = 0; levelPos < depthDim.getLength(); levelPos++) {
                        for (int yPos = 0; yPos < latOriginalDim.getLength(); yPos++) {
                            for (int xPos = 0; xPos < lonOriginalDim
                                    .getLength(); xPos++) {
                                tempData.setFloat(tempIndex.set(yPos, xPos),
                                        originalData.getFloat(varIndex.set(
                                                levelPos, yPos, xPos)));
                            }
                        }

                        final WritableRaster outData = Resampler(
                                latOriginalData, lonOriginalData,
                                lonOriginalDim.getLength(), latOriginalDim
                                        .getLength(), 2, tempData, fillValue);
                        for (int j = 0; j < latOriginalDim.getLength(); j++) {
                            for (int k = 0; k < lonOriginalDim.getLength(); k++) {
                                float sample = outData.getSampleFloat(k, j, 0);
                                destArray.setFloat(destIndex.set(0, levelPos,
                                        j, k), sample);
                            }
                        }

                    }
                } else {
                    for (int yPos = 0; yPos < latOriginalDim.getLength(); yPos++) {
                        for (int xPos = 0; xPos < lonOriginalDim.getLength(); xPos++) {
                            tempData.setFloat(tempIndex.set(yPos, xPos),
                                    originalData.getFloat(varIndex.set(yPos,
                                            xPos)));
                        }
                    }

                    final WritableRaster outData = Resampler(latOriginalData,
                            lonOriginalData, lonOriginalDim.getLength(),
                            latOriginalDim.getLength(), 2, tempData, fillValue);
                    for (int j = 0; j < latOriginalDim.getLength(); j++) {
                        for (int k = 0; k < lonOriginalDim.getLength(); k++) {
                            float sample = outData.getSampleFloat(k, j, 0);
                            destArray.setFloat(destIndex.set(0, j, k), sample);
                        }
                    }

                }
                // }
                ncFileOut.write(varName, destArray);
            }
            ncFileOut.close();
            outputFile.renameTo(fileOut);
        } catch (Exception e) {
            // something bad happened
            if (LOGGER.isLoggable(Level.INFO))
                LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            JAI.getDefaultInstance().getTileCache().flush();
        }

    }

    private int setTime(NetcdfFileWriteable ncFileOut,
            final Attribute referenceTime, final Attribute forecastDate) {
        final String timeOrigin = referenceTime.getStringValue();
        final String forecastDays = forecastDate.getStringValue();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setDateFormatSymbols(new DateFormatSymbols(Locale.CANADA));
        int numDay = 0;
        Date startDate;
        try {
            startDate = sdf.parse(timeOrigin);
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(startDate);
            final String year = Integer.toString(calendar.get(Calendar.YEAR));
            final String month = Integer
                    .toString((calendar.get(Calendar.MONTH) + 1));
            final String day = Integer.toString(calendar
                    .get(Calendar.DAY_OF_MONTH));
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

            final StringBuffer sbTime = new StringBuffer(year).append("-")
                    .append(month).append("-").append(day).append(" ").append(
                            hour).append(":").append(minute).append(":")
                    .append(second).append(".").append(millisecond);
            ncFileOut.addVariableAttribute(NetCDFUtilities.TIME, "units",
                    "days since " + sbTime.toString());
            ncFileOut.addVariableAttribute(NetCDFUtilities.TIME, "long_name",
                    "time");

        } catch (ParseException e) {

        }

        if (forecastDays != null) {
            final int index = forecastDays.indexOf("-day_forecast");
            if (index != -1) {
                numDay = Integer.parseInt(forecastDays.substring(0, index));

            }
            return numDay;
        } else
            throw new IllegalArgumentException("Unable to find forecast day");
    }

    /**
     * Disposing all the of objects created along the path and interrupting
     * Thread.
     */
    protected synchronized void dispose() {
        LOGGER.info("Disposing MercatorOceanConverter...");
        LOGGER.info("Disposing MercatorOceanConverter... Done!");
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
            this.periodX = rangeX / (imageWidth - 1);
            this.periodY = rangeY / (imageHeight - 1);

            System.out.println(this.xmin + ":" + this.ymin + " - " + this.xmax
                    + ":" + this.ymax + " / " + this.periodX + ":"
                    + this.periodY);
        }
    }

}