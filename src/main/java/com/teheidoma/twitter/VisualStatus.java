package com.teheidoma.twitter;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import twitter4j.MediaEntity;
import twitter4j.Status;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VisualStatus {
    private Status status;
    private List<VisualStatus> child;
    private boolean first;

    VisualStatus(Status status, VisualStatus... child) {
        this.status = status;
        this.child = new ArrayList<>();
        Collections.addAll(this.child, child);
    }

    private void addChild(VisualStatus status) {
        if (status==null)return;
        child.add(status);
    }

    public void addChild(VisualStatus... statuses){
        if (statuses==null)return;
        for (VisualStatus visualStatus : statuses) {
            addChild(visualStatus);
        }
    }

    public Status getStatus() {
        return status;
    }

    public void setFirst(){
        this.first=true;
    }

    @SuppressWarnings("unused")
    public List<VisualStatus> getChild() {
        return child;
    }

    public Node node(){
        VBox vBox1 = new VBox();
        for (VisualStatus m: child) {
            vBox1.getChildren().add(m.node());
        }

        vBox1.setPadding(new Insets(0.0, 0.0, 0.0, 10));

        Label label = new Label(status.getUser().getScreenName() + ": " + status.getText().replace("\n", " "));

        label.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                if (status.getMediaEntities().length>0) {
                    for (MediaEntity mediaEntity : status.getMediaEntities()) {
                        Stage stage = new Stage();
                        Group root = new Group();
                        Image image = new Image(mediaEntity.getMediaURL());
                        ImageView e1 = new ImageView(image);
                        double x = image.getHeight()/500f;
                        e1.setFitHeight(image.getHeight()/x);
                        e1.setFitWidth(image.getWidth()/x);
                        root.getChildren().add(e1);
                        Scene value = new Scene(root);
                        value.setOnKeyReleased(eee ->{
                            if(eee.getCode().equals(KeyCode.ESCAPE)){
                                stage.close();
                            }
                        });
                        stage.setScene(value);
                        stage.show();
                    }

                }
            }else {
                try {
                    Desktop.getDesktop().browse(URI.create("https://twitter.com/" + status.getUser().getScreenName() + "/status/" + status.getId()));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        VBox vBox = new VBox(label, vBox1);
        label.setOnMouseEntered(e -> vBox.setEffect(new Glow(0.5)));
        label.setOnMouseExited(e -> {
            vBox.setEffect(null);
//                vBox.setEffect(new Shadow(0,0));
        });
        HBox hBox = new HBox();
        if (!first) {
            Separator separator = new Separator(Orientation.VERTICAL);
            separator.setEffect(new Glow(0.5));
            hBox.getChildren().add(separator);
        }
        hBox.getChildren().add(vBox);
        return hBox;
    }
}