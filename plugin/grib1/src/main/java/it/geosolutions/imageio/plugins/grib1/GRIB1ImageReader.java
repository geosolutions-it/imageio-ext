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
package it.geosolutions.imageio.plugins.grib1;

import it.geosolutions.imageio.ndplugin.BaseImageReader;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities.CheckType;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities.KeyValuePair;
import it.geosolutions.imageio.utilities.ImageIOUtilities;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import ucar.grib.grib1.GribPDSLevel;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayShort;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;

/**
 * {@link GRIB1ImageReader} is a {@link ImageReader} able to create
 * {@link RenderedImage} from GRIB1 sources.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Alessio Fabiani, GeoSolutions
 */
public class GRIB1ImageReader extends BaseImageReader {
	
	private Map<String,Variable> boundsMap = new HashMap<String,Variable>();
	private Map<String,CoordinateAxis> coordSysMap = new HashMap<String,CoordinateAxis>();
	
	private Variable horizontalGrid;
	
    private int numGlobalAttributes;
	
    private NetcdfDataset dataset;
    
    protected final static Logger LOGGER = Logger.getLogger("it.geosolutions.imageio.plugins.grib1");
    
	private HashMap<Range, GribVariableWrapper> indexMap;
	
	class VerticalLevel{
		
		public VerticalLevel(final int levelType, final String levelName, 
				final String levelDescription, final String levelUnits, 
				final boolean hasExplicitVerticalAxis, final String axisType,
				final String positive){
			this.levelType=levelType;
			this.levelDescription=levelDescription;
			this.levelName=levelName;
			this.levelUnits=levelUnits;
			this.hasExplicitVerticalAxis = hasExplicitVerticalAxis;
			this.axisType = axisType;
			this.positive = positive;
		}
		
		public String getAxisType() {
			return axisType;
		}

		public void setAxisType(String axisType) {
			this.axisType = axisType;
		}

		public String getPositive() {
			return positive;
		}

		public void setPositive(String positive) {
			this.positive = positive;
		}

		public void setLevelType(int levelType) {
			this.levelType = levelType;
		}

		public void setLevelDescription(String levelDescription) {
			this.levelDescription = levelDescription;
		}

		public void setLevelName(String levelName) {
			this.levelName = levelName;
		}

		public void setLevelUnits(String levelUnits) {
			this.levelUnits = levelUnits;
		}

		public void setHasExplicitVerticalAxis(boolean hasExplicitVerticalAxis) {
			this.hasExplicitVerticalAxis = hasExplicitVerticalAxis;
		}

		private int levelType;
        private String levelDescription;
		private String levelName;
		private String levelUnits;
        private boolean hasExplicitVerticalAxis;
        private String axisType;
        private String positive;
        
        public boolean isHasExplicitVerticalAxis() {
			return hasExplicitVerticalAxis;
		}
		
		 public int getLevelType() {
				return levelType;
			}
			
			public String getLevelUnits() {
				return levelUnits;
			}

			public String getLevelName() {
				return levelName;
			}
		
		public String getLevelDescription() {
			return levelDescription;
		}
	}
	
	List<KeyValuePair> getCoordinateAttributes(){
		List<KeyValuePair> attribs = Collections.emptyList();
		if (horizontalGrid!=null){
			List<Attribute> attributes = horizontalGrid.getAttributes();
			attribs = new LinkedList<KeyValuePair>();
			for (Attribute attribute: attributes){
				final String attribName = attribute.getName();
				if (attribName.startsWith(GRIB1Utilities.GRIB_PARAM_PREFIX)){
					KeyValuePair kvp = new KeyValuePair(attribName.substring(GRIB1Utilities.GRIB_PARAM_PREFIX.length())
							,NetCDFUtilities.getAttributesAsString(attribute));
					attribs.add(kvp);
				}
			}
		}
		return attribs;
	}
	
	/**
     * A class wrapping a GribRecord and its basic properties and structures
     */
    class GribVariableWrapper {

		private VerticalLevel verticalLevel;
    	
		private Variable variable;

        public Variable getVariable() {
			return variable;
		}

		private String name;

        private int width;

        private int height;

		private String boundName;
		
        private SampleModel sampleModel;

        private int rank;
    	
        public int getRank() {
			return rank;
		}

        private String paramID;

		private List<CoordinateAxis> axes;

		private String parameterUnit;
		
		private Range range;

		private int parameterCenterID;

		private int parameterTableVersion;

