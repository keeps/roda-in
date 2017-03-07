package org.roda.rodain.ui.rules.ui;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-10-2015.
 */
public class UnexpectedDataTypeException extends Exception {
  private static final long serialVersionUID = -7778386522191536562L;

  /**
   * An exception in the expected data types.
   */
  public UnexpectedDataTypeException() {
    super("Unexpected user data type.");
  }
}
