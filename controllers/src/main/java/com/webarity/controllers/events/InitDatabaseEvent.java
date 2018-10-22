package com.webarity.controllers.events;

import javafx.event.EventType;

/**
 * <p>Fired at startup to trigger DB connection.</p>
 */
public class InitDatabaseEvent extends NewsStateEvenType {

    private static final long serialVersionUID = 1L;

    public static final EventType<CommentOpenEvent> INIT_DATABASE_EVENT = new EventType<>(NewsStateEvenType.NEWS_STATE_EVENT, "INIT_DATABASE_EVENT");

    public InitDatabaseEvent() {
        super(INIT_DATABASE_EVENT);
    }
    
}