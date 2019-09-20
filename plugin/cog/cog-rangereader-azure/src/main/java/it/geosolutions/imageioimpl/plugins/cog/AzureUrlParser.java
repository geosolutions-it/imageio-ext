package it.geosolutions.imageioimpl.plugins.cog;

import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
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
