package org.roda.rodain.core.creation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.SipNameStrategy;
import org.roda.rodain.core.Controller;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.report.InventoryReportCreator;
import org.roda.rodain.core.schema.DescriptionObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public abstract class SimpleSipCreator extends Thread {
  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSipCreator.class.getName());

  protected final static String actionCreatingFolders = I18n.t(Constants.I18N_SIMPLE_SIP_CREATOR_CREATING_STRUCTURE);
  protected final static String actionCopyingData = I18n.t(Constants.I18N_SIMPLE_SIP_CREATOR_COPYING_DATA);
  protected final static String actionCopyingMetadata = I18n.t(Constants.I18N_SIMPLE_SIP_CREATOR_COPYING_METADATA);
  protected final static String actionFinalizingSip = I18n.t(Constants.I18N_SIMPLE_SIP_CREATOR_FINALIZING_SIP);
  protected String agentName = Constants.SIP_DEFAULT_AGENT_NAME;

  protected final Path outputPath;
  protected final boolean createReport;
  protected final Map<DescriptionObject, List<String>> previews;
  protected final int sipPreviewCount;

  protected int createdSipsCount = 0;
  protected String currentSipName;
  protected String currentAction;

  // ETA
  protected long sipSize;
  protected long sipTransferedSize;
  protected long sipTransferedTime;
  protected long transferedSize;
  protected long transferedTime;
  protected long allSipsSize;
  protected Instant lastInstant;
  protected Instant sipStartInstant;

  protected boolean canceled = false;
  protected float currentSipProgress;

  protected Set<DescriptionObject> unsuccessful;

  /**
   * Creates a simple SIP exporter.
   * <p/>
   * <p>
   * This object doesn't export any SIPs, it must be extended and the run()
   * method overridden.
   * </p>
   *
   * @param outputPath
   *          The path to the output folder of the SIP exportation
   * @param previews
   *          The map with the SIPs that will be exported
   */
  public SimpleSipCreator(Path outputPath, Map<DescriptionObject, List<String>> previews, boolean createReport) {
    this.outputPath = outputPath;
    this.createReport = createReport;
    this.previews = previews;
    sipPreviewCount = previews.size();

    unsuccessful = new HashSet<>();

    try {
      agentName = String.format(Constants.SIP_AGENT_NAME_FORMAT, Controller.getCurrentVersion());
    } catch (ConfigurationException e) {
      LOGGER.debug("Could not get current version", e);
    }
  }

  /**
   * @return The number of SIPs that have already been created.
   */
  public int getCreatedSipsCount() {
    return createdSipsCount;
  }

  /**
   * @return The number of SIPs that haven't been created due to an error.
   */
  public int getErrorCount() {
    return unsuccessful.size();
  }

  /**
   * @return The action currently being done on the SIP.
   */
  public String getCurrentAction() {
    return currentAction;
  }

  /**
   * @return The name of the SIP currently being processed.
   */
  public String getCurrentSipName() {
    return currentSipName;
  }

  protected void deleteDirectory(Path dir) {
    try {
      FileUtils.deleteDirectory(dir.toFile());
    } catch (IOException e) {
      LOGGER.error("Error deleting directory", e);
    }
  }

  protected Map<String, String> getMetadata(String input) {
    Map<String, String> result = new HashMap<>();

    try {
      // apply the XSLT to the metadata content
      String transformed = transformXML(input);

      // parse the resulting document
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(new InputSource(new StringReader(transformed)));

      // add all the field nodes to the result map. Example: <field
      // name=".RDF.Description.date_txt">27-05-2016</field>
      NodeList fields = doc.getElementsByTagName("field");
      for (int i = 0; i < fields.getLength(); i++) {
        Node field = fields.item(i);
        NamedNodeMap attributes = field.getAttributes();
        String fieldName = attributes.getNamedItem("name").getNodeValue();
        String fieldValue = field.getTextContent().replaceAll("\\r\\n|\\r|\\n", " ");
        result.put(fieldName, fieldValue);
      }
    } catch (Exception e) {
      LOGGER.info("Error parsing the XML file, falling back to simple metadata mode", e);
      // if there's been an error when transforming the XML, remove all
      // new-lines from the metadata text and add it to the result as a single
      // line
      String noBreaks = input.replaceAll("\\r\\n|\\r|\\n", " ");
      result.put("metadata", noBreaks);
    }

    return result;
  }

  private String transformXML(String input) throws TransformerException, IOException {
    Source xmlSource = new StreamSource(new ByteArrayInputStream(input.getBytes()));
    StreamSource xsltSource = new StreamSource(ClassLoader.getSystemResource("plain.xslt").openStream());

    TransformerFactory transFact = TransformerFactory.newInstance();
    Transformer trans = transFact.newTransformer(xsltSource);

    Writer writer = new StringWriter();
    StreamResult streamResult = new StreamResult(writer);
    trans.transform(xmlSource, streamResult);
    return writer.toString();
  }

  /**
   * Halts the execution of this SIP creator.
   */
  public void cancel() {
    canceled = true;
    interrupt();
  }

  /**
   * This method must be overridden.
   */
  @Override
  public void run() {

  }

  /**
   * @return The time remaining estimate of the SIP creator.
   */
  public double getTimeRemainingEstimate() {
    return -1;
  }

  public void createReport(Map<Path, Object> sips) {
    InventoryReportCreator reportCreator = new InventoryReportCreator(outputPath);
    reportCreator.start(sips);
  }

  public String createSipName(DescriptionObject sip, String prefix, SipNameStrategy sipNameStrategy) {
    StringBuilder name = new StringBuilder();
    if (prefix != null && !"".equals(prefix)) {
      name.append(prefix).append(" - ");
    }
    switch (sipNameStrategy) {
      case TITLE_ID:
        name.append(sip.getTitle());
        name.append(" - ");
        name.append(sip.getId());
        break;
      case TITLE_DATE:
        name.append(sip.getTitle());
        name.append(" - ");
        name.append(new SimpleDateFormat(Constants.DATE_FORMAT_1).format(new Date()));
        break;
      case ID:
      default:
        name.append(sip.getId());
        break;
    }

    return name.toString();
  }
}
