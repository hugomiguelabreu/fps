package UI;

import Core.Connector;
import Util.ServerOperations;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainController implements Initializable{

    public AnchorPane slider;
    public Label status;
    @FXML
    private Label error_login;
    @FXML
    private PasswordField login_password;
    @FXML
    private TextField login_username;
    @FXML
    private TextField login_username_offline;
    @FXML
    private Pane paneOn;
    @FXML
    private Pane paneOff;

    private Connector channel;
    private ArrayList<String> servers;
    private ArrayList<String> trackers;
    private boolean type; // true -> online | false -> offline


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        type = false;

        servers = new ArrayList<>();
        trackers = new ArrayList<>();

        servers.add("159.65.48.36:2001");
        servers.add("188.166.165.159:2000");
        trackers.add("http://159.65.48.36:6969/announce");
        trackers.add("http://188.166.165.159:6969/announce");

        try {
            channel = new Connector(servers);
            if(channel.isConnected()){
                paneOn.setVisible(true);
                channel.start();
                ServerOperations.setChannel(channel);
                ServerOperations.setTrackersOnline(trackers);

                type = true;
                slider.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                status.setText("Online");
                TranslateTransition down = new TranslateTransition();
                down.setToY(40);
                down.setDuration(Duration.seconds(2));
                down.setNode(slider);

                down.play();
            }
            else {
                type = false;
                paneOff.setVisible(true);

                slider.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
                status.setText("Offline");


                TranslateTransition down = new TranslateTransition();
                down.setToY(40);
                down.setDuration(Duration.seconds(2));
                down.setNode(slider);

                down.play();
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


    }

    @FXML
    void loginHandle(ActionEvent event) throws IOException {
        if(type){
            if(ServerOperations.login(login_username.getText().substring(0, 11), login_password.getText())){
                FXMLLoader loader = new FXMLLoader();
                String fxmlDocPath = "src/main/java/UI/app.fxml";
                FileInputStream fxmlStream = new FileInputStream(fxmlDocPath);

                Parent root = loader.load(fxmlStream);
                AppController controller = loader.getController();

                Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } else{
                error_login.setTextFill(Color.web("#ff0000"));
                error_login.setVisible(true);
            }
        } else {
            paneOff.setVisible(true);
            FXMLLoader loader = new FXMLLoader();
            String fxmlDocPath = "src/main/java/UI/Offline.fxml";

            FileInputStream fxmlStream = new FileInputStream(fxmlDocPath);
            Parent root = loader.load(fxmlStream);

            OfflineUI controller = loader.getController();
            controller.initLocal(login_username_offline.getText().substring(0, 11));

            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        }
    }
}
