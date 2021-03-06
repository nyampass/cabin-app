package com.nyampass.cabin.app;

import com.nyampass.cabin.Driver;
import com.nyampass.cabin.Environ;
import com.nyampass.cabin.WebSocket;
import com.nyampass.cabin.lang.SchemeBridge;
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

    private boolean scriptDisabled;

    public Controller() {
    }

    public void initialize(URL location, ResourceBundle resources) {
        this.graphicsContext = this.canvas.getGraphicsContext2D();
        this.socket = new WebSocket();
        this.scriptDisabled = false;

        socket.addHandler(this);
        setKeyEventTextArea(this.textArea);

        this.promotedCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                scriptDisabled = true;
                textArea.setDisable(true);
                this.socket.sendPromote(peerId, passwordField.getText());
                clearLog();
            } else {
                scriptDisabled = false;
                this.socket.sendDemote(peerId);
                textArea.setDisable(false);
            }
        });


    }

    private Thread evalThread = null;

    private void setStartButtonImage(boolean canStart) {
        startButtonImageView.setImage(new Image(canStart ? "images/flag.png" : "images/stop.png"));
    }

    private void evalScheme() {
        if (scriptDisabled) {
            return;
        }

        if (evalThread != null) {
            setStartButtonImage(false);

            evalThread.stop();
            evalThread = null;

            return;
        }

        clearLog();
        setStartButtonImage(false);

        SchemeBridge langBridge = new SchemeBridge();

        this.evalThread = new Thread(() -> {
            try {
                Environ environ = Environ.instance();
                environ.peerId = peerId;
                environ.socket = socket;
                environ.graphicsContext = graphicsContext;

                langBridge.eval(textArea.getText()).toString();

                appendLog();

            } catch (Throwable e) {
                setStartButtonImage(true);
                this.evalThread = null;

                if (!(e instanceof ThreadDeath))
                    appendLog(e);

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

    public void clearLog() {
        consoleArea.clear();
    }

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

    private boolean isEventListeningRequest(WebSocket.Request command) {
        return command.command.startsWith("on");
    }

    @Override
    public void handleCommand(WebSocket.Request command) {
        if (isEventListeningRequest(command)) {
            Driver.addEventListener(command.klass, command.command, command.args, value -> {
                this.socket.sendEvent(command.id, command.to, command.from, value);
            });
        } else {
            Object ret = Driver.dispatch(command.klass, command.command, command.args);
            this.socket.sendResult(command.id, command.to, command.from, ret);
        }
    }

    @Override
    public void handleEvent(WebSocket.Response event) {
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

    public void setSourceCode(String sourceCode) {
        this.textArea.setText(sourceCode);
    }
}
