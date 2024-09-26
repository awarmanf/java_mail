package com.yudi.util;

import java.util.regex.Pattern;

/**
 *
 * constants
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

public class constants {
    
    public static final int BUFFER = 1024;
    public static final String CRLF = "\r\n";
    public static final String UNIX_EPOCH_DATE = "1 Jan 1970";
    public static final String UNIX_EPOCH_DATE1 = "1970-01-01 00:00:00";
    public static final String UNIX_EPOCH_DATE2 = "1970-01-01 00:00:00 +00:00";
    public static final String UNIX_EPOCH_DATE3 = "Thu Jan 01 00:00:00 UTC 1970";
    public static final String[] EXCLUDES = {"Chats", "Contacts", "Drafts", "Emailed Contacts", "Junk", "Junk E-mail", "Trash", "Restore"};
    public static final String FROM = "nobody@nowhere.com";
    public static final String SUBJECT = "No Subject";
    // patterm email for top header of file mbox
    public static final Pattern PAT_EMAIL = Pattern.compile("[\\w\\.-]{1,}@[\\w-\\.]{2,}\\.[\\w]{1,}", Pattern.CASE_INSENSITIVE);
}
