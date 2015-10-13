package utils;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.concurrent.atomic.AtomicInteger;

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
}
