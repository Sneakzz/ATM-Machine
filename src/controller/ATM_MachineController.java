/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javax.swing.JOptionPane;
import model.AtmMachine;
import model.Main;
import model.User;

/**
 * FXML Controller class
 *
 * @author Kenny
 */
public class ATM_MachineController implements Initializable {

    @FXML
    private ListView<User> lstUsers;
    @FXML
    private Button btnCardInOut;
    @FXML
    private MenuItem exit;
    @FXML
    private MenuItem modifyUser;
    @FXML
    private Button btnCheckBalance;
    @FXML
    private Button btnPay;
    @FXML
    private Button btnDepositCash;
    @FXML
    private Button btnWithdrawCash;
    @FXML
    private MenuItem modifyAccounts;
    @FXML
    private Label lblPay;
    @FXML
    private MenuItem removeUser;

    AtmMachine AtmMachine = new AtmMachine();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // upon startup of the program, try and use the db data to initialize all the users and their accounts
        try {
            AtmMachine.initUsers();
            lstUsers.setItems(AtmMachine.getListOfUsers());
            //System.out.println("Gui users list updated after initializing users! \r\n");
        } catch (SQLException e) {
            System.out.println("Something went wrong initializing users from the database on startup! \r\n");
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void clickedInteraction(MouseEvent event) throws SQLException, IOException {
        // before any of the interaction buttons actually do anything we need to make sure an account is inserted
        if (accountPresent()) {
            // if an account is present in the machine we grab the id of which button was pressed
            String btnID = ((Button) event.getSource()).getId();
            //System.out.println("the button that was just clicked: " + btnID + "\r\n");

            // depending on which button is clicked, different actions will be performed
            switch (btnID) {
                case "btnCheckBalance":
                    // for checking the balance there is no need to open another screen so this can be handled
                    // right from the checkBalance() that every Account has
                    AtmMachine.getInsertedAccount().checkBalance();
                    break;
                case "btnDepositCash":
                    // to make a deposit we also have to open another screen first to get additional info
                    // this could also be done using a JOptionPane but doesn't look as elegant..
                    //System.out.println("Opening deposit screen....\r\n");
                    Main.displayDepositScreen(AtmMachine, AtmMachine.getInsertedAccount());
                    break;
                case "btnWithdrawCash":
                    // this action is handled the same way as depositing
                    //System.out.println("Opening withdraw screen....\r\n");
                    Main.displayWithdrawScreen(AtmMachine, AtmMachine.getInsertedAccount());
                    break;
                case "btnPay":
                    // to make a payment we have to open another screen before the actual payment
                    // can be handled. this is necessary because addition we need addition information.
                    //System.out.println("Opening payment screen....\r\n");
                    Main.displayPaymentScreen(AtmMachine, AtmMachine.getInsertedAccount());
                    break;
            }
        }

    }

    @FXML
    private void clickedInOutCard(MouseEvent event) throws IOException {
        User selectedUser = null;
        if (!accountPresent()) {
            if (lstUsers.getSelectionModel().getSelectedIndex() > -1) {
                // if there is a user selected we get that user
                selectedUser = (User) lstUsers.getSelectionModel().getSelectedItem();
                //System.out.println("this is the selected user: " + selectedUser);

                // open the screen where the user can select the account to use
                Main.displaySelectAccountScreen(AtmMachine, selectedUser.getAccounts());
            }

            // when the machine has an inserted account we know the correct pin was entered
            if (accountPresent()) {
                // make the selectedUser the current user in the atm machine since that user has entered
                // his or her card
                AtmMachine.setCurrentUser(selectedUser);
                // update the gui accordingly (this would replace the changing of the button text as it would be handled
                // inside the updateGUI method
                updateGUI();
            }
        } else {
            // this block handles the functionality of when a card is in fact inserted and needs to be withdrawn
            AtmMachine.removeCard();
            updateGUI();
        }
    }

    @FXML
    private void clickedNewUser(MouseEvent event) throws IOException {
        // open new user screen
        Main.displayNewUserScreen(AtmMachine);

        // update the gui list in case a new user is added
        lstUsers.setItems(AtmMachine.getListOfUsers());
        //System.out.println("Gui users list updated after creating a new user! \r\n");
    }

    @FXML
    private void clickedOptionsMenu(ActionEvent event) throws IOException, SQLException {
        // get the id that is connected to the clicked menu item
        String menuItem = ((MenuItem) event.getSource()).getId();

        switch (menuItem) {
            case "modifyUser":
                // when the user clicks modify in the menu we first check if there is a user selected
                if (lstUsers.getSelectionModel().getSelectedIndex() > -1) {
                    // if a user is selected, we get that user
                    User selectedUser = (User) lstUsers.getSelectionModel().getSelectedItem();
                    //System.out.println("this is the selected user: " + selectedUser + "\r\n");

                    // we set the current user in the atm machine
                    AtmMachine.setCurrentUser(selectedUser);
                    //System.out.println("AtmMachine current user is now: " + selectedUser + "\r\n");

                    // after that we can open the screen to modify this user
                    //System.out.println("Opening the Modify User screen.... \r\n");
                    Main.displayModifyUserScreen(AtmMachine);
                } else {
                    JOptionPane.showMessageDialog(null, "Please select a user from the list first.");
                }
                break;
            case "removeUser":
                if (lstUsers.getSelectionModel().getSelectedIndex() > -1) {
                    // if there is a user selected we get that user
                    User selectedUser = (User) lstUsers.getSelectionModel().getSelectedItem();
                    //System.out.println("this is the selected user: " + selectedUser);
                    
                    // prompt if the user is sure the account can be removed
                    int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove this user? \r\nNote that this action will also remove all accounts and CANNOT be undone!", "Warning", JOptionPane.YES_NO_OPTION);
                    
                    if (choice == 0) {
                        // this means the user can be removed
                        AtmMachine.removeUser(selectedUser);
                    }

                } else {
                    JOptionPane.showMessageDialog(null, "Please select a user from the list first.");
                }
                break;
            case "modifyAccounts":
                // when the add account button is clicked again we first check that there is a user selected
                if (lstUsers.getSelectionModel().getSelectedIndex() > -1) {
                    // if there is a user selected we get that user
                    User selectedUser = (User) lstUsers.getSelectionModel().getSelectedItem();
                    //System.out.println("this is the selected user: " + selectedUser);
                    
                    // we set the current user in the atm machine
                    AtmMachine.setCurrentUser(selectedUser);
                    //System.out.println("AtmMachine current user is now: " + selectedUser + "\r\n");

                    // after that we can open the screen to modify an account for the user
                    //System.out.println("Opening the Modify Accounts screen.... \r\n");
                    Main.displayModifyAccountsScreen(AtmMachine);

                } else {
                    JOptionPane.showMessageDialog(null, "Please select a user from the list first.");
                }
                break;
            case "exit":
                System.exit(0);
                Platform.exit();
                break;
        }
    }

    private void updateGUI() {
        // when this method is called, first check if there is an account present in the machine.
        if (accountPresent()) {
            // first of all if a card is inserted we change the button text to reflect the possibility of withdrawing it
            btnCardInOut.setText("Withdraw Card");

            //System.out.println("Checking the account type... \r\n");
            // if there is an account present, get its type
            String currentAccountType = AtmMachine.getInsertedAccount().getType();
            //System.out.println("inserted account type: " + currentAccountType + "\r\n");

            // switch case statement to handle the different card types
            switch (currentAccountType) {
                case "savings":
                    // when the inserted account is of type savings we disable the pay button as this is not possible
                    // with a savings account as a restriction
                    btnPay.setDisable(true);
                    // also create a tooltip for the button in case anyone wonders why it doesn't work
                    Tooltip tooltip = new Tooltip("It is not possible to perform payments with a savings account");
                    lblPay.setTooltip(tooltip);
                    break;
            }
        } else {
            // when no account is inserted we change back the text, make sure all the buttons go back to their original state
            // and we clear the selection from the list of users ready for another user to be selected
            btnCardInOut.setText("Insert Card");
            btnCheckBalance.setDisable(false);
            btnWithdrawCash.setDisable(false);
            btnDepositCash.setDisable(false);
            btnPay.setDisable(false);
            lstUsers.getSelectionModel().clearSelection();

            /* there is a possibility here to switch to a generic welcome/home screen or panel whenever no card is inserted in the machine. */
        }
    }

    private Boolean accountPresent() {
        if (AtmMachine.getInsertedAccount() == null) {
            return false;
        } else {
            return true;
        }
    }
}
