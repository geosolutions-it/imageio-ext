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

import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Utility class to assist in parsing container names, user names, filenames, etc from either http or wasb URLs
 * pointing to Azure blob storage resources.
 *
 * @author joshfix
 * Created on 2/23/18
 */
public class AzureUrlParser {

    private static final Logger LOGGER = Logger.getLogger(AzureUrlParser.class.getName());

    public static String getAccountName(URL url) {
        return getAccountName(url.getHost());
    }

    public static String getAccountName(URI uri) {
        return getAccountName(uri.getHost());
    }

    public static String getAccountName(String host) {
        int firstPeriod = host.indexOf(".");
        if (firstPeriod == -1) {
            return host;
        }
        return host.substring(0, firstPeriod);
    }

    public static String getContainerName(URI uri) {
        return getContainerName(uri.getScheme(), uri.getPath(), uri.getUserInfo());
    }

    public static String getContainerName(URL url) {
        return getContainerName(url.getProtocol(), url.getPath(), url.getUserInfo());
    }

    public static String getContainerName(String protocol, String path, String userInfo) {
        switch (protocol.toLowerCase()) {
            case "wasb":
            case "wasbs":
                return userInfo;
            default:
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                try {
                    path.substring(0, path.indexOf("/"));
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error parsing container name from Azure URL.", e);
                    return null;
                }
                return path.substring(0, path.indexOf("/"));
        }
    }

    public static String getFilename(URL url, String containerName) {
        return getFilename(url.getProtocol(), url.getPath(), containerName);
    }

    public static String getFilename(URI uri, String containerName) {
        return getFilename(uri.getScheme(), uri.getPath(), containerName);
    }

    public static String getFilename(String protocol, String path, String containerName) {
        switch (protocol.toLowerCase()) {
            case "wasb":
            case "wasbs":
                if (path.startsWith("/")) {
                    return path.substring(1);
                }
                return path;
            default:
                return path.split(containerName + "/")[1];
        }
    }

}
