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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main extends Application {

    private EventHandler<ActionEvent> eventHandler = event -> {
        MenuItem menuItem = (MenuItem) event.getSource();

        File file = (File) menuItem.getUserData();
        setupStage(astage -> controller -> {
            seControllerSource(controller, file);
            setStageRandomPosition(astage);
        }).show();
    };

    @Override
    public void start(Stage stage) throws Exception {
        setupStage(stage, aStage -> aController -> {
            seControllerSource(aController, defaultSample);
        });
        stage.show();
    }

    private Stage setupStage(Function<Stage, Consumer<Controller>> consumer) {
        return setupStage(new Stage(), consumer);
    }

    private Stage setupStage(Stage stage, Function<Stage, Consumer<Controller>> consumer) {
        URL res = getClass().getResource("/app.fxml");
        assert res != null;
        FXMLLoader loader = new FXMLLoader(res);

        VBox root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int i = 16; i <= 512; i *= 2) {
            stage.getIcons().add(new Image("images/icon_" + i + ".png"));
        }

        setupMenu(root);

        stage.setTitle("Cabin");
        stage.setScene(new Scene(root, 800, 600));

        if (consumer != null)
            consumer.apply(stage).accept(loader.getController());

        return stage;
    }

    private File file(URL url) {
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private final File defaultSample = file(getClass().getResource("/samples/default.scm"));

    private List<File> sampleFiles() {
        URL samplesURL = getClass().getResource("/samples/");
        if (samplesURL == null)
            return new ArrayList<>();
        return Arrays.asList(file(samplesURL).listFiles()).stream().
                filter(f -> !f.equals(defaultSample)).collect(Collectors.toList());
    }

    private MenuItem file2menu(File file) {
        MenuItem menuItem = menuItem(file.getName(), null);

        menuItem.setUserData(file);
        menuItem.setOnAction(eventHandler);

        return menuItem;
    }

    @FunctionalInterface
    public interface Consumer<StageAndController> {
        public void accept(StageAndController stageAndController);
    }

    private void setupMenu(VBox root) {
        MenuBar menu = new MenuBar();
        menu.setUseSystemMenuBar(true);
        root.getChildren().add(menu);

        List<File> sampleFIles = sampleFiles();


        List<MenuItem> sampleMenus = sampleFiles().stream().
                map(amenu -> file2menu(amenu)).collect(Collectors.toList());

        menu.getMenus().addAll(
                menu("ファイル",
                        menuItem("新規ファイル", event -> {
                            try {
                                setupStage(new Stage(), astage -> controller -> {
                                    setStageRandomPosition(astage);
                                }).show();

                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }, new KeyCodeCombination(KeyCode.N, KeyCombination.META_DOWN)),
                        menu("サンプルから読み込む",
                                sampleMenus.toArray(new MenuItem[0]))));
    }

    private static Menu menu(String name, MenuItem... menuItems) {
        Menu menu = new Menu(name);
        menu.getItems().addAll(menuItems);
        return menu;
    }

    private static MenuItem menuItem(String name, EventHandler<ActionEvent> action) {
        return menuItem(name, action, null);
    }

    private static MenuItem menuItem(String name, EventHandler<ActionEvent> action, KeyCombination shortcutKey) {
        MenuItem menuItem = new MenuItem(name);
        menuItem.setOnAction(action);
        menuItem.setAccelerator(shortcutKey);
        return menuItem;
    }

    public static void main(String[] args) {
        Driver.load();
        launch(args);
    }

    public void setStageRandomPosition(Stage stage) {
        stage.setX(new Random().nextInt(100));
        stage.setY(new Random().nextInt(100));
    }

    public void seControllerSource(Controller controller, File file) {
        try {
            controller.setSourceCode(new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
