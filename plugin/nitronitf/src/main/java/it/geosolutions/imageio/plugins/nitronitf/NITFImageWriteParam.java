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
package it.geosolutions.imageio.plugins.nitronitf;

import it.geosolutions.imageio.plugins.nitronitf.NITFUtilities.WriteCompression;
import it.geosolutions.imageio.plugins.nitronitf.wrapper.NITFProperties;

import javax.imageio.ImageWriteParam;


/**
 * Class holding Write parameters to customize the write operations
 * 
 * @author Daniele Romagnoli, GeoSolutions SaS
 */
public class NITFImageWriteParam extends ImageWriteParam {

    @Override
    public boolean canWriteCompressed() {
        return true;
    }

    @Override
    public boolean canWriteTiles() {
        return true;
    }
    
    //TODO Convert some of them as proper metadata entities (imageMetadata/streamMetadata)
    
    private WriteCompression writeCompression;
    
    private NITFProperties nitfProperties;
    
    public NITFProperties getNitfProperties() {
        return nitfProperties;
    }

    public void setNitfProperties(NITFProperties nitfProperties) {
        this.nitfProperties = nitfProperties;
    }

    public WriteCompression getWriteCompression() {
        return writeCompression;
    }

    public void setWriteCompression(WriteCompression writeCompression) {
        this.writeCompression = writeCompression;
    }

}