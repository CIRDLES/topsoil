package org.cirdles.topsoil.app.control;

import javafx.collections.ListChangeListener;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import org.cirdles.topsoil.app.model.DataRow;
import org.cirdles.topsoil.app.model.DataSegment;
import org.cirdles.topsoil.app.model.DataTable;
import org.cirdles.topsoil.app.model.TopsoilProject;
import org.cirdles.topsoil.app.model.composite.DataComposite;
import org.cirdles.topsoil.app.model.composite.DataComponent;
import org.cirdles.topsoil.app.model.composite.DataLeaf;

import java.util.*;

/**
 * @author marottajb
 */
public class ProjectTreeView extends TreeView<DataComponent> {

    //**********************************************//
    //                 CONSTRUCTORS                 //
    //**********************************************//

    public ProjectTreeView(TopsoilProject project) {
        final CheckBoxTreeItem<DataComponent> rootItem = new CheckBoxTreeItem<>(new DataComposite<>("dummy"));
        this.setCellFactory(CheckBoxTreeCell.forTreeView());
        this.setRoot(rootItem);
        this.setShowRoot(false);

        for (DataTable table : project.getDataTableList()) {
            addDataTable(table);
        }
        project.dataTableListProperty().addListener((ListChangeListener<? super DataTable>) c -> {
            c.next();
            if (c.wasAdded()) {
                for (DataTable table : c.getAddedSubList()) {
                    addDataTable(table);
                }
            }
            if (c.wasRemoved()) {
                for (DataTable table : c.getRemoved()) {
                    removeDataTable(table);
                }
            }
        });
    }

    //**********************************************//
    //                PUBLIC METHODS                //
    //**********************************************//

    private void addDataTable(DataTable table) {
        CheckBoxTreeItem<DataComponent> tableItem, segmentItem, rowItem;
        tableItem = new CheckBoxTreeItem<>(new DataComposite<>(table.getLabel()));
        tableItem.selectedProperty().bindBidirectional(table.selectedProperty());
        tableItem.setExpanded(true);
        for (DataSegment segment : table.getChildren()) {
            segmentItem = new CheckBoxTreeItem<>(new DataComposite(segment.getLabel()));
            segmentItem.selectedProperty().bindBidirectional(segment.selectedProperty());
            for (DataRow row : segment.getChildren()) {
                rowItem = new CheckBoxTreeItem<>(new DataLeaf(row.getLabel()));
                rowItem.selectedProperty().bindBidirectional(row.selectedProperty());
                segmentItem.getChildren().add(rowItem);
            }
            tableItem.getChildren().add(segmentItem);
        }
        this.getRoot().getChildren().add(tableItem);
    }

    private void removeDataTable(DataTable table) {
        for (TreeItem<DataComponent> item : getRoot().getChildren()) {
            if (item.getValue().equals(table)) {
                this.getRoot().getChildren().remove(item);
            }
        }
    }

}
