package project.main;

import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import static project.main.Engine.U_FIELD;
import static project.main.Engine.V_FIELD;
import static project.main.Engine.ground;

public class Controller {


    @FXML
    private Button saveSettings;
    @FXML
    private TextField screenWidthText;
    @FXML
    private TextField screenHeightText;
    @FXML
    TextField customStyleName;
    @FXML
    private Button loadStyle;
    @FXML
    private Button applyStyle;
    @FXML
    TextArea fluidErr;
    @FXML
    private TextArea fileLoaderErr;
    @FXML
    private TextArea engineErr;
    @FXML
    private TextField fluidSpeedText;
    @FXML
    private TextField fluidDensityText;
    @FXML
    private CheckBox customScale;
    @FXML
    private TextField customScaleText;
    @FXML
    private Button loadObsBtn;
    @FXML
    private TextField resText;
    @FXML
    private TextField iterText;
    @FXML
    private CheckBox overRelax;
    @FXML
    private CheckBox showVelocity;
    @FXML
    private CheckBox showStreamlines;
    @FXML
    private CheckBox showPressure;
    @FXML
    private CheckBox showSmoke;
    @FXML
    private TextField groundYPos;
    @FXML
    private CheckBox showGround;
    @FXML
    private TextField obsX;
    @FXML
    private TextField obsY;
    @FXML
    private CheckBox showObsBorder;
    @FXML
    private Button startSim;
    @FXML
    private Button pauseSim;
    @FXML
    private Button resetSim;
    @FXML
    private HBox simInfo;
    @FXML
    private HBox fpsInfo;
    @FXML
    public Label fpsText;
    @FXML
    Label simDurText;
    @FXML
    private HBox frameTimeInfo;
    @FXML
    Label pressureInfoText;
    @FXML
    private Canvas canvasSim;
    @FXML
    private TabPane optionsMenu;
    @FXML
    private Tab controlsOptMenu;
    @FXML
    private Tab recordingOptMenu;
    @FXML
    private Tab settingsOptMenu;
    @FXML
    private Tab errorsOptMenu;
    @FXML
    private Button expandOptionsBtn;
    @FXML
    private Canvas compMemInfoCanvas;
    @FXML
    private Text obstacleNameInfoText;
    @FXML
    private Stage rootStage;

    private boolean optionsExpanded = true;
    private FileLoader fileLoader;
    private GraphicsContext gc;
    private int HEIGHT, WIDTH;
    private SetupChecksum setup;
    private String stylePath;

    static String pressure;

    private Engine engine;
    private static Image obstacleImage, modifiedImage;
    private static Obstacle obstacle;
    public static boolean obstacleBounds = false, groundBounds = false;

    @FXML
    private void initialize() {
        setup = new SetupChecksum(SetupChecksum.OS.WINDOWS);

        WIDTH = Integer.parseInt(setup.getSettings().get("Width"));
        HEIGHT = Integer.parseInt(setup.getSettings().get("Height"));

        if (WIDTH != 800) screenWidthText.setText(String.valueOf(WIDTH));
        if (HEIGHT != 650) screenHeightText.setText(String.valueOf(HEIGHT));

        engine = new Engine(WIDTH, HEIGHT - 50, compMemInfoCanvas);

        gc = canvasSim.getGraphicsContext2D();
        gc.setImageSmoothing(true);
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, WIDTH, HEIGHT - 50);

        Engine.showSmoke = true;
        Engine.showObstacle = true;

        //make a function for resetting Engine

        Image expandArrow = new Image(ClassLoader.getSystemResource("arrow.png").toExternalForm());
        ImageView eAView = new ImageView(expandArrow);
        eAView.setFitHeight(20);
        eAView.setFitWidth(13);
        eAView.setPreserveRatio(true);

        Engine.setupScene(1, Engine.numIters);

        expandOptionsBtn.setGraphic(eAView);

        startSim.setDisable(true);
        pauseSim.setDisable(true);
        resetSim.setDisable(true);
        showObsBorder.setDisable(true);
        customScale.setDisable(true);
        obsX.setDisable(true);
        obsY.setDisable(true);
        showGround.setDisable(true);
        applyStyle.setDisable(true);

