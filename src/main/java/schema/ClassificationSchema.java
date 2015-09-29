package schema;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by adrapereira on 22-09-2015.
 */
public class ClassificationSchema {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ClassificationSchema.class.getName());
    private ArrayList<DescriptionObject> dos;

    public ClassificationSchema(){
        dos = new ArrayList<DescriptionObject>();
    }

    public ArrayList<DescriptionObject> getDos() {
        return dos;
    }

    public void setDos(ArrayList<DescriptionObject> dos) {
        this.dos = dos;
    }

    public ClassificationSchema(ArrayList<DescriptionObject> dos) {
        this.dos = dos;
    }

    public static ClassificationSchema instantiate(){
        //read json file data to String
        try {
            InputStream input = ClassLoader.getSystemResourceAsStream("test.json");

            //create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();

            //convert json string to object
            return objectMapper.readValue(input, ClassificationSchema.class);
        } catch (IOException e) {
            log.debug(e.getMessage());
        }
        return null;
    }

    @Override
    public String toString() {
        return "ClassificationSchema{" +
                "dos=" + dos +
                '}';
    }
}
