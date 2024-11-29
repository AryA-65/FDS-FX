package project.main;

import com.jfoenix.controls.JFXButton;

import javafx.animation.AnimationTimer;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
//end of test code

public class Main extends Application {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    protected static final byte U_FIELD = 0;
    protected static final byte V_FIELD = 1;
    protected static final byte S_FIELD = 2;

    private Canvas canvas;
    private GraphicsContext c;
    private final float simHeight = 1.0f;
    private float cScale;
    private float simWidth;
    protected static int cnt = 0;
    private Fluid fluid;
    private int framesCount;
    private long lastTime = 0;
    private LinkedList<SaveSim> savedFrames;
    private int simDur = 0;
    private boolean isRecording = false;
    private int resolution = 100;
    private int numIter = 40;
    private Image testIMG;
    private double aspectRatio;
    private Thread thread;
    private final Object lock = new Object();
    private volatile boolean inSync = false;
    private float objAngle = 0;
    private float groundYPos;

    @Override
    public void start(Stage primaryStage) {
        canvas = new Canvas(WIDTH, HEIGHT);
        c = canvas.getGraphicsContext2D();
        c.setImageSmoothing(true);
        cScale = HEIGHT / simHeight;
        simWidth = WIDTH / cScale;

        StackPane root = new StackPane();
        root.getChildren().add(canvas);

        Button showStreamlineBtn = new Button("Show Streamline");
        Button showPressureBtn = new Button("Show Pressure");
        Button showVelocityBtn = new Button("Show Velocity");
        Button showSmokeBtn = new Button("Show Smoke");
        Button pauseBtn = new Button("Pause/Resume");
        Button overRelax = new Button("Overrelax");
        Button tempBtn = new Button("Temperature");

        Button loadRecordingBtn = new Button("Load Recording");
        Button groundBtn = new Button("Toggle Ground");

        Slider iterSlider = new Slider(0, 100, numIter);
        Slider resSlider = new Slider(0, 500, resolution);
        Slider objAngleSlider = new Slider(0, 360, objAngle);

        iterSlider.setShowTickLabels(true);
        iterSlider.setShowTickMarks(true);
        iterSlider.setMajorTickUnit(10);

        resSlider.setShowTickLabels(true);
        resSlider.setShowTickMarks(true);
        resSlider.setMajorTickUnit(10);

        objAngleSlider.setShowTickLabels(true);
        objAngleSlider.setShowTickMarks(true);
        objAngleSlider.setMajorTickUnit(10);

        Label fpsLabel = new Label("FPS: 0");
        Label simTimeLabel = new Label("Sim Time: 0ms");
        Button startSim = new Button("Start Simulation");
        Button playRec = new Button("Play Recording");
        Button loadObs = new Button("Load Obstacle");

        playRec.setDisable(true);
        playRec.setVisible(false);

        startSim.setOnAction(e -> {
            CanvasScene.paused = true;
            showRecordPopUp();
        });

        playRec.setOnAction(e -> replay());

        loadObs.setOnAction(e -> {
            loadImg();
            setImgObstacle((float) (.5f - (testIMG.getWidth() / (2 * WIDTH))), (float) (.5f + (testIMG.getHeight() / (2 * HEIGHT))), true);
        });

        HBox buttonBox = new HBox(10, showStreamlineBtn, showPressureBtn, showVelocityBtn, showSmokeBtn, overRelax, pauseBtn, tempBtn);
        buttonBox.setAlignment(Pos.CENTER);
        HBox midBox = new HBox(objAngleSlider, groundBtn, fpsLabel, loadRecordingBtn);
        midBox.setAlignment(Pos.CENTER);
        HBox simOptBox = new HBox(10, iterSlider, resSlider, simTimeLabel, startSim, playRec, loadObs);
        simOptBox.setAlignment(Pos.CENTER);

        VBox topVbox = new VBox(buttonBox, midBox, simOptBox);
        topVbox.setAlignment(Pos.CENTER);

        VBox sceneBox = new VBox(topVbox, root);

        Scene scene = new Scene(sceneBox);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Fluid Simulation");
        primaryStage.show();

//        loadImg();
        setupScene(1, resolution, numIter);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                simulate();
                long simTimeEnd = System.nanoTime();
                simTimeLabel.setText(String.format("Sim Time: %.2fms", ((simTimeEnd - now)/1000000.0)));


                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                if (isRecording) {
                    framesCount++;
                    savedFrames.add(new SaveSim());
                    if (framesCount >= simDur) {
                        isRecording = false;
                        playRec.setVisible(true);
                        playRec.setDisable(false);
                        try {
                            saveSimToFile("C:\\Users\\The Workstation\\Documents\\recording.bin");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        framesCount = 0;
                    }
                }

                double fps = 1.0 / deltaTime;
                fpsLabel.setText("FPS: " + String.format("%.2f", fps));

                if (primaryStage.isShowing()) {
                    draw();
                }
            }
        };
        timer.start();

