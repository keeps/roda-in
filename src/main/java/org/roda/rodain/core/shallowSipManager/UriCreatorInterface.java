package org.roda.rodain.core.shallowSipManager;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public interface UriCreatorInterface {

  /**
   * Convert the {@link Path} to {@link URI}.
   * 
   * @param path
   *          {@link Path}
   * @return an {@link URI}
   */
  Optional<URI> convertPathToUri(Path path);
}
