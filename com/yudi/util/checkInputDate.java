package com.yudi.util;

import java.text.ParseException;
import java.util.Date;

/**
 *
 * checkInputDate
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

public class checkInputDate {

    public static Date check(String dateStr) throws ParseException {

        Date date = null;

        // 30-12-2020
        if (dateStr.matches("\\d{1,2}-\\d{1,2}-\\d{4}")) {
            date = dateFormat.FORMAT_1.parse(dateStr);
        // 30-12-2020 10:11:12 +0700
        } else if (dateStr.matches("\\d{1,2}-\\d{1,2}-\\d{4} \\d{2}:\\d{2}:\\d{2} \\+\\d{4}")) {
            date = dateFormat.FORMAT_2Z.parse(dateStr);
        // 30/12/2020
        } else if (dateStr.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
            date = dateFormat.FORMAT_3.parse(dateStr);
        // 30/12/2020 10:11:12 +0700
        } else if (dateStr.matches("\\d{1,2}/\\d{1,2}/\\d{4} \\d{2}:\\d{2}:\\d{2} \\+\\d{4}")) {
            date = dateFormat.FORMAT_4Z.parse(dateStr);
        // 30 dec 2020
        } else if (dateStr.matches("\\d{1,2} \\w{3} \\d{4}")) {
            date = dateFormat.FORMAT_5.parse(dateStr);
        // 30 dec 2020 10:11:12 +0700
        } else if (dateStr.matches("\\d{1,2} \\w{3} \\d{4} \\d{2}:\\d{2}:\\d{2} \\+\\d{4}")) {
            date = dateFormat.FORMAT_6Z.parse(dateStr);
        }
        return date;
    }

    public static void main(String[] args) {

        String s = null;
        
        if (args.length > 0) {
            s = args[0];
        } else {
            s = "01-11-2020 10:11:12 +0700";
        }
        
        try {
            if (checkInputDate.check(s) != null) {
                System.out.println(s + " is date");
            } else {
                System.out.println(s + " is not date.");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
