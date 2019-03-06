/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

import java.sql.*;

/**
 *
 * @author Kenny
 */
public class DbSingleton {
    private static DbSingleton ref;
    private Connection connection;
    
    public DbSingleton() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static DbSingleton getDbSingleton() {
        if(ref == null) {
            ref = new DbSingleton();
        }
        
        return ref;
    }
    
    public Connection getConnection() {
        try {
            connection = DriverManager.getConnection(DbProperties.URL, DbProperties.USERNAME, DbProperties.PASSWORD);
            return connection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        return null;
    }
    
}
