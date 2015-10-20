package rules;

import java.util.List;

/**
 * Created by adrapereira on 20-10-2015.
 */
public interface SipCreator {
    List<SipPreview> getSips();
    int getCount();
    SipPreview getNext();
    boolean hasNext();
}
