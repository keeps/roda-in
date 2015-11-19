package org.roda.rodain.creation;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.PreBag;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.sip.SipPreview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class BagitSipCreator extends SimpleSipCreator {
    private static final Logger log = LoggerFactory.getLogger(BagitSipCreator.class.getName());
    private static final String DATAFOLDER = "data";

    public BagitSipCreator(Path outputPath, Map<SipPreview, String> previews){
        super(outputPath, previews);
    }

    public void run(){
        for(SipPreview preview: previews.keySet()) {
            if(canceled) {
                break;
            }
            createBagit(previews.get(preview), preview);
        }
    }

    private void createBagit(String schemaId, SipPreview sip){
        //we add a timestamp to the beginning of the SIP name to avoid same name conflicts
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk'h'mm'm'ss's'SSS");
        String dateToString = format.format(new Date());
        String timestampedName = String.format("%s %s", dateToString, sip.getName());
        currentSipName = timestampedName;
        currentAction = actionCreatingFolders;
        //make the directories
        Path name = outputPath.resolve(timestampedName);
        Path data = name.resolve(DATAFOLDER);
        new File(data.toString()).mkdirs();

        try {
            Set<TreeNode> files = sip.getFiles();
            currentAction = actionCopyingData;
            for(TreeNode tn: files)
                createFiles(tn, data);

            BagFactory bf = new BagFactory();
            PreBag pb = bf.createPreBag(new File(name.toString()));
            Bag b = pb.makeBagInPlace(BagFactory.Version.V0_97, false);

            //id and parent
            b.getBagInfoTxt().put("id", sip.getName());
            b.getBagInfoTxt().put("parent", schemaId);

            currentAction = actionCopyingMetadata;
            Map<String, String> metadata = createMetadata(sip);
            for(String key: metadata.keySet())
                b.getBagInfoTxt().put(key, metadata.get(key));

            b.makeComplete();
            b.close();

            currentAction = actionFinalizingSip;
            FileSystemWriter fsw = new FileSystemWriter(bf);
            fsw.write(b, new File(name.toString()));
            createdSipsCount ++;
        }
        catch (Exception e) {
            log.error("Error creating SIP", e);
            unsuccessful.add(sip);
        }
    }

    private Map<String, String> createMetadata(SipPreview preview){
        Map<String, String> result = new HashMap<>();
        String rawMetadata;
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
}
