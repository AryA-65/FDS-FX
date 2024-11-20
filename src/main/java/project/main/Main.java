package project.main;

import com.interactivemesh.jfx.importer.Importer;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import com.jfoenix.controls.JFXButton;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

//TEST code for future auto resolution/iteration settings
//public class Main extends Application {
//
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//
//    @Override
//    public void start(Stage stage) throws Exception {
//
//        SystemInfo systemInfo = new SystemInfo();
//        HardwareAbstractionLayer hardware = systemInfo.getHardware();
//
//        //CPU info-----
//        CentralProcessor cpu = hardware.getProcessor();
//        System.out.println("CPU Model: " + cpu.getProcessorIdentifier().getName());
//        System.out.println("CPU Cores: " + cpu.getPhysicalProcessorCount());
//        System.out.println("Logical CPUs: " + cpu.getLogicalProcessorCount());
//
//        //GPU info-----
//        for (GraphicsCard gpu : hardware.getGraphicsCards()) {
//            System.out.println("GPU Model: " + gpu.getName());
//            System.out.println("Total GPU VRAM: " + gpu.getVRam() / (1024 * 1024) + " MB");
//        }
//
//        //RAM info-----
//        GlobalMemory memory = hardware.getMemory();
//        long totalMemory = memory.getTotal();
//        long availableMemory = memory.getAvailable();
//        long usedMemory = totalMemory - availableMemory;
//
//        System.out.println("Total RAM: " + (totalMemory / (1024 * 1024)) + " MB");
//        System.out.println("Available RAM: " + (availableMemory / (1024 * 1024)) + " MB");
//        System.out.println("Used RAM: " + (usedMemory / (1024 * 1024)) + " MB");
//
//        stage.setScene();
//        stage.show();
//    }
//
//}
//
//public class Main extends Application {
//    private double anchorX, anchorY;
//    private double anchorAngleX = 0, anchorAngley = 0;
//    private DoubleProperty angleX = new SimpleDoubleProperty(0);
//    private DoubleProperty angleY = new SimpleDoubleProperty(0);
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//
//    @Override
//    public void start(Stage primaryStage) {
//        PerspectiveCamera camera = new PerspectiveCamera(true);
//        camera.setTranslateZ(-20);
//
//        Group model = null;
//
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ObjModel", "*.obj"));
//
//        File selectedFile = fileChooser.showOpenDialog(primaryStage);
//        if (selectedFile != null) {
//            try {
//                model = loadModel(selectedFile.toURI().toURL());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        if (model != null) {
//            model.getTransforms().add(new Rotate(90, new Point3D(0,1,0)));
//        }
//
//        Group root = new Group(model);
//
//        Scene scene = new Scene(root, 1280, 720, true, SceneAntialiasing.BALANCED);
//
//        initMouseCont(root, scene, primaryStage);
//
//        scene.setOnKeyPressed(e -> {
//            if (e.getCode() == KeyCode.UP) {
//                camera.setTranslateY(camera.getTranslateY() - ((double) 1 / 10));
//            }
//            if (e.getCode() == KeyCode.DOWN) {
//                camera.setTranslateY(camera.getTranslateY() + ((double) 1 / 10));
//            }
//            if (e.getCode() == KeyCode.LEFT) {
//                camera.setTranslateX(camera.getTranslateX() - ((double) 1 / 10));
//            }
//            if (e.getCode() == KeyCode.RIGHT) {
//                camera.setTranslateX(camera.getTranslateX() + ((double) 1 / 10));
//            }
//        });
//        scene.setCamera(camera);
//        scene.setFill(Color.LIGHTGRAY);
//
//        primaryStage.setScene(scene);
//        primaryStage.show();
//    }
//
//    public void initMouseCont(Group group, Scene scene, Stage stage) {
//        Rotate xRotate;
//        Rotate yRotate;
//        group.getTransforms().addAll(
//                xRotate = new Rotate(90, Rotate.X_AXIS),
//                yRotate = new Rotate(90, Rotate.Y_AXIS)
//        );
//
//        xRotate.angleProperty().bind(angleX);
//        yRotate.angleProperty().bind(angleY);
//
//        scene.setOnMousePressed(e -> {
//            anchorX = e.getSceneX();
//            anchorY = e.getSceneY();
//            anchorAngleX = angleX.get();
//            anchorAngley = angleY.get();
//        });
//
//        scene.setOnMouseDragged(e -> {
//            angleX.set(anchorAngleX - (anchorY - e.getSceneY()));
//            angleY.set(anchorAngley + (anchorX - e.getSceneX()));
//        });
//
//        stage.addEventHandler(ScrollEvent.SCROLL, e -> {
//            double mov = e.getDeltaY();
//            group.translateZProperty().set(group.getTranslateZ() - ((double) mov / 10));
//        });
//    }
//
//    public Group loadModel(URL url) {
//        Group modelRoot = new Group();
//
//        ObjModelImporter importer = new ObjModelImporter();
//        importer.read(url);
//
//        for (MeshView meshView : importer.getImport()) {
//            modelRoot.getChildren().add(meshView);
//        }
//
//        return modelRoot;
//    }
//
//}
//end of test code

