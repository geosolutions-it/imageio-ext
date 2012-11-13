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
package it.geosolutions.imageio.plugins.jp2k.box;

import it.geosolutions.imageio.plugins.jp2k.JP2KBox;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataNode;

import kdu_jni.Jp2_input_box;
import kdu_jni.KduException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Simone Giannecchini, GeoSolutions
 * @author Daniele Romagnoli, GeoSolutions
 */
public class BoxUtilities {
	
    private final static Logger LOGGER = Logger.getLogger("BoxUtilities");

    public final static String JP2_ASOC_LBL_GML_DATA = "gml.data";
    
    /**
     * A Hashtable contains the class names for each type of the boxes. This
     * table will be used to construct a Box object from a Node object by using
     * reflection.
     */
    public final static Map <Integer, Class<? extends BaseJP2KBox>> boxClasses = new HashMap <Integer, Class<? extends BaseJP2KBox>>();

    public final static Map <Integer, String> boxNames = new HashMap <Integer, String>();

    /**
     * The table to link tag names for all the JP2 boxes.
     */
    public final static Map<Integer, String> names = new HashMap<Integer, String>();
    
    
    public final static Set<String> SUPERBOX_NAMES= new HashSet<String>();
    
        // Initializes the "SUPERBOX_NAMES" set
    static {
    	SUPERBOX_NAMES.add(JP2HeaderBox.NAME);
    	SUPERBOX_NAMES.add(ResolutionBox.NAME);
    	SUPERBOX_NAMES.add(UUIDInfoBox.NAME);
    	SUPERBOX_NAMES.add(ASOCBox.NAME);
    	SUPERBOX_NAMES.add(CodestreamHeaderBox.NAME);
    	SUPERBOX_NAMES.add(CompositingLayerHeaderBox.NAME);
    }

    // Initializes the hash table "names".
    static {
        
        names.put(JP2KFileBox.BOX_TYPE, JP2KFileBox.JP2K_MD_NAME);
        
        // children for the root
        names.put(SignatureBox.BOX_TYPE, SignatureBox.JP2K_MD_NAME);
        names.put(FileTypeBox.BOX_TYPE, FileTypeBox.JP2K_MD_NAME);

        // children for the boxes other than
        // JPEG2000SignatureBox/JPEG2000FileTypeBox
        names.put(IPRBox.BOX_TYPE, IPRBox.JP2K_MD_NAME);
        names.put(XMLBox.BOX_TYPE, XMLBox.JP2K_MD_NAME);

        // Children of HeadCStream
        names.put(JP2HeaderBox.BOX_TYPE, JP2HeaderBox.JP2K_MD_NAME);
        names.put(ContiguousCodestreamBox.BOX_TYPE, ContiguousCodestreamBox.JP2K_MD_NAME);
        
        // Children of JPEG2000HeaderSuperBox
        names.put(ImageHeaderBox.BOX_TYPE, ImageHeaderBox.JP2K_MD_NAME);

        // Optional boxes in JPEG2000HeaderSuperBox
        names.put(BitsPerComponentBox.BOX_TYPE, BitsPerComponentBox.JP2K_MD_NAME);
        names.put(ColorSpecificationBox.BOX_TYPE, ColorSpecificationBox.JP2K_MD_NAME);
        names.put(PaletteBox.BOX_TYPE, PaletteBox.JP2K_MD_NAME);
        names.put(ComponentMappingBox.BOX_TYPE, ComponentMappingBox.JP2K_MD_NAME);
        names.put(ChannelDefinitionBox.BOX_TYPE, ChannelDefinitionBox.JP2K_MD_NAME);
        names.put(ResolutionBox.BOX_TYPE, ResolutionBox.JP2K_MD_NAME);
        names.put(ASOCBox.BOX_TYPE, ASOCBox.JP2K_MD_NAME);

        // Children of JPEG2000ResolutionBox
        names.put(ResolutionBox.BOX_TYPE_CAPTURE,
                "JPEG2000CaptureResolutionBox");
        names.put(ResolutionBox.BOX_TYPE_DEFAULT_DISPLAY,
                "JPEG2000DefaultDisplayResolutionBox");

        
        
        // Children of JPEG2000UUIDInfoBox
        names.put(UUIDBox.BOX_TYPE, UUIDBox.JP2K_MD_NAME);
        names.put(UUIDInfoBox.BOX_TYPE, UUIDInfoBox.JP2K_MD_NAME);
        names.put(UUIDListBox.BOX_TYPE, UUIDListBox.JP2K_MD_NAME);
        names.put(DataEntryURLBox.BOX_TYPE, DataEntryURLBox.JP2K_MD_NAME);
        
        // JPX rreq
        names.put(ReaderRequirementsBox.BOX_TYPE, ReaderRequirementsBox.JP2K_MD_NAME);  
        names.put(CodestreamHeaderBox.BOX_TYPE, CodestreamHeaderBox.JP2K_MD_NAME);
        names.put(CompositingLayerHeaderBox.BOX_TYPE, CompositingLayerHeaderBox.JP2K_MD_NAME);
        
        
        // JPX Label Box 
        names.put(LabelBox.BOX_TYPE, LabelBox.JP2K_MD_NAME); 
    }

