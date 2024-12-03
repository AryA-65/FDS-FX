package project.main;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;

public class Recording implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    static LinkedList<Frame> savedFrames;
    static LinkedList<Obstacle> obstacles;
    static int frameCount = 0;

    Recording(LinkedList<Obstacle> obstacles) {
        savedFrames = new LinkedList<Frame>();
        Recording.obstacles = obstacles;
    }

    public static void addFrame() {
        savedFrames.add(new Frame());
    }
}

class Frame {
    float[] u;
    float[] v;
    float[] p;
    float[] m;

    Frame() {
        this.u = Arrays.copyOf(CanvasScene.fluid.u, CanvasScene.fluid.u.length);
        this.v = Arrays.copyOf(CanvasScene.fluid.v, CanvasScene.fluid.v.length);
        this.p = Arrays.copyOf(CanvasScene.fluid.p, CanvasScene.fluid.p.length);
        this.m = Arrays.copyOf(CanvasScene.fluid.m, CanvasScene.fluid.m.length);
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