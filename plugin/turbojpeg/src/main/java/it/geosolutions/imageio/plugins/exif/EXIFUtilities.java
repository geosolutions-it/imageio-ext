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

import it.geosolutions.imageio.plugins.exif.EXIFTags.Type;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;

import com.sun.media.imageio.plugins.tiff.BaselineTIFFTagSet;
import com.sun.media.imageio.plugins.tiff.EXIFParentTIFFTagSet;
import com.sun.media.imageio.plugins.tiff.EXIFTIFFTagSet;
import com.sun.media.imageio.plugins.tiff.TIFFTag;

/**
 * @author Daniele Romagnoli, GeoSolutions SAS
 * 
 * Utility class providing methods to setup/parse EXIF, write it to stream, retrieve from stream. 
 */
public class EXIFUtilities {

    /** @deprecated use {@link EXIFTags#COPYRIGHT} */
    public static final int TAG_COPYRIGHT = BaselineTIFFTagSet.TAG_COPYRIGHT;

    /** @deprecated use {@link EXIFTags#EXIF_IFD_POINTER} */
    public static final int TAG_EXIF_IFD_POINTER = EXIFParentTIFFTagSet.TAG_EXIF_IFD_POINTER;

    /** @deprecated use {@link EXIFTags#USER_COMMENT} */
    public static final int TAG_USER_COMMENT = EXIFTIFFTagSet.TAG_USER_COMMENT;
    
    /** @deprecated use {@link EXIFTags#Type} */
    public enum EXIFTagType{
        BASELINE, EXIF
        //TODO more may be added in the future, like GPS, ...
    }
    /**
     * Simple dummy class to wrap an EXIFMetadata instance as well as the length
     * of the APP1 marker. 
     */
    static class EXIFMetadataWrapper {
        public EXIFMetadata getExif() {
            return exif;
        }

        public void setExif(EXIFMetadata exif) {
            this.exif = exif;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        /**
         * @param exif
         * @param length
         */
        public EXIFMetadataWrapper(EXIFMetadata exif, int length) {
            super();
            this.exif = exif;
            this.length = length;
        }

        EXIFMetadata exif;
        
        int length;
    }
    
    /** Utility buffer size */
    final static int DEFAULT_BUFFER_SIZE = 4096;
    
    final static int EXIF_SCAN_BUFFER_SIZE = 32768;

    final static byte _0 = 0x00;

    final static byte FF = (byte) 0xFF;

    /** EXIF Marker identifier */
    final static byte[] EXIF_MARKER = new byte[] { 'E', 'x', 'i', 'f', _0, _0 };

    /** Offset to be appended before starting IFD1 content */
    final static byte[] NEXT_IFD = new byte[] { _0, _0, _0, _0 };

    /** BIG Endian TIFF HEADER */
    final static byte[] TIFF_HEADER = new byte[] { 'M', 'M', _0, 0x2A, _0, _0, _0, 8 };
    
    final static int TIFF_HEADER_LENGTH = TIFF_HEADER.length;

    /** 
     * UserComment ASCII character code prefix to be inserted before any UserComment String when writing it 
     * into the EXIF marker
     */
    final static byte[] USER_COMMENT_ASCII_CHAR_CODE = new byte[] { 0x41, 0x53, 0x43, 0x49, 0x49, _0, _0, _0 };

    /** The APP1 Marker bytes, (APP1 is the one containing EXIF) */
    final static byte[] APP1_MARKER = new byte[] { FF, (byte) 0xE1 };

    /** 
     * The Data Quantization Table Marker. 
     * It is contained within a JPEG encoded data stream before the data image.
     * EXIF marker should be put before this marker
     */
    final static byte[] DQT_MARKER = new byte[] { FF, (byte) 0xDB };

    /** The specification requires 2 bytes to identify the number of tags */
    final static int BYTES_FOR_TAGS_NUMBER = 2;

    /** A byte array representing NULL String terminator, to be appended after any ASCII byte array */
    final static byte[] NULL_STRING = new byte[] { _0 };

    /** 
     * The fixed length of each IFD, 
     * made of Number (2 bytes) + Type (2 bytes) + Count (4 bytes) + Value/Offset (4 Bytes)
     */
    final static int IFD_LENGTH = 12;