    // Initializes the hash table "boxClasses".
    static {
        // children for the root
        boxClasses.put(JP2KFileBox.BOX_TYPE, JP2KFileBox.class);
        
        boxClasses.put(SignatureBox.BOX_TYPE, SignatureBox.class);
        boxClasses.put(FileTypeBox.BOX_TYPE, FileTypeBox.class);

        // children for the boxes other than
        // JPEG2000SignatureBox/JPEG2000FileTypeBox
        boxClasses.put(IPRBox.BOX_TYPE, IPRBox.class);
        boxClasses.put(XMLBox.BOX_TYPE, XMLBox.class);

        boxClasses.put(JP2HeaderBox.BOX_TYPE, JP2HeaderBox.class);
        boxClasses.put(ContiguousCodestreamBox.BOX_TYPE, ContiguousCodestreamBox.class);

        // Children of JPEG2000HeaderSuperBox
        boxClasses.put(ImageHeaderBox.BOX_TYPE, ImageHeaderBox.class);

        // Optional boxes in JPEG2000HeaderSuperBox
        boxClasses.put(BitsPerComponentBox.BOX_TYPE, BitsPerComponentBox.class);
        boxClasses.put(ColorSpecificationBox.BOX_TYPE, ColorSpecificationBox.class);
        boxClasses.put(PaletteBox.BOX_TYPE, PaletteBox.class);
        boxClasses.put(ComponentMappingBox.BOX_TYPE, ComponentMappingBox.class);
        boxClasses.put(ChannelDefinitionBox.BOX_TYPE, ChannelDefinitionBox.class);
        boxClasses.put(ResolutionBox.BOX_TYPE, ResolutionBox.class);
        
        boxClasses.put(ASOCBox.BOX_TYPE, ASOCBox.class);

        // Children of JPEG2000ResolutionBox
        boxClasses.put(ResolutionBox.BOX_TYPE_CAPTURE, CaptureResolutionBox.class);
        boxClasses.put(ResolutionBox.BOX_TYPE_DEFAULT_DISPLAY, DefaultDisplayResolutionBox.class);

        // Children of JPEG2000UUIDInfoBox
        boxClasses.put(UUIDInfoBox.BOX_TYPE, UUIDInfoBox.class);
        
        
        boxClasses.put(UUIDBox.BOX_TYPE, UUIDBox.class);
        boxClasses.put(UUIDListBox.BOX_TYPE, UUIDListBox.class);
        boxClasses.put(DataEntryURLBox.BOX_TYPE, DataEntryURLBox.class);
        
        // JPX rreq
        boxClasses.put(ReaderRequirementsBox.BOX_TYPE, ReaderRequirementsBox.class);   
        
        boxClasses.put(CodestreamHeaderBox.BOX_TYPE, CodestreamHeaderBox.class);
        boxClasses.put(CompositingLayerHeaderBox.BOX_TYPE, CompositingLayerHeaderBox.class);
        
        // JPX Label Box 
        boxClasses.put(LabelBox.BOX_TYPE, LabelBox.class); 
    }

