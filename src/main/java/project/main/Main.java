package project.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.*;

public class Main extends Application {
    static Stage root;
    static Scene scene;
    static Controller controller;

//    static final Set<KeyCode> keyPresses = new HashSet<>();

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemResource("FDS-UI.fxml"));
        Parent test = loader.load();

        root = stage;

        controller = loader.getController();
        controller.setStage(stage);

        scene = new Scene(test);

//        scene.setOnKeyPressed(e -> {
//            keyPresses.add(e.getCode());
//            checkKeysPressed();
//        });

        scene.getStylesheets().add(new File(controller.getSettingsManager().getSettings().get("Style")).toURI().toString());

        stage.setScene(scene);

        stage.setHeight(Double.parseDouble(controller.getSettingsManager().getSettings().get("Height")));
        stage.setWidth(Double.parseDouble(controller.getSettingsManager().getSettings().get("Width")));

        stage.setResizable(false);
        stage.setMaximized(false);
        stage.setTitle("Fluid Simulation");
        stage.getIcons().add(new Image("/app_ico.png"));
        stage.show();
    }

//    public void checkKeysPressed() {
//        if (keyPresses.contains(KeyCode.CONTROL) && keyPresses.contains(KeyCode.SHIFT)) {
//            if (keyPresses.contains(KeyCode.S)) {//show smoke
////            controller.
//            }
//            if (keyPresses.contains(KeyCode.A)) {//show pressure
//
//            }
//        }
//    }

    public static Stage getRoot() {
        return root;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
