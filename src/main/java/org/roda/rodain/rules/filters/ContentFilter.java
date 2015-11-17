package org.roda.rodain.rules.filters;

import org.roda.rodain.core.PathCollection;
import org.roda.rodain.source.ui.items.SourceTreeItemState;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 20-10-2015.
 */
public class ContentFilter {
    private HashSet<String> ignored;
    private HashSet<String> mapped;

    public ContentFilter(){
        ignored = new HashSet<>();
        mapped = new HashSet<>();
    }

    public void addIgnored(String st){
        ignored.add(st);
    }

    public void addAllIgnored(Collection col){
        ignored.addAll(col);
    }

    public void addMapped(String st){
        mapped.add(st);
    }

    public void addAllMapped(Collection col){
        mapped.addAll(col);
    }

    public boolean filter(String st){
        boolean result = false;
        if(ignored.contains(st) || mapped.contains(st) || PathCollection.getState(st) != SourceTreeItemState.NORMAL) {
            result = true;
        } else {
            int index = 0, end = st.length(), fromIndex = 0;
            String separator = File.separator;

            while (index < end && !result) { //while we still have string to read and haven't found a matching path
                index = st.indexOf(separator, fromIndex); //get the path until the slash we're checking
                if (index == -1) {
                    break;
                } else {
                    String sub = st.substring(0, index);
                    fromIndex = index + 1; // move the starting index for the next iteration so it's after the slash
                    if (ignored.contains(sub) || mapped.contains(sub)) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }

}
