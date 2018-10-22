package com.webarity.controllers.support;

import com.webarity.entities.Article;
import com.webarity.controllers.events.CommentOpenEvent;
import com.webarity.controllers.events.ElementReadEvent;

import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.control.TableRow;
import javafx.scene.effect.Effect;

public class TableRowFactory<T extends Article> extends TableRow<T> {
    
    Effect readRowEffect = null; 
    PseudoClass hasBeenRead = null;
    boolean dragged = false; //for table row - detect when user has dragged
    double[] startDragCoords = new double[2]; //to calculate drag slope

    ChangeListener<Boolean> changeListener = (a,b,c) -> {
        pseudoClassStateChanged(hasBeenRead, c);
        setEffect(c ? readRowEffect : null);
    };

    public TableRowFactory(Effect readRowEffect, PseudoClass hasBeenRead) {
        getStyleClass().clear();
        getStyleClass().add("table-row");

        this.readRowEffect = readRowEffect;
        this.hasBeenRead = hasBeenRead;

        init();
    }

    private void init() {
        itemProperty().addListener((observable, oldVal, newVal) -> {
            if (oldVal != null) oldVal.readProperty().removeListener(changeListener);
            if (newVal != null) {
                newVal.readProperty().addListener(changeListener);
                pseudoClassStateChanged(hasBeenRead, newVal.isRead());
                setEffect(newVal.isRead() ? readRowEffect : null);
            }
            else {
                pseudoClassStateChanged(hasBeenRead, false);
                setEffect(null);
            }
        });

        itemProperty().addListener((observable, oldVal, newVal) -> {
            if (oldVal != null) oldVal.readProperty().removeListener(changeListener);
            if (newVal != null) {
                newVal.readProperty().addListener(changeListener);
                pseudoClassStateChanged(hasBeenRead, newVal.isRead());
                setEffect(newVal.isRead() ? readRowEffect : null);
            }
            else {
                pseudoClassStateChanged(hasBeenRead, false);
                setEffect(null);
            }
        });

        setOnMousePressed(e -> {
            startDragCoords[0] = e.getX();
            startDragCoords[1] = e.getY();
        });
        setOnMouseDragged(e -> {
            dragged = true;
        });

        setOnMouseReleased(e -> {
            double slope = Math.abs((e.getY() - startDragCoords[1]) / (e.getX() - startDragCoords[0]));
            if (dragged && slope <= 0.3) {

                dragged = false;
                fireEvent(new ElementReadEvent(itemProperty().get()));

            } else if (dragged && slope > 0.3) {

                fireEvent(new CommentOpenEvent(itemProperty().get()));

            }
        });
    }
}