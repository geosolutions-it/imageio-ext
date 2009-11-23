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

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayShort;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

public class NetCDFConverterUtilities {

    public final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.imageio.plugins.netcdf");

    private NetCDFConverterUtilities() {
    }

    public static void setVariableAttributes(Variable variable,
            NetcdfFileWriteable writableFile, final String newVarName,
            String[] exceptions) {
        List<Attribute> attributes = variable.getAttributes();
        String name = newVarName;
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

    public static void setVariableAttributes(Variable variable,
            NetcdfFileWriteable writableFile) {
        setVariableAttributes(variable, writableFile, variable.getName(), null);
    }

    public static void setVariableAttributes(Variable variable,
            NetcdfFileWriteable writableFile, final String newVarName) {
        setVariableAttributes(variable, writableFile, newVarName, null);
    }

    public static void setVariableAttributes(Variable variable,
            NetcdfFileWriteable writableFile, String[] exceptions) {
        setVariableAttributes(variable, writableFile, variable.getName(),
                exceptions);
    }

    public static void copyGlobalAttributes(NetcdfFileWriteable writableFile,
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

    public static int JGREG = 15 + 31 * (10 + 12 * 1582);

    public static double HALFSECOND = 0.5;

    public static GregorianCalendar fromJulian(double injulian) {
        int jalpha, ja, jb, jc, jd, je, year, month, day;
        double julian = injulian + HALFSECOND / 86400.0;
        ja = (int) injulian;
        if (ja >= JGREG) {
            jalpha = (int) (((ja - 1867216) - 0.25) / 36524.25);
            ja = ja + 1 + jalpha - jalpha / 4;
        }

        jb = ja + 1524;
        jc = (int) (6680.0 + ((jb - 2439870) - 122.1) / 365.25);
        jd = 365 * jc + jc / 4;
        je = (int) ((jb - jd) / 30.6001);
        day = jb - jd - (int) (30.6001 * je);
        month = je - 1;
        if (month > 12)
            month = month - 12;
        year = jc - 4715;
        if (month > 2)
            year--;
        if (year <= 0)
            year--;

        // Calendar Months are 0 based
        return new GregorianCalendar(year, month - 1, day);
    }

    public static GregorianCalendar fromModifiedJulian(final double injulian,
            final String long_name, final String units) {
        final SimpleDateFormat sdf = new SimpleDateFormat(
                "dd-MMM-yyyy HH:mm:ss");
        sdf.setDateFormatSymbols(new DateFormatSymbols(Locale.US));

        Date startDate;
        try {
            startDate = sdf.parse(long_name.substring(long_name.length()
                    - "dd-MMM-yyyy HH:mm:ss".length(), long_name.length()));
        } catch (ParseException e) {
            return null;
        }

        final GregorianCalendar calendar = new GregorianCalendar();

        if (units.equalsIgnoreCase("days")) {
            // Julian day
            double jd = Math.floor(injulian);

            // Integer Julian day
            int jdi = (int) Math.floor(jd);

            // Fractional part of day
            double jdf = jd - jdi + 0.5;

            // Really the next calendar day?
            if (jdf >= 1.0) {
                jdf = jdf - 1.0;
                jdi = jdi + 1;
            }

            int hour = (int) (jdf * 24.0);

            calendar.setTime(startDate);
            calendar.add(GregorianCalendar.DAY_OF_MONTH, jdi);
            calendar.add(GregorianCalendar.HOUR, hour);
        }

        // Calendar Months are 0 based
        return calendar;
    }

    public static Array getArray(final int dimension,
            final DataType navLatDataType) {
        if (dimension < 1)
            throw new IllegalArgumentException(
                    "dimension should be greater than zero");
        int[] dim = new int[] { dimension };
        if (navLatDataType == DataType.FLOAT)
            return new ArrayFloat(dim);
        else if (navLatDataType == DataType.DOUBLE)
            return new ArrayDouble(dim);
        else if (navLatDataType == DataType.BYTE)
            return new ArrayByte(dim);
        else if (navLatDataType == DataType.SHORT)
            return new ArrayShort(dim);
        else if (navLatDataType == DataType.INT)
            return new ArrayInt(dim);
        throw new IllegalArgumentException("Actually unsupported Datatype");

    }

    public static void setData1D(Array originalData, Array destinationData,
            final DataType navLatDataType, final int maxIndex,
            final boolean flipData) {
        Index originalDataIndex = originalData.getIndex();
        Index destinationDataIndex = flipData ? destinationData.getIndex()
                : originalDataIndex;

        if (navLatDataType == DataType.FLOAT) {
            for (int pos = 0; pos < maxIndex; pos++) {
                float f = originalData.getFloat(originalDataIndex.set(pos));
                // Flipping latitudes
                if (flipData) {
                    destinationDataIndex.set(maxIndex - pos - 1);
                }
                destinationData.setFloat(destinationDataIndex, f);
            }
        } else if (navLatDataType == DataType.DOUBLE) {
            for (int pos = 0; pos < maxIndex; pos++) {
                double d = originalData.getDouble(originalDataIndex.set(pos));
                // Flipping latitudes
                if (flipData) {
                    destinationDataIndex.set(maxIndex - pos - 1);
                }
                destinationData.setDouble(destinationDataIndex, d);
            }
        } else if (navLatDataType == DataType.BYTE) {
            for (int pos = 0; pos < maxIndex; pos++) {
                byte b = originalData.getByte(originalDataIndex.set(pos));
                // Flipping latitudes
                if (flipData) {
                    destinationDataIndex.set(maxIndex - pos - 1);
                }
                destinationData.setByte(destinationDataIndex, b);
            }
        } else if (navLatDataType == DataType.SHORT) {
            for (int pos = 0; pos < maxIndex; pos++) {
                short s = originalData.getShort(originalDataIndex.set(pos));
                // Flipping latitudes
                if (flipData) {
                    destinationDataIndex.set(maxIndex - pos - 1);
                }
                destinationData.setShort(destinationDataIndex, s);
            }
        } else if (navLatDataType == DataType.INT) {
            for (int pos = 0; pos < maxIndex; pos++) {
                int i = originalData.getInt(originalDataIndex.set(pos));
                // Flipping latitudes
                if (flipData) {
                    destinationDataIndex.set(maxIndex - pos - 1);
                }
                destinationData.setInt(destinationDataIndex, i);
            }
        }
    }

    public static Array getRangeArray(DataType varDataType) {
        int[] dim = new int[] { 2 };
        if (varDataType == DataType.FLOAT) {
            Array array = new ArrayFloat(dim);
            Index index = array.getIndex();
            array.setFloat(index.set(0), Float.MIN_VALUE);
            array.setFloat(index.set(1), Float.MAX_VALUE);
            return array;
        } else if (varDataType == DataType.DOUBLE) {
            Array array = new ArrayDouble(dim);
            Index index = array.getIndex();
            array.setDouble(index.set(0), Double.MIN_VALUE);
            array.setDouble(index.set(1), Double.MAX_VALUE);
            return array;
        } else if (varDataType == DataType.BYTE) {
            Array array = new ArrayByte(dim);
            Index index = array.getIndex();
            array.setByte(index.set(0), Byte.MIN_VALUE);
            array.setByte(index.set(1), Byte.MAX_VALUE);
            return array;
        } else if (varDataType == DataType.SHORT) {
            Array array = new ArrayShort(dim);
            Index index = array.getIndex();
            array.setShort(index.set(0), Short.MIN_VALUE);
            array.setShort(index.set(1), Short.MAX_VALUE);
            return array;
        } else if (varDataType == DataType.INT) {
            Array array = new ArrayInt(dim);
            Index index = array.getIndex();
            array.setInt(index.set(0), Integer.MIN_VALUE);
            array.setInt(index.set(1), Integer.MAX_VALUE);
            return array;
        }
        throw new IllegalArgumentException("Actually unsupported Datatype");
    }

    public static Array getArray(int[] dimensions, DataType varDataType) {
        if (dimensions == null)
            throw new IllegalArgumentException("Illegal dimensions");
        final int nDims = dimensions.length;
        switch (nDims) {
        case 4:
            if (varDataType == DataType.FLOAT) {
                return new ArrayFloat.D4(dimensions[0], dimensions[1],
                        dimensions[2], dimensions[3]);
            } else if (varDataType == DataType.DOUBLE) {
                return new ArrayDouble.D4(dimensions[0], dimensions[1],
                        dimensions[2], dimensions[3]);
            } else if (varDataType == DataType.BYTE) {
                return new ArrayByte.D4(dimensions[0], dimensions[1],
                        dimensions[2], dimensions[3]);
            } else if (varDataType == DataType.SHORT) {
                return new ArrayShort.D4(dimensions[0], dimensions[1],
                        dimensions[2], dimensions[3]);
            } else if (varDataType == DataType.INT) {
                return new ArrayInt.D4(dimensions[0], dimensions[1],
                        dimensions[2], dimensions[3]);
            } else
                throw new IllegalArgumentException(
                        "Actually unsupported Datatype");

        case 3:
            if (varDataType == DataType.FLOAT) {
                return new ArrayFloat.D3(dimensions[0], dimensions[1],
                        dimensions[2]);
            } else if (varDataType == DataType.DOUBLE) {
                return new ArrayDouble.D3(dimensions[0], dimensions[1],
                        dimensions[2]);
            } else if (varDataType == DataType.BYTE) {
                return new ArrayByte.D3(dimensions[0], dimensions[1],
                        dimensions[2]);
            } else if (varDataType == DataType.SHORT) {
                return new ArrayShort.D3(dimensions[0], dimensions[1],
                        dimensions[2]);
            } else if (varDataType == DataType.INT) {
                return new ArrayInt.D3(dimensions[0], dimensions[1],
                        dimensions[2]);
            } else
                throw new IllegalArgumentException(
                        "Actually unsupported Datatype");
        }
        throw new IllegalArgumentException(
                "Unable to create a proper array unsupported Datatype");
    }

    public static void writeData(NetcdfFileWriteable ncFileOut,
            final String varName, Variable var, final Array originalVarData,
            final Array destArray, final boolean findNewRange,
            final boolean updateFillValue, final int[] loopLengths,
            final boolean flipY) throws IOException, InvalidRangeException {
        final int nestedLoops = loopLengths.length;
        final boolean setDepth = nestedLoops > 3;
        final int timePositions = loopLengths[0]; // timeDim
        final int depthPositions;
        final int latPositions;
        final int lonPositions;
        if (setDepth) {
            depthPositions = loopLengths[1];
            latPositions = loopLengths[2];
            lonPositions = loopLengths[3];
        } else {
            depthPositions = -1;
            latPositions = loopLengths[1];
            lonPositions = loopLengths[2];
        }

        final DataType varDataType = var.getDataType();
        Attribute fv = null;
        if (updateFillValue)
            fv = var
                    .findAttribute(NetCDFUtilities.DatasetAttribs.MISSING_VALUE);
        else
            fv = var.findAttribute(NetCDFUtilities.DatasetAttribs.FILL_VALUE);
        Index varIndex = originalVarData.getIndex();
        Index destIndex = destArray.getIndex();

        // //
        //
        // FLOAT
        //
        // //
        if (varDataType == DataType.FLOAT) {
            float min = Float.MAX_VALUE;
            float max = Float.MIN_VALUE;
            float fillValue = Float.MAX_VALUE;
            if (fv != null) {
                fillValue = (fv.getNumericValue()).floatValue();
            }

            if (setDepth) {
                for (int tPos = 0; tPos < timePositions; tPos++) {
                    for (int levelPos = 0; levelPos < depthPositions; levelPos++) {
                        for (int yPos = 0; yPos < latPositions; yPos++) {
                            for (int xPos = 0; xPos < lonPositions; xPos++) {
                                float sVal = originalVarData.getFloat(varIndex
                                        .set(tPos, levelPos, yPos, xPos));
                                if (findNewRange) {
                                    if (sVal >= max && sVal != fillValue)
                                        max = sVal;
                                    if (sVal <= min && sVal != fillValue)
                                        min = sVal;
                                }
                                int newYpos = yPos;
                                // Flipping y
                                if (flipY) {
                                    newYpos = latPositions - yPos - 1;
                                }
                                destArray.setFloat(destIndex.set(tPos,
                                        levelPos, newYpos, xPos), sVal);
                            }
                        }
                    }
                }
            } else {
                for (int tPos = 0; tPos < timePositions; tPos++) {
                    for (int yPos = 0; yPos < latPositions; yPos++) {
                        for (int xPos = 0; xPos < lonPositions; xPos++) {
                            float sVal = originalVarData.getFloat(varIndex.set(
                                    tPos, yPos, xPos));
                            if (findNewRange) {
                                if (sVal >= max && sVal != fillValue)
                                    max = sVal;
                                if (sVal <= min && sVal != fillValue)
                                    min = sVal;
                            }
                            // Flipping y
                            int newYpos = yPos;
                            // Flipping y
                            if (flipY) {
                                newYpos = latPositions - yPos - 1;
                            }
                            destArray.setFloat(destIndex.set(tPos, newYpos,
                                    xPos), sVal);
                        }
                    }
                }
            }
            ncFileOut.write(varName, destArray);
            if (findNewRange) {
                Array range = NetCDFConverterUtilities
                        .getRangeArray(varDataType);

                Index index = range.getIndex();
                range.setFloat(index.set(0), min);
                range.setFloat(index.set(1), max);
                ncFileOut.updateAttribute(ncFileOut.findVariable(varName),
                        new Attribute(
                                NetCDFUtilities.DatasetAttribs.VALID_RANGE,
                                range));
            }
            
            if (updateFillValue){
                ncFileOut.updateAttribute(ncFileOut.findVariable(varName),
                        new Attribute(
                                NetCDFUtilities.DatasetAttribs.FILL_VALUE,
                                new Float(fillValue)));
            }
            // //
            //
            // DOUBLE
            //
            // //
        } else if (varDataType == DataType.DOUBLE) {
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            double fillValue = Double.MAX_VALUE;
            if (fv != null) {
                fillValue = (fv.getNumericValue()).doubleValue();
            }

            if (setDepth) {
                for (int tPos = 0; tPos < timePositions; tPos++) {
                    for (int levelPos = 0; levelPos < depthPositions; levelPos++) {
                        for (int yPos = 0; yPos < latPositions; yPos++) {
                            for (int xPos = 0; xPos < lonPositions; xPos++) {
                                double sVal = originalVarData
                                        .getDouble(varIndex.set(tPos, levelPos,
                                                yPos, xPos));
                                if (findNewRange) {
                                    if (sVal >= max && sVal != fillValue)
                                        max = sVal;
                                    if (sVal <= min && sVal != fillValue)
                                        min = sVal;
                                }
                                int newYpos = yPos;
                                // Flipping y
                                if (flipY) {
                                    newYpos = latPositions - yPos - 1;
                                }
                                destArray.setDouble(destIndex.set(tPos,
                                        levelPos, newYpos, xPos), sVal);
                            }
                        }
                    }
                }
            } else {
                for (int tPos = 0; tPos < timePositions; tPos++) {
                    for (int yPos = 0; yPos < latPositions; yPos++) {
                        for (int xPos = 0; xPos < lonPositions; xPos++) {
                            double sVal = originalVarData.getDouble(varIndex
                                    .set(tPos, yPos, xPos));
                            if (findNewRange) {
                                if (sVal >= max && sVal != fillValue)
                                    max = sVal;
                                if (sVal <= min && sVal != fillValue)
                                    min = sVal;
                            }
                            // Flipping y
                            int newYpos = yPos;
                            // Flipping y
                            if (flipY) {
                                newYpos = latPositions - yPos - 1;
                            }
                            destArray.setDouble(destIndex.set(tPos, newYpos,
                                    xPos), sVal);
                        }
                    }
                }
            }
            ncFileOut.write(varName, destArray);
            if (findNewRange) {
                Array range = NetCDFConverterUtilities
                        .getRangeArray(varDataType);

                Index index = range.getIndex();
                range.setDouble(index.set(0), min);
                range.setDouble(index.set(1), max);
                ncFileOut.updateAttribute(ncFileOut.findVariable(varName),
                        new Attribute(
                                NetCDFUtilities.DatasetAttribs.VALID_RANGE,
                                range));
            }
            if (updateFillValue){
                ncFileOut.updateAttribute(ncFileOut.findVariable(varName),
                        new Attribute(
                                NetCDFUtilities.DatasetAttribs.FILL_VALUE,
                                new Double(fillValue)));
            }
            
            // //
            //
            // BYTE
            //
            // //
        } else if (varDataType == DataType.BYTE) {
            byte min = Byte.MAX_VALUE;
            byte max = Byte.MIN_VALUE;
            byte fillValue = Byte.MAX_VALUE;
            if (fv != null) {
                fillValue = (fv.getNumericValue()).byteValue();
            }

            if (setDepth) {
                for (int tPos = 0; tPos < timePositions; tPos++) {
                    for (int levelPos = 0; levelPos < depthPositions; levelPos++) {
                        for (int yPos = 0; yPos < latPositions; yPos++) {
                            for (int xPos = 0; xPos < lonPositions; xPos++) {
                                byte sVal = originalVarData.getByte(varIndex
                                        .set(tPos, levelPos, yPos, xPos));
                                if (findNewRange) {
                                    if (sVal >= max && sVal != fillValue)
                                        max = sVal;
                                    if (sVal <= min && sVal != fillValue)
                                        min = sVal;
                                }
                                int newYpos = yPos;
                                // Flipping y
                                if (flipY) {
                                    newYpos = latPositions - yPos - 1;
                                }
                                destArray.setByte(destIndex.set(tPos, levelPos,
                                        newYpos, xPos), sVal);
                            }
                        }
                    }
                }
            } else {
                for (int tPos = 0; tPos < timePositions; tPos++) {
                    for (int yPos = 0; yPos < latPositions; yPos++) {
                        for (int xPos = 0; xPos < lonPositions; xPos++) {
                            byte sVal = originalVarData.getByte(varIndex.set(
                                    tPos, yPos, xPos));
                            if (findNewRange) {
                                if (sVal >= max && sVal != fillValue)
                                    max = sVal;
                                if (sVal <= min && sVal != fillValue)
                                    min = sVal;
                            }
                            // Flipping y
                            int newYpos = yPos;
                            // Flipping y
                            if (flipY) {
                                newYpos = latPositions - yPos - 1;
                            }
                            destArray.setByte(destIndex
                                    .set(tPos, newYpos, xPos), sVal);
                        }
                    }
                }
            }
            ncFileOut.write(varName, destArray);
            if (findNewRange) {
                Array range = NetCDFConverterUtilities
                        .getRangeArray(varDataType);

                Index index = range.getIndex();
                range.setByte(index.set(0), min);
                range.setByte(index.set(1), max);
                ncFileOut.updateAttribute(ncFileOut.findVariable(varName),
                        new Attribute(
                                NetCDFUtilities.DatasetAttribs.VALID_RANGE,
                                range));
            }
            if (updateFillValue){
                ncFileOut.updateAttribute(ncFileOut.findVariable(varName),
                        new Attribute(
                                NetCDFUtilities.DatasetAttribs.FILL_VALUE,
                                new Byte(fillValue)));
            }

            // //
            //
            // SHORT
            //
            // //
        } else if (varDataType == DataType.SHORT) {
            short min = Short.MAX_VALUE;
            short max = Short.MIN_VALUE;
            short fillValue = Short.MAX_VALUE;
            if (fv != null) {
                fillValue = (fv.getNumericValue()).shortValue();
            }

            if (setDepth) {
                for (int tPos = 0; tPos < timePositions; tPos++) {
                    for (int levelPos = 0; levelPos < depthPositions; levelPos++) {
                        for (int yPos = 0; yPos < latPositions; yPos++) {
                            for (int xPos = 0; xPos < lonPositions; xPos++) {
                                short sVal = originalVarData.getShort(varIndex
                                        .set(tPos, levelPos, yPos, xPos));
                                if (findNewRange) {
                                    if (sVal >= max && sVal != fillValue)
                                        max = sVal;
                                    if (sVal <= min && sVal != fillValue)
                                        min = sVal;
                                }
                                int newYpos = yPos;
                                // Flipping y
                                if (flipY) {
                                    newYpos = latPositions - yPos - 1;
                                }
                                destArray.setShort(destIndex.set(tPos,
                                        levelPos, newYpos, xPos), sVal);
                            }
                        }
                    }
                }
            } else {
                for (int tPos = 0; tPos < timePositions; tPos++) {
                    for (int yPos = 0; yPos < latPositions; yPos++) {
                        for (int xPos = 0; xPos < lonPositions; xPos++) {
                            short sVal = originalVarData.getShort(varIndex.set(
                                    tPos, yPos, xPos));
                            if (findNewRange) {
                                if (sVal >= max && sVal != fillValue)
                                    max = sVal;
                                if (sVal <= min && sVal != fillValue)
                                    min = sVal;
                            }
                            // Flipping y
                            int newYpos = yPos;
                            // Flipping y
                            if (flipY) {
                                newYpos = latPositions - yPos - 1;
                            }
                            destArray.setShort(destIndex.set(tPos, newYpos,
                                    xPos), sVal);
                        }
                    }
                }
            }
            ncFileOut.write(varName, destArray);
            if (findNewRange) {
                Array range = NetCDFConverterUtilities
                        .getRangeArray(varDataType);

                Index index = range.getIndex();
                range.setShort(index.set(0), min);
                range.setShort(index.set(1), max);
                ncFileOut.updateAttribute(ncFileOut.findVariable(varName),
                        new Attribute(
                                NetCDFUtilities.DatasetAttribs.VALID_RANGE,
                                range));
            }
            if (updateFillValue){
                ncFileOut.updateAttribute(ncFileOut.findVariable(varName),
                        new Attribute(
                                NetCDFUtilities.DatasetAttribs.FILL_VALUE,
                                new Short(fillValue)));
            }
        }

        // //
        //
        // INTEGER
        //
        // //
        else if (varDataType == DataType.INT) {
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            int fillValue = Integer.MAX_VALUE;
            if (fv != null) {
                fillValue = (fv.getNumericValue()).intValue();
            }

            if (setDepth) {
                for (int tPos = 0; tPos < timePositions; tPos++) {
                    for (int levelPos = 0; levelPos < depthPositions; levelPos++) {
                        for (int yPos = 0; yPos < latPositions; yPos++) {
                            for (int xPos = 0; xPos < lonPositions; xPos++) {
                                int sVal = originalVarData.getInt(varIndex.set(
                                        tPos, levelPos, yPos, xPos));
                                if (findNewRange) {
                                    if (sVal >= max && sVal != fillValue)
                                        max = sVal;
                                    if (sVal <= min && sVal != fillValue)
                                        min = sVal;
                                }
                                int newYpos = yPos;
                                // Flipping y
                                if (flipY) {
                                    newYpos = latPositions - yPos - 1;
                                }
                                destArray.setInt(destIndex.set(tPos, levelPos,
                                        newYpos, xPos), sVal);
                            }
                        }
                    }
                }
            } else {
                for (int tPos = 0; tPos < timePositions; tPos++) {
                    for (int yPos = 0; yPos < latPositions; yPos++) {
                        for (int xPos = 0; xPos < lonPositions; xPos++) {
                            int sVal = originalVarData.getInt(varIndex.set(
                                    tPos, yPos, xPos));
                            if (findNewRange) {
                                if (sVal >= max && sVal != fillValue)
                                    max = sVal;
                                if (sVal <= min && sVal != fillValue)
                                    min = sVal;
                            }
                            // Flipping y
                            int newYpos = yPos;
                            // Flipping y
                            if (flipY) {
                                newYpos = latPositions - yPos - 1;
                            }
                            destArray.setInt(
                                    destIndex.set(tPos, newYpos, xPos), sVal);
                        }
                    }
                }
            }
            ncFileOut.write(varName, destArray);
            if (findNewRange) {
                Array range = NetCDFConverterUtilities
                        .getRangeArray(varDataType);

                Index index = range.getIndex();
                range.setInt(index.set(0), min);
                range.setInt(index.set(1), max);
                ncFileOut.updateAttribute(ncFileOut.findVariable(varName),
                        new Attribute(
                                NetCDFUtilities.DatasetAttribs.VALID_RANGE,
                                range));
            }
            if (updateFillValue){
                ncFileOut.updateAttribute(ncFileOut.findVariable(varName),
                        new Attribute(
                                NetCDFUtilities.DatasetAttribs.FILL_VALUE,
                                new Integer(fillValue)));
            }

        } else
            throw new IllegalArgumentException("Unsupported DataType");
    }

    /**
     * Return true if the provided variable has a
     * 
     * @param var
     * @param dimensionName
     * @return
     */
    public static boolean hasThisDimension(Variable var, String dimensionName) {
        final List<Dimension> dims = var.getDimensions();
        boolean hasDimension = false;
        for (Dimension dim : dims) {
            if (dim.getName().equalsIgnoreCase(dimensionName)) {
                hasDimension = true;
                break;
            }
        }
        return hasDimension;
    }

    public static boolean isFillValueOutsideValidRange(Attribute validMax,
            Attribute validMin, Attribute fillValue, DataType dataType) {
        if (dataType == DataType.FLOAT) {
            final float min = validMin.getNumericValue().floatValue();
            final float max = validMax.getNumericValue().floatValue();
            final float fill = fillValue.getNumericValue().floatValue();
            if (fill == min || fill == max)
                return false;
            else
                return true;
        } else if (dataType == DataType.DOUBLE) {
            final double min = validMin.getNumericValue().doubleValue();
            final double max = validMax.getNumericValue().doubleValue();
            final double fill = fillValue.getNumericValue().doubleValue();
            if (fill == min || fill == max)
                return false;
            else
                return true;
        } else if (dataType == DataType.BYTE) {
            final byte min = validMin.getNumericValue().byteValue();
            final byte max = validMax.getNumericValue().byteValue();
            final byte fill = fillValue.getNumericValue().byteValue();
            if (fill == min || fill == max)
                return false;
            else
                return true;
        } else if (dataType == DataType.SHORT) {
            final short min = validMin.getNumericValue().shortValue();
            final short max = validMax.getNumericValue().shortValue();
            final short fill = fillValue.getNumericValue().shortValue();
            if (fill == min || fill == max)
                return false;
            else
                return true;
        } else if (dataType == DataType.INT) {
            final int min = validMin.getNumericValue().intValue();
            final int max = validMax.getNumericValue().intValue();
            final int fill = fillValue.getNumericValue().intValue();
            if (fill == min || fill == max)
                return false;
            else
                return true;
        }
        throw new IllegalArgumentException("Actually unsupported Datatype");
    }

    public static boolean isFillValueOutsideValidRange(Attribute validRange,
            Attribute fillValue, DataType dataType) {
        Array range = validRange.getValues();
        Index index = range.getIndex();

        if (dataType == DataType.FLOAT) {
            final float min = range.getFloat(index.set(0));
            final float max = range.getFloat(index.set(1));
            final float fill = fillValue.getNumericValue().floatValue();
            if (fill == min || fill == max)
                return false;
            else
                return true;
        } else if (dataType == DataType.DOUBLE) {
            final double min = range.getDouble(index.set(0));
            final double max = range.getDouble(index.set(1));
            final double fill = fillValue.getNumericValue().doubleValue();
            if (fill == min || fill == max)
                return false;
            else
                return true;
        } else if (dataType == DataType.BYTE) {
            final byte min = range.getByte(index.set(0));
            final byte max = range.getByte(index.set(1));
            final byte fill = fillValue.getNumericValue().byteValue();
            if (fill == min || fill == max)
                return false;
            else
                return true;
        } else if (dataType == DataType.SHORT) {
            final short min = range.getShort(index.set(0));
            final short max = range.getShort(index.set(1));
            final short fill = fillValue.getNumericValue().shortValue();
            if (fill == min || fill == max)
                return false;
            else
                return true;
        } else if (dataType == DataType.INT) {
            final int min = range.getInt(index.set(0));
            final int max = range.getInt(index.set(1));
            final int fill = fillValue.getNumericValue().intValue();
            if (fill == min || fill == max)
                return false;
            else
                return true;
        }
        throw new IllegalArgumentException("Actually unsupported Datatype");
    }

    public static Number getNumber(DataType varDataType) {
        if (varDataType == DataType.FLOAT) {
            return new Float(Float.MIN_VALUE);
        } else if (varDataType == DataType.DOUBLE) {
            return new Double(Double.MIN_VALUE);
        } else if (varDataType == DataType.BYTE) {
            return new Byte(Byte.MIN_VALUE);
        } else if (varDataType == DataType.SHORT) {
            return new Short(Short.MIN_VALUE);
        } else if (varDataType == DataType.INT) {
            return new Integer(Integer.MIN_VALUE);
        }
        throw new IllegalArgumentException("Actually unsupported Datatype");
    }
}
