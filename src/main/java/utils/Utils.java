package utils;

import java.util.logging.Logger;

/**
 * Created by adrapereira on 24-09-2015.
 */
public class Utils {
    private static final Logger log = Logger.getLogger(Utils.class.getName());

    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }
}
