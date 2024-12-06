package project.main;

import javafx.animation.AnimationTimer;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;

public class Recording implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    static LinkedList<Frame> savedFrames;
    static LinkedList<Obstacle> obstacles;
    static int frameCount = 0, maxDuration = 0, index = 0;
    static AnimationTimer replayTimer;

    Recording() {
        savedFrames = new LinkedList<>();
        Recording.obstacles = new LinkedList<>();
//        this.maxDuration =
    }

    public void addObstacle(Obstacle obstacle) {
        obstacles.add(obstacle);
    }

    public void removeObstacle(Obstacle obstacle) {
        obstacles.remove(obstacle);
    }

    public static void addFrame() {
        savedFrames.add(new Frame());
    }

    public static void replayRecording() {
        Engine.replay = true;
        replayTimer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                if (index < Recording.savedFrames.size()) {
                    Frame frame = Recording.savedFrames.get(index++);
                    System.arraycopy(frame.u, 0, Engine.fluid.u, 0, frame.u.length);
                    System.arraycopy(frame.v, 0, Engine.fluid.v, 0, frame.v.length);
                    System.arraycopy(frame.p, 0, Engine.fluid.p, 0, frame.p.length);
                    System.arraycopy(frame.m, 0, Engine.fluid.m, 0, frame.m.length);
//                    Controller.draw();
                } else {
                    stop();
                }
            }
        };
        replayTimer.start();

    }
}

class Frame {
    float[] u;
    float[] v;
    float[] p;
    float[] m;

    Frame() {
        this.u = Arrays.copyOf(Engine.fluid.u, Engine.fluid.u.length);
        this.v = Arrays.copyOf(Engine.fluid.v, Engine.fluid.v.length);
        this.p = Arrays.copyOf(Engine.fluid.p, Engine.fluid.p.length);
        this.m = Arrays.copyOf(Engine.fluid.m, Engine.fluid.m.length);
    }
}

class SaveRecodring {
    String path;
    String fileName = "recording";

    SaveRecodring(String path) {
        this.path = path;
        for (Obstacle obstacle : Recording.obstacles) {
            if (obstacle != null) {
                this.fileName += obstacle.obstacleName + "_";
            }
        }
    }

    public boolean save() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path + fileName + ".txt")) {
            for (Obstacle obstacle : Recording.obstacles) {
                if (obstacle != null && obstacle.obstaclePath != null) {
                    fos.write(obstacle.obstaclePath.getBytes());
                }
            }
            fos.write((path + fileName + ".bin").getBytes());
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path + fileName + ".bin"))) {
                oos.writeObject(Recording.savedFrames);
            }
            fos.close();
            return true;
        }
    }
}