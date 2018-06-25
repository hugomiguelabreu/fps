package UI;

import Event.*;
import Offline.Offline;
import Offline.OfflineUploadThread;
import Offline.Utils.User;
import Util.FileUtils;
import Util.TorrentUtil;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.Tracker;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.controlsfx.control.CheckComboBox;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class OfflineUI implements MapEvent, ArrayEvent {

    public AnchorPane mainPane;
    public ListView list_users;
    public Label label_file;
    public AnchorPane paneDrop;
    public Label label_send;
    public AnchorPane slider;
    public Label slider_label;
    public Button slider_button;
    public Button button_send;
    public Button createGroupButton;
    public ListView groupsList;
    @FXML
    private SplitPane splitPane1;

    private HashMap<String, User> usersOn;
    private Tracker offlineTck;
    private String username;
    private ArrayList<AnchorPane> notifications;
    private HashMap<String, ArrayList<String>> groups;
    private HashMap<String,Client> torrentClients;
    private HashMap<String,Client> clientsDownloading;

    @FXML
    void initialize(){

        final double pos = splitPane1.getDividers().get(0).getPosition();
        splitPane1.getDividers().get(0).positionProperty().addListener(
                (observable, oldValue, newValue) -> splitPane1.getDividers().get(0).setPosition(pos)
        );

        notifications = new ArrayList<>();
        usersOn = new HashMap<>();
        groups = new HashMap<>();
        torrentClients = new HashMap<>();
        clientsDownloading = new HashMap<>();
    }

    public void initLocal(String username){

        this.username = username;

        System.out.println("username " + username);

        try {

            String httpAddress = Offline.findLocalAddresses().get(0).getIpv4();
            offlineTck = new Tracker(new InetSocketAddress(InetAddress.getByName(httpAddress), 6969));
            offlineTck.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        ConcurrentHashMapEvent<String , User> map = new ConcurrentHashMapEvent<>();
        map.registerCallback(this);

        ArrayListEvent<Torrent> available;
        available = new ArrayListEvent<>();
        available.registerCallback(this);

        Offline.startProbes(username, available, map);

        //UI
        paneDrop.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

//        TestThread t = new TestThread(available);
//        t.start();
    }


    //Drop File
    @FXML
    void handleDragOver(DragEvent event) {

        event.acceptTransferModes(TransferMode.ANY);
        event.consume();

        //TODO fade para cinzento
        paneDrop.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    //File droped to paneDrop
    @FXML
    void handleDragDropped(DragEvent event) {

        Dragboard db = event.getDragboard();
        String path;

        if (db.hasString()) {
            label_file.setText("File = " + db.getFiles().get(0).getName() );
            path = db.getFiles().get(0).getAbsolutePath();

            label_send.setVisible(true);
            label_send.setText("Select user to send");

            if (list_users.getSelectionModel().getSelectedItem() != null){

                String value = (String) list_users.getSelectionModel().getSelectedItem();

                label_send.setVisible(false);

                button_send.setText("send to " + value);
                button_send.setVisible(true);

                button_send.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        db.clear();
                        paneDrop.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
                        button_send.setVisible(false);

                        System.out.println("clicked on user, init local send process");
                        sendLocal(path, username, value);
                    }
                });

            }
            else{

                groupsList.getSelectionModel().selectedItemProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {

                    System.out.println("Selected item: " + newValue);

                    label_send.setVisible(false);

                    button_send.setText("send to group " + newValue);
                    button_send.setVisible(true);

                    button_send.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {

                            db.clear();

                            paneDrop.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
                            button_send.setVisible(false);

                            System.out.println("Sending to all menbers of the group");

                            for(String users : groups.get(newValue)){
                                sendLocal(path, username, users);
                            }
                        }
                    });
                });

                list_users.getSelectionModel().selectedItemProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {

                    System.out.println("Selected item: " + newValue);

                    label_send.setVisible(false);

                    button_send.setText("send to " + newValue);
                    button_send.setVisible(true);

                    button_send.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {

                            db.clear();

                            paneDrop.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
                            button_send.setVisible(false);

                            System.out.println("clicked on user, init local send process");
                            sendLocal(path, username, newValue);
                        }
                    });
                });
            }
        }

        event.setDropCompleted(true);
        event.consume();
    }

    //send Torrent
    private void sendLocal(String path, String username, String userToSend){

        label_file.setText("Drop Files Here");

        //if(!username.equals(userToSend)){

        OfflineUploadThread uploadThread = new OfflineUploadThread();
        uploadThread.newUpload(path,username, offlineTck, userToSend);
        uploadThread.start();

        //}
    }

    // drag sai do dropPane
    public void handleDragExited(DragEvent dragEvent) {

        paneDrop.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

    }

    // Add Peer to List
    @Override
    public void putEvent(int a, Object group) {

        try{

            System.out.println("new UI PEER");

            Collection<String> users = new ArrayList<>();

            for(User u : Offline.listener.getUsers().values()){

                if(!u.getUsername().equals(username)){

                    usersOn.put(u.getUsername(), u);
                    users.add(u.getUsername());
                }
            }

            ObservableList<String> items = FXCollections.observableArrayList(users);

            // update UI thread
            Platform.runLater(() -> list_users.setItems(items));
        }

        catch (NullPointerException e){
            System.out.println("listner null, inseriu o self");
        }
    }

    //Remove peer from List
    @Override
    public void removeEvent() {

        System.out.println("remove UI PEER");

        Collection<String> users = new ArrayList<>();

        for(Map.Entry<String, User> entry : Offline.listener.getUsers().entrySet()){

            if(!entry.getValue().equals(username)){

                usersOn.put(entry.getKey(), entry.getValue());
                users.add(entry.getValue().getUsername());
            }
        }

        ObservableList<String> items = FXCollections.observableArrayList(users);

        // update UI thread
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                list_users.setItems(items);
            }
        });
    }

    //Torrent received
    @Override
    public void addEventTorrent(Torrent t) {

        Label l = new Label();
        Button accept = new Button();
        Button close = new Button();
        AnchorPane pane = new AnchorPane();

        StringBuilder sb = new StringBuilder()
                .append(t.getCreatedBy() + " wants to share ");
        sb.append(t.getFilenames().get(0) + " ( " + t.getSize()/1024/1024 + " MB) " + " with you");


        // update UI thread
        Platform.runLater(() -> {

            pane.getChildren().add(l);
            pane.getChildren().add(accept);
            pane.getChildren().add(close);
            mainPane.getChildren().add(pane);

            pane.setLayoutX(244.0);
            pane.setLayoutY(-56.0);
            pane.setPrefHeight(56.0);
            pane.setPrefWidth(556.0);
            pane.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
            pane.setBorder(new Border(
                    new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)
            ));

            l.setText(sb.toString());
            l.setLayoutX(14.0);
            l.setLayoutY(20.0);
            l.setPrefHeight(16.0);
            l.setPrefWidth(314.0);

            accept.setAlignment(Pos.CENTER);
            accept.setLayoutX(378);
            accept.setLayoutY(15.0);
            accept.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
            accept.setText("Accept");
            accept.setUserData(l);

            close.setAlignment(Pos.CENTER);
            close.setLayoutX(466);
            close.setLayoutY(15.0);
            close.setBackground(new Background(new BackgroundFill(Color.INDIANRED, CornerRadii.EMPTY, Insets.EMPTY)));
            close.setText("Close");
            close.setUserData(pane);

            TranslateTransition down = new TranslateTransition();
            down.setFromY(0);
            down.setToY(56);
            down.setDuration(Duration.seconds(1));
            down.setNode(pane);
            down.play();

            for(AnchorPane p : notifications){

                down = new TranslateTransition();
                down.setByY(56);
                down.setDuration(Duration.seconds(1));
                down.setNode(p);
                down.play();
            }

            notifications.add(pane);
        });

        accept.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {

                pane.getChildren().remove(accept);

                //TODO mudar para a diretoria certa
                File dest = new File(FileUtils.saveFilesPath);

                ProgressBar pb = new ProgressBar(0);
                ProgressIndicator pi = new ProgressIndicator(0);

                pb.setPrefWidth(50);
                pb.setLayoutY(15);
                pb.setLayoutX(368);

                pi.setLayoutX(425);
                pi.setLayoutY(8);

                pane.getChildren().remove(accept);
                pane.getChildren().add(pb);
                pane.getChildren().add(pi);

                try {
                    SharedTorrent st = new SharedTorrent(t, dest);
                    Client c = TorrentUtil.download(st, false, username, "", pb, pi);
                    clientsDownloading.put(t.getHexInfoHash(),c);
                } catch (IOException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

            }
        });

        close.setOnMouseClicked(mouseEvent -> {

            Client c = clientsDownloading.get(t.getHexInfoHash());
            c.stop();
            clientsDownloading.remove(c);
            FileUtils.deletePartFile(t.getFilenames().get(0));

            AnchorPane selectedPane = (AnchorPane) close.getUserData();
            mainPane.getChildren().remove(selectedPane);
            int index = notifications.indexOf(selectedPane);
            notifications.remove(selectedPane);

            for(AnchorPane p : notifications){
                if(notifications.indexOf(p) < index){
                    System.out.println("go up");
                    TranslateTransition up = new TranslateTransition();
                    up.setDuration(Duration.seconds(1));
                    up.setByY(-56);
                    up.setNode(p);
                    up.play();
                }
            }

        });
    }

    public void handleCreateGroup(MouseEvent mouseEvent) {

        ObservableList<String> items = FXCollections.observableArrayList(groups.keySet());

        items.add(null);

        groupsList.setItems(items);

        groupsList.setCellFactory(param -> new ListCell<String>() {

            @Override
            public void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);

                if (empty) {
                    setText(null);
                    setGraphic(null);

                } else {

                    if(name == null){

                        AnchorPane p = new AnchorPane();
                        TextField tf = new TextField();
                        tf.setPromptText("Name");
                        tf.setMaxHeight(5);
                        tf.setMaxWidth(170);
                        p.getChildren().add(tf);
                        setGraphic(p);

                        tf.setOnKeyPressed((event) -> {

                            if(event.getCode().equals(KeyCode.ENTER)) {

                                String groupName = tf.getText();
                                p.getChildren().remove(tf);

                                ObservableList<String> users = FXCollections.observableArrayList(usersOn.keySet());

                                CheckComboBox cb = new CheckComboBox<String>(users);
                                cb.setMaxWidth(140);

                                p.getChildren().add(cb);

                                Button create = new Button();
                                create.setText("Done");
                                create.setLayoutX(161);

                                p.getChildren().add(create);

                                create.setOnMouseClicked(new EventHandler<MouseEvent>() {

                                    @Override
                                    public void handle(MouseEvent mouseEvent) {

                                        ArrayList<String> selectedList = new ArrayList<>();

                                        for (Object o : cb.getCheckModel().getCheckedItems()) {
                                            selectedList.add((String) o);
                                        }

                                        groups.put(groupName, selectedList);

                                        p.getChildren().remove(cb);
                                        p.getChildren().remove(create);

                                        setGraphic(null);
                                        setText(groupName);

                                        items.remove(null);
                                        items.add(groupName);
                                        groupsList.setItems(items);
                                    }
                                });
                            }
                        });
                    }

                    else{
                        setText(name);
                    }
                }
            }
        });
    }
}
