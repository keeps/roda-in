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
    final String absolutePath = path.toAbsolutePath().toString();
    Configuration configuration = null;
    for (Configuration config : configList) {
      if (config.getSourceBasepath() != null && absolutePath.contains(config.getSourceBasepath())) {
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
    final String sourceBasepath = config.getSourceBasepath();
    final String targetBasepath = config.getTargetBasepath();
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
    if (targetBasepath != null && targetBasepath.length() > 0) {
      uri.setPath(Paths.get(targetBasepath).resolve(Paths.get(sourceBasepath).relativize(path)).toString());
    } else {
      uri.setPath(Paths.get(sourceBasepath).relativize(path).toString());
    }
    if (!Constants.MISC_FWD_SLASH.startsWith(uri.getPath())) {
      uri.setPath(Constants.MISC_FWD_SLASH + uri.getPath());
    }
    return uri.build();
  }

}
