package schema.ui.descriptionlevel;

import java.util.ArrayList;

/**
 * Created by adrapereira on 22-09-2015.
 *
 * This class is only used to map the json to a list.
 * We then manually get the contents of this list to use in an HashMap in the DescriptionLevels class.
 */
public class DescriptionLevelList {
    private ArrayList<DescriptionLevelConfig> configs;

    public DescriptionLevelList() {
    }

    public DescriptionLevelList(ArrayList<DescriptionLevelConfig> configs) {
        this.configs = configs;
    }

    public ArrayList<DescriptionLevelConfig> getConfigs() {
        return configs;
    }

    public void setConfigs(ArrayList<DescriptionLevelConfig> configs) {
        this.configs = configs;
    }
}
