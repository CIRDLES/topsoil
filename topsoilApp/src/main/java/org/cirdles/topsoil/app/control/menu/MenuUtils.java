package org.cirdles.topsoil.app.control.menu;

import org.cirdles.topsoil.app.Topsoil;
import org.cirdles.topsoil.app.control.ProjectTableTab;
import org.cirdles.topsoil.app.control.ProjectView;
import org.cirdles.topsoil.app.data.DataTable;
import org.cirdles.topsoil.app.file.ProjectSerializer;

/**
 * Utility methods for menu actions in {@link TopsoilMenuBar} and its associated helpers.
 *
 * @author marottajb
 *
 * @see TopsoilMenuBar
 * @see FileMenu
 * @see org.cirdles.topsoil.app.control.menu.helpers.FileMenuHelper
 * @see org.cirdles.topsoil.app.control.menu.helpers.EditMenuHelper
 * @see org.cirdles.topsoil.app.control.menu.helpers.ViewMenuHelper
 * @see org.cirdles.topsoil.app.control.menu.helpers.VisualizationsMenuHelper
 * @see org.cirdles.topsoil.app.control.menu.helpers.HelpMenuHelper
 */
public class MenuUtils {

    /**
     * Returns the currently displayed {@code ProjectView}, if there is one. Otherwise, returns null.
     *
     * @return  current ProjectView; else null
     */
    public static ProjectView getProjectView() {
        if (ProjectSerializer.getCurrentProject() != null) {
            return (ProjectView) Topsoil.getController().getMainContent();
        }
        return null;
    }

    /**
     * If there is a {@link ProjectView} open, the currently selected {@code ProjectTableTab} is returned. Otherwise,
     * null is returned.
     *
     * @return  current tab; else null
     */
    public static ProjectTableTab getSelectedTableTab() {
        if (ProjectSerializer.getCurrentProject() != null) {
            ProjectView projectView = getProjectView();
            if (projectView != null) {
                return (ProjectTableTab) projectView.getTabPane().getSelectionModel().getSelectedItem();
            }
        }
        return null;
    }

    /**
     * Returns the {@code DataTable} that is currently being displayed; otherwise, null.
     *
     * @return  current DataTable; else null
     */
    public static DataTable getCurrentTable() {
        if (ProjectSerializer.getCurrentProject() != null) {
            ProjectTableTab tab = getSelectedTableTab();
            if (tab != null) {
                return tab.getDataTable();
            }
        }
        return null;
    }

}