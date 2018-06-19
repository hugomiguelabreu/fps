package UI;

import Util.FileUtils;
import Util.TorrentUtil;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedTorrent;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;

public class AppController {

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private ListView<String> list_groups;
    @FXML
    private ListView<String> list_users;
    @FXML
    private AnchorPane list_groups_files;
    @FXML
    private AnchorPane dragNdrop;
    @FXML
    private SplitPane splitPane1;
    @FXML
    private SplitPane splitPane2;

    private ArrayList<Pane> notifications;
    private HashMap<String, List<Torrent>> groupTorrents;

    @FXML
    void handleDragOver(DragEvent event) {
        event.acceptTransferModes(TransferMode.ANY);
        event.consume();

        dragNdrop.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        dragNdrop.setOpacity(0.8);
        dragNdrop.setPrefWidth(405);
        dragNdrop.setVisible(true);
    }

    @FXML
    void handleDragDropped(DragEvent event) {
        System.out.println(event.getDragboard().getFiles().get(0).getName());

        event.setDropCompleted(true);
        event.consume();
    }

    @FXML
    void initialize() throws IOException, NoSuchAlgorithmException {
        final double pos = splitPane1.getDividers().get(0).getPosition();
        splitPane1.getDividers().get(0).positionProperty().addListener(
                (observable, oldValue, newValue) -> splitPane1.getDividers().get(0).setPosition(pos)
        );
        final double pos2 = splitPane2.getDividers().get(0).getPosition();
        splitPane2.getDividers().get(0).positionProperty().addListener(
                (observable, oldValue, newValue) -> splitPane2.getDividers().get(0).setPosition(pos2)
        );

        Collection<String> labels = new ArrayList<>();
        notifications = new ArrayList<>();
        groupTorrents = new HashMap<>();
        labels.add("migos");
        groupTorrents.put("migos", null);
        for (int i = 0; i < 50; i++) {
            labels.add("okok" + i);
            groupTorrents.put("okok" + i, null);
        }

        ObservableList<String> items = FXCollections.observableArrayList(labels);
        list_groups.setItems(items);
        list_users.setItems(items);
        list_groups.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, event -> {
            try {
                list_groups_files.getChildren().remove(0, list_groups_files.getChildren().size());
                String groupSelected = list_groups.getSelectionModel().getSelectedItem();
                this.loadFiles(groupSelected);
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });
        //list_groups_files.setSpacing(8.0);
    }

    private void loadFiles(String group) throws IOException, NoSuchAlgorithmException {
        if(groupTorrents.get(group) == null)
            groupTorrents.put(group, FileUtils.load(group));
        notifications.clear();

        for(Torrent t: groupTorrents.get(group)){

            Label l = new Label();
            Button accept = new Button();
            Button close = new Button();
            AnchorPane pane = new AnchorPane();

            StringBuilder sb = new StringBuilder()
                    .append(t.getCreatedBy() + " wants to share ");
            sb.append(t.getFilenames().get(0) + " ( " + t.getSize()/1024/1024 + " MB) " + " with you");


            // update UI thread
            Platform.runLater(new Runnable() {
                @Override
                public void run() {

                    notifications.add(pane);

                    pane.getChildren().add(l);
                    pane.getChildren().add(accept);
                    pane.getChildren().add(close);
                    list_groups_files.getChildren().add(pane);

                    //pane.setLayoutX(244.0);
                    pane.setLayoutY(-92);
                    pane.setPrefHeight(92);
                    pane.setPrefWidth(385.0);
                    pane.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
                    pane.setBorder(new Border(
                            new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)
                    ));

                    l.setText(sb.toString());
                    l.setAlignment(Pos.CENTER);
                    l.setLayoutX(14.0);
                    l.setLayoutY(20.0);
                    l.setPrefHeight(16.0);
                    l.setPrefWidth(385.0);

                    accept.setAlignment(Pos.CENTER);
                    accept.setLayoutX(116);
                    accept.setLayoutY(55);
                    accept.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                    accept.setText("Accept");
                    accept.setUserData(l);

                    close.setAlignment(Pos.CENTER);
                    close.setLayoutX(209);
                    close.setLayoutY(55);
                    close.setBackground(new Background(new BackgroundFill(Color.INDIANRED, CornerRadii.EMPTY, Insets.EMPTY)));
                    close.setText("Delete");
                    close.setUserData(pane);


                    //TODO close button close.setUserdata(pane);

                    TranslateTransition down = new TranslateTransition();
                    down.setFromY(94 * (notifications.size() - 1));
                    down.setToY(94 * notifications.size());

                    down.setDuration(Duration.seconds(1));
                    down.setNode(pane);

                    down.play();
                }
            });

            accept.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
                @Override
                public void handle(javafx.scene.input.MouseEvent mouseEvent) {

                    //TODO mudar para a diretoria certa
                    File dest = new File("/tmp/");

                    try {

                        SharedTorrent st = new SharedTorrent(t, dest);
                        //TorrentUtil.download(st, false, username, (Label)accept.getUserData());

                    } catch (IOException | NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }

                }
            });

            close.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
                @Override
                public void handle(javafx.scene.input.MouseEvent mouseEvent) {

                    //System.out.println("accept torrent " + t.toString());

                    AnchorPane selectedPane = (AnchorPane) close.getUserData();

                    list_groups_files.getChildren().remove(selectedPane);

                    int index = notifications.indexOf(selectedPane);

                    notifications.remove(selectedPane);

                    for(Pane p : notifications){

                        if(notifications.indexOf(p) >= index){

                            System.out.println("go up");

                            TranslateTransition up = new TranslateTransition();
                            up.setDuration(Duration.seconds(1));
                            up.setByY(-94);
                            up.setNode(p);
                            up.play();

                        }

                    }
                }
            });

//
//            Label op = new Label();
//            op.setText(t.getName() + " | " + t.getSize() / 1024 / 1024 + " mb | " + t.getCreatedBy());
//            op.setStyle("-fx-background-color: #64BEF6; -fx-border-color: #90CAF9; -fx-padding: 5; " +
//                    "-fx-border-radius: 50 50 50 50; -fx-background-radius: 50 50 50 50; -fx-border-style: solid;" +
//                    "-fx-underline: true; -fx-cursor: hand;");
//            list_groups_files.getChildren().add(op);
        }
    }

    public void handleDragExited(DragEvent dragEvent) {
        dragNdrop.setVisible(false);
        dragEvent.consume();
    }


}