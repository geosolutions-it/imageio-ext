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
package it.geosolutions.imageio.gdalframework;

import it.geosolutions.imageio.utilities.ImageIOUtilities;

import java.awt.BorderLayout;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.List;

import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.sun.media.jai.operator.ImageReadDescriptor;

/**
 * Simple class used to visualize <code>RenderedImages</code>. It may be used
 * within plugin's testCase
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 * 
 * @TODO: fix visualization, graphic elements overlapping/location settings.
 * This class is used in simple tests which require rendering. 
 * It can be surely improved. 
 */
public final class Viewer {

    /** private constructor to prevent instantiation */
    private Viewer() {
    }

    /**
     * An auxiliary simple class containing only contants which are used to
     * handle text building and visualization
     * 
     * @author Daniele Romagnoli
     * 
     */
    private class TextType {
        private final static int ONLY_IMAGE_METADATA = 1;

        private final static int ONLY_STREAM_METADATA = 2;

        private final static int STREAM_AND_IMAGE_METADATA = 3;

        // private final static int PROJECTIONS = 10;
        //
        // private final static int GEOTRANSFORMATION = 11;

        private final static int PROJECT_AND_GEOTRANSF = 12;

        private final static int EVERYTHING = 30;

        private TextType() {

        }
    }

    private final static String newLine = System.getProperty("line.separator");

    /**
     * Most of the following methods have 4 overloaded sorts.
     */

    // //////////////////////////////////////////////////////////////////
    //
    // Image Metadata management methods
    //
    // //////////////////////////////////////////////////////////////////
    /**
     * Visualizes the <code>RenderedImage</code> (if <code>displayImage</code>
     * is <code>true</code>) and displays related image metadata.
     * 
     * @param ri
     *                <code>RenderedImage</code> to visualize
     * @param title
     *                title for the frame. (Usually it is the name of the
     *                originating data source. When the image come from a
     *                multi-subdatasets data source, the title is the subdataset
     *                name)
     * @param index
     *                the index of the required image we need to visualize. This
     *                method will be used when we need to visualize a specific
     *                image contained within a multi-subdataset data source.
     *                Otherwise, when you need to visualize the only one image
     *                related to a single-image format data source you may use
     *                the method without <code>index</code> parameter.
     * @param displayImage
     *                if <code>false</code> only metadata will be displayed
     * 
     */

    public static void visualizeImageMetadata(RenderedImage ri, String title,
            final int index, final boolean displayImage) {
        visualizeWithTextArea(ri, title, TextType.ONLY_IMAGE_METADATA, index,
                displayImage);
    }

    /**
     * Visualizes the <code>RenderedImage</code> and displays related image
     * metadata.
     * 
     * @param ri
     *                <code>RenderedImage</code> to visualize
     * @param title
     *                title for the frame. (Usually it is the name of the
     *                originating data source. When the image come from a
     *                multi-subdatasets data source, the title is the subdataset
     *                name)
     * @param index
     *                the index of the required image we need to visualize. This
     *                method will be used when we need to visualize a specific
     *                image contained within a multi-subdataset data source.
     *                Otherwise, when you need to visualize the only one image
     *                related to a single-image format data source you may use
     *                the method without <code>index</code> parameter.
     */

    public static void visualizeImageMetadata(RenderedImage ri, String title,
            final int index) {
        visualizeWithTextArea(ri, title, TextType.ONLY_IMAGE_METADATA, index,
                true);
    }

    /**
     * Visualizes the <code>RenderedImage</code> (if <code>displayImage</code>
     * is <code>true</code>) and displays related image metadata.
     * 
     * @param ri
     *                <code>RenderedImage</code> to visualize
     * @param title
     *                title for the frame. (Usually it is the name of the
     *                originating data source)
     * @param displayImage
     *                if <code>false</code> only metadata will be displayed
     */
    public static void visualizeImageMetadata(RenderedImage ri, String title,
            final boolean displayImage) {
        visualizeWithTextArea(ri, title, TextType.ONLY_IMAGE_METADATA, 0,
                displayImage);
    }