		private int parameterNumber;

		private String productDefinitionType;

		private String timeUnits="";

		private String timeName="";

		private String parameterName;
		
		public String getName() {
			return name;
		}

		public String getTimeUnits() {
			return timeUnits;
		}

		public String getTimeName() {
			return timeName;
		}
        /**
         * Constructor of the {@link GribVariableWrapper} class which allows to
         * wrap a grib record related to the input imageIndex
         * @param wrapperRange 
         * 
         * @param imageIndex
         *                the index need to be wrapped by this
         *                {@link GribVariableWrapper}
         */
        public GribVariableWrapper(final Variable variable, final Range range) {
            this.variable = variable;
            this.range = range;
            rank = variable.getRank();
            width = variable.getDimension(rank - NetCDFUtilities.X_DIMENSION).getLength();
            height = variable.getDimension(rank - NetCDFUtilities.Y_DIMENSION).getLength();
            final List<Dimension> dimensions = variable.getDimensions();
            axes = new LinkedList<CoordinateAxis>();
            for (Dimension dim : dimensions){
            	final String dimName = dim.getName();
            	final Variable coordinate = dataset.findVariable(dimName);
            	if (coordinate!=null){
            		CoordinateAxis axis = (CoordinateAxis)coordinate;
            		if (!coordSysMap.containsKey(dimName)){
            			coordSysMap.put(dimName, axis);
            		}
            		axes.add(axis);
            		final Attribute bounds = coordinate.findAttribute(GRIB1Utilities.BOUNDS);
            		if (bounds!=null){
            			final String boundName = bounds.getStringValue();
            			final Variable boundVar = dataset.findVariable(boundName);
            			if (boundVar!=null){
            				this.boundName = boundName;
            				if (!boundsMap.containsKey(boundName)){
            					boundsMap.put(boundName, boundVar);
                    		}		
            			}
            		}
            	}
            }
            
            final int bufferType = NetCDFUtilities.getRawDataType(variable);
          	name = variable.getName();
            sampleModel = new BandedSampleModel(bufferType, width, height, 1);
            initParam();
            initVerticalLevel();
            Variable temporalAxis = getTemporalAxis();
            if (temporalAxis!=null){
            	timeUnits = NetCDFUtilities.getAttributesAsString(temporalAxis, NetCDFUtilities.UNITS);
            	timeName = NetCDFUtilities.getAttributesAsString(temporalAxis, NetCDFUtilities.LONG_NAME);
            }
            
        }

        private void initParam() {
        	productDefinitionType = NetCDFUtilities.getAttributesAsString(variable, GRIB1Utilities.GRIB_PRODUCT_DEFINITION_TYPE);
            parameterName = NetCDFUtilities.getAttributesAsString(variable, GRIB1Utilities.GRIB_PARAM_NAME);
            parameterUnit = NetCDFUtilities.getAttributesAsString(variable, GRIB1Utilities.GRIB_PARAM_UNIT);
          //TODO: REMOVE ME: Testing JSR
//            Unit unit = null;
//            if (parameterUnit.equalsIgnoreCase("fraction") || parameterUnit.equalsIgnoreCase("%") )
//            	unit = Unit.ONE;
//            else if (parameterUnit.equalsIgnoreCase("degrees"))
//        		unit = NonSI.DEGREE_ANGLE;
//            else{
//            	parameterUnit = parameterUnit.replace("^", "");
//            	parameterUnit = parameterUnit.replace(" ", "*");
//            	try{
//            		unit = Unit.valueOf(parameterUnit);
//            	}catch(IllegalArgumentException iae){
//            		System.out.println(dataset.getLocation());
//            	}
//            }
            parameterCenterID = NetCDFUtilities.getAttributesAsNumber(variable, GRIB1Utilities.GRIB_PARAM_CENTER_ID).intValue();
            parameterNumber = NetCDFUtilities.getAttributesAsNumber(variable, GRIB1Utilities.GRIB_PARAM_NUMBER).intValue();
            parameterTableVersion = NetCDFUtilities.getAttributesAsNumber(variable, GRIB1Utilities.GRIB_TABLE_ID).intValue();
			
		}

