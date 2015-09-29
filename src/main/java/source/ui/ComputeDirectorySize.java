package source.ui;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by adrapereira on 24-09-2015.
 */
public class ComputeDirectorySize extends Thread {
    private static final Logger log = Logger.getLogger(ComputeDirectorySize.class.getName());
    private final int UPDATEFREQUENCY = 500; //in milliseconds
    private long filesCount = 0, size = 0;
    private FileExplorerPane ui;
    private long lastUIUpdate = 0;
    private String startPath;

    public ComputeDirectorySize(FileExplorerPane pane, String path){
        startPath = path;
        ui = pane;
    }

    @Override
    public void run() {
        Path path = Paths.get(startPath);

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    size += attrs.size();
                    filesCount++;
                    long now = System.currentTimeMillis();
                    if(now - lastUIUpdate > UPDATEFREQUENCY) {
                        ui.updateSize(filesCount, size);
                        lastUIUpdate = now;
                    }
                    //terminate if the thread has been interrupted
                    if(Thread.interrupted())
                        return FileVisitResult.TERMINATE;
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    if(Thread.interrupted())
                        return FileVisitResult.TERMINATE;
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
        //update the UI with the final values
        ui.updateSize(filesCount, size);
    }
}
