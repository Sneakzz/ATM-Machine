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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import model.*;

/**
 * FXML Controller class
 *
 * @author Kenny
 */
public class PayController implements Initializable {

    @FXML
    private TextField txtTo;
    @FXML
    private TextField txtFrom;
    @FXML
    private TextArea txtMessage;
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
        // first initialize the amount field
        initAmountField();
    }

    @FXML
    private void clickedSubmit(MouseEvent event) throws SQLException {
        Boolean clearForSubmit = false;
        double amount = 0.00;
        Account destAccount = null;

        // the user will need to fill out the TO field with the accountID of the account he wishes to make a payment to
        // so we make sure that it is filled in with a valid user accountID.
        if (validID(txtTo)) {
            // if the entered value is at least the correct length we can use this to check the database if the accountID exists or not.
            //System.out.println("checking if the entered destination accountID exists.... \r\n");
            if (dao.accountIdExists(txtTo.getText())) {
                //System.out.println("account with id: " + txtTo.getText() + " exists! \r\n");
                destAccount = getDestAccount(txtTo.getText());

                // now get the chosen amount to pay
                if (!txtAmount.getText().isEmpty() && !txtAmount.getText().equals(".")) {
                    // we have to wrap the conversion from string to double in a try catch block because users could always just try
                    // to type something weird and crash the program so this way at least we catch the error if one occurs.
                    try {
                        amount = Double.parseDouble(txtAmount.getText());
                    } catch (NumberFormatException e) {
                        System.out.println("numberFormatException when trying to convert input string to double for payment.");
                        JOptionPane.showMessageDialog(null, "Pleade make sure to enter an amount to pay in this format: #.##");
                        System.out.println(e.getMessage());
                    }

                    if (amount > 0.00) {
                        //System.out.println("the amount entered: " + amount + " \r\n");

                        // check if the payer has enough balance to make the payment
                        // we use BigDecimal to round the double result to 2 decimal places and still have the result as a double to use
                        double result = new BigDecimal((dao.getAccountBalance(insertedAccount.getAccountID()) - amount)).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        //System.out.println("current balance: " + dao.getAccountBalance(insertedAccount.getAccountID()) + "\r\n"
                                //+ "amount to pay: " + amount + "\r\n"
                                //+ "amount left over: " + result + "\r\n");

                        Boolean validPayment = (dao.getAccountBalance(insertedAccount.getAccountID()) - amount >= 0.00);

                        //System.out.println("valid payment: " + validPayment + "\r\n");
                        if (validPayment) {
                            // at this point we can grab the message if there is one
                            if (!txtMessage.getText().isEmpty()) {
                                String message = txtMessage.getText();
                            }
                            
                            // clear the form for submit
                            clearForSubmit = true;
                            //System.out.println("Payment form cleared for submit! \r\n");
                        } else {
                            JOptionPane.showMessageDialog(null, "It seems you do not have enough balance to make this transaction..");
                        }

                    } else {
                        JOptionPane.showMessageDialog(null, "Please make sure that the amount to pay is over 0.00");
                    }

                } else {
                    System.out.println("no amount to pay was entered or it was just a dot.. \r\n");
                    JOptionPane.showMessageDialog(null, "Please make sure to enter an amount to pay in this format: #.##");
                }

                // a message is not necessary but if there does happen to be one then we get it
                if (!txtMessage.getText().isEmpty()) {

                }
            } else {
                System.out.println("account with id: " + txtTo.getText() + " does not exist \r\n");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please make sure to fill in the accountID of the account you wish to transfer to.\r\n this accountID consists of 2 letters and 4 numbers");
        }
        
        if (clearForSubmit) {
            // send over the needed info to where the rest of the payment will be handled
            insertedAccount.pay(destAccount, insertedAccount, amount);
            
            // after this we can get the current window and close it.
            Stage stage = (Stage) btnSubmit.getScene().getWindow();
            stage.close();
        }
    }
    
    private Account getDestAccount(String accountIdTo) {
        Account destAccount = null;
        for (User user : AtmMachine.getListOfUsers()) {
            for (Account account : user.getAccounts()) {
                if (account.getAccountID().equals(accountIdTo)) {
                    destAccount = account;
                    break;
                }
            }
        }
        return destAccount;
    }

    private Boolean validID(TextField textField) {
        if (textField.getText().isEmpty() || textField.getText().length() != 6) {
            return false;
        } else {
            return true;
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
            } else if(txtAmount.getText().contains(".") && txtAmount.getText().length() >= txtAmount.getText().indexOf(".") + 3) {
                event.consume();
            }
        });
    }

    public void setAtmMachine(AtmMachine AtmMachine) {
        this.AtmMachine = AtmMachine;
    }

    public void setSelectedAccount(Account insertedAccount) {
        this.insertedAccount = insertedAccount;
        // as soon as the account is inserted into the controller we have to use its
        // details to prefill the "FROM" field.
        txtFrom.setText(insertedAccount.getAccountID());
    }

}