    static {
        // children for the root
        boxNames.put(JP2KFileBox.BOX_TYPE, JP2KFileBox.NAME);
        boxNames.put(SignatureBox.BOX_TYPE, SignatureBox.NAME);
        boxNames.put(FileTypeBox.BOX_TYPE, FileTypeBox.NAME);

        // children for the boxes other than
        // JPEG2000SignatureBox/JPEG2000FileTypeBox
        boxNames.put(IPRBox.BOX_TYPE, IPRBox.NAME);
        boxNames.put(XMLBox.BOX_TYPE, XMLBox.NAME);

        // Children of HeadCStream
        boxNames.put(JP2HeaderBox.BOX_TYPE, JP2HeaderBox.NAME);
        boxNames.put(ContiguousCodestreamBox.BOX_TYPE, ContiguousCodestreamBox.NAME);
        
        boxNames.put(ASOCBox.BOX_TYPE, ASOCBox.NAME);

        // Children of JPEG2000HeaderSuperBox
        boxNames.put(ImageHeaderBox.BOX_TYPE, ImageHeaderBox.NAME);

        // Optional boxes in JPEG2000HeaderSuperBox
        boxNames.put(BitsPerComponentBox.BOX_TYPE, BitsPerComponentBox.NAME);
        boxNames.put(ColorSpecificationBox.BOX_TYPE, ColorSpecificationBox.NAME);
        boxNames.put(PaletteBox.BOX_TYPE, PaletteBox.NAME);
        boxNames.put(ComponentMappingBox.BOX_TYPE, ComponentMappingBox.NAME);
        boxNames.put(ChannelDefinitionBox.BOX_TYPE, ChannelDefinitionBox.NAME);
        boxNames.put(ResolutionBox.BOX_TYPE, ResolutionBox.NAME);

        // Children of JPEG2000ResolutionBox
        boxNames.put(ResolutionBox.BOX_TYPE_CAPTURE, CaptureResolutionBox.CAP_NAME);
        boxNames.put(ResolutionBox.BOX_TYPE_DEFAULT_DISPLAY, DefaultDisplayResolutionBox.DEF_NAME);

        boxNames.put(UUIDBox.BOX_TYPE, UUIDBox.NAME);
        boxNames.put(UUIDInfoBox.BOX_TYPE, UUIDInfoBox.NAME);
        
        // Children of JPEG2000UUIDInfoBox
        boxNames.put(UUIDListBox.BOX_TYPE, UUIDListBox.NAME);
        boxNames.put(DataEntryURLBox.BOX_TYPE, DataEntryURLBox.NAME);
        
        // JPX rreq
        boxNames.put(ReaderRequirementsBox.BOX_TYPE, ReaderRequirementsBox.NAME);
        
        boxNames.put(CodestreamHeaderBox.BOX_TYPE, CodestreamHeaderBox.NAME);
        boxNames.put(CompositingLayerHeaderBox.BOX_TYPE, CompositingLayerHeaderBox.NAME);
        
        // JPX Label Box 
        boxNames.put(LabelBox.BOX_TYPE, LabelBox.NAME); 
    }

    /**
     * Copies that four bytes of an integer into the byte array. Necessary for
     * the subclasses to compose the content array from the data elements
     */
    public static void copyInt(final byte[] data, int pos, final int value) {
        data[pos++] = (byte) ((value >> 24)& 0xFF);
        data[pos++] = (byte) ((value >> 16)& 0xFF);
        data[pos++] = (byte) ((value >> 8)& 0xFF);
        data[pos++] = (byte) (value );
    }

    /**
     * Creates a <code>Box</code> object with the provided <code>type</code>
     * based on the provided Node object based on reflection.
     * 
     * @todo handle Exception
     */
    public static JP2KBox createBox(int type, Node node) throws IIOInvalidTreeException {
        Class<? extends JP2KBox> boxClass =boxClasses.get(new Integer(type));

        try {
            // gets the constructor with <code>Node</code> parameter
            Constructor<? extends JP2KBox> cons = boxClass.getConstructor(new Class[] { Node.class });
            if (cons != null) {
                return cons.newInstance(new Object[] { node });
            }
        } catch (NoSuchMethodException e) {
        	LOGGER.log(Level.SEVERE,e.getLocalizedMessage(),e);
        } catch (InvocationTargetException e) {
        	LOGGER.log(Level.SEVERE,e.getLocalizedMessage(),e);
        } catch (IllegalAccessException e) {
        	LOGGER.log(Level.SEVERE,e.getLocalizedMessage(),e);
        } catch (InstantiationException e) {
        	LOGGER.log(Level.SEVERE,e.getLocalizedMessage(),e);
        }
        throw new IllegalArgumentException("The provided type or Node are not valid");
    }

