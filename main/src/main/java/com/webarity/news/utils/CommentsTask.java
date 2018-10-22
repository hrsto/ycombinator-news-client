package com.webarity.news.utils;

import javax.persistence.EntityManagerFactory;

import com.webarity.entities.Article;

import javafx.concurrent.Task;

public class CommentsTask extends Task<Article> {

    Article parent;
    EntityManagerFactory emf;

    int workDone = 0;
    int totalWork = 0;

    public CommentsTask(Article parent, EntityManagerFactory emf) {
        this.parent = parent;
        this.emf = emf;
    }

    @Override
    protected Article call() throws Exception {
        updateMessage(String.format("Fetching comments for %s", parent.getTitle()));
        CommentsRecursive comments = new CommentsRecursive(parent, emf, this);
        Tools.commentExecutor.invoke(comments);
        updateMessage("... comments fetched.");
        return null;
    }

    public void addToWorkDone(int work) {
        workDone += work;
        updateProgress(workDone, totalWork);
    }
    public void addToTotalWork(int work) {
        totalWork += work;
        updateProgress(workDone, totalWork);
    }
    public void subtractFromWork(int work) {
        totalWork -= work;
        updateProgress(workDone, totalWork);
    }
}