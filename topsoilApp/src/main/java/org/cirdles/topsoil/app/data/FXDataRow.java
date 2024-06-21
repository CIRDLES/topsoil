package org.cirdles.topsoil.app.data;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.cirdles.topsoil.data.DataColumn;
import org.cirdles.topsoil.data.DataRow;
import org.cirdles.topsoil.symbols.SimpleSymbolMap;

import java.util.Map;

public class FXDataRow extends FXDataComponent<DataRow> implements DataRow {

    private boolean isGroup = false;

    private ListProperty<FXDataRow> childRows = new SimpleListProperty<>(
            // Initializes the list property with an observable list that listens to changes in the properties of each
            // row
            FXCollections.observableArrayList(row -> {
                return new Observable[]{
                        row.titleProperty(),
                        row.selectedProperty(),
                        row.columnMapReadOnlyProperty(),
                        row.childRowsProperty()
                };
            })
    );
    public final ReadOnlyListProperty<FXDataRow> childRowsProperty() {
        return childRows;
    }

    public boolean getGroupProperty() {
        return isGroup;
    }

    public void setGroupProperty(boolean newProperty) {
        this.isGroup = newProperty;
    }

    @Override
    public final ObservableList<FXDataRow> getChildren() {
        return childRows.get();
    }

//    private MapProperty<DataColumn<?>, ObjectProperty<?>> columnMap = new SimpleMapProperty<>();
//    public final ReadOnlyMapProperty<DataColumn<?>, ObjectProperty<?>> columnMapProperty() {
//        return columnMap;
//    }
    private MapProperty<DataColumn<?>, Object> columnMap = new SimpleMapProperty<>(
            FXCollections.observableMap(new SimpleSymbolMap<>()));
    public final ReadOnlyMapProperty<DataColumn<?>, Object> columnMapReadOnlyProperty() {
        return columnMap;
    }
    @Override
    public final ObservableMap<DataColumn<?>, Object> getColumnValueMap() {
        return columnMap.get();
    }

    private BooleanProperty visible = new SimpleBooleanProperty(true);
    public ReadOnlyBooleanProperty visibleProperty() {
        return visible;
    }
    public final boolean isVisible() {
        return visible.get();
    }
    public final void setVisible(boolean value) {
        visible.set(value);
    }

    //**********************************************//
    //                 CONSTRUCTORS                 //
    //**********************************************//

    public FXDataRow(String title, boolean selected) {
        super(title, selected);
    }

    public FXDataRow(DataRow row) {
        this(row.getTitle(), row.isSelected());

        for (Map.Entry<? extends DataColumn<?>, Object> entry : row.getColumnValueMap().entrySet()) {
            columnMap.put(entry.getKey(), entry.getValue());
        }

        for (DataRow child : row.getChildren()) {
            childRows.add(new FXDataRow(child));
        }
    }

    //**********************************************//
    //                PUBLIC METHODS                //
    //**********************************************//

    @Override
    public <T> T getValueForColumn(DataColumn<T> column) {
        if (columnMap.containsKey(column)) {
            return column.getType().cast(columnMap.get(column));
        }
        return null;
    }

    @Override
    public <T> void setValueForColumn(DataColumn<T> column, T value) {
        columnMap.put(column, value);
    }

    void setValueForColumnUnsafe(DataColumn<?> column, Object value) {
        if (! column.match(value)) {
            throw new IllegalArgumentException("Value \"" + value + "\" must match type of column (" + column.getType() +").");
        }
        columnMap.put(column, value);
    }

}
