package com.teheidoma.twitter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class SceneManager extends Application {
    private static StartCallback callback;
    private static SceneManager self;
    private Stage stage;
    private Scene scene;
    private Map<String, Parent> map;
    private String currentRoot;

    public SceneManager() {
        self = this;
        this.map = new HashMap<>();
    }

    public static void launchThis(StartCallback callback) {
        SceneManager.callback = callback;
        new Thread(() -> launch(SceneManager.class, (String[]) null)).start();
    }

    public static SceneManager getInstance() {
        return self;
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        this.scene = new Scene(new Group());
        this.stage.setScene(scene);
        this.stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("ico.png")));
        this.stage.setTitle("Twitter-explorer");

        this.scene.setOnKeyReleased(eee -> {
            if (eee.getCode().equals(KeyCode.ESCAPE)) {
                if (currentRoot.matches("tweet-(\\d*)")) {
                    changeScene("main");
                }
            }
        });
        callback.run();
    }

    public void addScene(String name, Parent root) {
        map.put(name, root);
    }

    public Scene getScene() {
        return scene;
    }

    public boolean changeScene(String name) {
        Parent parent = map.get(name);
        if (parent == null) return false;
        try {
            Platform.runLater(() -> {
                scene.setRoot(parent);
                stage.sizeToScene();
                stage.centerOnScreen();
                currentRoot = name;
                if (!stage.isShowing()) stage.show();
            });
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    private Stage getStage() {
        return stage;
    }

    public interface StartCallback {
        void run();
    }
}
