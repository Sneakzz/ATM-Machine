# ATM Machine Project

The idea of this project is to simulate the workings and functionality of how an ATM machine would work.
Users and accounts can be created, balance checking, depositing and withdrawing, payments and more!

This project is created using JavaFX and also depends on a MySQL database to hold information and uses the JDBC API to access
and manipulate this information.

More information can be found inside Atm_Machine_Project.pdf

# Requirements

* NetBeans 8.2 Java SE [Link](https://netbeans.org/downloads/8.2/)
* MySQL 8.0 [Link](https://dev.mysql.com/downloads/windows/installer/8.0.html)
* MySQL Connector/J 8.0 (This should come with the MySQL 8.0 installer, but in case you do not have it or lose it, here is the download link) [Link](https://dev.mysql.com/downloads/connector/j/8.0.html)

# Installation Guide

1) Import the source files into your IDE.

2) Import the build_script.sql file into your RDBMS and execute it to create the necessary schema and tables.

3) Add the JDBC connection for the DB in the services tab in NetBeans IDE. (Use the MySQL Connector/J 8.0 that comes with the MySQL 8.0 installation)

4) In the source files, update the database connectivity code in the DbProperties.java file. (found in the package "db")

5) Build and run!