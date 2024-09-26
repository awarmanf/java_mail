package com.yudi.mail;

import java.util.*;
import javax.mail.*;
import java.io.Console;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import com.yudi.util.*;

/**
 *
 * Imap2Flush
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

public class Imap2Flush {

    final static List<String> LIST_EXCLUDES = Arrays.asList(constants.EXCLUDES);
    final static String FILE_SEPARATOR = System.getProperty("file.separator");
    final static String TZ = (new SimpleDateFormat("Z", Locale.getDefault())).format(new Date());
    
    
    private String host;
    private String email;
    private String password;
    private String source;
    private String searchSubject;
    private String searchFrom;
    private boolean isTest;
    private boolean isTls;
    private Date dateStart;
    private Date dateEnd;
    
    public Imap2Flush(String host, String email, String password, String source, Date dateStart,
            Date dateEnd, String searchSubject, String searchFrom, boolean isTls, boolean isTest) {

        String protocol;
        String pattern = "%";
        Folder folder = null;
        Store store = null;

        // initialiaze static variables of class
        this.host = host;
        this.email = email;
        this.password = password;
        this.source = source;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.searchSubject = searchSubject;
        this.searchFrom = searchFrom;
        this.isTest = isTest;
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

            // Get a Session object
            Session session = Session.getInstance(props, null);
            store = session.getStore(protocol);

            // Connect
            store.connect(host, email, password);

            // List namespace
            if (source != null) {
                folder = store.getFolder(source);
            } else {
                folder = store.getDefaultFolder();
            }

            if ((folder.getType() & Folder.HOLDS_FOLDERS) != 0) {

                println(dateFormat.FORMAT_JMAIL2.format(new Date()));
                // Show date now using format SDFMT "E, dd MMM HH:mm:ss z yyyy"
                print("\nFlushing email(s) " + email + " at " + host + " from "
                        + dateFormat.FORMAT_1.format(dateStart)
                        + " until " + dateFormat.FORMAT_1.format(dateEnd));
                if (isTest) {
                    println(" (TEST ONLY)");
                } else {
                    println("");
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
            store.close();

        } catch (NoSuchProviderException ex) {
            println("NoSuchProviderException! " + ex.getMessage());
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
        int size;

        // check folder if not in excluded list then process to open folder
        if (isFolderNotExcludes(folder.getFullName())) {

            if (folder.getMessageCount() > 0) {

                try {
                    folder.open(Folder.READ_WRITE);
                } catch (MessagingException ex) {
                    folder.open(Folder.READ_ONLY);
                }
                Message[] msgs = folder.getMessages();

                int match = 0;
                for (Message msg : msgs) {
                    from = getFrom(msg);
                    date = getReceivedDate(msg);
                    subject = getSubject(msg);
                    size = getSize(msg);
                    datesql = convertDate(date);

                    if ((dateFormat.FORMAT_JMAIL.parse(date).compareTo(dateStart) >= 0)
                            && (dateFormat.FORMAT_JMAIL.parse(date).compareTo(dateEnd) <= 0)) {

                        if (searchSubject != null) {
                            if (subject.contains(searchSubject)) {
                                println(++match + ". " + "From: " + from
                                        + "; Subject: " + subject
                                        + "; Date: " + datesql
                                        + "; " + size + " bytes.");
                                if (!isTest) {
                                    msg.setFlag(Flags.Flag.DELETED, true);
                                }
                            }
                        } else if (searchFrom != null) {
                            if (from.contains(searchFrom)) {
                                println(++match + ". " + "From: " + from
                                        + "; Subject: " + subject
                                        + "; Date: " + datesql
                                        + "; " + size + " bytes.");
                                if (!isTest) {
                                    msg.setFlag(Flags.Flag.DELETED, true);
                                }
                            }
                        } else {
                            println(++match + ". " + "From: " + from
                                    + "; Subject: " + subject
                                    + "; Date: " + datesql
                                    + "; " + size + " bytes.");
                            if (!isTest) {
                                msg.setFlag(Flags.Flag.DELETED, true);
                            }
                        }
                    } // match date
                }
                if (isTest) {
                    folder.close(false);
                } else {
                    folder.close(true); // expunge message if flush is true
                }
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
                "\nUsage: java Imap2Flush -h host -m email -p [password] [-f folder]"
                + "[-start 'd mmm yyyy'][-end 'd mmm yyyy'][-tls][-S 'subject'][-F 'from'][-test]\n\n"
                + "subject: subject to search\n"
                + "   from: from to search\n"
                + "      f: folder imap to flush (default all folders)\n"
                + "  start: start date (default is '1 Jan 1970')\n"
                + "    end: end date (default is now)\n"
                + "    tls: using tls (default is no tls)\n"
                + "   test: only show email(s) to flush\n\n");
        System.exit(0);
    }
    
    public static void main(String argv[]) throws ParseException {

        int optind;
        String startDtStr, endDtStr;
        String host = null;
        String email = null;
        String password = null;
        String searchSubject = null;
        String searchFrom = null;
        String source = null;
        boolean isTls = false;
        boolean isTest = false;
        Date dateStart = dateFormat.FORMAT_3.parse("1/1/1970");
        Date dateEnd = new Date();
        Date dateNow = new Date();
        Console console = System.console();

        if (argv.length == 0) {
            help();
        }

        for (optind = 0; optind < argv.length; optind++) {
            if (argv[optind].equals("-h")) {
                host = argv[optind+1];
            } else if (argv[optind].equals("-m")) {
                email = argv[optind+1];
            } else if (argv[optind].equals("-p")) {
                try {
                    // user typing password after `-p`
                    password = argv[optind+1];                
                } catch (Exception e) {
                    // option `-p` at the end of command
                    password = "-blah"; 
                }
            } else if (argv[optind].equals("-S")) {
                searchSubject = argv[optind+1];
            } else if (argv[optind].equals("-F")) {
                searchFrom = argv[optind+1];
            } else if (argv[optind].equals("-f")) {
                source = argv[optind+1];
            } else if (argv[optind].equals("-start")) {
                startDtStr = argv[optind+1];
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
            } else if (argv[optind].equals("-end")) {
                endDtStr = argv[optind+1];
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
            } else if (argv[optind].equals("-tls")) {
                isTls = true;
            } else if (argv[optind].equals("-test")) {
                isTest = true;
            } else if (argv[optind].startsWith("-")) {
                help();
            }
        }

        if (password != null && password.startsWith("-")) {
           print("Enter password: ");
           password = String.valueOf(console.readPassword());
        };

        // check start and end date
        if (dateStart.compareTo(dateEnd) >= 0) {
            System.out.println("Start date should be equals or older than end date.");
            System.exit(1);
        }

        // all required arguments must be exist
        if (host == null || email == null || password == null) {
            help();
        }
        Imap2Flush imap2Flush = new Imap2Flush (host, email, password, source, dateStart, dateEnd, searchSubject, searchFrom, isTls, isTest);
    }
}

