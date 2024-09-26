/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yudi.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * DBUtil
 * 
 * License
 *
 * This program is copyleft. You have the right to freely use, modify,
 * copy, and share software, works of art, etc., on the condition that
 * these rights be granted to all subsequent users or owners. 
 * 
 * Last edited Mon, 23 Oct 2023 14:30 +0700
 *
 * @author Arief Yudhawarman <awarmanf@yahoo.com>
 *
 */

public class DBUtil {
    
    public static Connection dbConnector(String db) {

        Connection conn = null;
        final String table = "email";
        String sql;
        Statement stmt;

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + db);

            sql = "CREATE TABLE IF NOT EXISTS " + table + " ("
                    + " id integer PRIMARY KEY, "
                    + " date DATETIME, "
                    + " sender VARCHAR(80),"
                    + " subject VARCHAR(160),"
                    + " size INTEGER,"
                    + " header BLOB,"
                    + " body BLOB);";

            try {
                stmt = conn.createStatement();
                // create a new table
                stmt.execute(sql);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return null;
            }
            return conn;
        } catch (ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
            return null;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
}
