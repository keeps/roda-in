package source.representation;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by adrap on 17-09-2015.
 */
public class SourceTest {

    public static void main(String[] args) {
        Path pRoot = Paths.get("/");
        SourceDirectory root = new SourceDirectory(pRoot);
    }
}
