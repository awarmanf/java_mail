package com.yudi.mail;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;
import javax.mail.*;
import java.util.regex.Matcher;
import com.yudi.util.*;
import java.io.File;
import java.io.Console;
import java.text.ParseException;

/**
 *
 * Imap2Mbox
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

public class Imap2Mbox {

    final static List<String> LIST_EXCLUDES = Arrays.asList(constants.EXCLUDES);
    final static String FILE_SEPARATOR = System.getProperty("file.separator");

    private String host;
    private String email;
    private String password;
    private String dest;
    private String source;
    private String user_dir;
    private boolean isTest;
    private boolean isFlush;
    private boolean isTls;
    private Date dateStart;
    private Date dateEnd;
    
    private Map<String, String> hmDirLog = new HashMap<String, String>();
    private Map<String, Integer> hmLogCounterFolder = new HashMap<String, Integer>();
    private Map<String, java.io.PrintWriter> hmPrintLog = new HashMap<String, java.io.PrintWriter>();

    public Imap2Mbox(String host, String email, String password, String dest, String source, 
            Date dateStart, Date dateEnd, boolean isTls, boolean isTest, boolean isFlush) 
    {

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

            if (isTls) {
                props.put("mail.imap.starttls.enable", true);
                protocol = "imaps";
            }

            // Get a Session object
            Session session = Session.getInstance(props, null);
            store = session.getStore(protocol);

            // Connect
            if (host != null || email != null || password != null) {
                store.connect(host, email, password);
            }

            // List namespace
            if (source != null) {
                folder = store.getFolder(source);
            } else {
                folder = store.getDefaultFolder();
            }

            if ((folder.getType() & Folder.HOLDS_FOLDERS) != 0) {
                if (source != null) {
                    dumpFolder(folder, false);
                } else {
                    // check all folder (recursive)
                    Folder[] fd = folder.list(pattern);
                    for (int i = 0; i < fd.length; i++) {
                        dumpFolder(fd[i], true);
                    }
                }
            }
            
            // using for-each loop for iteration over Map.entrySet()
            // close file log
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

        String date, datesql, from, fromheader, subject;
        String[] parts;
        int size;
        String header, subdir, file, fileLog;
        File ff;
        user_dir = dest.concat(FILE_SEPARATOR + email);

        if (!new File(user_dir).exists()) {
            boolean mkdirs = new File(user_dir).mkdirs();
            if (!mkdirs) {
                println("Failed to create: " + user_dir);
                System.exit(1);
            }
        }

        // check folder if not in excluded list then process to open folder
        if (isFolderNotExcludes(folder.getFullName())) {

            if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0) {

                // only process folder which has message(s)
                if (folder.getMessageCount() > 0) {

                    try {
                        folder.open(Folder.READ_WRITE);
                    } catch (MessagingException ex) {
                        folder.open(Folder.READ_ONLY);
                    }
                    Message[] msgs = folder.getMessages();

                    /* Folder Full Name    File
                     * INBOX               INBOX.mbx
                     * INBOX/BAK2          INBOX.BAK2.mbx
                     * INBOX/BAK/BAKK1     INBOX.BAK.BAKK1.mbx
                     */
                    file = folder.getFullName().replaceAll(System.getProperty("file.separator"), ".");

                    int counter;
                    String userLogFolder;
                    String fetch = "Matching";

                    for (Message msg : msgs) {
                        from = getFrom(msg);
                        fromheader = getFromHeader(from);
                        date = getReceivedDate(msg);
                        subject = getSubject(msg);
                        size = getSize(msg);

                        if ((dateFormat.FORMAT_JMAIL.parse(date).compareTo(dateStart) >= 0)
                                && (dateFormat.FORMAT_JMAIL.parse(date).compareTo(dateEnd) <= 0)) {

                            datesql = convertDate(date);

                            parts = datesql.split("-"); // parts[0] -> Year, parts[1] -> Month
                            // subdir => /tmp/MBOX/user/yyyy/mm
                            subdir = user_dir + FILE_SEPARATOR + parts[0] + FILE_SEPARATOR + parts[1];
                            fileLog = subdir.concat(FILE_SEPARATOR + email + ".log");
                            userLogFolder = subdir.concat("/" + folder.getFullName());

                            // first found "yyyy/mm" then create
                            // print header
                            // set counter=1
                            // create subdir if not exists
                            // create user.log if not exists
                            if (hmDirLog.get(subdir) == null) {
                                hmDirLog.put(subdir, fileLog);

                                if (!isTest) {
                                    fetch = "Fetching ";
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
                                }
                                header = "\n" + dateFormat.FORMAT_JMAIL2.format(new Date());
                                printOut(header, subdir, isTest);
                                header = fetch + " email(s) " + email + " at server " + host + " from "
                                        + dateFormat.FORMAT_1.format(dateStart)
                                        + " until " + dateFormat.FORMAT_1.format(dateEnd) + ".\n";
                                printOut(header, subdir, isTest);

                                // set counter = 1 and print folder name
                                counter = 1;
                                if (hmLogCounterFolder.get(userLogFolder) == null) {
                                    header = "Folder: " + folder.getFullName();
                                    printOut(header, subdir, isTest);
                                    hmLogCounterFolder.put(userLogFolder, counter);
                                }
                            } // proses parsing message di folder baru
                            else if (hmLogCounterFolder.get(userLogFolder) == null) {
                                counter = 1;
                                hmLogCounterFolder.put(userLogFolder, counter);
                                header = "\nFolder: " + folder.getFullName();
                                printOut(header, subdir, isTest);

                            }
                            // print caught message
                            userLogFolder = subdir.concat("/" + folder.getFullName());
                            counter = hmLogCounterFolder.get(userLogFolder);
                            header = counter
                                    + ". " + "Email from: " + from
                                    + "; Subject: " + subject
                                    + "; Date: " + datesql
                                    + "; " + size + " bytes.";
                            printOut(header, subdir, isTest);
                            hmLogCounterFolder.put(userLogFolder, ++counter);

                            if (!isTest) {
                                writeMsg(subdir, file, fromheader, date, msg);

                                if (isFlush) {
                                    msg.setFlag(Flags.Flag.DELETED, isFlush);
                                }
                            }
                        } // match date    
                    }
                    folder.close(false);
                }
            }
            if ((folder.getType() & Folder.HOLDS_FOLDERS) != 0) {
                if (recurse) {
                    Folder[] f = folder.list();
                    for (int i = 0; i < f.length; i++) {
                        dumpFolder(f[i], recurse);
                    }
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

    private void writeMsg(String subdir, String file,
            String fromheader, String date, Message m) throws Exception {
        file = subdir + FILE_SEPARATOR + file.concat(".mbx");
        try (BufferedOutputStream out = new BufferedOutputStream(
                new FileOutputStream(file, true), constants.BUFFER)) {
            String headerTop = "From " + fromheader + " " + date + constants.CRLF;
            out.write(headerTop.getBytes());
            m.writeTo(out);
            out.write(constants.CRLF.getBytes());
            out.close();
        }
    }

    private String getFrom(Message m) throws Exception {
        Address[] a;
        if ((a = m.getFrom()) != null) {
            return a[0].toString();
        } else {
            return constants.FROM;
        }
    }

    private String getFromHeader(String from) throws Exception {
        String from2;
        from2 = constants.FROM;
        Matcher matcher = constants.PAT_EMAIL.matcher(from);
        while (matcher.find()) {
            from2 = matcher.group();
        }
        return from2;
    }

    private String getSentDate(Message m) throws Exception {
        Date d = m.getSentDate();
        return (d != null ? d.toString() : constants.UNIX_EPOCH_DATE3);
    }

    private String getReceivedDate(Message m) throws Exception {
        Date d = m.getReceivedDate();
        return (d != null ? d.toString() : constants.UNIX_EPOCH_DATE3);
    }

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

    private void printOut(String h, String s, boolean b) {
        println(h);
        if (!isTest) {
            hmPrintLog.get(s).println(h);
        }
    }

    public static void println(String s) {
        System.out.println(s);
    }

    public static void print(String s) {
        System.out.print(s);
    }

    public static void help() {
        print(
                "\nUsage: java Imap2Mbox -h host -m email -p [password] [-d directory] [-f folder]\n"
                + "    [-start 'd mmm yyyy'] [-end 'd mmm yyyy'] [-tls] [-flush] [-test]\n"
                + "    d: save folder(s) to directory (default /tmp/MBOX)\n"
                + "    f: folder imap to backup (default all folders)\n"
                + "start: start date (default is '1 Jan 1970')\n"
                + "  end: end date (default is now)\n"
                + "  tls: using tls (default is no tls)\n"
                + " test: no backup email(s) to mbox file(s)\n"
                + "flush: flush or delete email(s) after downloading (default is no)\n\n");
        System.exit(0);
    }


    public static void main(String args[]) throws NumberFormatException, ParseException {

        int optind;
        String startDtStr, endDtStr;
        String host = null;
        String email = null;
        String password = null;
        String dest = "/tmp/MBOX";
        String source = null;
        boolean isTls = false;
        boolean isTest = false;
        boolean isFlush = false;
        Date dateStart = dateFormat.FORMAT_3.parse("1/1/1970");
        Date dateEnd = new Date();
        Date dateNow = new Date();
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
                dateStart = checkInputDate.check(startDtStr);
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
                dateEnd = checkInputDate.check(endDtStr);
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
        Imap2Mbox imap2mbox = new Imap2Mbox (host, email, password, dest, source, dateStart, dateEnd, isTls, isTest, isFlush);
    }
}

