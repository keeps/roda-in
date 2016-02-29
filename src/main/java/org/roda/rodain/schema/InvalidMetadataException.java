package org.roda.rodain.schema;

/**
 * Created by adrapereira on 29-02-2016.
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
