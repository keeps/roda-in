package rules;

import schema.ui.SchemaNode;
import source.ui.items.SourceTreeDirectory;

import java.util.logging.Logger;

/**
 * Created by adrapereira on 29-09-2015.
 */
public class Rule {
    private static final Logger log = Logger.getLogger(Rule.class.getName());
    private SourceTreeDirectory source;
    private SchemaNode schemaNode;

    public Rule(SourceTreeDirectory source, SchemaNode schemaNode){
        this.source = source;
        this.schemaNode = schemaNode;
    }

    public SourceTreeDirectory getSource() {
        return source;
    }

    public void setSource(SourceTreeDirectory source) {
        this.source = source;
    }

    public SchemaNode getSchemaNode() {
        return schemaNode;
    }

    public void setSchemaNode(SchemaNode schemaNode) {
        this.schemaNode = schemaNode;
    }

    public String getFolderName(){
        return source.getValue().toString();
    }

    public String getDescObjName(){
        return schemaNode.getValue();
    }
}
