package com.teheidoma.twitter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import twitter4j.*;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Twitter twitter = new Twitter();
        VBox vBox = new VBox();
        TextField textField = new TextField();


        Button exitBtn = new Button("Выйти из аккаунта");
        exitBtn.setOnAction(e -> {
            twitter.logout();
            System.exit(1);
        });
        Hyperlink hyperlink = new Hyperlink("@teheidoma");
        hyperlink.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(URI.create("https://twitter.com/teheidoma"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        hyperlink.setFont(new Font(10));
        Pane space = new Pane();
        Pane space2 = new Pane();
        VBox.setVgrow(space2, Priority.ALWAYS);
        VBox vBox1 = new VBox(space2, hyperlink);
        HBox hBox = new HBox(exitBtn, space, vBox1);
        HBox.setHgrow(space, Priority.ALWAYS);
        vBox.getChildren().addAll(new Label("Введите ссылку на твит: "), textField, hBox);
        vBox.setPrefSize(300, 40);
        Group main = new Group(vBox);

        ImageView imageView = new ImageView();
        imageView.setImage(new Image(getClass().getClassLoader().getResourceAsStream("load.gif")));
        Group loading = new Group(imageView);

        Scene mainScene = new Scene(main);
        AtomicBoolean content = new AtomicBoolean(false);
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
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }
}
