package org.cirdles.topsoil.app.control.data;

import com.sun.javafx.scene.control.skin.TreeTableViewSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.layout.Region;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import org.cirdles.topsoil.app.control.undo.UndoAction;
import org.cirdles.topsoil.app.data.FXDataColumn;
import org.cirdles.topsoil.app.data.FXDataRow;
import org.cirdles.topsoil.app.data.FXDataTable;
import org.cirdles.topsoil.app.data.NumberColumnStringConverter;
import org.cirdles.topsoil.data.DataColumn;
import org.cirdles.topsoil.data.DataTable;
import org.cirdles.topsoil.data.TableUtils;
import org.cirdles.topsoil.javafx.SingleChildRegion;
import org.cirdles.topsoil.utils.IntuitiveStringComparator;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A customized {@code TreeTableView} that displays the data contained in a {@link DataTable}.
 *
 * @author marottajb
 */
public class FXDataTableViewer extends SingleChildRegion<TreeTableView<FXDataRow>> {

    private static final double LABEL_COL_WIDTH = 150.0;
    private static final double SELECTED_COL_WIDTH = 70.0;
    private static final double DATA_COL_WIDTH = 145.0;

    private FXDataTable table;
    private TreeTableView<FXDataRow> treeTableView;
    private Map<DataColumn<?>, StringConverter<?>> converterMap = new HashMap<>();

    //**********************************************//
    //                 CONSTRUCTORS                 //
    //**********************************************//

