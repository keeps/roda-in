package org.roda.rodain.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ControllerTest {
  private static Path tempDir;

  public ControllerTest() {
    // do nothing
  }

  @BeforeClass
  public static void setup() throws IOException {
    tempDir = Files.createTempDirectory(Controller.class.getSimpleName());
  }

  @AfterClass
  public static void shutdown() {
    ControllerUtils.deleteQuietly(tempDir);
  }

  @Test
  public void testEncodeIdForFilesystemUsage() throws IOException {
    // the following strings are possible Ids set through the interface & that
    // must play nice with filesystem and jar entries
    Path path;
    String id;
    if (Controller.systemIsWindows()) {
      id = "abc\\def";
    } else {
      id = "abc/def";
    }

    // without encoding, parent must/will be abc
    path = tempDir.resolve(id);
    try {
      Files.createFile(path);
    } catch (NoSuchFileException e) {
      // expected
    }
    Assert.assertNotEquals(path.getParent().toString(), tempDir.toString());
    Assert.assertEquals(path.getParent().toString(), tempDir.resolve("abc").toString());

    // with encoding, parent must be tempDir
    path = tempDir.resolve(Controller.encodeId(id));
    Files.createFile(path);
    Assert.assertEquals(path.getParent().toString(), tempDir.toString());
  }

  @Test
  public void testEncodeIdForGenericUsage() throws UnsupportedEncodingException {
    // the following is here to avoid that one changes the method without
    // changing tests (the chars that are supposed to be encoded are duplicated
    // to test multiple chars scenario)
    String chars = "|1!2\"@3#£4$§56&7{8([9)]0=}'?«»qQwWeE€rRtTyYuUiIoOpP+*¨´`aAsSdDfFgGhHjJkKlLçÇºª~^<>zZxXcCvVbBnNmM,;.:-_ //\\\\%%";
    String expectedCharsAfterEncode = "|1!2\"@3#£4$§56&7{8([9)]0=}'?«»qQwWeE€rRtTyYuUiIoOpP+*¨´`aAsSdDfFgGhHjJkKlLçÇºª~^<>zZxXcCvVbBnNmM,;.:-_ %2F%2F%5C%5C%25%25";
    Assert.assertEquals(expectedCharsAfterEncode, Controller.encodeId(chars));
  }

}
