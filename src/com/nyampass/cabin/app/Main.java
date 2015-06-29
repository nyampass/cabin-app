package com.nyampass.cabin.app;

import com.nyampass.cabin.Driver;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Random;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        setupStage(stage);
        stage.show();
    }

    private Stage setupStage(Stage stage) {
        VBox root;

        try {
            URL res = getClass().getClassLoader().getResource("app.fxml");
            assert res != null;
            root = FXMLLoader.load(res);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int i = 16; i <= 512; i *= 2) {
            stage.getIcons().add(new Image("images/icon_" + i + ".png"));
        }

        setupMenu(root);

        stage.setTitle("Cabin");
        stage.setScene(new Scene(root, 800, 600));
        return stage;
    }

    private void setupMenu(VBox root) {
        MenuBar menu = new MenuBar();
        menu.setUseSystemMenuBar(true);
        root.getChildren().add(menu);

        menu.getMenus().addAll(
                menu("ファイル", menuItem("新規ファイル", event -> {
                    try {
                        Stage stage = setupStage(new Stage());
                        stage.setX(new Random().nextInt(100));
                        stage.setY(new Random().nextInt(100));
                        stage.show();

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })));
    }

    private Menu menu(String name, MenuItem... menuItems) {
        Menu menu = new Menu(name);
        menu.getItems().addAll(menuItems);
        return menu;
    }

    private MenuItem menuItem(String name, EventHandler<ActionEvent> action) {
        MenuItem menuItem = new MenuItem(name);
        menuItem.setOnAction(action);
        return menuItem;
    }

    public static void main(String[] args) {
        Driver.load();
        launch(args);
    }
}
