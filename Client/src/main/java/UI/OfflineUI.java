package UI;

import Event.ConcurrentHashMapEvent;
import Event.MapEvent;
import Offline.Offline;
import Offline.OfflineUploadThread;
import Offline.Utils.User;
import com.turn.ttorrent.common.Torrent;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.util.*;

public class OfflineUI implements MapEvent {

    public ListView list_users;
    public Label label_file;
    public AnchorPane paneDrop;
    public Label label_send;

    private HashMap<String, User> usersOn;
    private OfflineUploadThread uploadThread;

    @FXML
    void initialize(){

        uploadThread = new OfflineUploadThread();
        usersOn = new HashMap<>();
        ArrayList<Torrent> available = new ArrayList<>();
        String username = "FALTA RECEBER O USERNAME";

        Offline.startProbes(username, available, this);

        ConcurrentHashMapEvent<String , User> map = new ConcurrentHashMapEvent<>();
        map.registerCallback(this);

        paneDrop.setOnDragEntered(new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                String path;
                boolean success = false;
                if (db.hasString()) {
                    label_file.setText("File = " + db.getFiles().get(0).getName() );
                    path = db.getFiles().get(0).getAbsolutePath();
                    label_send.setText("Select user to send");

                    // handler para recolher o mano que esta selecionado na lista
                    list_users.getSelectionModel().selectedItemProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
                        // Your action here
                        System.out.println("Selected item: " + newValue);
                        label_send.setText("send to " + newValue);

                        label_send.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent mouseEvent) {

                                System.out.println("clicked on user, init local send process");
                                sendLocal(path, username);
                            }
                        });

                    });

                    success = true;
                }

                event.consume();
            }
        });
    }

    @Override
    public void putEvent() {

        try{

            System.out.println("new UI PEER");

            Collection<String> users = new ArrayList<>();

            for(Map.Entry<String, User> entry : Offline.listener.getUsers().entrySet()){

                usersOn.put(entry.getKey(), entry.getValue());
                users.add(entry.getValue().getIpv6());
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

            users.add(entry.getValue().getIpv6());
        }

        ObservableList<String> items = FXCollections.observableArrayList(users);
        list_users.setItems(items);
    }

    private void sendLocal(String path, String username){

        uploadThread.newUpload(path,username);
        uploadThread.start();
    }
}
