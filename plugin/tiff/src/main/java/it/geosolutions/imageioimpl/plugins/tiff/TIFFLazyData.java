/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2012, GeoSolutions
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
package it.geosolutions.imageioimpl.plugins.tiff;

import java.io.IOException;

import it.geosolutions.imageio.plugins.tiff.TIFFTag;

import javax.imageio.stream.ImageInputStream;
/**
 * Lazy loading for large tiff fields. We use this approach for loading much less data for a single
 * request, namely for tile position and lengths which in bigtiff can be enormous.
 * <p>
 * To perform this we retain an open stream to the data and we jump to read as less information as possible
 * 
 * @author Daniele Romagnoli, GeoSolutions SAS
 *
 */
public class TIFFLazyData {

    private ImageInputStream stream;
    
    private long startPosition;
    
    private int count;
    
    private int size;

    public TIFFLazyData(ImageInputStream stream, int type,
            int count) throws IOException {
    	// checks
    	if (stream == null) {
    		throw new IllegalArgumentException("Provided stream argument is null.");
    	}
    	if (count < 0) {
    		throw new IllegalArgumentException("Provided count is negative.");
    	}
        this.size = TIFFTag.getSizeOfType(type);
    	this.stream = stream;
        this.startPosition = stream.getStreamPosition();
        this.count = count;

    }
    
    public long getAsLong(final int index) {
        checkIndex(index);
        long val;
        try {
            stream.mark();
            stream.seek(startPosition + index * size);
            val = stream.readUnsignedInt();
            stream.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    
        return val;
    }
    
    public long getAsLong8(final int index) {
        checkIndex(index);
        long val;
        try {
            stream.mark();
            stream.seek(startPosition + index * size);
            val = stream.readLong();
            stream.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    
        return val;
    }

    private void checkIndex(final int index) {
        if (index > count) {
            throw new IllegalArgumentException("Specified index (" + index + ") must be lower than Count:" + count);
        }
    }
}
