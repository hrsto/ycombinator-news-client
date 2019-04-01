package com.webarity.news;

import java.io.File;

import com.webarity.controllers.MainStage;
import com.webarity.controllers.events.CommentOpenEvent;
import com.webarity.controllers.events.ElementReadEvent;
import com.webarity.controllers.events.InitDatabaseEvent;
import com.webarity.controllers.events.LinkOpenEvent;
import com.webarity.controllers.events.NewsStateEvenType;
import com.webarity.controllers.events.PopulateTableEvent;
import com.webarity.entities.Article;
import com.webarity.news.utils.State;
import com.webarity.news.utils.Tools;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;



/**
 * 
 */
public class Main extends Application {

    private State state = State.get();
    private HostServices hs = null;

    public Main() {

    }

    @Override
    public void init() {
    }

    @Override
    public void stop() {
        state.cleanUp();
    }
    
    @Override
    public void start(Stage s) {
        hs = getHostServices();

        state.setMainStage(s);

        MainStage mainStage = new MainStage(s);
        Scene scene = new Scene(mainStage, 700, 400);
        scene.getStylesheets().add("/css/main.css");
        scene.setFill(null);

        state.setStatusMessage(mainStage.getStatusMessage());
        state.setProgressTracker(mainStage.getProgressTrack());
        state.setStoriesCount(mainStage.getStoriesCount());
        state.setIsDBConnected(mainStage.getIsDBActiveProperty());

        s.initStyle(StageStyle.TRANSPARENT);
        s.setTitle("YNEWS COMBINATOR");
        s.setScene(scene);
        s.show();

        scene.addEventHandler(NewsStateEvenType.NEWS_STATE_EVENT, event -> {
            if (event instanceof PopulateTableEvent) {

                PopulateTableEvent popualteTableEvent  = (PopulateTableEvent)event;

                long count = popualteTableEvent.getCount();
                if (count == 0) {
                    mainStage.getStories().clear();
                    return;
                }
        
                state.jpa(em -> {

                    return em.createNamedQuery("getAllArticles", Article.class).setMaxResults((int)count).getResultList();
                    
                }).thenAcceptAsync((storiesToShow) -> {
                    Platform.runLater(() -> {
                        mainStage.getStories().clear();
                        mainStage.getStories().addAll(storiesToShow);
                        mainStage.refreshTable();
                    });
                }, Tools.commentExecutor);

            } else if (event instanceof ElementReadEvent) {

                ElementReadEvent readEvent  = (ElementReadEvent)event;

                state.jpa((art, em) -> {
                    em.getTransaction().begin();
                    Article a = readEvent.getArticle();
                    a.setRead(true);
                    em.getTransaction().commit();
                    return a;
                }, readEvent.getArticle()).thenAccept((a)-> {
                    Platform.runLater(() -> {
                        state.setMessage(String.format("Read %s", a.getTitle()));
                    });
                });

            } else if (event instanceof CommentOpenEvent) {

                CommentOpenEvent commentOpenEvent  = (CommentOpenEvent)event;
                Article temp = commentOpenEvent.getMessageID();

                if (temp.getKids() == null || temp.getKids().size() == 0) {
                    Task<Article> commentTask = state.getComments(temp);
                    Article tempArticle = temp;

                    commentTask.setOnSucceeded(evt -> {
                        state.setMessage(String.format("Comments fetched for %s", tempArticle.getTitle()));
                        state.getProgressTracker().set(0);
                        state.jpa(em -> {
                            em.refresh(tempArticle);
                        }).thenRun(() -> Platform.runLater(()->mainStage.initPopup(tempArticle)));
                    });
                    commentTask.messageProperty().addListener((obs, old, newish) -> {
                        state.setMessage(newish);
                    });
                    commentTask.progressProperty().addListener((obs, old, newish) -> {
                        state.getProgressTracker().set(newish.doubleValue());
                    });
                    commentTask.setOnFailed(evt -> {
                        state.setMessage(String.format("Comments failed fetching for %s", tempArticle.getTitle()));
                        state.getProgressTracker().set(0D);
                        evt.getSource().getException().printStackTrace();
                    });
                } else mainStage.initPopup(temp);

            } else if (event instanceof LinkOpenEvent) {

                LinkOpenEvent linkOpenEvent = (LinkOpenEvent)event;
                hs.showDocument(linkOpenEvent.getLink().toString());

            } else if (event instanceof InitDatabaseEvent) {

                File f = null;
                String path = null;
                if (System.getenv("APPDATA") != null) path = System.getenv("APPDATA");
                else if (System.getenv("TEMP") != null) path = System.getenv("TEMP");
                else if (System.getenv("TMP") != null) path = System.getenv("TMP");
                else {
                    DirectoryChooser openFolderDialog = new DirectoryChooser();
                    openFolderDialog.setTitle("Select location for data files");
                    f = openFolderDialog.showDialog(s);
                }
                
                if (path != null) f = new File(path, "YCombinatorNews/");
                if (f != null) {
                    state.createConnection(f.getPath().concat(File.separator).concat(f.getName()));
                } else {
                    state.setMessage("COULDN'T SET DATA LOCATION DIR.");
                }
            }
        });

        mainStage.fireEvent(new InitDatabaseEvent());
    }

    public static void main(String[] args){
        launch(args);
    }
}
