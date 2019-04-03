/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import db.Dao;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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
public class WithdrawController implements Initializable {

    @FXML
    private Button btnSubmit;
    @FXML
    private TextField txtAmount;

    AtmMachine AtmMachine;
    Account insertedAccount;
    Dao dao = new Dao();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initAmountField();
    }

    @FXML
    private void clickedSubmit(MouseEvent event) throws SQLException {
        Boolean clearForSubmit = false;
        double amount = 0.00;

        // check to make sure the amount field is not empty or just a dot..
        if (!txtAmount.getText().isEmpty() && !txtAmount.getText().equals(".")) {
            // we wrap the parsing from string to a double in a try catch block because users could always just try
            // to type something weird and crash the program so this way we at least catch the error if one occurs.
            try {
                amount = Double.parseDouble(txtAmount.getText());
            } catch (NumberFormatException e) {
                // catch the exception so it doesn't crash the program and inform the user that the format of their input is incorrect
                System.out.println("numberFormatException when trying to convert input string to double for withdrawal.");
                JOptionPane.showMessageDialog(null, "Pleade make sure to enter an amount to withdraw in this format: #.##");
                System.out.println(e.getMessage());
            }

            // make sure if the given input can be parsed to a double, that the amount is over 0.00
            if (amount > 0.00) {
                //System.out.println("the amount entered: " + amount + " \r\n");

                // check if the user has enough balance to make a withdrawal
                // we use BigDecimal to round the double result to 2 decimal places and still have the result as a double to use
                double result = new BigDecimal((dao.getAccountBalance(insertedAccount.getAccountID()) - amount)).setScale(2, RoundingMode.HALF_UP).doubleValue();
                //System.out.println("current balance: " + dao.getAccountBalance(insertedAccount.getAccountID()) + "\r\n"
                        //+ "amount to withdraw: " + amount + "\r\n"
                        //+ "balance left over: " + result + "\r\n");

                Boolean validWithdrawal = (dao.getAccountBalance(insertedAccount.getAccountID()) - amount >= 0.00);

                //System.out.println("valid withdrawal: " + validWithdrawal + "\r\n");
                if (validWithdrawal) {
                    // if the withdrawal is valid we can clear the form for submit
                    clearForSubmit = true;
                    //System.out.println("withdrawal form cleared for submit! \r\n");
                } else {
                    JOptionPane.showMessageDialog(null, "It seems you do not have enough balance to make this withdrawal..");
                }

            } else {
                JOptionPane.showMessageDialog(null, "Please make sure that the amount to withdraw is over 0.00");
            }

        } else {
            System.out.println("no amount to withdraw was entered or it was just a dot.. \r\n");
            JOptionPane.showMessageDialog(null, "Please make sure to enter an amount to withdraw in this format: #.##");
        }
        
        if (clearForSubmit) {
            // get the original balance from the account so it can be passed through
            double originalBalance = dao.getAccountBalance(insertedAccount.getAccountID());
            // send over the information for the deposit to be handled
            insertedAccount.withdraw(amount, originalBalance);
            
            // after this we can get the current window and close it.
            Stage stage = (Stage) btnSubmit.getScene().getWindow();
            stage.close();
        }
    }

    private void initAmountField() {
        // create a filter for the amount text input so only numeric characters between 0-9 are accepted.
        // in addition we will also need to check for a dot since we will always need a double value
        // the filter acts whenever a change is detected on the element the filter is applied to.
        UnaryOperator<TextFormatter.Change> amountFilter = e -> {
            // when a change is detected like a typed key, an event happens
            // from this event we can get the text that the keypress would produce
            String character = e.getText();
            // we check the captured text against this regex expression that only allows characters from 0 - 9 to be accepted
            if (character.matches("[0-9]*")) {
                return e;

                // extra check for the "." and to make sure there cannot be more then 1 dot entered.
            } else if (character.matches("[.]*") && !txtAmount.getText().contains(".")) {
                return e;
            } else {
                // if the captured text does not match the condition we return null
                return null;
            }
        };

        // here the filter is connected to the element as formatter
        txtAmount.setTextFormatter(new TextFormatter<String>(amountFilter));

        // custom listener that restricts the amount of input on the amount field
        txtAmount.setOnKeyTyped(event -> {
            int maxChars = 8;
            if (txtAmount.getText().length() == maxChars) {
                event.consume();
            } else if (txtAmount.getText().contains(".") && txtAmount.getText().length() >= txtAmount.getText().indexOf(".") + 3) {
                event.consume();
            }
        });
    }

    public void setAtmMachine(AtmMachine AtmMachine) {
        this.AtmMachine = AtmMachine;
    }

    public void setSelectedAccount(Account insertedAccount) {
        this.insertedAccount = insertedAccount;
    }

}
