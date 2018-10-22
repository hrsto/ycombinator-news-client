package com.webarity.entities;

import java.util.Date;
import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@Table(name="message")
@Access(AccessType.PROPERTY)
public abstract class Message {

    @Access(AccessType.FIELD) Long version;

    private long id;
    private ObjectProperty<Date> created = new SimpleObjectProperty<Date>();
    private ObjectProperty<Date> lastUpdated = new SimpleObjectProperty<Date>();
    private StringProperty author = new SimpleStringProperty();

    private ObjectProperty<Set<Comment>> kids = new SimpleObjectProperty<>();

    @Id
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    @OneToMany(mappedBy="parent", fetch=FetchType.LAZY, targetEntity=Comment.class, cascade=CascadeType.ALL)
    @ElementCollection(targetClass=Comment.class, fetch=FetchType.LAZY)
    public Set<Comment> getKids() { return kids.get(); }
    public void setKids(Set<Comment> kids) {
        this.kids.set(kids);
    }

    public String getAuthor() { return author.get(); }
    public void setAuthor(String Author) { this.author.set(Author); }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getCreated() { return created.get(); }
    public void setCreated(Date created) { this.created.set(created); }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastUpdated() { return lastUpdated.get(); }
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated.set(lastUpdated); 
    }

    public ObjectProperty<Date> createdProperty() { return this.created; };
    public ObjectProperty<Date> lastUpdatedProperty() { return this.lastUpdated; };
    public StringProperty AuthorProperty() { return this.author; };
    public ObjectProperty<Set<Comment>> kidsProperty() { return this.kids; };
}