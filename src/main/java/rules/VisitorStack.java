package rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import utils.TreeVisitor;
import utils.WalkFileTree;

/**
 * Created by adrapereira on 06-10-2015.
 */
public class VisitorStack extends Observable{
    private ExecutorService visitors;
    private HashMap<String, Future> futures;

    public VisitorStack(){
        visitors = Executors.newSingleThreadExecutor();
        futures = new HashMap<String, Future>();
    }

    public void add(String path, TreeVisitor vis){
        final WalkFileTree walker = new WalkFileTree(path, vis);
        Future fut = visitors.submit(new Runnable() {
            public void run() {
                walker.start();
                try {
                    walker.join();
                } catch (InterruptedException e) { e.printStackTrace(); }
                finished();
            }
        });
        futures.put(vis.getId(), fut);
    }

    private void finished(){
        setChanged();
        notifyObservers();
    }

    public HashSet<String> getDone(){
        HashSet<String> result = new HashSet<String>();
        for(String s: futures.keySet()){
            if(futures.get(s).isDone())
                result.add(s);
        }
        return result;
    }

    public boolean cancel(TreeVisitor vis){
        return futures.get(vis.getId()).cancel(true);
    }
}