public class Main extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    protected static final int U_FIELD = 0;
    protected static final int V_FIELD = 1;
    protected static final int S_FIELD = 2;

    private Canvas canvas;
    private GraphicsContext c;
    private final float simHeight = 1.1f;
    private float cScale;
    private float simWidth;
    private float simDepth;
    protected static int cnt = 0;
    private Fluid fluid;
    private CanvasScene cScene;
    private int framesCount;
    private long lastTime = 0;
    private LinkedList<SaveSim> savedFrames;
    private int maxSimDur = 0; //later for memory usage and available memory
    private int simDur = 0;
    private boolean isRecording = false;
    private int resolution = 100;
    private int numIter = 40;
    private long recordInitNow;
    private ImageView imgView;
    private GraphicsContext gc;
    private int numRecordFrames = 0;

    @Override
    public void start(Stage primaryStage) {
        canvas = new Canvas(WIDTH, HEIGHT);
        c = canvas.getGraphicsContext2D();
        cScale = HEIGHT / simHeight;
        simWidth = WIDTH / cScale;

        StackPane root = new StackPane();
        root.getChildren().add(canvas);

        System.out.println(recordInitNow);

        Button showStreamlineBtn = new Button("Show Streamline");
        Button showPressureBtn = new Button("Show Pressure");
        Button showVelocityBtn = new Button("Show Velocity");
        Button showSmokeBtn = new Button("Show Smoke");
        Button pauseBtn = new Button("Pause/Resume");

        Slider objRadSlider = new Slider(0, 20, 15);
        objRadSlider.setShowTickLabels(true);
        objRadSlider.setShowTickMarks(true);
        objRadSlider.setMajorTickUnit(5);

        Slider iterSlider = new Slider(0, 100, numIter);
        Slider resSlider = new Slider(0, 500, resolution);

        iterSlider.setShowTickLabels(true);
        iterSlider.setShowTickMarks(true);
        iterSlider.setMajorTickUnit(10);

        resSlider.setShowTickLabels(true);
        resSlider.setShowTickMarks(true);
        resSlider.setMajorTickUnit(10);

        Label fpsLabel = new Label("FPS: 0");
        Label simTimeLabel = new Label("Sim Time: 0ms");
        Button startSim = new Button("Start Simulation");
        Button playRec = new Button("Play Recording");
//        Button loadObs = new Button("Load Obstacle");

        playRec.setDisable(true);
        playRec.setVisible(false);

        startSim.setOnAction(e -> {
            CanvasScene.paused = true;
            showRecordPopUp();
        });

        playRec.setOnAction(e -> {
            replay();
        });

//        loadObs.setOnAction(e -> {
//            loadImg();
//        });

        HBox buttonBox = new HBox(10, showStreamlineBtn, showPressureBtn, showVelocityBtn, showSmokeBtn, pauseBtn, objRadSlider, fpsLabel);
        buttonBox.setAlignment(Pos.CENTER);
        HBox simOptBox = new HBox(10, iterSlider, resSlider, simTimeLabel, startSim, playRec);
//        HBox simOptBox = new HBox(10, iterSlider, resSlider, simTimeLabel, startSim, playRec, loadObs);
        simOptBox.setAlignment(Pos.CENTER);

        VBox topVbox = new VBox(buttonBox, simOptBox);
        topVbox.setAlignment(Pos.CENTER);

        VBox sceneBox = new VBox(topVbox, root);

        Scene scene = new Scene(sceneBox, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Fluid Simulation");
        primaryStage.show();

        System.out.println(isRecording);

        this.cScene = new CanvasScene();
        setupScene(1, resolution, numIter);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
//                long simTimeIni = System.nanoTime();
                simulate();
//                long simTimeEnd = System.nanoTime();
//                simTimeLabel.setText("Sim Time: " + ((simTimeEnd - simTimeIni)/1000000.0) + "ms");


                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                if (isRecording) {
                    savedFrames.add(new SaveSim());
                    if ((now - recordInitNow) / 1000000000 > simDur) {
                        isRecording = false;
                        System.out.println("Recording completed");
//                        setupScene(1, resolution, numIter);
//                        replay();
                        playRec.setVisible(true);
                        playRec.setDisable(false);
                    }
                }

                double fps = 1.0 / deltaTime;
                fpsLabel.setText("FPS: " + String.format("%.2f", fps));

                draw();
            }
        };
        timer.start();

        showStreamlineBtn.setOnAction(e -> {
            CanvasScene.showStreamlines = !CanvasScene.showStreamlines;
        });

        showPressureBtn.setOnAction(e -> {
            CanvasScene.showPressure = !CanvasScene.showPressure;
        });

        showVelocityBtn.setOnAction(e -> {
           CanvasScene.showVelocities = !CanvasScene.showVelocities;
        });

        showSmokeBtn.setOnAction(e -> {
           CanvasScene.showSmoke = !CanvasScene.showSmoke;
        });

        pauseBtn.setOnAction(e -> {
            CanvasScene.paused = !CanvasScene.paused;
        });

        objRadSlider.setOnMouseReleased(e -> {
            CanvasScene.obstacleRadius = (float) (objRadSlider.getValue() / 100);
        });

        resSlider.setOnMouseReleased(e -> {
            resolution = (int) resSlider.getValue();
            System.out.println(resolution + ";" + numIter);
            setupScene(1, resolution, numIter);

        });

        iterSlider.setOnMouseReleased(e -> {
            numIter = (int) iterSlider.getValue();
            System.out.println(resolution + ";" + numIter);
            setupScene(1, resolution, numIter);
        });

        canvas.setOnMousePressed(this::startDrag);
        canvas.setOnMouseDragged(this::drag);
    }

    private void showRecordPopUp() {
        //turn simulation to frame based simulation rather than time based
        Stage popUpStage = new Stage();
        popUpStage.initModality(Modality.APPLICATION_MODAL);
        popUpStage.setTitle("Record Simulation");

        //might use for max simulation duration based on available and current system ram size
//        SystemInfo systemInfo = new SystemInfo();
//        HardwareAbstractionLayer hardware = systemInfo.getHardware();
//
//        GlobalMemory memory = hardware.getMemory();
//        long availableMemory = memory.getAvailable();
//
        simDur = 60;

        Label durLabel = new Label("Duration: ");

        JFXButton infoButton = new JFXButton();
        ImageView infoImg = new ImageView(new Image(ClassLoader.getSystemResource("images/infoPlaceholderImg.png").toExternalForm()));
        infoImg.setFitHeight(12);
        infoImg.setFitWidth(12);
        infoImg.setPreserveRatio(true);

        infoButton.setGraphic(infoImg);
        infoButton.setPrefHeight(20);
        infoButton.setPrefWidth(20);

        TextField preciseDurField = new TextField();
        preciseDurField.setPrefWidth(75.0);
        preciseDurField.setPromptText(String.format("%d seconds", simDur));
        preciseDurField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                simDur = Integer.parseInt(preciseDurField.getText());
                isRecording = true;
                setupScene(1, resolution, numIter);
                CanvasScene.paused = false;
                popUpStage.close();
                savedFrames = new LinkedList<>();
                recordInitNow = System.nanoTime();
            }
        });

        Slider recordDurSlider = new Slider(10, 600, simDur);
        recordDurSlider.setPrefWidth(175.0);
        recordDurSlider.valueProperty().addListener(e -> {
            simDur = (int) recordDurSlider.getValue();
            preciseDurField.setPromptText(String.format("%2d seconds", simDur));
        });

        Button confirm = new Button("Confirm and Start Recording");
        confirm.setOnAction(e -> {
            simDur = (int) recordDurSlider.getValue();
            isRecording = true;
            setupScene(1, resolution, numIter);
            CanvasScene.paused = false;
            popUpStage.close();
            savedFrames = new LinkedList<>();
            recordInitNow = System.nanoTime();
        });

        HBox container = new HBox(5, durLabel, recordDurSlider, preciseDurField);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10));

        preciseDurField.textProperty().addListener(e -> {
            try {
                if (!preciseDurField.getText().isEmpty()) container.getChildren().remove(infoButton);
                simDur = Integer.parseInt(preciseDurField.getText());
                recordDurSlider.setValue(simDur);
            } catch (Exception err) {
                preciseDurField.setPromptText("Invalid Value \"\"");
                Tooltip.install(infoButton, new Tooltip(err.getMessage()));
                container.getChildren().add(infoButton);
//                Tooltip.install(infoButton, new Tooltip("")); //might not even use tooltip, kinda useless
            }
        });

        VBox root = new VBox(container, confirm);
        root.setAlignment(Pos.CENTER);

        Scene popUpScene = new Scene(root, 350, 100);

        popUpStage.setScene(popUpScene);
        popUpStage.show();
        popUpStage.setResizable(false);
        popUpStage.setFullScreen(false);
        popUpStage.setOnCloseRequest(e -> {CanvasScene.paused = false;});
    }

    private void replay() {
        CanvasScene.paused = true;
        AnimationTimer replayTimer = new AnimationTimer() {
            int replayIndex = 0;

            @Override
            public void handle(long now) {
                if (replayIndex < savedFrames.size()) {
                    SaveSim frame = savedFrames.get(replayIndex++);
                    System.arraycopy(frame.u, 0, fluid.u, 0, frame.u.length);
                    System.arraycopy(frame.v, 0, fluid.v, 0, frame.v.length);
                    System.arraycopy(frame.p, 0, fluid.p, 0, frame.p.length);
                    System.arraycopy(frame.m, 0, fluid.m, 0, frame.m.length);
                    draw();
                } else {
                    stop();
                }
            }
        };
        replayTimer.start();
    }

    //test code for importing and using images as objects
    //image scaling in simulation/canvas is off: fits to canvas rather than being to scale
