package com.nyampass.cabin.app;

import com.nyampass.cabin.WebSocket;
import gnu.expr.Language;
import gnu.expr.ModuleBody;
import gnu.mapping.Environment;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import kawa.standard.Scheme;
import netscape.javascript.JSException;

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

    private static Controller instance;
    private GraphicsContext graphicsContext;
    private WebSocket socket;

    public Controller() {
    }

    public static Controller instance() {
        return instance;
    }

    public GraphicsContext graphicsContext() {
        return this.graphicsContext;
    }

    public void initialize(URL location, ResourceBundle resources) {
        instance = this;

        this.graphicsContext = this.canvas.getGraphicsContext2D();
        this.socket = new WebSocket(this);

        setKeyEventTextArea(this.textArea);

        this.promotedCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.socket.sendRequestPromote(peerId, passwordField.getText());
            } else {
                this.socket.sendDemote(peerId);
            }
        });


    }

    private void eval() {
        try {
            Scheme scheme = Scheme.getInstance();
            Environment env = Scheme.builtin();

            Language.setDefaults(scheme);
            Environment.setGlobal(env);

            new Thread(() -> {
                ModuleBody.setMainPrintValues(true);
                try {
                    scheme.loadClass("com.nyampass.cabin.lang.SchemeBridge");
                    appendLog(scheme.eval(textArea.getText()).toString());
                } catch (Throwable e) {
                    appendLog(e);
                    e.printStackTrace();
                }

            }).start();

        } catch (JSException e) {
            appendLog(e);
        }
    }

    @SuppressWarnings("UnusedParameters")
    @FXML
    protected void onStart(ActionEvent event) {
        eval();
    }

    private static final DateFormat DATE_FORMATER = new SimpleDateFormat("HH:mm:ss");

    public void appendLog(String... texts) {
        String date = DATE_FORMATER.format(new Date());
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
                eval();
            }

        });

        textArea.setOnKeyReleased(event -> {
            pressed.remove(event.getCode());
        });
    }

    @Override
    public void handleMessage(String message) {

    }

    @Override
    public void appendLog(String log) {
        this.appendLog(new String[]{log});
    }

    @Override
    public void onSetPeerId(String peerId) {
        this.peerId = peerId;
        peerIdLabel.setText("" +
                "Id: " + peerId);
    }
}
