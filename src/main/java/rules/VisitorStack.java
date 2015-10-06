package rules;

import java.util.HashMap;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import org.slf4j.LoggerFactory;

import utils.TreeVisitor;
import utils.WalkFileTree;

/**
 * Created by adrapereira on 06-10-2015.
 */
public class VisitorStack extends Observable{
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(VisitorStack.class.getName());
    private ExecutorService visitors;
    private HashMap<String, Future> futures;
    private String runningTask;

    public VisitorStack(){
        visitors = Executors.newSingleThreadExecutor();
        futures = new HashMap<String, Future>();
    }

    public void add(String path, TreeVisitor vis){
        final WalkFileTree walker = new WalkFileTree(path, vis);
        final String id = vis.getId();
        Task toRun = new Task<Void>() {
            public Void call() {
                walker.start();
                try {
                    walker.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    walker.interrupt();
                }
                return null;
            }
        };
        toRun.setOnRunning(new EventHandler<WorkerStateEvent>() {
            public void handle(WorkerStateEvent workerStateEvent) {
                runningTask = id;
                update();
            }
        });
        //notify the observers when the task finishes
        toRun.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            public void handle(WorkerStateEvent workerStateEvent) {
                runningTask = null;
                update();
            }
        });

        Future fut = visitors.submit(toRun);
        futures.put(vis.getId(), fut);
        update();
    }

    private void update(){
        setChanged();
        notifyObservers();
    }

    public VisitorState isDone(String visitorId){
        Future fut = futures.get(visitorId);
        if(fut == null) return VisitorState.VISITOR_NOTSUBMITTED;
        if(runningTask != null && visitorId.equals(runningTask)) return VisitorState.VISITOR_RUNNING;
        if(fut.isDone()) return VisitorState.VISITOR_DONE;
        else return VisitorState.VISITOR_QUEUED;
    }

    public boolean cancel(TreeVisitor vis){
        if(vis != null && futures.containsKey(vis.getId()))
            return futures.get(vis.getId()).cancel(true);
        else return false;
    }
}
