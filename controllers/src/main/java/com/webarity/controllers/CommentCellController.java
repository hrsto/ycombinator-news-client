package com.webarity.controllers;

import java.io.IOException;
import java.util.Date;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * <p>A comment cell is a row, this sets the inner contents from the fxml file.</p>
 */
public class CommentCellController extends VBox {

    @FXML Label commentText;
    @FXML Label author;
    @FXML Label dateCreated;    
    
    public CommentCellController() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CommentCell.fxml"));

        this.getStylesheets().add("/css/main.css");
        this.getStylesheets().add("/css/comments.css");

        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    // public WebView getCommentText() { return commentText; }

    public void setComment(String comment) {
        commentText.setText(comment);
    }
    public void setAuthor(String by) {
        author.setText(by);
    }
    @SuppressWarnings("deprecation")
    public void setDateCreated(Date created) {
        if (created == null) dateCreated.setText("");
        else dateCreated.setText(created.toLocaleString());
    }
}