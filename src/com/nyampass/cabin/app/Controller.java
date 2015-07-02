package com.nyampass.cabin.app;

import com.nyampass.cabin.Driver;
import com.nyampass.cabin.Environ;
import com.nyampass.cabin.WebSocket;
import gnu.expr.Language;
import gnu.expr.ModuleBody;
import gnu.mapping.Environment;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import kawa.standard.Scheme;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class Controller implements Initializable, WebSocket.WebSocketHandler {
    @FXML
    Button startButton;
    @FXML
    ImageView startButtonImageView;
    @FXML
    TextArea textArea;
    @FXML
    TextArea consoleArea;
    @FXML
    Label peerIdLabel;
    @FXML
    CheckBox promotedCheckbox;
    @FXML
    PasswordField passwordField;
    @FXML
    Canvas canvas;

    private String peerId;

    private GraphicsContext graphicsContext;
    private WebSocket socket;

    public Controller() {
    }

    public void initialize(URL location, ResourceBundle resources) {
        this.graphicsContext = this.canvas.getGraphicsContext2D();
        this.socket = new WebSocket(this);

        setKeyEventTextArea(this.textArea);

        this.promotedCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                textArea.setDisable(true);
                this.socket.sendPromote(peerId, passwordField.getText());
            } else {
                this.socket.sendDemote(peerId);
                textArea.setDisable(false);
            }
        });


    }

    private Thread evalThread = null;

    private void setStartButtonImage(boolean canStart) {
        startButtonImageView.setImage(new Image(canStart? "images/flag.png": "images/stop.png"));
    }

    private void evalScheme() {
        if (evalThread != null) {
            setStartButtonImage(false);

            evalThread.stop();
            evalThread = null;

            return;
        }

        setStartButtonImage(false);

        Scheme scheme = Scheme.getInstance();
        Environment env = Scheme.builtin();

        Language.setDefaults(scheme);
        Environment.setGlobal(env);
        ModuleBody.setMainPrintValues(true);

        this.evalThread = new Thread(() -> {
            try {
                Environ environ = Environ.instance();
                environ.peerId = peerId;
                environ.socket = socket;
                environ.graphicsContext = graphicsContext;
                scheme.loadClass("com.nyampass.cabin.lang.SchemeBridge");
                appendLog(scheme.eval(textArea.getText()).toString());

            } catch (Throwable e) {
                setStartButtonImage(true);
                this.evalThread = null;

                Driver.destroy();
            }
        });
        this.evalThread.start();
    }

    @SuppressWarnings("UnusedParameters")
    @FXML
    protected void onStart(ActionEvent event) {
        evalScheme();
    }

    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("HH:mm:ss");

    public void appendLog(String... texts) {
        String date = DATE_FORMATTER.format(new Date());
        for (String text : texts) {
            consoleArea.appendText("[" + date + "] " + text + "\n");
        }
    }

    public void appendLog(Throwable e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);

        appendLog(writer.toString());
    }

    public void setKeyEventTextArea(TextArea textArea) {
        final Map<KeyCode, Boolean> pressed = new HashMap<>();

        textArea.setOnKeyPressed(event -> {
            pressed.put(event.getCode(), true);

            if (pressed.containsKey(KeyCode.COMMAND) && pressed.containsKey(KeyCode.ENTER)) {
                evalScheme();
            }
        });

        textArea.setOnKeyReleased(event -> pressed.remove(event.getCode()));
    }

    @Override
    public void handleCommand(WebSocket.Request command) {
        Object ret = Driver.dispatch(command.klass, command.command, command.args);

        this.socket.sendResult(command.id, command.to, command.from, ret);
    }

    @Override
    public void appendLog(String log) {
        this.appendLog(new String[]{log});
    }

    @Override
    public void onSetPeerId(String peerId) {
        Platform.runLater(() -> {
            Controller.this.peerId = peerId;
            peerIdLabel.setText("" +
                    "Id: " + peerId);
        });
    }
}
