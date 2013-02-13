package it.geosolutions.imageio.plugins.netcdf;

import it.geosolutions.imageio.plugins.netcdf.NetCDFUtilities.KeyValuePair;
import it.geosolutions.imageio.utilities.ImageIOUtilities;

import java.awt.image.SampleModel;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.ImageReaderSpi;

import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;

public abstract class UcarImageReader extends ImageReader {

    protected int numRasters = -1;

    public UcarImageReader( ImageReaderSpi originatingProvider ) {
        super(originatingProvider);
    }

    public abstract BaseVariableWrapper getVariableWrapper( int imageIndex );

    public abstract NetcdfDataset getDataset();

    public int getNumImages( final boolean allowSearch ) throws IOException {
        return numRasters;
    }

    protected void setNumImages( final int numImages ) {
        if (this.numRasters == -1)
            this.numRasters = numImages;
    }

    /**
     * Simple check of the specified image index. Valid indexes are belonging
     * the range [0 - numRasters]. In case this constraint is not respected, an
     * {@link IndexOutOfBoundsException} is thrown.
     * 
     * @param imageIndex
     *                the index to be checked
     * 
     * @throw {@link IndexOutOfBoundsException} in case the provided imageIndex
     *        is not in the range of supported ones.
     */
    public void checkImageIndex( final int imageIndex ) {
        if (imageIndex < 0 || imageIndex >= numRasters) {
            throw new IndexOutOfBoundsException("Invalid imageIndex. It should "
                    + (numRasters > 0 ? ("belong the range [0," + (numRasters - 1)) : "be 0"));
        }
    }

