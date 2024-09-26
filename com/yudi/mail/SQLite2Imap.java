package com.yudi.mail;

import com.yudi.util.*;
import java.util.Date;
import java.util.Locale;
import java.text.ParseException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;

/**
 *
 * SQLite2Imap
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

public class SQLite2Imap {
   
    final static String TZ = (new SimpleDateFormat("Z", Locale.getDefault())).format(new Date());
    final static Flags.Flag FLAG = Flags.Flag.SEEN;
    final static boolean DEBUG = false;
    
    private String host;
    private String email;
    private String password;
    private String file;
    private String folderImap;
    private String subject;
    private String from;
    private boolean isTest;
    private boolean isTls;
    private Date dateStart;
    private Date dateEnd;

    public SQLite2Imap(String email, String password, String file, String host,
            String folderImap, Date dateStart, Date dateEnd, String subject, String from, 
            boolean isTls, boolean isTest) {

        Message[] msgs;
        Message msg;
        Store store;
        String sql;
        String protocol;
        Properties props;
        Session session;
        InputStream input;
        byte[] buffer;
        GZIPInputStream gZIPInputStream;
        int bytes_read;
        ByteArrayOutputStream bOutStream;
        ByteArrayInputStream bInStream;
        Connection conn;
        
        this.host = host;
        this.email = email;
        this.password = password;
        this.file = file;
        this.folderImap = folderImap;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.subject = subject;
        this.from = from;
        this.isTls = isTls;
        this.isTest = isTest;
        
        Folder folder = null;
        Statement stmt = null;

        String mailhost = host;
        conn = DBUtil.dbConnector(file);
        int i = 1;

        if (conn != null) {

            sql = "SELECT date,sender,subject,body FROM email "
                    + "WHERE date>'" + dateFormat.FORMAT_SQL2.format(dateStart) + "'"
                    + " AND date<'" + dateFormat.FORMAT_SQL2.format(dateEnd) + "'"
                    + " AND subject like '%" + subject + "%'"
                    + " AND sender like '%" + from + "%'";

            // Initialize the JavaMail Session.
            props = System.getProperties();
            
            protocol = "imap";
            // Get a Store object
            if (isTls) {
                props.put("mail.imap.starttls.enable", true);
                protocol = "imaps";
            }
            
            // Set properties
            props.put("mail.imap.timeout", 300);
            props.put("mail.imap.connectiontimeout", 300);

            // Get a Session object
            session = Session.getInstance(props, null);
            if (DEBUG) {
                session.setDebug(true);
            }

            // Get a Store 
            try {
                store = session.getStore(protocol);
                store.connect(host, email, password);
                folder = store.getFolder(folderImap);

                if (!folder.exists()) {
                    folder.create(Folder.HOLDS_MESSAGES);
                }
            } catch (AuthenticationFailedException ex) {
                Logger.getLogger(SQLite2Imap.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            } catch (MessagingException ex) {
                Logger.getLogger(SQLite2Imap.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            }

            try {
                stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                // loop through the result set
                while (rs.next()) {

                    println(i++ + ". " + rs.getString("date") + "; "
                            + rs.getString("sender") + "; " + rs.getString("subject"));
                   
                    if (!isTest) {
                        
                        // initialize
                        buffer = new byte[1024];
                        input = rs.getBinaryStream("body");
                        bOutStream = new ByteArrayOutputStream();

                        try {
                            gZIPInputStream = new GZIPInputStream(input);
                            while ((bytes_read = gZIPInputStream.read(buffer)) > 0) {
                                bOutStream.write(buffer, 0, bytes_read);
                            }
                            gZIPInputStream.close();
                        } catch (IOException ex) {
                            System.err.println(ex);
                            System.exit(1);
                        }
                        bInStream = new ByteArrayInputStream(bOutStream.toByteArray());

                        try {
                            // Construct the message
                            msg = new MimeMessage(session, bInStream);
                            msgs = new Message[1];
                            // set the messages to read / seen
                            msg.setFlag(FLAG, true);
                            msgs[0] = msg;
                            folder.appendMessages(msgs);

                        } catch (MessagingException ex) {
                            Logger.getLogger(SQLite2Imap.class.getName()).log(Level.SEVERE, null, ex);
                            System.exit(1);
                        }
                    }
                }

            } catch (SQLException e) {
                println(e.getMessage());
            } finally {
                try {
                    stmt.close();
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(SQLite2Imap.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(1);
                }
            }
        }
        
        if (isTest) {
            println("\nMatching " + --i + " email(s) at file " + file + " (not saving).\n\n");
        } else {
            println("\nSaving " + --i + " email(s) from file " + file + " to folder " + 
                    folder + " at server " + host + ".\n\n");
        }
    }

    public static void checkInput(String s) {
        if (s.startsWith("-")) {
            help();
        }
    }
    
    public static void println(String s) {
        System.out.println(s);
    }

    public static void print(String s) {
        System.out.print(s);
    }
    
    public static void help() {
        println(
            "\nUsage: java SQLite2Imap -m email -p [password] -f 'file db' -h host [ -r 'folder imap'] "
            + "[-start date] [-end date] [-S 'Subject'] [-F 'sender'] [-tls] [-test]\n"
            + "   r : folder imap to restore (default is Inbox)\n"
            + "start: start date (default is '1 Jan 1970')\n"
            + "  end: end date (default is now)\n"
            + "  tls: using tls (default is no tls)\n"
            + " test: no restore message(s) to imap folder\n");
        System.exit(0);
    }
    
    public static void main(String args[]) throws ParseException {

        int optind;
        String email = null;
        String password = null;
        String host = null;
        String file = null;
        String folderImap = "INBOX";
        String subject = "";
        String from = "";
        String startDtStr, endDtStr;
        Console console = System.console();
        Date dateStart = dateFormat.FORMAT_3.parse(dateFormat.UNIX_EPOCH_DATE);
        Date dateEnd = new Date();
        Date dateNow = new Date();
        boolean isTest = false;
        boolean isTls = false;

        if (args.length == 0) {
            help();
        }

        for (optind = 0; optind < args.length; optind++) {
            if (args[optind].equals("-m")) {
                email = args[optind+1];
                checkInput(email);
            } else if (args[optind].equals("-p")) {
                try {
                    // user typing password after `-p`
                    password = args[optind+1];                
                } catch (Exception e) {
                    // option `-p` at the end of command
                    password = "-blah"; 
                }
            } else if (args[optind].equals("-h")) {
                host = args[optind+1];
                checkInput(host);
            } else if (args[optind].equals("-f")) {
                file = args[optind+1];
                checkInput(file);
            } else if (args[optind].equals("-r")) {
                folderImap = args[optind+1];
                checkInput(folderImap);
            } else if (args[optind].equals("-S")) {
                subject = args[optind+1];
                checkInput(subject);
            } else if (args[optind].equals("-F")) {
                from = args[optind+1];
                checkInput(from);

            } else if (args[optind].equals("-start")) {
                startDtStr = args[optind+1];
                dateStart = checkInputDate.check(startDtStr.concat(" 00:00:00 " + TZ)); // append time and timezone
                if (dateStart == null) {
                    System.out.println("Input date not valid");
                    System.exit(1);
                } else {
                    if (dateStart.compareTo(dateNow) > 0) {
                        System.out.println("Start date should be equal or older than now.");
                        System.exit(0);
                    }
                }
            } else if (args[optind].equals("-end")) {
                endDtStr = args[optind+1];
                dateEnd = checkInputDate.check(endDtStr.concat(" 23:59:29 " + TZ)); // append time and timezone
                if (dateEnd == null) {
                    System.out.println("Input date not valid");
                    System.exit(1);
                } else {
                    if (dateStart.compareTo(dateEnd) > 0) {
                        System.out.println("End date should be equal or younger than start date.");
                        System.exit(0);
                    }
                }
            } else if (args[optind].equals("-tls")) {
                isTls = true;
            } else if (args[optind].equals("-test")) {
                isTest = true;
            } else if (args[optind].startsWith("-")) {
                help();
            }
        }

        if (password != null && password.startsWith("-")) {
           print("Enter password: ");
           password = String.valueOf(console.readPassword());
        };

        // check start and end date
        if (dateStart.compareTo(dateEnd) > 0) {
            println("Start date should be equals or older than end date.");
            System.exit(1);
        }

        // all required arguments must be exist
        if (email == null || password == null || host == null || file == null) {
            help();
        }

        // Check if file exist
        if (!new java.io.File(file).exists()) {
            println("File " + file + " not exist!");
            System.exit(1);
        }
        SQLite2Imap sqlite2Imap = new SQLite2Imap(email, password, file, host, folderImap, dateStart, dateEnd, subject, from, isTls, isTest);
    }
}

