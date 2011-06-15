/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
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

import it.geosolutions.imageio.ndplugin.BaseImageReader;
import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities.KeyValuePair;
import it.geosolutions.imageio.stream.input.URIImageInputStream;
import it.geosolutions.imageio.utilities.ImageIOUtilities;
import it.geosolutions.imageio.utilities.Utilities;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;

/**
 * Base implementation for NetCDF based image flat reader. 
 * 
 * Each ImageIndex corresponds to a 2D-slice of NetCDF.
 * 
 * {@link BaseNetCDFImageReader} is a {@link ImageReader} able to create
 * {@link RenderedImage} from NetCDF-CF sources.
 * 
 * @author Alessio Fabiani, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 */
public final class BaseNetCDFImageReader extends BaseImageReader{

    protected final static Logger LOGGER = Logger .getLogger(BaseNetCDFImageReader.class.toString());

    /**
     * The NetCDF dataset, or {@code null} if not yet open. The NetCDF file is
     * open by {@link #ensureOpen} when first needed.
     */
    private NetcdfDataset dataset;
    
    private Map<Range,? extends BaseVariableWrapper> indexMap; 
    
    public Map<Range, ?> getIndexMap() {
		return indexMap;
	}

	public synchronized void setIndexMap(final Map<Range, ? extends BaseVariableWrapper> indexMap) {
		if (initMap)
			throw new IllegalStateException("Map already initialized");
		initMap=true;
		this.indexMap = indexMap;
	}

	private boolean initMap;
    
    public NetcdfDataset getDataset() {
		return dataset;
	}

	private int numGlobalAttributes;

	public void setNumGlobalAttributes(int numGlobalAttributes) {
		this.numGlobalAttributes = numGlobalAttributes;
	}

    /**
     * Sets the input source to use within this reader. {@code URI}s,
     * {@code File}s, {@code String}s, {@code URL}s, {@code ImageInputStream}s
     * are accepted input types.<BR>
     * Other parameters ({@code seekForwardOnly} and {@code ignoreMetadata})
     * are actually ignored.
     * 
     * @param input
     *                the {@code Object} to be set as input of this reader.
     * 
     * @throws exception
     *                 {@link IllegalArgumentException} in case the provided
     *                 input {@code Object} cannot be properly parsed and used
     *                 as input for the reader.
     */
    public void setInput(Object input, boolean seekForwardOnly,
            boolean ignoreMetadata) {
        try {
            if (dataset != null)
                reset();
            
            if (input instanceof URIImageInputStream) {
                URIImageInputStream uriInStream = (URIImageInputStream) input;
                dataset = NetcdfDataset.openDataset(uriInStream.getUri().toString());
            }
            if (input instanceof URL) {
                final URL tempURL = (URL) input;
                String protocol = tempURL.getProtocol();
                if (protocol.equalsIgnoreCase("http") || protocol.equalsIgnoreCase("dods")) {
                    dataset = NetcdfDataset.openDataset(tempURL.toExternalForm());
                }
            }
            
            if (dataset == null) {
                dataset = NetCDFUtilities.getDataset(input);
            }

            super.setInput(input, seekForwardOnly, ignoreMetadata);

        } catch (IOException e) {
            throw new IllegalArgumentException("Error occurred during NetCDF file parsing", e);
        }
    }

