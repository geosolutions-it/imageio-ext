/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://imageio-ext.dev.java.net/
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
package it.geosolutions.imageio.matfile5;

import it.geosolutions.imageio.utilities.Utilities;

import java.awt.image.DataBuffer;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt32;
import com.jmatio.types.MLInt64;
import com.jmatio.types.MLInt8;
import com.jmatio.types.MLSingle;

public class Utils {
    
    private Utils(){
        
    }
   
    public static String getString(final MatFileReader reader, final String element) {
        String value = "";
        Utilities.checkNotNull(reader, "The provided MatFileReader was Null");
        if (element != null) {
            final MLArray array = reader.getMLArray(element);
            if (array != null && array instanceof MLChar){
            	final MLChar text = (MLChar) array;
                if (text != null)
                    value = text.getString(0);	
            }
        }
        return value;
    }
    
    
    public static int getDatatype (final MLArray array){
    	Utilities.checkNotNull(array, "Unable to setup a proper datatype, being the provided data NULL");
        if (array instanceof MLDouble){
            return DataBuffer.TYPE_DOUBLE;
        }
        if (array instanceof MLSingle){
            return DataBuffer.TYPE_FLOAT;
        }
        if (array instanceof MLInt32){
            return DataBuffer.TYPE_INT;
        }
        if (array instanceof MLInt8){
            return DataBuffer.TYPE_BYTE;
        }
        return DataBuffer.TYPE_UNDEFINED;    
                
    }

    /**
     * Return the Matlab Array type (class) for the provided Matlab Array 
     * @param array the input array to be checked
     * @return an int specifying the proper {@link MLArray}.mxXXX_CLASS 
     */
    public static int getMatDatatype(final MLArray array) {
    	Utilities.checkNotNull(array, "Unable to setup a proper datatype, being the provided data NULL");
        //TODO: add more cases
        if (array instanceof MLSingle){
            return MLArray.mxSINGLE_CLASS;
        }
        if (array instanceof MLDouble){
            return MLArray.mxDOUBLE_CLASS;
        }
        if (array instanceof MLInt8){
            return MLArray.mxINT8_CLASS;
        }
        if (array instanceof MLInt32){
            return MLArray.mxINT32_CLASS;
        }
        if (array instanceof MLInt64){
            return MLArray.mxINT64_CLASS;
        }
        return MLArray.mxUNKNOWN_CLASS;
    }    
}
