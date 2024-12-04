package project.main;

import javafx.scene.image.Image;

public class Obstacle {
    protected enum Type {OBSTACLE, CONSTRAINTS}

    float x, y;
    int width, height;
    Image image;
    private Type obstacleType;
    String obstacleName, obstaclePath;
    boolean showBorder = false;

    public Obstacle(Image obstacle) {
        this.image = obstacle;
        this.obstaclePath = obstacle.getUrl();
    }

    public Obstacle(float x, float y, Image obstacle) {
        this.x = x;
        this.y = y;
        this.image = obstacle;
        this.obstacleType = Type.OBSTACLE;
    }

    public Obstacle(float y, int width, int height) {
        this.x = 0;
        this.y = y;
        this.width = width;
        this.height = height;
        this.obstacleType = Type.CONSTRAINTS;
    }

    public Type getObstacleType() {
        return obstacleType;
    }

    public void setObstacleName(String obstacleName) {
        this.obstacleName = obstacleName;
    }
}
