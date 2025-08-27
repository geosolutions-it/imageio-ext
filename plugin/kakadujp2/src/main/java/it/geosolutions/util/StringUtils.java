package it.geosolutions.util;

/**
 * Replacement for com.sun.media.imageioimpl.common.ImageUtil#convertToString
 */
public class StringUtils {

    public static String convertObjectToString(Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj instanceof byte[]) {
            byte[] bArray = (byte[]) obj;
            StringBuilder sb = new StringBuilder();
            for (byte b : bArray) {
                sb.append(b).append(" ");
            }

            return sb.toString();
        } else if (obj instanceof int[]) {
            int[] iArray = (int[]) obj;
            StringBuilder sb = new StringBuilder();
            for (int i : iArray) {
                sb.append(i).append(" ");
            }
            return sb.toString();
        } else if (obj instanceof short[]) {
            short[] sArray = (short[]) obj;
            StringBuilder sb = new StringBuilder();
            for (short value : sArray) {
                sb.append(value).append(" ");
            }
            return sb.toString();
        }

        return obj.toString();
    }


}