//    private void loadImg() {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.svg"));
//
//        File selectedObs = fileChooser.showOpenDialog(null);
//        if (selectedObs != null) {
//            try {
////                String fileExtension = selectedObs.getName().substring(selectedObs.getName().lastIndexOf(".") + 1).toLowerCase();
////                BufferedImage bufferedImage = ImageIO.read(selectedObs); //not needed yet
//                Image obsImg = new Image(selectedObs.toURI().toString());
//                gc = c.getCanvas().getGraphicsContext2D();
//                imgView = new ImageView(obsImg);
//                imgView.setPreserveRatio(true);
//                setObstacle(imgView, true);
//                applyCObs(obsImg);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    //will be used shortly
//    private boolean isTransparent(BufferedImage bI) {
//        int width = bI.getWidth();
//        int height = bI.getHeight();
//
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                int aplha = (bI.getRGB(x, y) >> 24) & 0xff;
//                if (aplha < 255) return true;
//            }
//        }
//
//        return false;
//    }
//
//    private void applyCObs(Image img) {
//        PixelReader pixelReader = img.getPixelReader();
//        if (pixelReader == null) return;
//
//        int width = (int) img.getWidth();
//        int height = (int) img.getHeight();
//
//        float scaleX = (float) fluid.numX / width;
//        float scaleY = (float) fluid.numY / height;
//
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                int argb = pixelReader.getArgb(x, y);
//
//                int alpha = (argb >> 24) & 0xff;
//                int red = (argb >> 16) & 0xff;
//                int green = (argb >> 8) & 0xff;
//                int blue = (argb) & 0xff;
//
//                if (alpha > 0) {
//                    int flippedY = height - y - 1;
//
//                    int gridX = (int) (x * scaleX);
//                    int gridY = (int) (flippedY * scaleY);
//
//                    fluid.s[gridX * fluid.numY + gridY] = 0.0f;
//                }
//            }
//        }
//    }

