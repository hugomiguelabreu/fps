package UI;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.effect.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

public class Type implements Initializable {


    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button online_ball;

    @FXML
    private Button offline_ball;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    void removeShadowOnline(MouseEvent event) {
        offline_ball.setEffect(null);
    }

    @FXML
    void applyShadowOnline(MouseEvent event) {
        BoxBlur bb = new BoxBlur();
        offline_ball.setEffect(bb);
    }

    @FXML
    void clickOnline(MouseEvent event) {
        System.out.println("Online clicked");
    }

    @FXML
    void removeShadowOffline(MouseEvent event) {
        online_ball.setEffect(null);
    }

    @FXML
    void applyShadowOffline(MouseEvent event) {
        BoxBlur bb = new BoxBlur();
        online_ball.setEffect(bb);
    }

    @FXML
    void clickOffline(MouseEvent event) {
        System.out.println("Offline clicked");
    }
}
