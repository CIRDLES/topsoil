package org.cirdles.topsoil.app;

import com.sun.javafx.css.StyleManager;
import com.sun.javafx.stage.StageHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.stage.*;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.topsoil.app.style.StyleLoader;
import org.cirdles.topsoil.app.util.TopsoilException;
import org.cirdles.topsoil.app.util.dialog.TopsoilNotification;
import org.cirdles.topsoil.app.util.serialization.Serializer;
import org.cirdles.topsoil.app.menu.helpers.FileMenuHelper;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @see Application
 * @see MainWindowController
 */
public class MainWindow extends Application {

    //**********************************************//
    //                  CONSTANTS                   //
    //**********************************************//

    private static final String ARIMO_FONT = "style/font/arimo/Arimo-Regular.ttf";

    //**********************************************//
    //                  ATTRIBUTES                  //
    //**********************************************//

    static Stage primaryStage;
    private static MainWindowController controller;

    //**********************************************//
    //                PUBLIC METHODS                //
    //**********************************************//

    @Override
    public void start(Stage primary) {

        ResourceExtractor resourceExtractor = new ResourceExtractor(MainWindow.class);

        MainWindow.primaryStage = primary;
        MainWindow.controller = new MainWindowController();

        // Create main Scene
        Scene scene = new Scene(controller, 900, 700);

        // Load CSS
        try {
            Font.loadFont(resourceExtractor.extractResourceAsFile(ARIMO_FONT).toURI().toURL().toExternalForm(), 14);
        } catch (MalformedURLException e) {
            new TopsoilException("Unable to load custom font.", e).printStackTrace();
        }
        StyleLoader styleLoader = new StyleLoader();
        scene.getStylesheets().addAll(styleLoader.getStylesheets());
        StyleManager.getInstance().setUserAgentStylesheets(styleLoader.getStylesheets());
        primaryStage.setScene(scene);

        // If main window is closed, all other windows close.
        configureCloseRequest(primaryStage);

        // If a .topsoil file is open, the name of the file is appended to "Topsoil" at the top of the window
        primaryStage.titleProperty().bind(Bindings.createStringBinding(() -> {
            return Serializer.projectFileExists()
                    ? "Topsoil - " + Serializer.getCurrentProjectFile().getName()
                    : "Topsoil";
        }, Serializer.currentProjectFileProperty()));

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static MainWindowController getController() {
        return controller;
    }

    /**
     * Asks the user whether they want to save their work, typically when exiting Topsoil.
     *
     * @return true if saving, false if not, null if cancelled
     */
    public static Boolean verifyFinalSave() {
        final AtomicReference<Boolean> reference = new AtomicReference<>(null);

        TopsoilNotification.showNotification(
                TopsoilNotification.NotificationType.YES_NO,
                "Save Changes",
                "Would you like to save your work?"
        ).ifPresent(response -> {
            if (response == ButtonType.YES) {
                reference.set(true);
            } else if (response == ButtonType.NO) {
                reference.set(false);
            }
        });

        return reference.get();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static Node replaceMainContent(Node content) {
        return replaceMainContent(content);
    }

    public static void shutdown() {
        List<Stage> stages = StageHelper.getStages();
        for (int index = stages.size() - 1; index > 0; index--) {
            stages.get(index).close();
        }
        Platform.exit();
    }

    //**********************************************//
    //               PRIVATE METHODS                //
    //**********************************************//

    private static void configureCloseRequest(Stage stage) {
        stage.setOnCloseRequest(event -> {
            event.consume();
            FileMenuHelper.exitTopsoilSafely();
        });
    }

}
