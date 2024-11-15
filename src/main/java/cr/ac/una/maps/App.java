package cr.ac.una.maps;

import cr.ac.una.maps.util.FlowController;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FlowController.getInstance().InitializeFlow(stage, null);
        FlowController.getInstance().goMain();
        stage.setTitle("Routify");
        stage.getIcons().add(new Image(getClass().getResource("/cr/ac/una/maps/resources/icon.jpg").toExternalForm()));

    }

    public static void main(String[] args) {

        launch(args);
    }

}