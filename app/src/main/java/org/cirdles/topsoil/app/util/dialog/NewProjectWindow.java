package org.cirdles.topsoil.app.util.dialog;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.topsoil.app.MainWindow;
import org.cirdles.topsoil.app.dataset.entry.TopsoilDataEntry;
import org.cirdles.topsoil.app.isotope.IsotopeType;
import org.cirdles.topsoil.app.menu.MenuItemEventHandler;
import org.cirdles.topsoil.app.tab.TopsoilTabPane;
import org.cirdles.topsoil.app.table.TopsoilDataTable;
import org.cirdles.topsoil.app.table.uncertainty.UncertaintyFormat;
import org.cirdles.topsoil.app.util.dialog.controller.ProjectPreviewController;
import org.cirdles.topsoil.app.util.dialog.controller.ProjectSourcesController;
import org.cirdles.topsoil.app.util.dialog.controller.ProjectTitleController;
import org.cirdles.topsoil.app.util.serialization.TopsoilSerializer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A custom {@code Stage} that handles the creation of a new project file.
 *
 * @author Jake Marotta
 */
public class NewProjectWindow extends Stage {

    private static final String PROJECT_TITLE_CONTROLLER_FXML = "controller/project-title.fxml";
    private static final String PROJECT_SOURCES_CONTROLLER_FXML = "controller/project-sources.fxml";
    private static final String PROJECT_PREVIEW_CONTROLLER_FXML = "controller/project-preview.fxml";

    private final ResourceExtractor RESOURCE_EXTRACTOR = new ResourceExtractor(NewProjectWindow.class);

    private final double width = 600.0;
    private final double height = 550.0;

    private NewProjectWindow() {
        super();

        this.setTitle("New Project");
        this.getIcons().add(MainWindow.getWindowIcon());
        this.initOwner(MainWindow.getPrimaryStage());
        this.setResizable(false);
    }

    /**
     * Creates a new project and adds its source data to the TopsoilTabPane, if such data is specified.
     *
     * @param tabs  the current TopsoilTabPane
     */
    public static void newProject(TopsoilTabPane tabs) {
        new NewProjectWindow().createNewProject(tabs);
    }

    private void createNewProject(TopsoilTabPane tabs) {

        try {
            // Extract FXML files
            FXMLLoader titleControllerLoader = new FXMLLoader(RESOURCE_EXTRACTOR.extractResourceAsPath
                    (PROJECT_TITLE_CONTROLLER_FXML).toUri().toURL());
            Scene projectTitleScene = new Scene(titleControllerLoader.load(), width, height);
            ProjectTitleController titleController = titleControllerLoader.getController();

            FXMLLoader sourcesControllerLoader = new FXMLLoader(RESOURCE_EXTRACTOR.extractResourceAsPath
                    (PROJECT_SOURCES_CONTROLLER_FXML).toUri().toURL());
            Scene projectSourcesScene = new Scene(sourcesControllerLoader.load(), width, height);
            ProjectSourcesController sourcesController = sourcesControllerLoader.getController();

            FXMLLoader previewControllerLoader = new FXMLLoader(RESOURCE_EXTRACTOR.extractResourceAsPath
                    (PROJECT_PREVIEW_CONTROLLER_FXML).toUri().toURL());
            Scene projectPreviewScene = new Scene(previewControllerLoader.load(), width, height);
            ProjectPreviewController previewController = previewControllerLoader.getController();

            // Set order of scenes
            titleController.setNextScene(projectSourcesScene);

            sourcesController.setPreviousScene(projectTitleScene);
            sourcesController.setNextScene(projectPreviewScene);

            previewController.setPreviousScene(projectSourcesScene);

            // Bind list of Paths in preview screen to list of Paths in source selection screen
            previewController.pathDelimiterListProperty().bind(sourcesController.pathDelimiterListProperty());

            // Set stage for project name controller
            this.setScene(projectTitleScene);

            // Wait for the window to close
            this.showAndWait();

            // New project from existing sources.
            if (previewController.didFinish()) {

                List<Map<DataImportKey, Object>> allSelections = previewController.getSelections();

                String[] headers;
                IsotopeType isotopeType;
                UncertaintyFormat format;
                List<TopsoilDataEntry> entries;

                // Creates a new TopsoilDataTable for each file.
                for (Map<DataImportKey, Object> selections : allSelections) {

                    headers = (String[]) selections.get(DataImportKey.HEADERS);
                    isotopeType = (IsotopeType) selections.get(DataImportKey.ISOTOPE_TYPE);
                    format = (UncertaintyFormat) selections.get(DataImportKey.UNCERTAINTY);
                    entries = (List<TopsoilDataEntry>) selections.get(DataImportKey.DATA);

                    ObservableList<TopsoilDataEntry> data = FXCollections.observableArrayList(entries);
                    MenuItemEventHandler.applyUncertaintyFormat(format, data);

                    TopsoilDataTable table = new TopsoilDataTable(
                            headers,
                            isotopeType,
                            format,
                            data.toArray(new TopsoilDataEntry[data.size()])
                    );

                    table.setTitle((String) selections.get(DataImportKey.TITLE));
                    tabs.add(table);
                }

                File projectFile = new File(titleController.getProjectLocation().toString() + "\\" +
                                            titleController.getTitle() + ".topsoil");

                // Saves and sets the current project
                TopsoilSerializer.serialize(projectFile, tabs);
                TopsoilSerializer.setCurrentProjectFile(projectFile);

            // New empty project
            } else if (sourcesController.didFinish()) {

                // Adds an empty table to the project to show the user that something happened
                tabs.add(new TopsoilDataTable(
                        IsotopeType.Generic.getHeaders(),
                        IsotopeType.Generic,
                        UncertaintyFormat.TWO_SIGMA_ABSOLUTE,
                        new TopsoilDataEntry[]{}
                ));

                File projectFile = new File(titleController.getProjectLocation().toString() + "\\" +
                                            titleController.getTitle() + ".topsoil");

                // Saves and sets the current project
                TopsoilSerializer.serialize(projectFile, tabs);
                TopsoilSerializer.setCurrentProjectFile(projectFile);

                // Notification to make sure that the user knows that the new table is not itself a "project".
                TopsoilNotification.showNotification(
                        TopsoilNotification.NotificationType.INFORMATION,
                        "Empty Table Created",
                        "An empty table was placed in your project."
                );
            }

            // If neither the sources or preview controllers finished the process, it is assumed that the user
            // cancelled the new project creation.

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
