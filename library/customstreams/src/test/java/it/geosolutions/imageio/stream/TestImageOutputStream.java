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
package it.geosolutions.imageio.stream;

import java.util.logging.Logger;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

/**
 * @author Simone Giannecchini, GeoSolutions
 */
public class TestImageOutputStream extends TestCase {
    private final static Logger LOGGER = Logger
            .getLogger(TestImageOutputStream.class.toString());

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestImageOutputStream.class);
    }

    public TestImageOutputStream(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();
        ImageIO.setUseCache(true);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void test() {
    }
}
