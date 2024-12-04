package project.main;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static project.main.Engine.ground;
import static project.main.Main.*;

public class CanvasSim {

    public static Canvas canvas;
    public static GraphicsContext gc;
    public static boolean obstacleBounds = false, groundBounds = false;

    CanvasSim() {
        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(true);

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 800, 600);
        gc.setFill(Color.WHITE);
        System.out.println("canvas created");
    }

    CanvasSim(int width, int height) {
        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(true);
    }

    static void draw() {
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

            gc.drawImage(Main.testIMG, Engine.cX(Main.obstacle.x), Engine.cY(Main.obstacle.y));
        }

        if (Engine.showPressure) {
            String s = "pressure: " + String.format("%.0f", minP) + " - " + String.format("%.0f", maxP) + " N/m";
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", 16));
            gc.fillText(s, 10, 35);
        }

        if (ground != null) {
            gc.setFill(Color.LIGHTGRAY);
            gc.fillRect(0, Engine.cY(ground.y), Engine.getWidth(), Engine.getHeight() - Engine.cY(ground.height));
        }
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

    public static Canvas returnCanvas() {
        System.out.println("returning canvas");
        return canvas;
    }
}
