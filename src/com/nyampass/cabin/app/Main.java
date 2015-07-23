package com.nyampass.cabin.app;

import com.nyampass.cabin.Driver;
import com.nyampass.cabin.Environ;
import com.nyampass.cabin.WebSocket;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
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
                                    seControllerSource(controller, defaultSample);
                                    setStageRandomPosition(astage);
                                }).show();

                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }, new KeyCodeCombination(KeyCode.N, KeyCombination.META_DOWN)),
                        menu("サンプルから読み込む",
                                sampleMenus.toArray(new MenuItem[0]))),
                menu("ツール",
                        menuItem("サーバ設定", event -> {
                            createServerSettingDialog();
                        })));
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

    private void createServerSettingDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("サーバ設定");
        dialog.setHeaderText("サーバ名・パスワードを設定して下さい。");

        // Set the button types.
        ButtonType settingButtonType = new ButtonType("設定", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("キャンセル", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(settingButtonType, cancelButtonType);

        // Create the customName and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField();
        name.setPromptText("サーバ名");
        PasswordField password = new PasswordField();
        password.setPromptText("パスワード");

        grid.add(new Label("サーバ名: "), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("パスワード: "), 0, 1);
        grid.add(password, 1, 1);

        // Enable/Disable login button depending on whether a customName was entered.
        Node settingButton = dialog.getDialogPane().lookupButton(settingButtonType);
        settingButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        name.textProperty().addListener((observable, oldValue, newValue) -> {
            settingButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        // Request focus on the customName field by default.
        Platform.runLater(() -> name.requestFocus());

        // Convert the result to a customName-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == settingButtonType) {
                return new Pair<>(name.getText(), password.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(customNamePassword -> {
            String inputName = customNamePassword.getKey();
            String inputPassword = customNamePassword.getValue();
            Environ environ = Environ.instance();
            if (inputName != null && !inputName.equals("") && inputPassword != null && !inputPassword.equals("")) {
                environ.customName = customNamePassword.getKey();
                environ.customNamePassword = customNamePassword.getValue();
            } else {
                environ.customName = null;
                environ.customNamePassword = null;
            }
        });
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
