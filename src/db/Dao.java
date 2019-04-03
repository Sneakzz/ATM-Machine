/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import model.*;

/**
 *
 * @author Kenny
 */
public class Dao {

    private Connection connection;
    private PreparedStatement pStatement;
    private ResultSet rs;

    public Boolean checkUser(String firstName, String lastName, LocalDate chosenDate) throws SQLException {
        String query = "SELECT firstname, lastname, birthdate FROM users WHERE firstname = ? AND lastname = ? AND birthdate = ?";

        //System.out.println("checkUser query: " + query);

        try {
            // set up connection
            connection = DbSingleton.getDbSingleton().getConnection();
            // create pStatement object
            pStatement = connection.prepareStatement(query);
            pStatement.setString(1, firstName);
            pStatement.setString(2, lastName);
            pStatement.setString(3, String.valueOf(chosenDate));
            // create resultset
            rs = pStatement.executeQuery();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        if (!rs.isBeforeFirst()) {
            System.out.println("this user does not exist yet \r\n");
            return false;
        } else {
            while (rs.next()) {
                System.out.println("user allready exists \r\n");
                System.out.println("found user: \r\n"
                        + "\t first name: " + rs.getNString(1) + "\r\n"
                        + "\t last name: " + rs.getNString(2) + "\r\n"
                        + "\t birthdate: " + rs.getDate(3) + "\r\n");
            }
            return true;
        }
    }

    public void insertUser(User newUser) {
        String query = "INSERT INTO users (firstname, lastname, birthdate) VALUES (?, ?, ?);";

        //System.out.println("insertUser query: " + query + "\r\n");

        try {
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            pStatement.setString(1, newUser.getFirstName());
            pStatement.setString(2, newUser.getLastName());
            pStatement.setString(3, String.valueOf(newUser.getBirthdate()));
            pStatement.executeUpdate();
            
            //System.out.println("User inserted into database! \r\n");
            //System.out.println("Adding accounts to the database \r\n");
            insertAccounts(newUser.getAccounts(), newUser);
        } catch (SQLException e) {
            System.out.println("Something went terribly wrong when trying to insert a user into the database.... \r\n");
            System.out.println(e.getMessage());
        }

    }

    private void insertAccounts(ArrayList<Account> accounts, User newUser) throws SQLException {
        // for every account in the users' list of accounts we add it to the database
        for (Account account : accounts) {
            // build the query to insert into the accounts table
            String query = "INSERT INTO accounts (accountid, userid, accounttype, accountnumber, balance) VALUES (?, ?, ?, ?, ?);";
            
            //System.out.println("The insertAccount query: " + query);
            
            int userid = getUserId(newUser.getFirstName(), newUser.getLastName(), newUser.getBirthdate());

            try {
                connection = DbSingleton.getDbSingleton().getConnection();
                
                pStatement = connection.prepareStatement(query);
                pStatement.setString(1, account.getAccountID());
                pStatement.setInt(2, userid);
                pStatement.setString(3, account.getType());
                pStatement.setString(4, account.getAccountNumber());
                pStatement.setDouble(5, account.getBalance());
                pStatement.executeUpdate();
                
                //System.out.println("Account with id: " + account.getAccountID() + " has been added to the database \r\n");
                // after inserting a particular account to the database we also need to 
                // insert the authentication for the account
                insertAuth(account);
            } catch (SQLException e) {
                System.out.println("Something went wrong when inserting the accounts into the database! \r\n");
                System.out.println(e.getMessage());
            }
        }
    }

    private void insertAuth(Account account) {
        String query = "INSERT INTO auth (accountid, pin) VALUES (?, ?);";
        //System.out.println("The insertAuth query: " + query);

        try {
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            pStatement.setString(1, account.getAccountID());
            pStatement.setInt(2, account.getPin());
            pStatement.executeUpdate();
            
            //System.out.println("Authentication for account with id: " + account.getAccountID() + " has been added to the database \r\n");
        } catch (SQLException e) {
            System.out.println("Something went wrong when inserting the authentication into the database! \r\n");
            System.out.println(e.getMessage());
        }
    }

    public void changeUser(User currentUser, String newLastName, String newFirstName, LocalDate newBirthdate) throws SQLException {
        // to change details of the user and their accountID's we first need the userid
        // with this userid we can pinpoint what user details to change and we can also grab the accounts associated with that userid
        int userid = getUserId(currentUser.getFirstName(), currentUser.getLastName(), currentUser.getBirthdate());
        ArrayList<String> accountIds = new ArrayList<>();
        // check if the userid is not -1, which means that a userid has been found
        if (!(userid == -1)) {
            // now that we have a userid we can grab an ArrayList of accountid's associated with this userid
            accountIds = getAccountIds(userid);

            // now that we have a userid and also the associated account id's we can start with updating the user details
            updateUserDetails(userid, newLastName, newFirstName, newBirthdate);

            // to update the account id's we need to go over every accountid in the ArrayList
            for (String accountid : accountIds) {
                // create the new accountID based on the previous one and the new initials
                String newAccountId = newFirstName.substring(0, 1) + newLastName.substring(0, 1) + accountid.substring(2);

                // then we also have to update the accountID for all the accounts the user has
                updateAccountId(accountid, newAccountId);
            }
        } else {
            //System.out.println("Userid when trying to change an account's details seems to be -1. \r\n");
            System.out.println("error changing user!");
        }
    }

    private void updateAccountId(String accountid, String newAccountId) {
        String query = "UPDATE accounts SET accountid = ? WHERE accountid = ?;";
        //System.out.println("updateAccountId query: " + query + "\r\n");

        try {
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            pStatement.setString(1, newAccountId);
            pStatement.setString(2, accountid);
            pStatement.executeUpdate();
            
            //System.out.println("accountid has been updated! \r\n");
        } catch (SQLException e) {
            System.out.println("Something has gone wrong trying to update the accountid when modifying a user");
            System.out.println(e.getMessage());
        }
    }

    private void updateUserDetails(int userid, String newLastName, String newFirstName, LocalDate newBirthdate) {
        String query = "UPDATE users SET firstname = ?, lastname = ?, birthdate = ? WHERE userid = ?;";

        //System.out.println("updateUserDetails query: " + query + "\r\n");

        try {
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            pStatement.setString(1, newFirstName);
            pStatement.setString(2, newLastName);
            pStatement.setString(3, String.valueOf(newBirthdate));
            pStatement.setInt(4, userid);
            pStatement.executeUpdate();
            
            //System.out.println("User details have been updated! \r\n");
        } catch (SQLException e) {
            System.out.println("Something has gone wrong trying to update the user details when modifying a user");
            System.out.println(e.getMessage());
        }
    }

    private ArrayList<String> getAccountIds(int userid) throws SQLException {
        ArrayList<String> accountids = new ArrayList<>();
        
        String query = "SELECT accountid FROM accounts WHERE userid = ?;";
        //System.out.println("getAccountIds query: " + query + "\r\n");

        try {
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            pStatement.setInt(1, userid);
            rs = pStatement.executeQuery();
            
            //System.out.println("Retrieved accountids for the associated account");
        } catch (SQLException e) {
            System.out.println("Something went wrong trying to get the accountids! \r\n");
        }

        if (!rs.isBeforeFirst()) {
            //System.out.println("the resultset with accountids seems to be empty...\r\n");
            return null;
        } else {
            while (rs.next()) {
                accountids.add(rs.getNString(1));
            }
            return accountids;
        }
    }

    private int getUserId(String firstName, String lastName, LocalDate birthdate) throws SQLException {
        int userid = -1;
        
        String query = "SELECT userid FROM users WHERE firstname = ? AND lastname = ? AND birthdate = ?;";
        //System.out.println("getUserId query: " + query + "\r\n");

        try {
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            pStatement.setString(1, firstName);
            pStatement.setString(2, lastName);
            pStatement.setString(3, String.valueOf(birthdate));
            rs = pStatement.executeQuery();
            
            //System.out.println("Successfully retrieved userid! \r\n");
        } catch (SQLException e) {
            System.out.println("Something went wrong trying to get the userid for changing user details \r\n");
            System.out.println(e.getMessage());
        }

        // check if the resultSet is empty or not
        if (!rs.isBeforeFirst()) {
            //System.out.println("there were no userid's found.... \r\n");
            return userid;
        } else {
            while (rs.next()) {
                userid = rs.getInt(1);
                //System.out.println("the found userid: " + userid + "\r\n");
            }

            return userid;
        }
    }

    public Boolean validatePin(Account selectedAccount, int answer) throws SQLException {
        //System.out.println("Validating Pin....... \r\n");

        String query = "SELECT pin FROM auth WHERE accountid = ?;";
        //System.out.println("the validatePin query: " + query + "\r\n");

        try {
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            pStatement.setString(1, selectedAccount.getAccountID());
            rs = pStatement.executeQuery();
            
        } catch (SQLException e) {
            System.out.println("Something went wrong trying to validate the pin! \r\n");
            System.out.println(e.getMessage());
        }

        int pin = 0;

        if (!rs.isBeforeFirst()) {
            System.out.println("Something has gone wrong, pin doesn't seem to exist... \r\n");
        } else {
            while (rs.next()) {
                //System.out.println("Pin found: " + rs.getInt(1) + "\r\n");
                pin = rs.getInt(1);
            }
        }

        if (answer == pin) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean changePin(Account selectedAccount, int newPin) {
        String query = "UPDATE auth SET pin = ? WHERE accountid = ?;";
        //System.out.println("changePin query: " + query + "\r\n");

        try {
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            pStatement.setInt(1, newPin);
            pStatement.setString(2, selectedAccount.getAccountID());
            pStatement.executeUpdate();
            
            //System.out.println("pin has been changed to " + newPin + "for account with accountid: " + selectedAccount.getAccountID() + "\r\n");
            return true;
        } catch (SQLException e) {
            System.out.println("Something has gone wrong trying to change the pin \r\n");
            System.out.println(e.getMessage());
            return false;
        }
    }

    public ResultSet getUsers() {
        String query = "SELECT lastname, firstname, birthdate FROM users;";
        
        //System.out.println("getUsers query: " + query + "\r\n");

        //System.out.println("Trying to get all users from the database....\r\n");
        try {
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            rs = pStatement.executeQuery();
            
            // check if the ResultSet contains any data
            if (rs.isBeforeFirst()) {
                return rs;
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Something went wrong trying to get all users from the database! \r\n");
            System.out.println(e.getMessage());
            return null;
        }
    }

    public ResultSet getAccounts() {
        String query = "SELECT lastname, firstname, birthdate, a.accountid, accounttype, accountnumber, balance, pin "
                + "FROM users u "
                + "JOIN accounts a on u.userid = a.userid "
                + "JOIN auth au on a.accountid = au.accountid;";
        
        //System.out.println("getAccounts query: " + query + "\r\n");

        //System.out.println("Trying to get all accounts from the database....\r\n");
        try {
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            rs = pStatement.executeQuery();
            
            // check if the ResultSet contains any data
            if (rs.isBeforeFirst()) {
                return rs;
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.out.println("Something went wrong trying to get all accounts from the database! \r\n");
            System.out.println(e.getMessage());
            return null;
        }
    }

    public double getAccountBalance(String accountID) throws SQLException {
        String query = "SELECT balance FROM accounts WHERE accountid = ?;";

        //System.out.println("getAccountBalance query: " + query + "\r\n");
        
        try {
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            pStatement.setString(1, accountID);
            rs = pStatement.executeQuery();
            
        } catch (SQLException e) {
            System.out.println("Something went wrong trying to fetch the balance from the database! \r\n");
            System.out.println(e.getMessage());
        }

        double balance = 0.0;
        while (rs.next()) {
            balance = rs.getDouble(1);
        }
        return balance;
    }

    public Boolean accountIdExists(String accountID) throws SQLException {
        String query = "SELECT accountid FROM accounts WHERE accountid = ?;";
        //System.out.println("accountIdExists query: " + query);

        try {
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            pStatement.setString(1, accountID);
            rs = pStatement.executeQuery();
            
        } catch (SQLException e) {
            System.out.println("Something went wrong trying to find the accountID for a payment in the database \r\n");
            System.out.println(e.getMessage());
        }

        if (!rs.isBeforeFirst()) {
            System.out.println("the requested accountID does not exist! \r\n");
            return false;
        } else {
            while (rs.next()) {
                //System.out.println("the accountID that was found: " + rs.getNString(1) + "\r\n");
            }
            return true;
        }
    }

    public void removeBalance(double amount, String accountID, double originalBalance) {
        double newBalance = new BigDecimal((originalBalance - amount)).setScale(2, RoundingMode.HALF_UP).doubleValue();

        String query = "UPDATE accounts SET balance = ? WHERE accountid = ?;";
        //System.out.println("remove balance query: " + query + "\r\n");

        try {
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            pStatement.setDouble(1, newBalance);
            pStatement.setString(2, accountID);
            pStatement.executeUpdate();
            
            //System.out.println("balance has been changed from " + originalBalance + " to " + newBalance + "\r\n");
        } catch (SQLException e) {
            System.out.println("Something has gone wrong trying to withdraw from the account balance \r\n");
            System.out.println(e.getMessage());
        }
    }

    public void addBalance(double amount, String accountID, double originalBalance) {
        double newBalance = new BigDecimal((originalBalance + amount)).setScale(2, RoundingMode.HALF_UP).doubleValue();

        String query = "UPDATE accounts SET balance = ? WHERE accountid = ?;";
        //System.out.println("add balance query: " + query + "\r\n");

        try {
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            pStatement.setDouble(1, newBalance);
            pStatement.setString(2, accountID);
            pStatement.executeUpdate();
            
            //System.out.println("Balance has been changed from " + originalBalance + " to " + newBalance + "\r\n");
        } catch (SQLException e) {
            System.out.println("Something has gone wrong trying to add to the account balance \r\n");
            System.out.println(e.getMessage());
        }
    }

    public void updateSavingsBalance(String accountId, double currentBalance, double intrest) {
        double calculation = currentBalance + (currentBalance * intrest);
        double newBalance = new BigDecimal(calculation).setScale(2, RoundingMode.HALF_UP).doubleValue();

        String query = "UPDATE accounts SET balance = ? WHERE accountid = ?;";
        //System.out.println("updateSavingsBalance query: " + query + "\r\n");

        try {
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            pStatement.setDouble(1, newBalance);
            pStatement.setString(2, accountId);
            pStatement.executeUpdate();
            
            //System.out.println("Balance updated from: " + currentBalance + " to: " + newBalance + "\r\n");
        } catch (SQLException e) {
            System.out.println("Something went wrong trying to update the balance! \r\n");
            System.out.println(e.getMessage());
        }
    }

    public ResultSet getSavingsAccounts() throws SQLException {
        String query = "SELECT accountid, balance FROM accounts "
                + "WHERE accounttype = 'savings' ;";
        
        //System.out.println("getSavingsAccounts query: " + query);

        try {
            //System.out.println("fetching accountid and balance for savings accounts...\r\n");
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            rs = pStatement.executeQuery();
            
           // System.out.println("successfully executed query for fetching savings accounts");
        } catch (SQLException e) {
            System.out.println("Something went wrong trying to get the savings accounts from the database \r\n");
            System.out.println(e.getMessage());
        }

        if (!rs.isBeforeFirst()) {
            return null;
        } else {
            //System.out.println("Savings accounts were found! \r\n");
            return rs;
        }
    }

    public void addAccount(String firstName, String lastName, LocalDate birthdate, Account newAccount) throws SQLException {
        // to properly add an account to the database we first need to grab the userid for the user for who the account is added
        int userid = getUserId(firstName, lastName, birthdate);

        // then add the account to the accounts table with all the needed details
        insertAccount(userid, newAccount);

        // to finish, insert a new row into the auth table with needed details
        insertAuth(newAccount);

        //System.out.println("newly created account successfully added to the database! \r\n");
    }

    private void insertAccount(int userid, Account newAccount) {
        String query = "INSERT INTO accounts (accountid, userid, accounttype, accountnumber, balance) VALUES (?, ?, ?, ?, ?);";
        //System.out.println("insertAccount query: " + query + "\r\n");

        try {
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            pStatement.setString(1, newAccount.getAccountID());
            pStatement.setInt(2, userid);
            pStatement.setString(3, newAccount.getType());
            pStatement.setString(4, newAccount.getAccountNumber());
            pStatement.setDouble(5, newAccount.getBalance());
            pStatement.executeUpdate();
            
            //System.out.println("New account was added to the accounts table \r\n");
        } catch (SQLException e) {
            System.out.println("Something went wrong trying to add a new account to the accounts table! \r\n");
            System.out.println(e.getMessage());
        }
    }

    public Boolean removeAccount(Account selectedAccount) {
        String query = "DELETE FROM accounts WHERE accountid = ?;";
        //System.out.println("removeAccount query: " + query + "\r\n");

        try {
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            pStatement.setString(1, selectedAccount.getAccountID());
            pStatement.executeUpdate();
            
            //System.out.println("account successfully removed! \r\n");
            return true;
        } catch (SQLException e) {
            System.out.println("Something went wrong trying to remove the account from the database! \r\n");
            System.out.println(e.getMessage());
            return false;
        }
    }

    public Boolean removeUser(User selectedUser) throws SQLException {
        int userid = getUserId(selectedUser.getFirstName(), selectedUser.getLastName(), selectedUser.getBirthdate());

        String query = "DELETE FROM users WHERE userid = ?;";
        //System.out.println("removeUser query: " + query + "\r\n");

        try {
            connection = DbSingleton.getDbSingleton().getConnection();
            
            pStatement = connection.prepareStatement(query);
            pStatement.setInt(1, userid);
            pStatement.executeUpdate();
            
            //System.out.println("User successfully removed! \r\n");
            return true;
        } catch (SQLException e) {
            System.out.println("Something went wrong trying to remove the user from the database! \r\n");
            System.out.println(e.getMessage());
            return false;
        }

    }

}
