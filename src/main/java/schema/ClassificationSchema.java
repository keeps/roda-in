package schema;

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

    @Override
    public String toString() {
        return "ClassificationSchema{" +
                "dos=" + dos +
                '}';
    }
}
