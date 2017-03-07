package org.roda.rodain.ui.schema.ui;

import java.util.Comparator;

import javafx.scene.control.TreeItem;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 07-12-2015.
 */
public class SchemaComparator implements Comparator<TreeItem<String>> {
  /**
   * Compares two TreeItems.
   * <p/>
   * <p>
   * If the items are of the same class, compares its titles/names. Otherwise,
   * puts the SchemaNodes in the top.
   * </p>
   *
   * @param o1
   *          The first TreeItem in the comparison.
   * @param o2
   *          The second TreeItem in the comparison.
   * @return A value smaller than 0 if the o1 should appear first, 0 if they are
   *         equal and bigger than 0 if o2 should appear first.
   */
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
        s1 = n1.getSip().getTitle();
        s2 = n2.getSip().getTitle();
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
