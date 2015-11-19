package org.roda.rodain.creation;

import org.roda.rodain.core.Main;
import org.roda.rodain.rules.sip.SipPreview;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class CreateSips {
    private SipTypes type;
    private Path outputPath;
    private SimpleSipCreator creator;

    private int sipsCount;

    public CreateSips(Path outputPath, SipTypes type){
        this.type = type;
        this.outputPath = outputPath;
    }

    public void start(){
        Map<SipPreview, String> sips = Main.getSipPreviews();
        sipsCount = sips.size();
        if(type == SipTypes.BAGIT){
            creator = new BagitSipCreator(outputPath, sips);
            creator.start();
        }
    }

    public int getSipsCount(){
        return sipsCount;
    }

    public int getCreatedSipsCount(){
        return creator.getCreatedSipsCount();
    }

    public double getProgress(){
        return creator.getCreatedSipsCount() / (sipsCount * 1.0);
    }

    public String getSipName(){
        return creator.currentSipName;
    }

    public String getAction(){
        return creator.currentAction;
    }

}