    /**
     * Creates a <code>Box</code> object with the provided <code>type</code>
     * based on the provided data object based on reflection.
     */
    public static JP2KBox createBox(int type, byte[] data) {
        Class<? extends JP2KBox> boxClass =  boxClasses.get(new Integer(type));

        try {
        	//super box elements have default contructors
        	if(data==null)
        		return boxClass.newInstance();
        	
            // gets the constructor with <code>byte[]</code> parameter
            final Constructor<? extends JP2KBox> cons = boxClass.getConstructor(Array.newInstance(
                    byte.class, 0).getClass());
            if (cons != null) {
                return cons.newInstance(new Object[] { data });
            }
        } catch (NoSuchMethodException e) {
        	LOGGER.log(Level.SEVERE,e.getLocalizedMessage(),e);
        } catch (InvocationTargetException e) {
        	LOGGER.log(Level.SEVERE,e.getLocalizedMessage(),e);
        } catch (IllegalAccessException e) {
        	LOGGER.log(Level.SEVERE,e.getLocalizedMessage(),e);
        } catch (InstantiationException e) {
        	LOGGER.log(Level.SEVERE,e.getLocalizedMessage(),e);
        }
        throw new IllegalArgumentException("The provided type or data are not valid");
    }

    /** Extracts the value of the attribute from name. */
    public static Object getAttribute(Node node, String name) {
        NamedNodeMap map = node.getAttributes();
        node = map.getNamedItem(name);
        return (node != null) ? node.getNodeValue() : null;
    }

    /** Gets the byte array from an <code>IIOMetadataNode</code>. */
    public static byte[] getByteArrayElementValue(Node node) {
        if (node instanceof IIOMetadataNode) {
            Object obj = ((IIOMetadataNode) node).getUserObject();
            if (obj instanceof byte[])
                return (byte[]) obj;
        }

        return parseByteArray(node.getNodeValue());
    }

    /** Gets its byte value from an <code>IIOMetadataNode</code>. */
    public static byte getByteElementValue(Node node) {
        if (node instanceof IIOMetadataNode) {
            Object obj = ((IIOMetadataNode) node).getUserObject();
            if (obj instanceof Byte)
                return ((Byte) obj).byteValue();
        }

        String value = node.getNodeValue();
        if (value != null)
            return new Byte(value).byteValue();
        return (byte) 0;
    }

    /** Gets the integer array from an <code>IIOMetadataNode</code>. */
    public static int[] getIntArrayElementValue(Node node) {
        if (node instanceof IIOMetadataNode) {
            Object obj = ((IIOMetadataNode) node).getUserObject();
            if (obj instanceof int[])
                return (int[]) obj;
        }

        return parseIntArray(node.getNodeValue());
    }

    /** Gets its integer value from an <code>IIOMetadataNode</code>. */
    public static int getIntElementValue(Node node) {
        if (node instanceof IIOMetadataNode) {
            Object obj = ((IIOMetadataNode) node).getUserObject();
            if (obj instanceof Integer)
                return ((Integer) obj).intValue();
        }

        String value = node.getNodeValue();
        if (value != null)
            return new Integer(value).intValue();
        return 0;
    }

    /**
     * Returns the XML tag name defined in JP2 XML xsd/dtd for the box with the
     * provided <code>type</code>. If the <code>type</code> is not known,
     * the string <code>"unknown"</code> is returned.
     */
    public static String getName(int type) {
        String name = names.get(new Integer(type));
        return name == null ? "unknown" : name;
    }

    /**
     * Returns the BoxName for the box with the provided <code>type</code>.
     * If the <code>type</code> is not known, the string
     * <code>"unknown"</code> is returned.
     */
    public static String getBoxName(int type) {
        String name = boxNames.get(new Integer(type));
        return name == null ? "unknown" : name;
    }

