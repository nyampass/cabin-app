package com.nyampass.cabin.app;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL res = getClass().getClassLoader().getResource("app.fxml");
        VBox root = FXMLLoader.load(res);

        for (int i = 16; i <= 512; i *= 2) {
            stage.getIcons().add(new Image("images/icon_" + i + ".png"));
        }

        setupMenu(root);

        stage.setTitle("Cabin");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    private void setupMenu(VBox root) {
        MenuBar menu = new MenuBar();
        menu.setUseSystemMenuBar(true);
        root.getChildren().add(menu);

        final Controller controller = Controller.instance();

        menu.getMenus().addAll(
                menu("ファイル", menuItem("新規ファイル", event -> {

                })),
                menu("ツール", menuItem("デバッグ", event -> {
                    controller.onDebug();
                }))
        );
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
        launch(args);
    }
}
