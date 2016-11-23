package com.oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * C
 */
public class JDBCConnection {

    private static String url="jdbc:oracle:thin:@localhost:1521:xe";
    private static String username = "admin";
    private static String password = "123456";
    private static String dbDriver = "oracle.jdbc.driver.OracleDriver";

    static Connection createDBConnection() throws SQLException {
        try {
            Class.forName(dbDriver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(url, username, password);
    }
}