package com.yudi.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 *
 * dateFormat
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

public class dateFormat {

    public static final String UNIX_EPOCH_DATE = "1/1/1970";
    public static final String UNIX_EPOCH_DATE1 = "1970-01-01 00:00:00";
    public static final String UNIX_EPOCH_DATE2 = "1970-01-01 00:00:00 +0000";
    public static final String UNIX_EPOCH_DATE3 = "Thu Jan 01 00:00:00 UTC 1970";
    public static final DateFormat FORMAT_1 = new SimpleDateFormat("dd-MM-yyyy");
    public static final String PATTERN_1 = "\\d{1,2}-\\d{1,2}-\\d{4}";
    public static final DateFormat FORMAT_2 = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    public static final String PATTERN_2 = "\\d{1,2}-\\d{1,2}-\\d{4} \\d{2}:\\d{2}";
    public static final DateFormat FORMAT_2Z = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    public static final String PATTERN_2Z = "\\d{1,2}-\\d{1,2}-\\d{4} \\d{2}:\\d{2}:\\d{2} \\+\\d{4}";
    public static final DateFormat FORMAT_3 = new SimpleDateFormat("dd/MM/yyyy");
    public static final String PATTERN_3 = "\\d{1,2}/\\d{1,2}/\\d{4}";
    public static final DateFormat FORMAT_4 = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    public static final String PATTERN_4 = "\\d{1,2}/\\d{1,2}/\\d{4} \\d{2}:\\d{2}";
    public static final DateFormat FORMAT_4Z = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z");
    public static final String PATTERN_4Z = "\\d{1,2}/\\d{1,2}/\\d{4} \\d{2}:\\d{2}:\\d{2} \\+\\d{4}";
    public static final DateFormat FORMAT_5 = new SimpleDateFormat("dd MMM yyyy");
    public static final String PATTERN_5 = "\\d{1,2} \\w{3} \\d{4}";
    public static final DateFormat FORMAT_6 = new SimpleDateFormat("dd MMM yyyy HH:mm");
    public static final String PATTERN_6 = "\\d{1,2} \\w{3} \\d{4} \\d{2}:\\d{2}";
    public static final DateFormat FORMAT_6Z = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
    public static final String PATTERN_6Z = "\\d{1,2} \\w{3} \\d{4} \\d{2}:\\d{2}:\\d{2} \\+\\d{4}";
    public static final DateFormat FORMAT_SQL1 = new SimpleDateFormat("yyyy-MM-dd");
    public static final String PATTERN_SQL1 = "\\d{4}-\\d{2}-\\d{2}";
    public static final DateFormat FORMAT_SQL2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final String PATTERN_SQL2 = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}";
    public static final DateFormat FORMAT_SQL2Z = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    public static final String PATTERN_SQL2Z = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} \\+\\d{4}";
    public static final DateFormat FORMAT_SQL3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final DateFormat FORMAT_SQL3Z = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    public static final DateFormat FORMAT_JMAIL = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
    public static final DateFormat FORMAT_JMAIL2 = new SimpleDateFormat("E, dd MMM HH:mm:ss z yyyy");
}
