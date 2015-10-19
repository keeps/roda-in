package rules.ui;

import java.util.Set;

import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import schema.ui.SchemaNode;
import source.ui.items.SourceTreeItem;

/**
 * Created by adrapereira on 15-10-2015.
 */
public class RuleModalStage extends Stage{
    public RuleModalStage(final Stage primaryStage, Set<SourceTreeItem> source, SchemaNode schema){
        super(StageStyle.TRANSPARENT);
        initModality(Modality.WINDOW_MODAL);
        initOwner(primaryStage);

        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(-0.25);
        primaryStage.getScene().getRoot().setEffect(colorAdjust);

        setScene(new Scene(new HBox(), 400, 200));

        show();
    }

    @Override
    public void close(){
        getOwner().getScene().getRoot().setEffect(null);
        super.close();
    }

    public void setRoot(Parent root){
        this.getScene().setRoot(root);

        // allow the dialog to be dragged around.
        final Delta dragDelta = new Delta();
        final RuleModalStage thisDialog = this; //reference to be used in the handlers
        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent mouseEvent) {
                // record a delta distance for the drag and drop operation.
                dragDelta.x = thisDialog.getX() - mouseEvent.getScreenX();
                dragDelta.y = thisDialog.getY() - mouseEvent.getScreenY();
            }
        });
        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent mouseEvent) {
                thisDialog.setX(mouseEvent.getScreenX() + dragDelta.x);
                thisDialog.setY(mouseEvent.getScreenY() + dragDelta.y);
            }
        });
    }

    // records relative x and y co-ordinates.
    class Delta { double x, y; }
}
