package org.roda.rodain.rules.ui;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-10-2015.
 */
public class UnexpectedDataTypeException extends Exception {

  /**
   * An exception in the expected data types.
   */
  public UnexpectedDataTypeException() {
    super("Unexpected user data type.");
  }
}