    /**
     * This method will update the image referred by the specified inputStream, by replacing
     * the underlying EXIF with the one represented by the specified {@link EXIFMetadata} instance.
     * Write the result to the specified {@link OutputStream}.
     * 
     * @param outputStream the stream where to write the result
     * @param inputStream the input stream referring to the original image to be copied back to the
     *   output
     * @param exif the {@link EXIFMetadata} instance containing updated exif to replace the one
     *   contained into the input image. 
     * @param previousEXIFLength 
     *   the length of the previous EXIF marker. 
     *   It is needed in order to understand the portion of the input image to be copied back to 
     *   the output
     * @throws IOException
     */
    private static void updateStream(
            final OutputStream outputStream, 
            final FileImageInputStreamExt inputStream,
            final EXIFMetadata exif, 
            final int previousEXIFLength) throws IOException {
        ByteArrayOutputStream baos = null;
        BufferedOutputStream bos = null;
        try {
            
            // Setup a new byteArrayOutputStream on top of the Exif object 
            baos = initializeExifStream(exif, null);
            
            // Update this outputStream by copying bytes from the original image 
            // referred by the inputStream, but inserting updated EXIF 
            // instead of copying it from the original image
            bos = new BufferedOutputStream(outputStream, DEFAULT_BUFFER_SIZE);
            updateFromStream(bos, baos, inputStream, previousEXIFLength);
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (Throwable t) {
                    // Eat exception on close
                }
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (Throwable t) {
                    // Eat exception on close
                }
            }
        }

    }

    /**
     * This method allows to parse the provided {@link EXIFMetadata} object and put it into 
     * the specified outputStream while copying back the JPEG encoded image referred by
     * the imageData argument.
     * 
     * @param outputStream the stream where to write
     * @param imageData the bytes containing JPEG encoded image data 
     * @param imageDataSize the number of bytes to be used from the data array
     * @param exif the {@link EXIFMetadata} object holding EXIF.
     * @throws IOException
     */
    public static void insertEXIFintoStream(
            final OutputStream outputStream, 
            final byte[] imageData, 
            final int imageDataSize,
            final EXIFMetadata exif) throws IOException {
        ByteArrayOutputStream baos = null;
        if (outputStream instanceof ByteArrayOutputStream){
            baos = (ByteArrayOutputStream) outputStream;
            writeToByteStream(baos, imageData, imageDataSize, exif);
        } else {
            writeBuffered(outputStream, imageData, imageDataSize, exif);
        }
        
    }

