package source.representation;

import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Created by adrapereira on 17-09-2015.
 */
public class SourceFile implements SourceItem{
    private static final Logger log = Logger.getLogger(SourceFile.class.getName());
    public Path path;

    public SourceFile(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
