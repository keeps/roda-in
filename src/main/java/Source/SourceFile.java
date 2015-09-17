package Source;

import java.nio.file.Path;

/**
 * Created by adrap on 17-09-2015.
 */
public class SourceFile implements SourceItem{
    private Path path;

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