    /**
     * Visualizes the <code>RenderedImage</code> and displays related image
     * metadata.
     * 
     * @param ri
     *                <code>RenderedImage</code> to visualize
     * @param title
     *                title for the frame. (Usually it is the name of the
     *                originating data source)
     */
    public static void visualizeImageMetadata(RenderedImage ri, String title) {
        visualizeWithTextArea(ri, title, TextType.ONLY_IMAGE_METADATA, 0, true);
    }

    // //////////////////////////////////////////////////////////////////
    //
    // Stream Metadata management methods
    //
    // //////////////////////////////////////////////////////////////////

    /**
     * Visualizes the <code>RenderedImage</code> (if <code>displayImage</code>
     * is <code>true</code>) and displays stream metadata.
     * 
     * @param ri
     *                <code>RenderedImage</code> to visualize
     * @param title
     *                title for the frame. (Usually it is the name of the
     *                originating data source)
     * @param displayImage
     *                if <code>false</code> only metadata will be displayed
     */

    public static void visualizeStreamMetadata(RenderedImage ri, String title,
            final boolean displayImage) {
        visualizeWithTextArea(ri, title, TextType.ONLY_STREAM_METADATA, 0,
                displayImage);
    }

    /**
     * Visualizes the <code>RenderedImage</code> and displays stream metadata.
     * 
     * @param ri
     *                <code>RenderedImage</code> to visualize
     * @param title
     *                title for the frame. (Usually it is the name of the
     *                originating data source)
     */
    public static void visualizeStreamMetadata(RenderedImage ri, String title) {
        visualizeWithTextArea(ri, title, TextType.ONLY_STREAM_METADATA, 0, true);
    }

    // //////////////////////////////////////////////////////////////////
    //
    // Metadata management methods
    //
    // //////////////////////////////////////////////////////////////////

    /**
     * Visualizes the <code>RenderedImage</code> (if <code>displayImage</code>
     * is <code>true</code>) and displays related metadata (both stream
     * metadata and image metadata)
     * 
     * @param ri
     *                <code>RenderedImage</code> to visualize
     * @param title
     *                title for the frame. (Usually it is the name of the
     *                originating data source. When the image come from a
     *                multi-subdatasets data source, the title is the subdataset
     *                name)
     * @param index
     *                the index of the required image we need to visualize. This
     *                method will be used when we need to visualize a specific
     *                image contained within a multi-subdataset data source.
     *                Otherwise, when you need to visualize the only one image
     *                related to a single-image format data source you may use
     *                the method without <code>index</code> parameter.
     * @param displayImage
     *                if <code>false</code> only metadata will be displayed
     * 
     */

    public static void visualizeBothMetadata(RenderedImage ri, String title,
            final int index, final boolean displayImage) {
        visualizeWithTextArea(ri, title, TextType.STREAM_AND_IMAGE_METADATA,
                index, displayImage);
    }

    /**
     * Visualizes the <code>RenderedImage</code> and displays related metadata
     * (both stream metadata and image metadata)
     * 
     * @param ri
     *                <code>RenderedImage</code> to visualize
     * @param title
     *                title for the frame. (Usually it is the name of the
     *                originating data source. When the image come from a
     *                multi-subdatasets data source, the title is the subdataset
     *                name)
     * @param index
     *                the index of the required image we need to visualize. This
     *                method will be used when we need to visualize a specific
     *                image contained within a multi-subdataset data source.
     *                Otherwise, when you need to visualize the only one image
     *                related to a single-image format data source you may use
     *                the method without <code>index</code> parameter.
     */

    public static void visualizeBothMetadata(RenderedImage ri, String title,
            final int index) {
        visualizeWithTextArea(ri, title, TextType.STREAM_AND_IMAGE_METADATA,
                index, true);
    }

