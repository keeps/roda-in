package org.roda.rodain.schema;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-02-2016.
 */
public class InvalidMetadataException extends Exception {
  private String message;

  public InvalidMetadataException(String mess) {
    message = mess;
  }

  public String getMessage() {
    return message;
  }
}
