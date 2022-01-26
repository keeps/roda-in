package org.roda.rodain.core.shallowSipManager;

import java.util.List;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class ShallowSipConfigurationManager {
  private static final List<Configuration> shallow_sip_configurations = null;

  private ShallowSipConfigurationManager() {
    // do nothing
  }

  public static List<Configuration> getShallowSipConfigurations() {
    return shallow_sip_configurations;
  }

}