		/**
         * Setting vertical level 
         */
        private void initVerticalLevel() {
        	final Number levelNum = NetCDFUtilities.getAttributesAsNumber(variable, GRIB1Utilities.GRIB_LEVEL_TYPE);
            int levelType = levelNum != null ? levelNum.intValue() : GRIB1Utilities.UNDEFINED_NUMBER;
            String levelName = GribPDSLevel.getNameShort(levelType);
            String levelUnits = GribPDSLevel.getUnits(levelType);
            String levelDescription = GribPDSLevel.getLevelDescription(levelType);
            boolean hasExplicitVerticalAxis = !GRIB1Utilities.isVerticalLevelSymbolic(levelType);
            String axisType = "";
            String positive = "";
            if (hasExplicitVerticalAxis){
	            final Variable verticalAxis = getVerticalAxis();
	            if (verticalAxis!=null){
	            	axisType = NetCDFUtilities.getAttributesAsString(verticalAxis, NetCDFUtilities.COORDINATE_AXIS_TYPE);
	            	positive = NetCDFUtilities.getAttributesAsString(verticalAxis, NetCDFUtilities.POSITIVE);
	            }
	        }
            
            verticalLevel = new VerticalLevel(levelType,levelName,levelDescription,levelUnits,hasExplicitVerticalAxis,axisType,positive);
		}

		public VerticalLevel getVerticalLevel() {
			return verticalLevel;
		}

		public String getProductDefinitionType() {
			return productDefinitionType;
		}

		/** Return the width of the wrapped GRIB record */
        public int getHeight() {
            return height;
        }

        /** Return the width of the wrapped GRIB record */
        public int getWidth() {
            return width;
        }

        /** Return the {@code SampleModel} used for the wrapped GRIB record */
        public SampleModel getSampleModel() {
            return sampleModel;
        }

        public String getParamID() {
            return paramID;
        }

        public String getLevelValues(final int index) {
			if (!getVerticalLevel().isHasExplicitVerticalAxis())
				return "";
			else {
				final int zIndex = NetCDFUtilities.getZIndex(variable, range, index);
				if (boundName != null) {
					Variable bound = boundsMap.get(boundName);
					if (bound != null) {
						final int[] shape = bound.getShape();
						final int step = shape[1];
						final String values = GRIB1Utilities.getValuesAsString(bound,new int[]{zIndex * step,zIndex * step+1});
						return values;
					}
				} else {
					final Variable verticalAxis = getVerticalAxis();
					final String values = GRIB1Utilities.getValuesAsString(verticalAxis,new int[]{zIndex});
					return values;

				}
				return "";
			}
		}
        
        public String getTimeValues(final int index) {
        	Variable temporalAxis = getTemporalAxis();
			if (temporalAxis==null)
				return "";
			else {
				final int tIndex = NetCDFUtilities.getTIndex(variable, range, index);
				//TODO: Check data with temporal bounds
//				if (boundName != null) {
//					Variable bound = boundsMap.get(boundName);
//					if (bound != null) {
//						final int dataType = NetCDFUtilities.getRawDataType(bound);
//
//						try {
//							final Array values = bound.read();
//							final int[] shape = bound.getShape();
//							final int step = shape[1];
//							switch (dataType) {
//							// TODO: ADD MORE.
//							case DataBuffer.TYPE_INT:
//								final int val1 = values.getInt(zIndex * step);
//								final int val2 = values.getInt(zIndex * step
//										+ 1);
//								return new StringBuilder(val1).append(
//										VALUE_SEPARATOR).append(val2)
//										.toString();
//							case DataBuffer.TYPE_FLOAT:
//								final float val1f = values.getFloat(zIndex
//										* step);
//								final float val2f = values.getFloat(zIndex
//										* step + 1);
//								return new StringBuilder(Float.toString(val1f))
//										.append(VALUE_SEPARATOR).append(
//												Float.toString(val2f))
//										.toString();
//							case DataBuffer.TYPE_DOUBLE:
//								final double val1d = values.getDouble(zIndex
//										* step);
//								final double val2d = values.getDouble(zIndex
//										* step + 1);
//								return new StringBuilder(Double.toString(val1d))
//										.append(VALUE_SEPARATOR).append(
//												Double.toString(val2d))
//										.toString();
//							}
//
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							// TODO LOGME
//						}
//					}
//				} else {
					return GRIB1Utilities.getValuesAsString(temporalAxis, new int[]{tIndex});
//				}
			}
		}


		private Variable getVerticalAxis() {
			final int axesSize = axes.size();
			Variable verticalAxis = null;
			if (axesSize == 4)
				verticalAxis = axes.get(1);
			else {
				verticalAxis = axes.get(0);
			}
			return verticalAxis;
		}
		
