/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import db.Dao;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.swing.JOptionPane;

/**
 *
 * @author Kenny
 */
public class AtmMachine {

    private ObservableList<User> listUsers;
    private Account insertedAccount;
    private User currentUser;

    private Dao dao = new Dao();

    public AtmMachine() {
        // initialize the list of users to make it ready for use
        this.listUsers = FXCollections.observableArrayList();
        
        // initialize a timer that handles an hourly check and reset User data inside the program
        // this is done because over time data will end up changing inside the database but not necessarely allways inside the program
        // so once every so often the list of users will be emptied and refilled with the correct data from the database
        // this timer will also serve the purpose of handling the intrest that gets added to savings accounts so that will need to happen
        // before the user list is purged and reset so the change immediately takes effect to the objects inside the program as well
        initResetTimer();
    }

    private void initResetTimer() {
        // timer executes every hour
        long execTime = TimeUnit.HOURS.toMillis(1);
        
        // for presentation purposes we make the timer execute every 3 minutes
        //long execTime = TimeUnit.MINUTES.toMillis(3);
        
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    try {
                        handleReset();
                    } catch (SQLException e) {
                        System.out.println("Something went wrong when trying to run the handleReset method in the reset timer \r\n");
                        System.out.println(e.getMessage());
                    }
                });

            }
        }, execTime, execTime);
    }

    private void handleReset() throws SQLException {
        System.out.println("Reset in progress.....\r\n");

        // generating intrest for savings accounts
        SavingsAccount.generateIntrest();
        
        // resetting all objects in the program with the information from the database
        resetUserList();

        System.out.println("Reset done! \r\n");
    }
    
    public void resetUserList() throws SQLException {
        if (listUsers.isEmpty()) {
            // if the list is empty for whatever reason we initialize the users from the database
            initUsers();
        } else {
            // if the list of users is not empty, we need to empty and refill it with database information to reset it
            listUsers.clear();
            initUsers();
        }
    }

    public void newUser(String lastName, String firstName, LocalDate birthdate, int pin, String type) {
        System.out.println("User is being created... \r\n");

        // create the new user
        User newUser = new User(lastName, firstName, birthdate, pin, type);
        System.out.println("User created! \r\n");

        // add the user to the database
        System.out.println("Adding user to the database... \r\n");
        dao.insertUser(newUser);

        // add user to the list of users
        addNewUserToList(newUser);
    }

    private void addNewUserToList(User newUser) {
        System.out.println("Adding user to the list... \r\n");
        listUsers.add(0, newUser);
        System.out.println("User is added to the list");

        // printing the current list
        //System.out.println("This is what the current list now looks like: ");
        //printListOfUsers();
    }

    // not in use right now but can be used for debugging or informational purposes
    private void printListOfUsers() {
        if (!listUsers.isEmpty()) {
            for (User user : listUsers) {
                System.out.println("\t" + user);
            }
        }
    }

    public void modifyUser(String newLastName, String newFirstName, LocalDate newBirthdate) throws SQLException {
        System.out.println("Changing the details in the database....");
        // first make the change in the database
        dao.changeUser(this.currentUser, newLastName, newFirstName, newBirthdate);
        
        // after changing a user we should also trigger a reset of the user list since that information becomes out of date after a user change has been made
        resetUserList();
    }
    
    public void removeUser(User selectedUser) throws SQLException {
        // we try to remove the user from the database
        Boolean userRemoved = dao.removeUser(selectedUser);
        
        // check if the user was successfully removed
        if (userRemoved) {
            // inform that the user was removed succesfully
            JOptionPane.showMessageDialog(null, "User successfully removed!");
            
            // then reset the user list to reflect the changes in the program
            resetUserList();
        } else {
            JOptionPane.showMessageDialog(null, "User could not be removed!");
        }
    }

    public void insertCard(Account selectedAccount) {
        // since the user entered the right pin we can accept the "card" or account into the machine
        this.insertedAccount = selectedAccount;
        System.out.println("Card accepted, welcome! \r\n");
    }

    public void removeCard() {
        System.out.println("Removing the card from the machine....\r\n");
        this.insertedAccount = null;
        this.currentUser = null;
        System.out.println("Card successfully removed! \r\n");
    }

    public void initUsers() throws SQLException {
        // right now this method assumes the list of users is allready empty because of a program shutdown
        System.out.println("Trying to initialize existing users and their accounts...\r\n");

        //for initializing the users and their accounts we need two ResultSets
        ResultSet rsUsers = dao.getUsers();
        ResultSet rsAccounts = dao.getAccounts();

        // check to make sure the resultsets aren't empty
        if (rsUsers != null && rsAccounts != null) {
            // go over every user
            while (rsUsers.next()) {
                // for every user encountered in the database, create a user object
                User newUser = new User(rsUsers.getNString(1), rsUsers.getNString(2), rsUsers.getDate(3).toLocalDate());
                System.out.println("new User created: " + newUser + "\r\n");
                // before moving on to the next user, first check the resultset that contains accounts so we can create the account objects
                // that this user should have and add them to the user list of accounts
                while (rsAccounts.next()) {
                    // go over every account in the resultset and if the lastname, firstname, and birthday is the same as the just created user
                    // we know it is his account and we create + add that account to the user
                    if (rsAccounts.getNString(1).equals(newUser.getLastName())
                            && rsAccounts.getNString(2).equals(newUser.getFirstName())
                            && rsAccounts.getDate(3).toLocalDate().isEqual(newUser.getBirthdate())) {
                        newUser.addAccount(rsAccounts.getNString(4), rsAccounts.getNString(5), rsAccounts.getNString(6), rsAccounts.getDouble(7), rsAccounts.getInt(8));
                        System.out.println("Account for user created! \r\n");
                    }
                }
                // move the cursor of the accounts ResultSet back to the front to get ready to do the search for the next user
                rsAccounts.beforeFirst();

                // evertime a user is entirely created with its account(s), add it to the list of users
                this.listUsers.add(newUser);
                System.out.println("User added to the list of users\r\n");
            }
        } else {
            // when there are no users in the database we at least output this to the console so we know we did not encounter a problem or error
            System.out.println("The database is empty, there are no users and accounts to initialize. \r\n");
        }
    }

    public ObservableList<User> getListOfUsers() {
        return this.listUsers;
    }

    public Account getInsertedAccount() {
        return this.insertedAccount;
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    public void setInsertedAccount(Account account) {
        this.insertedAccount = account;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

}
