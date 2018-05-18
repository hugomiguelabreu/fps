package UI;

import Util.ServerOperations;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable{

    @FXML
    private Label error_login;
    @FXML
    private PasswordField login_password;
    @FXML
    private Button registerButton;
    @FXML
    private Button loginButton;
    @FXML
    private TextField login_username;
    @FXML
    private TextField register_username;
    @FXML
    private PasswordField register_password;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    void loginHandle(ActionEvent event) throws IOException {
        if(ServerOperations.login(login_username.getText(), login_password.getText())){
            FXMLLoader loader = new FXMLLoader();
            // Path to the FXML File
            String fxmlDocPath = "src/main/java/UI/app.fxml";
            FileInputStream fxmlStream = new FileInputStream(fxmlDocPath);
            File f = new File("src/main/java/UI/material.css");
            // Create the Pane and all Details
            Pane root = (Pane) loader.load(fxmlStream);

            Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
            // Swap screen
            stage.setScene(new Scene(root));
        }else{
            error_login.setTextFill(Color.web("#ff0000"));
            error_login.setVisible(true);
        }
    }

    @FXML
    void registerHandle(ActionEvent event) {

    }
}
