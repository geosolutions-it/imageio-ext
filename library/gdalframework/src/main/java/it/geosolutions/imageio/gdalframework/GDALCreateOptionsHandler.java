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
package it.geosolutions.imageio.gdalframework;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Abstract class which allows to properly handle the set of "format specific"
 * create options. Each Image I/O plugin exploiting a GDAL driver which supports
 * create options, should extend this class and define the proper
 * <code>GDALCreateOptionsHandler</code> constructor.<BR>
 * 
 * To write the extended <code>GDALCreateOptionsHandler</code> constructor you
 * need to instantiate the <code>createOptions</code> array with the number of
 * supported create options. Then, you need to set the proper fields of each
 * {@link GDALCreateOption} using the constructor as shown in the example listed
 * below.<BR>
 * <BR>
 * ... <BR>
 * <BR>
 * Firstly: set the validityValues for the create option. See
 * <code>GDALCreateOption</code> source code for more information about
 * <code>validityValues</code> and others fields. <BR>
 * <BR>
 * <code>final String nameOfCreateOptionValidityValues[] = new String[N];</code><BR>
 * <code>nameOfCreateOptionValidityValues[0] = "FIRST VALUE";</code><BR>
 * <code>nameOfCreateOptionValidityValues[1] = "SECOND VALUE";</code><BR>
 * <code>...</code><BR>
 * <code>nameOfCreateOptionValidityValues[N-1] = "LAST VALUE";</code><BR>
 * <BR>
 * Then, create a new <code>GDALCreateOption</code> setting the
 * <code>optionName</code>, the <code>validityCheckType</code>, the
 * <code>validityValues</code> array and the <code>representedType</code>.<BR>
 * <BR>
 * <code>createOptions[i] = new GDALCreateOption( "CREATEOPTIONNAME",</code><BR>
 * <code>GDALCreateOption.VALIDITYCHECKTYPE_XXXX, nameOfCreateOptionValidityValues, GDALCreateOption.TYPE_XXXX);</code><BR>
 * <BR>
 * <BR>
 * <BR>
 * PRACTICAL EXAMPLE: Suppose we are setting a Quality Create options which
 * accepts integer values belonging the range [1,100]<BR>
 * <code>final String qualityValues[] = new String[2];</code>
 * <code>qualityValues[0] = "1";</code><BR>
 * <code>qualityValues[1] = "100";</code><BR>
 * <code>...</code><BR>
 * <code>createOptions[0]=new GDALCreateOption("Quality", <BR>
 * GDALCreateOption.VALIDITYCHECKTYPE_VALUE_BELONGINGRANGE_EXTREMESINCLUDED,<BR>
 * qualityValues, GDALCreateOption.TYPE_INT);</code>
 * 
 * Available information about create options properties can be found at <a
 * href="http://www.gdal.org/formats_list.html"> GDAL Supported formats list</a>.
 * Look at the proper format page to retrieve names and values.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini, GeoSolutions
 */
public abstract class GDALCreateOptionsHandler {

    /**
     * NOTE: ------------------------------------------------------------------
     * When extending this class for different formats, you need to respect
     * case-sensitiveness of create Options when setting <code>optionName</code>
     * field.
     */

    private final Map<String, GDALCreateOption> createOptionsMap = Collections
            .synchronizedMap(new HashMap<String, GDALCreateOption>());

    /**
     * Provides to return a {@link List} containing <code>String</code>s
     * representing all specified create options we need to give to the writer
     * when it call GDAL's create/createCopy method.
     */
    public List<String> getCreateOptions() {

        // ////
        // 
        // approach 1
        //
        // ////
        final Vector<String> options = new Vector<String>();
        synchronized (createOptionsMap) {
            final Collection<GDALCreateOption> values = createOptionsMap.values();
            final Iterator<GDALCreateOption> it = values.iterator();
            while (it.hasNext()) {

                // retrieving the index of the next set option
                final GDALCreateOption selectedOption = it.next();
                if (selectedOption.isSet()) {
                    final StringBuilder opt = new StringBuilder(selectedOption.getOptionName());
                    if (selectedOption.getRepresentedValueType() != GDALCreateOption.TYPE_NONE)
                        opt.append("=").append(selectedOption.getValue());
                    options.add(opt.toString());
                }
            }
        }

        return options;
    }

    /**
     * Set the value of the create option identified by <code>optionName</code>
     * to <code>optionValue</code>
     * 
     * @param optionName
     *                name of the create option we want to set.
     * @param optionValue
     *                value for the specified create option.
     */
    public void setCreateOption(final String optionName, final String optionValue) {
        synchronized (createOptionsMap) {
            if (!createOptionsMap.containsKey(optionName))
                throw new IllegalArgumentException("Create option with name"
                        + optionName + " does not exist");
            createOptionsMap.get(optionName).setValue(optionValue);
        }
    }

    /**
     * Set the create option identified by <code>optionName</code>
     * 
     * @param optionName
     *                name of the create option we want to set.
     */
    public void setCreateOption(final String optionName) {
        synchronized (createOptionsMap) {
            if (!createOptionsMap.containsKey(optionName))
                throw new IllegalArgumentException("Create option with name"
                        + optionName + " does not exist");
            ((GDALCreateOption) createOptionsMap.get(optionName)).setValue("");
        }
    }

    /**
     * This method add a create option to this handler.
     * 
     * @param option
     *                to add to this handler.
     */
    public void addCreateOption(final GDALCreateOption option) {
        if (option != null) {
            synchronized (createOptionsMap) {
                createOptionsMap.put(option.getOptionName(), option);
            }
        } else
            throw new NullPointerException();
    }

    /**
     * This method add a collection of create options to this handler.
     * 
     * <p>
     * Objects that are not of type {@link GDALCreateOption} are not added.
     * 
     * @param option
     *                to add to this handler.
     */
    public void addCreateOptions(final Collection<GDALCreateOption> options) {
        if (options != null && options.size() > 0) {
            synchronized (createOptionsMap) {
                final Iterator<GDALCreateOption> it = options.iterator();
                while (it.hasNext()) {
                    final Object o = it.next();
                    // we add it only in case it has the right type
                    if (o != null && o instanceof GDALCreateOption) {
                        final GDALCreateOption option = (GDALCreateOption) o;
                        createOptionsMap.put(option.getOptionName(), option);
                    }
                }
            }
        } else
            throw new IllegalArgumentException("The provided collection is null or empty");
    }

    /**
     * Set the value of the create option identified by <code>optionName</code>
     * to <code>optionValue</code>
     * 
     * @param optionName
     *                name of the create option we want to set.
     * @param optionValue
     *                value for the specified create option.
     */
    public void setCreateOption(final String optionName, final int optionValue) {
        setCreateOption(optionName, Integer.toString(optionValue));
    }

    /**
     * Set the value of the create option identified by <code>optionName</code>
     * to <code>optionValue</code>
     * 
     * @param optionName
     *                name of the create option we want to set.
     * @param optionValue
     *                value for the specified create option.
     */
    public void setCreateOption(final String optionName, final float optionValue) {
        setCreateOption(optionName, Float.toString(optionValue));
    }

}
