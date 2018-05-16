package UI;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

public class AppController {

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private ListView<String> list_groups;
    @FXML
    private VBox list_groups_files;

    @FXML
    void handleDragEntered(DragEvent event) {
        System.out.println( event.getDragboard().getFiles().get(0).getName());
        System.out.println("okok");
    }

    @FXML
    void initialize() {
        Collection<String> labels = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            labels.add("okok" + i);
        }
        ObservableList<String> items = FXCollections.observableArrayList(labels);
        list_groups.setItems(items);
        list_groups.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, event -> {
            System.out.println(list_groups.getSelectionModel().getSelectedItem());
        });

        list_groups_files.setSpacing(8.0);
        Label op = new Label();
        op.setText("algortimo.pdf");
        op.setStyle("-fx-background-color: #64BEF6; -fx-border-color: #90CAF9; -fx-padding: 5; " +
                "-fx-border-radius: 50 50 50 50; -fx-background-radius: 50 50 50 50; -fx-border-style: solid;");

        Label aa = new Label();
        aa.setText("okok.zip");
        aa.setStyle("-fx-background-color: #64BEF6; -fx-border-color: #90CAF9; -fx-padding: 5; " +
                "-fx-border-radius: 50 50 50 50; -fx-background-radius: 50 50 50 50; -fx-border-style: solid;");
        list_groups_files.getChildren().addAll(op, aa);
    }
}