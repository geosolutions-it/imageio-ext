/* =========================================================================
 * This file is part of NITRO
 * =========================================================================
 * 
 * (C) Copyright 2004 - 2010, General Dynamics - Advanced Information Systems
 *
 * NITRO is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program; if not, If not, 
 * see <http://www.gnu.org/licenses/>.
 *
 */

package it.geosolutions.imageio.plugins.nitronitf;

import it.geosolutions.imageio.plugins.nitronitf.NITFReaderSpi;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReaderTest extends TestCase {
    private static final Log log = LogFactory.getLog(ReaderTest.class);

    static {
        IIORegistry registry = IIORegistry.getDefaultInstance();
        registry.registerServiceProvider(new NITFReaderSpi());
    }

    public void testInstalled() {
        assertTrue(ImageIO.getImageReadersBySuffix("ntf").hasNext());
        assertTrue(ImageIO.getImageReadersBySuffix("nitf").hasNext());
        assertTrue(ImageIO.getImageReadersBySuffix("nsf").hasNext());
    }
}
