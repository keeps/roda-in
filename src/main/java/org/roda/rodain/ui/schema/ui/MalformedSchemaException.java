package org.roda.rodain.ui.schema.ui;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 03-12-2015.
 */
public class MalformedSchemaException extends Exception {
  private static final long serialVersionUID = -8659784639571997213L;

  /**
   * Creates a new MalformedSchemaException object.
   *
   * @param message
   *          The message to be used in the exception.
   */
  public MalformedSchemaException(String message) {
    super(message);
  }
}
