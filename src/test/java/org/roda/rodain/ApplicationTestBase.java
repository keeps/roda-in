package org.roda.rodain;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.hamcrest.Matcher;
import org.loadui.testfx.GuiTest;
import org.loadui.testfx.exceptions.NoNodesFoundException;
import org.loadui.testfx.exceptions.NoNodesVisibleException;
import org.testfx.framework.junit.ApplicationTest;

import javafx.scene.Node;
import javafx.scene.control.ComboBoxBase;

/**
 * Test base based on the one from the project
 * https://github.com/HubTurbo/HubTurbo/
 * 
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class ApplicationTestBase extends ApplicationTest {
  public void clickOk(){
    waitUntilNodeAppearsAndClick("OK");
  }

  public void waitUntilNodeAppears(Node node) {
    GuiTest.waitUntil(node, n -> n != null && n.isVisible() && n.getParent() != null);
  }

  public void waitUntilNodeDisappears(Node node) {
    GuiTest.waitUntil(node, n -> n != null && (!n.isVisible() || n.getParent() == null));
  }

  public void waitUntilNodeAppears(String selector) {
    awaitCondition(() -> existsQuiet(selector));
  }

  public void waitUntilNodeAppearsAndClick(String selector) {
    waitUntilNodeAppears(selector);
    clickOn(selector);
  }

  public void waitUntilNodeAppearsAndClick(Node node) {
    waitUntilNodeAppears(node);
    clickOn(node);
  }

  public void waitUntilNodeAppearsAndDoubleClick(String selector) {
    waitUntilNodeAppears(selector);
    doubleClickOn(selector);
  }

  public void waitUntilNodeAppearsAndDoubleClick(Node node) {
    waitUntilNodeAppears(node);
    doubleClickOn(node);
  }

  public void waitUntilNodeDisappears(String selector) {
    awaitCondition(() -> !existsQuiet(selector));
  }

  public void waitUntilNodeAppears(Matcher<Object> matcher) {
    // We use find because there's no `exists` for matchers
    awaitCondition(() -> findQuiet(matcher).isPresent());
  }

  public void waitUntilNodeDisappears(Matcher<Object> matcher) {
    // We use find because there's no `exists` for matchers
    awaitCondition(() -> !findQuiet(matcher).isPresent());
  }

  public <T extends Node> void waitUntil(String selector, Predicate<T> condition) {
    awaitCondition(() -> condition.test(GuiTest.find(selector)));
  }

  /**
   * Waits for the result of a function, then asserts that it is equal to some
   * value.
   */
  public <T> void waitAndAssertEquals(T expected, Supplier<T> actual) {
    awaitCondition(() -> expected.equals(actual.get()));
  }

  public <T> void waitForValue(ComboBoxBase<T> comboBoxBase) {
    GuiTest.waitUntil(comboBoxBase, c -> c.getValue() != null);
  }

  public <T extends Node> T findOrWaitFor(String selector) {
    waitUntilNodeAppears(selector);
    return GuiTest.find(selector);
  }

  public <T extends Node> Optional<T> findQuiet(String selectorOrText) {
    try {
      return Optional.of(GuiTest.find(selectorOrText));
    } catch (NoNodesFoundException | NoNodesVisibleException e) {
      return Optional.empty();
    }
  }

  private <T extends Node> Optional<T> findQuiet(Matcher<Object> matcher) {
    try {
      return Optional.ofNullable(GuiTest.find(matcher));
    } catch (NoNodesFoundException | NoNodesVisibleException e) {
      return Optional.empty();
    }

  }

  public boolean existsQuiet(String selector) {
    try {
      return GuiTest.exists(selector);
    } catch (NoNodesFoundException | NoNodesVisibleException e) {
      return false;
    }
  }

  /**
   * Allows test threads to busy-wait on some condition.
   * <p>
   * Taken from org.loadui.testfx.utils, but modified to synchronise with the
   * JavaFX Application Thread, with a lower frequency. The additional
   * synchronisation prevents bugs where
   * <p>
   * awaitCondition(a); awaitCondition(b);
   * <p>
   * sometimes may not be equivalent to
   * <p>
   * awaitCondition(a && b);
   * <p>
   * The lower frequency is a bit more efficient, since a frequency of 10 ms
   * just isn't necessary for GUI interactions, and we're bottlenecked by the FX
   * thread anyway.
   */
  public void awaitCondition(Callable<Boolean> condition) {
    awaitCondition(condition, 5);
  }

  protected void awaitCondition(Callable<Boolean> condition, int timeoutInSeconds) {
    long timeout = System.currentTimeMillis() + timeoutInSeconds * 1000;
    try {
      while (!condition.call()) {
        Thread.sleep(100);
        PlatformEx.waitOnFxThread();
        if (System.currentTimeMillis() > timeout) {
          throw new TimeoutException();
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void selectAllText(){
    push(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
  }

  public void overwriteText(String newText){
    selectAllText();
    write(newText);
  }
}
