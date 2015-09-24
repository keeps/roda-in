package schema;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by adrap on 22-09-2015.
 */
public class ClassificationSchema {
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
            ClassificationSchema cs = objectMapper.readValue(input, ClassificationSchema.class);

            return cs;
        } catch (IOException e) {
            e.printStackTrace();
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
