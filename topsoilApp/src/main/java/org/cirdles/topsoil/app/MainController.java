package org.cirdles.topsoil.app;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.topsoil.app.control.HomeView;
import org.cirdles.topsoil.app.control.ProjectView;
import org.cirdles.topsoil.app.control.menu.helpers.FileMenuHelper;
import org.cirdles.topsoil.app.util.FXMLUtils;
import org.cirdles.topsoil.app.util.file.ProjectSerializer;

import java.io.IOException;

/**
 * A controller class for Topsoil's {@link Main}.
 *
 * @author Jake Marotta
 * @see Main
 */
public class MainController extends VBox {

	//**********************************************//
	//                  CONSTANTS                   //
	//**********************************************//

	private static final String CONTROLLER_FXML = "main-window.fxml";
	private static final String TOPSOIL_LOGO = "topsoil-logo.png";

	//**********************************************//
	//                   CONTROLS                   //
	//**********************************************//

	@FXML private AnchorPane mainContentPane;
	private Image topsoilLogo;
	private HomeView homeView;

	//**********************************************//
	//                  ATTRIBUTES                  //
	//**********************************************//

	private Stage primaryStage;

	//**********************************************//
	//                  PROPERTIES                  //
	//**********************************************//

	private BooleanProperty dataShowing = new SimpleBooleanProperty(false);
	public BooleanProperty dataShowingProperty() {
		return dataShowing;
	}
	public boolean isDataShowing() {
		return (mainContentPane.getChildren().get(0) instanceof ProjectView);
	}

	//**********************************************//
	//                 CONSTRUCTORS                 //
	//**********************************************//

	MainController(Stage primaryStage) {
		this.primaryStage = primaryStage;
		// If a .topsoil file is open, the name of the file is appended to "Topsoil" at the top of the window
		this.primaryStage.titleProperty().bind(Bindings.createStringBinding(() -> {
			return ProjectSerializer.getCurrentProjectPath() != null
					? "Topsoil - " + ProjectSerializer.getCurrentProjectPath().getFileName().toString()
					: "Topsoil";
		}, ProjectSerializer.currentProjectPathProperty()));
		this.primaryStage.setOnCloseRequest(event -> {
			event.consume();
			FileMenuHelper.exitTopsoilSafely();
		});

		try {
			FXMLUtils.loadController(CONTROLLER_FXML, MainController.class, this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		topsoilLogo =
				new Image(new ResourceExtractor(MainController.class).extractResourceAsPath(TOPSOIL_LOGO).toUri().toString());
		this.primaryStage.getIcons().add(topsoilLogo);
	}

	@FXML
	protected void initialize() {
		homeView = new HomeView();
		replaceMainContent(homeView);
	}

	//**********************************************//
	//                PUBLIC METHODS                //
	//**********************************************//

	public Node getMainContent() {
		return mainContentPane.getChildren().get(0);
	}

	public Node setProjectView(ProjectView projectView) {
		return replaceMainContent(projectView);
	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	public void closeProjectView() {
		replaceMainContent(homeView);
	}

	public Image getTopsoilLogo() {
	    return topsoilLogo;
    }

	//**********************************************//
	//               PRIVATE METHODS                //
	//**********************************************//

    private Node replaceMainContent(Node content) {
    	Node rtnval = mainContentPane.getChildren().isEmpty() ? null : mainContentPane.getChildren().get(0);
    	mainContentPane.getChildren().clear();
    	mainContentPane.getChildren().add(content);
    	AnchorPane.setTopAnchor(content, 0.0);
    	AnchorPane.setRightAnchor(content, 0.0);
    	AnchorPane.setBottomAnchor(content, 0.0);
    	AnchorPane.setLeftAnchor(content, 0.0);
    	return rtnval;
	}
}
