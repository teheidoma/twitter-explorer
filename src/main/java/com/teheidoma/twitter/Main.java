package com.teheidoma.twitter;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import twitter4j.TwitterException;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    private static final float VERSION = 0.3f;
    private SceneManager sceneManager;

    public static void checkVersion() {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("https://raw.githubusercontent.com/teheidoma/twitter-explorer/master/build.gradle").openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            AtomicReference<Float> newVer = new AtomicReference<>((float) 0);
            reader.lines().forEach(l -> {
                if (l.startsWith("version")) {
                    newVer.set(Float.parseFloat(l.replaceAll("(version( *)')|(')", "")));
                }
            });
            if (VERSION < newVer.get())
                Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Доступна новая версия!").show());
        } catch (IOException e) {

        }
    }

    public static void main(String[] args) {
        new Main().start();
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                checkVersion();
            }
        }, 6000);
    }

    public void start() {
        try {
            final Twitter twitter = new Twitter();
            SceneManager.launchThis(() -> {
                this.sceneManager = SceneManager.getInstance();
                sceneManager.addScene("main", getMain(twitter));
                sceneManager.addScene("load", getLoad());
                sceneManager.changeScene("main");
            });
        } catch (TwitterException e) {
            new Alert(Alert.AlertType.ERROR, "Хм... похоже что-то произошло с твиттером");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Parent getLoad() {
        final ImageView imageView = new ImageView();
        imageView.setImage(new Image(getClass().getClassLoader().getResourceAsStream("load.gif")));
        return new Group(imageView);
    }

    private Parent getMain(Twitter twitter) {
        final VBox vBox = new VBox();
        final TextField textField = new TextField();
        final Button exitBtn = new Button("Выйти из аккаунта");
        final Hyperlink hyperlink = new Hyperlink("@teheidoma");
        final Pane hSpace = new Pane();
        final HBox hBox = new HBox(exitBtn, hSpace, hyperlink);
        final Group main = new Group(vBox);


        exitBtn.setOnAction(e -> {
            twitter.logout();
            System.exit(1);
        });
        hyperlink.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(URI.create("https://twitter.com/teheidoma"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        hyperlink.setFont(new Font(10));
        HBox.setHgrow(hSpace, Priority.ALWAYS);

        vBox.getChildren().addAll(new Label("Введите ссылку на твит: "), textField, hBox);
        vBox.setPrefSize(300, 40);


        textField.setOnKeyTyped(e -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) return;
            if (textField.getText().matches("(.*)twitter.com/(.*)/status/(\\d*)")) {
                String[] split = textField.getText().split("/");
                String tweetId = split[split.length - 1];
                sceneManager.changeScene("load");
                if (sceneManager.hasScene("tweet-" + tweetId)) {
                    sceneManager.changeScene("tweet-" + tweetId);
                    return;
                }
                Thread thread = new Thread(() -> {
                    Group value = null;
                    try {
                        value = new Group(twitter.getDiscussion(Long.parseLong(tweetId)).node());
                    } catch (TwitterException e1) {
                        e1.printStackTrace();
                        return;
                    }
                    Group finalValue = value;

                    Platform.runLater(() -> {
                        sceneManager.addScene("tweet-" + tweetId, finalValue);
                        sceneManager.changeScene("tweet-" + tweetId);
                    });

                });
                thread.start();
            }
        });
        return main;
    }
}
