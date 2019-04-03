/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import model.*;

/**
 * FXML Controller class
 *
 * @author Kenny
 */
public class Modify_UserController implements Initializable {

    @FXML
    private TextField txtNewLastName;
    @FXML
    private TextField txtNewFirstName;
    @FXML
    private DatePicker newBirthdate;
    @FXML
    private Button btnSubmit;

    AtmMachine AtmMachine;
    User currentUser;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {  
    }
    
    // instead of using the initialize method of the controller we use our own custom starting method
    // reason for this is that the initialize method is called from the loader.load() method in the Main class.
    // this is a problem because at that point the AtmMachine object is set yet and so we cannot access its
    // variables and methods to autofill the textfields.
    public void initMucController(AtmMachine AtmMachine) {
        this.AtmMachine = AtmMachine;
        //first set the currentUser
        this.currentUser = AtmMachine.getCurrentUser();
        // initialize the text fiels with the allready existing values
        txtNewLastName.setText(currentUser.getLastName());
        txtNewFirstName.setText(currentUser.getFirstName());
        newBirthdate.setValue(currentUser.getBirthdate());
    }

    @FXML
    private void clickedSubmit(MouseEvent event) throws SQLException {
        Boolean clearForSubmit = false;
        
        // creating reference date to make sure the date entered is valid.
        LocalDate current = LocalDate.now();
        LocalDate ref = LocalDate.of(current.getYear() - 16, current.getMonthValue(), current.getDayOfMonth());
        
        // get the entered values
        String newLastName = txtNewLastName.getText();
        String newFirstName = txtNewFirstName.getText();
        LocalDate chosenDate = newBirthdate.getValue();
        
        //first make sure the input fields are valid
        if(nameValid(txtNewLastName) && nameValid(txtNewFirstName)) {
            // if the name fields are valid we check the chosen date to make sure the user would still be old enough after the change
            if (oldEnough(chosenDate, ref)) {
                // if both of these checks pass it can still mean that nothing has changed since data is prefilled
                // so first check if a change has even been made
                if(changeMade(newLastName, newFirstName, chosenDate)) {
                    // if this all passes we know everything is valid and at least 1 change has been made that is also still valid
                    // at this point we can clear the form for submit
                    clearForSubmit = true;
                } else {
                    JOptionPane.showMessageDialog(null, "Nothing has changed, make sure that any needed change has been made before clicking the submit button.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please make sure you are 16 or older.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please correctly fill in the first and last name.");
        }
        
        if(clearForSubmit) {
            //System.out.println("Modify User form is clear for submitting \r\n");
            
            // send all the user information over to be handled by the AtmMachine object
            AtmMachine.modifyUser(newLastName, newFirstName, chosenDate);

            // after this we can get the current window and close it.
            Stage stage = (Stage) btnSubmit.getScene().getWindow();
            stage.close();
        }
        
    }
    
    private Boolean changeMade(String newLastName, String newFirstName, LocalDate newBirthdate) {
        String oldLastName = currentUser.getLastName();
        String oldFirstName = currentUser.getFirstName();
        LocalDate oldBirthdate = currentUser.getBirthdate();
        
        if (oldLastName.equals(newLastName) && oldFirstName.equals(newFirstName) && oldBirthdate.isEqual(newBirthdate)) {
            return false;
        } else {
            return true;
        }
    }
    
    
    private Boolean oldEnough(LocalDate chosenDate, LocalDate refDate) {
        return (chosenDate.isBefore(refDate) || chosenDate.isEqual(refDate));
    }

    private Boolean nameValid(TextField textField) {
        if (textField.getText().isEmpty()) {
            return false;
        } else if (textField.getText().length() < 2 || textField.getText().length() > 25) {
            return false;
        } else {
            return true;
        }
    }
}
