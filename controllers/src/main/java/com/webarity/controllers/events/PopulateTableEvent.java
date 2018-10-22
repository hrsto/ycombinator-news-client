package com.webarity.controllers.events;

import com.webarity.entities.Article;

import javafx.event.EventType;

/**
 * <p>Fired when table contents will be changed. Like when dragging slider to set how many {@link Article Articles} to show.</p>
 */
public class PopulateTableEvent extends NewsStateEvenType {

    private static final long serialVersionUID = 1L;

    public static final EventType<CommentOpenEvent> POPULATE_TABLE_EVENT = new EventType<>(NewsStateEvenType.NEWS_STATE_EVENT, "POPULATE_TABLE_EVENT");

    private long count;

    public PopulateTableEvent(long count) {
        super(POPULATE_TABLE_EVENT);

        this.count = count;
    }
    
    public long getCount() {
        return count;
    }
}