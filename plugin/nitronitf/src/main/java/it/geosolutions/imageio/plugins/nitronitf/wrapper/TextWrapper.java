/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2012, GeoSolutions
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
package it.geosolutions.imageio.plugins.nitronitf.wrapper;

/**
 * Wrapper class related to a TextSegment of a NITF file.
 * 
 * @author Daniele Romagnoli, GeoSolutions s.a.s.
 */
public class TextWrapper extends IdentifiableNITFObjectWrapper {

    private String format;

    private String attachmentLevel = "000";

    private byte[] textContent;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getAttachmentLevel() {
        return attachmentLevel;
    }

    public void setAttachmentLevel(String attachmentLevel) {
        this.attachmentLevel = attachmentLevel;
    }

    public byte[] getTextContent() {
        return textContent;
    }

    public void setTextContent(byte[] textContent) {
        this.textContent = textContent;
    }

}
