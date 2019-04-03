/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import db.Dao;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Kenny
 */
public class SavingsAccount extends Account {

    public SavingsAccount(int pin, String type, String firstName, String lastName) {
        super(pin, type, firstName, lastName);
    }
    
    // static method that handles generating and adding the intrest to savingsAccounts
    static void generateIntrest() throws SQLException {
        Dao dao = new Dao();
        //System.out.println("Generating and adding intrest to savings accounts....\r\n");
        // get a resultset of all the savings accounts
            ResultSet savingsAccounts = dao.getSavingsAccounts();
            double intrest = 0.01;

            // if the resultset is empty we report it and move on
            if(savingsAccounts == null) {
                //System.out.println("There were no savings account found in the database!\r\n");
            } else {
                // for every savings account in the resultset we update the balance accordingly
                //System.out.println("Updating the balance for all savings accounts with intrest... \r\n");
                while(savingsAccounts.next()) {
                    dao.updateSavingsBalance(savingsAccounts.getNString(1), savingsAccounts.getDouble(2), intrest);
                }
            }
    }
}