    @Override
    public int getHeight( int imageIndex ) throws IOException {
        final BaseVariableWrapper wrapper = getVariableWrapper(imageIndex);
        if (wrapper != null)
            return wrapper.getHeight();
        return -1;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes( int imageIndex ) throws IOException {
        final List<ImageTypeSpecifier> l = new java.util.ArrayList<ImageTypeSpecifier>();
        final BaseVariableWrapper wrapper = getVariableWrapper(imageIndex);
        if (wrapper != null) {
            final SampleModel sampleModel = wrapper.getSampleModel();
            final ImageTypeSpecifier imageType = new ImageTypeSpecifier(ImageIOUtilities.createColorModel(sampleModel),
                    sampleModel);
            l.add(imageType);
        }
        return l.iterator();
    }

    public int getWidth( int imageIndex ) throws IOException {
        final BaseVariableWrapper wrapper = getVariableWrapper(imageIndex);
        if (wrapper != null)
            return wrapper.getWidth();
        return -1;
    }

    public String getAttributeAsString( final int imageIndex, final String attributeName ) {
        return getAttributeAsString(imageIndex, attributeName, false);
    }

    public String getAttributeAsString( final int imageIndex, final String attributeName, final boolean isUnsigned ) {
        String attributeValue = "";
        final BaseVariableWrapper wrapper = getVariableWrapper(imageIndex);
        final Attribute attr = wrapper.getVariable().findAttributeIgnoreCase(attributeName);
        if (attr != null)
            attributeValue = NetCDFUtilities.getAttributesAsString(attr, isUnsigned);
        return attributeValue;
    }

    public KeyValuePair getAttribute( final int imageIndex, final int attributeIndex ) throws IOException {
        KeyValuePair attributePair = null;
        final Variable var = getVariable(imageIndex);
        if (var != null)
            attributePair = NetCDFUtilities.getAttribute(var, attributeIndex);
        return attributePair;
    }

    protected Variable getVariable( final int imageIndex ) {
        Variable var = null;
        final BaseVariableWrapper wrapper = getVariableWrapper(imageIndex);
        if (wrapper != null)
            var = wrapper.getVariable();
        return var;
    }

    public String getVariableName( int imageIndex ) {
        String name = "";
        BaseVariableWrapper wrapper = getVariableWrapper(imageIndex);
        if (wrapper != null) {
            name = wrapper.getName();
        }
        return name;
    }

    protected Variable getVariableByName( final String varName ) {
        final List<Variable> varList = getDataset().getVariables();
        for( Variable var : varList ) {
            if (var.getName().equals(varName))
                return var;
        }
        return null;
    }

    public int getNumAttributes( int imageIndex ) {
        int numAttribs = 0;
        final Variable var = getVariable(imageIndex);
        if (var != null) {
            final List<Attribute> attributes = var.getAttributes();
            if (attributes != null && !attributes.isEmpty())
                numAttribs = attributes.size();
        }
        return numAttribs;
    }

    public KeyValuePair getGlobalAttribute( final int attributeIndex ) throws IOException {
        return NetCDFUtilities.getGlobalAttribute(getDataset(), attributeIndex);
    }

    /**
     * TODO move this to utility?
     * 
     * @param variable
     * @return
     */
    CoordinateSystem getCoordinateSystem( Variable variable ) {
        CoordinateSystem cs = null;
        if (variable != null) {
            final List<CoordinateSystem> systems = ((VariableDS) variable).getCoordinateSystems();
            if (!systems.isEmpty())
                cs = systems.get(0);
        }
        return cs;
    }

    /**
     * Retrieve the scale factor for the specified imageIndex. Return
     * {@code Double.NaN} if parameter isn't available
     * 
     * @throws IOException
     */
    double getScale( final int imageIndex ) throws IOException {
        checkImageIndex(imageIndex);
        double scale = Double.NaN;
        final String scaleS = getAttributeAsString(imageIndex, NetCDFUtilities.DatasetAttribs.SCALE_FACTOR);
        if (scaleS != null && scaleS.trim().length() > 0)
            scale = Double.parseDouble(scaleS);
        return scale;
    }

    /**
     * Retrieve the fill value for the specified imageIndex. Return
     * {@code Double.NaN} if parameter isn't available
     * 
     * @throws IOException
     */
    double getFillValue( final int imageIndex ) throws IOException {
        checkImageIndex(imageIndex);
        double fillValue = Double.NaN;
        final String fillValueS = getAttributeAsString(imageIndex, NetCDFUtilities.DatasetAttribs.FILL_VALUE);
        if (fillValueS != null && fillValueS.trim().length() > 0)
            fillValue = Double.parseDouble(fillValueS);
        return fillValue;
    }

    /**
     * Retrieve the offset factor for the specified imageIndex. Return
     * {@code Double.NaN} if parameter isn't available
     * 
     * @throws IOException
     */
    double getOffset( final int imageIndex ) throws IOException {
        checkImageIndex(imageIndex);
        double offset = Double.NaN;
        final String offsetS = getAttributeAsString(imageIndex, NetCDFUtilities.DatasetAttribs.ADD_OFFSET);
        if (offsetS != null && offsetS.trim().length() > 0)
            offset = Double.parseDouble(offsetS);
        return offset;
    }

    /**
     * Retrieve the valid Range for the specified imageIndex. Return null if
     * parameters aren't available
     * 
     * @throws IOException
     */
    double[] getValidRange( final int imageIndex ) throws IOException {
        checkImageIndex(imageIndex);
        double range[] = null;

        final String validRange = getAttributeAsString(imageIndex, NetCDFUtilities.DatasetAttribs.VALID_RANGE, true);
        if (validRange != null && validRange.trim().length() > 0) {
            String validRanges[] = validRange.split(",");
            if (validRanges.length == 2) {
                range = new double[2];
                range[0] = Double.parseDouble(validRanges[0]);
                range[1] = Double.parseDouble(validRanges[1]);
            }
        } else {
            final String validMin = getAttributeAsString(imageIndex, NetCDFUtilities.DatasetAttribs.VALID_MIN, true);
            final String validMax = getAttributeAsString(imageIndex, NetCDFUtilities.DatasetAttribs.VALID_MAX, true);
            if (validMax != null && validMax.trim().length() > 0 && validMin != null && validMin.trim().length() > 0) {
                range = new double[2];
                range[0] = Double.parseDouble(validMin);
                range[1] = Double.parseDouble(validMax);
            }
        }
        return range;
    }

    @Override
    public void setInput( Object input, boolean seekForwardOnly ) {
        this.setInput(input, seekForwardOnly, false);
    }

    @Override
    public void setInput( Object input ) {
        this.setInput(input, false, false);
    }

    /**
     * Reset the status of this reader
     */
    public void reset() {
        super.setInput(null, false, false);
        dispose();
    }

}