        showSmoke.setSelected(true);
        overRelax.setSelected(true);

        Engine.showSmoke = showSmoke.isSelected();

        startSim.setOnAction(e -> {
            Engine.startSimulation();
            checkboxStatus();
        });

        pauseSim.setOnAction(e -> {
            Engine.paused = true;
        });

        expandOptionsBtn.setOnAction(e -> {
            TranslateTransition transitionButton = new TranslateTransition(Duration.millis(300), expandOptionsBtn);
            TranslateTransition transitionOptions = new TranslateTransition(Duration.millis(300), optionsMenu);
            RotateTransition rotate = new RotateTransition(Duration.millis(300), eAView);
            if (!optionsExpanded) {
                transitionButton.setToX(0);
                transitionOptions.setToX(0);
                rotate.setToAngle(0);
            } else {
                transitionButton.setToX(250);
                transitionOptions.setToX(250);
                rotate.setToAngle(180);
            }
            optionsExpanded = !optionsExpanded;
            transitionButton.play();
            transitionOptions.play();
            rotate.play();
        });

        resetSim.setOnAction(e -> {
            Engine.setupScene(1, Engine.numIters);
            Engine.setObstacle(obstacle.x, obstacle.y, true);
            if (showGround.isSelected()) Engine.setGround(ground.y);
            checkboxStatus();
        });

        canvasSim.setOnMousePressed(this::startDrag);
        canvasSim.setOnMouseDragged(this::drag);

        loadObsBtn.setOnAction(e -> {
            loadFile();
        });

        loadStyle.setOnAction(e -> {
            fileLoader = new FileLoader(FileLoader.Type.STYLE);

            if (fileLoader.returnFile() != null) {
                stylePath = fileLoader.returnFilePath();
                customStyleName.setText(stylePath.substring(stylePath.lastIndexOf("\\") + 1, stylePath.lastIndexOf(".")));
                applyStyle.setDisable(false);
            }
        });

        applyStyle.setOnAction(e -> {
            Main.scene.getStylesheets().add(stylePath);
        });

        showGround.setOnAction(e -> {
            if (showGround.isSelected()) {
                ground = new Obstacle(Float.parseFloat(groundYPos.getText()));
                Engine.setGround(ground.y);
            } else {
                ground = null;
                Engine.setupScene(1, Engine.numIters);
                if (obstacle != null) Engine.setObstacle(obstacle.x, obstacle.y, true);
            }
        });

        showSmoke.setOnAction(e -> {
            if (showPressure.isSelected()) {
                Engine.showSmoke = showSmoke.isSelected();
            } else showSmoke.setSelected(true);
        });

        showPressure.setOnAction(e -> {
            if (showSmoke.isSelected()) {
                Engine.showPressure = showPressure.isSelected();
            } else showPressure.setSelected(true);
        });

        showStreamlines.setOnAction(e -> {
            Engine.showStreamlines = showStreamlines.isSelected();
        });

        showObsBorder.setOnAction(e -> {
            obstacleBounds = showObsBorder.isSelected();
        });

        showVelocity.setOnAction(e -> {
            Engine.showVelocities = showVelocity.isSelected();
        });

        overRelax.setOnAction(e -> {
            if (overRelax.isSelected()) Engine.overRelaxation = 1.9f;
            else Engine.overRelaxation = 1.0f;
        });