//    private void setObstacle(ImageView obstacleImage, boolean reset) {
//        float vx = 0.0f;
//        float vy = 0.0f;
//
//        // Calculate the obstacle's current position in the simulation grid
//        float x = (float) (obstacleImage.getTranslateX() + obstacleImage.getFitWidth() / 2);
//        float y = (float) (obstacleImage.getTranslateY() + obstacleImage.getFitHeight() / 2);
//
//        if (!reset) {
//            vx = (x - CanvasScene.obstacleX) / CanvasScene.dt;
//            vy = (y - CanvasScene.obstacleY) / CanvasScene.dt;
//        }
//
//        CanvasScene.obstacleX = x;
//        CanvasScene.obstacleY = y;
//
//        // Scale obstacle dimensions to simulation grid
//        float obstacleWidth = (float) obstacleImage.getFitWidth() / simWidth * fluid.numX;
//        float obstacleHeight = (float) obstacleImage.getFitHeight() / simHeight * fluid.numY;
//
//        int gridWidth = Math.round(obstacleWidth);
//        int gridHeight = Math.round(obstacleHeight);
//
//        // Determine the top-left corner of the obstacle in the grid
//        int gridStartX = (int) (x / simWidth * fluid.numX) - gridWidth / 2;
//        int gridStartY = (int) (y / simHeight * fluid.numY) - gridHeight / 2;
//
//        // Apply obstacle shape to the simulation grid
//        for (int i = Math.max(0, gridStartX); i < Math.min(fluid.numX, gridStartX + gridWidth); i++) {
//            for (int j = Math.max(0, gridStartY); j < Math.min(fluid.numY, gridStartY + gridHeight); j++) {
//                fluid.s[i * fluid.numY + j] = 0.0f; // Mark as solid
//                fluid.u[i * fluid.numY + j] = vx;   // Set velocity
//                fluid.v[i * fluid.numY + j] = vy;
//
//                if (CanvasScene.sceneNr == 2) {
//                    fluid.m[i * fluid.numY + j] = 0.5f + 0.5f * (float) Math.sin(0.1f * CanvasScene.frameNr);
//                } else {
//                    fluid.m[i * fluid.numY + j] = 1.0f;
//                }
//            }
//        }
//
//        gc.drawImage(imgView.getImage(), gridStartX, gridStartY);
//
//        CanvasScene.showObstacle = true;
//    }

    private void setupScene(int sceneNr, int resolution, int numIters) {
        CanvasScene.sceneNr = sceneNr;
        CanvasScene.obstacleRadius = 0.15f;
        CanvasScene.overRelaxation = 1.9f;

        CanvasScene.dt = 1.0f / 60.0f;
        CanvasScene.numIters = numIters;

        if (sceneNr == 0)
            resolution = 50;
        else if (sceneNr == 3)
            resolution = 200;

        float domainHeight = 1.0f;
        float domainWidth = domainHeight / simHeight * simWidth;
        float h = domainHeight / resolution;

        int numX = (int) (domainWidth / h);
        int numY = (int) (domainHeight / h);

        float density = 1000.0f;

        fluid = new Fluid(density, numX, numY, h);

        CanvasScene.fluid = fluid;

        int n = fluid.numY;

        if (sceneNr == 0) {    // tank

            for (int i = 0; i < fluid.numX; i++) {
                for (int j = 0; j < fluid.numY; j++) {
                    float s = 1.0f;  // fluid
                    if (i == 0 || i == fluid.numX - 1 || j == 0)
                        s = 0.0f;  // solid
                    fluid.s[i * n + j] = s;
                }
            }
            CanvasScene.gravity = -9.81f;
            CanvasScene.showPressure = true;
            CanvasScene.showSmoke = false;
            CanvasScene.showStreamlines = false;
            CanvasScene.showVelocities = false;
        } else if (sceneNr == 1 || sceneNr == 3) { // vortex shedding

            float inVel = 2.0f;
            for (int i = 0; i < fluid.numX; i++) {
                for (int j = 0; j < fluid.numY; j++) {
                    float s = 1.0f;  // fluid
                    if (i == 0 || j == 0 || j == fluid.numY - 1)
                        s = 0.0f;  // solid
                    fluid.s[i * n + j] = s;

                    if (i == 1) {
                        fluid.u[i * n + j] = inVel;
                    }
                }
            }

            int pipeH = (int) (0.1f * fluid.numY);
            int minJ = (int) (0.5f * fluid.numY - 0.5f * pipeH);
            int maxJ = (int) (0.5f * fluid.numY + 0.5f * pipeH);

            for (int j = minJ; j < maxJ; j++)
                fluid.m[j] = 0.0f;

            setObstacle(0.4f, 0.5f, true);
//            if (imgView == null) {
//                loadImg();
//            }
//            setObstacle(imgView,true);

            CanvasScene.gravity = 0.0f;
            CanvasScene.showPressure = false;
            CanvasScene.showSmoke = true;
            CanvasScene.showStreamlines = false;
            CanvasScene.showVelocities = false;

            if (sceneNr == 3) {
                CanvasScene.dt = 1.0f / 120.0f;
                CanvasScene.numIters = 100;
                CanvasScene.showPressure = true;
            }

        } else if (sceneNr == 2) { // paint

            CanvasScene.gravity = 0.0f;
            CanvasScene.overRelaxation = 1.0f;
            CanvasScene.showPressure = false;
            CanvasScene.showSmoke = true;
            CanvasScene.showStreamlines = false;
            CanvasScene.showVelocities = false;
            CanvasScene.obstacleRadius = 0.1f;
        }
    }

    private void setObstacle(float x, float y, boolean reset) {
        float vx = 0.0f;
        float vy = 0.0f;

        if (!reset) {
            vx = (x - CanvasScene.obstacleX) / CanvasScene.dt;
            vy = (y - CanvasScene.obstacleY) / CanvasScene.dt;
        }

        CanvasScene.obstacleX = x;
        CanvasScene.obstacleY = y;
        float r = CanvasScene.obstacleRadius;
        int n = fluid.numY;
//        float cd = (float) (Math.sqrt(2) * fluid.h);

        for (int i = 1; i < fluid.numX - 2; i++) {
            for (int j = 1; j < fluid.numY - 2; j++) {

                fluid.s[i * n + j] = 1.0f;

                float dx = (i + 0.5f) * fluid.h - x;
                float dy = (j + 0.5f) * fluid.h - y;

                if (dx * dx + dy * dy < r * r) {
                    fluid.s[i * n + j] = 0.0f;
                    if (CanvasScene.sceneNr == 2)
                        fluid.m[i * n + j] = 0.5f + 0.5f * (float) Math.sin(0.1f * CanvasScene.frameNr);
                    else
                        fluid.m[i * n + j] = 1.0f;
                    fluid.u[i * n + j] = vx;
                    fluid.u[(i + 1) * n + j] = vx;
                    fluid.v[i * n + j] = vy;
                    fluid.v[i * n + j + 1] = vy;
                }
            }
        }

        CanvasScene.showObstacle = true;
    }

    private void simulate() {
        if (!CanvasScene.paused) {
            fluid.simulate(CanvasScene.dt, CanvasScene.gravity, CanvasScene.numIters);
            CanvasScene.frameNr++;
        }
    }

    private void draw() {
        c.clearRect(0, 0, WIDTH, HEIGHT);

        Fluid f = CanvasScene.fluid;
        int n = f.numY;

        float cellScale = 1.1f;

        float h = f.h;

        float minP = f.p[0];
        float maxP = f.p[0];

        for (int i = 0; i < f.numCells; i++) {
            minP = Math.min(minP, f.p[i]);
            maxP = Math.max(maxP, f.p[i]);
        }

        int[] color = new int[4];

        for (int i = 0; i < f.numX; i++) {
            for (int j = 0; j < f.numY; j++) {

                if (CanvasScene.showPressure) {
                    float p = f.p[i * n + j];
                    float s = f.m[i * n + j];
                    color = getSciColor(p, minP, maxP);
                    if (CanvasScene.showSmoke) {
                        color[0] = Math.max(0, color[0] - (int) (255 * s));
                        color[1] = Math.max(0, color[1] - (int) (255 * s));
                        color[2] = Math.max(0, color[2] - (int) (255 * s));
                    }
                } else if (CanvasScene.showSmoke) {
                    float s = f.m[i * n + j];
                    color[0] = (int) (255 * s);
                    color[1] = (int) (255 * s);
                    color[2] = (int) (255 * s);
                    if (CanvasScene.sceneNr == 2)
                        color = getSciColor(s, 0.0f, 1.0f);
                } else if (f.s[i * n + j] == 0.0f) {
                    color[0] = 0;
                    color[1] = 0;
                    color[2] = 0;
                }

                int x = (int) (cX(i * h));
                int y = (int) (cY((j + 1) * h));
                int cx = (int) (cScale * cellScale * h) + 1;
                int cy = (int) (cScale * cellScale * h) + 1;

                int r = color[0];
                int g = color[1];
                int b = color[2];

                c.setFill(Color.rgb(r, g, b));
                c.fillRect(x, y, cx, cy);
            }
        }

        if (CanvasScene.showVelocities) {
            c.setStroke(Color.BLACK);
            float scale = 0.02f;

            for (int i = 0; i < f.numX; i++) {
                for (int j = 0; j < f.numY; j++) {
                    float u = f.u[i * n + j];
                    float v = f.v[i * n + j];

                    int x0 = (int) cX(i * h);
                    int x1 = (int) cX(i * h + u * scale);
                    int y = (int) cY((j + 0.5f) * h);

                    c.strokeLine(x0, y, x1, y);

                    int x = (int) cX((i + 0.5f) * h);
                    int y0 = (int) cY(j * h);
                    int y1 = (int) cY(j * h + v * scale);

                    c.strokeLine(x, y0, x, y1);
                }
            }
        }

        if (CanvasScene.showStreamlines) {
            float segLen = f.h * 0.2f;
            int numSegs = 15;

            c.setStroke(Color.BLACK);

            for (int i = 1; i < f.numX - 1; i += 5) {
                for (int j = 1; j < f.numY - 1; j += 5) {
                    float x = (i + 0.5f) * f.h;
                    float y = (j + 0.5f) * f.h;

                    int lastX = (int) cX(x);
                    int lastY = (int) cY(y);

                    for (int z = 0; z < numSegs; z++) {
                        float u = f.sampleField(x, y, U_FIELD);
                        float v = f.sampleField(x, y, V_FIELD);
                        float l = (float) Math.sqrt(u * u + v * v);
                        x += u * 0.01f;
                        y += v * 0.01f;
                        if (x > f.numX * f.h)
                            break;

                        int currX = (int) cX(x);
                        int currY = (int) cY(y);
                        c.strokeLine(lastX, lastY, currX, currY);
                        lastX = currX;
                        lastY = currY;
                    }
                }
            }
        }

        if (CanvasScene.showObstacle) {
            //part of the image importation code, still useful though
            c.setImageSmoothing(true);

            float r = CanvasScene.obstacleRadius + f.h;
            c.setFill(CanvasScene.showPressure ? Color.BLACK : Color.rgb(217, 217, 217));
            c.fillOval(cX(CanvasScene.obstacleX - r), cY(CanvasScene.obstacleY + r), 2 * cScale * r, 2 * cScale * r);

            c.setStroke(Color.BLACK);
            c.setLineWidth(3.0);
            c.strokeOval(cX(CanvasScene.obstacleX - r), cY(CanvasScene.obstacleY + r), 2 * cScale * r, 2 * cScale * r);
            c.setLineWidth(1.0);
        }

        if (CanvasScene.showPressure) {
            String s = "pressure: " + String.format("%.0f", minP) + " - " + String.format("%.0f", maxP) + " N/m";
            c.setFill(Color.BLACK);
            c.setFont(javafx.scene.text.Font.font("Arial", 16));
            c.fillText(s, 10, 35);
        }
    }

    private int[] getSciColor(float val, float minVal, float maxVal) {
        val = Math.min(Math.max(val, minVal), maxVal - 0.0001f);
        float d = maxVal - minVal;
        val = d == 0.0f ? 0.5f : (val - minVal) / d;
        float m = 0.25f;
        int num = (int) Math.floor(val / m);
        float s = (val - num * m) / m;
        int r, g, b;

        switch (num) {
            case 0:
                r = 0;
                g = (int) (255 * s);
                b = 255;
                break;
            case 1:
                r = 0;
                g = 255;
                b = (int) (255 * (1 - s));
                break;
            case 2:
                r = (int) (255 * s);
                g = 255;
                b = 0;
                break;
            case 3:
                r = 255;
                g = (int) (255 * (1 - s));
                b = 0;
                break;
            default:
                r = g = b = 0;
        }

        return new int[]{r, g, b, 255};
    }

    private float cX(float x) {
        return x * cScale;
    }

    private float cY(float y) {
        return HEIGHT - y * cScale;
    }

    private void startDrag(MouseEvent e) {
        float mx = (float) e.getX() / cScale;
        float my = (float) (HEIGHT - e.getY()) / cScale;
        setObstacle(mx, my, true);

        //in testing
//        setObstacle(imgView, true);

    }

    private void drag(MouseEvent e) {
        float mx = (float) e.getX() / cScale;
        float my = (float) (HEIGHT - e.getY()) / cScale;
        setObstacle(mx, my, false);

        //in testing
//        imgView.setTranslateX(mx);
//        imgView.setTranslateY(my);
//        setObstacle(imgView, false);
    }

