package com.webarity.controllers;

import java.io.IOException;

import com.webarity.entities.Article;
import com.webarity.entities.Message;
import com.webarity.controllers.events.InitDatabaseEvent;
import com.webarity.controllers.events.PopulateTableEvent;
import com.webarity.controllers.support.CommentCellFactory;
import com.webarity.controllers.support.CommentColumnFactory;
import com.webarity.controllers.support.TableRowFactory;

import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * <p>Main window of the app, as loaded by the fxml file.</p>
 */
public class MainStage extends AnchorPane {
    
    @FXML private AnchorPane rootContainer;

    @FXML private HBox appTopBar;
    @FXML private Label statusText;
    @FXML private ProgressBar titleProgressBar;
    @FXML private Region appCloseButton;
    @FXML private Region appMaximizeButton;
    @FXML private Region appMinimizeButton;

    @FXML private Group mainBackground;
    @FXML private StackPane rootStack;
    @FXML private Slider storiesSlider;
    @FXML private GridPane bottomBar;
    
    @FXML private TableView<Article> tableContent;
    @FXML private TableColumn<Article, Number> rankColumn;
    @FXML private TableColumn<Article, Article> articleColumn;
    @FXML private TableColumn<Article, Article> commentsColumn;

    @FXML private Button saveButton;

    private static double[] initialClickCoords = new double[2];
    private static final int edge = 10;
    private final SimpleBooleanProperty isDBActive = new SimpleBooleanProperty(false);

    private final ObservableList<Article> stories = FXCollections.observableArrayList();
    private final LongProperty storiesCount = new SimpleLongProperty(0);
    private Stage stageRef = null;

    private final SimpleStringProperty statusMessage = new SimpleStringProperty("");
    private final SimpleDoubleProperty progressTrack = new SimpleDoubleProperty(0);

    public SimpleStringProperty getStatusMessage() { return statusMessage; }
    public SimpleBooleanProperty getIsDBActiveProperty() { return isDBActive; }
    public SimpleDoubleProperty getProgressTrack() { return progressTrack; }
    public LongProperty getStoriesCount() { return storiesCount; }
    public ObservableList<Article> getStories() { return stories; }

    public MainStage(Stage stageRef) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainScene.fxml"));

        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
        this.stageRef = stageRef;

        initTable();
        initWindowControlButtons();
        initWindowDraggingAndResizing(rootContainer, appTopBar, this.stageRef);
        initToolbarDBButton();
        initSlider();

        titleProgressBar.progressProperty().bind(progressTrack);
        statusText.textProperty().bind(statusMessage);

        rootContainer.heightProperty().addListener((obs, old, newish) -> {
            computeTitle(rootContainer.widthProperty().get(), newish.doubleValue());
        });
        
