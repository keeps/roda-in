package rodain.rules.filters;

import java.util.Collection;

/**
 * Created by adrapereira on 20-10-2015.
 */
public interface ContentFilter {
    void add(String st);
    void addAll(Collection c);
    boolean filter(String st);
}
