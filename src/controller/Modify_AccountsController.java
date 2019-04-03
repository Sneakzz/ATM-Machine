/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import db.Dao;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javax.swing.JOptionPane;
import model.Account;
import model.AtmMachine;

/**
 * FXML Controller class
 *
 * @author Kenny
 */
public class Modify_AccountsController implements Initializable {

    @FXML
    private RadioButton radioAddAccount;
    @FXML
    private ToggleGroup modifyOperation;
    @FXML
    private RadioButton radioRemoveAccount;
    @FXML
    private RadioButton radioModifyAccount;
    @FXML
    private AnchorPane anchorAddAccount;
    @FXML
    private RadioButton radioNormal;
    @FXML
    private RadioButton radioSavings;
    @FXML
    private Button btnAddAccountSubmit;
    @FXML
    private AnchorPane anchorRemoveAccount;
    @FXML
    private Button btnRemoveAccountSubmit;
    @FXML
    private ComboBox<Account> cmbSelectAccount;
    @FXML
    private AnchorPane anchorModifyAccount;
    @FXML
    private Button btnModifyAccountSubmit;
    @FXML
    private ComboBox<Account> cmbSelectModify;
    @FXML
    private PasswordField txtOldPin;
    @FXML
    private PasswordField txtNewPin;
    @FXML
    private PasswordField txtPin;
    @FXML
    private ToggleGroup toggleAccountType;

    AtmMachine AtmMachine;
    Dao dao = new Dao();
    String radioButtonId = "radioAddAccount";
    String anchorPaneId = "anchorAddAccount";

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void clickedModifyOperation(MouseEvent event) {
        // get the new radioButton id
        String newRadioButtonId = ((RadioButton) event.getSource()).getId();
        //System.out.println("radioButtonId: " + newRadioButtonId + "\r\n");

        // use a switch case to handle the different radio buttons that can be clicked
        switch (newRadioButtonId) {
            case "radioAddAccount":
                anchorAddAccount.setVisible(true);
                anchorRemoveAccount.setVisible(false);
                anchorModifyAccount.setVisible(false);
                initAddAccountPane();
                break;
            case "radioRemoveAccount":
                anchorAddAccount.setVisible(false);
                anchorRemoveAccount.setVisible(true);
                anchorModifyAccount.setVisible(false);
                initRemoveAccountPane();
                break;
            case "radioModifyAccount":
                anchorAddAccount.setVisible(false);
                anchorRemoveAccount.setVisible(false);
                anchorModifyAccount.setVisible(true);
                initModifyAccountPane();
                break;
        }
    }

    private void initAddAccountPane() {
        initPwField(txtPin);
    }

    private void initRemoveAccountPane() {
        cmbSelectAccount.getItems().clear();
        ArrayList<Account> accounts = AtmMachine.getCurrentUser().getAccounts();

        if (!accounts.isEmpty()) {
            cmbSelectAccount.getItems().addAll(accounts);
            cmbSelectAccount.setValue(accounts.get(0));
        }
    }

    private void initModifyAccountPane() {
        cmbSelectModify.getItems().clear();
        ArrayList<Account> accounts = AtmMachine.getCurrentUser().getAccounts();

        if (!accounts.isEmpty()) {
            cmbSelectModify.getItems().addAll(accounts);
            cmbSelectModify.setValue(accounts.get(0));
        }

        initPwField(txtOldPin);
        initPwField(txtNewPin);
    }

    @FXML
    private void clickedSubmit(MouseEvent event) throws SQLException {
        String newAnchorPaneId = ((Button) event.getSource()).getParent().getId();
        //System.out.println("anchorPaneId: " + newAnchorPaneId + "\r\n");

        // first we need to check which anchorpane is visible and in use before we can handle the information
        // we also do this with a switch case
        switch (newAnchorPaneId) {
            case "anchorAddAccount":
                handleAddAccount();
                break;
            case "anchorRemoveAccount":
                handleRemoveAccount();
                break;
            case "anchorModifyAccount":
                handleModifyAccount();
                break;
        }
    }

    private void handleAddAccount() throws SQLException {
        //System.out.println("submit button clicked for add account pane");
        Boolean clearForSubmit = false;
        int chosenPin = 0;

        // get the type that is chosen
        String chosenType = ((RadioButton) toggleAccountType.getSelectedToggle()).getText().toLowerCase();
        //System.out.println("the chosentype is: " + chosenType + "\r\n");

        // get the entered pin
        // first check if the format for the entered pin is correct
        if (pinFormatCorrect(txtPin)) {
            chosenPin = Integer.parseInt(txtPin.getText());

            // clear the form for submit
            clearForSubmit = true;
        } else {
            JOptionPane.showMessageDialog(null, "Please make sure the pin consists of 4 digits.");
        }

        if (clearForSubmit) {
            // when the form is clear for submitting we have to check if the user can still have an account of the chosen type..
            //System.out.println("the result of the accountClearForCreation method: " + accountClearForCreation(chosenType) + "\r\n");
            if (accountClearForCreation(chosenType)) {
                // when the account is clear for creation we can send over the info so a new account can be made for the user
                AtmMachine.getCurrentUser().addAccount(chosenType, chosenPin);

                // when a new account is made we trigger a reset of the userlist
                AtmMachine.resetUserList();

                // after this, clear the password field and nothing else, since other actions might need to be taken inside this screen.
                txtPin.clear();
            } else {
                // inform the user that the account is not clear for creation so they have a chance to choose another type or exit
                JOptionPane.showMessageDialog(null, "the user allready has this type of account, feel free to try and create another type of account or exit");
            }
        }
    }

