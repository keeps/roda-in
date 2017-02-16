package org.roda.rodain.source.ui;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.roda.rodain.core.PathCollection;
import org.roda.rodain.core.RodaIn;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.roda.rodain.utils.TreeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateFolderTreeVisitor implements TreeVisitor {
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateFolderTreeVisitor.class.getName());

  @Override
  public void preVisitDirectory(Path path, BasicFileAttributes attrs) {
    // TODO Auto-generated method stub

  }

  @Override
  public void postVisitDirectory(Path path) {
    LOGGER.error("DIR: "+path.toString());
    SourceTreeItemState state = PathCollection.getStateWithoutFallback(path);
    if(state==null ){
      PathCollection.addPath(path, SourceTreeItemState.NORMAL);
      LOGGER.error("Need to add "+path.toString());
    }else{
      LOGGER.error("State: "+state.toString());
      PathCollection.addPath(path, state);
    }
    
  }

  @Override
  public void visitFile(Path path, BasicFileAttributes attrs) {
    LOGGER.error("FILE: "+path.toString());
    SourceTreeItemState state = PathCollection.getStateWithoutFallback(path);
    if(state==null){
      LOGGER.error("Need to add "+path.toString());
      PathCollection.addPath(path, SourceTreeItemState.NORMAL);
    }else{
      LOGGER.error("State: "+state.toString());
      PathCollection.addPath(path, state);
    }
  }

  @Override
  public void visitFileFailed(Path path) {
    // TODO Auto-generated method stub

  }

  @Override
  public void end() {
    // TODO Auto-generated method stub

  }

  @Override
  public String getId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setStartPath(String path) {
    // TODO Auto-generated method stub

  }

}
