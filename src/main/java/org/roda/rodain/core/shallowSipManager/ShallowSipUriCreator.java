package org.roda.rodain.core.shallowSipManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.apache.http.client.utils.URIBuilder;
import org.roda.rodain.core.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class ShallowSipUriCreator extends UriCreator {
  /**
   * {@link Logger}.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ShallowSipUriCreator.class.getName());

  /**
   * Empty constructor.
   */
  public ShallowSipUriCreator() {
    // do nothing
  }

  @Override
  public Optional<URI> convertPathToUri(final Path path) {
    final List<Configuration> configList = getShallowSipConfigurations();
    final Configuration config = getConfigurationFromPath(path, configList);
    Optional<URI> uri;
    if (config != null) {
      try {
        uri = Optional.of(createURI(path, config));
      } catch (final URISyntaxException e) {
        LOGGER.error("Error in URI syntax", e);
        uri = Optional.empty();
      }
    } else {
      uri = Optional.empty();
    }
    return uri;
  }

  /**
   * Checks if exist one config with the same base path of given {@link Path}.
   * 
   * @param path
   *          a {@link Path}
   * @param configList
   *          a {@link List} of {@link Configuration}
   * @return a {@link Configuration}
   */
  private Configuration getConfigurationFromPath(final Path path, final List<Configuration> configList) {
    final Path absolutePath = path.toAbsolutePath();
    Configuration configuration = null;
    for (Configuration config : configList) {
      if (config.getSourceBasepath() != null
        && absolutePath.startsWith(Paths.get(config.getSourceBasepath()).toAbsolutePath())) {
        configuration = config;
        break;
      }
    }
    return configuration;
  }

  /**
   * Creates an {@link URI} from the {@link Path} with the {@link Configuration}.
   * 
   * @param path
   *          {@link Path}
   * @param config
   *          {@link Configuration}
   * @return an {@link URI}
   * @throws URISyntaxException
   *           {@link URISyntaxException} if the created {@link URI} have a syntax
   *           error.
   */
  private URI createURI(final Path path, final Configuration config) throws URISyntaxException {
    final String sourceBasePath = config.getSourceBasepath();
    final String targetBasePath = config.getTargetBasepath();
    final String host = config.getHost();
    final String protocol = config.getProtocol();
    final String port = config.getPort();
    final URIBuilder uri = new URIBuilder();
    uri.setScheme(protocol);
    if (host != null && host.length() > 0) {
      uri.setHost(host);
    }
    if (port != null && port.length() > 0) {
      uri.setPort(Integer.parseInt(port));
    }

    uri.setPath(createUriPath(path, sourceBasePath, targetBasePath));
    return uri.build();
  }

  /**
   * Creates the URI path with the basePath and the target base path if exists.
   * 
   * @param path
   *          {@link Path}
   * @param sourceBasePath
   *          the source base path
   * @param targetBasePath
   *          the target base path
   * @return the URI path
   */
  private String createUriPath(final Path path, final String sourceBasePath, final String targetBasePath) {
    final StringBuilder pb = new StringBuilder();
    if (targetBasePath != null && targetBasePath.length() > 0) {
      Paths.get(targetBasePath).forEach(pi -> pb.append(Constants.MISC_FWD_SLASH).append(pi.toString()));
    }
    Paths.get(sourceBasePath).relativize(path).forEach(pi -> pb.append(Constants.MISC_FWD_SLASH).append(pi.toString()));
    return pb.toString();
  }

}
