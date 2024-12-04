package project.main;

import javafx.animation.AnimationTimer;
import javafx.scene.image.PixelReader;

public class Engine {
    public static float gravity = -9.81f, dt, overRelaxation, simWidth, cScale;
    public static final float simHeight = 1.0f;
    public static int numIters = 100, frameNr = 0, sceneNr = 0, resolution = 100;
    public static boolean paused = false, showObstacle = false, showStreamlines = false, showVelocities = false, showPressure = false, showSmoke = true, recording = false, replay;
    public static Fluid fluid;
    public static Obstacle ground;
    public static Obstacle obstacle;

    private long lastTime = 0;
    private static AnimationTimer simulationTimer;
    private static int WIDTH, HEIGHT;


    Engine(int width, int height) {
        Engine.WIDTH = width;
        Engine.HEIGHT = height;

        overRelaxation = 1.9f;
        dt = 1f / 120;

        cScale = HEIGHT / simHeight;
        simWidth = WIDTH / cScale;

        //temporary
        float domainHeight = 1.0f;
        float domainWidth = domainHeight / simHeight * simWidth;
        float h = domainHeight / resolution;

        int numX = (int) (domainWidth / h);
        int numY = (int) (domainHeight / h);
        //

        fluid = new Incompressible(1000.0f, numX, numY, h);

        simulationTimer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                simulate();
                long simTimeEnd = System.nanoTime();
//                simTimeLabel.setText(String.format("Sim Time: %.2fms", ((simTimeEnd - l)/1000000.0)));


                double deltaTime = (l - lastTime) / 1_000_000_000.0;
                lastTime = l;

                if (recording) {
                    Recording.frameCount++;
                    System.out.println(Recording.frameCount);
                    Recording.addFrame();
                    if (Recording.frameCount >= Recording.maxDuration) {
                        recording = false;

                        Recording.frameCount = 0;
                    }
                }

                double fps = 1.0 / deltaTime;
//                fpsLabel.setText("FPS: " + String.format("%.2f", fps));

                if (Main.getRoot().isShowing()) {
                    CanvasSim.draw();
                }
            }
        };
    }

    static void setupScene(int sceneNr, int resolution, int numIters) {
        Engine.sceneNr = sceneNr;
        Engine.overRelaxation = 1.9f;

        Engine.dt = 1.0f / 60.0f;
        Engine.numIters = numIters;

        if (sceneNr == 0)
            Engine.resolution = 50;
        else if (sceneNr == 3)
            Engine.resolution = 200;

        float domainHeight = 1.0f;
        float domainWidth = domainHeight / simHeight * simWidth;
        float h = domainHeight / Engine.resolution;

        int numX = (int) (domainWidth / h);
        int numY = (int) (domainHeight / h);

        float density = 1000.0f;

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

            float inVel = 2.0f;
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

            if (obstacle != null) setObstacle((float) (.5f - (obstacle.image.getWidth() / (2 * obstacle.image.getWidth()))), (float) (.5f + (obstacle.image.getHeight() / (2 * obstacle.image.getHeight()))), true);

            Engine.gravity = 0.0f;
            Engine.showPressure = false;
            Engine.showSmoke = true;
            Engine.showStreamlines = false;
            Engine.showVelocities = false;

            if (sceneNr == 3) {
                Engine.dt = 1.0f / 120.0f;
                Engine.numIters = 100;
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
        if (obstacle.image == null) return;

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
    }

    public static void startSimulation() {
        if (obstacle != null) {
            simulationTimer.start();
        }
    }

    private void simulate() {
        if (!paused) {
//            System.out.println("simming in Engine");
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


}
