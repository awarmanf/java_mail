# ShowMsgSQL

Show the content of the message which is saved as compressed blob at sqlite3 db.

## How to compile

Export CLASSPATH first

```bash
export CLASSPATH=lib/javax.mail.jar:lib/sqlite-jdbc-3.27.2.1.jar:lib/activation.jar:.
```

Then compile
```bash
javac com/yudi/mail/ShowMsgSQL.java
```

## How to run

First, enter command below to show the help

```bash
$ java com.yudi.mail.ShowMsgSQL
Usage: java ShowMessage folderimap.db id
```

## Show content of the message

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

