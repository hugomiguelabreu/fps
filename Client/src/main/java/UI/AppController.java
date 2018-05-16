package UI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AppController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;
    @FXML
    private Label initial_label;

    @FXML
    void handleDragEntered(DragEvent event) {
        System.out.println( event.getDragboard().getFiles().get(0).getName());
        System.out.println("okok");
    }

    @FXML
    void initialize() {
        Label l = initial_label;
        Scene s = l.getScene();
        Stage st = (Stage) s.getWindow();
        for(int i = 0; i<10; i++){
            Label k = new Label();
            k.setText("kekeke" + i);

        }
    }
}