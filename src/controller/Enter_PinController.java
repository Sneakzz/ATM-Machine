/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import db.Dao;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import model.Account;
import model.AtmMachine;

/**
 * FXML Controller class
 *
 * @author Kenny
 */
public class Enter_PinController implements Initializable {
    @FXML
    private PasswordField txtPin;
    @FXML
    private Button btnSubmit;
    
    AtmMachine AtmMachine;
    Account selectedAccount;
    Dao dao = new Dao();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // on starting of the screen first initialize the password field
        initPwField();

    }

    @FXML
    private void clickedSubmit(MouseEvent event) throws SQLException {
        // check if the pin field is valid before going any further
        if (pinFormatCorrect(txtPin)) {
            // if the format of the pin is correct, which means 4 digits have been entered
            // then we can check if the entered pin is correct for the chosen account.
            int answer = Integer.parseInt(txtPin.getText());
            //System.out.println("entered pin: " + answer + "\r\n");
            // check in the database if the pin is correct for the chosen account
            Boolean pinCorrect = dao.validatePin(selectedAccount, answer);
            //System.out.println("pinCorrect outcome: " + pinCorrect + "\r\n");

            if (pinCorrect) {
                //System.out.println("Pin is correct! starting login procedure... \r\n");
                // if the pin is correct we can send over the authenticated account to the atm machine for further processing
                AtmMachine.insertCard(selectedAccount);

                // after this we can get the current window and close it.
                Stage stage = (Stage) btnSubmit.getScene().getWindow();
                stage.close();
            } else {
                JOptionPane.showMessageDialog(null, "The pin code you have entered is incorrect.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please make sure the pin consists of 4 digits.");
        }
    }

    private Boolean pinFormatCorrect(PasswordField pw) {
        if (pw.getText().isEmpty() || pw.getText().length() < 4) {
            return false;
        } else {
            return true;
        }
    }

    private void initPwField() {
        // create a filter for the PIN text input so only numeric characters between 0-9 are accepted.
        // the filter acts whenever a change is detected on the element the filter is applied to.
        UnaryOperator<TextFormatter.Change> integerFilter = e -> {
            // when a change is detected like a typed key, an event happens
            // from this event we can get the text that the keypress would produce
            String newText = e.getText();
            // we check the captured text against this regex expression that only allows characters from 0 - 9 to be accepted
            if (newText.matches("[0-9]*")) {
                // if the captured text does match the condition it is returned and entered in the PasswordField
                return e;
            }
            // if the captured text does not match the condition we return null
            return null;
        };

        // here the filter is connected to the element as formatter
        txtPin.setTextFormatter(new TextFormatter<>(integerFilter));

        // custom listener that restricts the amount of input on the PIN
        txtPin.setOnKeyTyped(event -> {
            int maxChars = 4;
            if (txtPin.getText().length() == maxChars) {
                event.consume();
            }

        });
    }

    public void setAtmMachine(AtmMachine AtmMachine) {
        this.AtmMachine = AtmMachine;
    }

    public void setSelectedAccount(Account selectedAccount) {
        this.selectedAccount = selectedAccount;
    }

}
