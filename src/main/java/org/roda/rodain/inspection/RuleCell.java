package org.roda.rodain.inspection;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang.StringUtils;
import org.roda.rodain.core.Main;
import org.roda.rodain.rules.Rule;
import org.roda.rodain.rules.RuleTypes;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeFile;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.utils.FontAwesomeImageCreator;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 16-11-2015.
 */
public class RuleCell extends HBox {
    private static Properties properties;
    private Rule rule;
    private SchemaNode schemaNode;
    public RuleCell(SchemaNode node, Rule rule){
        this.rule = rule;
        this.schemaNode = node;
        this.getStyleClass().add("ruleCell");

        VBox root = new VBox(5);
        HBox.setHgrow(root, Priority.ALWAYS);

        HBox top = createTop();
        VBox center = createCenter();

        root.getChildren().addAll(top, center);
        getChildren().add(root);
    }

    private HBox createTop(){
        HBox top =  new HBox(10);
        top.setPadding(new Insets(5, 5, 5, 5));
        top.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(top, Priority.ALWAYS);
        top.getStyleClass().add("top");

        HBox spaceLeft = new HBox();
        HBox spaceRight = new HBox();
        HBox.setHgrow(spaceLeft, Priority.ALWAYS);
        HBox.setHgrow(spaceRight, Priority.ALWAYS);

        Label id = new Label("#" + rule.getId());
        id.getStyleClass().add("title");

        Button remove = new Button("Remove");
        remove.setAlignment(Pos.CENTER);

        remove.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                schemaNode.removeRule(rule);
                Main.inspectionNotifyChanged();
            }
        });

        int sipCount = rule.getSipCount();
        String format = "Created %d item";
        if(sipCount != 1){
            format += "s";
        }
        String created = String.format(format, rule.getSipCount());
        Label lCreated = new Label(created);

        top.getChildren().addAll(id, spaceLeft, lCreated, spaceRight, remove);
        return top;
    }

    private VBox createCenter(){
        VBox content = new VBox(5);
        content.setPadding(new Insets(5, 5, 5, 5));

        // rule type
        RuleTypes ruleType = rule.getAssocType();
        String type;
        switch (ruleType){
            case SINGLESIP:
                type = properties.getProperty("association.singleSip.title");
                break;
            case SIPPERFILE:
                type = properties.getProperty("association.sipPerFile.title");
                break;
            case SIPPERFOLDER:
                type = properties.getProperty("association.sipPerFolder.title");
                break;
            case SIPPERSELECTION:
                type = properties.getProperty("association.sipSelection.title");
                break;
            default:
                type = "Unknown association type";
                break;
        }
        Label lType = new Label(type);

        // source items
        Set<SourceTreeItem> source = rule.getSource();
        ArrayList<String> dirs = new ArrayList<>();
        ArrayList<String> fil = new ArrayList<>();
        for(SourceTreeItem it: source) {
            if(it instanceof SourceTreeDirectory)
                dirs.add(it.getValue());
            else fil.add(it.getValue());
        }

        VBox sourceBox = new VBox(5);
        if(dirs.size() > 0) {
            Label directories = new Label();
            directories.maxWidthProperty().bind(widthProperty().subtract(20));
            directories.setGraphic(new ImageView(SourceTreeDirectory.folderCollapseImage));
            directories.setWrapText(true);
            String directoriesString = StringUtils.join(dirs, ", ");
            directories.setText(directoriesString);
            sourceBox.getChildren().add(directories);
        }
        if(fil.size() > 0) {
            Label files = new Label();
            files.maxWidthProperty().bind(widthProperty().subtract(20));
            files.setGraphic(new ImageView(SourceTreeFile.fileImage));
            files.setWrapText(true);
            String filesString = StringUtils.join(fil, ", ");
            files.setText(filesString);
            sourceBox.getChildren().add(files);
        }
        content.getChildren().addAll(lType, sourceBox);

        return content;
    }

    public static void setProperties(Properties prop){
        properties = prop;
    }
}
