package project.main;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

public class Engine { //make this non-static

    protected static final byte U_FIELD = 0;
    protected static final byte V_FIELD = 1;
    protected static final byte S_FIELD = 2;
    public static final float simHeight = 1.0f;

    public static float gravity = -9.81f, dt, overRelaxation, simWidth, cScale, inVel = 2.0f, density;
    public static int numIters = 40, frameNr = 0, sceneNr = 0, resolution = 100;
    public static boolean paused = false, showObstacle = false, showStreamlines = false, showVelocities = false, showPressure = false, showSmoke = true, recording = false, replay;
    public static Fluid fluid;
    public static Obstacle ground;
    public static Obstacle obstacle;

    private long lastTime = 0;
    private static AnimationTimer simulationTimer;
    private static int WIDTH, HEIGHT;
    private final ComputerInfo computerInfo;
    private static Canvas canvas;
    private GraphicsContext gc;

    Engine(int width, int height, Canvas c) {
        computerInfo = new ComputerInfo();
        Engine.canvas = c;
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.PURPLE);

        int cHeight = (int) canvas.getHeight(), cWidth = (int) canvas.getWidth();

        density = 1000.0f;

        Engine.WIDTH = width;
        Engine.HEIGHT = height;

        overRelaxation = 1.9f;
        dt = 1.0f / 60.0f;

        cScale = HEIGHT / simHeight;
        simWidth = WIDTH / cScale;

        simulationTimer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                Engine.simulate();
                long simTimeEnd = System.nanoTime();
                Main.controller.simDurText.setText(String.format("%.2fms", ((simTimeEnd - l)/1000000.0)));

                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                double deltaTime = (l - lastTime) / 1_000_000_000.0;
                lastTime = l;

                if (Engine.recording) {
                    Recording.frameCount++;
                    System.out.println(Recording.frameCount);
                    Recording.addFrame();
                    if (Recording.frameCount >= Recording.maxDuration) {
                        Engine.recording = false;

                        Recording.frameCount = 0;
                    }
                }

                long used = computerInfo.getMemory().getInUse();
                long total = computerInfo.getMemory().getTotal();

                gc.fillRect(0, cHeight - ((double) used / total * cHeight), cWidth, cHeight);

                Main.controller.pressureInfoText.setText(Controller.pressure);

                double fps = 1.0 / deltaTime;
                Main.controller.fpsText.setText(String.format("%.1f", fps));

