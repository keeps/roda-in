package rodain.rules.filters;

import java.util.HashSet;

/**
 * Created by adrapereira on 20-10-2015.
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
    public boolean filter(String st){
        if(ignored.contains(st))
            return true;
        int index = 0, end = st.length(), fromIndex = 0;
        while(index < end && (index = st.indexOf("/", fromIndex)) != -1){
            String sub = st.substring(0, index);
            fromIndex = index + 1;
            if(ignored.contains(sub))
                return true;
        }
        return false;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(String s: ignored)
            sb.append(s + "\n");
        return sb.toString();
    }

}
