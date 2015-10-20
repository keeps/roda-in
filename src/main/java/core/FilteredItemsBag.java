package core;

import source.ui.items.SourceTreeItem;

import java.util.*;

/**
 * Created by adrapereira on 13-10-2015.
 */
public class FilteredItemsBag {
    private Map<String, SourceTreeItem> ignored = new HashMap<>();
    private Map<String, SourceTreeItem> ignoredParents = new HashMap<>();
    private Map<String, SourceTreeItem> mapped = new HashMap<>();
    private Map<String, SourceTreeItem> mappedParents = new HashMap<>();

    public FilteredItemsBag(){
    }

    public void ignore(String ign, SourceTreeItem sti, SourceTreeItem parent){
        ignored.put(ign, sti);
        ignoredParents.put(ign, parent);
    }
    public void map(String mp, SourceTreeItem sti, SourceTreeItem parent){
        mapped.put(mp, sti);
        mappedParents.put(mp, parent);
    }

    public SourceTreeItem getIgnoredParent(String ign){
        return ignoredParents.get(ign);
    }

    public SourceTreeItem getIgnored(String ign){
        return ignored.get(ign);
    }

    public SourceTreeItem removeIgnored(String ign){
        SourceTreeItem ret = ignored.get(ign);
        ignored.remove(ign);
        ignoredParents.remove(ign);
        return ret;
    }
    public SourceTreeItem removeMapped(String mp){
        SourceTreeItem ret = mapped.get(mp);
        mapped.remove(mp);
        mappedParents.remove(mp);
        return ret;
    }

    public boolean isIgnored(String ign){
        return ignored.containsKey(ign);
    }
    public boolean isMapped(String mp){
        return mapped.containsKey(mp);
    }

    public Collection<String> getIgnored(){
        return ignored.keySet();
    }

}
