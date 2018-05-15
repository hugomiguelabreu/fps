import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import javafx.scene.control.TextField;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MainUI extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        // Path to the FXML File
        String fxmlDocPath = "src/main/java/UI/main.fxml";
        FileInputStream fxmlStream = new FileInputStream(fxmlDocPath);
        File f = new File("src/main/java/UI/material.css");

        // Create the Pane and all Details
        Pane root = (Pane) loader.load(fxmlStream);
        Scene scene = new Scene(root);
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///" + f.getAbsolutePath());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Serviço Distribuído de Publicação e Subscrição de Ficheiros");
        primaryStage.show();
    }

}
