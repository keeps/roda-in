package schema.ui.DescriptionLevel;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by adrap on 22-09-2015.
 */
public class DescriptionLevels {
    private static HashMap<String, DescriptionLevelConfig> configs;

    static DescriptionLevelConfig getConfig(String desclevel){
        if(configs == null) initialize();
        // we need to lowercase the string because that's the way they are written in the JSON file
        String lower = desclevel.toLowerCase();
        if(configs.containsKey(lower))
            return configs.get(lower);
        else return getDefaultConfig();
    }

    private static void initialize(){
        //read json file data to String
        try {
            InputStream input = ClassLoader.getSystemResourceAsStream("descriptionlevels.json");

            //create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();

            //convert json string to object
            DescriptionLevelList list = objectMapper.readValue(input, DescriptionLevelList.class);
            configs = new HashMap<String, DescriptionLevelConfig>();
            for(DescriptionLevelConfig dlc: list.getConfigs())
                configs.put(dlc.getTitle(), dlc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static DescriptionLevelConfig getDefaultConfig(){
        DescriptionLevelConfig dlc = new DescriptionLevelConfig();
        dlc.setTitle("Unknown");
        dlc.setAbbreviation("?");
        dlc.setBackgroundColor("red");
        dlc.setFontColor("black");
        dlc.setFontSize(16);
        dlc.setWidth(16);
        return dlc;
    }
}
