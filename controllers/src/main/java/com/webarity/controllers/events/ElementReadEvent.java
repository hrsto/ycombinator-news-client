package com.webarity.controllers.events;

import com.webarity.entities.Article;

import javafx.event.EventType;

/**
 * <p>Fired when an {@link Article} has been marked as read</p>
 */
public class ElementReadEvent extends NewsStateEvenType {

    private static final long serialVersionUID = 1L;

    public static final EventType<ElementReadEvent> READ_EVENT = new EventType<>(NewsStateEvenType.NEWS_STATE_EVENT, "READ_EVENT");

    private Article article;

    public ElementReadEvent(Article article) {
        super(READ_EVENT);

        this.article = article;
    }
    
    public Article getArticle() {
        return article;
    }
}