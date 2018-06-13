package UI;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class OfflineUI implements Observer {

    public ListView list_users;

    @FXML
    void initialize(){

    }

    @Override
    public void update(Observable observable, Object o) {
        
    }
}
