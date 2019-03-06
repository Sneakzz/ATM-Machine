/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.Account;
import model.AtmMachine;
import model.Main;

/**
 * FXML Controller class
 *
 * @author Kenny
 */
public class Select_AccountController implements Initializable {

    @FXML
    private ListView<Account> lstAccounts;
    @FXML
    private Button btnSelect;

    AtmMachine AtmMachine;
    ObservableList<Account> listAccounts = FXCollections.observableArrayList();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // on showing of the screen, load in the accounts the user has into the listview
        lstAccounts.setItems(listAccounts);
    }

    @FXML
    private void clickedSelect(MouseEvent event) throws IOException {
        if (lstAccounts.getSelectionModel().getSelectedIndex() > -1) {
            // if an account is selected, we get that account
            Account selectedAccount = (Account) lstAccounts.getSelectionModel().getSelectedItem();
            System.out.println("this is the selected account: " + selectedAccount);

            // prompt the screen where the user enters their pin
            Main.displayEnterPinScreen(AtmMachine, selectedAccount);
            
            // after this we can get the current window and close it.
            Stage stage = (Stage) btnSelect.getScene().getWindow();
            stage.close();
        }
    }

    public void setAtmMachine(AtmMachine AtmMachine) {
        this.AtmMachine = AtmMachine;
    }

    public void setListAccounts(ArrayList<Account> accounts) {
        this.listAccounts.setAll(accounts);
    }

}
