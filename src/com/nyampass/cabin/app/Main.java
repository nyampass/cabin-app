package com.nyampass.cabin.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        URL res = getClass().getClassLoader().getResource("app.fxml");
        Parent root = FXMLLoader.load(res);

        for (int i = 16; i <= 512; i *= 2) {
            primaryStage.getIcons().add(new Image("icon_" + i + ".png"));
        }

        primaryStage.setTitle("cabin");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
