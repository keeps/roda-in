package org.roda.rodain.source.ui;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static org.roda.rodain.source.ui.FileExplorerPane.watcher;

import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;

import org.roda.rodain.core.PathCollection;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeItem;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-08-2016.
 */
public class DirectoryWatcher extends Thread {

  @Override
  public void run() {
    for (;;) {

      // wait for key to be signaled
      WatchKey key;
      try {
        key = watcher.take();
      } catch (InterruptedException | ClosedWatchServiceException x) {
        return;
      }

      //WatchKey watchable returns the calling Path object of Path.register
      Path watchedPath = (Path) key.watchable();

      for (WatchEvent<?> event: key.pollEvents()) {
        WatchEvent.Kind<?> kind = event.kind();

        // This key is registered only
        // for ENTRY_CREATE and ENTRY_DELETE events,
        // but an OVERFLOW event can
        // occur regardless if events
        // are lost or discarded.
        if (kind == OVERFLOW) {
          continue;
        }

        // The filename is the
        // context of the event.
        WatchEvent<Path> ev = (WatchEvent<Path>)event;
        Path filename = ev.context();

        String fullPath = watchedPath.resolve(filename).toString();
        SourceTreeItem sourceTreeItem = PathCollection.getItem(watchedPath.toString());
        if(sourceTreeItem instanceof SourceTreeDirectory){
          SourceTreeDirectory directory = ((SourceTreeDirectory) sourceTreeItem);
          if(kind == ENTRY_CREATE) {
            directory.addChild(fullPath);
          }else if(kind == ENTRY_DELETE){
            SourceTreeItem itemToDelete = PathCollection.getItem(fullPath);
            if(itemToDelete != null) {
              directory.removeChild(itemToDelete);
            }
            PathCollection.removePathAndItem(fullPath);
          }
        }
      }

      // Reset the key -- this step is critical if you want to
      // receive further watch events.  If the key is no longer valid,
      // the directory is inaccessible so exit the loop.
      boolean valid = key.reset();
      if (!valid) {
        break;
      }
    }
  }
}
