package rodain.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.LoggerFactory;

/**
 * Created by adrapereira on 24-09-2015.
 */
public class Utils {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Utils.class.getName());
    private Utils(){
    }

    public static String formatSize(long v) {
        if (v < 1024)
            return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }

    public static int getRelativeMaxDepth(Path path){
        final AtomicInteger depth = new AtomicInteger(0);
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    if(dir.getNameCount() > depth.get())
                        depth.set(dir.getNameCount());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("" + e);
        }
        //return the relative depth to the start path
        return depth.get() - path.getNameCount();
    }

    public static String longestCommonPrefix(List<String> strs) {
        if(strs == null || strs.isEmpty())
            return "";

        int minLen=Integer.MAX_VALUE;
        for(String str: strs){
            if(minLen > str.length())
                minLen = str.length();
        }
        if(minLen == 0)
            return "";

        for(int j=0; j<minLen; j++){
            char prev='0';
            for(int i=0; i<strs.size() ;i++){
                if(i==0) {
                    prev = strs.get(i).charAt(j);
                    continue;
                }

                if(strs.get(i).charAt(j) != prev){
                    return strs.get(i).substring(0, j);
                }
            }
        }
        return strs.get(0).substring(0, minLen);
    }

    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
