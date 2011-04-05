/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2008, GeoSolutions
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
package it.geosolutions.imageio.plugins.jp2k;

import it.geosolutions.imageio.plugins.jp2k.box.ASOCBox;
import it.geosolutions.imageio.plugins.jp2k.box.BoxUtilities;
import it.geosolutions.imageio.plugins.jp2k.box.XMLBox;
import it.geosolutions.resources.TestData;

import java.io.File;
import java.io.IOException;

import javax.imageio.metadata.IIOMetadata;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Experimental test class for JP2Boxes parsing
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @todo remove System.out
 */
public class JP2BoxesTest extends AbstractJP2KakaduTestCase {

    private final static String fileName = "jpx.jpx";

    // // private final static String fileName3 = "simple.jpx";
    //
    // private final static String fileName3 = "bogota.jp2";

    // private final static String fileName2 = "example.jp2";


    @org.junit.Test
    public void testBoxInfo() throws IOException {
        if (!runTests)
            return;
        File file = new File(fileName);
        if (!file.exists()) {
            file = TestData.file(this, fileName);
        }
        // File file2 = new File(fileName2);
        // if (!file2.exists()) {
        // file2 = TestData.file(this, fileName2);
        // }
        //
        // File file3 = new File(fileName3);
        // if (!file3.exists()) {
        // file3 = TestData.file(this, fileName3);
        // }

        JP2KKakaduImageReader reader = new JP2KKakaduImageReader(
                new JP2KKakaduImageReaderSpi());

        reader.setInput(file);
        // visualize(reader.readAsRenderedImage(0, null), "");
        IIOMetadata imageMetadata = reader.getImageMetadata(0);
        IIOMetadata streamMetadata = reader.getStreamMetadata();
        reader.dispose();

        // reader.setInput(file2);
        // IIOMetadata imageMetadata2 = reader.getImageMetadata(0);
        // IIOMetadata streamMetadata2 = reader.getStreamMetadata();
        // reader.dispose();
        //
        // reader.setInput(file3);
        // IIOMetadata imageMetadata3 = reader.getImageMetadata(0);
        // IIOMetadata streamMetadata3 = reader.getStreamMetadata();
        // reader.dispose();

        if (TestData.isInteractiveTest()) {
            if (imageMetadata != null)
                displayImageIOMetadata(imageMetadata
                        .getAsTree(JP2KImageMetadata.nativeMetadataFormatName));
            if (streamMetadata != null)
                displayImageIOMetadata(streamMetadata
                        .getAsTree(JP2KStreamMetadata.nativeMetadataFormatName));
        }

        // if (imageMetadata2 != null)
        // displayImageIOMetadata(imageMetadata2
        // .getAsTree(JP2KImageMetadata.nativeMetadataFormatName));
        // if (streamMetadata2 != null)
        // displayImageIOMetadata(streamMetadata2
        // .getAsTree(JP2KStreamMetadata.nativeMetadataFormatName));
        //
        // if (imageMetadata3 != null) {
        // Node treeNode = imageMetadata3
        // .getAsTree(JP2KImageMetadata.nativeMetadataFormatName);
        // displayImageIOMetadata(treeNode);
        // }
        // if (streamMetadata3 != null)
        // displayImageIOMetadata(streamMetadata3
        // .getAsTree(JP2KStreamMetadata.nativeMetadataFormatName));

    }

    public static void displayImageIOMetadata(Node root) {
        displayMetadata(root, 0);
    }

    static void indent(int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("  ");
        }
    }

    static void displayMetadata(Node node, int level) {
        indent(level); // emit open tag
        String nodeName = node.getNodeName();
        System.out.print("<" + nodeName);
        NamedNodeMap map = node.getAttributes();
        // int attributeLength = -1;
        if (map != null) { // print attribute values
            int length = map.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = map.item(i);
                String attribName = attr.getNodeName();
                String attribValue = attr.getNodeValue();
                // if (attribName.equalsIgnoreCase("Length")) {
                // attributeLength = Integer.parseInt(attribValue);
                // }
                System.out.print(" " + attribName + "=\"" + attribValue + "\"");
            }
        }
        System.out.print(">"); // close current tag
        if ((nodeName.equalsIgnoreCase(BoxUtilities.getName(XMLBox.BOX_TYPE)) || nodeName
                .equalsIgnoreCase(BoxUtilities.getName(ASOCBox.BOX_TYPE)))) {
            System.out.println(" VALUE HAS BEEN SKIPPED FROM VISUALIZATION");
        } else {
            String nodeValue = node.getNodeValue();
            if (nodeValue != null)
                System.out.println(" " + nodeValue);
            else
                System.out.println("");
        }
        Node child = node.getFirstChild();
        if (child != null) {
            while (child != null) { // emit child tags recursively
                displayMetadata(child, level + 1);
                child = child.getNextSibling();
            }
            indent(level); // emit close tag
            System.out.println("</" + node.getNodeName() + ">");
        } else {
            // System.out.println("/>");
        }
    }

}
