package com.webarity.controllers.events;

import com.webarity.entities.Article;

import javafx.event.EventType;

/**
 * <p>Fired when comment window is requested to be loaded</p>
 */
public class CommentOpenEvent extends NewsStateEvenType {

    private static final long serialVersionUID = 1L;

    public static final EventType<CommentOpenEvent> COMMENT_OPEN_EVENT = new EventType<>(NewsStateEvenType.NEWS_STATE_EVENT, "COMMENT_OPEN_EVENT");

    private Article article;

    public CommentOpenEvent(Article article) {
        super(COMMENT_OPEN_EVENT);

        this.article = article;
    }
    
    public Article getMessageID() {
        return article;
    }
}