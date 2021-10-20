/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
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

import java.util.List;
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

    private static List<Listener> LISTENERS = new CopyOnWriteArrayList<>();

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
     * Adds a clean even listener
     */
    public static void addListener(Listener listener) {
        LISTENERS.add(listener);
    }

    /**
     * Removes a clean even listener
     *
     * @param listener The listener to remove
     * @return true if the listener was removed, false if it was not found among those registered
     */
    public static boolean removeListener(Listener listener) {
        return LISTENERS.remove(listener);
    }
}
