package com.webarity.news.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.webarity.entities.helpers.JSONElements;
import com.webarity.entities.Comment;
import com.webarity.entities.Message;

class CommentsRecursive extends RecursiveAction {

    private static final long serialVersionUID = 1L;

    Message parent;
    EntityManagerFactory emf;
    JSONElements parentData;
    CommentsTask taskRef;

    CommentsRecursive(Message parent, EntityManagerFactory emf, CommentsTask taskRef) {
        this.parent = parent;
        this.emf = emf;
        parentData = null;
        this.taskRef = taskRef;
    }
    CommentsRecursive(Message parent, EntityManagerFactory emf, JSONElements list, CommentsTask taskRef) {
        this.parent = parent;
        this.emf = emf;
        this.parentData = list;
        this.taskRef = taskRef;
    }

    @Override
    protected void compute() {
        
        if (parentData == null) parentData = Tools.httpMessageRQ(parent.getId());

        if (parentData == null || parentData.comments == null) return;

        List<Callable<JSONElements>> commentData = parentData.comments
        .stream()
        .map(kidID -> (Callable<JSONElements>) () -> Tools.httpMessageRQ(kidID))
        .collect(Collectors.toList());

        EntityManager em = emf.createEntityManager();

        List<ForkJoinTask<Void>> subComments = new LinkedList<>();

        try {
            taskRef.addToTotalWork(commentData.size());

            Tools.ex.invokeAll(commentData).forEach(dat -> {
                
                em.getTransaction().begin();

                try {
                    JSONElements kidData = dat.get();
                    if (kidData != null) {
                        Comment c = em.merge(new Comment(kidData, parent));
                        if (kidData.comments != null) {
                            subComments.add(new CommentsRecursive(c, emf, kidData, taskRef).fork());
                        }
                    }
                    
                em.getTransaction().commit();

                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                } finally {
                    taskRef.addToWorkDone(1);
                }
            });

        } catch (InterruptedException ex) {
            ex.printStackTrace();
            taskRef.subtractFromWork(commentData.size());
        } finally {
            em.close();
        }
        subComments.forEach(subC -> subC.join());
    }
}