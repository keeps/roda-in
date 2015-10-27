package rodain.core;

import java.io.File;
import java.util.*;

import org.slf4j.LoggerFactory;

import rodain.rules.*;
import rodain.rules.sip.SipPreview;
import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.PreBag;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;

/**
 * Created by adrapereira on 06-10-2015.
 */
public class CreateBagits extends Thread implements Observer {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CreateBagits.class.getName());
    private VisitorStack visitors;
    private HashMap<String, Rule> unfinished;
    private int successful = 0, error = 0;
    private String startPath;

    public CreateBagits(String path){
        startPath = path;
        visitors = new VisitorStack();
        unfinished = new HashMap<>();
        visitors.addObserver(this);
    }

    @Override
    public void run(){
        /*for(Rule rule : Main.getRules()){
            log.info(rule.getId());
            TreeVisitor visitor = rule.apply();
            //visitors.add(rule.getSourceString(), visitor);
            unfinished.put(rule.getId(), rule);
        }*/
        updateFooter();
    }

    @Override
    public void update(Observable o, Object arg) {
        for(String id: unfinished.keySet()){
            if(visitors.isDone(id) == VisitorState.VISITOR_DONE){
                createBagits(unfinished.get(id));
                unfinished.remove(id);
            }
        }
        updateFooter();
    }

    private void updateFooter(){
        if(unfinished.size() == 0){
            Footer.activeButton();
            Footer.setStatus("Created " + successful + " Bagits. Errors creating " + error + " Bagits.");
        }
    }

    private void createBagits(Rule rule){
        List<SipPreview> sips = rule.getSips();

        String path = startPath + "/" + rule.getId() + "/";
        File ruleDir = new File(path);
        ruleDir.mkdir();
        int numSips = 0;
        for(SipPreview sip: sips){
            String name = path + "sip_" + rule.getId() + "_" + numSips;
            //make the directory
            new File(name).mkdir();
            new File(name+"/data").mkdir();

            try {
                Set<TreeNode> files = sip.getFiles();
                BagFactory bf = new BagFactory();

                Bag b = bf.createBag();
                //b.addFileToPayload(new File(node.getPath()));
                b.makeComplete();
                b.close();

                FileSystemWriter fsw = new FileSystemWriter(bf);
                fsw.write(b, new File(name));

                PreBag pb = bf.createPreBag(new File(name));
                pb.makeBagInPlace(BagFactory.Version.V0_97, false);

                numSips++;
            }
            catch (Exception e) {
                log.error("" + e);
                error++;
            }
        }
        successful += numSips;
    }
}
