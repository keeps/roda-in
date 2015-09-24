/**
 * Created by adrapereira on 16-09-2015.
 */

import javafx.scene.control.*;
import javafx.scene.layout.*;
import schema.ClassificationSchema;
import schema.DescriptionObject;
import schema.ui.SchemaNode;
import source.ui.FileExplorerPane;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {
    public Stage stage;
    private double minWidth;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        createFrameStructure();
    }

    private void createFrameStructure(){
        // Maximize window
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        minWidth = bounds.getWidth()/3 * 0.7;

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        // Divide Pane in 3
        SplitPane split = new SplitPane();

        //StackPane previewExplorer = createPreviewExplorer();
        BorderPane previewExplorer = new FileExplorerPane(minWidth, stage);
        StackPane rulesPane = createRulesPane();
        StackPane schemaPane = createSchemaPane();

        split.getItems().addAll(previewExplorer, rulesPane, schemaPane);

        // setup and show the window
        stage.setTitle("RODA-In");
        stage.setScene(new Scene(split, bounds.getWidth(), bounds.getHeight()));
        stage.show();
    }

    private StackPane createSchemaPane(){
        //create tree pane
        VBox treeBox=new VBox();
        treeBox.setPadding(new Insets(10, 10, 10, 10));
        treeBox.setSpacing(10);

        TreeItem<String> rootNode = new TreeItem<String>();
        rootNode.setExpanded(true);

        // get the classification schema and add all its nodes to the tree
        ClassificationSchema cs = ClassificationSchema.instantiate();
        for(DescriptionObject obj: cs.getDos()){
            SchemaNode sn = new SchemaNode(obj);
            rootNode.getChildren().add(sn);
        }

        // create the tree view
        TreeView<String> treeView=new TreeView<String>(rootNode);
        treeView.setShowRoot(false);
        // add everything to the tree pane
        treeBox.getChildren().addAll(new Label("Classification Schema"), treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);

        StackPane schemaPane = new StackPane();
        schemaPane.getChildren().add(treeBox);

        schemaPane.setMinWidth(minWidth);

        return schemaPane;
    }

    public StackPane createRulesPane(){
        StackPane rulesPane = new StackPane();
        rulesPane.setMinWidth(minWidth);
        return rulesPane;
    }
}
