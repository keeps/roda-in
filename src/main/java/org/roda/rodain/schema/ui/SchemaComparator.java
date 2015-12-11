package org.roda.rodain.schema.ui;

import java.util.Comparator;

import javafx.scene.control.TreeItem;

/**
 * Created by adrapereira on 07-12-2015.
 */
public class SchemaComparator implements Comparator<TreeItem<String>> {
  @Override
  public int compare(TreeItem o1, TreeItem o2) {
    if (o1.getClass() == o2.getClass()) { // sort items of the same class by
      // value
      String s1 = null, s2 = null;
      if (o1 instanceof SchemaNode) {
        SchemaNode n1 = (SchemaNode) o1;
        SchemaNode n2 = (SchemaNode) o2;
        s1 = n1.getDob().getTitle();
        s2 = n2.getDob().getTitle();
      } else if (o1 instanceof SipPreviewNode) {
        SipPreviewNode n1 = (SipPreviewNode) o1;
        SipPreviewNode n2 = (SipPreviewNode) o2;
        s1 = n1.getSip().getName();
        s2 = n2.getSip().getName();
      }
      if (s1 != null && s2 != null)
        return s1.compareToIgnoreCase(s2);
    }
    // schema nodes must appear first
    if (o1 instanceof SchemaNode)
      return -1;
    return 1;
  }
}