    protected void initialize(){
    	throw new UnsupportedOperationException("Implement ME");
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

    /**
     * Explicit Constructor getting {@link ImageReaderSpi} originatingProvider
     * as actual parameter.
     * 
     * @param originatingProvider
     *                {@link ImageReaderSpi}
     */
    public BaseNetCDFImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    /**
     * @see javax.imageio.ImageReader#getHeight(int)
     */
    @Override
    public int getHeight(int imageIndex) throws IOException {
    	final BaseVariableWrapper wrapper = getVariableWrapper(imageIndex);
        if (wrapper != null)
            return wrapper.getHeight();
        return -1;
    }

    public BaseVariableWrapper getVariableWrapper(final int imageIndex){
    	checkImageIndex(imageIndex);
        BaseVariableWrapper wrapper = null;
        for (Range range : indexMap.keySet()) {
            if (range.contains(imageIndex) && range.first() <= imageIndex&& imageIndex < range.last()) {
                wrapper = indexMap.get(range);
            }
        }
        return wrapper;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        final List<ImageTypeSpecifier> l = new java.util.ArrayList<ImageTypeSpecifier>();
        final BaseVariableWrapper wrapper = getVariableWrapper(imageIndex);
        if (wrapper != null) {
        	final SampleModel sampleModel = wrapper.getSampleModel();
        	final ImageTypeSpecifier imageType = new ImageTypeSpecifier(
        	ImageIOUtilities.createColorModel(sampleModel), sampleModel);
            l.add(imageType);
        }
        return l.iterator();
    }

//    public IIOMetadata getStreamMetadata() throws IOException {
//        return new NetCDFStreamMetadata(this);
//    }

    public int getWidth(int imageIndex) throws IOException {
    	final BaseVariableWrapper wrapper = getVariableWrapper(imageIndex);
        if (wrapper != null)
            return wrapper.getWidth();
        return -1;
    }
   
    /**
     * Allows any resources held by this reader to be released. <BR>
     * TODO: To grant thread safety, we may prevent a user call of this method.
     * 
     * @throws IOException
     */
    public void dispose() {
        super.dispose();
        initMap = false;
        indexMap.clear();
        indexMap = null;
        numGlobalAttributes = -1;
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
     * Reset the status of this reader
     */
    public void reset() {
        super.setInput(null, false, false);
        dispose();
    }

    public String getAttributeAsString(final int imageIndex, final String attributeName) {
        return getAttributeAsString(imageIndex, attributeName, false);
    }

    public String getAttributeAsString(final int imageIndex, final String attributeName,
            final boolean isUnsigned) {
        String attributeValue = "";
        final BaseVariableWrapper wrapper = getVariableWrapper(imageIndex);
        final Attribute attr = wrapper.getVariable().findAttributeIgnoreCase(attributeName);
        if (attr != null)
            attributeValue = NetCDFUtilities.getAttributesAsString(attr,
                    isUnsigned);
        return attributeValue;
    }
    
    public KeyValuePair getAttribute(final int imageIndex, final int attributeIndex)
    throws IOException {
		KeyValuePair attributePair = null;
		final Variable var = getVariable(imageIndex);
		if (var != null) 
			attributePair = NetCDFUtilities.getAttribute(var, attributeIndex);
		return attributePair;
	}

    Variable getVariable(final int imageIndex) {
        Variable var = null;
        final BaseVariableWrapper wrapper = getVariableWrapper(imageIndex);
        if (wrapper != null)
            var = wrapper.getVariable();
        return var;
    }
    
    public String getVariableName(int imageIndex) {
    	String name = "";
    	BaseVariableWrapper wrapper = getVariableWrapper(imageIndex);
    	if (wrapper != null) {
    		name = wrapper.getName();
    	}
    	return name;
    }

    Variable getVariableByName(final String varName) {
    	final List<Variable> varList = dataset.getVariables();
        for (Variable var : varList) {
            if (var.getName().equals(varName))
                return var;
        }
        return null;
    }

    CoordinateSystem getCoordinateSystem(Variable variable) {
        CoordinateSystem cs = null;
        if (variable != null) {
            final List<CoordinateSystem> systems = ((VariableDS) variable)
                    .getCoordinateSystems();
            if (!systems.isEmpty())
                cs = systems.get(0);
        }
        return cs;
    }

    public int getNumGlobalAttributes() {
        return numGlobalAttributes;
    }

    public int getNumAttributes(int imageIndex) {
        int numAttribs = 0;
        final Variable var = getVariable(imageIndex);
        if (var != null) {
        	final List<Attribute> attributes = var.getAttributes();
            if (attributes != null && !attributes.isEmpty())
                numAttribs = attributes.size();
        }
        return numAttribs;
    }

    public KeyValuePair getGlobalAttribute(final int attributeIndex) throws IOException {
		return NetCDFUtilities.getGlobalAttribute(dataset, attributeIndex);
	}

	@Override
	public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
		throw new UnsupportedOperationException("Implement ME");
	}

	@Override
	public IIOMetadata getStreamMetadata() throws IOException {
		throw new UnsupportedOperationException("Implement ME");
	}

	@Override
	public BufferedImage read(int imageIndex, ImageReadParam param){
		throw new UnsupportedOperationException("Implement ME");
	}
}