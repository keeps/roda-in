package rodain.inspection;

import java.io.IOException;
import java.util.Set;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;
import rodain.schema.ui.SchemaNode;
import rodain.source.ui.items.SourceTreeDirectory;
import rodain.source.ui.items.SourceTreeFile;
import rodain.source.ui.items.SourceTreeItem;

/**
 * Created by adrapereira on 27-10-2015.
 */
public class LoadingPane extends BorderPane {
    private static Image loadingGif;
    private Set<SourceTreeItem> sourceSet;
    private SchemaNode schema;

    public LoadingPane(Set<SourceTreeItem> sourceSet, SchemaNode schemaNode){
        super();

        this.sourceSet = sourceSet;
        this.schema = schemaNode;
        createTop();

        setStyle("-fx-border-color: lightgray; -fx-border-width: 2px; -fx-background-color: white;");

        HBox centerBox = new HBox();
        centerBox.setAlignment(Pos.CENTER);
        try {
            if(loadingGif == null)
                loadingGif = new Image(ClassLoader.getSystemResource("loading.GIF").openStream());
            centerBox.getChildren().add(new ImageView(loadingGif));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setCenter(centerBox);
    }

    private void createTop(){
        StackPane pane = new StackPane();
        pane.setPadding(new Insets(0, 0, 10, 0));

        HBox hbox = new HBox();
        HBox.setHgrow(hbox, Priority.ALWAYS);
        hbox.setStyle("-fx-background-color: lightgray;");
        hbox.setPadding(new Insets(5, 5, 5, 5));
        pane.getChildren().add(hbox);

        Label source = new Label();
        StringBuilder sb = new StringBuilder();
        for(SourceTreeItem it: sourceSet) {
            if(it instanceof SourceTreeDirectory)
                sb.append(((SourceTreeDirectory) it).getValue()).append(",");
            else if(it instanceof SourceTreeFile)
                sb.append(((SourceTreeFile)it).getValue()).append(",");
        }
        //remove the last comma
        int lastComma = sb.lastIndexOf(",");
        sb.replace(lastComma, lastComma + 1,"");
        source.setText(sb.toString());

        source.setMinHeight(24);
        source.setId("title");
        source.setGraphic(new ImageView(SourceTreeDirectory.folderCollapseImage));
        source.setStyle(" -fx-text-fill: black");
        source.setWrapText(true);

        Label descObj = new Label(schema.getDob().getTitle());
        descObj.setMinHeight(24);
        descObj.setId("title");
        descObj.setGraphic(new ImageView(schema.getImage()));
        descObj.setTextAlignment(TextAlignment.LEFT);
        descObj.setStyle(" -fx-text-fill: black");

        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);
        space.setMinWidth(50);

        hbox.getChildren().addAll(source, space, descObj);

        setTop(pane);
    }
}
