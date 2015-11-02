package org.roda.rodain.rules.filters;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 20-10-2015.
 */
public class FilterIgnored implements ContentFilter {
    private HashSet<String> ignored;

    public FilterIgnored(){
        ignored = new HashSet<>();
    }

    @Override
    public void add(String st){
        ignored.add(st);
    }

    @Override
    public void addAll(Collection col){
        ignored.addAll(col);
    }

    @Override
    public boolean filter(String st){
        if(ignored.contains(st))
            return true;
        int index = 0, end = st.length(), fromIndex = 0;
        boolean result = false;

        while(index < end && !result){ //while we still have string to read and haven't found a matching path
            index = st.indexOf('/', fromIndex); //get the path until the slash we're checking
            if(index == -1) {
                index = end + 1; //end the loop
            }else {
                String sub = st.substring(0, index);
                fromIndex = index + 1; // move the starting index for the next iteration so it's after the slash
                if (ignored.contains(sub))
                    result = true;
            }
        }
        return result;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(String s: ignored)
            sb.append(s + "\n");
        return sb.toString();
    }

}
