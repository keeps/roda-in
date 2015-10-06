package rules.ui;

import java.util.Observable;
import java.util.Observer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.TextAlignment;

import org.slf4j.LoggerFactory;

import rules.Rule;
import rules.RuleTypes;
import rules.VisitorStack;
import rules.VisitorState;
import schema.ui.SchemaNode;
import source.ui.items.SourceTreeDirectory;
import utils.TreeVisitor;
import core.Main;

/**
 * Created by adrapereira on 28-09-2015.
 */
public class RuleComponent extends BorderPane implements Observer {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RuleComponent.class.getName());
    private RuleComponent toRemove = this; //we need a pointer to this object so we can send it when the "remove" button is pressed
    private SchemaNode schema;
    private Rule rule;
    private ToggleGroup group;
    private ComboBox<Integer> level;
    private Label l_sipCount, l_state;

    private HBox buttons;
    private Button apply, remove, cancel;

    private VisitorStack visitors;

    public RuleComponent(SourceTreeDirectory sourcePath, SchemaNode schemaNode, VisitorStack visitors){
        super();
        schema = schemaNode;
        this.visitors = visitors;
        this.visitors.addObserver(this);
        setStyle("-fx-border-color: lightgray; -fx-border-width: 2px; -fx-background-color: white;");

        rule = new Rule(sourcePath);
        rule.addObserver(this);
        rule.addObserver(schema);

        //schema.addRule(rule);

        createTop();
        createCenter();
        createBottom();
    }

    private void createTop(){
        StackPane pane = new StackPane();
        pane.setPadding(new Insets(0, 0, 10, 0));

        HBox hbox = new HBox();
        HBox.setHgrow(hbox, Priority.ALWAYS);
        hbox.setStyle("-fx-background-color: lightgray;");
        hbox.setPadding(new Insets(5, 5, 5, 5));
        pane.getChildren().add(hbox);

        Label source = new Label(rule.getFolderName());
        source.setMinHeight(24);
        source.setFont(new Font("System", 14));
        source.setGraphic(new ImageView(SourceTreeDirectory.folderCollapseImage));

        Label descObj = new Label(schema.dob.getTitle());
        descObj.setMinHeight(24);
        descObj.setFont(new Font("System", 14));
        descObj.setGraphic(new ImageView(schema.getImage()));
        descObj.setTextAlignment(TextAlignment.LEFT);

        HBox spaceLeft = new HBox();
        HBox.setHgrow(spaceLeft, Priority.ALWAYS);
        HBox spaceRight = new HBox();
        HBox.setHgrow(spaceRight, Priority.ALWAYS);

        l_sipCount = new Label();
        l_sipCount.setMinHeight(24);
        l_sipCount.setFont(Font.font("System", FontPosture.ITALIC, 12));
        l_sipCount.setStyle("-fx-opacity: 0.5;");

        hbox.getChildren().addAll(source, spaceLeft, l_sipCount, spaceRight, descObj);

        setTop(pane);
    }

    private void createCenter(){
        GridPane gridCenter = new GridPane();
        gridCenter.setPadding(new Insets(0, 10, 0, 10));
        gridCenter.setHgap(10);

        group = new ToggleGroup();

        RadioButton byFile = new RadioButton("1 SIP por ficheiro");
        byFile.setToggleGroup(group);
        byFile.setUserData(RuleTypes.SIPPERFILE);
        byFile.setSelected(true);

        RadioButton byFolder = new RadioButton("1 SIP por pasta até ao nível");
        byFolder.setUserData(RuleTypes.SIPPERFOLDER);
        byFolder.setToggleGroup(group);

        ObservableList<Integer> options = FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10);
        level = new ComboBox<Integer>(options);
        level.setValue(3);

        gridCenter.add(byFile, 0, 1);
        gridCenter.add(byFolder, 0, 2);
        gridCenter.add(level, 1, 2);

        setCenter(gridCenter);
    }

    private void createBottom(){
        remove = new Button("Remove");
        apply = new Button("Apply");
        l_state = new Label("");
        VBox.setVgrow(l_state, Priority.ALWAYS);
        l_state.setAlignment(Pos.BOTTOM_CENTER);
        l_state.setOpacity(0.6);

        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);

        buttons = new HBox();
        buttons.setPadding(new Insets(10, 10, 10, 10));
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(remove, space, l_state, apply);


        apply.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                apply.setDisable(true);
                remove.setText("Cancel");
                //we need to remove the traces of this rule in the schema node, so we can then add the new SIPs
                schema.removeRule(rule);
                apply();
                schema.addRule(rule);
            }
        });

        remove.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if(remove.getText().contains("Cancel")){
                    boolean cancelled = visitors.cancel(rule.getVisitor());
                    if(cancelled) {
                        apply.setDisable(false);
                        remove.setText("Remove");
                    }
                }else {
                    //remove the count on the associated schema node
                    schema.removeRule(rule);
                    Main.removeRule(toRemove);
                }
            }
        });

        setBottom(buttons);
    }

    public void update(Observable o, Object arg) {
        if(o == rule){
            Platform.runLater(new Runnable() {
                public void run() {
                    l_sipCount.setText(rule.getSipCount() + " items");
                }
            });
        }else if(o == visitors){
            VisitorState state = visitors.isDone(rule.getId());
            switch (state){
                case VISITOR_DONE:
                    apply.setDisable(false);
                    remove.setText("Remove");
                    l_state.setText("Finished!");
                    break;
                case VISITOR_RUNNING:
                    l_state.setText("Processing!");
                    break;
                case VISITOR_NOTSUBMITTED:
                    break;
                case VISITOR_QUEUED:
                    remove.setText("Cancel");
                    apply.setDisable(true);
                    l_state.setText("Queued!");
                    break;
                default:
                    break;
            }
        }
    }

    private void apply(){
        Toggle active = group.getSelectedToggle();
        if (active.getUserData() instanceof RuleTypes) {
            RuleTypes type = (RuleTypes) active.getUserData();
            int lev = level.getValue();
            TreeVisitor visitor = rule.apply(type, lev);
            visitors.add(rule.getSource().getPath(), visitor);
        }
    }

}
