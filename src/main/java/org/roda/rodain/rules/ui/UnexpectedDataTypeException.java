package org.roda.rodain.rules.ui;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-10-2015.
 */
public class UnexpectedDataTypeException extends Exception {
    public UnexpectedDataTypeException(){
        super("Unexpected user data type.");
    }
}
