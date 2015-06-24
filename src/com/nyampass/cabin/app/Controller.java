package com.nyampass.cabin.app;

import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.IntFunction;

public class Controller implements Initializable {
    @FXML
    Button startButton;
    @FXML
    WebView web;
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

    private static Controller instance;

    public static Controller instance() {
        return instance;
    }

    public void initialize(URL location, ResourceBundle resources) {
        instance = this;

        final WebEngine webEngine = web.getEngine();

        final URL html = getClass().getResource("/client.html");
        webEngine.load(html.toExternalForm());

        webEngine.setOnAlert(event -> System.out.println("alert:" + event.getData()));

        webEngine.getLoadWorker().stateProperty().addListener(
                (ov, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        JSObject window = (JSObject) webEngine.executeScript("window");

                        window.setMember("app", new JSBridge());
                        webEngine.executeScript("console.log = function(message)　{app.log(message);};");
                    }
                });

        promotedCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            webEngine.executeScript("window.onPromotedChanged(" + Boolean.toString(newValue) + ", '" +
                    passwordField.getText() + "');");
        });
    }

    @SuppressWarnings("UnusedParameters")
    @FXML
    protected void onStart(ActionEvent event) {
        try {
            this.web.getEngine().executeScript(textArea.getText());
        } catch (JSException e) {
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            e.printStackTrace(printWriter);

            appendLog(writer.toString());
        }
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
        web.getEngine().executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");

    }
}
