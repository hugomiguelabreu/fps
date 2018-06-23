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
import javafx.scene.control.*;
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
    private PasswordField login_password;
    @FXML
    private TextField login_username;
    @FXML
    private PasswordField register_password;
    @FXML
    private TextField register_username;
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

        servers.add("167.99.90.193:2001");
        servers.add("207.154.229.185:2002");
        trackers.add("http://167.99.90.193:6969/announce");
        trackers.add("http://207.154.229.185:6969/announce");

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
            if(login_username.getText().length() > 12) {
                showError("Username too long", "The username should be lower than 12 chars.");
            }else if(ServerOperations.login(login_username.getText(), login_password.getText())){
                FXMLLoader loader = new FXMLLoader();
                String fxmlDocPath = "src/main/java/UI/app.fxml";
                FileInputStream fxmlStream = new FileInputStream(fxmlDocPath);

                Parent root = loader.load(fxmlStream);
                AppController controller = loader.getController();

                Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } else{
                showError("Error on login", "Username or Password wrong");
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

    @FXML
    void registerHandle(ActionEvent event) throws IOException {
        if (type) {
            if (register_username.getText().length() > 12) {
                showError("Username too long", "The username should be lower than 12 chars.");
            } else if (ServerOperations.register(register_username.getText(), register_password.getText(), register_username.getText())) {
                showSuccess("Account created", "Account successfully created");
            } else {
                showError("Error on register", "Something went wrong, please check username");
            }
        }
    }

    public void showError(String header, String info){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(info);

        alert.showAndWait();
    }

    public void showSuccess(String header, String info){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(header);
        alert.setContentText(info);

        alert.showAndWait();
    }

}
