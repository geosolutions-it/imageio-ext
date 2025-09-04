/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
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

import it.geosolutions.imageio.gdalframework.GDALUtilities.MetadataChoice;
import org.eclipse.imagen.media.viewer.RenderedImageBrowser;

import java.awt.BorderLayout;
import java.awt.image.RenderedImage;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

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
@SuppressWarnings("deprecation")
public final class Viewer {
    /** private constructor to prevent instantiation */
    private Viewer() {
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
        visualizeWithTextArea(ri, title, MetadataChoice.ONLY_IMAGE_METADATA, index,displayImage);
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
        visualizeWithTextArea(ri, title, MetadataChoice.ONLY_IMAGE_METADATA, index,true);
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
        visualizeWithTextArea(ri, title, MetadataChoice.ONLY_IMAGE_METADATA, 0,
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
        visualizeWithTextArea(ri, title, MetadataChoice.ONLY_IMAGE_METADATA, 0, true);
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
        visualizeWithTextArea(ri, title, MetadataChoice.ONLY_STREAM_METADATA, 0,
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
        visualizeWithTextArea(ri, title, MetadataChoice.ONLY_STREAM_METADATA, 0, true);
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
        visualizeWithTextArea(ri, title, MetadataChoice.STREAM_AND_IMAGE_METADATA,
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
        visualizeWithTextArea(ri, title, MetadataChoice.STREAM_AND_IMAGE_METADATA,
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
        visualizeWithTextArea(ri, title, MetadataChoice.STREAM_AND_IMAGE_METADATA, 0,displayImage);
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
        visualizeWithTextArea(ri, title, MetadataChoice.STREAM_AND_IMAGE_METADATA, 0,
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
        visualizeWithTextArea(ri, title, MetadataChoice.PROJECT_AND_GEOTRANSF, index,
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
        visualizeWithTextArea(ri, title, MetadataChoice.PROJECT_AND_GEOTRANSF, index,
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
        visualizeWithTextArea(ri, title, MetadataChoice.PROJECT_AND_GEOTRANSF, 0,
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
        visualizeWithTextArea(ri, title, MetadataChoice.PROJECT_AND_GEOTRANSF, 0,
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
        visualizeWithTextArea(ri, title, MetadataChoice.EVERYTHING, index,
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
        visualizeWithTextArea(ri, title, MetadataChoice.EVERYTHING, index, true);
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
        visualizeWithTextArea(ri, title, MetadataChoice.EVERYTHING, 0, displayImage);
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
        visualizeWithTextArea(ri, title, MetadataChoice.EVERYTHING, 0, true);
    }

    // ///////////////////////////////////////////////////////////////////////
    //
    // Provides to retrieve metadata from the provided
    // <code>RenderedImage</code>}
    // and return the String containing properly formatted text.
    //	 
    // ///////////////////////////////////////////////////////////////////////

    // TODO: Fix it since frame components are not well composed/located
    private static void visualizeWithTextArea(RenderedImage ri, String title,
            final MetadataChoice textFields, final int index, final boolean displayImage) {
        if (displayImage) {
            RenderedImageBrowser.showChainAndWaitOnClose(ri);
        }
    }

}