        rootContainer.widthProperty().addListener((obs, old, newish) -> {
            computeTitle(newish.doubleValue(), rootContainer.heightProperty().get());
        });
    }

    /**
     * <p>Refreshes table so that it's sorted by non-read article and its rank and fixes a layout problem</p>
     * <p>Usually called after contents of table has changed</p>
     */
    public void refreshTable() {
        tableContent.getSortOrder().clear();
        tableContent.getSortOrder().add(articleColumn);
        tableContent.refresh();
    }

    /**
     * <p>Open a new window displaying comments for the given {@link Article}.</p>
     * @param m {@link Article} whose comments to display.
     */
    public void initPopup(Article m) {
        
        TreeView<Message> commentsTree = new TreeView<>();
        commentsTree.setId("commentsTree");
        commentsTree.prefWidthProperty().set(-1);
        commentsTree.prefHeightProperty().set(-1);
        TreeItem<Message> root = new TreeItem<>(m);

        commentsTree.setCellFactory(p -> new CommentCellFactory<Message>(new CommentCellController(), p));

        commentsTree.setRoot(root);
        commentsTree.setShowRoot(false);
        root.setExpanded(true);
        addKids(m, root);

        commentsTree.setStyle("-fx-background-color: transparent;");
        
        HBox titleBar = new CommentTitle(m.getTitle());
        VBox commentsContainer = new VBox(4, titleBar, commentsTree);
        
        VBox.setVgrow(commentsTree, Priority.ALWAYS);
        commentsContainer.setStyle(
            "-fx-background-color: rgba(63,63,63,0.2);" + 
            "-fx-border-width: 1;" +
            "-fx-border-color: #2b7fdf;"
            );
        
        Scene commentsPopup = new Scene(commentsContainer, 600, 300);
        commentsPopup.setFill(null);
        Stage pop = new Stage(StageStyle.TRANSPARENT);
        pop.setScene(commentsPopup);
        pop.show();

        initWindowDraggingAndResizing(commentsContainer, titleBar, pop);
    }

    /**
     * <p>Recursively populates the TreeView with comments</p>
     * @param m
     * @param parentNode
     */
    private void addKids(Message m, TreeItem<Message> parentNode) {
        m.getKids().forEach(kid -> {
            TreeItem<Message> newParent = new TreeItem<>(kid);
            parentNode.getChildren().add(newParent);
            
            parentNode.setExpanded(true);
            addKids(kid, newParent);
        });
    }

    /**
     * <p>Align by diagonal the title text</p>
     * @param w
     * @param h
     */
    private void computeTitle(Double w, Double h) {
        
        double hp = mainBackground.getBoundsInLocal().getHeight();
        double wp = mainBackground.getBoundsInLocal().getWidth();
        double hc = rootContainer.getHeight();
        double wc = rootContainer.getWidth();

        double hScale = hc / hp;
        double wScale = wc / wp;
        
        double scale = Math.min(hScale, wScale);

        mainBackground.scaleXProperty().set(scale);
        mainBackground.scaleYProperty().set(scale);

        mainBackground.rotateProperty().set(
            Math.toDegrees(Math.atan(-h / w))
        );
    }
    
    private void initTable() {

        tableContent.setRowFactory(table -> new TableRowFactory<Article>(new GaussianBlur(4), PseudoClass.getPseudoClass("read")));
        
        rankColumn.setCellValueFactory(cellData -> Bindings.createIntegerBinding(() -> cellData.getValue().getScore(), cellData.getValue().scoreProperty()));

        articleColumn.setCellValueFactory(cellData -> {
            Article temp = cellData.getValue();
                return Bindings.createObjectBinding(() -> temp, temp.articleProperty(), temp.AuthorProperty(), temp.urlProperty(), temp.createdProperty());
        });

        commentsColumn.setCellValueFactory(cellData -> Bindings.createObjectBinding(() -> cellData.getValue(), cellData.getValue().kidsProperty()));

        commentsColumn.setCellFactory(e -> new CommentColumnFactory());

        commentsColumn.setComparator((a, b) -> (int)(a.getDescendants() - b.getDescendants()));

        articleColumn.setComparator((a, b) -> {
            if (a.isRead() == b.isRead()) return b.getScore() - a.getScore();
            if (a.isRead()) return 1;
            if (b.isRead()) return -1;
            return 0;
        });

        articleColumn.setCellFactory(p -> new TableCell<>() {
            @Override
            protected void updateItem(Article item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                
                TableCellController articleCell = new TableCellController();
                setGraphic(articleCell);
                if (item == null) return;
                articleCell.setArticleTitle(item.getTitle());
                articleCell.setCreatedBy(item.getAuthor());
                articleCell.setDateCreated(item.getCreated().getTime());
                articleCell.setURL(item.getUrl());
                this.prefHeightProperty().bind(articleCell.heightProperty());
            }
        });

        articleColumn.prefWidthProperty().bind(tableContent.widthProperty().subtract(commentsColumn.prefWidthProperty().add(rankColumn.prefWidthProperty())));

        tableContent.setItems(stories);
    }

    private void initSlider() {

        storiesCount.addListener((obs, old, newish) -> {
            long s = newish.longValue();
            storiesSlider.maxProperty().set(s);
            storiesSlider.blockIncrementProperty().set(s * 0.125);

            int a = (int)Math.round(s / 4f);
            storiesSlider.majorTickUnitProperty().set(a <= 0 ? 4 : a);

            statusMessage.set(String.format("Currently showing %d articles; %d are hidden", Math.round(storiesSlider.valueProperty().get()), Math.round(storiesSlider.maxProperty().get() - storiesSlider.valueProperty().get())));
        });

        storiesSlider.valueProperty().addListener((obs, old, newish) -> {
            statusMessage.set(String.format("Currently showing %d articles; %d are hidden", Math.round(storiesSlider.valueProperty().get()), Math.round(storiesSlider.maxProperty().get() - storiesSlider.valueProperty().get())));

            if (storiesSlider.valueChangingProperty().get()) return; //when clicking slider, change table (ignores slider dragging)
            
            fireEvent(new PopulateTableEvent(newish.longValue()));
        });
        
        storiesSlider.valueChangingProperty().addListener((obs, old, newish) -> { //when dragging slider stops, change table
            if (old && newish == false) {
                
                fireEvent(new PopulateTableEvent(Double.valueOf(Math.round(storiesSlider.valueProperty().get())).longValue()));
            }
        });
    }

    private void initToolbarDBButton() {
        PseudoClass dbActive = PseudoClass.getPseudoClass("dbActive");
        saveButton.setOnAction(e -> {
            if (!isDBActive.get()) fireEvent(new InitDatabaseEvent());
        });

        isDBActive.addListener((observable, oldVal, newVal) -> {;
            saveButton.pseudoClassStateChanged(dbActive, newVal);
        });
    }

    private void initWindowControlButtons() {
        appCloseButton.setOnMouseClicked(evtt -> stageRef.close());
        appMaximizeButton.setOnMouseClicked(evtt -> {
            if (stageRef.isMaximized()) stageRef.setMaximized(false);
            else stageRef.setMaximized(true);
        });
        appMinimizeButton.setOnMouseClicked(evtt -> stageRef.setIconified(true));
        
        @SuppressWarnings("unused")
        EventHandler<MouseEvent> mouseOver = (e) -> {
            Region target = null;
            if (e.getTarget() instanceof Region) {
                target = (Region)e.getTarget();
            } else return;
        };
        
        @SuppressWarnings("unused")
        EventHandler<MouseEvent> mouseExit = (e) -> {
            Region target = null;
            if (e.getTarget() instanceof Region) {
                target = (Region)e.getTarget();
            } else return;
        };
        
        appCloseButton.setOnMouseEntered(mouseOver);
        appMaximizeButton.setOnMouseEntered(mouseOver);
        appMinimizeButton.setOnMouseEntered(mouseOver);
        appCloseButton.setOnMouseExited(mouseExit);
        appMaximizeButton.setOnMouseExited(mouseExit);
        appMinimizeButton.setOnMouseExited(mouseExit);
    }
    
    /**
     * <p>Initializes title bar dragging and resizing.</p>
     * FIXME: Resizing disabled for other borders, since there is a redrawing issue for the left border...
     * @param parent
     * @param titleBar
     * @param s Stage
     */
    private void initWindowDraggingAndResizing(Node parent, Node titleBar, Stage s) {
        titleBar.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> { //top bar window dragging
            s.setX(e.getScreenX() - initialClickCoords[0]);
            s.setY(e.getScreenY() - initialClickCoords[1]);
            e.consume();
        });

        parent.addEventFilter(MouseEvent.ANY, e -> { //bottom right corner window resize
            double stageH = s.getHeight();
            double stageW = s.getWidth();
            double mouseX = e.getX();
            double mouseY = e.getY();
            Cursor cursor = parent.getCursor();

            if (e.getEventType().equals(MouseEvent.MOUSE_MOVED)) {
                if (mouseX > stageW - edge && mouseY > stageH - edge) { //bottom right
                    cursor = Cursor.SE_RESIZE;
                } else {
                    cursor = Cursor.DEFAULT;
                }
                parent.setCursor(cursor);
            } else if (e.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
                if (cursor.equals(Cursor.SE_RESIZE)) {
                    initialClickCoords[0] = s.getWidth() - e.getX();
                    initialClickCoords[1] = s.getHeight() - e.getY();
                } else {
                    initialClickCoords[0] = e.getSceneX();
                    initialClickCoords[1] = e.getSceneY();
                }
            } else if (e.getEventType().equals(MouseEvent.MOUSE_DRAGGED) && cursor.equals(Cursor.SE_RESIZE)) {
                s.setWidth(Math.max(130, e.getX() - initialClickCoords[0] + edge / 2));
                s.setHeight(Math.max(180, e.getY() - initialClickCoords[1] + edge / 2));
            }
        });
    }

}

                // if (x <= edge) {
                //     if (y > edge && y < h - edge) { //left
                //         c = Cursor.E_RESIZE;
                //     } else if (y <= edge) { //top left
                //         c = Cursor.NW_RESIZE;
                //     } else { //bottom left
                //         c = Cursor.SW_RESIZE;
                //     }
                // } else if (y <= edge) { //top
                //     if (x > edge && x < h - edge) { //top
                //         c = Cursor.N_RESIZE;
                //     } else if (x >= w - edge) { //top left
                //         c = Cursor.NE_RESIZE;
                //     }
                // } else if (x > w - edge) { //right
                //     if (y < h - edge) { //right
                //         c = Cursor.W_RESIZE;
                //     } else { //bottom right
                //         c = Cursor.SE_RESIZE;
                //     }
                // } else if (y > h - edge) { //bottom
                //     c = Cursor.S_RESIZE;
                // } else {
                //     c = Cursor.DEFAULT;
                // }