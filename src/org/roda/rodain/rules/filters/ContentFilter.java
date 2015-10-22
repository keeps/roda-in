package rodain.rules.filters;

/**
 * Created by adrapereira on 20-10-2015.
 */
public interface ContentFilter {
    void add(String st);
    boolean filter(String st);
}
