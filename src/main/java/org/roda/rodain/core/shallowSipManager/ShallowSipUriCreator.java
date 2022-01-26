package org.roda.rodain.core.shallowSipManager;

import java.net.URI;
import java.nio.file.Path;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public interface ShallowSipUriCreator {

  URI convertPathToUri(Path path);
}
