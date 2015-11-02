package org.roda.rodain.rules.filters;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 20-10-2015.
 */
public class FilterMapped implements ContentFilter {
    private HashSet<String> mapped;

    public FilterMapped(){
        mapped = new HashSet<>();
    }

    @Override
    public void add(String st){
        mapped.add(st);
    }

    @Override
    public void addAll(Collection col){
        mapped.addAll(col);
    }

    @Override
    public boolean filter(String st){
        if(mapped.contains(st))
            return true;
        int index = 0, end = st.length(), fromIndex = 0;
        while(index < end && (index = st.indexOf("/", fromIndex)) != -1){
            String sub = st.substring(0, index);
            fromIndex = index + 1;
            if(mapped.contains(sub))
                return true;
        }
        return false;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(String s: mapped)
            sb.append(s + "\n");
        return sb.toString();
    }

}
