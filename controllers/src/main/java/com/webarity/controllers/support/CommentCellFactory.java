package com.webarity.controllers.support;

import com.webarity.entities.Comment;
import com.webarity.controllers.CommentCellController;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;

/**
 * <p>Creates comment cells</p>
 * @param <T>
 */
public class CommentCellFactory<T> extends TreeCell<T> {

    TreeView<T> treeView = null;
    CommentCellController commentCellController = null;

    public CommentCellFactory(CommentCellController commentCellController, TreeView<T> treeView) {
        this.commentCellController = commentCellController;
        this.treeView = treeView;

        this.getStylesheets().add("/css/comments.css");
        this.prefWidthProperty().bind(treeView.widthProperty().subtract(15));
    }

    @Override
    public void updateItem(T it, boolean empty) {
        super.updateItem(it, empty);
        
        if (empty || it == null) {
            setText(null);
            setGraphic(null);
            return;
        }
        if (it instanceof Comment) {
            Document d = Jsoup.parse(((Comment)it).getText());
            commentCellController.setComment(d.text());
            commentCellController.setDateCreated(((Comment)it).getCreated());
            commentCellController.setAuthor(((Comment)it).getAuthor());
            setGraphic(commentCellController);
        } else {
            setText(null);
            setGraphic(null);
        }
    }
}