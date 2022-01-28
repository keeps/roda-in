package org.roda.rodain.core.shallowSipManager;

import java.util.ArrayList;
import java.util.List;

import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class ShallowSipConfigurationManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ShallowSipConfigurationManager.class.getName());
  private static List<Configuration> shallow_sip_configurations = null;

  private ShallowSipConfigurationManager() {
    // do nothing
  }

  public static List<Configuration> getShallowSipConfigurations() {
    final String basePathsRaw = ConfigurationManager.getConfig(Constants.CONF_K_SHALLOW_SIP_BASEPATH);
    final String hostsRaw = ConfigurationManager.getConfig(Constants.CONF_K_SHALLOW_SIP_HOST);
    final String protocolsRaw = ConfigurationManager.getConfig(Constants.CONF_K_SHALLOW_SIP_PROTOCOL);

    if (basePathsRaw != null && hostsRaw != null && protocolsRaw != null) {
      final String[] basePaths = basePathsRaw.split(Constants.MISC_COMMA);
      final String[] hosts = hostsRaw.split(Constants.MISC_COMMA);
      final String[] protocols = protocolsRaw.split(Constants.MISC_COMMA);
      shallow_sip_configurations = new ArrayList<>();
      if (basePaths.length == hosts.length && basePaths.length == protocols.length) {
        int i = 0;
        for (String basePath : basePaths) {
          shallow_sip_configurations.add(new Configuration(basePath, hosts[i], protocols[i]));
          i++;
        }
      } else {
        LOGGER.debug("The number of base paths, hosts and protocols doesn't match");
      }
    }
    return shallow_sip_configurations;
  }

}