        obsX.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && (!obsX.getText().matches("^[^a-zA-Z]*$") || !obsX.getText().isEmpty()) && obstacle != null) {
                obstacle.x = Math.min(returnFloatValue(obsX.getText()), 1);
                if (customScale.isSelected()) obstacle.x = Math.max(returnFloatValue(obsX.getText()), (float) (modifiedImage.getHeight() / Engine.getHeight()));
                else obstacle.x = Math.max(returnFloatValue(obsX.getText()), (float) (obstacleImage.getHeight() / Engine.getHeight()));
                obsX.setText(String.valueOf(obstacle.x));
                Engine.setObstacle(obstacle.x, obstacle.y, true);
                if (ground != null) Engine.setGround(ground.y);
            }
        });

        obsY.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && (!obsY.getText().matches("^[^a-zA-Z]*$") || !obsY.getText().isEmpty()) && obstacle != null) {
                obstacle.y = Math.min(returnFloatValue(obsY.getText()), 1);
                if (customScale.isSelected()) obstacle.y = Math.max(returnFloatValue(obsY.getText()), (float) (modifiedImage.getHeight() / Engine.getHeight()));
                else obstacle.y = Math.max(returnFloatValue(obsY.getText()), (float) (obstacleImage.getHeight() / Engine.getHeight()));
                obsY.setText(String.valueOf(obstacle.y));
                Engine.setObstacle(obstacle.x, obstacle.y, true);
                if (ground != null) Engine.setGround(ground.y);
            }
        });

        resText.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && (!resText.getText().matches("^[^a-zA-Z]*$") || !resText.getText().isEmpty() || !(resText.getText().equals("0.0")))) {
                Engine.resolution = Integer.parseInt(resText.getText());
                Engine.setupScene(1, Engine.numIters);
            }
        });

        iterText.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && (!iterText.getText().matches("^[^a-zA-Z]*$") || !iterText.getText().isEmpty() || !(iterText.getText().equals("0.0")))) {
                Engine.numIters = Integer.parseInt(iterText.getText());
                Engine.setupScene(1, Engine.numIters);
            }
        });

        customScale.setOnAction(e -> {
            customScaleText.setDisable(!customScale.isSelected());
            if (!customScale.isSelected()) {
                float xPos = 0.2f, yPos = (float) (.5f + (obstacleImage.getHeight() /  (2 * (HEIGHT - 50))));

                obstacle = new Obstacle(xPos, yPos, obstacleImage);

                Engine.obstacle = obstacle;
                Engine.setupScene(1, Engine.numIters);
                Engine.setObstacle(obstacle.x, obstacle.y, true);
            }
        });

        customScaleText.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && (!customScaleText.getText().matches("^[^a-zA-Z]*$") || !customScaleText.getText().isEmpty())) {
                Float scale = returnFloatValue(customScaleText.getText());

                float scaleWidth = (float) obstacleImage.getWidth() * scale, scaleHeight = (float) obstacleImage.getHeight() * scale;
                modifiedImage = new Image(fileLoader.returnFile().toURI().toString(), scaleWidth, scaleHeight, false, true);

                float xPos = 0.2f, yPos = (float) (.5f + (modifiedImage.getHeight() /  (2 * (HEIGHT - 50))));

                obstacle = new Obstacle(xPos, yPos, modifiedImage);

                Engine.obstacle = obstacle;
                Engine.setupScene(1, Engine.numIters);
                Engine.setObstacle(obstacle.x, obstacle.y, true);
                if (ground != null) Engine.setGround(ground.y);
            }
        });

        fluidSpeedText.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && (!fluidSpeedText.getText().matches("^[^a-zA-Z]*$") || !fluidSpeedText.getText().isEmpty() || !(fluidSpeedText.getText().equals("0.0"))) && obstacleImage != null) {
                Engine.inVel = returnFloatValue(fluidSpeedText.getText());
                Engine.setupScene(1, Engine.numIters);
                Engine.setObstacle(obstacle.x, obstacle.y, true);
            }
        });

        fluidDensityText.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && (!fluidDensityText.getText().matches("^[^a-zA-Z]*$") || !fluidDensityText.getText().isEmpty() || !(fluidSpeedText.getText().equals("0.0"))) && obstacleImage != null) {
                Engine.density = returnFloatValue(fluidDensityText.getText());

                Engine.setupScene(1, Engine.numIters);
                Engine.setObstacle(obstacle.x, obstacle.y, true);
            }
        });

        fluidDensityText.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && (fluidDensityText.getText().matches("^[^a-zA-Z]*$") || fluidDensityText.getText().isEmpty())) {
                fluidDensityText.setText(String.format("%.1f", Engine.density));
            }
        });

        fluidSpeedText.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && (fluidSpeedText.getText().matches("^[^a-zA-Z]*$") || fluidSpeedText.getText().isEmpty())) {
                fluidSpeedText.setText(String.format("%.1f", Engine.inVel));
            }
        });

        saveSettings.setOnAction(e -> {
            int screenHeight = Integer.parseInt(screenHeightText.getText());
            screenHeight = Math.max(screenHeight, 1080);
            screenHeight = Math.min(screenHeight, 240);

            setup.getSettings().put("Height", String.valueOf(screenHeight));

            int screenWidth = Integer.parseInt(screenWidthText.getText());
            screenWidth = Math.max(screenWidth, 1920);
            screenWidth = Math.min(screenWidth, 426);

            setup.getSettings().put("Width", String.valueOf(screenWidth));

            setup.saveSettings();
        });

        expandErrArea(fileLoaderErr);
        expandErrArea(engineErr);
        expandErrArea(fluidErr);