//    private void

    public static void main(String[] args) {
        launch(args);
    }
}

class Fluid {
    float density;
    int numX, numY, numCells;
    float h;
    float[] u, v, newU, newV, p, s, m, newM;

    public Fluid(float density, int numX, int numY, float h) {
        this.density = density;
        this.numX = numX + 2;
        this.numY = numY + 2;
        this.numCells = this.numX * this.numY;
        this.h = h;
        this.u = new float[this.numCells];
        this.v = new float[this.numCells];
        this.newU = new float[this.numCells];
        this.newV = new float[this.numCells];
        this.p = new float[this.numCells];
        this.s = new float[this.numCells];
        this.m = new float[this.numCells];
        this.newM = new float[this.numCells];
        Arrays.fill(this.m, 1.0f);
    }

    public void integrate(float dt, float gravity) {
        int n = this.numY;
        for (int i = 1; i < this.numX; i++) {
            for (int j = 1; j < this.numY - 1; j++) {
                if (this.s[i * n + j] != 0.0f && this.s[i * n + j - 1] != 0.0f)
                    this.v[i * n + j] += gravity * dt;
            }
        }
    }

    public void solveIncompressibility(int numIters, float dt) {
        int n = this.numY;
        float cp = this.density * this.h / dt;

        for (int iter = 0; iter < numIters; iter++) {
            for (int i = 1; i < this.numX - 1; i++) {
                for (int j = 1; j < this.numY - 1; j++) {
                    if (this.s[i * n + j] == 0.0f)
                        continue;

                    float s = this.s[i * n + j];
                    float sx0 = this.s[(i - 1) * n + j];
                    float sx1 = this.s[(i + 1) * n + j];
                    float sy0 = this.s[i * n + j - 1];
                    float sy1 = this.s[i * n + j + 1];
                    float sSum = sx0 + sx1 + sy0 + sy1;
                    if (sSum == 0.0f)
                        continue;

                    float div = this.u[(i + 1) * n + j] - this.u[i * n + j] +
                            this.v[i * n + j + 1] - this.v[i * n + j];

                    float p = -div / sSum;
                    p *= CanvasScene.overRelaxation;
                    this.p[i * n + j] += cp * p;

                    this.u[i * n + j] -= sx0 * p;
                    this.u[(i + 1) * n + j] += sx1 * p;
                    this.v[i * n + j] -= sy0 * p;
                    this.v[i * n + j + 1] += sy1 * p;
                }
            }
        }
    }