    /**
     * Constructs a new tree table view for the given data table.
     *
     * @param table     DataTable
     */
    public FXDataTableViewer(FXDataTable table) {
        super(new TreeTableView<>());

        this.table = table;

        treeTableView = getChild();
        treeTableView.setEditable(true);
        treeTableView.setShowRoot(false);
        treeTableView.setSkin(new CustomTreeTableViewSkin(treeTableView));

        // Create root item
        CheckBoxTreeItem<FXDataRow> rootItem = new CheckBoxTreeItem<>();

        // Add TreeItems for data
        for (FXDataRow row : table.getRows()) {
            rootItem.getChildren().add(treeItemForDataRow(row));
        }
        treeTableView.setRoot(rootItem);

        // Create converters for leaf columns
        int maxFractionDigits;
        for (DataColumn<?> column : table.getLeafColumns()) {
            if (column.getType() == Number.class) {
                NumberColumnStringConverter converter = new NumberColumnStringConverter();
                maxFractionDigits = TableUtils.maxFractionDigitsForColumn(
                        table.getRows(),
                        (DataColumn<Number>) column,
                        table.isScientificNotation()
                );
                if (table.getMaxFractionDigits() >= 0) {
                    maxFractionDigits = Math.min(maxFractionDigits, table.getMaxFractionDigits());
                }
                converter.setNumFractionDigits(maxFractionDigits);
                converterMap.put(column, converter);
            } else {
                converterMap.put(column, new DefaultStringConverter());
            }
        }

        // Add tree table columns
        treeTableView.getColumns().add(labelColumn());
        treeTableView.getColumns().add(checkBoxColumn());
        for (FXDataColumn<?> column : table.getColumns()) {
            treeTableView.getColumns().add(treeColumnForDataColumn(column));
        }

        // Refresh cells on fraction digit changes
        table.maxFractionDigitsProperty().addListener(c -> {
            updateFractionDigitsForLeafColumns();
            refreshCells();
        });
        table.scientificNotationProperty().addListener(c -> {
            updateFractionDigitsForLeafColumns();
            refreshCells();
        });

        // MULTI-SELECT
        treeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        treeTableView.setRowFactory(new Callback<TreeTableView<FXDataRow>, TreeTableRow<FXDataRow>>() {
            @Override
            public TreeTableRow<FXDataRow> call(TreeTableView<FXDataRow> param) {
                final TreeTableRow<FXDataRow> row = new TreeTableRow<>();
                final ContextMenu rowMenu = new ContextMenu();
                ContextMenu tableMenu = treeTableView.getContextMenu();

                MenuItem editItem = new MenuItem("Group");
                editItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        // todo: add popup for naming the group and specifying the plot color
                        // todo: prevent nested grouping
                        System.out.println("ActionEvent handled this group: " + treeTableView.getSelectionModel().getSelectedItems());
                        ObservableList<TreeItem<FXDataRow>> itemsForGrouping = treeTableView.getSelectionModel().getSelectedItems();
                        TreeItem<FXDataRow> firstItem = itemsForGrouping.get(0);

                        FXDataRow groupRow = new FXDataRow("group", true);
                        groupRow.setGroupProperty(true);
                        TreeItem<FXDataRow> groupItem = new TreeItem<FXDataRow>(groupRow);

                        // Copies content into new TreeItem objects instead of using groupItem.getChildren().addAll(itemsForGrouping)
                        // because the latter caused an issue with spacing
                        boolean isGroupable = true;
                        for (TreeItem<FXDataRow> item : itemsForGrouping) {
                            FXDataRow fxDataRowContent = item.getValue();

                            if (!fxDataRowContent.getGroupProperty()) {
                                groupItem.getChildren().add(new TreeItem<FXDataRow>(fxDataRowContent));
                            } else {
                                isGroupable = false;
                                break;
                            }
                        }

                        if (isGroupable) {
                            int indexOfFirstSelection = treeTableView.getSelectionModel().getSelectedIndices().get(0);
                            treeTableView.getRoot().getChildren().add(indexOfFirstSelection, groupItem);
                            treeTableView.getRoot().getChildren().removeAll(itemsForGrouping);
                        }

                        treeTableView.getSelectionModel().clearSelection();
                    }
                });

                rowMenu.getItems().addAll(editItem);
                row.contextMenuProperty().bind(
                        Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                .then(rowMenu)
                                .otherwise((ContextMenu) null));
                return row;
            }
        });
    }

    //**********************************************//
    //                PRIVATE METHODS               //
    //**********************************************//

    /**
     * Returns the column of {@code String} labels for rows/segments.
     *
     * @return  new TreeTableColumn
     */
    private TreeTableColumn<FXDataRow, String> labelColumn() {
        MultilineHeaderTreeTableColumn<FXDataRow, String> column = new MultilineHeaderTreeTableColumn<>("Label");
        column.setCellFactory(param -> {
            TextFieldTreeTableCell<FXDataRow, String> cell = new TextFieldTreeTableCell<>();
            cell.setTextAlignment(TextAlignment.LEFT);
            cell.setEditable(false);
            return cell;
        });
        column.setCellValueFactory(param -> {
            FXDataRow row = param.getValue().getValue();
            return row.titleProperty();
        });
        IntuitiveStringComparator<String> comparator = new IntuitiveStringComparator<>();
        column.setComparator(comparator);
        column.setPrefWidth(LABEL_COL_WIDTH);
        return column;
    }

    /**
     * Returns the column of {@link javafx.scene.control.CheckBox}es for row/segment selection.
     *
     * @return  new TreeTableColumn
     */
    private TreeTableColumn<FXDataRow, Boolean> checkBoxColumn() {
        MultilineHeaderTreeTableColumn<FXDataRow, Boolean> column = new MultilineHeaderTreeTableColumn<>("Included");
        column.setCellFactory(param -> {
            CheckBoxTreeTableCell<FXDataRow, Boolean> cell = new CheckBoxTreeTableCell<>();
            cell.setAlignment(Pos.CENTER);
            cell.setEditable(true);
            return cell;
        });
        column.setCellValueFactory(param -> {
            FXDataRow row = param.getValue().getValue();
            return row.selectedProperty();
        });
        column.setEditable(true);
        column.setPrefWidth(SELECTED_COL_WIDTH);
        return column;
    }

    private <T> TreeTableColumn<FXDataRow, T> treeColumnForDataColumn(FXDataColumn<T> column) {
        DataTreeTableColumn<FXDataRow, T> treeTableColumn = new DataTreeTableColumn<>(column);
        if (column.getChildren().size() == 0) {
            treeTableColumn.setCellFactory(param -> {
                TreeTableCell<FXDataRow, T> cell = new FXDataTreeTableCell<>(column, (StringConverter<T>) converterMap.get(column));
                if (column.getType() == Number.class) {
                    cell.setAlignment(Pos.CENTER_RIGHT);
                } else {
                    cell.setAlignment(Pos.CENTER_LEFT);
                }
                return cell;
            });
            treeTableColumn.setCellValueFactory(param -> {
                FXDataRow row = param.getValue().getValue();
                if (row.getChildren().size() == 0) {
                    return new SimpleObjectProperty<>(row.getValueForColumn(column));
                }
                return null;
            });
            treeTableColumn.setOnEditCommit(event -> {
                UndoAction editAction = new CellEditUndoAction<>(
                        column,
                        event
                );
                editAction.execute();
                this.table.addUndo(editAction);
            });
            treeTableColumn.visibleProperty().bindBidirectional(column.selectedProperty());
            treeTableColumn.setPrefWidth(DATA_COL_WIDTH);
        } else {
            for (FXDataColumn<?> child : column.getChildren()) {
                treeTableColumn.getColumns().add(treeColumnForDataColumn(child));
            }
        }
        return treeTableColumn;
    }

    /**
     * Returns a new {@code CheckBoxTreeItem} for the provided {@code DataRow}.
     *
     * @param row       DataRow
     * @return          CheckBoxTreeItem;
     */
    private CheckBoxTreeItem<FXDataRow> treeItemForDataRow(FXDataRow row) {
        CheckBoxTreeItem<FXDataRow> treeItem = new CheckBoxTreeItem<>(row);
        for (FXDataRow child : row.getChildren()) {
            treeItem.getChildren().add(treeItemForDataRow(child));
        }
        treeItem.selectedProperty().bindBidirectional(row.selectedProperty());
        return treeItem;
    }

    private void updateFractionDigitsForLeafColumns() {
        int tableSetting = table.getMaxFractionDigits();
        int maxFractionDigits;
        for (Map.Entry<DataColumn<?>, StringConverter<?>> entry : converterMap.entrySet()) {
            if (entry.getValue() instanceof NumberColumnStringConverter) {
                NumberColumnStringConverter converter = (NumberColumnStringConverter) entry.getValue();
                if (tableSetting > -1) {
                    maxFractionDigits = Math.min(tableSetting, TableUtils.maxFractionDigitsForColumn(
                            table.getRows(),
                            (DataColumn<Number>) entry.getKey(),
                            table.isScientificNotation()
                    ));
                } else {
                    maxFractionDigits = TableUtils.maxFractionDigitsForColumn(
                            table.getRows(),
                            (DataColumn<Number>) entry.getKey(),
                            table.isScientificNotation()
                    );
                }
                converter.setNumFractionDigits(maxFractionDigits);
                converter.setScientificNotation(table.isScientificNotation());
            }
        }
    }

    private void refreshCells() {
        ((CustomTreeTableViewSkin) treeTableView.getSkin()).refreshCells();
    }

    private class CustomTreeTableViewSkin extends TreeTableViewSkin<FXDataRow> {

        CustomTreeTableViewSkin(TreeTableView<FXDataRow> treeTableView) {
            super(treeTableView);
        }

        public void refreshCells() {
            super.flow.recreateCells();
        }

    }

}
