package UI;

import Util.FileUtils;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedTorrent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

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
    private VBox list_groups_files;
    @FXML
    private ScrollPane scroll_panel;

    @FXML
    void handleDragEntered(DragEvent event) {
        System.out.println( event.getDragboard().getFiles().get(0).getName());
        System.out.println("okok");
    }

    @FXML
    void initialize() throws IOException, NoSuchAlgorithmException {
        Collection<String> labels = new ArrayList<>();
        labels.add("migos");
        for (int i = 0; i < 50; i++) {
            labels.add("okok" + i);
        }
        ObservableList<String> items = FXCollections.observableArrayList(labels);
        list_groups.setItems(items);
        list_users.setItems(items);
        list_groups.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, event -> {
            try {
                list_groups_files.getChildren().remove(0, list_groups_files.getChildren().size());
                this.loadFiles(list_groups.getSelectionModel().getSelectedItem());
                scroll_panel.setVvalue(scroll_panel.vmaxProperty().doubleValue());
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });
        list_groups_files.setSpacing(8.0);
    }

    private void loadFiles(String group) throws IOException, NoSuchAlgorithmException {
        List<Torrent> groupTorrents = FileUtils.load(group);
        for(Torrent t: groupTorrents){
            Label op = new Label();
            op.setText(t.getName() + " | " + t.getSize() / 1024 / 1024 + " mb | " + t.getCreatedBy());
            op.setStyle("-fx-background-color: #64BEF6; -fx-border-color: #90CAF9; -fx-padding: 5; " +
                    "-fx-border-radius: 50 50 50 50; -fx-background-radius: 50 50 50 50; -fx-border-style: solid;" +
                    "-fx-underline: true; -fx-cursor: hand;");
            list_groups_files.getChildren().add(op);
        }
    }
}