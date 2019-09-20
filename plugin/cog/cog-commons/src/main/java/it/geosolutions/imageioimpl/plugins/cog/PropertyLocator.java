package it.geosolutions.imageioimpl.plugins.cog;

/**
 * @author joshfix
 * Created on 2019-09-19
 */
public class PropertyLocator {

    public static String getPropertyValue(String key, String defaultValue) {
        String environmentKey = convertPropertytoEnvVar(key);
        String value = System.getenv(environmentKey);
        if (null != value) {
            return value;
        }
        value = System.getProperty(key);
        if (null != value) {
            return value;
        }

        return defaultValue;
    }

    public static String convertPropertytoEnvVar(String property) {
        return property.toUpperCase().replace(".", "_");
    }
}
