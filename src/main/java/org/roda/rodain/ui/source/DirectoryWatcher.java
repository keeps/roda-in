package org.roda.rodain.ui.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FIXME 20170310 hsilva: this class will/should/must be removed
 * 
 * @author Andre Pereira apereira@keep.pt
 * @since 29-08-2016.
 */
@Deprecated
public class DirectoryWatcher extends Thread {
  private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryWatcher.class.getName());

  @Override
  public void run() {
    // 20170308 hsilva: disabled watchservice
    // for (;;) {
    //
    // // wait for key to be signaled
    // WatchKey key;
    // try {
    // key = watcher.take();
    // } catch (InterruptedException | ClosedWatchServiceException x) {
    // return;
    // }
    //
    // // WatchKey watchable returns the calling Path object of Path.register
    // Path watchedPath = (Path) key.watchable();
    //
    // for (WatchEvent<?> event : key.pollEvents()) {
    // WatchEvent.Kind<?> kind = event.kind();
    //
    // // This key is registered only
    // // for ENTRY_CREATE and ENTRY_DELETE events,
    // // but an OVERFLOW event can
    // // occur regardless if events
    // // are lost or discarded.
    // if (kind == OVERFLOW) {
    // continue;
    // }
    //
    // // The filename is the
    // // context of the event.
    // WatchEvent<Path> ev = (WatchEvent<Path>) event;
    // Path filename = ev.context();
    //
    // String fullPath = watchedPath.resolve(filename).toString();
    // SourceTreeItem sourceTreeItem = PathCollection.getItem(watchedPath);
    // if (sourceTreeItem instanceof SourceTreeDirectory) {
    // SourceTreeDirectory directory = ((SourceTreeDirectory) sourceTreeItem);
    // if (kind == ENTRY_CREATE) {
    // directory.addChild(fullPath);
    // } else if (kind == ENTRY_DELETE) {
    // SourceTreeItem itemToDelete =
    // PathCollection.getItem(Paths.get(fullPath));
    // if (itemToDelete != null) {
    // directory.removeChild(itemToDelete);
    // }
    // PathCollection.removePathAndItem(Paths.get(fullPath));
    // }
    // }
    // }
    //
    // // Reset the key -- this step is critical if you want to
    // // receive further watch events. If the key is no longer valid,
    // // the directory is inaccessible so exit the loop.
    // boolean valid = key.reset();
    // if (!valid) {
    // break;
    // }
    // }
  }
}