    private Boolean accountClearForCreation(String chosenType) {
        Boolean answer = null;
        // count the number of accounts the user has
        int amountOfNormalAccounts = 0;
        int amountOfSavingsAccounts = 0;

        for (Account account : AtmMachine.getCurrentUser().getAccounts()) {
            if (account.getType().equals("normal")) {
                amountOfNormalAccounts++;
            } else if (account.getType().equals("savings")) {
                amountOfSavingsAccounts++;
            }
        }

        switch (chosenType) {
            case "normal":
                if (amountOfNormalAccounts < 1) {
                    //System.out.println("normal account can be created \r\n");
                    answer = true;
                } else {
                    //System.out.println("user allready has a normal type account \r\n");
                    answer = false;
                }
                break;
            case "savings":
                if (amountOfSavingsAccounts < 1) {
                    //System.out.println("savings account can be created \r\n");
                    answer = true;
                } else {
                    //System.out.println("user allready has a savings type account \r\n");
                    answer = false;
                }
                break;
        }

        return answer;

    }

    private void handleRemoveAccount() throws SQLException {
        int choice = -1;
        //System.out.println("submit button clicked for remove account pane");

        // first we need to get the chosen account to remove
        Account selectedAccount = cmbSelectAccount.getSelectionModel().getSelectedItem();
        //System.out.println("this is the selected account for removal: " + selectedAccount + "\r\n");

        if (dao.getAccountBalance(selectedAccount.getAccountID()) > 0.0) {
            JOptionPane.showMessageDialog(null, "Please withdraw all available balance from the account before removing!");
        } else {
            // make sure the user is sure of the removal of the account. this action CANNOT be undone!
            choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove this account? \r\nNote that this action CANNOT be undone!", "Warning", JOptionPane.YES_NO_OPTION);
            //System.out.println("the value from the choice made: " + choice + "\r\n");
        }

        if (choice == 0) {
            //if the user clicked "yes" on the confirmation dialog the account can be removed
            Boolean accountRemoved = dao.removeAccount(selectedAccount);

            // after it is done, inform the user
            if (accountRemoved) {
                cmbSelectAccount.getItems().remove(selectedAccount);
                this.AtmMachine.getCurrentUser().removeAccount(selectedAccount);
                JOptionPane.showMessageDialog(null, "Account successfully removed!");
                
                this.AtmMachine.removeCard();
                // check if the user has any accounts left, if not, automatically delete the user as well
                if (this.AtmMachine.getCurrentUser().getAccounts().isEmpty()) {
                    this.AtmMachine.removeUser(this.AtmMachine.getCurrentUser());
                } 

                // when an account and or user is removed we reset the userlist and reinitialize the panes and so also those comboBoxes
                AtmMachine.resetUserList();
            } else {
                JOptionPane.showMessageDialog(null, "Account could not be removed!");
            }
        }

    }

    private void handleModifyAccount() throws SQLException {
        //System.out.println("submit button clicked for modify account pane");

        // get the selected account
        Account selectedAccount = cmbSelectModify.getSelectionModel().getSelectedItem();
        //System.out.println("this is the selected account for removal: " + selectedAccount + "\r\n");

        // check for formatting of both the pin numbers
        if (pinFormatCorrect(txtOldPin) && pinFormatCorrect(txtNewPin)) {
            // get the old pin
            // get the new pin
            int oldPin = Integer.parseInt(txtOldPin.getText());
            int newPin = Integer.parseInt(txtNewPin.getText());

            // check if the old pin is correct
            if (dao.validatePin(selectedAccount, oldPin)) {
                // if the old pin is correct, change the old pin to the new in the database
                Boolean pinChanged = dao.changePin(selectedAccount, newPin);

                // reset the user list if changing of the pin worked
                if (pinChanged) {
                    JOptionPane.showMessageDialog(null, "Pin successfully changed!");
                    txtOldPin.clear();
                    txtNewPin.clear();
                    AtmMachine.resetUserList();
                } else {
                    JOptionPane.showMessageDialog(null, "Pin could not be changed.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Old pin is incorrect, please enter the correct pin code first");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please make sure both the old and new pin are correctly filled in.");
        }
    }

    public void setAtmMachine(AtmMachine AtmMachine) {
        this.AtmMachine = AtmMachine;
    }

    private Boolean pinFormatCorrect(PasswordField pw) {
        if (pw.getText().isEmpty() || pw.getText().length() < 4) {
            return false;
        } else {
            return true;
        }
    }

    private void initPwField(PasswordField pw) {
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
        pw.setTextFormatter(new TextFormatter<>(integerFilter));

        // custom listener that restricts the amount of input on the PIN
        pw.setOnKeyTyped(event -> {
            int maxChars = 4;
            if (pw.getText().length() == maxChars) {
                event.consume();
            }

        });
    }

}