//
    }

    public SetupChecksum getSettingsManager() {
        return setup;
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

    private void expandErrArea(TextArea errArea) {
        errArea.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) errArea.setPrefHeight(computePrefHeight(errArea));
            else errArea.setPrefHeight(100);
        });
    }

    private double computePrefHeight(Node node) {
        double height = 0;

        if (node instanceof TextArea) {
            height = TextArea.USE_COMPUTED_SIZE;
        }

        return height;
    }

    private void checkboxStatus() {
        Engine.showSmoke = showSmoke.isSelected();
        Engine.showPressure = showPressure.isSelected();
        Engine.showVelocities = showVelocity.isSelected();
        Engine.showStreamlines = showStreamlines.isSelected();
        if (overRelax.isSelected()) Engine.overRelaxation = 1.9f;
        else Engine.overRelaxation = 1.0f;
    }

    void draw() {
        gc.clearRect(0, 0, Engine.getWidth(), Engine.getHeight());

        Fluid f = Engine.fluid;
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

                if (Engine.showPressure) {
                    float p = f.p[i * n + j];
                    float s = f.m[i * n + j];
                    color = getSciColor(p, minP, maxP);
                    if (Engine.showSmoke) {
                        color[0] = Math.max(0, color[0] - (int) (255 * s));
                        color[1] = Math.max(0, color[1] - (int) (255 * s));
                        color[2] = Math.max(0, color[2] - (int) (255 * s));
                    }
                } else if (Engine.showSmoke) {
                    float s = f.m[i * n + j];
                    color[0] = (int) (255 * s);
                    color[1] = (int) (255 * s);
                    color[2] = (int) (255 * s);
                    if (Engine.sceneNr == 2)
                        color = getSciColor(s, 0.0f, 1.0f);
                } else if (!f.s[i * n + j]) {
                    color[0] = 0;
                    color[1] = 0;
                    color[2] = 0;
                }

                int x = (int) (Engine.cX(i * h));
                int y = (int) (Engine.cY((j + 1) * h));
                int cx = (int) (Engine.cScale * cellScale * h) + 1;
                int cy = (int) (Engine.cScale * cellScale * h) + 1;

                int r = color[0];
                int g = color[1];
                int b = color[2];

                gc.setFill(Color.rgb(r, g, b));
                gc.fillRect(x, y, cx, cy);
            }
        }

        if (Engine.showVelocities) {
            gc.setStroke(Color.BLACK);
            float scale = 0.02f;

            for (int i = 0; i < f.numX; i++) {
                for (int j = 0; j < f.numY; j++) {
                    float u = f.u[i * n + j];
                    float v = f.v[i * n + j];

                    int x0 = (int) Engine.cX(i * h);
                    int x1 = (int) Engine.cX(i * h + u * scale);
                    int y = (int) Engine.cY((j + 0.5f) * h);

                    gc.strokeLine(x0, y, x1, y);

                    int x = (int) Engine.cX((i + 0.5f) * h);
                    int y0 = (int) Engine.cY(j * h);
                    int y1 = (int) Engine.cY(j * h + v * scale);

                    gc.strokeLine(x, y0, x, y1);
                }
            }
        }

        if (Engine.showStreamlines) {
            int numSegs = 10;

            gc.setStroke(Color.BLACK);

            for (int i = 1; i < f.numX - 1; i += 5) {
                for (int j = 1; j < f.numY - 1; j += 5) {
                    float x = (i + 0.5f) * f.h;
                    float y = (j + 0.5f) * f.h;

                    int lastX = (int) Engine.cX(x);
                    int lastY = (int) Engine.cY(y);

                    for (int z = 0; z < numSegs; z++) {
                        float u = f.sampleField(x, y, U_FIELD);
                        float v = f.sampleField(x, y, V_FIELD);
//                        float l = (float) Math.sqrt(u * u + v * v);
                        x += u * 0.01f;
                        y += v * 0.01f;
                        if (x > f.numX * f.h)
                            break;

                        int currX = (int) Engine.cX(x);
                        int currY = (int) Engine.cY(y);
                        gc.strokeLine(lastX, lastY, currX, currY);
                        lastX = currX;
                        lastY = currY;
                    }
                }
            }
        }

        if (Engine.showObstacle) {
            if (obstacleBounds) {
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(3.0);
                gc.strokeRect(Engine.cX(Engine.obstacle.x), Engine.cY(Engine.obstacle.y), Engine.obstacle.image.getWidth(), Engine.obstacle.image.getHeight());
                gc.setLineWidth(1.0);
            }

            if (customScale.isSelected()) gc.drawImage(modifiedImage, Engine.cX(Controller.getObstacle().x), Engine.cY(Controller.getObstacle().y));
            else gc.drawImage(obstacleImage, Engine.cX(Controller.getObstacle().x), Engine.cY(Controller.getObstacle().y));
        }

        if (ground != null) {
            gc.setFill(Color.DARKGREY);
            gc.fillRect(0, Engine.cY(ground.y), Engine.getWidth(), Engine.getHeight() - Engine.cY(ground.height));
        }

        pressure = "[" + String.format("%.0f", minP) + "," + String.format("%.0f", maxP) + "] N/m";
    }

    private static int[] getSciColor(float val, float minVal, float maxVal) {
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

    public void setStage(Stage stage) {
        rootStage = stage;
    }

    public Stage getStage() {
        return rootStage;
    }

    public void loadFile() {
        fileLoader = new FileLoader(FileLoader.Type.OBSTACLE);

        try {
            obstacleImage = new Image(fileLoader.returnFile().toURI().toString());

            float scaleWidth = (float) obstacleImage.getWidth() * Float.parseFloat(customScaleText.getText()), scaleHeight = (float) obstacleImage.getHeight() * Float.parseFloat(customScaleText.getText());

            modifiedImage = new Image(fileLoader.returnFile().toURI().toString(), scaleWidth, scaleHeight, false, true);

            float xPos = 0.2f, yPos = (float) (.5f + (obstacleImage.getHeight() /  (2 * (HEIGHT - 50))));

            if (!obsY.getText().matches("^[^a-zA-Z]*$") || !obsY.getText().isEmpty()) {
                yPos = Float.parseFloat(obsY.getText());
            }

            if (!obsX.getText().matches("^[^a-zA-Z]*$") || !obsX.getText().isEmpty()) {
                xPos = Float.parseFloat(obsX.getText());
            }

            obstacle = new Obstacle(xPos, yPos, obstacleImage);
            if (customScale.isSelected()) obstacle = new Obstacle(xPos, yPos, modifiedImage);

            Engine.obstacle = obstacle;
            Engine.setupScene(1, Engine.numIters);
            Engine.setObstacle(obstacle.x, obstacle.y, true);

            obstacleNameInfoText.setText(obstacle.obstacleName);

            startSim.setDisable(false);
            pauseSim.setDisable(false);
            resetSim.setDisable(false);
            showObsBorder.setDisable(false);
            customScale.setDisable(false);
            obsX.setDisable(false);
            obsY.setDisable(false);
            showGround.setDisable(false);
        } catch (Exception e) {
            fileLoaderErr.setText(e.getMessage());
        }
    }

    float returnFloatValue(String input) {
        if (input.charAt(0) != '0') input = "0" + input;
        return Float.parseFloat(input);
    }

    public static Obstacle getObstacle() {
        return obstacle;
    }
}
