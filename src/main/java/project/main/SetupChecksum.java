package project.main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class SetupChecksum {
    protected enum OS{
        WINDOWS, LINUX
    }

    private SettingsManager settingsManager;
    private LinkedHashMap<String, String> settings = new LinkedHashMap<>();

    SetupChecksum(OS os) {
        settings.put("Height", "650");
        settings.put("Width", "800");

        if (os.equals(OS.WINDOWS)) {
            File appData = new File(System.getenv("APPDATA") + "\\FDS-FX");
            File appSettings = new File(System.getenv("APPDATA") + "\\FDS-FX\\settings.json");
            File fdsDir = new File(System.getProperty("user.home") + "/Documents/FDS-FX");
            File fdsObs = new File(System.getProperty("user.home") + "/Documents/FDS-FX/Obstacles");
            File fdsRec = new File(System.getProperty("user.home") + "/Documents/FDS-FX/Recordings");
            File fdsCss = new File(System.getProperty("user.home") + "/Documents/FDS-FX/Styles");

            try {
                settings.put("Style", fdsCss.getAbsolutePath() + "\\default.css");
            } catch (Exception e) {
                e.printStackTrace();
            }

            settingsManager = new SettingsManager(appSettings.getAbsolutePath());

            if (!checkExistence(appData)) {
                settingsManager.saveSettings(settings);
            }

            if (!appSettings.exists()) {
                settingsManager.saveSettings(settings);
            }

            if (!checkExistence(fdsDir)) {
                checkExistence(fdsObs);
                checkExistence(fdsRec);
                checkExistence(fdsCss);
            }
        } else {
            //no linux support for now

        }
    }

    private Boolean checkExistence(File file) {
        if (!file.exists()) {
            file.mkdir();
            return false;
        }
        return true;
    }

    public LinkedHashMap<String, String> getSettings() {
        return settings;
    }

    public void saveSettings() {
        settingsManager.saveSettings(settings);
        System.out.println("success");
    }

}

class SettingsManager {
    private final String SETTINGS_FILE;

    SettingsManager(String settingsDir) {
        SETTINGS_FILE = settingsDir;
        System.out.println(SETTINGS_FILE);
    }

    public HashMap<String, String> getSettings() {
        Gson gson = new Gson();
        try (FileReader fr = new FileReader(SETTINGS_FILE)) {
            Type type = new TypeToken<LinkedHashMap<String, String>>() {}.getType();
            return gson.fromJson(fr, type);
        } catch (IOException e) {
            e.printStackTrace();
            return new LinkedHashMap<>();
        }
    }

    public void saveSettings(LinkedHashMap<String, String> settings) {
        Gson gson = new Gson();
        try (FileWriter fw = new FileWriter(SETTINGS_FILE)) {
            gson.toJson(settings, fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
