package project.main;

import javafx.scene.image.Image;

public class Obstacle {
    private enum Type {OBSTACLE, CONSTRAINTS}

    private float x, y;
    private int width, height;
    private Image obstacle;
    private Type obstacleType;
    String obstacleName, obstaclePath;
    private boolean fixed;

    public Obstacle(Image obstacle) {
        this.obstacle = obstacle;
        this.obstaclePath = obstacle.getUrl();
    }

    public Obstacle(float x, float y, Image obstacle) {
        this.x = x;
        this.y = y;
        this.obstacle = obstacle;
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

    public void setObstacleCanvas() {

    }

    public void setObstacleName(String obstacleName) {
        this.obstacleName = obstacleName;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }
}
