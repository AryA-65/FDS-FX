package project.main;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Main extends Application {

    protected static final byte U_FIELD = 0;
    protected static final byte V_FIELD = 1;
    protected static final byte S_FIELD = 2;

    static Image testIMG;
    private Recording recording;
    LinkedList<Obstacle> obstacles;
    static Obstacle obstacle;

    static Stage root;
    CanvasSim canvasSim;

    static int HEIGHT = 600, WIDTH = 800;
    
    static Engine simEngine = new Engine(WIDTH, HEIGHT);

    static final Set<KeyCode> keyPresses = new HashSet<>();

    @Override
    public void start(Stage primaryStage) {
        root = primaryStage;
        obstacles = new LinkedList<>();

//        Engine.cScale = simEngine.getHeight() / Engine.simHeight;
//        Engine.simWidth = simEngine.getWidth() / Engine.cScale;

        canvasSim = new CanvasSim(WIDTH, HEIGHT);

        StackPane root = new StackPane();
        root.getChildren().add(CanvasSim.returnCanvas());

        Button loadRecordingBtn = new Button("Load Recording");

        Label fpsLabel = new Label("FPS: 0");
        Label simTimeLabel = new Label("Sim Time: 0ms");
        Button startRec = new Button("Start Simulation");
        Button playRec = new Button("Play Recording");
        Button loadObs = new Button("Load Obstacle");
        Button startSim = new Button("Start Simulation");

        startRec.setDisable(true);
        startSim.setDisable(true);

        startRec.setOnAction(e -> {
            if (obstacles.isEmpty() || obstacles.get(1).getObstacleType() == Obstacle.Type.CONSTRAINTS) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("Cannot proceed with recording");
                alert.setContentText("No Obstacles in Sim");
                alert.showAndWait();
                return;
            }
            recording = new Recording();
            for (Obstacle obstacle : obstacles) {
                recording.addObstacle(obstacle);
            }
            System.out.println("recording");
        });

        startSim.setOnAction(e -> {
            Engine.startSimulation();
        });

        playRec.setDisable(true);

        playRec.setOnAction(e -> Recording.replayRecording());

        loadObs.setOnAction(e -> {
            startRec.setDisable(false);
            startSim.setDisable(false);
            loadImg();
            Engine.setObstacle((float) (.5f - (testIMG.getWidth() / (2 * Engine.getWidth()))), (float) (.5f + (testIMG.getHeight() / (2 * Engine.getHeight()))), true);
        });

        HBox simOptBox = new HBox(10, simTimeLabel, startRec, playRec, loadObs, loadRecordingBtn, startSim);
        simOptBox.setAlignment(Pos.CENTER);

        VBox sceneBox = new VBox(simOptBox, root);

        Scene scene = new Scene(sceneBox);

        scene.setOnKeyPressed(e -> {
            keyPresses.add(e.getCode());
            checkKeysPressed();
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Fluid Simulation");
        primaryStage.show();

//        Engine.setupScene(1, Engine.resolution, Engine.numIters);

        loadRecordingBtn.setOnAction(e -> {
            FileLoader fileLoader = new FileLoader(FileLoader.Type.RECORDING);

            File file = fileLoader.returnFile();

            if (file != null) {
                try {
                    openSim(file.getAbsolutePath());
                    Recording.replayRecording();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        CanvasSim.returnCanvas().setOnMousePressed(this::startDrag);
        CanvasSim.returnCanvas().setOnMouseDragged(this::drag);

        primaryStage.getIcons().add(new Image("/app_ico.png"));
    }

    public void checkKeysPressed() {
        if (keyPresses.contains(KeyCode.CONTROL) && keyPresses.contains(KeyCode.ALT) && keyPresses.contains(KeyCode.ENTER)) {
            Engine.showPressure = !Engine.showPressure;
        }
    }

    public static Stage getRoot() {
        return root;
    }

    private void openSim(String fileName) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            Object readObject = ois.readObject();

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadImg() {
        FileLoader fl = new FileLoader(FileLoader.Type.OBSTACLE);

        if (fl.returnFile() == null) loadImg();
        testIMG = new Image(fl.returnFile().toURI().toString());

        float scale = .75f;

        if (testIMG.getHeight() >= Engine.getHeight() * scale) {
            testIMG = new Image(fl.returnFile().toURI().toString(), testIMG.getWidth() * 0.5f, testIMG.getHeight() * .5f, false, true);
        }

        if (testIMG.getWidth() >= Engine.getWidth() * scale) {
            testIMG = new Image(fl.returnFile().toURI().toString(), testIMG.getWidth() * .5f, testIMG.getHeight() * .5f, false, true);
        }

        System.out.println(testIMG.getUrl());

        System.out.println(testIMG.getHeight());

        obstacle = new Obstacle((float) (.5f - (testIMG.getWidth() / (2 * Engine.getWidth()))), (float) (.5f + (testIMG.getHeight() / (2 * Engine.getHeight()))), testIMG);
//        obstacle = new Obstacle(300, 400, testIMG);

        Engine.showObstacle = true;

        System.out.println(obstacle.image);
        System.out.println(obstacle.image.getHeight());

        Engine.obstacle = obstacle;
        obstacles.add(obstacle);

        Engine.setupScene(1, Engine.resolution, Engine.numIters);

        Engine.setObstacle((float) (.5f - (testIMG.getWidth() / (2 * Engine.getWidth()))), (float) (.5f + (testIMG.getHeight() / (2 * Engine.getHeight()))), true);
    }

    private void startDrag(MouseEvent e) {
        if (e.isPrimaryButtonDown()) {
            float mx = (float) e.getX() / Engine.cScale;
            float my = (float) (Engine.getHeight() - e.getY()) / Engine.cScale;
            Engine.setObstacle(mx, my, true);
        }
    }

    private void drag(MouseEvent e) {
        if (e.isPrimaryButtonDown()) {
            float mx = (float) e.getX() / Engine.cScale;
            float my = (float) (Engine.getHeight() - e.getY()) / Engine.cScale;
            Engine.setObstacle(mx, my, false);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
