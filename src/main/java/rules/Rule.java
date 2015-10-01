package rules;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;

import org.slf4j.LoggerFactory;

import schema.ui.SchemaNode;
import schema.ui.SipPreview;
import source.ui.items.SourceTreeDirectory;

/**
 * Created by adrapereira on 29-09-2015.
 */
public class Rule {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Rule.class.getName());
    private SourceTreeDirectory source;
    private SchemaNode schemaNode;
    private RuleTypes type;
    private HashSet<SipPreview> sips;
    private int sipCount = 0;
    private int level;

    public Rule(SourceTreeDirectory source, SchemaNode schemaNode){
        this.source = source;
        this.schemaNode = schemaNode;
    }

    public SourceTreeDirectory getSource() {
        return source;
    }

    public void setSource(SourceTreeDirectory source) {
        this.source = source;
    }

    public SchemaNode getSchemaNode() {
        return schemaNode;
    }

    public void setSchemaNode(SchemaNode schemaNode) {
        this.schemaNode = schemaNode;
    }

    public String getFolderName(){
        return source.getValue().toString();
    }

    public String getDescObjName(){
        return schemaNode.dob.getTitle();
    }

    public int getSipCount() {
        return sipCount;
    }

    public void apply(RuleTypes type, int level){
        this.type = type;
        this.level = level;
        sipCount = 0;
        sips = new HashSet<SipPreview>();

        switch (type){
            case SIPPERFILE:
                previewSipPerFile();
                break;
            case SIPPERFOLDER:
                previewSipPerFolder();
                break;
        }

        schemaNode.addRule(this);
    }

    private void previewSipPerFile(){
        final Path path = Paths.get(source.getPath());
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    SipPreview newSip = new SipPreview(file.getFileName().toString());
                    sipCount++;
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void previewSipPerFolder(){
        final Path path = Paths.get(source.getPath());
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    //every directory is a sub-directory of the start path, so if we remove it, we get the relative path to it
                    String relative = dir.toString().replace(path.toString(), "");
                    Path relativePath = Paths.get(relative);
                    int relativeLevel = relativePath.getNameCount();

                    if(relativeLevel <= level)
                        sipCount++;
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
