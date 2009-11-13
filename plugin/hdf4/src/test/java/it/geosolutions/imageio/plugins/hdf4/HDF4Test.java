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
package it.geosolutions.imageio.plugins.hdf4;

import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import junit.framework.TestCase;

import org.junit.Test;
/**
 * Basic tests for hdf4
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public class HDF4Test extends TestCase {


    @Test
    public void testService() throws IOException {
       final Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("HDF4");
       assertNotNull(readers);
       assertTrue(readers.hasNext());
       
       final ImageReader reader= readers.next();
       assertNotNull(reader);
       assertTrue(reader.getFormatName().equalsIgnoreCase("HDF4"));
    }
}