    public void extrapolate() {
        int n = this.numY;
        for (int i = 0; i < this.numX; i++) {
            this.u[i * n] = this.u[i * n + 1];
            this.u[i * n + this.numY - 1] = this.u[i * n + this.numY - 2];
        }
        for (int j = 0; j < this.numY; j++) {
            this.v[j] = this.v[n + j];
            this.v[(this.numX - 1) * n + j] = this.v[(this.numX - 2) * n + j];
        }
    }

    public float sampleField(float x, float y, int field) {
        int n = this.numY;
        float h = this.h;
        float h1 = 1.0f / h;
        float h2 = 0.5f * h;

        x = Math.max(Math.min(x, this.numX * h), h);
        y = Math.max(Math.min(y, this.numY * h), h);

        float dx = 0.0f;
        float dy = 0.0f;

        float[] f;

        switch (field) {
            case Main.U_FIELD:
                f = this.u;
                dy = h2;
                break;
            case Main.V_FIELD:
                f = this.v;
                dx = h2;
                break;
            case Main.S_FIELD:
                f = this.m;
                dx = h2;
                dy = h2;
                break;
            default:
                f = null;
        }

        int x0 = Math.min((int) ((x - dx) * h1), this.numX - 1);
        float tx = ((x - dx) - x0 * h) * h1;
        int x1 = Math.min(x0 + 1, this.numX - 1);

        int y0 = Math.min((int) ((y - dy) * h1), this.numY - 1);
        float ty = ((y - dy) - y0 * h) * h1;
        int y1 = Math.min(y0 + 1, this.numY - 1);

        float sx = 1.0f - tx;
        float sy = 1.0f - ty;

        float val = sx * sy * f[x0 * n + y0] +
                tx * sy * f[x1 * n + y0] +
                tx * ty * f[x1 * n + y1] +
                sx * ty * f[x0 * n + y1];

        return val;
    }

