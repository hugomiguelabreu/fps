package UI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.ResourceBundle;

public class AppController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    void handleDragEntered(ActionEvent event) {
        System.out.println("okok");
    }

    @FXML
    void initialize() {

    }
}