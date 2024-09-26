# Imap2Mbox

Save imap folder(s) via protocol imap to files with mbox format. Messages will be appended to the existing file.

## How to compile

Export CLASSPATH first

```bash
export CLASSPATH=lib/javax.mail.jar:lib/sqlite-jdbc-3.27.2.1.jar:lib/activation.jar:.
```

Then compile
```bash
javac com/yudi/mail/Imap2Mbox.java
```

## How to run

First, enter command below to show the help

```bash
java com.yudi.mail.Imap2Mbox
```

```
Usage: java Imap2Mbox -h host -m email -p [password] [-d directory] [-f folder]
    [-start 'd mmm yyyy'] [-end 'd mmm yyyy'] [-tls] [-flush] [-test]
    d: save folder(s) to directory (default /tmp/MBOX)
    f: folder imap to backup (default all folders)
start: start date (default is '1 Jan 1970')
  end: end date (default is now)
  tls: using tls (default is no tls)
 test: no backup email(s) to mbox file(s)
flush: flush or delete email(s) after downloading (default is no)
```

Without tls connection

```bash
java com.yudi.mail.Imap2Mbox -h jember.net -m yudi@jember.net -p -f Inbox.Sent -test
```

>Use option `-t` to show the messages that match while not downloading them.

Add option `-Djavax.net.ssl.trustStore=/home/yudi/keystore` if using tls connection to a mail server which have a self signed certificate.

Download email from '1 jan 2023' until '31 dec 2024'.

```bash
java -Djavax.net.ssl.trustStore=/home/yudi/keystore com.yudi.mail.Imap2Mbox -h jember.net -m yudi@jember.net -p -f Inbox.Sent -tls -start '1 jan 2023' -end '31 dec 2024'
```

You can see the messages using Mozilla Thunderbird.

[How to Open an MBOX File (Using Mozilla Thunderbird)](https://www.howtogeek.com/709718/how-to-open-an-mbox-file-in-mozilla-thunderbird/)

## Structure of mbox files

The messages saved at directory `/tmp/MBOX`

```bash
/tmp/MBOX/
└── yudi@jember.net
    ├── 2023
    │   ├── 01
    │   │   ├── INBOX.Sent.mbx
    │   │   └── yudi@jember.net.log
    │   ├── 03
    │   │   ├── INBOX.Sent.mbx
    │   │   └── yudi@jember.net.log
    │   ├── 08
    │   │   ├── INBOX.Sent.mbx
    │   │   └── yudi@jember.net.log
    │   ├── 09
    │   │   ├── INBOX.Sent.mbx
    │   │   └── yudi@jember.net.log
    │   └── 11
    │       ├── INBOX.Sent.mbx
    │       └── yudi@jember.net.log
    └── 2024
        ├── 01
        │   ├── INBOX.Sent.mbx
        │   └── yudi@jember.net.log
        ├── 02
        │   ├── INBOX.Sent.mbx
        │   └── yudi@jember.net.log
        ├── 05
        │   ├── INBOX.Sent.mbx
        │   └── yudi@jember.net.log
        ├── 06
        │   ├── INBOX.Sent.mbx
        │   └── yudi@jember.net.log
        ├── 08
        │   ├── INBOX.Sent.mbx
        │   └── yudi@jember.net.log
        └── 09
            ├── INBOX.Sent.mbx
            └── yudi@jember.net.log

24 directories, 22 files
```

## Show content of the mbox file

```bash
head -n 30 /tmp/MBOX/yudi@jember.net/2024/09/INBOX.Sent.mbx 
```

The output

```
From yudi@jember.net Sun Sep 08 18:05:27 WIB 2024
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
```

