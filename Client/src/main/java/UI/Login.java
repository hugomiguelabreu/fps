package UI;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javafx.scene.control.TextField;
import java.io.FileInputStream;
import java.io.IOException;

public class Login extends Application {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Button k;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        FXMLLoader loader = new FXMLLoader();
        // Path to the FXML File
        String fxmlDocPath = "src/main/java/UI/Login.fxml";
        FileInputStream fxmlStream = new FileInputStream(fxmlDocPath);

        // Create the Pane and all Details
        AnchorPane root = (AnchorPane) loader.load(fxmlStream);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("A simple FXML Example");
        primaryStage.show();

    }

    @FXML
    void khandle(ActionEvent event){

        System.out.println("username: " + username.getText());
        System.out.println("password: " + password.getText());
    }
}
