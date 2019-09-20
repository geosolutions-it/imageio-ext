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
            String connectionString = PropertyLocator.getPropertyValue(envVarName, "");
            directory.put(accountName, connectionString);
            return connectionString;
        }
        return directory.get(accountName);
    }
}
