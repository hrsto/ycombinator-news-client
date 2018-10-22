package com.webarity.news.utils;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.webarity.entities.Article;
import com.webarity.entities.Message;
import com.webarity.entities.helpers.JSONElements;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.stage.Stage;

/**
 * <p>Singleton.</p>
 */
public class State {

    private Stage mainStage = null;

    private SimpleBooleanProperty isDBConnected = null;
    private SimpleDoubleProperty progressTracker = null;
    private SimpleStringProperty statusMessage = null;
    private LongProperty storiesCount = null;

    private EntityManagerFactory emf = null;

    private EntityManager globalEntityManager = null;

    public Stage getMainStage() { return mainStage; }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }
    public void setIsDBConnected(SimpleBooleanProperty b) {
        isDBConnected = b;
    }
    public void setStatusMessage(SimpleStringProperty s) {
        statusMessage = s;
    }
    public void setProgressTracker(SimpleDoubleProperty d) {
        progressTracker = d;
    }
    public void setStoriesCount(LongProperty l) { 
        storiesCount = l;
    }
    public void setMessage(String msg) {
        statusMessage.set(msg);
    }

    public SimpleDoubleProperty getProgressTracker() {
        return progressTracker;
    }

    /**
     * <p>Execute an operation on the single global entity manager using a single thread sequential executor.</p>
     * 
     * @param op A BiFunction that provides the Message argument that it was passed, and a EntityManager, then returns the Message
     * @param entity 
     * @return CompletableFuture
     */
    public <T extends Message> CompletableFuture<T> jpa(BiFunction<T, EntityManager, T> op, T entity) {
        return CompletableFuture.supplyAsync(()-> {
            T temp = null;
            try {
                temp = op.apply(entity, globalEntityManager);
            } catch (Exception ex) {
                if (globalEntityManager.getTransaction().isActive()) globalEntityManager.getTransaction().setRollbackOnly();
                ex.printStackTrace();
            } finally {
                
            }
            return temp;
        }, Tools.entityExecutor);
    }

    /**
    * <p>Execute an operation on the single global entity manager using a single thread sequential executor.</p>
     * 
     * @param op A Consumer that provides an EntityManager
     * @return CompletableFuture
     */
    public CompletableFuture<?> jpa(Consumer<EntityManager> op) {
        return CompletableFuture.runAsync(()-> {
            try {
                op.accept(globalEntityManager);
            } catch (Exception ex) {
                if (globalEntityManager.getTransaction().isActive()) globalEntityManager.getTransaction().setRollbackOnly();
                ex.printStackTrace();
            }
        }, Tools.entityExecutor);
    }

    /**
     * <p>Execute an operation on the single global entity manager using a single thread sequential executor.</p>
     * 
     * @param op A Function that provides a EntityManager and returns a result
     * @return CompletableFuture
     */
    public <T> CompletableFuture<T> jpa(Function<EntityManager, T> op) {
        return CompletableFuture.supplyAsync(()-> {
            try {
                T temp = op.apply(globalEntityManager);
                return temp;
            } catch (Exception ex) {
                if (globalEntityManager.getTransaction().isActive()) globalEntityManager.getTransaction().setRollbackOnly();
                ex.printStackTrace();
                return null;
            }
        }, Tools.entityExecutor);
    }

    public Task<Article> getComments(Article parent) {
        CommentsTask commentTask = new CommentsTask(parent, emf);
        Tools.ex.submit(commentTask);
        return commentTask;
    }

    /**
     * <p>Initiates connection to DB.</p>
     * 
     * @param path Where the DB is located. Set from {@link com.webarity.news.Main#start(Stage) Main}. 
     */
    public void createConnection(String path) {
        if (emf != null && emf.isOpen()) return;
        
        Task<Boolean> t = new Task<>() {

            @Override
            public Boolean call() {
                if (path != null && path.length() > 0) {

                    updateMessage("Setting up DB...");
                    emf = Persistence.createEntityManagerFactory("top-stories", Collections.singletonMap("javax.persistence.jdbc.url", String.format("jdbc:h2:%s", path)));

                    globalEntityManager = emf.createEntityManager();

                    updateMessage("... DB set.");
                    return true;
                }
                updateMessage("DB link failed: path to DB not specified.");
                return false;
            }
        };

        t.setOnSucceeded(e -> {
            try {
                if (t.get()) {
                    setMessage("Success db connect");
                    isDBConnected.set(t.get());
                    initData();
                    // syncTopStories();
                } else {
                    setMessage("Fail db connect");
                    isDBConnected.set(t.get());
                }
            } catch (InterruptedException | ExecutionException ex) {
                isDBConnected.set(false);
                ex.printStackTrace();
            }
        });
        t.setOnFailed(e -> {
            isDBConnected.set(false);
            setMessage("Exception in Persistance when connecting to DB.");
            e.getSource().getException().printStackTrace();
        });
        t.messageProperty().addListener((obs, before, after) -> setMessage(after));
        Tools.ex.execute(t);
    }

    /**
     * <p>After connection to DB is established from {@link #createConnection(String)}, it will fetch the latest top stories via GET request. Then it will fetch all available stories from the local DB, if any. Stories not in the local DB will be retrieved and persisted to the DB. Comments are retieved only on when explicitly requested.</p>
     * <p>After successful data retieve and persist, it will set the stories total count var.</p>
     */
    private void initData() {
        Task<Integer> q = new Task<Integer>() {
            @Override
            public Integer call() throws Exception {
//////////////////////////////////////////////////////////
                return jpa((em) -> {

                    em.getTransaction().begin();
                    Integer size = CompletableFuture.supplyAsync(()-> {
                        //gets all top articles as ranks

                        updateMessage("Fetching top stories...");
                        Set<Integer> topStories = Tools.httpTopStoriesRQ();
                        updateProgress(0, topStories.size());
                        return topStories;

                    }, Tools.ex).thenCombine(CompletableFuture.supplyAsync(() -> {
                        //gets all articles from DB
                                                
                        return em.createNamedQuery("getAllArticleIDs", Long.class)
                        .getResultStream()
                        .collect(Collectors.toSet());

                    }, Tools.ex), (totalStoriesByID, DBState) -> {
                        //filter out IDs that exist in the db and collect the new IDs for retrieval

                        Set<Integer> newStories = totalStoriesByID.stream().filter(storyID -> !DBState.contains(storyID.longValue())).collect(Collectors.toSet());
                        
                        updateMessage(String.format("%d stories available, fetching %d new stories.", DBState.size(), newStories.size()));
                        updateProgress(DBState.size(), totalStoriesByID.size());

                        //fetching the new articles

                        ExecutorCompletionService<JSONElements> ecs = new ExecutorCompletionService<>(Tools.ex);

                        newStories
                        .stream()
                        .map(articleID -> (Callable<JSONElements>) () -> Tools.httpMessageRQ(articleID))
                        .forEach(articleTask-> ecs.submit(articleTask));

                        for (int i = 0; i < newStories.size(); i++) {
                            try {
                                JSONElements dat = ecs.take().get();
                                if (dat == null) {
                                    updateProgress(DBState.size() + i, totalStoriesByID.size());
                                    continue;
                                }
                                Article newArticle = new Article(dat);

                                em.persist(newArticle);
                                updateMessage(String.format("%d new stories added, %d remaining.", i + 1, newStories.size() - (i + 1)));
                                updateProgress(DBState.size() + i, totalStoriesByID.size());
                            } catch (InterruptedException | ExecutionException ex) {
                                ex.printStackTrace();
                            }
                        }
                        return DBState.size() + newStories.size();
                    }).join();

                    em.getTransaction().commit();
                    return size;
                }).join();
//////////////////////////////////////////////////////////
            }
        };

        q.setOnSucceeded(e -> {
            setMessage("Init data success.");
            try {
                storiesCount.setValue(q.get());
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
            progressTracker.setValue(0);
        });
        q.setOnFailed(e -> {
            setMessage("Init data failed.");
            e.getSource().getException().printStackTrace();
        });
        q.progressProperty().addListener((obs, old, newish) -> {
            progressTracker.setValue(newish);
        });
        q.messageProperty().addListener((obs, old, newish) -> {
            setMessage(newish);
        });
        Tools.ex.execute(q);
    }
    
    private void closeJPA() {
        Tools.log("Closing up DB connection.");
        
        if (globalEntityManager != null) globalEntityManager.close();
        if (emf != null) emf.close();
        
        isDBConnected.set(false);
    }
    public void cleanUp() {
        closeJPA();

        Tools.log("Shutting down Comment Executor. %d tasks didn't complete.", Tools.commentExecutor.shutdownNow().size());
        Tools.log("Shutting down Main Executor. %d tasks didn't complete.", Tools.ex.shutdownNow().size());
        Tools.log("Shutting down Entity Executor. %d tasks didn't complete.", Tools.entityExecutor.shutdownNow().size());
    }

    private State() {}
    private static State state = null;
    public static State get() {
        if (state == null) {
            synchronized (State.class) {
                if (state == null) {
                    state = new State();
                }
            }
        }
        return state;
    }
}