                if (Main.getRoot().isShowing()) {
                    Main.controller.draw();
                }
            }
        };
    }

    static void setupScene(int sceneNr, int numIters) {
        Engine.sceneNr = sceneNr;
        Engine.numIters = numIters;

        float domainHeight = 1.0f;
        float domainWidth = domainHeight / simHeight * simWidth;
        float h = domainHeight / Engine.resolution;

        int numX = (int) (domainWidth / h);
        int numY = (int) (domainHeight / h);

//        fluid = new Compressible(density, numX, numY, h);

        Engine.fluid = new Incompressible(density, numX, numY, h);

        int n = Engine.fluid.numY;

        if (sceneNr == 0) {

            for (int i = 0; i < Engine.fluid.numX; i++) {
                for (int j = 0; j < Engine.fluid.numY; j++) {
                    boolean s = i != 0 && i != Engine.fluid.numX - 1 && j != 0;
                    Engine.fluid.s[i * n + j] = s;
                }
            }
            Engine.gravity = -9.81f;
            Engine.showPressure = true;
            Engine.showSmoke = false;
            Engine.showStreamlines = false;
            Engine.showVelocities = false;
        } else if (sceneNr == 1 || sceneNr == 3) {
            for (int i = 0; i < Engine.fluid.numX; i++) {
                for (int j = 0; j < Engine.fluid.numY; j++) {
                    boolean s = i != 0 && j != 0 && j != Engine.fluid.numY - 1;
                    Engine.fluid.s[i * n + j] = s;

                    if (i == 1) {
                        Engine.fluid.u[i * n + j] = inVel;
                    }
                }
            }

            int pipeH = (int) (0.1f * Engine.fluid.numY);
            int minJ = (int) (0.5f * Engine.fluid.numY - 0.5f * pipeH);
            int maxJ = (int) (0.5f * Engine.fluid.numY + 0.5f * pipeH);

            for (int j = minJ; j < maxJ; j++)
                Engine.fluid.m[j] = 0.0f;

            if (obstacle != null) setObstacle(obstacle.x, obstacle.y, true);

            Engine.gravity = 0.0f;
//            Engine.showPressure = false;
//            Engine.showSmoke = true;
//            Engine.showStreamlines = false;
//            Engine.showVelocities = false;

            if (sceneNr == 3) {
//                Engine.dt = 1.0f / 120.0f;
                Engine.numIters = 100; //make this an option for the sim
                Engine.showPressure = true;
            }

        } else if (sceneNr == 2) {

            Engine.gravity = 0.0f;
            Engine.overRelaxation = 1.0f;
            Engine.showPressure = false;
            Engine.showSmoke = true;
            Engine.showStreamlines = false;
            Engine.showVelocities = false;
        }
    }

    static void setObstacle(float x, float y, boolean reset) {
        try {
            float vx = 0.0f;
            float vy = 0.0f;

            if (!reset) {
                vx = (x - Engine.obstacle.x) / Engine.dt;
                vy = (y - Engine.obstacle.y) / Engine.dt;
            }

            Engine.obstacle.x = x;
            Engine.obstacle.y = y;

            PixelReader reader = obstacle.image.getPixelReader();

            int n = Engine.fluid.numY;
            float h = Engine.fluid.h;

            for (int i = 1; i < Engine.fluid.numX - 2; i++) {
                for (int j = 1; j < Engine.fluid.numY - 2; j++) {
                    Engine.fluid.s[i * n + j] = true;

                    float xPos = cX(i * h);
                    float yPos = cY(j * h);

                    if (xPos >= cX(x) &&
                            xPos < cX(x) + obstacle.image.getWidth() &&
                            yPos >= cY(y) &&
                            yPos < cY(y) + obstacle.image.getHeight()) {
                        int alphaValue = (reader.getArgb((int) (xPos - cX(x)), (int) (yPos - cY(y))) >> 24) & 0xff;

                        if (alphaValue > 0) {
                            Engine.fluid.s[i * n + j] = false;
                            if (Engine.sceneNr == 2) {
                                Engine.fluid.m[i * n + j] = 0.5f + 0.5f * (float) Math.sin(0.1f * Engine.frameNr);
                            } else {
                                Engine.fluid.m[i * n + j] = 1.0f;
                            }
                            Engine.fluid.u[i * n + j] = vx;
                            Engine.fluid.u[(i + 1) * n + j] = vx;
                            Engine.fluid.v[i * n + j] = vy;
                            Engine.fluid.v[i * n + j + 1] = vy;
                        }
                    }
                }
            }

            Engine.showObstacle = true;
        } catch (Exception e) {
            e.printStackTrace();
            //make a error listener class that gets the error and set it in controller (same with the other classes that throw errors)
        }
    }

    static void setGround(float simY) {
        int n = fluid.numY;
        float h = fluid.h;

        for (int i = 0; i < fluid.numX - 2; i++) {
            for (int j = 0; j < fluid.numY - 2; j++) {

                float xPos = cX(i * h);
                float yPos = cY(j * h);

                if (xPos > 1 && xPos < WIDTH && yPos > cY(simY) && yPos < HEIGHT) {
                    fluid.s[i * n + j] = false;
                }
            }
        }
    }

    public static void startSimulation() {
        if (obstacle != null) {
            simulationTimer.start();
            paused = false;
        }
    }

    static void simulate() {
        if (!paused) {
            fluid.simulate(dt, gravity, numIters);
            frameNr++;
        }
    }

    protected static float cX(float x) {
        return x * Engine.cScale;
    }

    protected static float cY(float y) {
        return HEIGHT - y * Engine.cScale;
    }

    public static int getHeight() {
        return HEIGHT;
    }

    public static int getWidth() {
        return WIDTH;
    }

    static void setCanvas(Canvas canvas) {
        Engine.canvas = canvas;
    }
}
