package com.example.services;

import javafx.scene.image.Image;
import javafx.stage.Stage;

public class WindowIcon {
    private static Image icon;
    public static void setIcon(Stage stage) {
        try{
            icon = new Image(WindowIcon.class.getResourceAsStream("/com/example/bearsfrontend/images/icon.png"));
            stage.getIcons().clear();
            stage.getIcons().add(icon);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static void setIcon(Stage stage, String path) {
        try{
            Image icon = new Image(WindowIcon.class.getResourceAsStream(path));
            stage.getIcons().clear();
            stage.getIcons().add(icon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
