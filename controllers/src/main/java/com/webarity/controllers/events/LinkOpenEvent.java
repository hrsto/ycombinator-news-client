package com.webarity.controllers.events;

import java.net.URL;

import com.webarity.entities.Article;

import javafx.event.EventType;

/**
 * <p>Fired when clicked on an {@link Article} link. Payload contains URL object.</p>
 */
public class LinkOpenEvent extends NewsStateEvenType {

    private static final long serialVersionUID = 1L;

    public static final EventType<LinkOpenEvent> LINK_OPEN_EVENT = new EventType<>(NewsStateEvenType.NEWS_STATE_EVENT, "LINK_OPEN_EVENT");

    private URL link;

    public LinkOpenEvent(URL link) {
        super(LINK_OPEN_EVENT);

        this.link = link;
    }
    
    public URL getLink() {
        return link;
    }
}