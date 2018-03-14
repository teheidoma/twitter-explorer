package com.teheidoma.twitter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
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
import javafx.stage.Stage;
import twitter4j.TwitterException;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Twitter twitter = new Twitter();
        final VBox vBox = new VBox();
        final TextField textField = new TextField();


        final Button exitBtn = new Button("Выйти из аккаунта");
        final Hyperlink hyperlink = new Hyperlink("@teheidoma");
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
        final Pane hSpace = new Pane();
        HBox.setHgrow(hSpace, Priority.ALWAYS);
        HBox hBox = new HBox(exitBtn, hSpace, hyperlink);
        vBox.getChildren().addAll(new Label("Введите ссылку на твит: "), textField, hBox);
        vBox.setPrefSize(300, 40);
        final Group main = new Group(vBox);

        final ImageView imageView = new ImageView();
        imageView.setImage(new Image(getClass().getClassLoader().getResourceAsStream("load.gif")));
        final Group loading = new Group(imageView);

        final Scene mainScene = new Scene(main);
        final AtomicBoolean content = new AtomicBoolean(false);
        textField.setOnAction(e -> {
            if (textField.getText().matches("(.*)twitter.com/(.*)/status/(\\d*)")) {
                String[] split = textField.getText().split("/");
                mainScene.setRoot(loading);
                primaryStage.sizeToScene();
                primaryStage.centerOnScreen();
                Thread thread = new Thread(() -> {
                    Group value = null;
                    try {
                        value = new Group(twitter.getDiscussion(Long.parseLong(split[split.length - 1])).node());
                    } catch (TwitterException e1) {
                        e1.printStackTrace();
                    }
                    Group finalValue = value;
                    Platform.runLater(() -> {
                        mainScene.setRoot(finalValue);
                        primaryStage.sizeToScene();
                        primaryStage.centerOnScreen();
                        primaryStage.setTitle("Twitter-explorer");
                        content.set(true);
                    });

                });
                thread.start();
            }
        });
        mainScene.setOnKeyReleased(eee -> {
            if (eee.getCode().equals(KeyCode.ESCAPE) && content.get()) {
                mainScene.setRoot(main);
                primaryStage.sizeToScene();
                primaryStage.centerOnScreen();
                content.set(false);
            }
        });
        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("ico.png")));
        primaryStage.setTitle("Twitter-explorer");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }
}
