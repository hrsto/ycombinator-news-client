package com.webarity.controllers.support;

import com.webarity.entities.Article;
import com.webarity.controllers.events.CommentOpenEvent;

import javafx.scene.control.TableCell;

/**
 * <p>Creates comment column cells. Uses the descendents property of a Message to display the number of comments. But the actual property that contains the comments is the kids property. When user clicks a comment cell, an event is fired that will open a new comment window</p>
 */
public class CommentColumnFactory extends TableCell<Article, Article> {

    public CommentColumnFactory() {
        intt();
    }

    @Override
    public void updateItem(Article item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null) return;
        setText("" + item.getDescendants());
    }

    private void intt() {
        setOnMouseClicked(e -> {

            fireEvent(new CommentOpenEvent(itemProperty().get()));

        });
    }

    
}