package UI;

import Core.Connector;
import Event.ConcurrentHashMapEvent;
import Event.MapEvent;
import Util.FileUtils;
import Util.ServerOperations;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;

public class AppController implements MapEvent{

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
    @FXML
    private Label label_file;

    private ArrayList<Pane> notifications;
    private String groupSelected;
    private ConcurrentHashMapEvent<String, ArrayList<Torrent>> groupTorrents;
    private ConcurrentHashMapEvent<String, ArrayList<Torrent>> groupUsers;


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
        groupTorrents = new ConcurrentHashMapEvent<>();
        groupTorrents.registerCallback(this);
        ServerOperations.setGroupTorrents(groupTorrents);
        groupUsers = new ConcurrentHashMapEvent<>();
        groupUsers.registerCallback(this);
        ServerOperations.setGroupUsers(groupUsers);

        notifications = new ArrayList<>();

        labels.add("migos");
        groupTorrents.put("migos", new ArrayList<>());
        for (int i = 0; i < 50; i++) {
            labels.add("okok" + i);
            groupTorrents.put("okok" + i, new ArrayList<>());
        }

        ObservableList<String> items = FXCollections.observableArrayList(labels);
        list_groups.setItems(items);
        list_users.setItems(items);
        list_groups.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, event -> {
            try {
                label_file.setVisible(true);
                list_groups_files.getChildren().remove(0, list_groups_files.getChildren().size());
                groupSelected = list_groups.getSelectionModel().getSelectedItem();
                this.loadFiles(groupSelected);
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });
        //list_groups_files.setSpacing(8.0);
    }

    private void loadFiles(String group) throws IOException, NoSuchAlgorithmException {
        notifications.clear();
        if(groupTorrents.get(group).size() == 0)
            groupTorrents.put(group, FileUtils.load(group));
        else
            label_file.setVisible(false);

        for(Torrent t: groupTorrents.get(group)){
            panelUI(t);
        }
    }

    void panelUI(Torrent t){
        Label l = new Label();
        Button accept = new Button();
        Button close = new Button();
        AnchorPane pane = new AnchorPane();

        StringBuilder sb = new StringBuilder()
                .append(t.getCreatedBy() + " wants to share ");
        sb.append(t.getFilenames().get(0) + " ( " + t.getSize()/1024/1024 + " MB) " + " with you");

        // update UI thread
        Platform.runLater(() -> {
            notifications.add(pane);

            pane.getChildren().add(l);
            pane.getChildren().add(accept);
            pane.getChildren().add(close);
            list_groups_files.getChildren().add(pane);

            //pane.setLayoutX(244.0);
            pane.setLayoutY(-92);
            pane.setLayoutX(2);
            pane.setPrefHeight(92);
            pane.setPrefWidth(365);
            pane.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
            pane.setBorder(new Border(
                    new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)
            ));

            l.setText(sb.toString());
            l.setAlignment(Pos.CENTER);
            l.setLayoutX(14.0);
            l.setLayoutY(20.0);
            l.setPrefHeight(16.0);
            l.setPrefWidth(365);

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
        });

        accept.setOnMouseClicked(mouseEvent -> {
            //TODO mudar para a diretoria certa
            File dest = new File("/tmp/");
            try {
                SharedTorrent st = new SharedTorrent(t, dest);
                //TorrentUtil.download(st, false, username, (Label)accept.getUserData());
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });

        close.setOnMouseClicked(mouseEvent -> {
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
        });
    }

    @FXML
    void handleDragOver(DragEvent event) {
        event.acceptTransferModes(TransferMode.ANY);
        event.consume();

        dragNdrop.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        dragNdrop.setOpacity(0.8);
        dragNdrop.setPrefWidth(385);
        label_file.setVisible(false);
        dragNdrop.setVisible(true);
    }

    @FXML
    void handleDragDropped(DragEvent event) {
        System.out.println(event.getDragboard().getFiles().get(0).getName());
        if(groupSelected == null || groupSelected == "") {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setContentText("No group selected");
            a.showAndWait().filter(response -> response == ButtonType.OK);
        }else{

        }
        event.setDropCompleted(true);
        event.consume();
    }

    public void handleDragExited(DragEvent dragEvent) {
        dragNdrop.setVisible(false);
        if(groupSelected != null && groupTorrents.get(groupSelected).size() == 0)
            label_file.setVisible(true);
        dragEvent.consume();
    }

    @Override
    public void putEvent(int type, Object key) {
        if(type == 1){
            //Torrent novo;
            String group = String.valueOf(key);
            panelUI(groupTorrents.get(group).get(0));
        }
    }

    @Override
    public void removeEvent() {

    }
}