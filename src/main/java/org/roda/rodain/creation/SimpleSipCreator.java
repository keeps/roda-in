package org.roda.rodain.creation;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.sip.SipPreview;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class SimpleSipCreator extends Thread{
    protected final Path outputPath;
    protected final Map<SipPreview, String> previews;
    protected final int sipPreviewCount;
    protected final static String actionCreatingFolders = "Creating the SIP's directory structure";
    protected final static String actionCopyingData = "Copying the SIP's data";
    protected final static String actionCopyingMetadata = "Copying the SIP's metadata";
    protected final static String actionFinalizingSip = "Finalizing the SIP";

    protected int createdSipsCount = 0;
    protected String currentSipName;
    protected String currentAction;

    protected boolean canceled = false;

    protected Set<SipPreview> unsuccessful;

    public SimpleSipCreator(Path outputPath, Map<SipPreview, String> previews){
        this.outputPath = outputPath;
        this.previews = previews;
        sipPreviewCount = previews.size();

        unsuccessful = new HashSet<>();
    }

    /**
     * @return The number of SIPs that have already been created.
     */
    public int getCreatedSipsCount(){
        return createdSipsCount;
    }

    /**
     * @return The number of SIPs that haven't been created due to an error.
     */
    public int getErrorCount(){
        return unsuccessful.size();
    }

    /**
     * @return The action currently being done on the SIP.
     */
    public String getCurrentAction() {
        return currentAction;
    }

    /**
     * @return The name of the SIP currently being processed.
     */
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

    /**
     * Halts the execution of this SIP creator.
     */
    public void cancel(){
        canceled = true;
    }

    @Override
    public void run() {

    }
}
