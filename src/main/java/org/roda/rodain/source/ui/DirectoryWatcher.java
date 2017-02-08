package org.roda.rodain.source.ui;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static org.roda.rodain.source.ui.FileExplorerPane.watcher;

import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.util.List;
import java.util.Map;

import org.roda.rodain.core.PathCollection;
import org.roda.rodain.core.RodaIn;
import org.roda.rodain.creation.EarkSipCreator;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.schema.DescriptionObject;
import org.roda.rodain.sip.SipPreview;
import org.roda.rodain.sip.SipRepresentation;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-08-2016.
 */
public class DirectoryWatcher extends Thread {
  private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryWatcher.class.getName());

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
        SourceTreeItem sourceTreeItem = PathCollection.getItem(watchedPath);
        if(sourceTreeItem instanceof SourceTreeDirectory){
          SourceTreeDirectory directory = ((SourceTreeDirectory) sourceTreeItem);
          if(kind == ENTRY_CREATE) {
            LOGGER.debug("CREATE "+fullPath);
            directory.addChild(fullPath);
            PathCollection.addPath(watchedPath.resolve(filename), SourceTreeItemState.NORMAL);
          }else if(kind == ENTRY_DELETE){
            LOGGER.debug("DELETE "+fullPath);
            SourceTreeItem itemToDelete = PathCollection.getItem(Paths.get(fullPath));
            if(itemToDelete==null){     //file
              Map<DescriptionObject,List<String>> dos = RodaIn.getAllDescriptionObjects();
              boolean match = false;
              for(Map.Entry<DescriptionObject, List<String>> entry : dos.entrySet()){
                DescriptionObject descriptionObject = entry.getKey();
                if (descriptionObject instanceof SipPreview) {
                  SipPreview sip = (SipPreview) descriptionObject;
                  for (SipRepresentation sr : sip.getRepresentations()) {
                    if(sr.getFiles()!=null){
                      for(TreeNode t : sr.getFiles()){
                        if(t.getPath()!=null){
                          if(t.getPath().toString().equalsIgnoreCase(fullPath)){
                            sr.remove(t.getPath());
                            if(sr.getFiles().size()==0){
                              sip.removeRepresentation(sr);
                            }
                            match = true;
                            break;
                          }
                        }
                        
                      }
                      if(match){
                        break;
                      }
                    }
                  }
                }
                if(match){
                  break;
                }
              }
            }else{
              //TODO support folder removing when mapped...
             
              directory.removeChild(itemToDelete);
              
            }
            PathCollection.removePathAndItem(Paths.get(fullPath));
          }else{
            LOGGER.error(kind.toString()+" "+fullPath);
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
