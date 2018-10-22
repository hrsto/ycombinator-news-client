package com.webarity.controllers;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * <p>The titlebar for a comment window, this sets the inner contents from the fxml file.</p>
 */
public class CommentTitle extends HBox {

    @FXML Label commentTitle;

    @FXML Region commentMinimizeButton;
    @FXML Region commentMaximizeButton;
    @FXML Region commentCloseButton;

    public CommentTitle() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CommentTitle.fxml"));

        this.getStylesheets().add("/css/comments.css");
        this.getStylesheets().add("/css/main.css");
        
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public CommentTitle(String title) {
        this();

        commentTitle.setText(title);
        
        commentCloseButton.setOnMouseClicked(evtt -> ((Stage)this.getScene().getWindow()).close());
        commentMaximizeButton.setOnMouseClicked(evtt -> {
            if (stageRef().isMaximized()) stageRef().setMaximized(false);
            else stageRef().setMaximized(true);
        });
        commentMinimizeButton.setOnMouseClicked(evtt -> stageRef().setIconified(true));
    }

    private Stage stageRef() {
        return (Stage)this.getScene().getWindow();
    }
}