        showStreamlineBtn.setOnAction(e -> CanvasScene.showStreamlines = !CanvasScene.showStreamlines);

        showPressureBtn.setOnAction(e -> CanvasScene.showPressure = !CanvasScene.showPressure);

        showVelocityBtn.setOnAction(e -> CanvasScene.showVelocities = !CanvasScene.showVelocities);

        showSmokeBtn.setOnAction(e -> CanvasScene.showSmoke = !CanvasScene.showSmoke);

        overRelax.setOnAction(e -> CanvasScene.overRelaxation = CanvasScene.overRelaxation == 1.0f ? 1.9f : 1.0f);

        pauseBtn.setOnAction(e -> CanvasScene.paused = !CanvasScene.paused);

        tempBtn.setOnAction(e -> CanvasScene.showTemperature = !CanvasScene.showTemperature);

        groundBtn.setOnAction(e -> {});

        resSlider.setOnMouseReleased(e -> {
            resolution = (int) resSlider.getValue();
            setupScene(1, resolution, numIter);
        });

        iterSlider.setOnMouseReleased(e -> {
            numIter = (int) iterSlider.getValue();
            setupScene(1, resolution, numIter);
        });

        objAngleSlider.setOnMouseReleased(e -> {
            objAngle = (float) objAngleSlider.getValue();
            System.out.println("objAngle: " + objAngle);
        });

        loadRecordingBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Recording File");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Recording File", "*.bin"));

            File file = fileChooser.showOpenDialog(null);

            if (file != null) {
                try {
                    openSim(file.getAbsolutePath());
                    replay();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        canvas.setOnMousePressed(this::startDrag);
        canvas.setOnMouseDragged(this::drag);

        if (CanvasScene.ground) setGround(.1f);

//        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
//            CanvasScene.paused = !CanvasScene.paused;
//            WIDTH = newVal.intValue();
//
//            canvas = new Canvas(WIDTH, HEIGHT);
//            cScale = HEIGHT / simHeight;
//            simWidth = WIDTH / cScale;
//            root.getChildren().add(canvas);
//
//            setupScene(1, resolution, numIter);
//            CanvasScene.paused = !CanvasScene.paused;
//        });
//
//        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> {
//            CanvasScene.paused = !CanvasScene.paused;
//            HEIGHT = (int) (newVal.intValue() - 135.5);
//            SCREEN_HEIGHT = newVal.intValue();
//
//            canvas = new Canvas(WIDTH, HEIGHT);
//            cScale = HEIGHT / simHeight;
//            simWidth = WIDTH / cScale;
//            root.getChildren().add(canvas);
//
//            setupScene(1, resolution, numIter);
//            CanvasScene.paused = !CanvasScene.paused;
//        });
    }

    private void showRecordPopUp() {
        Stage popUpStage = new Stage();
        popUpStage.initModality(Modality.APPLICATION_MODAL);
        popUpStage.setTitle("Recording Settings");

        long[] memUsage = memInfo();

        float domainHeight = 1.0f;
        float domainWidth = domainHeight / simHeight * simWidth;
        float h = domainHeight / resolution;

        int numX = (int) (domainWidth / h);
        int numY = (int) (domainHeight / h);

        int maxSimDur = (int) ((memUsage[1] * 0.5) / (4 * 4 * (numY * numX * 1.05)));

        simDur = 1024 > maxSimDur ? (int) (maxSimDur * .5) : 1024;

        Label durLabel = new Label("Duration: ");

        if (savedFrames != null && !savedFrames.isEmpty()) savedFrames.clear();

        TextField preciseDurField = new TextField();
        preciseDurField.setPrefWidth(75.0);
        preciseDurField.setPromptText(String.format("%d frames", simDur));
        preciseDurField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                simDur = Math.min(Integer.parseInt(preciseDurField.getText()), maxSimDur);
                isRecording = true;
                setupScene(1, resolution, numIter);
                CanvasScene.paused = false;
                popUpStage.close();
                savedFrames = new LinkedList<>();
            }
        });

        Slider recordDurSlider = new Slider(10, maxSimDur, simDur);
        recordDurSlider.setPrefWidth(175.0);
        recordDurSlider.valueProperty().addListener(e -> {
            simDur = (int) recordDurSlider.getValue();
            preciseDurField.setPromptText(String.format("%2d frames", simDur));
        });

        BorderPane bP = new BorderPane(recordDurSlider);
        bP.setLeft(new Label("0"));
        bP.setRight(new Label(String.valueOf(maxSimDur)));
        BorderPane.setAlignment(bP.getRight(), Pos.CENTER);
        BorderPane.setAlignment(bP.getLeft(), Pos.CENTER);
        bP.setPadding(new Insets(10));

        Button confirm = new Button("Confirm");
        confirm.setOnAction(e -> {
            simDur = (int) recordDurSlider.getValue();
            isRecording = true;
            setupScene(1, resolution, numIter);
            CanvasScene.paused = false;
            popUpStage.close();
            savedFrames = new LinkedList<>();
        });

        HBox container = new HBox(5, durLabel, bP, preciseDurField);
        container.setAlignment(Pos.CENTER);

        VBox root = new VBox(5, container, confirm);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        Scene popUpScene = new Scene(root);

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

    private long[] memInfo() {
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        GlobalMemory memory = hardware.getMemory();
        return new long[]{memory.getTotal(), memory.getAvailable(), (memory.getTotal() - memory.getAvailable())};
    }

    private void setupScene(int sceneNr, int resolution, int numIters) {
        CanvasScene.sceneNr = sceneNr;
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

        fluid = new Incompressible(density, numX, numY, h);
//        fluid = new Compressible(density, numX, numY, h);

        CanvasScene.fluid = fluid;

        int n = fluid.numY;

        if (sceneNr == 0) {

            for (int i = 0; i < fluid.numX; i++) {
                for (int j = 0; j < fluid.numY; j++) {
                    boolean s = i != 0 && i != fluid.numX - 1 && j != 0;
                    fluid.s[i * n + j] = s;
                }
            }
            CanvasScene.gravity = -9.81f;
            CanvasScene.showPressure = true;
            CanvasScene.showSmoke = false;
            CanvasScene.showStreamlines = false;
            CanvasScene.showVelocities = false;
        } else if (sceneNr == 1 || sceneNr == 3) {

            float inVel = 2.0f;
            for (int i = 0; i < fluid.numX; i++) {
                for (int j = 0; j < fluid.numY; j++) {
                    boolean s = i != 0 && j != 0 && j != fluid.numY - 1;
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

//            setObstacle(0.4f, 0.5f, true);
//            setImgObstacle((float) (.5f - (testIMG.getWidth() / (2 * WIDTH))), (float) (.3f + (testIMG.getHeight() / HEIGHT)), true);
            if (testIMG != null) setImgObstacle((float) (.5f - (testIMG.getWidth() / (2 * WIDTH))), (float) (.5f + (testIMG.getHeight() / (2 * HEIGHT))), true);

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

        } else if (sceneNr == 2) {

            CanvasScene.gravity = 0.0f;
            CanvasScene.overRelaxation = 1.0f;
            CanvasScene.showPressure = false;
            CanvasScene.showSmoke = true;
            CanvasScene.showStreamlines = false;
            CanvasScene.showVelocities = false;
        }
    }

//    private void setObstacle(float x, float y, boolean reset) {
//        float vx = 0.0f;
//        float vy = 0.0f;
//
//        if (!reset) {
//            vx = (x - CanvasScene.obstacleX) / CanvasScene.dt;
//            vy = (y - CanvasScene.obstacleY) / CanvasScene.dt;
//        }
//
//        CanvasScene.obstacleX = x;
//        CanvasScene.obstacleY = y;
//        float r = CanvasScene.obstacleRadius;
//
//        int n = fluid.numY;
////        float cd = (float) (Math.sqrt(2) * fluid.h);
//
//        for (int i = 1; i < fluid.numX - 2; i++) {
//            for (int j = 1; j < fluid.numY - 2; j++) {
//
//                fluid.s[i * n + j] = 1.0f;
//
//                float dx = (i + 0.5f) * fluid.h - x;
//                float dy = (j + 0.5f) * fluid.h - y;
//
//                if (dx * dx + dy * dy < r * r) {
//                    fluid.s[i * n + j] = 0.0f;
//                    if (CanvasScene.sceneNr == 2)
//                        fluid.m[i * n + j] = 0.5f + 0.5f * (float) Math.sin(0.1f * CanvasScene.frameNr);
//                    else
//                        fluid.m[i * n + j] = 1.0f;
//                    fluid.u[i * n + j] = vx;
//                    fluid.u[(i + 1) * n + j] = vx;
//                    fluid.v[i * n + j] = vy;
//                    fluid.v[i * n + j + 1] = vy;
//                }
//            }
//        }
//
//        CanvasScene.showObstacle = true;
//    }

    private void saveSimToFile(String fileName) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(savedFrames);
            System.out.println("saved");
        }
    }

    private void openSim(String fileName) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            System.out.println("loaded");
            savedFrames = (LinkedList<SaveSim>) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadImg() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png"));
        fileChooser.setTitle("Select Obstacle");

        File file = fileChooser.showOpenDialog(null);

        testIMG = new Image(file.toURI().toString());

        float scale = .75f;

        if (testIMG.getHeight() >= canvas.getHeight() * scale) {
            testIMG = new Image(file.toURI().toString(), testIMG.getWidth() * 0.5f, testIMG.getHeight() * .5f, false, true);
        }

        if (testIMG.getWidth() >= canvas.getWidth() * scale) {
            testIMG = new Image(file.toURI().toString(), testIMG.getWidth() * .5f, testIMG.getHeight() * .5f, false, true);
        }

        aspectRatio = testIMG.getHeight() / testIMG.getWidth();
    }

    private void setImgObstacle(float x, float y, boolean reset) {
        if (testIMG == null) return;

        float vx = 0.0f;
        float vy = 0.0f;

        if (!reset) {
            vx = (x - CanvasScene.obstacleX) / CanvasScene.dt;
            vy = (y - CanvasScene.obstacleY) / CanvasScene.dt;
        }

        CanvasScene.obstacleX = x;
        CanvasScene.obstacleY = y;

        PixelReader reader = testIMG.getPixelReader();

        int n = fluid.numY;
        float h = fluid.h;

        for (int i = 1; i < fluid.numX - 2; i++) {
            for (int j = 1; j < fluid.numY - 2; j++) {
                fluid.s[i * n + j] = true;

                float xPos = cX(i * h);
                float yPos = cY(j * h);

                if (xPos >= cX(CanvasScene.obstacleX) &&
                        xPos < cX(CanvasScene.obstacleX) + testIMG.getWidth() &&
                        yPos >= cY(CanvasScene.obstacleY) &&
                        yPos < cY(CanvasScene.obstacleY) + testIMG.getHeight()) {
                    int alphaValue = (reader.getArgb((int) (xPos - cX(CanvasScene.obstacleX)), (int) (yPos - cY(CanvasScene.obstacleY))) >> 24) & 0xff;

                    if (alphaValue > 0) {
                        fluid.s[i * n + j] = false;
                        if (CanvasScene.sceneNr == 2) {
                            fluid.m[i * n + j] = 0.5f + 0.5f * (float) Math.sin(0.1f * CanvasScene.frameNr);
                        } else {
                            fluid.m[i * n + j] = 1.0f;
                        }
                        fluid.u[i * n + j] = vx;
                        fluid.u[(i + 1) * n + j] = vx;
                        fluid.v[i * n + j] = vy;
                        fluid.v[i * n + j + 1] = vy;
                    }
                }
            }
        }

        CanvasScene.showObstacle = true;
    }

    private void setGround(float y) {
        groundYPos = y;

        int n = fluid.numY;
        float h = fluid.h;

        for (int i = 0; i < fluid.numX - 2; i++) {
            for (int j = 0; j < fluid.numY - 2; j++) {
//                fluid.s[i * n + j] = true;

                float xPos = cX(i * h);
                float yPos = cY(j * h);

                if (xPos > 1 && xPos < WIDTH && yPos > cY(groundYPos) && yPos < HEIGHT) {
                    fluid.s[i * n + j] = false;
                }

            }
        }
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
                } else if (!f.s[i * n + j]) {
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
            int numSegs = 10;

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
//                        float l = (float) Math.sqrt(u * u + v * v);
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
            c.setStroke(Color.BLACK);
            c.setLineWidth(3.0);
            c.strokeRect(cX(CanvasScene.obstacleX), cY(CanvasScene.obstacleY), testIMG.getWidth(), testIMG.getHeight());
            c.setLineWidth(1.0);

            c.drawImage(testIMG, cX(CanvasScene.obstacleX), cY(CanvasScene.obstacleY));
        }

        if (CanvasScene.showPressure) {
            String s = "pressure: " + String.format("%.0f", minP) + " - " + String.format("%.0f", maxP) + " N/m";
            c.setFill(Color.WHITE);
            c.setFont(javafx.scene.text.Font.font("Arial", 16));
            c.fillText(s, 10, 35);
        }

