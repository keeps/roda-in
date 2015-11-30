package org.roda.rodain.rules;

import java.util.HashMap;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.utils.TreeVisitor;
import org.roda.rodain.utils.WalkFileTree;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 06-10-2015.
 */
public class VisitorStack extends Observable {
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(VisitorStack.class.getName());
  private static ExecutorService visitors;
  private HashMap<String, Future> futures;
  private String runningTask;

  public VisitorStack() {
    visitors = Executors.newSingleThreadExecutor();
    futures = new HashMap<>();
  }

  /**
   * Adds a new TreeVisitor to the stack.
   *
   * <p>
   * Creates a new WalkFileTree with the set of paths and TreeVisitor received
   * as parameter. Wraps it in a Task and adds the Task to an ExecutorService.
   * </p>
   * 
   * @param items
   *          The set of items associated in the Rule
   * @param paths
   *          The set of paths associated in the Rule
   * @param vis
   *          The TreeVisitor created by the Rule
   *
   * @see TreeVisitor
   * @see ExecutorService
   */
  public void add(final Set<SourceTreeItem> items, Set<String> paths, TreeVisitor vis) {
    final WalkFileTree walker = new WalkFileTree(paths, vis);
    final String id = vis.getId();
    Task toRun = new Task<Void>() {
      @Override
      public Void call() {
        walker.start();
        try {
          walker.join();
        } catch (InterruptedException e) {
          log.debug(e.getMessage());
          walker.interrupt();
        }
        return null;
      }
    };
    toRun.setOnRunning(new EventHandler<WorkerStateEvent>() {
      @Override
      public void handle(WorkerStateEvent workerStateEvent) {
        runningTask = id;
        update();
      }
    });
    // notify the observers when the task finishes
    toRun.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
      @Override
      public void handle(WorkerStateEvent workerStateEvent) {
        runningTask = null;
        update();
      }
    });

    Future fut = visitors.submit(toRun);
    futures.put(vis.getId(), fut);
    update();
  }

  private void update() {
    setChanged();
    notifyObservers();
  }

  /**
   * @param visitorId
   *          The id of the TreeVisitor we want to know the state of.
   * @return The state of the TreeVisitor.
   * @see TreeVisitor
   */
  public VisitorState getState(String visitorId) {
    Future fut = futures.get(visitorId);
    if (fut == null)
      return VisitorState.VISITOR_NOTSUBMITTED;
    if (runningTask != null && visitorId.equals(runningTask))
      return VisitorState.VISITOR_RUNNING;
    if (fut.isDone())
      return VisitorState.VISITOR_DONE;
    else
      return VisitorState.VISITOR_QUEUED;
  }

  /**
   * Cancels the execution of the TreeVisitor received as parameter.
   * 
   * @param vis
   *          The TreeVisitor to be canceled
   * @return True if the TreeVisitor has been canceled, false otherwise.
   * @see TreeVisitor
   */
  public boolean cancel(TreeVisitor vis) {
    if (vis != null && futures.containsKey(vis.getId()))
      return futures.get(vis.getId()).cancel(true);
    else
      return false;
  }

  /**
   * Shuts down all TreeVisitors.
   * 
   * @see TreeVisitor
   */
  public static void end() {
    if (visitors != null)
      visitors.shutdownNow();
  }
}
