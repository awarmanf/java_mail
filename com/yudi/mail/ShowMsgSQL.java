package com.yudi.mail;

import com.yudi.util.DBUtil;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 *
 * ShowMsgSQL
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

public class ShowMsgSQL {

    public static void main(String[] args) {
        
        final String TABLE = "email";

        Connection conn;
        GZIPInputStream gZIPInputStream;
        StringBuilder msg;
        String sql;
        int bytes_read;

        if (args.length != 2) {
            System.out.println("Usage: java ShowMessage folderimap.db id");
            System.exit(0);
        }

        Statement stmt = null;
        String db = args[0];
        int id = Integer.parseInt(args[1]);
        conn = DBUtil.dbConnector(db);

        if (conn != null) {

            sql = "SELECT date, sender, subject, body FROM " + TABLE  + " WHERE id=" + id;

            try {
                stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                // loop through the result set
                while (rs.next()) {
                    System.out.println("\nDate: " + rs.getString("date") + "\n"
                            + "From: " + rs.getString("sender") + "\n"
                            + "Subject: " + rs.getString("subject"));
                    System.out.println();
                    InputStream input = rs.getBinaryStream("body");
                    byte[] buffer = new byte[1024];

                    try {
                        gZIPInputStream = new GZIPInputStream(input);

                        while ((bytes_read = gZIPInputStream.read(buffer)) > 0) {
                            System.out.write(buffer, 0, bytes_read);
                        }
                        gZIPInputStream.close();
                    } catch (IOException ex) {
                        Logger.getLogger(ShowMsgSQL.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            } finally {
                try {
                    stmt.close();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
    }
}