    /**
     * Visualizes the <code>RenderedImage</code> (if <code>displayImage</code>
     * is <code>true</code>) and displays related metadata (both stream
     * metadata and image metadata)
     * 
     * @param ri
     *                <code>RenderedImage</code> to visualize
     * @param title
     *                title for the frame. (Usually it is the name of the
     *                originating data source)
     * @param displayImage
     *                if <code>false</code> only metadata will be displayed
     * 
     */

    public static void visualizeBothMetadata(RenderedImage ri, String title,
            final boolean displayImage) {
        visualizeWithTextArea(ri, title, TextType.STREAM_AND_IMAGE_METADATA, 0,
                displayImage);
    }

    /**
     * Visualizes the <code>RenderedImage</code> and displays related
     * metadata. (both stream metadata and image metadata)
     * 
     * @param ri
     *                <code>RenderedImage</code> to visualize
     * @param title
     *                title for the frame. (Usually it is the name of the
     *                originating data source)
     */

    public static void visualizeBothMetadata(RenderedImage ri, String title) {
        visualizeWithTextArea(ri, title, TextType.STREAM_AND_IMAGE_METADATA, 0,
                true);
    }

    // //////////////////////////////////////////////////////////////////
    //
    // CRS management methods
    //
    // //////////////////////////////////////////////////////////////////

    /**
     * Visualizes the <code>RenderedImage</code> (if <code>displayImage</code>
     * is <code>true</code>) and displays related Coordinate Reference System
     * Information
     * 
     * @param ri
     *                <code>RenderedImage</code> to visualize
     * @param title
     *                title for the frame. (Usually it is the name of the
     *                originating data source. When the image come from a
     *                multi-subdatasets data source, the title is the subdataset
     *                name)
     * @param index
     *                the index of the required image we need to visualize. This
     *                method will be used when we need to visualize a specific
     *                image contained within a multi-subdataset data source.
     *                Otherwise, when you need to visualize the only one image
     *                related to a single-image format data source you may use
     *                the method without <code>index</code> parameter.
     * @param displayImage
     *                if <code>false</code> only metadata will be displayed
     * 
     */

    public static void visualizeCRS(RenderedImage ri, String title,
            final int index, final boolean displayImage) {
        visualizeWithTextArea(ri, title, TextType.PROJECT_AND_GEOTRANSF, index,
                displayImage);
    }

    /**
     * Visualizes the <code>RenderedImage</code> and displays related
     * Coordinate Reference System Information
     * 
     * @param ri
     *                <code>RenderedImage</code> to visualize
     * @param title
     *                title for the frame. (Usually it is the name of the
     *                originating data source. When the image come from a
     *                multi-subdatasets data source, the title is the subdataset
     *                name)
     * @param index
     *                the index of the required image we need to visualize. This
     *                method will be used when we need to visualize a specific
     *                image contained within a multi-subdataset data source.
     *                Otherwise, when you need to visualize the only one image
     *                related to a single-image format data source you may use
     *                the method without <code>index</code> parameter.
     */

    public static void visualizeCRS(RenderedImage ri, String title,
            final int index) {
        visualizeWithTextArea(ri, title, TextType.PROJECT_AND_GEOTRANSF, index,
                true);
    }

    /**
     * Visualizes the <code>RenderedImage</code> (if <code>displayImage</code>
     * is <code>true</code>) and displays related Coordinate Reference System
     * Information
     * 
     * @param ri
     *                <code>RenderedImage</code> to visualize
     * @param title
     *                title for the frame. (Usually it is the name of the
     *                originating data source. When the image come from a
     *                multi-subdatasets data source, the title is the subdataset
     *                name)
     * @param displayImage
     *                if <code>false</code> only metadata will be displayed
     */

