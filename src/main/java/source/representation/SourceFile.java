package source.representation;

import java.nio.file.Path;

/**
 * Created by adrapereira on 17-09-2015.
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
