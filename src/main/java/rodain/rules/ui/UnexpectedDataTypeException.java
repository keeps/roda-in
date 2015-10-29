package rodain.rules.ui;

/**
 * Created by adrapereira on 29-10-2015.
 */
public class UnexpectedDataTypeException extends Exception {
    public UnexpectedDataTypeException(){
        super("Unexpected user data type.");
    }
}
