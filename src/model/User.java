/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import db.Dao;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 *
 * @author Kenny
 */
public class User {

    private String lastName;
    private String firstName;
    private LocalDate birthdate;
    private ArrayList<Account> accounts;
    
    Dao dao = new Dao();

    // normal constructor
    public User(String lastName, String firstName, LocalDate birthdate, int pin, String type) {
        // when creating new user the details are filled in
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthdate = birthdate;
        // the ArrayList that holds the accounts gets initialised
        this.accounts = new ArrayList<>();

        // as soon as a user is created, we also create the chosen account for the user
        // for that, we also need the pin and type for the account
        generateAccount(pin, type);
    }
    
    // overloaded constructor for user creation from database information
    public User(String lastName, String firstName, LocalDate birthdate) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthdate = birthdate;
        this.accounts = new ArrayList<>();
    }
    
    public void addAccount(String accountID, String type, String accountNumber, double balance, int pin) {
        this.accounts.add(new Account(accountID, type, accountNumber, balance, pin));
    }
    
    public void addAccount(String chosenType, int chosenPin) throws SQLException {
        // create the new account
        Account newAccount = new Account(chosenPin, chosenType, this.firstName, this.lastName);
        
        // add the account to the list of the user accounts
        addNewAccountToList(newAccount);
        
        // add the account to the database
        dao.addAccount(this.firstName, this.lastName, this.birthdate, newAccount);
        
    }
    
    private void generateAccount(int pin, String type) {
        System.out.println("Creating chosen account... \r\n");

        // initialize the number of accounts the user has
        int amountOfNormalAccounts = 0;
        int amountOfSavingsAccounts = 0;

        for (Account account : this.accounts) {
            if (account.type.equals("normal")) {
                amountOfNormalAccounts++;
            } else if (account.type.equals("savings")) {
                amountOfSavingsAccounts++;
            }
        }

        // check the type that was given by the user
        if (type.equals("normal")) {
            // if the type chosen is normal then we check if a normal account allready exists or not.
            // we can increase this number to allow more or less of the same account
            if (amountOfNormalAccounts < 1) {
                NormalAccount newNormalAccount = new NormalAccount(pin, type, this.firstName, this.lastName);
                System.out.println("Account created! \r\n");
                System.out.println("new account: \r\n " + newNormalAccount);
                System.out.println("new account type: " + newNormalAccount.type);
                
                // next we add the account to the list
                addNewAccountToList(newNormalAccount);
            } else {
                // if it does we inform the user
                System.out.println("this user allready has a normal account.");
            }
        } else if (type.equals("savings")) {
            // if the user chose savings we also check if there is no savings account in existence yet
            if (amountOfSavingsAccounts < 1) {
                SavingsAccount newSavingsAccount = new SavingsAccount(pin, type, this.firstName, this.lastName);
                System.out.println("Account created!");
                System.out.println("new account: " + newSavingsAccount);
                System.out.println("new account type: " + newSavingsAccount.type);
                
                // next we add the account to the list
                addNewAccountToList(newSavingsAccount);
            } else {
                // if it does we inform the user
                System.out.println("this user allready has a savings account");
            }
        }
    }
    
    private void addNewAccountToList(Account account) {
        System.out.println("Adding new account to the list...");
        this.accounts.add(account);
        System.out.println("Account added to the list");
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public ArrayList<Account> getAccounts() {
        return accounts;
    }

    @Override
    public String toString() {
        return this.firstName + " " + this.lastName;
    }

}
