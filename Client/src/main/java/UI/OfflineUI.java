package UI;

import Event.*;
import Offline.Offline;
import Offline.OfflineUploadThread;
import Offline.Utils.User;
import Util.TorrentUtil;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

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
    public Button button_broadcast;
    @FXML
    private SplitPane splitPane1;

    private HashMap<String, User> usersOn;
    private ArrayListEvent<Torrent> available;
    private Tracker offlineTck;
    private String username;
    private ArrayList<AnchorPane> notifications;

    @FXML
    void initialize(){
        final double pos = splitPane1.getDividers().get(0).getPosition();
        splitPane1.getDividers().get(0).positionProperty().addListener(
                (observable, oldValue, newValue) -> splitPane1.getDividers().get(0).setPosition(pos)
        );
        notifications = new ArrayList<>();

        usersOn = new HashMap<>();
        available = new ArrayListEvent<>();
        available.registerCallback(this);
    }

    @FXML
    void handleDragOver(DragEvent event) {

        event.acceptTransferModes(TransferMode.ANY);
        event.consume();

        //TODO fade para cinzento
        paneDrop.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    @FXML
    void handleDragDropped(DragEvent event) { // TODO meter botao broadcast

        Dragboard db = event.getDragboard();
        String path;

        if (db.hasString()) {
            label_file.setText("File = " + db.getFiles().get(0).getName() );
            path = db.getFiles().get(0).getAbsolutePath();

            label_send.setVisible(true);
            label_send.setText("Select user to send");

            button_broadcast.setUserData(path);
            button_broadcast.setVisible(true);

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
                        button_broadcast.setVisible(false);

                        System.out.println("clicked on user, init local send process");
                        sendLocal(path, username, value);
                    }
                });

            }
            else{

                list_users.getSelectionModel().selectedItemProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
                    // Your action here
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
                            button_broadcast.setVisible(false);

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

    @Override
    public void putEvent() {

        try{

            System.out.println("new UI PEER");

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

        catch (NullPointerException e){
            System.out.println("listner null, inseriu o self");
        }

    }

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

    private void sendLocal(String path, String username, String userToSend){

        label_file.setText("Drop Files Here");

        if(!username.equals(userToSend)){

            OfflineUploadThread uploadThread = new OfflineUploadThread();
            uploadThread.newUpload(path,username, offlineTck, userToSend);
            uploadThread.start();
        }
    }

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
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                notifications.add(pane);

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
                    down.setByY(-56);
                    down.setDuration(Duration.seconds(1));
                    down.setNode(p);
                    down.play();
                }
            }
        });

        accept.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {

                //TODO mudar para a diretoria certa
                File dest = new File("/tmp/");

                try {

                    SharedTorrent st = new SharedTorrent(t, dest);
                    TorrentUtil.download(st, false, username, (Label)accept.getUserData());

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

            }
        });

        close.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {

                //System.out.println("accept torrent " + t.toString());

                AnchorPane selectedPane = (AnchorPane) close.getUserData();

                mainPane.getChildren().remove(selectedPane);

                int index = notifications.indexOf(selectedPane);

                notifications.remove(selectedPane);

                for(AnchorPane p : notifications){

                    if(notifications.indexOf(p) >= index){

                        System.out.println("go up");

                        TranslateTransition up = new TranslateTransition();
                        up.setDuration(Duration.seconds(1));
                        up.setByY(-56);
                        up.setNode(p);
                        up.play();

                    }

                }
            }
        });
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

        Offline.startProbes(username, available, this);

        ConcurrentHashMapEvent<String , User> map = new ConcurrentHashMapEvent<>();
        map.registerCallback(this);

        //UI
        paneDrop.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

    }

    public void handleDragExited(DragEvent dragEvent) {

        paneDrop.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

    }

    public void handleBroadcastClicked(MouseEvent mouseEvent) {

        String path = (String) button_broadcast.getUserData();
        sendLocal(path, username, null);
        button_send.setVisible(false);
        button_broadcast.setVisible(false);
        label_send.setVisible(false);

    }
}