    /**
     * This method write the provided {@link EXIFMetadata} into the specified outputStream 
     * while copying back the JPEG encoded image referred by the imageData argument.
     * 
     * @param outputStream the stream where to write
     * @param imageData the bytes containing JPEG encoded image data 
     * @param imageDataSize the number of bytes to be used from the data array
     * @param exif the {@link EXIFMetadata} object holding EXIF.
     * @throws IOException
     */
    private static void writeBuffered(
            final OutputStream outputStream, 
            final byte[] imageData, 
            final int imageDataSize,
            final EXIFMetadata exif) throws IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = initializeExifStream(exif, null);
            updateFromBytes(outputStream, baos, imageData, imageDataSize);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (Throwable t) {
                    // Eat exception on close
                }
            }
        }
    }

    /**
     * Write the specified {@link EXIFMetadata} object as well as the specified image data bytes to
     * the specified outputStream. It will take care of inserting the EXIF marker in the proper 
     * location within the outputStream when writing image data bytes.  
     * 
     * @param outputStream the outputStream where to write
     * @param imageData the bytes containing JPEG encoded image data 
     * @param imageDataSize the number of bytes to be used from the data array
     * @param exif the {@link EXIFMetadata} object holding EXIF.
     * @throws IOException
     */
    private static void writeToByteStream(
            ByteArrayOutputStream outputStream, 
            final byte[] imageData, 
            final int imageDataSize,
            final EXIFMetadata exif) throws IOException {
        
        // locate the DQT marker in the input imageData bytes
        final int dqtMarkerPos = locateFirst(imageData, DQT_MARKER);
        if (dqtMarkerPos != -1) {
            
            // write to stream the initial part of imageData before appending EXIF marker
            // at the proper position
            outputStream.write(imageData, 0, dqtMarkerPos);
            outputStream.flush();
            
            // Append the EXIF content
            outputStream = initializeExifStream(exif, outputStream);
            outputStream.write(_0);
            
            // Proceed with writing the remaining part of image data bytes.
            outputStream.write(imageData, dqtMarkerPos, imageDataSize - dqtMarkerPos);
        }
    }

    /**
     * Initialize a ByteArrayOutputStream on top of an {@link EXIFMetadata} entity.
     * 
     * @param exif an {@link EXIFMetadata} instance representing EXIF tags to be put to the stream
     * @param outputStream an optional {@link ByteArrayOutputStream} where to write the exif marker.
     * If null, a new {@link ByteArrayOutputStream} will be created and returned
     * 
     * @return the {@link ByteArrayOutputStream} containing the written EXIF bytes.
     * @throws IOException
     */
    private static ByteArrayOutputStream initializeExifStream(
            final EXIFMetadata exif, 
            final ByteArrayOutputStream outputStream) throws IOException {
        
        // Preliminar check. Write on: the provided ByteArrayOutputStream VS a newly created one
        final ByteArrayOutputStream baos = outputStream == null ? new ByteArrayOutputStream() : outputStream;

        // Get exif tags from the specified EXIF object.
        List<TIFFTagWrapper> baselineTags = exif.getList(Type.BASELINE);
        List<TIFFTagWrapper> exifTags = exif.getList(Type.EXIF);
        final int baseLength = TIFF_HEADER_LENGTH + BYTES_FOR_TAGS_NUMBER + NEXT_IFD.length; 

        // Initialize number of fields and tags
        final int numBaselineTags = baselineTags.size();
        final int numExifTags = exifTags.size();
        final byte[] numFieldsB = intToBytes(numBaselineTags);
        final byte[] numSpecificFieldsB = intToBytes(numExifTags);

        // Initialize tags sizes and offsets
        final int baseLineTagsOffsets[] = new int[numBaselineTags];
        final int baseLineTagsContentSizes[] = new int[numBaselineTags];
        computeOffsetsAndSizes(baselineTags, baseLength, baseLineTagsOffsets, baseLineTagsContentSizes);
        final int baselineContentLength = sum(baseLineTagsContentSizes);

        final int exifTagsOffsets[] = new int[numExifTags];
        final int exifTagsContentSizes[] = new int[numExifTags];
        computeOffsetsAndSizes(exifTags, baseLineTagsOffsets[numBaselineTags - 1]
                + BYTES_FOR_TAGS_NUMBER, exifTagsOffsets, exifTagsContentSizes);
        final int exifTagsContentLength = sum(exifTagsContentSizes);

        // Compute total marker length (which is the first entry to be written out after the marker)
        int app1Lenght = APP1_MARKER.length
                - 1 // -1 due to the 0xFF part
                + EXIF_MARKER.length + baseLength
                + BYTES_FOR_TAGS_NUMBER // Num Fields (2 bytes)
                + BYTES_FOR_TAGS_NUMBER // Num EXIF Fields (2 bytes)
                + numBaselineTags * IFD_LENGTH + numExifTags * IFD_LENGTH // Bytes needed to represent all IFD
                + baselineContentLength + exifTagsContentLength; // Bytes used for tags contents

        // Write headers
        baos.write(APP1_MARKER);
        baos.write(intToBytes(app1Lenght));
        baos.write(EXIF_MARKER);
        baos.write(TIFF_HEADER);
        baos.write(numFieldsB);

        // Write BaseLine IFDs and their content
        writeIFDs(baos, baselineTags);
        baos.write(NEXT_IFD);
        writeTagsContent(baos, baselineTags);
        baos.write(numSpecificFieldsB);

        // Write EXIF Specific IFDs and their content
        writeIFDs(baos, exifTags);
        writeTagsContent(baos, exifTags);
        baos.flush();
        return baos;
    }

    /**
     * Update the EXIF content referred by a {@link FileImageInputStreamExt} using the 
     * content available in the {@link ByteArrayOutputStream}. Store the result
     * to the specified {@link OutputStream}.
     * 
     * @param outputStream a {@link OutputStream} where to write
     * @param byteStream a {@link ByteArrayOutputStream} previously populated with the content
     *   of an EXIF marker.   
     * @param inputStream a {@link FileImageInputStreamExt} referring to a file containing 
     *   EXIF metadata to be updated
     * @param originalAPP1MarkerLength is the length of the original APP1 marker 
     *          (the one containing EXIF content), before the update
     * @throws IOException
     */
    private static void updateFromStream(
            final OutputStream outputStream, 
            final ByteArrayOutputStream byteStream,
            final FileImageInputStreamExt inputStream, 
            final int originalAPP1MarkerLength) throws IOException {
        final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int readlength = 0;
        boolean replacedExif = false;
        
        // Read from the input stream  
        while ((readlength = inputStream.read(buffer)) != -1) {

            //Look for the EXIF marker 
            int app1MarkerPos = replacedExif ? -1 : locateFirst(buffer, APP1_MARKER);

            if (app1MarkerPos != -1) {
                replacedExif = true;
                
                // Copy back all the previous part before putting the updated EXIF
                outputStream.write(buffer, 0, app1MarkerPos);
                outputStream.write(byteStream.toByteArray());
                outputStream.write(_0);

                // Copy back the remaining part of the image by skipping the original EXIF marker
                // TODO: make sure we still work within the buffer boundaries 
                outputStream.write(buffer, app1MarkerPos + originalAPP1MarkerLength + 2, readlength
                        - (app1MarkerPos + originalAPP1MarkerLength + 2));
            } else {
                outputStream.write(buffer, 0, readlength);
            }
        }
    }

    /**
     * Update the EXIF content of the image stored in the specified byte array, using the 
     * content available in the input {@link ByteArrayOutputStream}. Store the result
     * to the specified {@link OutputStream}
     * 
     * @param outputStream The stream where to write
     * @param byteStream the byte stream containing exif metadata to be written
     * @param imageData the original image bytes
     * @param imageDataSize the portion of the image bytes array to be used
     * @throws IOException
     */
    private static void updateFromBytes(
            final OutputStream outputStream, 
            final ByteArrayOutputStream byteStream,
            final byte[] imageData, 
            final int imageDataSize) throws IOException {
        int dqtMarkerPos = locateFirst(imageData, DQT_MARKER);
        
        // Look for the DQT marker
        if (dqtMarkerPos != -1) {

            // copy back the first part of the image bytes
            outputStream.write(imageData, 0, dqtMarkerPos);
            
            // insert the EXIF content
            outputStream.write(byteStream.toByteArray());
            outputStream.write(_0);
            
            // continue copying back the remaining part of data
            outputStream.write(imageData, dqtMarkerPos, imageDataSize - dqtMarkerPos);
            outputStream.flush();
        }
    }

    /**
     * Write the IFDs referred by the tags list argument, to the specified stream.
     * No content referred by a offset, is written.  
     * 
     * @param stream the {@link ByteArrayOutputStream} where to append the IFDs.
     * @param tags the IFDs to be written. In order to respect the TIFF specification, make
     * sure the element of this list are sorted in ascending order.
     * @throws IOException
     */
    private static void writeIFDs(
            final ByteArrayOutputStream stream, 
            final List<TIFFTagWrapper> tags)
            throws IOException {
        if (stream == null) {
            throw new IllegalArgumentException("Null stream has been provided");
        }
        for (TIFFTagWrapper tag : tags) {
            stream.write(tagAsBytes(tag, true));
        }

    }

    /**
     * Write the content of the IFDs referred by the tags list argument, to the specified stream.
     * Note that only the content of the IFDs having an offset will be written.
     * 
     * @param stream the {@link ByteArrayOutputStream} where to append the IFDs content.
     * @param tags the Tags list to be written. In order to respect the TIFF specification, make
     * sure the element of this list are sorted in ascending order.
     * @throws IOException
     */
    private static void writeTagsContent(
            final ByteArrayOutputStream stream, 
            final List<TIFFTagWrapper> tags)
            throws IOException {
        
        // Scan tags looking for entries which has a not null content to be written
        for (TIFFTagWrapper tag : tags) {
            if (tag.getContent() != null) {

                //Make sure to write prefix and suffix bytes if present
                if (tag.getPrefix() != null) {
                    stream.write(tag.getPrefix());
                }
                stream.write((byte[]) tag.getContent());
                if (tag.getSuffix() != null) {
                    stream.write(tag.getSuffix());
                }
            }
        }
    }

    /**
     * Simply computes the sum of the value provided in the array.
     * @param values
     * @return
     */
    final static int sum(final int values[]) {
        int sum = 0;
        for (int s : values) {
            sum += s;
        }
        return sum;
    }

    /**
     * Scan the IFDs contained in the input tags collection.
     * Store the offsetValue and size of each entry in the proper array.
     * 
     * @param tags the tags to be scan
     * @param baseOffset the baseOffset to be taken into account when computing 
     *          the tags offsets
     * @param valueOffsets the array where to put found valueOffsets
     * @param sizes the array where to put found sizes
     */
    private static void computeOffsetsAndSizes(
            final List<TIFFTagWrapper> tags, 
            final int baseOffset,
            final int[] valueOffsets, 
            final int[] sizes) {
        final int elementsSize = tags.size();
        int i = 0;
        int previousOffset = 0;
        
        // Scan the tags list
        for (TIFFTagWrapper tag : tags) {
            
            // Offsets refer to position in the stream after the IFD, where the content will be written
            valueOffsets[i] = baseOffset + elementsSize * IFD_LENGTH + previousOffset;
            if (tag.getNumber() == EXIFParentTIFFTagSet.TAG_EXIF_IFD_POINTER) {
                // Special case, being a pointer, make sure to set the offset as value,
                // without byte counts increment
                tag.setValue(valueOffsets[i]);
            } else if (tag.getContent() != null) {
                tag.setValue(valueOffsets[i]);
                previousOffset += tag.getCount();
                sizes[i] = tag.getCount();
            } else {
                sizes[i] = 0;
            }
            i++;
        }
    }

    /**
     * Simple utility method returning a 2 bytes representation of a value, in compliance with the
     * specified endianness 
     * 
     * @param value the value to be represented through 2 bytes
     * @param isBigEndian {@code true} in case of bigEndian
     * @return
     */
    public final static byte[] intToBytes(final int value, final boolean isBigEndian) {
        if (isBigEndian) {
            return new byte[] { (byte) ((value >> 8) & 0xFF), (byte) (value & 0xFF) };
        } else {
            return new byte[] { (byte) (value & 0xFF), (byte) ((value >> 8) & 0xFF) };
        }
    }
    
    /**
     * Simple utility method returning a 2 bytes representation of a value as bigEndian
     * 
     * @param value the value to be represented through 2 bytes
     * @return
     */
    public final static byte[] intToBytes(final int value) {
        return intToBytes(value, true);
    }

    /**
     * Simple utility methods returning an int built on top of 2 bytes, in compliance
     * with the specified endianness
     * @param buff the buffer containing 2 bytes to be transformed
     * @param start the position within the buffer of the first byte to be transformed 
     * @param isBigEndian {@code true} in case we need to encode it as bigEndian
     * @return
     */
    public final static int bytes2ToInt(byte[] buff, int start, final boolean isBigEndian) {
        int intValue = 0;
        intValue |= buff[start + (isBigEndian ? 0 : 1)] & 0xFF;
        intValue <<= 8;
        intValue |= buff[start + (isBigEndian ? 1 : 0)] & 0xFF;
        return intValue;
    }

    /**
     * Simple utility methods returning an int built on top of 4 bytes, in compliance
     * with the specified endianness
     * @param buff the buffer containing 4 bytes to be transformed
     * @param start the position within the buffer of the first byte to be transformed 
     * @param isBigEndian {@code true} in case we need to encode it as bigEndian
     * @return
     */
    public final static int bytes4ToInt(byte[] buff, int start, final boolean isBigEndian) {
        int intValue = 0;
        intValue |= buff[start + (isBigEndian ? 0 : 3)] & 0xFF;
        intValue <<= 8;
        intValue |= buff[start + (isBigEndian ? 1 : 2)] & 0xFF;
        intValue <<= 8;
        intValue |= buff[start + (isBigEndian ? 2 : 1)] & 0xFF;
        intValue <<= 8;
        intValue |= buff[start + (isBigEndian ? 3 : 0)] & 0xFF;
        return intValue;
    }

    /**
     * Return the specified {@link TIFFTagWrapper} as a byte array, by taking endianness into
     * account
     * 
     * @param tag
     * @param isBigEndian
     * @return
     */
    private static byte[] tagAsBytes(final TIFFTagWrapper tag, final boolean isBigEndian) {
        final byte[] output = new byte[IFD_LENGTH];
        final int number = tag.getNumber();
        final int type = tag.getType();
        final int count = tag.getCount();
        final int offset = tag.getValue();
        output[isBigEndian ? 1 : 0] = (byte) (number & 0xFF);
        output[isBigEndian ? 0 : 1] = (byte) ((number >> 8) & 0xFF);
        output[isBigEndian ? 3 : 2] = (byte) (type & 0xFF);
        output[isBigEndian ? 2 : 3] = (byte) ((type >> 8) & 0xFF);
        output[isBigEndian ? 7 : 4] = (byte) (count & 0xFF);
        output[isBigEndian ? 6 : 5] = (byte) ((count >> 8) & 0xFF);
        output[isBigEndian ? 5 : 6] = (byte) ((count >> 16) & 0xFF);
        output[isBigEndian ? 4 : 7] = (byte) ((count >> 24) & 0xFF);
        output[isBigEndian ? 11 : 8] = (byte) (offset & 0xFF);
        output[isBigEndian ? 10 : 9] = (byte) ((offset >> 8) & 0xFF);
        output[isBigEndian ? 9 : 10] = (byte) ((offset >> 16) & 0xFF);
        output[isBigEndian ? 8 : 11] = (byte) ((offset >> 24) & 0xFF);
        return output;

    }

    /**
     * Scan the input byte buffer, looking for a candidate bytes sequence and return
     * the index of the first occurrence within the input buffer, or -1 in case nothing
     * is found.   
     */
    public static int locateFirst(final byte[] buffer, final byte[] candidate) {
        if (IsEmptyLocate(buffer, candidate)) {
            return -1;
        }
        for (int i = 0; i < buffer.length; i++) {
            if (!IsMatch(buffer, i, candidate)) {
                continue;
            }
            return i;
        }
        return -1;
    }

    /**
     * Scan the input buffer, starting from the specified position, and check whether it matches the
     * candidate byte sequence.
     * 
     * @param buffer the byte array to be scanned
     * @param start the starting index 
     * @param candidate the byte array to be matched
     * @return {@code true} in case the buffer match the candidate sequence
     */
    final static boolean IsMatch(final byte[] buffer, final int start, final byte[] candidate) {
        if (candidate.length > (buffer.length - start)) {
            return false;
        }

        for (int i = 0; i < candidate.length; i++) {
            if (buffer[start + i] != candidate[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Simple utility method which check for conditions which won't allow to get a match
     * from the scan. As an instance, a null candidate byte array or a candidate array longer
     * than the array to be scan won't allow to get a match.  
     * @param toBeScan
     * @param candidate
     * @return {@code false} in case the needed conditions for matching aren't satisfied.
     */
    static boolean IsEmptyLocate(byte[] toBeScan, byte[] candidate) {
        return (toBeScan == null) || (candidate == null) || (toBeScan.length == 0)
                || (candidate.length == 0) || (candidate.length > toBeScan.length);
    }

    /** 
     * Replace the EXIF contained within a file referred by a {@link FileImageInputStreamExt} instance
     * with the EXIF represented by the specified {@link EXIFMetadata} instance. The original file
     * will be overwritten by the new one containing updated EXIF.
     * 
     * It is worth to point out that this replacing method won't currently perform any fields delete, 
     * but simply content update. Therefore, tags in the original EXIF which are missing in the 
     * updated EXIF parameter, won't be modified. 
     * 
     * @param inputStream a {@link FileImageInputStreamExt} referring to a JPEG containing EXIF 
     * @param exif the {@link EXIFMetadata} instance containing tags to be updated
     */
    public static void replaceEXIFs(
            final FileImageInputStreamExt inputStream, 
            final EXIFMetadata exif)
            throws IOException {
        
        EXIFMetadataWrapper exifMarker = parseExifMetadata(inputStream, exif);
        EXIFMetadata updatedExif = exifMarker.getExif();
        
        final int app1Length = exifMarker.getLength();
        if (updatedExif != null){
            // Create a temp file where to store the updated EXIF
            final File file = File.createTempFile("replacingExif", ".exif");
            final OutputStream fos = new FileOutputStream(file);
            updateStream(fos, inputStream, updatedExif, app1Length);
            final File previousFile = inputStream.getFile();
            FileUtils.deleteQuietly(previousFile);
            FileUtils.moveFile(file, previousFile);
        }
    }

    /**
     * Return an {@link EXIFMetadataWrapper} made of an {@link EXIFMetadata} setup on top of the 
     * EXIF found in the inputStream, merged with the EXIF found on the specified 
     * exif parameter (if any). The EXIF tags contained in the specified parameter will override
     * the ones found within the inputStream. 
     * The returned wrapper will also contain the length of the APP1 marker of the original EXIF.
     * 
     * @param inputStream a {@link FileImageInputStreamExt} referring to a JPEG containing EXIF 
     * @param exif the optional {@link EXIFMetadata} instance containing tags to be updated
     * @return a {@link EXIFMetadataWrapper} containing the merged/updated EXIF as well as the 
     * length of the original APP1 marker 
     * @throws IOException
     */
    private static EXIFMetadataWrapper parseExifMetadata(
            final FileImageInputStreamExt inputStream, 
            final EXIFMetadata exif) throws IOException {
        
        List<TIFFTagWrapper> baselineTags = null;
        List<TIFFTagWrapper> exifTags = null;
        int app1Length = -1;
        if (exif != null){
            // Get the updated Tags
            baselineTags = exif.getList(Type.BASELINE);
            exifTags = exif.getList(Type.EXIF);
        }
        
        inputStream.mark();
        final Map<Integer, TIFFTagWrapper> foundBaseLineTags = new TreeMap<Integer, TIFFTagWrapper>();
        final Map<Integer, TIFFTagWrapper> foundExifTags = new TreeMap<Integer, TIFFTagWrapper>();
        
        EXIFMetadata updatedExif = null;
        final byte[] buff = new byte[EXIF_SCAN_BUFFER_SIZE];
        boolean contains_EXIF_IFD = false;
        boolean found = false;
        
        // Scan the stream looking for exif tags
        while ((inputStream.read(buff)) != -1) {

            // Look for the EXIF section (referred by the APP1 marker)
            int exifTagPos = found ? -1 : locateFirst(buff, APP1_MARKER);
            int pos = 0;
            if (exifTagPos != -1) {
                found = true;
                pos = exifTagPos;
                
                // Get the original EXIF length
                app1Length = bytes2ToInt(buff, pos + APP1_MARKER.length, true);
                final boolean isBigEndian = buff[pos + APP1_MARKER.length + 2 + EXIF_MARKER.length] == 'M'
                        && buff[pos + APP1_MARKER.length + 2 + 1 + EXIF_MARKER.length] == 'M';

                // Initialize number of tags
                final int numBaselineTags = bytes2ToInt(buff, pos + APP1_MARKER.length + 2 + EXIF_MARKER.length
                        + TIFF_HEADER.length, isBigEndian);
                int skip = 0;
                int start = pos + APP1_MARKER.length + 2 + EXIF_MARKER.length;
                int globalContentLength = 0;
                
                for (int i = 0; i < numBaselineTags; i++) {
                    skip = start + TIFF_HEADER.length + 2 + i * IFD_LENGTH;
                    
                    // Retrieve TIFF Tag information by scanning the buffer (Tag Number, type, count, offsetValue) 
                    int number = bytes2ToInt(buff, skip, isBigEndian);
                    int type = bytes2ToInt(buff, skip + 2, isBigEndian);
                    int count = bytes4ToInt(buff, skip + 4, isBigEndian);
                    int offsetValue = bytes4ToInt(buff, skip + 8, isBigEndian);
                    Object content = null;
                    
                    // In case we find an IFDPointer during the scan, we need to take note of this
                    if (number == EXIFParentTIFFTagSet.TAG_EXIF_IFD_POINTER) {
                        contains_EXIF_IFD = true;
                    }
                    // Increase the globalContentLength when finding specific fields
                    if (type == TIFFTag.TIFF_ASCII || type == TIFFTag.TIFF_UNDEFINED) {
                        content = new byte[count];
                        System.arraycopy(buff, offsetValue + start, content, 0, count);
                        globalContentLength += count;
                    }
                    
                    // Check whether the TAG found in the original EXIF is also contained
                    // in the EXIF to be updated
                    TIFFTagWrapper updatedTag = null;
                    for (TIFFTagWrapper tag : baselineTags) {
                        if (tag.getNumber() == number) {
                            updatedTag = tag;
                            break;
                        }
                    }
                    
                    // Update the Map of merged EXIFs 
                    if (updatedTag == null) {
                        
                        // In case the list of EXIF to be merged doesn't contain this tag id,
                        // take the one from the original EXIF to preserve its value
                        updatedTag = new TIFFTagWrapper(number, type, content, offsetValue,
                                count, null, null);
                    }
                    foundBaseLineTags.put(number, updatedTag);
                }

                // Handle the EXIF Specific tags
                if (contains_EXIF_IFD) {
                    start = skip + globalContentLength + IFD_LENGTH + 4; // 4 due to 4 0x00 to start
                                                                         // the IFD values
                    int numExifFields = bytes2ToInt(buff, start, isBigEndian);

                    for (int i = 0; i < numExifFields; i++) {
                        skip = start + 2 + i * IFD_LENGTH;
                        
                        // Retrieve TIFF Tag information by scanning the buffer (Tag Number, type, count, offsetValue)
                        int number = bytes2ToInt(buff, skip, isBigEndian);
                        int type = bytes2ToInt(buff, skip + 2, isBigEndian);
                        int count = bytes4ToInt(buff, skip + 4, isBigEndian);
                        int offsetValue = bytes4ToInt(buff, skip + 8, isBigEndian);
                        Object content = null;
                        
                        // Increase the globalContentLength when finding specific fields
                        if (type == TIFFTag.TIFF_ASCII || type == TIFFTag.TIFF_UNDEFINED) {
                            content = new byte[count];
                            System.arraycopy(buff, offsetValue + start, content, 0, count);
                            globalContentLength += count;
                        }
                        
                        // Check whether the TAG found in the original EXIF is also contained
                        // in the EXIF to be updated
                        TIFFTagWrapper updatedTag = null;
                        for (TIFFTagWrapper tag : exifTags) {
                            if (tag.getNumber() == number) {
                                updatedTag = tag;
                                break;
                            }
                        }
                        
                        // Update the Map of merged EXIFs  
                        if (updatedTag == null) {
                            
                            // In case the list of EXIF to be merged doesn't contain this tag id,
                            // take the one from the original EXIF to preserve its value
                            updatedTag = new TIFFTagWrapper(number, type, content, offsetValue,
                                    count, null, null);
                        }
                        foundExifTags.put(number, updatedTag);
                    }
                }

                List<TIFFTagWrapper> mergedBaselineTags = null;
                List<TIFFTagWrapper> mergedExifTags = null;
                if (!foundBaseLineTags.isEmpty()) {
                    mergedBaselineTags = new ArrayList<TIFFTagWrapper>(foundBaseLineTags.values());

                }
                if (!foundExifTags.isEmpty()) {
                    mergedExifTags = new ArrayList<TIFFTagWrapper>(foundExifTags.values());
                }

                // Setup a new EXIF Metadata object containing all the EXIFs to be put
                updatedExif = new EXIFMetadata(mergedBaselineTags, mergedExifTags);
            }
        }
        inputStream.reset();
        return new EXIFMetadataWrapper(updatedExif, app1Length);
    }

    /**
     * @param tagNumber
     * @return
     */
    public static TIFFTagWrapper createTag(int tagNumber) {
        switch (tagNumber){
        case EXIFTags.USER_COMMENT:
            return new TIFFTagWrapper(EXIFTags.USER_COMMENT, TIFFTag.TIFF_UNDEFINED, null, -1, 0, 
                    EXIFUtilities.USER_COMMENT_ASCII_CHAR_CODE, null);
        case EXIFTags.COPYRIGHT:
            return new TIFFTagWrapper(EXIFTags.COPYRIGHT, TIFFTag.TIFF_ASCII, null, 0, 0, 
                    null, EXIFUtilities.NULL_STRING);
        case EXIFTags.EXIF_IFD_POINTER:
            return new TIFFTagWrapper(EXIFTags.EXIF_IFD_POINTER, TIFFTag.TIFF_LONG, null, -1, 1);
        }
        return null;
    }
}
