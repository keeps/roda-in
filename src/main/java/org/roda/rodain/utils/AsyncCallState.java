package org.roda.rodain.utils;

/**
 * Created by adrapereira on 15-12-2015.
 */
public class AsyncCallState {

  public AsyncCallState() {
  }

  public void setFinished() {
    synchronized (this) {
      notifyAll();
    }
  }
}
