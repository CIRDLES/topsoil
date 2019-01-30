package org.cirdles.topsoil.app.view.wizards;

import javafx.scene.control.ButtonType;
import org.controlsfx.dialog.Wizard;

import java.util.Map;

/**
 * @author marottajb
 */
public class NewProjectWizard extends Wizard {

    //**********************************************//
    //                  CONSTANTS                   //
    //**********************************************//

    private static final double WIDTH = 600.0;
    private static final double HEIGHT = 550.0;

    //**********************************************//
    //                  ATTRIBUTES                  //
    //**********************************************//

    private final NewProjectTitleView titleView = new NewProjectTitleView();
    private final NewProjectSourcesView sourcesView = new NewProjectSourcesView();
    private final NewProjectPreView preView = new NewProjectPreView();

    //**********************************************//
    //                 CONSTRUCTORS                 //
    //**********************************************//

    private NewProjectWizard() {
        LinearFlow pageOrder = new Wizard.LinearFlow(
                titleView,
                sourcesView,
                preView
        );
        this.setFlow(pageOrder);
    }

    //**********************************************//
    //                PUBLIC METHODS                //
    //**********************************************//

    public static Map<String, Object> startWizard() {
        NewProjectWizard wizard = new NewProjectWizard();
        ButtonType response = wizard.showAndWait().orElse(null);
        if (response == null || response.equals(ButtonType.CANCEL)) {
            return null;
        } else {
            if (wizard.getSettings() != null) {
                return wizard.getSettings();
            }
        }
        return null;
    }

    //**********************************************//
    //                INNER CLASSES                 //
    //**********************************************//

    public class Key {
        public static final String TITLE = "TITLE";
        public static final String LOCATION = "LOCATION";
        public static final String SOURCES = "SOURCES";
    }

}
