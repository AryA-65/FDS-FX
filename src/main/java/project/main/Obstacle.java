package project.main;

import javafx.scene.image.Image;

public class Obstacle {
    protected enum Type {OBSTACLE, CONSTRAINTS}

    float x, y;
    float width, height;
    Image image;
    private Type obstacleType;
    String obstacleName, obstaclePath;

    public Obstacle(Image obstacle) {
        this.image = obstacle;
        this.obstaclePath = obstacle.getUrl();
        this.obstacleName = obstaclePath.substring(obstaclePath.lastIndexOf("/") + 1, obstaclePath.lastIndexOf("."));
    }

    public Obstacle(float x, float y, Image obstacle) {
        this.x = x;
        this.y = y;
        this.image = obstacle;
        this.height = (float) (obstacle.getHeight() / Engine.getHeight());
        this.width = (float) (obstacle.getWidth() / Engine.getWidth());
        this.obstaclePath = obstacle.getUrl();
        this.obstacleName = obstaclePath.substring(obstaclePath.lastIndexOf("/") + 1, obstaclePath.lastIndexOf("."));
        this.obstacleType = Type.OBSTACLE;
    }

    public Obstacle(float y) {
        this.x = 0;
        this.y = y;
        this.width = Engine.getWidth();
        this.height = y;
        this.obstacleType = Type.CONSTRAINTS;
    }
}
