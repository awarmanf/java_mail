# SQLite2Imap

Restore compressed messages which saved at sqlite3 database to imap folder.

## How to compile

Export CLASSPATH first

```bash
export CLASSPATH=lib/javax.mail.jar:lib/sqlite-jdbc-3.27.2.1.jar:lib/activation.jar:.
```

Then compile

```bash
javac com/yudi/mail/SQLite2Imap.java
```

## How to run

First, enter command below to show the help

```bash
java com.yudi.mail.SQLite2Imap
```

```
Usage: java SQLite2Imap -m email -p [password] -f 'file db' -h host [ -r 'folder imap']
   [-start date] [-end date] [-S 'Subject'] [-F 'sender'] [-tls] [-test]
   r : folder imap to restore (default is Inbox)
start: start date (default is '1 Jan 1970')
  end: end date (default is now)
  tls: using tls (default is no tls)
 test: no restore message(s) to imap folder
```

Without tls connection

```bash
java com.yudi.mail.SQLite2Imap -h jember.net -m yudi@jember.net -p -f /tmp/DB/2024/09/yudi@jember.net/INBOX.Sent.db -r Inbox.Sent -test
```

>Use option `-t` to show the messages that match while not downloading them.

Add option `-Djavax.net.ssl.trustStore=/home/yudi/keystore` if using tls connection to a mail server which have a self signed certificate.

Restore message at file database `/tmp/DB/2024/09/yudi@jember.net/INBOX.Sent.db` to folder imap _Inbox.Sent_

```bash
java -Djavax.net.ssl.trustStore=/home/yudi/keystore com.yudi.mail.SQLite2Imap -h jember.net -m yudi@jember.net -p -f /tmp/DB/2024/09/yudi@jember.net/INBOX.Sent.db -r Inbox.Sent -tls
```