		private Variable getTemporalAxis() {
			final int axesSize = axes.size();
			Variable temporalAxis = null;
			if (axesSize > 2){
				temporalAxis = axes.get(0);
				final CoordinateAxis1D axis = (CoordinateAxis1D) temporalAxis;
				final AxisType axisType = axis.getAxisType();
				if (axisType==AxisType.Time || axisType==AxisType.RunTime)
					return axis;
			}
			return temporalAxis;
				
			
		}


		public String getParameterUnit() {
			return parameterUnit;
		}

		public int getParameterCenterID() {
			return parameterCenterID;
		}

		public int getParameterTableVersion() {
			return parameterTableVersion;
		}

		public int getParameterNumber() {
			return parameterNumber;
		}

		public String getParameterName() {
			return parameterName;
		}

	}

    /**
     * Sets the input source to use within this reader. {@code URI}s,
     * {@code File}s (also representing a Directory), {@code String}s (also
     * representing the path of a Directory), {@code URL}s,
     * {@code ImageInputStream}s are accepted input types.<BR>
     * Other parameters ({@code seekForwardOnly} and {@code ignoreMetadata})
     * are actually ignored.
     * 
     * @param input
     *                the {@code Object} to be set as input of this reader.
     * 
     * @throws {@link IllegalArgumentException}
     *                 in case the provided input {@code Object} cannot be
     *                 properly parsed and used as input for the reader.
     */
    public void setInput(Object input, boolean seekForwardOnly,
            boolean ignoreMetadata) {
        try {
            if (dataset != null)
                reset();

            if (dataset == null) {
                dataset = NetCDFUtilities.getDataset(input);
            }

            super.setInput(input, seekForwardOnly, ignoreMetadata);
            initialize();

        } catch (IOException e) {
            throw new IllegalArgumentException("Error occurred during NetCDF file parsing", e);
        } catch (InvalidRangeException e) {
            throw new IllegalArgumentException( "Error occurred during NetCDF file parsing", e);
        }
    }

    /**
     * Initialize main properties for this reader.
     * 
     * @throws exception
     *                 {@link InvalidRangeException}
     */
    @SuppressWarnings("unchecked")
    private synchronized void initialize() throws InvalidRangeException {
        int numImages = 0;
        indexMap = new HashMap<Range, GribVariableWrapper>();
        if (dataset != null) {

            final List<Variable> variables = dataset.getVariables();
            if (variables != null) {
                for (final Variable variable : variables) {
                    if (variable != null && variable instanceof VariableDS) {
                        if (!NetCDFUtilities.isVariableAccepted(variable,CheckType.NONE)){
                        	if (variable.getName().equalsIgnoreCase(NetCDFUtilities.COORDSYS)){
                        		horizontalGrid = variable;
                        	}
                        	continue;
                        }
                        int[] shape = variable.getShape();
                        Range wrapperRange = null;
                        switch (shape.length) {
                        case 2:
                        	wrapperRange = new Range(numImages, numImages + 1);
                            indexMap.put(wrapperRange,new GribVariableWrapper(variable,wrapperRange));
                            numImages++;
                            break;
                        case 3:
                        	wrapperRange = new Range(numImages, numImages+ shape[0]);
                            indexMap.put(wrapperRange, new GribVariableWrapper(variable,wrapperRange));
                            numImages += shape[0];
                            break;
                        case 4:
                        	wrapperRange = new Range(numImages, numImages+ shape[0] * shape[1]);
                            indexMap.put(wrapperRange,new GribVariableWrapper(variable,wrapperRange));
                            numImages += shape[0] * shape[1];
                            break;
                        }
                    }
                }
            }
        }
        setNumImages(numImages);
        numGlobalAttributes = 0;
        final List<Attribute> globalAttributes = dataset.getGlobalAttributes();
        if (globalAttributes != null && !globalAttributes.isEmpty())
            numGlobalAttributes = globalAttributes.size();
    }

    /**
     * Sets the input source to use within this reader. {@code URI}s,
     * {@code File}s (also representing a Directory), {@code String}s (also
     * representing the path of a Directory), {@code URL}s,
     * {@code ImageInputStream}s are accepted input types.<BR>
     * The parameter ({@code seekForwardOnly} is actually ignored.
     * 
     * @param input
     *                the {@code Object} to be set as input of this reader.
     * 
     * @throws {@link IllegalArgumentException}
     *                 in case the provided input {@code Object} cannot be
     *                 properly parsed and used as input for the reader.
     */
    public void setInput(Object input, boolean seekForwardOnly) {
        this.setInput(input);
    }

