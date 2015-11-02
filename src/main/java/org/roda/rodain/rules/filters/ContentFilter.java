package org.roda.rodain.rules.filters;

import java.util.Collection;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 20-10-2015.
 */
public interface ContentFilter {
    void add(String st);
    void addAll(Collection c);
    boolean filter(String st);
}
