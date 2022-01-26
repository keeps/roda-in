package org.roda.rodain.core.creation;

import java.util.List;

import org.roda.rodain.core.schema.IPContentType;
import org.roda.rodain.core.schema.RepresentationContentType;

public interface ISipCreator {
  static String getText() {
    throw new IllegalStateException("Concrete class must implement this method!");
  }
  
  static public boolean requiresMETSHeaderInfo() {
    throw new IllegalStateException("Concrete class must implement this method!");
  }

  static List<IPContentType> ipSpecificContentTypes() {
    throw new IllegalStateException("Concrete class must implement this method!");
  }

  static List<RepresentationContentType> representationSpecificContentTypes() {
    throw new IllegalStateException("Concrete class must implement this method!");
  }
}
