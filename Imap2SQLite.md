# Imap2SQLite

Save imap folder(s) via protocol imap to database sqlite3. The message body will be saved in compressed mode.

## How to compile

Export CLASSPATH first

```bash
export CLASSPATH=lib/javax.mail.jar:lib/sqlite-jdbc-3.27.2.1.jar:lib/activation.jar:.
```

Then compile
```bash
javac com/yudi/mail/Imap2SQLite.java
```

## How to run

First, enter command below to show the help

```bash
java com.yudi.mail.Imap2SQLite
```

```
Usage: java Imap2SQLite -h host -m email -p [password] [-d directory] [-f folder]
    [-start 'd mmm yyyy'] [-end 'd mmm yyyy'] [-tls] [-flush] [-test]

    d: save folder(s) to directory (default /tmp/DB)
    f: folder imap to backup (default all folders)
start: start date (default is '1 Jan 1970')
  end: end date (default is now)
  tls: using tls (default is no tls)
 test: no backup email(s) to database file(s)
flush: flush or delete email(s) after downloading (default is no)
```

Without tls connection

```bash
java com.yudi.mail.Imap2SQLite -h jember.net -m yudi@jember.net -p -f Inbox.Sent -test
```

>Use option `-t` to show the messages that match while not downloading them.

Add option `-Djavax.net.ssl.trustStore=/home/yudi/keystore` if using tls connection to a mail server which have a self signed certificate.

Download email from '1 jan 2023' until '31 dec 2024'.

```bash
java -Djavax.net.ssl.trustStore=/home/yudi/keystore com.yudi.mail.Imap2SQLite -h jember.net -m yudi@jember.net -p -f Inbox.Sent -tls -start '1 jan 2023' -end '31 dec 2024'
```

## Structure of database files

The messages saved at directory `/tmp/DB`

```
/tmp/DB/
├── 2023
│   ├── 01
│   │   └── yudi@jember.net
│   │       ├── INBOX.Sent.db
│   │       └── yudi@jember.net.log
│   ├── 03
│   │   └── yudi@jember.net
│   │       ├── INBOX.Sent.db
│   │       └── yudi@jember.net.log
│   ├── 08
│   │   └── yudi@jember.net
│   │       ├── INBOX.Sent.db
│   │       └── yudi@jember.net.log
│   ├── 09
│   │   └── yudi@jember.net
│   │       ├── INBOX.Sent.db
│   │       └── yudi@jember.net.log
│   └── 11
│       └── yudi@jember.net
│           ├── INBOX.Sent.db
│           └── yudi@jember.net.log
└── 2024
    ├── 01
    │   └── yudi@jember.net
    │       ├── INBOX.Sent.db
    │       └── yudi@jember.net.log
    ├── 02
    │   └── yudi@jember.net
    │       ├── INBOX.Sent.db
    │       └── yudi@jember.net.log
    ├── 05
    │   └── yudi@jember.net
    │       ├── INBOX.Sent.db
    │       └── yudi@jember.net.log
    ├── 06
    │   └── yudi@jember.net
    │       ├── INBOX.Sent.db
    │       └── yudi@jember.net.log
    ├── 08
    │   └── yudi@jember.net
    │       ├── INBOX.Sent.db
    │       └── yudi@jember.net.log
    └── 09
        └── yudi@jember.net
            ├── INBOX.Sent.db
            └── yudi@jember.net.log

24 directories, 22 files
```

## Show content of the message

Show content of the message using sqlite commands

```bash
sqlite3 /tmp/DB/2024/09/yudi@jember.net/INBOX.Sent.db 
```

```sql
SQLite version 3.37.2 2022-01-06 13:25:41
Enter ".help" for usage hints.
sqlite> .schema
CREATE TABLE email ( id integer PRIMARY KEY,  date DATETIME,  sender VARCHAR(80), subject VARCHAR(160), size INTEGER, header BLOB, body BLOB);
sqlite> select * from email limit 1;
1|2024-09-08 18:05:27|Arief Yudhawarman <yudi@jember.net>|Tes html|841|Message-ID: <ad17a999-34b5-4681-bdfe-915a5c77e0a5@jember.net>
Date: Sun, 8 Sep 2024 17:21:02 +0700
MIME-Version: 1.0
User-Agent: Mozilla Thunderbird
Content-Language: en-US
To: it.sysadmin@domain.org
From: Arief Yudhawarman <yudi@jember.net>
Subject: Tes html
Content-Type: text/html; charset=UTF-8
Content-Transfer-Encoding: 7bit
|�
```

The message body was compressed. You must use a tool `ShowMsgSQL`.

```bash
java com.yudi.mail.ShowMsgSQL /tmp/DB/2024/09/yudi@jember.net/INBOX.Sent.db 1
```

The output

```
Date: 2024-09-08 18:05:27
From: Arief Yudhawarman <yudi@jember.net>
Subject: Tes html

Message-ID: <ad17a999-34b5-4681-bdfe-915a5c77e0a5@jember.net>
Date: Sun, 8 Sep 2024 17:21:02 +0700
MIME-Version: 1.0
User-Agent: Mozilla Thunderbird
Content-Language: en-US
To: it.sysadmin@domain.org
From: Arief Yudhawarman <yudi@jember.net>
Subject: Tes html
Content-Type: text/html; charset=UTF-8
Content-Transfer-Encoding: 7bit

<!DOCTYPE html>
<html>
  <head>

    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
  </head>
  <body>
    <p><br>
    </p>
    <p>Hi,</p>
    <p>This is the link of the file <a moz-do-not-send="true"
href="http://domain.org/app/get.cgi?id=158&amp;f=Homer_Simpson_2006.png">Homer
        Simpson</a><br>
    </p>
    <p><br>
    </p>
    <p>Sincerely,</p>
    <p><br>
    </p>
    <p>Arief Yudhawarman</p>
    <p><br>
    </p>
  </body>
</html>
```

