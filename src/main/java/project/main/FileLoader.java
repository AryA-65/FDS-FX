package project.main;

import javafx.stage.FileChooser;

import java.io.File;

public class FileLoader {
    private enum Type {OBSTACLE, RECORDING, OTHER}

    private Type fileType;
    private String filePath;
    private File file;

    public FileLoader(Type fileType, String filePath) {
        this.fileType = fileType;
        this.filePath = filePath;
    }

    public Type getFileType() {
        return fileType;
    }

    public File getFile() {
        return file;
    }

    public void loadFile() {
        FileChooser fileChooser = new FileChooser();
        switch (fileType) {
            case OBSTACLE -> {
                fileChooser.setTitle("Load Obstacle");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Obstacle", "*.PNG"));
            }
            case RECORDING -> {
                fileChooser.setTitle("Load Recording");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Recording", "*.bin"));
            }
            case OTHER -> fileChooser.setTitle("Load Other");
            default -> fileChooser.setTitle("Load File");
        }

        file = fileChooser.showOpenDialog(null);
    }
}
