/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2019, GeoSolutions
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
package it.geosolutions.imageioimpl.plugins.cog;

/**
 * This utility class will look for either a system property or environment variable.  When calling
 * `getPropertyValue`, if no system property is found, the property key with the format `my.property.key` will be
 * converted to `MY_PROPERTY_KEY` and an attempt will be made to fetch from the environment.  Similarly, when
 * `getEnvironmentValue` is called, if no env var is found, the key will be converted to system property style and an
 * attempt will be made to locate the value in system properties.
 *
 * @author joshfix
 * Created on 2019-09-19
 */
public class PropertyLocator {

    public static String getPropertyValue(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (null != value) {
            return value;
        }

        String environmentKey = convertPropertyToEnvVar(key);
        value = System.getenv(environmentKey);
        if (null != value) {
            return value;
        }

        return defaultValue;
    }

    public static String getEnvironmentValue(String key, String defaultValue) {
        String value = System.getenv(key);
        if (null != value) {
            return value;
        }

        String propertyKey = convertEnvVarToProperty(key);
        value = System.getProperty(propertyKey);
        if (null != value) {
            return value;
        }

        return defaultValue;
    }

    public static String convertEnvVarToProperty(String envVar) {
        return envVar
                .toLowerCase()
                .replace("_", ".");
    }

    public static String convertPropertyToEnvVar(String property) {
        return property
                .toUpperCase()
                .replace(".", "_")
                .replace("-", "_");
    }
}