    public static void visualizeCRS(RenderedImage ri, String title,
            final boolean displayImage) {
        visualizeWithTextArea(ri, title, TextType.PROJECT_AND_GEOTRANSF, 0,
                displayImage);
    }

    /**
     * Visualizes the <code>RenderedImage</code> and displays related
     * Coordinate Reference System Information.
     * 
     * @param ri
     *                <code>RenderedImage</code> to visualize
     * @param title
     *                title for the frame. (Usually it is the name of the
     *                originating data source)
     * 
     */
    public static void visualizeCRS(RenderedImage ri, String title) {
        visualizeWithTextArea(ri, title, TextType.PROJECT_AND_GEOTRANSF, 0,
                true);
    }

    // //////////////////////////////////////////////////////////////////
    //
    // All-In-One Information management methods
    //
    // //////////////////////////////////////////////////////////////////

    /**
     * Visualizes the <code>RenderedImage</code> (if <code>displayImage</code>
     * is <code>true</code>) and displays every available information:
     * (Metadata as well as Coordinate Reference System information).
     * 
     * @param ri
     *                <code>RenderedImage</code> to visualize
     * @param title
     *                title for the frame. (Usually it is the name of the
     *                originating data source. When the image come from a
     *                multi-subdatasets data source, the title is the subdataset
     *                name)
     * @param index
     *                the index of the required image we need to visualize. This
     *                method will be used when we need to visualize a specific
     *                image contained within a multi-subdataset data source.
     *                Otherwise, when you need to visualize the only one image
     *                related to a single-image format data source you may use
     *                the method without <code>index</code> parameter.
     * @param displayImage
     *                if <code>false</code> only metadata will be displayed
     * 
     */

    public static void visualizeAllInformation(RenderedImage ri, String title,
            final int index, final boolean displayImage) {
        visualizeWithTextArea(ri, title, TextType.EVERYTHING, index,
                displayImage);
    }

    /**
     * Visualizes the <code>RenderedImage</code> and displays every available
     * information: (Metadata as well as Coordinate Reference System
     * information).
     * 
     * @param ri
     *                <code>RenderedImage</code> to visualize
     * @param title
     *                title for the frame. (Usually it is the name of the
     *                originating data source. When the image come from a
     *                multi-subdatasets data source, the title is the subdataset
     *                name)
     * @param index
     *                the index of the required image we need to visualize. This
     *                method will be used when we need to visualize a specific
     *                image contained within a multi-subdataset data source.
     *                Otherwise, when you need to visualize the only one image
     *                related to a single-image format data source you may use
     *                the method without <code>index</code> parameter.
     */

    public static void visualizeAllInformation(RenderedImage ri, String title,
            final int index) {
        visualizeWithTextArea(ri, title, TextType.EVERYTHING, index, true);
    }

    /**
     * Visualizes the <code>RenderedImage</code> (if <code>displayImage</code>
     * is <code>true</code>) and displays every available information:
     * (Metadata as well as Coordinate Reference System information).
     * 
     * @param ri
     *                <code>RenderedImage</code> to visualize
     * @param title
     *                title for the frame. (Usually it is the name of the
     *                originating data source)
     * @param displayImage
     *                if <code>false</code> only metadata will be displayed
     * 
     */

    public static void visualizeAllInformation(RenderedImage ri, String title,
            final boolean displayImage) {
        visualizeWithTextArea(ri, title, TextType.EVERYTHING, 0, displayImage);
    }

    /**
     * Visualizes the <code>RenderedImage</code> and displays every available
     * information:(Metadata as well as Coordinate Reference System
     * information).
     * 
     * @param ri
     *                <code>RenderedImage</code> to visualize
     * @param title
     *                title for the frame. (Usually it is the name of the
     *                originating data source)
     */

    public static void visualizeAllInformation(RenderedImage ri, String title) {
        visualizeWithTextArea(ri, title, TextType.EVERYTHING, 0, true);
    }

