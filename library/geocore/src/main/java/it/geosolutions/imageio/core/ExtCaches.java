/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    https://github.com/geosolutions-it/imageio-ext
 *    (C) 2007 - 2021, GeoSolutions
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
package it.geosolutions.imageio.core;

import it.geosolutions.imageio.utilities.SoftValueHashMap;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central control for ImageI/O-Ext internal caches. Allows external code to reset
 * all caches, and bits of code to register and listen for such clean events.
 */
public class ExtCaches {

    private static final Logger LOGGER = Logger.getLogger(ExtCaches.class.getName());

    /**
     * Listener that will be called back when the {@link ExtCaches#clean()} method is called
     */
    public interface Listener {
        void clean();
    }

    /**
     * Listener that will be called back when the {@link ExtCaches#cleanForResource(String resourceIdentifier)} method
     * is called with a matching resource identifier.
     */
    public interface ResourceListener {
        void clean(String resourceIdentifier);
    }

    private static List<Listener> LISTENERS = new CopyOnWriteArrayList<>();
    private static SoftValueHashMap<String, ResourceListener> RESOURCE_LISTENERS = new SoftValueHashMap<>();

    /**
     * Cleans ImageI/O-Ext caches
     */
    public static void clean() {
        for (Listener listener : LISTENERS) {
            try {
                listener.clean();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Clean up invocation failed on listener: " + listener, e);
            }
        }
    }

    /**
     * Disposes of all resources connected with a given resource URI and removes the
     * corresponding {@link ResourceListener} afterward.
     */
    public static void cleanForResource(String resourceIdentifier) {
        ResourceListener listener = RESOURCE_LISTENERS.remove(resourceIdentifier);
        if (listener != null) {
            try {
                listener.clean(resourceIdentifier);
            } catch (Exception e) {
                LOGGER.log(
                    Level.WARNING,
                    "Clean up invocation failed listener for resource '" + resourceIdentifier + "': " + listener,
                    e
                );
            }
        }
    }

    /**
     * Adds a clean event listener
     */
    public static void addListener(Listener listener) {
        LOGGER.log(Level.FINE, "Adding or updating listener " + listener);
        LISTENERS.add(listener);
    }

    /**
     * Adds or updates the resource listener for a given resource id. The listener will be invoked when
     * {@link ExtCaches#cleanForResource(String resourceIdentifier)} is called with a matching resourceId.
     */
    public static void addOrUpdateResourceListener(String resourceId, ResourceListener listener) {
        LOGGER.log(Level.FINE, "Adding or updating resource listener for " + resourceId);
        RESOURCE_LISTENERS.put(resourceId, listener);
    }

    /**
     * Removes a clean event listener
     *
     * @param listener The listener to remove
     * @return true if the listener was removed, false if it was not found among those registered
     */
    public static boolean removeListener(Listener listener) {
        return LISTENERS.remove(listener);
    }
}
