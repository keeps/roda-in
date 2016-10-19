package org.roda.rodain.report;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.roda_project.commons_ip.model.SIP;
import org.roda_project.commons_ip.utils.METSZipEntryInfo;
import org.roda_project.commons_ip.utils.ZipEntryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.Manifest;

public class InventoryReportCreator {

  private static final String CSV_FIELD_FILE_SIZE = "size";
  private static final String CSV_FIELD_FILE_CHECKSUM = "checksum";
  private static final String CSV_FIELD_FILE_CHECKSUM_TYPE = "checksum_type";
  private static final String CSV_FIELD_FILE_ABSOLUTE_PATH = "absolute path";
  private static final String CSV_FIELD_FILE_RELATIVE_ZIP_PATH = "zip relative path";
  public static final String CSV_FIELD_SIP_ID = "SIP ID";
  public static final String CSV_FIELD_ZIP_RELATIVE_PATH = "ZIP relative path";


  private static final Logger LOGGER = LoggerFactory.getLogger(InventoryReportCreator.class.getName());
  private Path outputPath;

  public InventoryReportCreator(Path outputPath) {
    this.outputPath = outputPath;
  }

  public void start(Map<Path, Object> sips) {
    CSVPrinter csvFilePrinter = null;
    CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator());
    BufferedWriter fileWriter = null;

    try {
      StringBuffer name = new StringBuffer();
      name.append("inventory_report");
      name.append(" - ");
      name.append(new SimpleDateFormat("yyyy.MM.dd HH.mm.ss.SSS").format(new Date()));
      name.append(".csv");
      Path csvTempFile = outputPath.resolve(name.toString());

      fileWriter = Files.newBufferedWriter(csvTempFile);
      csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
      List<String> headers = new ArrayList<String>();
      headers.add(CSV_FIELD_SIP_ID);
      headers.add(CSV_FIELD_FILE_RELATIVE_ZIP_PATH);
      headers.add(CSV_FIELD_FILE_ABSOLUTE_PATH);
      headers.add(CSV_FIELD_FILE_CHECKSUM_TYPE);
      headers.add(CSV_FIELD_FILE_CHECKSUM);
      headers.add(CSV_FIELD_FILE_SIZE);
      csvFilePrinter.printRecord(headers);
      for (Map.Entry<Path, Object> entry : sips.entrySet()) {
        if (entry.getValue() instanceof Bag) {
          List<List<String>> lines = bagToCSVLines(entry.getKey());
          csvFilePrinter.printRecords(lines);
        } else if (entry.getValue() instanceof SIP) {
          SIP sip = (SIP) entry.getValue();
          List<List<String>> lines = SIPToCSVLines(entry.getKey(), sip);
          csvFilePrinter.printRecords(lines);
        }

      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      IOUtils.closeQuietly(fileWriter);
      IOUtils.closeQuietly(csvFilePrinter);
    }
  }

  private List<List<String>> SIPToCSVLines(Path path, SIP sip) {
    List<List<String>> lines = new ArrayList<List<String>>();
    for (ZipEntryInfo entry : sip.getZipEntries()) {
      if (!(entry instanceof METSZipEntryInfo)) {
        try {
          List<String> line = new ArrayList<String>();
          Long size = Files.size(entry.getFilePath());
          line.add(path.getFileName().toString());
          line.add(entry.getName());
          line.add(entry.getFilePath().toString());
          line.add(entry.getChecksumAlgorithm());
          line.add(entry.getChecksum());
          line.add(Long.toString(size));
          lines.add(line);
        } catch (IOException e) {
          LOGGER.debug("Error calculating file size", e);
        }
      }
    }
    return lines;
  }

  private List<List<String>> bagToCSVLines(Path path) {
    BagFactory fact = new BagFactory();

    Bag bag = fact.createBag(path.toFile()); 
    
    
    
    List<List<String>> lines = new ArrayList<List<String>>();
    for (Manifest manifest : bag.getTagManifests()) {
      for (Map.Entry<String, String> entry : manifest.entrySet()) {
        List<String> line = new ArrayList<String>();
        line.add(path.getFileName().toString());
        line.add(entry.getKey());
        line.add("");
        line.add("MD5");
        line.add(entry.getValue());
        line.add(Long.toString(bag.getBagFile(entry.getKey()).getSize()));
        lines.add(line);
      }
    }
    for (Manifest manifest : bag.getPayloadManifests()) {
      for (Map.Entry<String, String> entry : manifest.entrySet()) {
        List<String> line = new ArrayList<String>();
        line.add(path.getFileName().toString());
        line.add(entry.getKey());
        line.add("");
        line.add("MD5");
        line.add(entry.getValue());
        line.add(Long.toString(bag.getBagFile(entry.getKey()).getSize()));
        lines.add(line);
      }
    }
    return lines;
  }

}