    // ///////////////////////////////////////////////////////////////////////
    //
    // Provides to retrieve metadata from the provided
    // <code>RenderedImage</code>}
    // and return the String containing properly formatted text.
    //	 
    // ///////////////////////////////////////////////////////////////////////

    private static String buildMetadataText(RenderedImage ri,
            final int metadataFields, final int index) {
        try {
            final String newLine = System.getProperty("line.separator");

            GDALImageReader reader = (GDALImageReader) ri
                    .getProperty(ImageReadDescriptor.PROPERTY_NAME_IMAGE_READER);

            StringBuffer sb = new StringBuffer("");

            switch (metadataFields) {
            case TextType.ONLY_IMAGE_METADATA:
            case TextType.EVERYTHING:
                sb.append(getImageMetadata(reader, index));
                break;
            case TextType.ONLY_STREAM_METADATA:
                sb.append(getStreamMetadata(reader));
                break;
            case TextType.STREAM_AND_IMAGE_METADATA:
                sb.append(getImageMetadata(reader, index)).append(newLine)
                        .append(getStreamMetadata(reader));
                break;
            }

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // ////////////////////////////////////////////////////////////////////////
    //
    // Provides to retrieve projections from the provided {@lik RenderedImage}
    // and return the String containing properly formatted text.
    //
    // ////////////////////////////////////////////////////////////////////////
    private static String buildCRSProperties(RenderedImage ri, final int index) {
        GDALImageReader reader = (GDALImageReader) ri
                .getProperty(ImageReadDescriptor.PROPERTY_NAME_IMAGE_READER);

        StringBuffer sb = new StringBuffer("CRS Information:").append(newLine);
        final String projection = reader.getProjection(index);
        if (!projection.equals(""))
            sb.append("Projections:").append(projection).append(newLine);

        // Retrieving GeoTransformation Information
        final double[] geoTransformations = reader.getGeoTransform(index);
        if (geoTransformations != null) {
            sb.append("Geo Transformation:").append(newLine);
            sb
                    .append("Origin = (")
                    .append(Double.toString(geoTransformations[0]))
                    .append(",")
                    .append(Double.toString(geoTransformations[3]))
                    .append(")")
                    .append(newLine)
                    .append("Pixel Size = (")
                    .append(Double.toString(geoTransformations[1]))
                    .append(",")
                    .append(Double.toString(geoTransformations[5]))
                    .append(")")
                    .append(newLine)
                    .append(newLine)
                    .append(
                            "---------- Affine GeoTransformation Coefficients ----------")
                    .append(newLine);
            for (int i = 0; i < 6; i++)
                sb.append("adfTransformCoeff[").append(i).append("]=").append(
                        Double.toString(geoTransformations[i])).append(newLine);
        }

        // Retrieving Ground Control Points Information
        final int gcpCount = reader.getGCPCount(index);
        if (gcpCount != 0) {
            sb.append(newLine).append("Ground Control Points:").append(newLine)
                    .append("Projections:").append(newLine).append(
                            reader.getGCPProjection(index)).append(newLine);

            final List gcps = reader.getGCPs(index);

            int size = gcps.size();
            for (int i = 0; i < size; i++)
                sb.append("GCP ").append(i + 1).append(gcps.get(i)).append(
                        newLine);
        }
        return sb.toString();
    }

    // ////////////////////////////////////////////////////////////////////////
    //
    // returns a String containing metadata from the provided reader
    //
    // ////////////////////////////////////////////////////////////////////////
    // TODO: change with the new ImageIO - Metadata Capabilities
    private static String getImageMetadata(GDALImageReader reader,
            final int index) {
        final GDALCommonIIOImageMetadata mt = reader.getDatasetMetadata(index);
        final List metadata = GDALUtilities.getGDALImageMetadata(mt
                .getDatasetName());
        if (metadata != null) {
            final int size = metadata.size();
            StringBuffer sb = new StringBuffer("Image Metadata:")
                    .append(newLine);
            for (int i = 0; i < size; i++)
                sb.append(metadata.get(i)).append(newLine);
            return sb.toString();
        }
        return "Image Metadata not found";
    }

    // ////////////////////////////////////////////////////////////////////////
    //
    // returns a String containing stream metadata from the provided reader
    //
    // ////////////////////////////////////////////////////////////////////////
    private static String getStreamMetadata(GDALImageReader reader)
            throws IOException {
        final GDALCommonIIOImageMetadata mt = reader.getDatasetMetadata(reader
                .getNumImages(true) - 1);
        final List metadata = GDALUtilities.getGDALStreamMetadata(mt
                .getDatasetName());
        if (metadata != null) {
            final int size = metadata.size();
            StringBuffer sb = new StringBuffer("Stream Metadata:")
                    .append(newLine);
            for (int i = 0; i < size; i++)
                sb.append(metadata.get(i)).append(newLine);
            return sb.toString();
        }
        return "Stream Metadata not found";
    }

    // ////////////////////////////////////////////////////////////////////////
    //
    // Provides to setup a {@link JFrame} containing both image and additional
    // information (containing metadata or projections or both of them).
    //
    // ////////////////////////////////////////////////////////////////////////

    // TODO: Fix it since frame components are not well composed/located
    private static void visualizeWithTextArea(RenderedImage ri, String title,
            final int textFields, final int index, final boolean displayImage) {

        StringBuffer sb = new StringBuffer();
        switch (textFields) {
        case TextType.ONLY_IMAGE_METADATA:
            sb.append("  Image Metadata from ").append(title);
            break;
        case TextType.ONLY_STREAM_METADATA:
            sb.append("  Stream Metadata");
            break;
        case TextType.STREAM_AND_IMAGE_METADATA:
            sb.append("  Metadata from ").append(title);
            break;
        case TextType.PROJECT_AND_GEOTRANSF:
            sb.append("  CRS Information for ").append(title);
            break;
        case TextType.EVERYTHING:
            sb.append(" Additional Information from ").append(title);
            break;
        }
        final JFrame frame = new JFrame(title);

        frame.getContentPane().setLayout(new BorderLayout());

        // Sometime, we dont want to display image, only text data which
        // need to be placed to the start of the area.
        String textPosition = BorderLayout.PAGE_START;

        if (displayImage) {
            frame.getContentPane().add(new ScrollingImagePanel(ri, 640, 480));

            textPosition = BorderLayout.LINE_START;
        }

        JLabel label = new JLabel(sb.toString());
        frame.getContentPane().add(label, textPosition);

        JTextArea textArea = new JTextArea();
        if (textFields == TextType.PROJECT_AND_GEOTRANSF)
            textArea.setText(buildCRSProperties(ri, index));
        else if (textFields != TextType.EVERYTHING)
            textArea.setText(buildMetadataText(ri, textFields, index));
        else
            textArea.setText(new StringBuffer(buildMetadataText(ri, textFields,
                    index).toString()).append(newLine).append(
                    buildCRSProperties(ri, index)).toString());
        textArea.setEditable(false);

        frame.getContentPane().add(textArea);
        frame.getContentPane().add(new JScrollPane(textArea),
                BorderLayout.PAGE_END);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                frame.pack();
                frame.setSize(1024, 768);
                frame.setVisible(true);
            }
        });
    }

    /**
     * @deprecated use {@link ImageIOUtilities#visualize(RenderedImage)}
     */
    public static void visualize(RenderedImage ri) {
        ImageIOUtilities.visualize(ri);
    }

    /**
     * @deprecated use {@link ImageIOUtilities#visualize(RenderedImage, String))}
     */
    public static void visualize(RenderedImage ri, final String title) {
        ImageIOUtilities.visualize(ri, title);
    }
}
