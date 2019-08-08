package client;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegController {
    public TextField loginField;
    public TextField passField;
    public TextField nickField;
    public Button btnClose;
    public Button btnReg;

    Controller controller;

    public void clickClose(ActionEvent actionEvent) {
        Stage stage = (Stage)btnClose.getScene().getWindow();
        stage.close();
    }


    public void clickReg(ActionEvent actionEvent) {
        try {
            controller.out.writeUTF("/reg "+loginField.getText()+" "+
                    passField.getText()+" "+nickField.getText());
            Stage stage = (Stage)btnReg.getScene().getWindow();
            stage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