    /** Gets its short value from an <code>IIOMetadataNode</code>. */
    public static short getShortElementValue(Node node) {
        if (node instanceof IIOMetadataNode) {
            Object obj = ((IIOMetadataNode) node).getUserObject();
            if (obj instanceof Short)
                return ((Short) obj).shortValue();
        }
        String value = node.getNodeValue();
        if (value != null)
            return new Short(value).shortValue();
        return (short) 0;
    }

    /**
     * Gets its <code>String</code> value from an <code>IIOMetadataNode</code>.
     */
    public static String getStringElementValue(Node node) {

        if (node instanceof IIOMetadataNode) {
            Object obj = ((IIOMetadataNode) node).getUserObject();
            if (obj instanceof String)
                return (String) obj;
        }

        return node.getNodeValue();
    }

    /** Returns the type String based on the provided name. */
    public static String getTypeByName(String name) {
        for (Map.Entry<Integer,String> entry:names.entrySet()) {
            if (name.equals(entry.getValue()))
                return getTypeString(entry.getKey());
        }
        return null;
    }

    /**
     * Converts the box type from integer to string. This is necessary because
     * type is defined as String in xsd/dtd and integer in the box classes.
     */
    public static int getTypeInt(String s) {
        byte[] buf = s.getBytes();
        int t = buf[0];
        for (int i = 1; i < 4; i++) {
            t = (t << 8) | buf[i];
        }

        return t;
    }

    /**
     * Converts the box type from integer to string. This is necessary because
     * type is defined as String in xsd/dtd and integer in the box classes.
     */
    public static String getTypeString(int type) {
        byte[] buf = new byte[4];
        for (int i = 3; i >= 0; i--) {
            buf[i] = (byte) (type & 0xFF);
            type >>>= 8;
        }

        return new String(buf);
    }

    /** Parses the byte array expressed by a string. */
    public static byte[] parseByteArray(String value) {
        if (value == null)
            return null;

        StringTokenizer token = new StringTokenizer(value);
        int count = token.countTokens();

        byte[] buf = new byte[count];
        int i = 0;
        while (token.hasMoreElements()) {
            buf[i++] = new Byte(token.nextToken()).byteValue();
        }
        return buf;
    }

    /** Parses the integer array expressed a string. */
    public static int[] parseIntArray(String value) {
        if (value == null)
            return null;

        StringTokenizer token = new StringTokenizer(value);
        int count = token.countTokens();

        int[] buf = new int[count];
        int i = 0;
        while (token.hasMoreElements()) {
            buf[i++] = new Integer(token.nextToken()).intValue();
        }
        return buf;
    }

    /**
     * Returns the Box class for the box with the provided <code>type</code>.
     */
    public static Class<? extends BaseJP2KBox> getBoxClass(int type) {
        switch (type) {
	        case ResolutionBox.BOX_TYPE:
	        case ResolutionBox.BOX_TYPE_CAPTURE:
	        case ResolutionBox.BOX_TYPE_DEFAULT_DISPLAY:
	            return ResolutionBox.class;
        }
        return boxClasses.get(type);
    }

    /**
     * Return the numeric decimal value of an ASCII code representing a
     * Hexadecimal value.
     * 
     * @param c
     *                the ASCII code representing a Hexadecimal value.
     * @return the numeric decimal value of an ASCII code representing a
     *         Hexadecimal value.
     */
    public static int getValue(int c) {
        if (c < 58 && c >= 48)
            return c - 48;
        else if (c < 71 && c >= 65)
            return c - 55;
        return -1;
    }

    /**
     * 
     * @param box
     * @return
     * @throws KduException
     * TODO optimize me
     */
	public static byte[] getContent(final Jp2_input_box box) throws KduException {
	    final int nBytes = (int) box.Get_box_bytes();
	    final byte[] buffer = new byte[nBytes];
	    int readBytes = box.Read(buffer, nBytes);
	    final byte[] destBuffer = new byte[readBytes];
	    System.arraycopy(buffer, 0, destBuffer, 0, readBytes);
	    return destBuffer;
	}
}
