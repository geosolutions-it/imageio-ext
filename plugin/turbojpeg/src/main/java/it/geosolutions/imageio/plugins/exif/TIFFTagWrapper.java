/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2011, GeoSolutions
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
package it.geosolutions.imageio.plugins.exif;

import java.util.Arrays;

/**
 * @author  Daniele Romagnoli, GeoSolutions SaS
 * 
 * A class holding TIFF Tag properties like TAG ID (number), count, type, value/offset, content 
 * 
 * @see <a href="http://partners.adobe.com/public/developer/en/tiff/TIFF6.pdf">TIFF specification, page 15</a>
 */
public class TIFFTagWrapper{
    
    /** the TAG ID number */
    private int number; 
    
    /** the number of values */
    private int count;
    
    /** the TIFF field type */ 
    private int type;
    
    /** the value/offset */
    private int value; 
    
    /** 
     * specific byte[] suffix and prefix for specific tag to be used to fully represent a field.
     * 
     * As an instance, each ascii value should be terminated by a null byte.
     * In that case, the user will set a content made of bytes representing the text and 
     * the suffix will be a byte[]{0}.
     * 
     * Another example: the UserComment allows to put a comment into exif. The user should
     * simply specify the bytes representing the comment content. The prefix will be specified
     * as requested by the specification. 
     * See {@link EXIFUtilities#USER_COMMENT_ASCII_CHAR_CODE}
     * @see <a href="http://www.awaresystems.be/imaging/tiff/tifftags/privateifd/exif/usercomment.html">
     * UserComment character code prefix</a>
     */
    private byte[] suffix;
    private byte[] prefix;
    
    /** 
     * An object storing the Field value (currently as a byte[]), setup by the user representing the 
     * raw content to be set without any prefix/suffix additional bytes requested by the specifications.  
     */
    private Object content;  
    

    @Override
    public String toString() {
        return "TIFFTagWrapper [content=" + content + ", value=" + value + ", count=" + count
                + ", prefix=" + Arrays.toString(prefix) + ", suffix=" + Arrays.toString(suffix)
                + ", type=" + type + ", number=" + number + "]";
    }

    public TIFFTagWrapper(final int tagNumber, final int type, final String content, final int value, final int count) {
        this(tagNumber, type, content, value, count, null, null);
    }
    
    /**
     * A fully specified {@link TIFFTagWrapper} constructor.
     * 
     * @param tagNumber the ID of the underlying TIFF Tag
     * @param type the type of the TIFF Field (BYTE, ASCII, SHORT, LONG, ...)
     * @param content the content (currently, a byte[]) to be set for that field 
     *  (without any prefix/suffix).
     * @param valueOffset the value of this  
     * @param count the number of values. (this value is ignored in case of not null content/prefix/suffix since
     *   it will be computed on top of these byte arrays)
     * @param prefix an optional byte[] to be inserted before the specified content to fully represent the field 
     *    (as an instance, the UserComment content need to be prefixed by a 8 byte array representing the character code)
     *     @see <a href="http://www.awaresystems.be/imaging/tiff/tifftags/privateifd/exif/usercomment.html">
     * UserComment character code prefix</a>
     * @param suffix an optional byte[] to be appended after the specified content to fully represent the field 
     *    (as an instance, a byte[]{0} null char to be appended to an ASCII content)  
     */
    public TIFFTagWrapper(
            final int tagNumber, 
            final int type, 
            final Object content, 
            final int valueOffset, 
            final int count, 
            final byte[] prefix, 
            final byte[] suffix) {
        this.count = count;
        this.number = tagNumber;
        this.type = type;
        this.prefix = prefix != null ? prefix.clone() : null;
        this.suffix = suffix != null ? suffix.clone() : null;
        
        if (content != null) {
            this.content = content;    
            this.count = (content instanceof byte[]) ? ((byte[]) content).length : 0 ;
            if (suffix != null){
                this.count += suffix.length;
            }
            if (prefix != null){
                this.count += prefix.length;
            }
        }
        this.value = valueOffset;
        
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public byte[] getPrefix() {
        return prefix;
    }

    public void setPrefix(byte[] prefix) {
        this.prefix = prefix;
    }

    public byte[] getSuffix() {
        return suffix;
    }

    public void setSuffix(byte[] suffix) {
        this.suffix = suffix;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
    
}