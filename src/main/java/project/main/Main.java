package project.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Main extends Application {

    protected static final byte U_FIELD = 0;
    protected static final byte V_FIELD = 1;
    protected static final byte S_FIELD = 2;

    LinkedList<Obstacle> obstacles;

    static Stage root;

    static Controller controller;

    static final Set<KeyCode> keyPresses = new HashSet<>();

    @Override
    public void start(Stage stage) throws IOException {
        obstacles = new LinkedList<>();

        FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemResource("FDS-UI.fxml"));
        Parent test = loader.load();
//        scene.setOnKeyPressed(e -> {
//            keyPresses.add(e.getCode());
//            checkKeysPressed();
//        });

        root = stage;

        controller = loader.getController();
        controller.setStage(stage);

        Scene scene = new Scene(test);

//        scene.getStylesheets().add(ClassLoader.getSystemResource("styles.css").toExternalForm());

        stage.setScene(scene);
        stage.setResizable(false);
        stage.setMaximized(false);
        stage.setTitle("Fluid Simulation");
        stage.getIcons().add(new Image("/app_ico.png"));
        stage.show();



//        Label fpsLabel = new Label("FPS: 0");
//        Label simTimeLabel = new Label("Sim Time: 0ms");
//        Button startRec = new Button("Start Simulation");
//        Button playRec = new Button("Play Recording");
//        Button loadObs = new Button("Load Obstacle");
//        Button startSim = new Button("Start Simulation");
//
//        startRec.setDisable(true);
//        startSim.setDisable(true);
//
//        startRec.setOnAction(e -> {
//            if (obstacles.isEmpty() || obstacles.get(1).getObstacleType() == Obstacle.Type.CONSTRAINTS) {
//                Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                alert.setTitle("Error");
//                alert.setHeaderText("Cannot proceed with recording");
//                alert.setContentText("No Obstacles in Sim");
//                alert.showAndWait();
//                return;
//            }
//            recording = new Recording();
//            for (Obstacle obstacle : obstacles) {
//                recording.addObstacle(obstacle);
//            }
//            System.out.println("recording");
//        });
//
//        startSim.setOnAction(e -> {
//            Engine.startSimulation();
//        });
//
//        playRec.setDisable(true);
//
//        playRec.setOnAction(e -> Recording.replayRecording());
//
//        loadObs.setOnAction(e -> {
//            startRec.setDisable(false);
//            startSim.setDisable(false);
//            loadImg();
//            Engine.setObstacle((float) (.5f - (testIMG.getWidth() / (2 * Engine.getWidth()))), (float) (.5f + (testIMG.getHeight() / (2 * Engine.getHeight()))), true);
//        });

//        Engine.setupScene(1, Engine.resolution, Engine.numIters);

//        CanvasSim.returnCanvas().setOnMousePressed(this::startDrag);
//        CanvasSim.returnCanvas().setOnMouseDragged(this::drag);
    }

    public void checkKeysPressed() {
        if (keyPresses.contains(KeyCode.CONTROL) && keyPresses.contains(KeyCode.ALT) && keyPresses.contains(KeyCode.ENTER)) {
            Engine.showPressure = !Engine.showPressure;
        }
    }

    private void openSim(String fileName) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            Object readObject = ois.readObject();

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void startDrag(MouseEvent e) {
        if (e.isPrimaryButtonDown()) {
            float mx = (float) e.getX() / Engine.cScale;
            float my = (float) (Engine.getHeight() - e.getY()) / Engine.cScale;
            Engine.setObstacle(mx, my, true);
        }
    }

    public static void drag(MouseEvent e) {
        if (e.isPrimaryButtonDown()) {
            float mx = (float) e.getX() / Engine.cScale;
            float my = (float) (Engine.getHeight() - e.getY()) / Engine.cScale;
            Engine.setObstacle(mx, my, false);
        }
    }

    public static Stage getRoot() {
        return root;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
