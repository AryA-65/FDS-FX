package project.main;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

//public abstract class PopUpWindow {
//    enum Type {
//        WARNING, ERROR, RECORDER, SETTINGS, CONFIRMATION, OTHER
//    }
//
//    private Type popUpType;
//    private int WIDTH, HEIGHT;
//    private Scene scene;
//    public boolean actionFinished;
//
//    PopUpWindow() {
//        super.setTitle("");
//    }
//
//    PopUpWindow(Type popUpType) {
//        this.popUpType = popUpType;
//        super.setResizable(false);
//        super.setFullScreen(false);
//        this.scene = contents();
//        super.setScene(scene);
//    }
//
//    PopUpWindow(Type popUpType, int WIDTH, int HEIGHT) {
//        this.popUpType = popUpType;
//        this.WIDTH = WIDTH;
//        this.HEIGHT = HEIGHT;
//        super.setResizable(false);
//        super.setFullScreen(false);
//        this.scene = contents();
//        super.setScene(scene);
//    }
//
//    public abstract Scene contents();
//}
//
//class RecordSim extends PopUpWindow {
//    RecordSim(Type popUpType) {
//        super(popUpType);
//    }
//
//    RecordSim(Type popUpType, int WIDTH, int HEIGHT) {
//        super(popUpType, WIDTH, HEIGHT);
//    }
//
//    @Override
//    public Scene contents() {
//        return null;
//    }
//}
//
//class DeleteRecording extends PopUpWindow {
//    DeleteRecording(Type popUpType) {
//        super(popUpType);
//    }
//
//    DeleteRecording(Type popUpType, int WIDTH, int HEIGHT) {
//        super(popUpType, WIDTH, HEIGHT);
//    }
//
//    @Override
//    public Scene contents() {
//        return null;
//    }
//}
//
//class EndSim extends PopUpWindow {
//    EndSim() {
//        super.setTitle("Confirmation");
//    }
//
//    EndSim(Type popUpType) {
//        super(popUpType);
//
//    }
//
//    EndSim(PopUpWindow.Type popUpType, int WIDTH, int HEIGHT) {
//        super(popUpType, WIDTH, HEIGHT);
//
//    }
//
//    @Override
//    public Scene contents() {
//        Label promptLabel = new Label("Close Simulation?");
//
//        Button continueButton = new Button("Continue");
//        Button endButton = new Button("End");
//
//        endButton.setOnAction(e -> {
//            super.close();
//            this.actionFinished = true;
//        });
//
//        HBox btnContainer = new HBox(5, continueButton, endButton);
//        VBox mainContainer = new VBox(5, promptLabel, btnContainer);
//        mainContainer.setPadding(new Insets(10));
//
//        if (this.getHeight() != 0 || this.getWidth() != 0) return new Scene(mainContainer, this.getWidth(), this.getHeight());
//        return new Scene(mainContainer);
//    }
//}