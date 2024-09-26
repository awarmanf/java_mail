package com.yudi.mail;

import java.io.FileNotFoundException;
import java.util.*;
import javax.mail.*;
import com.yudi.util.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Console;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import java.text.SimpleDateFormat;

/**
 *
 * Imap2SQLite
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


public class Imap2SQLite {

    final static String TABLE = "email";
    final static List<String> LIST_EXCLUDES = Arrays.asList(constants.EXCLUDES);
    final static String FILE_SEPARATOR = System.getProperty("file.separator");
    final static String TZ = (new SimpleDateFormat("Z", Locale.getDefault())).format(new Date());

    private String host;
    private String email;
    private String password;
    private String dest;
    private String source;
    private boolean isTest;
    private boolean isFlush;
    private boolean isTls;
    private Date dateStart;
    private Date dateEnd;
    
    static Map<String, String> hmDirLog = new HashMap<String, String>();
    static Map<String, Integer> hmLogCounterFolder = new HashMap<String, Integer>();
    static Map<String, java.io.PrintWriter> hmPrintLog = new HashMap<String, java.io.PrintWriter>();

    public Imap2SQLite(String host, String email, String password, String dest, String source, 
            Date dateStart, Date dateEnd, boolean isTls, boolean isTest, boolean isFlush) {

        String protocol;
        String pattern = "%";
        Folder folder = null;
        Store store = null;

        this.host = host;
        this.email = email;
        this.password = password;
        this.dest = dest;
        this.source = source;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.isTest = isTest;
        this.isFlush = isFlush;
        this.isTls = isTls;
       
        try {
            // Get a Properties object
            Properties props = System.getProperties();

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
            Session session = Session.getInstance(props, null);
            store = session.getStore(protocol);
            store.connect(host, email, password);
            
            // List namespace
            if (source != null) {
                folder = store.getFolder(source);
            } else {
                folder = store.getDefaultFolder();
            }

            if ((folder.getType() & Folder.HOLDS_FOLDERS) != 0) {

                // print header only once (test mode)
                if (isTest) {
                    // Show date now using format SDFMT "E, dd MMM HH:mm:ss z yyyy"
                    println(dateFormat.FORMAT_JMAIL2.format(new Date()));
                    println("\nMatching email(s) " + email + " at " + host + " from "
                            + dateFormat.FORMAT_1.format(dateStart)
                            + " until " + dateFormat.FORMAT_1.format(dateEnd));
                }

                if (source != null) {
                    dumpFolder(folder, false);
                } else {
                    // check all folder (recursive)
                    Folder[] fd = folder.list(pattern);
                    for (int i = 0; i < fd.length; i++) {
                        dumpFolder(fd[i], true);
                    }
                }
                println("");
            }
            // using for-each loop for iteration over Map.entrySet() to close file log
            for (Map.Entry<String, java.io.PrintWriter> entry : hmPrintLog.entrySet()) {
                entry.getValue().close();
            }
            store.close();

        } catch (NoSuchProviderException ex) {
            println("NoSuchProviderException! " + ex.getMessage());
            System.exit(1);
        } catch (FileNotFoundException ex) {
            println("FileNotFoundException! " + ex.getMessage());
            System.exit(1);
        } catch (FolderNotFoundException ex) {
            println("FolderNotFoundException! " + ex.getMessage());
            System.exit(1);
        } catch (Exception ex) {
            println("Exception! " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private void dumpFolder(Folder folder, boolean recurse)
            throws Exception {

        String date, datesql, from, subject;
        String[] parts;
        int size;
        String subdir, file, fileLog;
        StringBuilder sb;
        File ff;
        Connection conn = null;

        if (!new File(dest).exists()) {
            boolean mkdirs = new File(dest).mkdirs();
            if (!mkdirs) {
                println("Failed to create: " + dest);
                System.exit(1);
            }
        }

        // check folder if not in excluded list then process to open folder
        if (isFolderNotExcludes(folder.getFullName())) {

            if (folder.getMessageCount() > 0) {

                try {
                    folder.open(Folder.READ_WRITE);
                } catch (MessagingException ex) {
                    folder.open(Folder.READ_ONLY);
                }
                Message[] msgs = folder.getMessages();

                /* Folder Full Name    File
                     * INBOX               INBOX.db
                     * INBOX/BAK2          INBOX.BAK2.db
                     * INBOX/BAK/BAKK1     INBOX.BAK.BAKK1.db
                 */
                file = folder.getFullName().replaceAll(System.getProperty("file.separator"), ".");

                int counter;
                String header;
                String userLogFolder;
                int match = 0;
                for (Message msg : msgs) {
                    from = getFrom(msg);
                    date = getReceivedDate(msg);
                    subject = getSubject(msg);
                    size = getSize(msg);

                    if ((dateFormat.FORMAT_JMAIL.parse(date).compareTo(dateStart) >= 0)
                            && (dateFormat.FORMAT_JMAIL.parse(date).compareTo(dateEnd) <= 0)) {

                        datesql = convertDate(date);

                        if (isTest) {
                            if (match == 0) {
                                println("\nFolder: " + folder.getFullName());
                            }
                        }

                        parts = datesql.split("-"); // parts[0] -> Year, parts[1] -> Month
                        // subdir => /tmp/DB/yyyy/mm/user
                        subdir = dest + FILE_SEPARATOR + parts[0]
                                + FILE_SEPARATOR + parts[1]
                                + FILE_SEPARATOR + email;
                        // fileLog => /tmp/DB/yyyy/mm/user/user.log
                        fileLog = subdir.concat(FILE_SEPARATOR + email + ".log");
                        userLogFolder = subdir.concat("/" + folder.getFullName());

                        if (!isTest) {
                            // first found "yyyy/mm" then create
                            // print header
                            // set counter=1
                            // create subdir if not exists
                            // create user.log if not exists
                            if (hmDirLog.get(subdir) == null) {
                                hmDirLog.put(subdir, fileLog);

                                if (!new File(subdir).exists()) {
                                    new File(subdir).mkdirs();
                                }
                                // if user.log not exist then create a new empty file
                                ff = new File(fileLog);
                                if (!ff.exists()) {
                                    ff.createNewFile();
                                }
                                // create a new instance of FileWriter with append set true
                                // create a new instance of PrintWriter with auto flush set true
                                hmPrintLog.put(subdir,
                                        new java.io.PrintWriter(new java.io.FileWriter(fileLog, true), true));

                                header = "\n" + dateFormat.FORMAT_JMAIL2.format(new Date());
                                hmPrintLog.get(subdir).println(header);
                                header = "Fetching email(s) " + email + " at server " + host + " from "
                                        + dateFormat.FORMAT_1.format(dateStart)
                                        + " until " + dateFormat.FORMAT_1.format(dateEnd) + ".\n";
                                hmPrintLog.get(subdir).println(header);

                                // set counter = 1 and print folder name
                                counter = 1;
                                if (hmLogCounterFolder.get(userLogFolder) == null) {
                                    header = "Folder: " + folder.getFullName();
                                    hmPrintLog.get(subdir).println(header);
                                    hmLogCounterFolder.put(userLogFolder, counter);
                                }
                            } // proses parsing message di folder baru
                            else if (hmLogCounterFolder.get(userLogFolder) == null) {
                                counter = 1;
                                hmLogCounterFolder.put(userLogFolder, counter);
                                header = "\nFolder: " + folder.getFullName();
                                hmPrintLog.get(subdir).println(header);

                            }
                            // print caught message
                            userLogFolder = subdir.concat("/" + folder.getFullName());
                            counter = hmLogCounterFolder.get(userLogFolder);
                            header = counter
                                    + ". " + "From: " + from
                                    + "; Subject: " + subject
                                    + "; Date: " + datesql
                                    + "; " + size + " bytes.";
                            hmPrintLog.get(subdir).println(header);
                            hmLogCounterFolder.put(userLogFolder, ++counter);

                            sb = dumpHeader(msg);
                            ByteArrayOutputStream bout = new ByteArrayOutputStream();
                            msg.writeTo(bout);

                            // file database to open /tmp/DB/yyyy/mm/user/folder.db
                            String fullpath = subdir + FILE_SEPARATOR + file.concat(".db");

                            conn = DBUtil.dbConnector(fullpath);
                            insertData(conn, TABLE, date, from, subject, size, sb.toString(), bout);
                            conn.close();

                            // reset the stringBuilder object
                            sb.setLength(0); // set length of buffer to 0
                            sb.trimToSize(); // trim the underlying buffer

                            if (isFlush) {
                                msg.setFlag(Flags.Flag.DELETED, isFlush);
                            }
                        } else { // Test mode

                            println(++match + ". " + "From: " + from
                                    + "; Subject: " + subject
                                    + "; Date: " + datesql
                                    + "; " + size + " bytes.");
                        }
                    } // match date
                }
                folder.close(isFlush); // expunge message if flush is true
            } // hanya proses folder yang berisi message > 0
            if (recurse) {
                Folder[] f = folder.list();
                for (int i = 0; i < f.length; i++) {
                    dumpFolder(f[i], recurse);
                }
            }
        }
    }

    private boolean isFolderNotExcludes(String folder) {
        int m = 0;
        for (String exclude : LIST_EXCLUDES) {
            if (folder.toLowerCase().matches(exclude.toLowerCase() + ".*")) {
                m++;
            }
        }
        return m <= 0;
    }

    private void insertData(Connection conn, String table, String date, String sender,
            String subject, int size, String header, ByteArrayOutputStream body) {

        String sql;
        GZIPOutputStream gzipOuputStream;
        PreparedStatement pstmt = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = body.toByteArray();

        if (conn != null) {
            sql = "INSERT INTO " + table + " (date,sender,subject,size,header,body) VALUES(?,?,?,?,?,?)";

            try {
                // compress body
                gzipOuputStream = new GZIPOutputStream(bos);
                gzipOuputStream.write(data);
                gzipOuputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(Imap2SQLite.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, convertDate(date));
                pstmt.setString(2, sender);
                pstmt.setString(3, subject);
                pstmt.setInt(4, size);
                pstmt.setString(5, header);
                pstmt.setBytes(6, bos.toByteArray());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            } finally {
                try {
                    pstmt.close();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
    }

    private StringBuilder dumpHeader(Message m) throws Exception {

        StringBuilder sb = new StringBuilder();

        // Dump all headers
        Enumeration headers = m.getAllHeaders();
        while (headers.hasMoreElements()) {
            Header header = (Header) headers.nextElement();
            sb.append(header.getName()).append(": ");
            sb.append(header.getValue()).append("\n");
        }
        return sb;
    }

    private String getFrom(Message m) throws Exception {

        Address[] a;

        if ((a = m.getFrom()) != null) {
            return a[0].toString();
        } else {
            return constants.FROM;
        }
    }

    private String getSentDate(Message m) throws Exception {
        Date d = m.getSentDate();
        return (d != null ? d.toString() : constants.UNIX_EPOCH_DATE3);
    }

    private String getReceivedDate(Message m) throws Exception {
        Date d = m.getReceivedDate();
        return (d != null ? d.toString() : constants.UNIX_EPOCH_DATE3);
    }

    // Convert date from email's date to sqlite's datetime
    private String convertDate(String s) {
        try {
            Date dt = dateFormat.FORMAT_JMAIL.parse(s);
            return dateFormat.FORMAT_SQL3.format(dt);
        } catch (Exception e) {
            return constants.UNIX_EPOCH_DATE1;
        }
    }

    private String getSubject(Message m) throws Exception {
        String s;
        if ((s = m.getSubject()) != null) {
            return s;
        } else {
            return constants.SUBJECT;
        }
    }

    private int getSize(Message m) throws Exception {
        return m.getSize();
    }

    public static void println(String s) {
        System.out.println(s);
    }

    public static void print(String s) {
        System.out.print(s);
    }
    
    public static void help() {
        print(
                "\nUsage: java Imap2SQLite -h host -m email -p [password] [-d directory] [-f folder]\n"
                + "    [-start 'd mmm yyyy'] [-end 'd mmm yyyy'] [-tls] [-flush] [-test]\n\n"
                + "    d: save folder(s) to directory (default /tmp/DB)\n"
                + "    f: folder imap to backup (default all folders)\n"
                + "start: start date (default is '1 Jan 1970')\n"
                + "  end: end date (default is now)\n"
                + "  tls: using tls (default is no tls)\n"
                + " test: no backup email(s) to database file(s)\n"
                + "flush: flush or delete email(s) after downloading (default is no)\n\n");
        System.exit(0);
    }
    
    public static void main(String args[]) throws NumberFormatException, ParseException {

        int optind;
        String startDtStr, endDtStr;
        String host = null;
        String email = null;
        String password = null;
        String dest = "/tmp/DB";
        String source = null;
        boolean isTls = false;
        boolean isTest = false;
        boolean isFlush = false;
        Date dateStart = dateFormat.FORMAT_3.parse("1/1/1970");
        Date dateEnd = new Date();
        Date now = new Date();
        Console console = System.console();

        if (args.length == 0) {
            help();
        }

        for (optind = 0; optind < args.length; optind++) {
            if (args[optind].equals("-h")) {
                host = args[optind+1];
            } else if (args[optind].equals("-m")) {
                email = args[optind+1];
            } else if (args[optind].equals("-p")) {
                try {
                    // user typing password after `-p`
                    password = args[optind+1];                
                } catch (Exception e) {
                    // option `-p` at the end of command
                    password = "-blah"; 
                }
            } else if (args[optind].equals("-d")) {
                dest = args[optind+1];
            } else if (args[optind].equals("-f")) {
                source = args[optind+1];
            } else if (args[optind].equals("-start")) {
                startDtStr = args[optind+1];
                dateStart = checkInputDate.check(startDtStr.concat(" 00:00:00 " + TZ));
                if (dateStart == null) {
                    println("Input date not valid");
                    System.exit(1);
                } else {
                    if (dateStart.compareTo(now) > 0) {
                        println("Start date should be equal or older than now.");
                        System.exit(0);
                    }
                }
            } else if (args[optind].equals("-end")) {
                endDtStr = args[optind+1];
                dateEnd = checkInputDate.check(endDtStr.concat(" 23:59:29 " + TZ));
                if (dateEnd == null) {
                    println("Input date not valid");
                    System.exit(1);
                } else {
                    if (dateStart.compareTo(dateEnd) > 0) {
                        println("End date should be equal or younger than start date.");
                        System.exit(0);
                    }
                }
            } else if (args[optind].equals("-tls")) {
                isTls = true;
            } else if (args[optind].equals("-test")) {
                isTest = true;
            } else if (args[optind].equals("-flush")) {
                isFlush = true;
            } else if (args[optind].startsWith("-")) {
                help();
            }
        }

        if (password != null && password.startsWith("-")) {
           print("Enter password: ");
           password = String.valueOf(console.readPassword());
        };

        if (dateStart.compareTo(dateEnd) >= 0) {
            System.out.println("Start date should be equals or older than end date.");
            System.exit(1);
        }

        if (email == null || password == null || host == null) {
            help();
        }
        Imap2SQLite imap2SQLite = new Imap2SQLite(host, email, password, dest, source, dateStart, dateEnd, isTls, isTest, isFlush);
    }
}

