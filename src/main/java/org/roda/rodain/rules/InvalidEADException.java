package org.roda.rodain.rules;

/**
 * Created by adrapereira on 18-02-2016.
 */
public class InvalidEADException extends Exception {
  private String message;

  public InvalidEADException(String mess) {
    message = mess;
  }

  public String getMessage() {
    return message;
  }
}