    public float avgU(int i, int j) {
        int n = this.numY;
        float u = (this.u[i * n + j - 1] + this.u[i * n + j] +
                this.u[(i + 1) * n + j - 1] + this.u[(i + 1) * n + j]) * 0.25f;
        return u;
    }

    public float avgV(int i, int j) {
        int n = this.numY;
        float v = (this.v[(i - 1) * n + j] + this.v[i * n + j] +
                this.v[(i - 1) * n + j + 1] + this.v[i * n + j + 1]) * 0.25f;
        return v;
    }

    public void advectVel(float dt) {
        System.arraycopy(this.u, 0, this.newU, 0, this.u.length);
        System.arraycopy(this.v, 0, this.newV, 0, this.v.length);

        int n = this.numY;
        float h = this.h;
        float h2 = 0.5f * h;

        for (int i = 1; i < this.numX; i++) {
            for (int j = 1; j < this.numY; j++) {
                Main.cnt++;

                // u component
                if (this.s[i * n + j] != 0.0f && this.s[(i - 1) * n + j] != 0.0f && j < this.numY - 1) {
                    float x = i * h;
                    float y = j * h + h2;
                    float u = this.u[i * n + j];
                    float v = this.avgV(i, j);
                    x = x - dt * u;
                    y = y - dt * v;
                    u = this.sampleField(x, y, Main.U_FIELD);
                    this.newU[i * n + j] = u;
                }
                // v component
                if (this.s[i * n + j] != 0.0f && this.s[i * n + j - 1] != 0.0f && i < this.numX - 1) {
                    float x = i * h + h2;
                    float y = j * h;
                    float u = this.avgU(i, j);
                    float v = this.v[i * n + j];
                    x = x - dt * u;
                    y = y - dt * v;
                    v = this.sampleField(x, y, Main.V_FIELD);
                    this.newV[i * n + j] = v;
                }
            }
        }

        System.arraycopy(this.newU, 0, this.u, 0, this.newU.length);
        System.arraycopy(this.newV, 0, this.v, 0, this.newV.length);
    }

