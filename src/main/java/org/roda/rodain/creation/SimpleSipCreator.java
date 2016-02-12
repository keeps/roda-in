package org.roda.rodain.creation;

import java.io.*;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
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

import org.apache.commons.io.FileUtils;
import org.roda.rodain.core.AppProperties;
import org.roda.rodain.rules.sip.SipPreview;
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
public class SimpleSipCreator extends Thread {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(SimpleSipCreator.class.getName());
  protected final Path outputPath;
  protected final Map<SipPreview, String> previews;
  protected final int sipPreviewCount;
  protected final static String actionCreatingFolders = AppProperties.getLocalizedString("SimpleSipCreator.creatingStructure");
  protected final static String actionCopyingData = AppProperties.getLocalizedString("SimpleSipCreator.copyingData");
  protected final static String actionCopyingMetadata = AppProperties.getLocalizedString("SimpleSipCreator.copyingMetadata");
  protected final static String actionFinalizingSip = AppProperties.getLocalizedString("SimpleSipCreator.finalizingSip");

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

  protected Set<SipPreview> unsuccessful;

  /**
   * Creates a simple SIP exporter.
   * <p/>
   * <p>
   * This object doesn't export any SIPs, it must be extended and the run() method overridden.
   * </p>
   *
   * @param outputPath The path to the output folder of the SIP exportation
   * @param previews   The map with the SIPs that will be exported
   */
  public SimpleSipCreator(Path outputPath, Map<SipPreview, String> previews) {
    this.outputPath = outputPath;
    this.previews = previews;
    sipPreviewCount = previews.size();

    unsuccessful = new HashSet<>();
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
      log.error("Error deleting directory", e);
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
      log.info("Error parsing the XML file, falling back to simple metadata mode", e);
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
  }

  /**
   * This method must be overridden.
   */
  @Override
  public void run() {

  }

  public double getTimeRemainingEstimate() {
    return -1;
  }
}
