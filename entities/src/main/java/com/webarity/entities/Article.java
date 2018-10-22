package com.webarity.entities;

import java.net.URL;
import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.webarity.entities.helpers.JSONElements;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@Entity
@Table(name="article")
@NamedQueries({
    @NamedQuery(name="getAllArticles", query="SELECT a FROM Article a ORDER BY a.score DESC"),
    @NamedQuery(name="getAllArticleIDs", query="SELECT a.id FROM Article a"),
})
@Access(AccessType.PROPERTY)
public class Article extends Message {

    @Access(AccessType.FIELD) private Long version;

    public Article() {}
    public Article(JSONElements els) {
        this.setId(els.id);
        this.setAuthor(els.by);
        this.setCreated(new Date(els.time));
        this.setDescendants(els.descendants);
        this.setLastUpdated(new Date());
        this.setRead(false);
        this.setScore(els.score);
        this.setTitle(els.article);
        this.setUrl(els.url);
    }

    private IntegerProperty score = new SimpleIntegerProperty();
    private StringProperty title = new SimpleStringProperty();
    private LongProperty descendants = new SimpleLongProperty();
    private ObjectProperty<URL> url = new SimpleObjectProperty<URL>();
    private BooleanProperty read = new SimpleBooleanProperty();

    @Lob
    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }
    
    @Lob
    public URL getUrl() { return url.get(); }
    public void setUrl(URL url) { this.url.set(url); }
    
    public long getDescendants() { return descendants.get(); }
    public void setDescendants(long descendants) { this.descendants.set(descendants); }
    
    public boolean isRead() { return read.get(); }
    public void setRead(boolean read) { this.read.set(read); }
    
    public int getScore() { return score.get(); }
    public void setScore(int rank) { this.score.set(rank); }
    
    public IntegerProperty scoreProperty() { return this.score; };
    public StringProperty articleProperty() { return this.title; };
    public LongProperty descendantsProperty() { return this.descendants; }
    public ObjectProperty<URL> urlProperty() { return this.url; };
    public BooleanProperty readProperty() { return this.read; };
}