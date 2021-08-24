/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2016, GeoSolutions
 *    All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of GeoSolutions nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY GeoSolutions ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GeoSolutions BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package it.geosolutions.imageio.plugins.tiff;

import java.util.ArrayList;
import java.util.List;

/**
 * A class representing private and custom tags.
 */
public class PrivateTIFFTagSet extends TIFFTagSet {

    private static PrivateTIFFTagSet theInstance = null;

    /**
     * Used by GDAL: a XML document providing
     */
    public static final int TAG_GDAL_METADATA = 42112;

    /**
     * Used by GDAL: an ASCII encoded nodata value.
     */
    public static final int TAG_GDAL_NODATA = 42113;

    /**
     * Used by ZSTD
     */
    public static final int COMPRESSION_ZSTD = 50000;

    static class GDALNoData extends TIFFTag {
        public GDALNoData () {
            super("GDALNoDataTag",
                  TAG_GDAL_NODATA,
                  1 << TIFFTag.TIFF_ASCII);
        }
    }

    static class GDALMetadata extends TIFFTag {
        public GDALMetadata() {
            super("GDALMetadata",
                    TAG_GDAL_METADATA,
                    1 << TIFFTag.TIFF_ASCII);
        }
    }
    
    private static List<TIFFTag> tags;

    private static void initTags() {
        tags = new ArrayList<TIFFTag>(1);
        tags.add(new PrivateTIFFTagSet.GDALNoData());
        tags.add(new PrivateTIFFTagSet.GDALMetadata());
    }

    private PrivateTIFFTagSet() {
        super(tags);
    }

    /**
     * Returns a shared instance of a <code>PrivateTIFFTagSet</code>.
     *
     * @return a <code>PrivateTIFFTagSet</code> instance.
     */
    public synchronized static PrivateTIFFTagSet getInstance() {
        if (theInstance == null) {
            initTags();
            theInstance = new PrivateTIFFTagSet();
            tags = null;
        }
        return theInstance;
    }
}
