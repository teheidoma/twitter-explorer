package com.teheidoma.twitter;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Twitter {
    private twitter4j.Twitter twitter;
    private final Saver saver;

    Twitter() throws TwitterException, IOException {
        twitter4j.Twitter twitter = twitter4j.TwitterFactory.getSingleton();
        twitter.setOAuthConsumer("7Lr8A9PEWprzhZLKsYO5C8y6S", "rrO5obfVyuBs5zQlOe86hY3cbADD1D4BZzQj2ohYt4S3MjDMNz");
        saver = new Saver();
        AccessToken accessToken = null;
        if (saver.isExists()) {
            accessToken = new AccessToken(saver.get("token"), saver.get("secret"), Long.parseLong(saver.get("id")));
        } else {
            final RequestToken requestToken = twitter.getOAuthRequestToken();
            Desktop.getDesktop().browse(URI.create(requestToken.getAuthorizationURL()));
            final AtomicBoolean atomicBoolean = new AtomicBoolean(true);
            while (atomicBoolean.get()) {
                try {
                    final TextInputDialog textInputDialog = new TextInputDialog();
                    textInputDialog.setOnCloseRequest(e -> {
                        if (textInputDialog.getResult() == null || textInputDialog.getResult().isEmpty()) {
                            System.exit(1);
                        }
                    });
                    textInputDialog.getDialogPane().setHeaderText("Введите pin");
                    textInputDialog.showAndWait();
                    accessToken = twitter.getOAuthAccessToken(requestToken, textInputDialog.getResult());
                    atomicBoolean.set(false);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            assert accessToken != null;
            saver.add("token", accessToken.getToken());
            saver.add("secret", accessToken.getTokenSecret());
            saver.add("id", String.valueOf(accessToken.getUserId()));
            saver.save();
            final Alert alert = new Alert(Alert.AlertType.INFORMATION, "Для выхода на предыдущее окно используйте Escape.\n" +
                    "В режиме просмотра твита можно нажать левой клавишой мыши, для открытия картинки в твите\n" +
                    "или правой для открытия в бразуре.", ButtonType.OK);
            alert.getDialogPane().setHeaderText("Краткий экскурс");
            final ImageView graphic = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("hello.png"), 300, 300, false, false));
            alert.getDialogPane().setGraphic(graphic);
            alert.setTitle("Краткий экскурс");
            alert.showAndWait();
        }
        twitter.setOAuthAccessToken(accessToken);
        this.twitter = twitter;
    }

    public void logout() {
        saver.delete();
    }

    public VisualStatus getDiscussion(long tweet) throws TwitterException {
        final VisualStatus visualStatus = new VisualStatus(twitter.tweets().showStatus(tweet));
        visualStatus.addChild(getChildDiscussion(visualStatus.getStatus().getId()));
        visualStatus.setFirst();
        return visualStatus;
    }

    private VisualStatus[] getChildDiscussion(long tweet) throws TwitterException {
        final List<Status> statusAfter = getStatusAfter(tweet);
        if (statusAfter.size() == 0) return null;

        if (statusAfter.size() == 1) {
            final VisualStatus visualStatus = new VisualStatus(statusAfter.get(0));
            visualStatus.addChild(getChildDiscussion(statusAfter.get(0).getId()));
            return new VisualStatus[]{visualStatus};
        } else {
            final VisualStatus[] visualStatuses = new VisualStatus[statusAfter.size()];
            for (int i = 0; i < statusAfter.size(); i++) {
                VisualStatus status1 = new VisualStatus(statusAfter.get(i));
                visualStatuses[i] = status1;
                status1.addChild(getChildDiscussion(status1.getStatus().getId()));
            }
            return visualStatuses;
        }
    }

    private List<Status> getStatusAfter(Status status) throws TwitterException {
        final List<Status> list = new ArrayList<>();
        Query query = new Query("to:" + status.getUser().getScreenName() + " since_id:" + status.getId());
        QueryResult results;
        do {
            results = twitter.search(query);
            List<Status> tweets = results.getTweets();

            for (Status tweet : tweets)
                if (tweet.getInReplyToStatusId() == status.getId())
                    list.add(tweet);
        } while ((query = results.nextQuery()) != null);
        return list;
    }

    private List<Status> getStatusAfter(long statusId) throws TwitterException {
        return getStatusAfter(twitter.tweets().showStatus(statusId));
    }


}