    public void advectSmoke(float dt) {
        System.arraycopy(this.m, 0, this.newM, 0, this.m.length);

        int n = this.numY;
        float h = this.h;
        float h2 = 0.5f * h;

        for (int i = 1; i < this.numX - 1; i++) {
            for (int j = 1; j < this.numY - 1; j++) {
                if (this.s[i * n + j] != 0.0f) {
                    float u = (this.u[i * n + j] + this.u[(i + 1) * n + j]) * 0.5f;
                    float v = (this.v[i * n + j] + this.v[i * n + j + 1]) * 0.5f;
                    float x = i * h + h2 - dt * u;
                    float y = j * h + h2 - dt * v;

                    this.newM[i * n + j] = this.sampleField(x, y, Main.S_FIELD);
                }
            }
        }
        System.arraycopy(this.newM, 0, this.m, 0, this.newM.length);
    }

    public void simulate(float dt, float gravity, int numIters) {
        this.integrate(dt, gravity);

        Arrays.fill(this.p, 0.0f);
        this.solveIncompressibility(numIters, dt);

        this.extrapolate();
        this.advectVel(dt);
        this.advectSmoke(dt);
    }
}

class CanvasScene {
    public static float gravity = -9.81f;
    public static float dt = 1.0f / 120.0f;
    public static int numIters = 100;
    public static int frameNr = 0;
    public static float overRelaxation = 1.9f;
    public static float obstacleX = 0.0f;
    public static float obstacleY = 0.0f;
    public static float obstacleRadius = 0.15f;
    public static boolean paused = false;
    public static int sceneNr = 0;
    public static boolean showObstacle = false;
    public static boolean showStreamlines = false;
    public static boolean showVelocities = false;
    public static boolean showPressure = false;
    public static boolean showSmoke = true;
    public static Fluid fluid = null;
}

