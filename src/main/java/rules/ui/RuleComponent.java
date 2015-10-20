package rules.ui;

import java.util.ArrayList;
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
import javafx.scene.text.TextAlignment;

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
    private RuleComponent toRemove = this; //we need a pointer to this object so we can send it when the "remove" button is pressed
    private SchemaNode schema;
    private Rule rule;
    private ToggleGroup group;
    private ComboBox<Integer> level;
    private Label lState;

    private HBox buttons;
    private Button apply, remove;

    private VisitorStack visitors;

    public RuleComponent(SourceTreeDirectory sourcePath, SchemaNode schemaNode, VisitorStack visitors){
        super();
        schema = schemaNode;
        this.visitors = visitors;
        this.visitors.addObserver(this);
        setStyle("-fx-border-color: lightgray; -fx-border-width: 2px; -fx-background-color: white;");

        //TODO
        //rule = new Rule(Paths.get(sourcePath.getPath()), RuleTypes.SINGLESIP, 1, null, null);
        rule.addObserver(this);
        rule.addObserver(schema);

        createTop();
        createCenter();
        createBottom();

        apply();
    }

    private void createTop(){
        StackPane pane = new StackPane();
        pane.setPadding(new Insets(0, 0, 10, 0));

        HBox hbox = new HBox();
        HBox.setHgrow(hbox, Priority.ALWAYS);
        hbox.setStyle("-fx-background-color: lightgray;");
        hbox.setPadding(new Insets(5, 5, 5, 5));
        pane.getChildren().add(hbox);

        Label source = new Label();//rule.getFolderName());
        source.setMinHeight(24);
        source.setFont(new Font("System", 14));
        source.setGraphic(new ImageView(SourceTreeDirectory.folderCollapseImage));
        source.setStyle(" -fx-text-fill: black");

        Label descObj = new Label(schema.getDob().getTitle());
        descObj.setMinHeight(24);
        descObj.setFont(new Font("System", 14));
        descObj.setGraphic(new ImageView(schema.getImage()));
        descObj.setTextAlignment(TextAlignment.LEFT);
        descObj.setStyle(" -fx-text-fill: black");

        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);


        hbox.getChildren().addAll(source, space, descObj);

        setTop(pane);
    }

    private void createCenter(){
        GridPane gridCenter = new GridPane();
        gridCenter.setPadding(new Insets(0, 10, 0, 10));
        gridCenter.setVgap(5);
        gridCenter.setHgap(10);

        group = new ToggleGroup();

        RadioButton singleSip = new RadioButton("Create a single SIP");
        singleSip.setToggleGroup(group);
        singleSip.setUserData(RuleTypes.SIPPERFILE);
        singleSip.setSelected(true);
        singleSip.setStyle(" -fx-text-fill: black");

        RadioButton perFile = new RadioButton("Create one SIP per file");
        perFile.setToggleGroup(group);
        perFile.setUserData(RuleTypes.SIPPERFILE);
        perFile.setSelected(true);
        perFile.setStyle(" -fx-text-fill: black");

        RadioButton byFolder = new RadioButton("Create one SIP per folder until level");
        byFolder.setUserData(RuleTypes.SIPPERFOLDER);
        byFolder.setToggleGroup(group);
        byFolder.setStyle(" -fx-text-fill: black");

        //Path startPath = rule.getSource();
        int depth = 0;//Utils.getRelativeMaxDepth(startPath);

        ArrayList<Integer> levels = new ArrayList<>();
        for(int i = 1; i <= depth; i++)
            levels.add(i);

        ObservableList<Integer> options = FXCollections.observableArrayList(levels);
        level = new ComboBox<>(options);
        level.setValue((int)Math.ceil(depth/2.0));

        gridCenter.add(singleSip, 0, 1);
        gridCenter.add(perFile, 0, 2);
        gridCenter.add(byFolder, 0, 3);
        gridCenter.add(level, 1, 3);

        setCenter(gridCenter);
    }

    private void createBottom(){
        remove = new Button("Remove");
        apply = new Button("Apply");
        lState = new Label("");
        VBox.setVgrow(lState, Priority.ALWAYS);
        lState.setAlignment(Pos.BOTTOM_CENTER);
        lState.setStyle(" -fx-text-fill: darkgrey");

        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);

        buttons = new HBox();
        buttons.setPadding(new Insets(10, 10, 10, 10));
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(remove, space, lState, apply);


        apply.setOnAction(new EventHandler<ActionEvent>() {
            @Override
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
            @Override
            public void handle(ActionEvent e) {
                if (remove.getText().contains("Cancel")) {
                    boolean cancelled = visitors.cancel(rule.getVisitor());
                    if (cancelled) {
                        apply.setDisable(false);
                        remove.setText("Remove");
                    }
                } else {
                    //remove the count on the associated schema node
                    schema.removeRule(rule);
                    //Main.removeRule(toRemove);
                }
            }
        });

        setBottom(buttons);
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o == rule){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    lState.setText(rule.getSipCount() + " items ...");
                }
            });
        }else if(o == visitors){
            VisitorState state = visitors.isDone(rule.getId());
            switch (state){
                case VISITOR_DONE:
                    apply.setDisable(false);
                    schema.setExpanded(true);
                    remove.setText("Remove");
                    lState.setText(rule.getSipCount() + " items");
                    break;
                case VISITOR_RUNNING:
                    lState.setText(rule.getSipCount() + " items ...");
                    break;
                case VISITOR_NOTSUBMITTED:
                    break;
                case VISITOR_QUEUED:
                    remove.setText("Cancel");
                    apply.setDisable(true);
                    lState.setText("Waiting in queue...");
                    break;
                default:
                    break;
            }
        }
    }

    private void apply(){
        /*Toggle active = group.getSelectedToggle();
        if (active.getUserData() instanceof RuleTypes) {
            RuleTypes type = (RuleTypes) active.getUserData();
            int lev = level.getValue();
            TreeVisitor visitor = rule.apply(type, lev);
            visitors.add(rule.getSourceString(), visitor);
        }*/
    }

    public Rule getRule(){
        return rule;
    }
    public RuleTypes getType(){
        Toggle active = group.getSelectedToggle();
        return (RuleTypes)active.getUserData();
    }
    public int getLevel(){
        return level.getValue();
    }
}