    /**
     * Sets the input source to use within this reader. {@code URI}s,
     * {@code File}s (also representing a Directory), {@code String}s (also
     * representing the path of a Directory), {@code URL}s,
     * {@code ImageInputStream}s are accepted input types.<BR>
     * 
     * @param input
     *                the {@code Object} to be set as input of this reader.
     * 
     * @throws {@link IllegalArgumentException}
     *                 in case the provided input {@code Object} cannot be
     *                 properly parsed and used as input for the reader.
     */
    public void setInput(Object input) {
        this.setInput(input, true, true);
    }

    public GRIB1ImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    /**
     * Returns an {@code Iterator} containing possible image types to which the
     * requested image may be decoded, in the form of
     * {@code ImageTypeSpecifiers}s. At least one legal image type will be
     * returned.
     * 
     * @param imageIndex
     *                the index of the image to be retrieved.
     * 
     * @return an {@code Iterator} containing the image types.
     */
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex)
            throws IOException {
        final List<ImageTypeSpecifier> l = new java.util.ArrayList<ImageTypeSpecifier>(1);
        // Getting a proper GribVariableWrapper for the specified index
        final GribVariableWrapper gw = getGribVariableWrapper(imageIndex);
        final SampleModel sm = gw.getSampleModel();
        ImageTypeSpecifier imageType = new ImageTypeSpecifier(GRIB1Utilities.getColorModel(sm.getDataType()), sm);
        l.add(imageType);
        return l.iterator();
    }

    /**
     * Returns a {@link GribVariableWrapper} instance given a specified
     * imageIndex.
     * 
     * @param imageIndex
     *                the index of the record for which to retrieve a
     *                {@link GribVariableWrapper}
     * @return a {@link GribVariableWrapper}.
     */
    GribVariableWrapper getGribVariableWrapper(int imageIndex) {
        checkImageIndex(imageIndex);
        GribVariableWrapper wrapper = null;
        for (Range range : indexMap.keySet()) {
            if (range.contains(imageIndex) && range.first() <= imageIndex&& imageIndex < range.last()) {
                wrapper = indexMap.get(range);
            }
        }
        return wrapper;
    }

    /**
     * Returns the width of the specified image.
     * 
     * @param imageIndex
     *                the index of the required image
     * @return the width of the specified image.
     * @throws IOException
     */
    public int getWidth(int imageIndex) throws IOException {
        return getGribVariableWrapper(imageIndex).getWidth();
    }

    /**
     * Returns the height of the specified image.
     * 
     * @param imageIndex
     *                the index of the required image
     * @return the height of the specified image.
     * @throws IOException
     */
    public int getHeight(int imageIndex) throws IOException {
        return getGribVariableWrapper(imageIndex).getHeight();
    }


    /**
     * @see javax.imageio.ImageReader#read(int, javax.imageio.ImageReadParam)
     */
    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param)
            throws IOException {
    	BufferedImage image = null;
    	Variable variable = null;
        Range indexRange = null;
        GribVariableWrapper wrapper = null;
        for (Range range : indexMap.keySet()) {
            if (range.contains(imageIndex) && range.first() <= imageIndex
                    && imageIndex < range.last()) {
                wrapper = indexMap.get(range);
                indexRange = range;
                break;
            }
        }
        variable = wrapper.getVariable();

        /*
         * Fetches the parameters that are not already processed by utility
         * methods like 'getDestination' or 'computeRegions' (invoked below).
         */
        final int strideX, strideY;
        final int[] srcBands, dstBands;
        if (param != null) {
            strideX = param.getSourceXSubsampling();
            strideY = param.getSourceYSubsampling();
            srcBands = param.getSourceBands();
            dstBands = param.getDestinationBands();
        } else {
            strideX = 1;
            strideY = 1;
            srcBands = null;
            dstBands = null;
        }
        final int rank = wrapper.getRank();
        final int bandDimension = rank - NetCDFUtilities.Z_DIMENSION;

        /*
         * Gets the destination image of appropriate size. We create it now
         * since it is a convenient way to get the number of destination bands.
         */
        final int width = wrapper.getWidth();
        final int height = wrapper.getHeight();
        /*
         * Computes the source region (in the NetCDF file) and the destination
         * region (in the buffered image). Copies those informations into UCAR
         * Range structure.
         */
        final Rectangle srcRegion = new Rectangle();
        final Rectangle destRegion = new Rectangle();
        computeRegions(param, width, height, null, srcRegion, destRegion);
        // flipVertically(param, height, srcRegion);
        int destWidth = destRegion.x + destRegion.width;
        int destHeight = destRegion.y + destRegion.height;

        final List<Range> ranges = new LinkedList<Range>();
        for (int i = 0; i < rank; i++) {
            final int first, length, stride;
            switch (rank - i) {
            case NetCDFUtilities.X_DIMENSION: {
                first = srcRegion.x;
                length = srcRegion.width;
                stride = strideX;
                break;
            }
            case NetCDFUtilities.Y_DIMENSION: {
                first = srcRegion.y;
                length = srcRegion.height;
                stride = strideY;
                break;
            }
            default: {
                if (i == bandDimension) {
                    first = NetCDFUtilities.getZIndex(variable, indexRange,
                            imageIndex);
                } else {
                    first = NetCDFUtilities.getTIndex(variable, indexRange,
                            imageIndex);
                }
                length = 1;
                stride = 1;
                break;
            }
            }
            try {
                ranges.add(new Range(first, first + length - 1, stride));
            } catch (InvalidRangeException e) {
            }
        }
        final Section sections = new Section(ranges);

        /*
         * Setting SampleModel and ColorModel.
         */
        SampleModel sampleModel = wrapper.getSampleModel()
                .createCompatibleSampleModel(destWidth, destHeight);
        ColorModel colorModel = ImageIOUtilities
                .getCompatibleColorModel(sampleModel);

        /*
         * Reads the requested sub-region only.
         */
        final int numDstBands = 1;
        final int size = destHeight*destWidth*numDstBands;
        for (int zi = 0; zi < numDstBands; zi++) {
            final int dstBand = (dstBands == null) ? zi : dstBands[zi];
            final Array array;
            try {
                array = variable.read(sections);
                DataBuffer dataBuffer = null;
                if (array instanceof ArrayByte){
                	dataBuffer = new DataBufferByte((byte[])array.get1DJavaArray(byte.class),size);
                } else if (array instanceof ArrayShort){
                	dataBuffer = new DataBufferShort((short[])array.get1DJavaArray(short.class),size);
                } else if (array instanceof ArrayInt){
                	dataBuffer = new DataBufferInt((int[])array.get1DJavaArray(int.class),size);
                } else if (array instanceof ArrayFloat){
                	dataBuffer = new DataBufferFloat((float[])array.get1DJavaArray(float.class),size);
                } else if (array instanceof ArrayDouble){
                	dataBuffer = new DataBufferDouble((double[])array.get1DJavaArray(double.class),size);
                }
                
                WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, new Point(0,0));
                image = new BufferedImage(colorModel, raster,
                        colorModel.isAlphaPremultiplied(), null);
            } catch (InvalidRangeException e) {
            	
            }
        }
        return image;
    }


    /**
     */
    public synchronized IIOMetadata getStreamMetadata() {
        throw new UnsupportedOperationException();
    }

    /**
     */
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        return new GRIB1ImageMetadata(this, imageIndex);
    }

    /**
     * Allows any resources held by this reader to be released. <BR>
     */
    public void dispose() {
        super.dispose();
        try {
            if (dataset != null) {
                dataset.close();
            }
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning("Errors closing NetCDF dataset."
                        + e.getLocalizedMessage());
        } finally {
            dataset = null;
        }
        
    }

    /**
     * Returns {@code true} since this plug-in also supports reading just a
     * Raster of pixel data
     */
    public boolean canReadRaster() {
        return true;
    }

    /**
     * Returns {@code false} since the image is not organized into tiles.
     */
    public boolean isImageTiled(int imageIndex) throws IOException {
        return false;
    }

    /**
     * Returns a {@code RenderedImage} object that contains the contents of the
     * image indexed by {@code imageIndex}.
     * 
     * @param imageIndex
     *                the index of the required image
     * @param param
     *                an {@code ImageReadParam} used to customize the reading
     *                process, or {@code null}
     * @return the requested image as a {@code RenderedImage}
     */
    public RenderedImage readAsRenderedImage(int imageIndex,
            ImageReadParam param) throws IOException {
        return read(imageIndex, param);
    }

    /**
     * Reset the status of this reader
     */
    public void reset() {
        dispose();
    }

	public String getName(final int imageIndex) {
		return getGribVariableWrapper(imageIndex).getName();
	}
}
