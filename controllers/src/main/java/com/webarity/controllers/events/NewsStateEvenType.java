package com.webarity.controllers.events;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * <p>Base class for all custom news state events</p>
 */
public class NewsStateEvenType extends Event {

    private static final long serialVersionUID = 1L;

    public static final EventType<NewsStateEvenType> NEWS_STATE_EVENT = new EventType<>("NEWS_STATE_EVENT");

    private NewsStateEvenType() {
        super(NEWS_STATE_EVENT);
    }
    public NewsStateEvenType(EventType<? extends Event> eventType) {
        super(eventType);
    }
}