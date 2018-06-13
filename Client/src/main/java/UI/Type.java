package UI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class Type implements Initializable {

    public Circle onlineCircle;
    public Circle offlineCircle;
    public AnchorPane pane;

    private int onlineSize;
    private int offlineSize;

    private Timer growOffline;
    private Timer shrinkOffline;
    private Timer growOnline;
    private Timer shrinkOnline;

    @FXML
    private Text textOnline;
    public Text textOffline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        System.out.println("init type");

        onlineSize = 100;
        growOnline = new Timer();
        shrinkOnline = new Timer();

        offlineSize = 100;
        growOffline = new Timer();
        shrinkOffline = new Timer();
    }

    @FXML
    void clickOnline() throws IOException {

        System.out.println("click online");

        FXMLLoader loader = new FXMLLoader();
        String fxmlDocPath = "src/main/java/UI/app.fxml";
        FileInputStream fxmlStream = new FileInputStream(fxmlDocPath);
        File f = new File("src/main/java/UI/material.css");

        Pane root = (Pane) loader.load(fxmlStream);
        Stage stage = (Stage) onlineCircle.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    @FXML
    void resizeOnline(){

        shrinkOnline.cancel();

        growOnline = new Timer();
        growOnline.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                onlineCircle.setRadius(onlineSize);
                onlineSize++;

                textOnline.setScaleX(textOnline.getScaleX() + 0.02);
                textOnline.setScaleY(textOnline.getScaleY() + 0.02);

                if(onlineSize > 150)
                    this.cancel();

            }

        }, 0, 2);

    }

    @FXML
    void resizeOnlineReverse(){

        growOnline.cancel();

        shrinkOnline = new Timer();
        shrinkOnline.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                onlineSize--;
                onlineCircle.setRadius(onlineSize);

                textOnline.setScaleX(textOnline.getScaleX() - 0.02);
                textOnline.setScaleY(textOnline.getScaleY() - 0.02);

                if(onlineSize < 100)
                    this.cancel();

            }

        }, 0, 2);
    }

    @FXML
    void clickOffline(MouseEvent event) throws IOException {
        System.out.println("Offline clicked");

        FXMLLoader loader = new FXMLLoader();
        String fxmlDocPath = "src/main/java/UI/Offline.fxml";
        FileInputStream fxmlStream = new FileInputStream(fxmlDocPath);
        File f = new File("src/main/java/UI/material.css");

        Pane root = (Pane) loader.load(fxmlStream);
        Scene scene = new Scene(root);
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///" + f.getAbsolutePath());
        Stage stage = (Stage) onlineCircle.getScene().getWindow();
        stage.setScene(scene);
        
    }

    @FXML
    void resizeOffline(){

        shrinkOffline.cancel();

        growOffline = new Timer();
        growOffline.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                offlineCircle.setRadius(offlineSize);
                offlineSize++;

                textOffline.setScaleX(textOffline.getScaleX() + 0.02);
                textOffline.setScaleY(textOffline.getScaleY() + 0.02);

                if(offlineSize > 150)
                    this.cancel();

            }

        }, 0, 2);
    }

    @FXML
    void resizeOfflineReverse(){

        growOffline.cancel();

        shrinkOffline = new Timer();
        shrinkOffline.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                offlineSize--;
                offlineCircle.setRadius(offlineSize);

                textOffline.setScaleX(textOffline.getScaleX() - 0.02);
                textOffline.setScaleY(textOffline.getScaleY() - 0.02);

                if(offlineSize < 100)
                    this.cancel();

            }

        }, 0, 2);
    }
}
