package UI;

import Event.ArrayEvent;
import Event.ArrayListEvent;
import Event.ConcurrentHashMapEvent;
import Event.MapEvent;
import Offline.Offline;
import Offline.OfflineUploadThread;
import Offline.Utils.User;
import com.turn.ttorrent.common.Torrent;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import java.util.*;

public class OfflineUI implements MapEvent, ArrayEvent {

    public ListView list_users;
    public Label label_file;
    public AnchorPane paneDrop;
    public Label label_send;
    public AnchorPane slider;
    public Label slider_label;
    public Button slider_button;

    private HashMap<String, User> usersOn;
    private OfflineUploadThread uploadThread;
    private ArrayListEvent<Torrent> available;
    private String username = "FALTA RECEBER O USERNAME";

    @FXML
    void initialize(){

        uploadThread = new OfflineUploadThread();
        usersOn = new HashMap<>();
        available = new ArrayListEvent<>();
        available.registerCallback(this);

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

        addReturnIndex(0); //TODO tirar isto daqui
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

            usersOn.remove(entry.getKey());
            users.add(entry.getValue().getIpv6());
        }

        ObservableList<String> items = FXCollections.observableArrayList(users);
        list_users.setItems(items);
    }

    private void sendLocal(String path, String username){

        uploadThread.newUpload(path,username);
        uploadThread.start();
    }

    @Override
    public void addEvent() {

    }

    @Override
    public void addReturnIndex(int i) {

        System.out.println("slide");

//        Torrent t = available.get(i);
//
//        StringBuilder sb = new StringBuilder()
//                .append(t.getCreatedBy() + " wants to share ");
//
//        if(t.getFilenames().size() > 1)
//            sb.append(t.getFilenames().size() + "files with you");
//        else
//            sb.append(t.getFilenames().get(0) + "with you");
//
//        slider_label.setText(sb.toString());

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

//                File dest = new File("/tmp/");
//                try {
//
//                    SharedTorrent st = new SharedTorrent(available.get(i), dest);
//
//                    TorrentUtil.download(st, false, username);
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (NoSuchAlgorithmException e) {
//                    e.printStackTrace();
//                }

            }
        });
    }
}
