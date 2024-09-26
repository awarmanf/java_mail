# Imap2Flush

Flush (delete) emails at certain folder or all folders imap - except exclude folders - can be based on time span, subject or sender.

## How to compile

Export CLASSPATH first

```bash
export CLASSPATH=lib/javax.mail.jar:lib/sqlite-jdbc-3.27.2.1.jar:lib/activation.jar:.
```

Then compile

```bash
javac com/yudi/mail/Imap2Flush.java
```

## How to run

First, enter command below to show the help

```bash
java com.yudi.mail.Imap2Flush
```

```
Usage: java Imap2Flush -h host -m email -p [password] [-f folder][-start 'd mmm yyyy'][-end 'd mmm yyyy'][-tls][-S 'subject'][-F 'from'][-test]

subject: subject to search
   from: from to search
      f: folder imap to flush (default all folders)
  start: start date (default is '1 Jan 1970')
    end: end date (default is now)
    tls: using tls (default is no tls)
   test: only show email(s) to flush
```

Without tls connection

```bash
java com.yudi.mail.Imap2Flush -h jember.net -m yudi@jember.net -p -f Inbox.Sent -test
```

>Use option `-t` to show the messages that match while not really flushing them.

Add option `-Djavax.net.ssl.trustStore=/home/yudi/keystore` if  using tls connection to a mail server which have a self signed certificate.

Test to flush (delete) email from '1 jan 2023' until '31 dec 2024'.

```bash
java -Djavax.net.ssl.trustStore=/home/yudi/keystore com.yudi.mail.Imap2Flush -h jember.net -m yudi@jember.net -p -f Inbox.Sent -tls -start '1 jan 2023' -end '31 dec 2024' -test
```

The output

```
Thu, 26 Sep 14:38:36 WIB 2024

Flushing email(s) yudi@jember.net at jember.net from 01-01-2023 until 31-12-2024 (TEST ONLY)
1. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: [gadtorade] WTA: mengatasi icloud penuh; Date: 2023-01-05 10:19:38; 4544 bytes.
2. From: Arief Yudhawarman <yudi@jember.net>; Subject: WTI: IPv6 (Was: [gadtorade] CCTV/NVR EZVIZ DAN HIKVISON); Date: 2023-01-19 14:28:37; 91948 bytes.
3. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: WTI: IPv6 (Was: [gadtorade] CCTV/NVR EZVIZ DAN HIKVISON); Date: 2023-01-19 14:40:17; 88025 bytes.
4. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: WTI: IPv6 (Was: [gadtorade] CCTV/NVR EZVIZ DAN HIKVISON); Date: 2023-01-19 15:09:34; 3280 bytes.
5. From: Arief Yudhawarman <yudi@jember.net>; Subject: tes email; Date: 2023-01-24 15:31:40; 428 bytes.
6. From: Arief Yudhawarman <yudi@jember.net>; Subject: tes; Date: 2023-01-24 15:33:12; 413 bytes.
7. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: [gadtorade] Wta: redmi tab atau realme tab?; Date: 2023-01-31 11:58:17; 1271 bytes.
8. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: [gadtorade] WTA: Putus Indihome; Date: 2023-03-09 11:37:00; 1050 bytes.
9. From: Arief Yudhawarman <yudi@jember.net>; Subject: WTB: Monitor LCD 14" atau 15" second mulus (Surabaya); Date: 2023-08-15 20:48:34; 938 bytes.
10. From: Arief Yudhawarman <yudi@jember.net>; Subject: WTB: Lenovo ThinkPad X230 atau series di atasnya; Date: 2023-08-15 21:03:27; 1270 bytes.
11. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: [gadtorade] WTB: Monitor LCD 14" atau 15" second mulus (Surabaya); Date: 2023-08-15 21:05:17; 1314 bytes.
12. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: [gadtorade] WTA: Cara Belanja di TikTok; Date: 2023-09-26 10:18:40; 988 bytes.
13. From: Arief Yudhawarman <yudi@jember.net>; Subject: Tes email; Date: 2023-11-03 10:12:32; 2552894 bytes.
14. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: [gadtorade] WTA OOT : ac mobil kurang dingin; Date: 2023-11-20 15:25:20; 205968 bytes.
15. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: [gadtorade] WTA: wireless cctv outdoor; Date: 2023-11-27 10:19:47; 1296 bytes.
16. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: [gadtorade] WTA : email system; Date: 2024-01-05 14:43:39; 5001 bytes.
17. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: [gadtorade] WTA rekomendasi laptop thinkpad dan recommended seller; Date: 2024-02-01 09:41:40; 5365 bytes.
18. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: [gadtorade] WTA rekomendasi laptop thinkpad dan recommended seller; Date: 2024-02-23 11:39:09; 4882 bytes.
19. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: [gadtorade] WTA rekomendasi laptop thinkpad dan recommended seller; Date: 2024-02-23 11:43:37; 6178 bytes.
20. From: Arief Yudhawarman <yudi@jember.net>; Subject: WTB: Android TV Box; Date: 2024-05-21 18:49:58; 939 bytes.
21. From: Arief Yudhawarman <yudi@jember.net>; Subject: WTB: Samsung S20 RAM 12 / ROM 128 (Second); Date: 2024-05-25 22:06:16; 139584 bytes.
22. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: [gadtorade] Berita Duka: Istri Om Ju Ming Berpulang; Date: 2024-05-25 22:08:03; 4197 bytes.
23. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: [gadtorade] WTB: Samsung S20 RAM 12 / ROM 128 (Second); Date: 2024-05-26 08:19:31; 1256 bytes.
24. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: [gadtorade] WTB: Android TV Box; Date: 2024-05-26 09:42:17; 5115 bytes.
25. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: [gadtorade] WTB: Samsung S20 RAM 12 / ROM 128 (Second); Date: 2024-05-26 13:09:49; 1337 bytes.
26. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: [gadtorade] WTB: Samsung S20 RAM 12 / ROM 128 (Second); Date: 2024-05-26 13:13:57; 1310 bytes.
27. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: [gadtorade] OOT WTA Komik jadul; Date: 2024-06-18 19:56:47; 1744 bytes.
28. From: Arief Yudhawarman <yudi@jember.net>; Subject: Re: [gadtorade] OOT WTA Komik jadul; Date: 2024-06-18 20:12:22; 1814 bytes.
29. From: Arief Yudhawarman <yudi@jember.net>; Subject: WTA: CCTV dengan dual camera bertolak belakang; Date: 2024-08-09 12:16:46; 667 bytes.
30. From: Arief Yudhawarman <yudi@jember.net>; Subject: Tes html; Date: 2024-09-08 18:05:27; 841 bytes.
31. From: Arief Yudhawarman <yudi@jember.net>; Subject: Tes email plain and html; Date: 2024-09-08 18:09:53; 4971 bytes.
```



