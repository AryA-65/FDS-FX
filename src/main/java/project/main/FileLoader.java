package project.main;

import javafx.stage.FileChooser;

import java.io.File;

public class FileLoader {
    enum Type {
        RECORDING, OBSTACLE, STYLE, OTHER
    }

    private File file;
    private FileChooser fileChooser;

    FileLoader(Type type) {
        fileChooser = new FileChooser();
        File documentsFolder;
        switch (type) {
            case RECORDING -> {
                documentsFolder = new File(System.getProperty("user.home") + "/Documents/FDS-FX/Recordings");
                fileChooser.setInitialDirectory(documentsFolder);
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Recording", "*.bin"));
                fileChooser.setTitle("Select Recording");
                file = fileChooser.showOpenDialog(null);
            }
            case OBSTACLE -> {
                documentsFolder = new File(System.getProperty("user.home") + "/Documents/FDS-FX/Obstacles");
                fileChooser.setInitialDirectory(documentsFolder);
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Obstacle", "*.png"));
                fileChooser.setTitle("Select Obstacle");
                file = fileChooser.showOpenDialog(null);
            }
            case STYLE -> {
                documentsFolder = new File(System.getProperty("user.home") + "/Documents/FDS-FX/Styles");
                fileChooser.setInitialDirectory(documentsFolder);
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Custom Style", "*.css"));
                fileChooser.setTitle("Select Custom Style");
                file = fileChooser.showOpenDialog(null);
            }
            default -> {
                fileChooser.setTitle("Select File");
                file = fileChooser.showOpenDialog(null);
            }
        }
    }

    FileLoader() {
        try {
//            file = new File();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File returnFile() {
        return file;
    }

    public String returnFilePath() {
        return file.getAbsolutePath();
    }
}
