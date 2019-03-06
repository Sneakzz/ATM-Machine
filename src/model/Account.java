/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import db.Dao;
import java.sql.SQLException;
import java.util.Random;
import javax.swing.JOptionPane;

/**
 *
 * @author Kenny
 */
public class Account {
    protected String accountNumber;
    protected int pin;
    protected String accountID;
    protected String type;
    protected double balance;
    
    Dao dao = new Dao();

    // normal constructor
    public Account(int pin, String type, String firstName, String lastName) {
        // every Account, when created, needs the given pin by the user as well as what type the account will be
        this.pin = pin;
        this.type = type;
        // the account number gets generated
        this.accountNumber = generateAccountNumber();
        // the accountID also gets generated
        this.accountID = generateAccountID(firstName, lastName);
        this.balance = 4.99;
    }
    
    // overloaded constructor for account creation from database information
    public Account(String accountID, String type, String accountNumber, double balance, int pin) {
        this.accountID = accountID;
        this.type = type;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.pin = pin;
    }
    
    public void checkBalance() throws SQLException {
        // when this method is called, we know an account and user are inserted
        System.out.println("Looking up current balance for accountID: " + this.accountID + "\r\n");
        
        // so at this point we can lookup the balance for the account
        double currentBalance = dao.getAccountBalance(this.accountID);
        
        // for now output it to a JOptionPane to inform the user of their balance
        JOptionPane.showMessageDialog(null, "Your current balance is now: \r\n" + currentBalance);
    }
    
    public void pay(Account destAccount, Account insertedAccount, double amount) throws SQLException {
        System.out.println("creating transactions, please wait.... \r\n");
        
        // in order to be sure of a correct payment we first get the balance of both accounts involved from the database
        double destBalance = dao.getAccountBalance(destAccount.getAccountID());
        double originalBalance = dao.getAccountBalance(insertedAccount.getAccountID());
        
        
        // remove the amount from the payer balance
        withdraw(amount, originalBalance);
        // add the amount to the receiver balance
        depositTo(amount, destAccount.getAccountID(), destBalance);
        
        System.out.println("Transactions complete! \r\n");
    }
    
    public void withdraw(double amount, double originalBalance) {
        System.out.println("Trying to withdraw from the account...\r\n");
        dao.removeBalance(amount, accountID, originalBalance);
        System.out.println("Withdraw complete \r\n");
    }

    public void deposit(double amount, double originalBalance) {
        System.out.println("Trying to deposit to the account...\r\n");
        dao.addBalance(amount, accountID, originalBalance);
        System.out.println("Deposit complete! \r\n");
    }
    
    private void depositTo(double amount, String accountId, double originalBalance) {
        System.out.println("Trying to deposit to the destination account....\r\n");
        dao.addBalance(amount, accountId, originalBalance);
        System.out.println("Deposit complete! \r\n");
    }
    
    private String generateAccountNumber() {
        /**
         * TODO!!
         * 
         * we honestly still need a resultset of all current accountNumbers so when we generate 
         * a new accountNumber we can check it against existing ones and make sure we dont create 
         * any double accountNumbers.
         */
        
        
        System.out.println("1) Creating the account number");
        
        Random r = new Random();
        int amountOfNumbers = 10;
        int newNumber = 0;
        String newAccountNumber = "AG";
        
        for (int i = 0; i < amountOfNumbers; i++) {
            newNumber = r.nextInt(10);
            newAccountNumber += newNumber;
        }
        System.out.println("created account number: " + newAccountNumber);
        System.out.println("Account number creation done! \r\n");
        return newAccountNumber;
    }
    
    private String generateAccountID(String firstName, String lastName) {
        /**
         * TODO!!
         * 
         * here we need te same as for the accountNumbers but a ResultSet of accountID's instead 
         * where we can loop through and check that the newly generated accountID is unique
         */
        
        System.out.println("Creating the account ID \r\n");
        
        String firstInitial = firstName.substring(0, 1);
        String lastInitial = lastName.substring(0, 1);
        String digits = this.accountNumber.substring(this.accountNumber.length() - 4);
        
        String newAccountID = firstInitial + lastInitial + digits;
        
        System.out.println("the new account ID: " + newAccountID);
        System.out.println("Account ID created \r\n");
        
        return newAccountID;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public int getPin() {
        return pin;
    }

    public String getAccountID() {
        return accountID;
    }

    public String getType() {
        return type;
    }

    public double getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return this.accountID + ", " + this.type;
    }
    
}
