package org.roda.rodain.creation;

import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.sip.SipPreview;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class SimpleSipCreator extends Thread{
    protected final Path outputPath;
    protected final Map<SipPreview, String> previews;
    protected final int sipPreviewCount;
    protected final String actionCreatingFolders = "Creating the SIP's directory structure";
    protected final String actionCopyingData = "Copying the SIP's data";
    protected final String actionCopyingMetadata = "Copying the SIP's metadata";
    protected final String actionFinalizingSip = "Finalizing the SIP";

    protected int createdSipsCount = 0;
    protected String currentSipName;
    protected String currentAction;

    protected Set<SipPreview> unsuccessful;

    public SimpleSipCreator(Path outputPath, Map<SipPreview, String> previews){
        this.outputPath = outputPath;
        this.previews = previews;
        sipPreviewCount = previews.size();

        unsuccessful = new HashSet<>();
    }

    public int getCreatedSipsCount(){
        return createdSipsCount;
    }

    public String getCurrentAction() {
        return currentAction;
    }

    public String getCurrentSipName() {
        return currentSipName;
    }

    protected void createFiles(TreeNode node, Path dest) throws IOException {
        Path nodePath = node.getPath();
        if(Files.isDirectory(nodePath)){
            Path directory = dest.resolve(nodePath.getFileName().toString());
            new File(directory.toString()).mkdir();
            for(TreeNode tn: node.getAllFiles().values()){
                createFiles(tn, directory);
            }
        }else{
            Path destination = dest.resolve(nodePath.getFileName().toString());
            Files.copy(nodePath, destination, COPY_ATTRIBUTES);
        }
    }


}
