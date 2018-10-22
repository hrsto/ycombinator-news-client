package com.webarity.entities;

import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REFRESH;
import static javax.persistence.CascadeType.REMOVE;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.webarity.entities.helpers.JSONElements;

@Entity
@Table(name="comment")
@Access(AccessType.PROPERTY)
public class Comment extends Message {

    @Access(AccessType.FIELD) Long version;

    public Comment() {}
    public Comment(JSONElements dat, Message parent) {
        setId(dat.id);
        setAuthor(dat.by);
        setCreated(new Date(dat.time));
        setLastUpdated(new Date());
        setParent(parent);
        setText(dat.commentText);
    }

    private Message parent;
    private String text;

    @ManyToOne(targetEntity=Message.class, fetch=FetchType.LAZY, cascade={REFRESH, REMOVE, PERSIST})
    @JoinColumn(name="parent", referencedColumnName="id")
    public Message getParent() { return parent; }
    public void setParent(Message parent) { this.parent = parent; }

    @Lob
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
} 