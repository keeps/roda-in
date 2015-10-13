package core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by adrapereira on 13-10-2015.
 */
public class HiddenFilesBag {
    private static Set<String> ignored = new HashSet<>();
    private static Set<String> mapped = new HashSet<>();

    private HiddenFilesBag(){
    }

    public static void ignore(String ign){
        ignored.add(ign);
    }
    public static void ignore(Collection igns){
        ignored.addAll(igns);
    }
    public static void map(String mp){
        mapped.add(mp);
    }
    public static void map(Collection mps){
        mapped.addAll(mps);
    }

    public static void removeIgnored(String ign){
        ignored.remove(ign);
    }
    public static void removeMapped(String mp){
        mapped.remove(mp);
    }

    public static boolean isIgnored(String ign){
        return ignored.contains(ign);
    }
    public static boolean isMapped(String mp){
        return mapped.contains(mp);
    }

}
