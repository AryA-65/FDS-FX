package project.main;

import javafx.stage.Stage;

public class PopUpWindow extends Stage {
    private enum Type {
        WARNING, ERROR, RECORDER, SETTINGS, CONFIRMATION, OTHER
    }

    private Type popUpType;
    private boolean fixedSize;
    private int WIDTH, HEIGHT;

    public PopUpWindow(Type popUpType, int WIDTH, int HEIGHT) {
        this.popUpType = popUpType;
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        if (WIDTH == 0 || HEIGHT == 0) fixedSize = false;
        else fixedSize = true;
    }
}

//class RecordSim extends PopUpWindow {
//
//
//}
//
//class DeleteRecord extends PopUpWindow {
//
//}
//
//class EndSim extends PopUpWindow {}