//        if (CanvasScene.ground) {
//            c.setFill(Color.LIGHTGRAY);
//            c.fillRect(0, cY(groundYPos), WIDTH, HEIGHT - cY(groundYPos));
//        }
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
        if (e.isPrimaryButtonDown()) {
            float mx = (float) e.getX() / cScale;
            float my = (float) (HEIGHT - e.getY()) / cScale;
            setImgObstacle(mx, my, true);
        }
    }

    private void drag(MouseEvent e) {
        if (e.isPrimaryButtonDown()) {
            float mx = (float) e.getX() / cScale;
            float my = (float) (HEIGHT - e.getY()) / cScale;
            setImgObstacle(mx, my, false);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

abstract class Fluid {
    float density;
    int numX, numY, numCells;
    float h;
    boolean[] s;
    float[] u, v, newU, newV, p, newP, m, newM, T, newT;
    float thermalCoef;

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
        this.s = new boolean[this.numCells];
        this.m = new float[this.numCells];
        this.newM = new float[this.numCells];
        this.T = new float[this.numCells];
        this.newT = new float[this.numCells];
        this.thermalCoef = 0.35f;
        Arrays.fill(this.m, 1.0f);
    }

    public abstract void simulate(float dt, float gravity, int numIters);

    public void integrate(float dt, float gravity) {
        int n = this.numY;
        for (int i = 1; i < this.numX; i++) {
            for (int j = 1; j < this.numY - 1; j++) {
                if (this.s[i * n + j] && this.s[i * n + j - 1])
                    this.v[i * n + j] += gravity * dt;
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

        return sx * sy * Objects.requireNonNull(f)[x0 * n + y0] +
                tx * sy * f[x1 * n + y0] +
                tx * ty * f[x1 * n + y1] +
                sx * ty * f[x0 * n + y1];
    }

    public float avgU(int i, int j) {
        int n = this.numY;
        return (this.u[i * n + j - 1] + this.u[i * n + j] +
                this.u[(i + 1) * n + j - 1] + this.u[(i + 1) * n + j]) * 0.25f;
    }

    public float avgV(int i, int j) {
        int n = this.numY;
        return (this.v[(i - 1) * n + j] + this.v[i * n + j] +
                this.v[(i - 1) * n + j + 1] + this.v[i * n + j + 1]) * 0.25f;
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
                if (this.s[i * n + j] && this.s[(i - 1) * n + j] && j < this.numY - 1) {
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
                if (this.s[i * n + j] && this.s[i * n + j - 1] && i < this.numX - 1) {
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
                if (this.s[i * n + j]) {
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

    public float boolToFloat(boolean b) {
        return b ? 1.0f : 0.0f;
    }
}

class Incompressible extends Fluid {
    public Incompressible(float density, int numX, int numY, float h) {
        super(density, numX, numY, h);
        this.newP = new float[this.numCells];
    }

    @Override
    public void simulate(float dt, float gravity, int numIters) {
        this.integrate(dt, gravity);
//        this.computeHeatConduction(dt);
//        this.advectTemperature(dt);

        Arrays.fill(this.p, 0.0f);
        this.solveIncompressibility(numIters, dt);

        this.extrapolate();
        this.advectVel(dt);
        this.advectSmoke(dt);
    }

    public void solveIncompressibility(int numIters, float dt) {
        int n = this.numY;
        float cp = this.density * this.h / dt;

        for (int iter = 0; iter < numIters; iter++) {
            for (int i = 1; i < this.numX - 1; i++) {
                for (int j = 1; j < this.numY - 1; j++) {
                    if (!this.s[i * n + j])
                        continue;

                    boolean sx0 = this.s[(i - 1) * n + j];
                    boolean sx1 = this.s[(i + 1) * n + j];
                    boolean sy0 = this.s[i * n + j - 1];
                    boolean sy1 = this.s[i * n + j + 1];
                    float sSum = boolToFloat(sx0) + boolToFloat(sx1) + boolToFloat(sy0) + boolToFloat(sy1);
                    if (sSum == 0.0f)
                        continue;

                    float div = this.u[(i + 1) * n + j] - this.u[i * n + j] +
                            this.v[i * n + j + 1] - this.v[i * n + j];

                    float p = -div / sSum;
                    p *= CanvasScene.overRelaxation;
                    this.p[i * n + j] += cp * p;

                    this.u[i * n + j] -= boolToFloat(sx0) * p;
                    this.u[(i + 1) * n + j] += boolToFloat(sx1) * p;
                    this.v[i * n + j] -= boolToFloat(sy0) * p;
                    this.v[i * n + j + 1] += boolToFloat(sy1) * p;
                }
            }
        }
    }
}

//work in progress
class Compressible extends Fluid {
    public Compressible(float density, int numX, int numY, float h) {
        super(density, numX, numY, h);
        this.newP = new float[this.numCells];
    }

    @Override
    public void simulate(float dt, float gravity, int numIters) {
        this.integrate(dt, gravity);

        Arrays.fill(this.p, 0.0f);
        this.solveCompressibility(numIters, dt);

        this.extrapolate();
        this.advectVel(dt);
        this.advectSmoke(dt);
    }

    public void solveCompressibility(int numIters, float dt) {
        int n = this.numY;
        float cp = this.h / (dt * dt);

        for (int iter = 0; iter < numIters; iter++) {

            float totalDivergence = 0.0f;

            for (int i = 1; i < this.numX - 1; i++) {
                for (int j = 1; j < this.numY - 1; j++) {
                    if (!this.s[i * n + j])
                        continue;

                    // Compute divergence of velocity
                    float div = this.u[(i + 1) * n + j] - this.u[i * n + j] +
                            this.v[i * n + j + 1] - this.v[i * n + j];

                    // Update pressure
                    float pressureUpdate = -div * cp;
                    this.p[i * n + j] += pressureUpdate;

                    // Adjust velocities
                    this.u[i * n + j] -= pressureUpdate;
                    this.u[(i + 1) * n + j] += pressureUpdate;
                    this.v[i * n + j] -= pressureUpdate;
                    this.v[i * n + j + 1] += pressureUpdate;
                }
            }

//            this.density += -dt * totalDivergence * this.density / (this.numX * this.numY);
//
//            float newPressure = (this.density / this.initialDensity) * this.initialPressure;
//            for (int i = 0; i < this.numX; i++) {
//                for (int j = 0; j < this.numY; j++) {
//                    this.p[i * n + j] = newPressure;
//                }
//            }
        }
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
    public static boolean paused = false;
    public static int sceneNr = 0;
    public static boolean showObstacle = false;
    public static boolean showStreamlines = false;
    public static boolean showVelocities = false;
    public static boolean showPressure = false;
    public static boolean showSmoke = true;
    public static boolean showTemperature = false;
    public static Fluid fluid = null;
    public static boolean ground = false;
}

