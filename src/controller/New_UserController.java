/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import db.Dao;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import model.AtmMachine;

/**
 * FXML Controller class
 *
 * @author Kenny
 */
public class New_UserController implements Initializable {

    @FXML
    private PasswordField txtPin;
    @FXML
    private DatePicker Birthdate;
    @FXML
    private TextField txtLastName;
    @FXML
    private TextField txtFirstName;
    @FXML
    private RadioButton radioNormal;
    @FXML
    private ToggleGroup accountType;
    @FXML
    private RadioButton radioSavings;
    @FXML
    private Button btnSubmit;

    AtmMachine AtmMachine;
    Dao dao = new Dao();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // to initialise a DatePicker object we can set its value with this method
        // and either use LocalDate.of(2010, 06, 06) or the following...
        Birthdate.setValue(LocalDate.now());
        //Birthdate.setValue(LocalDate.of(1990, 06, 06));

        // initialisation of the password field
        initPwField();

    }

    @FXML
    private void clickedSubmit(MouseEvent event) throws NoSuchMethodException, NoSuchFieldException, SQLException {
        // boolean that checks overall correctness of the form
        Boolean clearForSubmit = false;

        // a reference date gets created from the current date so we can check for age
        // upon creating a new user (currently saying that we cannot have users under the age of 16)
        LocalDate current = LocalDate.now();
        LocalDate ref = LocalDate.of(current.getYear() - 16, current.getMonthValue(), current.getDayOfMonth());

        // getValue() from a DatePicker object returns a LocalDate object
        // value returned is in this format: YYYY/MM/DD
        LocalDate chosenDate = Birthdate.getValue();

        String chosenType = "";

        // check if needed details are filled in correctly
        // first we check if the first name and last name are correctly filled in
        if (nameValid(txtLastName) && nameValid(txtFirstName)) {
            if (oldEnough(chosenDate, ref)) {
                // if the fields are correctly filled in and the user is old enough,
                // check the db if the user allready exists
                if (!userExists(txtFirstName, txtLastName, chosenDate)) {
                    // if the user does not allready exist we can lastly, check if the pin is 
                    // correctly filled in
                    if (pinValid(txtPin)) {
                        // if the pin is valid, then it means everything else is as well and the user does not allready exist.
                        // at this point we can get the text for the chosen account type.
                        // so we get the selected toggle from the group and cast the object to RadioButton
                        // otherwise we cannot get the text from it
                        RadioButton selectedButton = (RadioButton) accountType.getSelectedToggle();
                        chosenType = selectedButton.getText().toLowerCase();
                        // after getting the chosen account type we set the clear for submit to true
                        clearForSubmit = true;
                    } else {
                        JOptionPane.showMessageDialog(null, "Please correctly fill in the pin.");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "This user allready exists.");
                }
            } else {
                // if the user isn't old enough we also inform the user about this
                JOptionPane.showMessageDialog(null, "Please make sure you are 16 or older.");
            }
        } else {
            // if either first or last name, or both, are not correctly filled in
            // inform the user
            JOptionPane.showMessageDialog(null, "Please correctly fill in the first and last name.");
        }

        // check and make sure the form is clear for submitting
        if (clearForSubmit) {
            System.out.println("Form clear for submit! \r\n");

            // send all the user information over to be handled by the AtmMachine object
            AtmMachine.newUser(txtLastName.getText(), txtFirstName.getText(), chosenDate, Integer.parseInt(txtPin.getText()), chosenType);

            // after this we can get the current window and close it.
            Stage stage = (Stage) btnSubmit.getScene().getWindow();
            stage.close();
        }
    }

    /*
        Methods starting from this comment section will always be needed for the form 
        no matter what! do not touch!
     */
    private Boolean userExists(TextField firstName, TextField lastName, LocalDate chosenDate) throws SQLException {
        // get the name
        String fName = firstName.getText();
        String lName = lastName.getText();
        // send it over to the method that handles checking the db
        Boolean nameExists = dao.checkUser(fName, lName, chosenDate);

        return nameExists;
    }

    private Boolean pinValid(PasswordField pw) {
        if (pw.getText().isEmpty() || pw.getText().length() < 4) {
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

    private void initPwField() {
        // create a filter for the PIN text input so only numeric characters between 0-9 are accepted.
        // the filter acts whenever a change is detected on the element the filter is applied to.
        UnaryOperator<Change> integerFilter = e -> {
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
        txtPin.setTextFormatter(new TextFormatter<String>(integerFilter));

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

}
