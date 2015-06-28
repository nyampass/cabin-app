package com.nyampass.cabin.app;

import com.nyampass.cabin.WebSocket;
import gnu.expr.KawaScriptEngine;
import gnu.expr.Language;
import gnu.expr.ModuleBody;
import gnu.lists.FString;
import gnu.lists.LList;
import gnu.mapping.Environment;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import kawa.standard.Scheme;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

import javax.script.*;
import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;

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

    private static Controller instance;
    private JSObject windowObject;
    private GraphicsContext graphicsContext;
    private TextArea keyEventTextArea;
    private WebSocket socket;

    public static Controller instance() {
        return instance;
    }

    public GraphicsContext graphicsContext() {
        return this.graphicsContext;
    }

    public void initialize(URL location, ResourceBundle resources) {
        instance = this;

        this.graphicsContext = this.canvas.getGraphicsContext2D();

        setKeyEventTextArea(this.textArea);

        this.socket = new WebSocket(this);

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

    public void setPeerId(String peerId) {
        peerIdLabel.setText("" +
                "Id: " + peerId);
    }

    public void onDebug() {
        // web.getEngine().executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
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

            if (pressed.get(KeyCode.COMMAND) && pressed.get(KeyCode.ENTER)) {
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
}
