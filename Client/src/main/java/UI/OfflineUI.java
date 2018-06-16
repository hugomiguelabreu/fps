package UI;

import Event.ArrayEvent;
import Event.ArrayListEvent;
import Event.ConcurrentHashMapEvent;
import Event.MapEvent;
import Offline.Offline;
import Offline.OfflineUploadThread;
import Offline.Utils.User;
import Util.TorrentUtil;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.Tracker;
import javafx.animation.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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

    public ListView list_users;
    public Label label_file;
    public AnchorPane paneDrop;
    public Label label_send;
    public AnchorPane slider;
    public Label slider_label;
    public Button slider_button;
    public Button button_send;

    private HashMap<String, User> usersOn;
    private ArrayListEvent<Torrent> available;
    private Tracker offlineTck;
    private String username;

    @FXML
    void initialize(){

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
    void handleDragDropped(DragEvent event) {

        Dragboard db = event.getDragboard();
        String path;

        if (db.hasString()) {
            label_file.setText("File = " + db.getFiles().get(0).getName() );
            path = db.getFiles().get(0).getAbsolutePath();
            label_send.setText("Select user to send");

            // handler para recolher o mano que esta selecionado na lista
            list_users.getSelectionModel().selectedItemProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
                // Your action here
                System.out.println("Selected item: " + newValue);

                label_send.setVisible(false);

                button_send.setText("send to " + newValue);
                button_send.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {

                        paneDrop.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

                        System.out.println("clicked on user, init local send process");
                        sendLocal(path, username);
                    }
                });



//                label_send.setText("send to " + newValue);
//
//                label_send.setOnMouseClicked(new EventHandler<MouseEvent>() {
//                    @Override
//                    public void handle(MouseEvent mouseEvent) {
//
//                        System.out.println("clicked on user, init local send process");
//                        sendLocal(path, username);
//                    }
//                });

            });
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

                usersOn.put(entry.getKey(), entry.getValue());
                users.add(entry.getValue().getUsername());
            }

            ObservableList<String> items = FXCollections.observableArrayList(users);
            list_users.setItems(items);
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

            usersOn.remove(entry.getKey());
            users.add(entry.getValue().getIpv6());
        }

        ObservableList<String> items = FXCollections.observableArrayList(users);
        list_users.setItems(items);
    }

    private void sendLocal(String path, String username){

        OfflineUploadThread uploadThread = new OfflineUploadThread();
        uploadThread.newUpload(path,username, offlineTck);
        uploadThread.start();
    }

    @Override
    public void addEvent() {

    }

    @Override
    public void addReturnIndex(int i) {

        System.out.println("slide");

        Torrent t = available.get(i);

        StringBuilder sb = new StringBuilder()
                .append(t.getCreatedBy() + " wants to share ");

        if(t.getFilenames().size() > 1)
            sb.append(t.getFilenames().size() + "files with you");
        else
            sb.append(t.getFilenames().get(0) + "with you");

        slider_label.setText(sb.toString());

        TranslateTransition down = new TranslateTransition();
        down.setToY(56);

        down.setDuration(Duration.seconds(1));
        down.setNode(this.slider);

        down.play();

        slider_button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {

                //System.out.println("accept torrent " + t.toString());

                TranslateTransition up = new TranslateTransition();
                up.setToY(-56);

                up.setDuration(Duration.seconds(1));
                up.setNode(slider);

                up.play();

                File dest = new File("/tmp/");

                try {

                    SharedTorrent st = new SharedTorrent(available.get(i), dest);

                    TorrentUtil.download(st, false, username);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
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
        paneDrop.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        slider.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));


    }

    public void handleDragExited(DragEvent dragEvent) {

        paneDrop.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

    }
}
