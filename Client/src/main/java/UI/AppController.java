package UI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.input.DragEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class AppController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    void handleDragEntered(DragEvent event) {
        System.out.println( event.getDragboard().getFiles().get(0).getName());
        System.out.println("okok");
    }

    @FXML
    void initialize() {

    }
}