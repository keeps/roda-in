package org.roda.rodain.ui.creation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Pair;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SingleSelectionModel;
import javafx.util.Callback;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class SIPNameStrategyComboBox extends ComboBox<Pair> {
  // these items are Pair.(Constants.SipNameStrategy)key objects
  private final Set<Object> enabledItems = new HashSet<>();
  private ListView<Pair> internalListView;

  public SIPNameStrategyComboBox(Set<Constants.SipNameStrategy> allowedStrategies) {
    super();
    enabledItems.addAll(allowedStrategies);
    setup();
  }

  private SIPNameStrategyComboBox() {
    // do not use
  }

  private void setup() {
    // A selection model that only allows enabled items to be selected
    setSelectionModel(new SingleSelectionModel<Pair>() {
      @Override
      public void select(Pair item) {
        if (item == null || enabledItems.contains(item.getKey())) {
          super.select(item);
        }
      }

      @Override
      public void select(int index) {
        Pair item = getModelItem(index);
        if (item == null || enabledItems.contains(item.getKey())) {
          super.select(index);
        }
      }

      @Override
      protected int getItemCount() {
        return getItems().size();
      }

      @Override
      protected Pair getModelItem(int index) {
        return getItems().get(index);
      }

      @Override
      public void selectFirst() {
        for (int i = 0; i < getItemCount(); i++) {
          Pair item = getModelItem(i);
          if (item == null || enabledItems.contains(item.getKey())) {
            select(i);
            break;
          }
        }
      }

      @Override
      public void selectLast() {
        for (int i = getItemCount() - 1; i >= 0; i--) {
          Pair item = getModelItem(i);
          if (item == null || enabledItems.contains(item.getKey())) {
            select(i);
            break;
          }
        }
      }

      @Override
      public void selectPrevious() {
        for (int i = getSelectedIndex() - 1; i >= 0; i--) {
          Pair item = getModelItem(i);
          if (item == null || enabledItems.contains(item.getKey())) {
            select(i);
            break;
          }
        }
      }

      @Override
      public void selectNext() {
        for (int i = getSelectedIndex() + 1; i < getItemCount(); i++) {
          Pair item = getModelItem(i);
          if (item == null || enabledItems.contains(item.getKey())) {
            select(i);
            break;
          }
        }
      }
    });

    // a cell factory that sets items as disabled if they are not in the enabled
    // items set
    setCellFactory(new Callback<ListView<Pair>, ListCell<Pair>>() {
      @Override
      public ListCell<Pair> call(ListView<Pair> param) {
        internalListView = param;

        return new ListCell<Pair>() {
          @Override
          public void updateItem(Pair item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
              setText(item.toString());
              this.setDisable(!enabledItems.contains(item.getKey()));
            } else {
              setText(null);
            }
          }
        };
      }

    });
  }

  public void addEnabledItems(Collection<Constants.SipNameStrategy> sipNameStrategies) {
    enabledItems.addAll(sipNameStrategies);
    reRenderComboBoxItems();
  }

  public void setEnabledItems(Collection<Constants.SipNameStrategy> sipNameStrategies) {
    enabledItems.clear();
    addEnabledItems(sipNameStrategies);
  }

  /**
   * Render the combo box again to update the display of enabled/disabled items
   */
  private void reRenderComboBoxItems() {
    if (internalListView != null) {
      internalListView.refresh();
    }
    getSelectionModel().selectFirst();
  }
}
