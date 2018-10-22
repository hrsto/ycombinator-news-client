package com.webarity.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import com.webarity.controllers.events.LinkOpenEvent;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * <p>A table cell with article title, author, url, date created; this sets the inner contents from the fxml file.
 */
public class TableCellController extends VBox {

    @FXML Label articleTitle;
    @FXML Label dateCreated;
    @FXML Label createdBy;
    @FXML Hyperlink url;

    public TableCellController() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TableCell.fxml"));

        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle.setText(articleTitle);
    }
    public void setCreatedBy(String creator) {
        this.createdBy.setText(String.format("by: %s", creator));
    }
    public void setURL(URL url) {
        if (url == null) return;
        this.url.setText(url.getHost());
        this.url.setOnAction((e) -> fireEvent(new LinkOpenEvent(url)));
    }
    public void setDateCreated(long date) {
        Duration d = Duration.between(OffsetDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault()), OffsetDateTime.now());
        
        this.dateCreated.setText(String.format("%d days, %d hours, and %d mins ago", d.toDays(), d.toHoursPart(), d.toMinutesPart()));
    }

}