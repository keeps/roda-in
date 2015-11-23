package org.roda.rodain.inspection;

import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 21-09-2015.
 */
public class ContentClickedEventHandler implements EventHandler<MouseEvent> {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ContentClickedEventHandler.class.getName());
    private TreeView<Object> treeView;
    private InspectionPane ipane;

    public ContentClickedEventHandler(TreeView<Object> tree, InspectionPane pane){
        this.treeView = tree;
        ipane = pane;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            TreeItem<Object> item = treeView.getSelectionModel().getSelectedItem();
            if(item instanceof SipContentDirectory) {
                ipane.setStateContentButtons(false);
            }else if(item instanceof SipContentFile){
                ipane.setStateContentButtons(true);
            }
        }else if (mouseEvent.getClickCount() == 2) {
            Object source = treeView.getSelectionModel().getSelectedItem();
            if(source instanceof SipContentFile) {
                SipContentFile sipFile = (SipContentFile) source;
                try {
                    Desktop.getDesktop().open(new File(sipFile.getPath().toString()));
                } catch (IOException e) {
                    log.info("Error opening file from SIP content", e);
                }
            }
        }
    }
}
