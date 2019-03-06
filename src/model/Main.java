/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import controller.*;
import java.io.IOException;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

/**
 *
 * @author Kenny
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/ATM_Machine.fxml"));

        Scene scene = new Scene(root);
        stage.setTitle("ATM Machine");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public void stop() {
        System.exit(0);
        Platform.exit();
    }

    // the way this method creates a new stage is a way to pass through objects from one controller to the next
    public static void displayNewUserScreen(AtmMachine AtmMachine) throws IOException {
        // build the stage and show the new user screen
        Stage stage = new Stage();
        
        // get the layout (in this case, fxml file)
        // we need to do it this way so we can pass the AtmMachine object between controllers
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("/view/New_User.fxml"));
        Parent fxml = loader.load();

        // we get the controller for the new user creation screen
        New_UserController nuc = loader.getController();
        // now we can perform methods from it right in the main class
        // and pass through the AtmMachine object we got from the ATM_MachineController
        nuc.setAtmMachine(AtmMachine);
        
        // create a new scene with the layout
        Scene scene = new Scene(fxml);

        stage.setTitle("Create new user");

        // the initModality(Modality.APPLICATION_MODAL) statement sets the modality of the created stage
        // what this does is make sure that this window is handled first (closed) before the main program
        // can be interacted with again
        stage.initModality(Modality.APPLICATION_MODAL);

        // lambda handler for the "X" button that closes the window
        stage.setOnCloseRequest(e -> {
            // consume the close event so we can handle it ourselves
            e.consume();
            // custom method for handling the closing of the window
            int answer = JOptionPane.showConfirmDialog(null, "Are you sure you want to close the window?", "Warning", JOptionPane.YES_NO_OPTION);
            if (answer == 0) {
                stage.close();
            }
        });

        stage.setScene(scene);
        stage.showAndWait();
    }
    
    public static void displayModifyUserScreen(AtmMachine AtmMachine) throws IOException {
        Stage stage = new Stage();
        
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("/view/Modify_User.fxml"));
        Parent fxml = loader.load(); // this statement triggers the initialize method in the controller
        
        Modify_UserController muc = loader.getController();
        muc.initMucController(AtmMachine);
        
        Scene scene = new Scene(fxml);
        
        stage.setTitle("Modify user");
        stage.initModality(Modality.APPLICATION_MODAL);
        
        stage.setOnCloseRequest(e -> {
            e.consume();
            
            int answer = JOptionPane.showConfirmDialog(null, "Are you sure you want to close the window?", "Warning", JOptionPane.YES_NO_OPTION);
            if (answer == 0) {
                stage.close();
            }
        });
        
        stage.setScene(scene);
        stage.showAndWait();
    }
    
    public static void displayModifyAccountsScreen(AtmMachine AtmMachine) throws IOException {
        Stage stage = new Stage();
        
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("/view/Modify_Accounts.fxml"));
        Parent fxml = loader.load();
        
        Modify_AccountsController mac = loader.getController();
        mac.setAtmMachine(AtmMachine);
        
        Scene scene = new Scene(fxml);
        
        stage.setTitle("Modify Accounts");
        stage.initModality(Modality.APPLICATION_MODAL);
        
        stage.setOnCloseRequest(e -> {
            e.consume();
            
            int answer = JOptionPane.showConfirmDialog(null, "Are you sure you want to close the window?", "Warning", JOptionPane.YES_NO_OPTION);
            if (answer == 0) {
                stage.close();
            }
        });
        
        stage.setScene(scene);
        stage.showAndWait();
    }
    
    public static void displaySelectAccountScreen(AtmMachine AtmMachine, ArrayList<Account> listAccounts) throws IOException {
        Stage stage = new Stage();
        
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("/view/Select_Account.fxml"));
        Parent fxml = loader.load();
        
        Select_AccountController sac = loader.getController();
        sac.setAtmMachine(AtmMachine);
        sac.setListAccounts(listAccounts);
        
        Scene scene = new Scene(fxml);
        
        stage.setTitle("Select account");
        stage.initModality(Modality.APPLICATION_MODAL);
        
        stage.setOnCloseRequest(e -> {
            e.consume();
            
            int answer = JOptionPane.showConfirmDialog(null, "Are you sure you want to close the window?", "Warning", JOptionPane.YES_NO_OPTION);
            if (answer == 0) {
                stage.close();
            }
        });
        
        stage.setScene(scene);
        stage.showAndWait();
    }
    
    public static void displayEnterPinScreen(AtmMachine AtmMachine, Account selectedAccount) throws IOException {
        Stage stage = new Stage();
        
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("/view/Enter_Pin.fxml"));
        Parent fxml = loader.load();
        
        Enter_PinController epc = loader.getController();
        epc.setAtmMachine(AtmMachine);
        epc.setSelectedAccount(selectedAccount);
        
        Scene scene = new Scene(fxml);
        
        stage.setTitle("Enter pin");
        stage.initModality(Modality.APPLICATION_MODAL);
        
        stage.setOnCloseRequest(e -> {
            e.consume();
            
            int answer = JOptionPane.showConfirmDialog(null, "Are you sure you want to close the window?", "Warning", JOptionPane.YES_NO_OPTION);
            if (answer == 0) {
                stage.close();
            }
        });
        
        stage.setScene(scene);
        stage.showAndWait();
    }
    
    public static void displayPaymentScreen(AtmMachine AtmMachine, Account insertedAccount) throws IOException {
        Stage stage = new Stage();
        
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("/view/Pay.fxml"));
        Parent fxml = loader.load();
        
        PayController pc = loader.getController();
        pc.setAtmMachine(AtmMachine);
        pc.setSelectedAccount(insertedAccount);
        
        Scene scene = new Scene(fxml);
        
        stage.setTitle("Payment");
        stage.initModality(Modality.APPLICATION_MODAL);
        
        stage.setOnCloseRequest(e -> {
            e.consume();
            
            int answer = JOptionPane.showConfirmDialog(null, "Are you sure you want to close the window?", "Warning", JOptionPane.YES_NO_OPTION);
            if (answer == 0) {
                stage.close();
            }
        });
        
        stage.setScene(scene);
        stage.showAndWait();
    }
    
    public static void displayDepositScreen(AtmMachine AtmMachine, Account insertedAccount) throws IOException {
        Stage stage = new Stage();
        
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("/view/Deposit.fxml"));
        Parent fxml = loader.load();
        
        DepositController dc = loader.getController();
        dc.setAtmMachine(AtmMachine);
        dc.setSelectedAccount(insertedAccount);
        
        Scene scene = new Scene(fxml);
        
        stage.setTitle("Deposit");
        stage.initModality(Modality.APPLICATION_MODAL);
        
        stage.setOnCloseRequest(e -> {
            e.consume();
            
            int answer = JOptionPane.showConfirmDialog(null, "Are you sure you want to close the window?", "Warning", JOptionPane.YES_NO_OPTION);
            if (answer == 0) {
                stage.close();
            }
        });
        
        stage.setScene(scene);
        stage.showAndWait();
    }
    
    public static void displayWithdrawScreen(AtmMachine AtmMachine, Account insertedAccount) throws IOException {
        Stage stage = new Stage();
        
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("/view/Withdraw.fxml"));
        Parent fxml = loader.load();
        
        WithdrawController wc = loader.getController();
        wc.setAtmMachine(AtmMachine);
        wc.setSelectedAccount(insertedAccount);
        
        Scene scene = new Scene(fxml);
        
        stage.setTitle("Withdraw");
        stage.initModality(Modality.APPLICATION_MODAL);
        
        stage.setOnCloseRequest(e -> {
            e.consume();
            
            int answer = JOptionPane.showConfirmDialog(null, "Are you sure you want to close the window?", "Warning", JOptionPane.YES_NO_OPTION);
            if (answer == 0) {
                stage.close();
            }
        });
        
        stage.setScene(scene);
        stage.showAndWait();
    }

}
