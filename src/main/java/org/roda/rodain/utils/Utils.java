package org.roda.rodain.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 24-09-2015.
 */
public class Utils {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(Utils.class.getName());

  private Utils() {
  }

  public static String formatSize(long v) {
    if (v < 1024)
      return v + " B";
    int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
    return String.format("%.1f %sB", (double) v / (1L << (z * 10)), " KMGTPE".charAt(z));
  }

  public static int getRelativeMaxDepth(Path path) {
    final AtomicInteger depth = new AtomicInteger(0);
    try {
      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
          if (dir.getNameCount() > depth.get())
            depth.set(dir.getNameCount());
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (AccessDeniedException e) {
      log.info("Access denied to file", e);
    } catch (IOException e) {
      log.error("Error walking the file tree", e);
    }
    // return the relative depth to the start path
    return depth.get() - path.getNameCount();
  }

  public static String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  public static String convertStreamToString(InputStream is) {
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  public static Document loadXMLFromString(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();

    return builder.parse(new InputSource(new StringReader(xml)));
  }

  public static String replaceTag(String content, String tag, String replacement) {
    String escapedString = replacement.replaceAll("\\$", "\\\\\\$");
    return content.replaceAll(tag, escapedString);
  }
}
