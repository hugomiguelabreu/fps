package UI;

import Event.ConcurrentHashMapEvent;
import Event.MapEvent;
import Util.FileUtils;
import Util.ServerOperations;
import Util.TorrentUtil;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppController implements MapEvent{

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
    private ConcurrentHashMapEvent<String, ArrayList<String>> groupUsers;


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

        groupTorrents = new ConcurrentHashMapEvent<>();
        groupTorrents.registerCallback(this);
        ServerOperations.setGroupTorrents(groupTorrents);
        groupUsers = new ConcurrentHashMapEvent<>();
        groupUsers.registerCallback(this);
        ServerOperations.setGroupUsers(groupUsers);

        notifications = new ArrayList<>();

        ArrayList<String> groups = ServerOperations.getUsersGroup();

        for(String g: groups){

            ArrayList<String> list = ServerOperations.getOnlineUsers(g);

            if(list != null){
                groupUsers.put(g, list);
            }

            groupTorrents.put(g,new ArrayList<>());
        }

//        groupTorrents.put("migos", new ArrayList<>());
//        groupUsers.put("migos", new ArrayList<>());
//        groupTorrents.put("leddit", new ArrayList<>());
//        groupUsers.put("leddit", new ArrayList<>());

        //list_groups.setItems(FXCollections.observableArrayList(new ArrayList<>(groupTorrents.keySet())));

        list_groups.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, event -> {
            try {
                label_file.setVisible(true);
                groupSelected = list_groups.getSelectionModel().getSelectedItem();
                list_users.setItems(FXCollections.observableArrayList(groupUsers.get(groupSelected)));
                //Limpa os files
                list_groups_files.getChildren().remove(0, list_groups_files.getChildren().size());
                this.loadFiles(groupSelected);

            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });

        updateUsersGroups();
    }

    private void loadFiles(String group) throws IOException, NoSuchAlgorithmException {
        notifications.clear();
        if(groupTorrents.get(group).size() == 0)
            groupTorrents.put(group, FileUtils.load(group));

        if(groupTorrents.get(group).size() > 0)
            label_file.setVisible(false);
        int i = 0;
        for(Torrent t: groupTorrents.get(group)){
            panelUI(t, true);
        }
    }

    void panelUI(Torrent t, boolean bulk){
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

            TranslateTransition down = new TranslateTransition();
            if(bulk) {
                down.setFromY(94 * (notifications.size() - 1));
                down.setToY(94 * notifications.size());
            }else{
                down.setFromY(0);
                down.setToY(94);
            }

            down.setDuration(Duration.seconds(1));
            down.setNode(pane);
            down.play();
        });

        accept.setOnMouseClicked(mouseEvent -> {
            File dest = new File(FileUtils.saveFilesPath);

            try {
                SharedTorrent st = new SharedTorrent(t, dest);
                ProgressBar pb = new ProgressBar(0);
                ProgressIndicator pi = new ProgressIndicator(0);
                pb.setPrefWidth(250);
                pb.setLayoutY(55);
                pb.setLayoutX(50);
                pi.setLayoutX(310);
                pi.setLayoutY(45);
                pane.getChildren().remove(accept);
                pane.getChildren().remove(close);
                pane.getChildren().add(pb);
                pane.getChildren().add(pi);
                new Thread(() -> {

                    TorrentUtil.download(st, true, ServerOperations.username, groupSelected, pb, pi);

                }).start();
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });

        close.setOnMouseClicked(mouseEvent -> {

            ServerOperations.removeTorrent(t, groupSelected);
            ServerOperations.removeClient(t);
            AnchorPane selectedPane = (AnchorPane) close.getUserData();
            list_groups_files.getChildren().remove(selectedPane);
            int indexNot = notifications.indexOf(selectedPane);
            notifications.remove(selectedPane);

            for(Pane p : notifications){
                if(notifications.indexOf(p) >= indexNot){
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
            String path = event.getDragboard().getFiles().get(0).getAbsolutePath();
            new Thread(()-> {
                //String p = "/root/120MB.tar.gz";
                ServerOperations.sendTorrent(path, groupSelected);
            }

            ).start();
        }

        showAlert("File sent");

        event.setDropCompleted(true);
        event.consume();
    }

    public void handleDragExited(DragEvent dragEvent) {
        dragNdrop.setVisible(false);
        if(groupSelected != null && groupTorrents.get(groupSelected).size() == 0)
            label_file.setVisible(true);
        dragEvent.consume();
    }

    public void handleClickJoin(MouseEvent e){
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Join group");
        dialog.setHeaderText("Name of group");
        dialog.setContentText("Name:");

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        // The Java 8 way to get the response value (with lambda expression).
        result.ifPresent(name -> {
            if(ServerOperations.joinGroup(name))
                showAlert("Grupo criado com sucesso");
            else
                showAlert("Erro a criar grupo");
        });
        e.consume();
    }

    public void handleClickCreate(MouseEvent e){
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Create group");
        dialog.setHeaderText("Name of group");
        dialog.setContentText("Name:");

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        // The Java 8 way to get the response value (with lambda expression).
        result.ifPresent(name -> {
            if(ServerOperations.createGroup(name))
                showAlert("Grupo criado com sucesso");
            else
                showAlert("Erro a criar grupo");
        });
        e.consume();
    }

    public void showAlert(String info){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(info);

        alert.showAndWait();
    }

    @Override
    public void putEvent(int type, Object key) {
        if(type == 0){
            String group = String.valueOf(key);
            if(!list_groups.getItems().contains(group))
                list_groups.getItems().add(0, group);
        }else if(type == 1){
            //Torrent novo;
            String group = String.valueOf(key);
            for(Pane p : notifications){
                TranslateTransition down = new TranslateTransition();
                down.setByY(94);
                down.setDuration(Duration.seconds(1));
                down.setNode(p);
                down.play();
            }
            panelUI(groupTorrents.get(group).get(0), false);
        }
    }

    @Override
    public void removeEvent() {

    }

    private void updateUsersGroups(){

        ScheduledExecutorService executor = Executors.newScheduledThreadPool ( 1 );

        Runnable r = () -> {

            try {

                for (Map.Entry<String, ArrayList<String>> entry : groupUsers.entrySet()) {

                    groupUsers.put(entry.getKey(), ServerOperations.getOnlineUsers(entry.getKey()));
                }

                Platform.runLater(() -> {

                    try{

                        list_users.setItems(FXCollections.observableArrayList(groupUsers.get(groupSelected)));

                    }catch (NullPointerException e){
                        //no group selected
                    }
                });

            } catch ( Exception e ) {
                //e.printStackTrace();
            }
        };

        executor.scheduleAtFixedRate( r , 500 , 3000 , TimeUnit.MILLISECONDS ); // ( runnable , initialDelay , period , TimeUnit )
    }
}