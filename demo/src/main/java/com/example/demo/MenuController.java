package com.example.demo;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import java.io.IOException;

public class MenuController {

    @FXML
    private void onStartBtn(ActionEvent event) throws IOException {
        HelloApplication.setRoot("game");
    }

    @FXML
    private void onSettingsBtn(ActionEvent event) throws IOException{
        HelloApplication.setRoot("settings");
    }

    @FXML
    private void onCreditsBtn(ActionEvent event) throws IOException{
        HelloApplication.setRoot("credits");
    }

    @FXML
    private void onExitBtn(ActionEvent event){
        System.exit(0);
    }
}
