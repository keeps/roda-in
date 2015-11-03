package org.roda.rodain.core;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.sip.SipPreview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.PreBag;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 06-10-2015.
 */
public class CreateBagits extends Thread {
    private static final Logger log = LoggerFactory.getLogger(CreateBagits.class.getName());
    private static final String DATAFOLDER = "data";
    private int successful = 0, error = 0;
    private Path startPath;
    private long time;

    public CreateBagits(String path){
        startPath = Paths.get(path);
    }

    @Override
    public void run(){
        //Create output folder
        File ruleDir = new File(startPath.toString());
        ruleDir.mkdir();

        long start = System.currentTimeMillis();
        long lastUpdate = 0;
        Map<SipPreview, String> previews = Main.getSipPreviews();
        for(SipPreview preview: previews.keySet()){
            createBagit(previews.get(preview), preview);

            if(System.currentTimeMillis() - lastUpdate > 1000){
                Footer.setStatus("Processing... " + successful + " created");
                lastUpdate = System.currentTimeMillis();
            }
        }
        long end = System.currentTimeMillis();
        time = end - start;
        updateFooter();
    }

    private void updateFooter(){
        Footer.activeButton();
        long second = (time / 1000) % 60;
        Footer.setStatus("Created " + successful + " Bagits. Errors creating " + error + " Bagits. Time: " + second + " seconds");
    }

    private void createBagit(String schemaId, SipPreview sip){
        //make the directories
        Path name = startPath.resolve(sip.getName());
        Path data = name.resolve(DATAFOLDER);
        new File(data.toString()).mkdirs();

        try {
            Set<TreeNode> files = sip.getFiles();
            for(TreeNode tn: files)
                createFiles(tn, data);

            BagFactory bf = new BagFactory();
            PreBag pb = bf.createPreBag(new File(name.toString()));
            Bag b = pb.makeBagInPlace(BagFactory.Version.V0_97, false);

            //id and parent
            b.getBagInfoTxt().put("id", sip.getName());
            b.getBagInfoTxt().put("parent", schemaId);

            Map<String, String> metadata = createMetadata(sip);
            for(String key: metadata.keySet())
                b.getBagInfoTxt().put(key, metadata.get(key));

            b.makeComplete();
            b.close();

            FileSystemWriter fsw = new FileSystemWriter(bf);
            fsw.write(b, new File(name.toString()));
            successful ++;
        }
        catch (Exception e) {
            log.error("Error creating SIP", e);
            error++;
        }
    }

    private Map<String, String> createMetadata(SipPreview preview){
        Map<String, String> result = new HashMap<>();
        String rawMetadata = null;
        rawMetadata = preview.getMetadataContent();

        if(rawMetadata != null){
            String transformed = transformXML(rawMetadata);

            String[] lines = transformed.split(System.lineSeparator());
            for(String s: lines) {
                int colon = s.indexOf(":");
                String key = s.substring(0, colon);
                String value = s.substring(colon + 1);
                result.put(key, value);
            }
        }
        return result;
    }

    private String transformXML(String input){
        try {
            Source xmlSource = new StreamSource(new ByteArrayInputStream( input.getBytes() ));
            StreamSource xsltSource = new StreamSource(ClassLoader.getSystemResource("metadata.xsl").openStream());

            TransformerFactory transFact = TransformerFactory.newInstance();
            Transformer trans = transFact.newTransformer(xsltSource);

            Writer writer = new StringWriter();
            StreamResult streamResult = new StreamResult(writer);
            trans.transform(xmlSource, streamResult);

            return writer.toString();
        } catch (TransformerException e) {
            log.error("Error transforming XML", e);
        } catch (IOException e) {
            log.error("Error reading XSLT file", e);
        }
        return null;
    }

    private void createFiles(TreeNode node, Path dest) throws IOException{
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
