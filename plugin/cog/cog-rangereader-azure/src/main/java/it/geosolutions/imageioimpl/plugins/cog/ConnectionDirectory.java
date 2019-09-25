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

import java.util.HashMap;
import java.util.Map;

/**
 * @author joshfix
 * Created on 2019-04-03
 */
public class ConnectionDirectory {

    private static Map<String, String> directory = new HashMap<>();
    public static final String AZURE_CONNECTION_STRING_ENV_VAR_SUFFIX = "_AZURE_CONNECTION_STRING";

    public static String getConnectionString(String accountName) {
        if (!directory.containsKey(accountName)) {
            String envVarName = accountName.toUpperCase() + AZURE_CONNECTION_STRING_ENV_VAR_SUFFIX;
            String connectionString = PropertyLocator.getEnvironmentValue(envVarName, "");
            directory.put(accountName, connectionString);
            return connectionString;
        }
        return directory.get(accountName);
    }
}
