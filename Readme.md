
# Java Mail Utilities

This is a collection of mail utilities written in Java.

- [Imap2Mbox](Imap2Mbox.md)
- [Imap2SQLite](Imap2SQLite.md)
- [SQLite2Imap](SQLite2Imap.md)
- [Imap2Flush](Imap2Flush.md)
- [ShowMsgSQL](ShowMsgSQL.md)

I wrote these codes to support my job as mail administrator. 

You should compile the source with java version 1.8 or above in order to use a tls connection with 
a mail server which has an untrusted certificate (self signed certificate).

Requirements (jar libraries)

- activation.jar
- javax.mail.jar
- sqlite-jdbc-3.27.2.1.jar

## Imap Folder Separator

Some imap server use character `/` as a folder separator, others use `.`. For example, `Inbox/Sent`, `Inbox.Sent`, etc.

## Exclude Folders

The imap folders below are excluded or skipped by the my java applications.

- Chats
- Contacts
- Drafts
- Emailed Contacts
- Junk
- Junk E-mail
- Trash
- Restore

You can edit, add or delete the folders at source file `com/yudi/util/constants.java`.

```java
public static final String[] EXCLUDES = {"Chats", "Contacts", "Drafts", "Emailed Contacts", "Junk", "Junk E-mail", "Trash", "Restore"};
```

Compile the source file after you edit it.

## Trusted Certificates (SSL Notes)

To establish an SSL/TLS connection, the JavaMail client must be able to verify that the 
security certificate presented by the server it is connecting to is "trusted" by the 
client. Trusted certificates are maintained in a Java keystore file on the client. 
The J2SE SDK "keytool" command is used to maintain the keystore file.

There are two common approaches for verifying server certificates.

1. Server certificates may be signed be a well known public
   Certificate Authority.  The default Java keystore file contains
   the public keys of well known Certificate Authorities and can
   verify the server's certificate by following the chain of
   certificates signing the server's certificate back to one of
   these well known CA certificates.

   In this case the client doesn't need to manage certificates
   explicitly but can just use the default keystore file.

2. Server certificates may be _self-signed_. In this case there is
   no chain of signatures to use in verifying the server's certificate.
   Instead, the client will need the server's certificate in the
   client's keystore file.  The server's certificate is imported into
   the keystore file once, using the keytool command, and after that
   is used to verify connections to the server. A single keystore file
   may contain certificates of many servers.

## Adding Self Signed Certificate with Java keytool

You should run the command `keytool` in java version 1.8 or above.

### Download Self Signed Certificate

You can download the certificate in pem format using your favourite browser or `openssl` command.

    openssl s_client -showcerts -connect domain.org:443 < /dev/null 2> /dev/null|openssl x509 -outform PEM > /tmp/domain.org.pem

To view the certificate content

    openssl x509 -text -noout -in /tmp/domain.org.pem

### Add Self Signed Certificate

In this example certificates will be saved to the JRE keystore, eg. `/home/yudi/keystore`

This _domain.org_ certificate will be stored with alias _server1_

```bash
$ keytool -import -file "/tmp/domain.org.pem" -alias server1 -keystore "/home/yudi/keystore" -storepass "123456" 

Owner: CN=mail.domain.org, OU=Zimbra Collaboration Server
Issuer: CN=mail.domain.org, OU=Zimbra Collaboration Server, O=CA
Serial number: 1687503075
Valid from: Fri Jun 23 13:51:19 WIB 2023 until: Wed Jun 21 13:51:19 WIB 2028
Certificate fingerprints:
	 SHA1: 49:37:49:D4:95:DE:33:4C:3F:D9:9A:EA:A9:FB:26:0E:F1:40:8B:7A
	 SHA256: 9E:CE:53:F6:67:C4:2A:2A:66:B8:2D:6C:A1:C1:98:EE:F7:5A:7D:EB:61:AD:3D:36:B1:8C:B0:F7:92:91:B7:31
Signature algorithm name: SHA256withRSA
Subject Public Key Algorithm: 2048-bit RSA key
Version: 3

Extensions: 

#1: ObjectId: 2.5.29.19 Criticality=false
BasicConstraints:[
  CA:false
  PathLen: undefined
]

#2: ObjectId: 2.5.29.15 Criticality=false
KeyUsage [
  DigitalSignature
  Non_repudiation
  Key_Encipherment
]

#3: ObjectId: 2.5.29.17 Criticality=false
SubjectAlternativeName [
  DNSName: mail.domain.org
]

Trust this certificate? [no]:  yes
Certificate was added to keystore
```

You can add another certificate and saved as alias _server2_.

### List certificates

Use this command to list the certificates

```bash
keytool -list -v -keystore /home/yudi/keystore
```

### How to use 

Run the java application with the option `-Djavax.net.ssl.trustStore=/home/yudi/keystore`

For example

```bash
java -Djavax.net.ssl.trustStore=/home/yudi/keystore com.yudi.mail.Imap2SQLite -h host -m email -p -tls -test

```

## References

- [JavaMail API SSL Notes](https://www.oracle.com/java/technologies/javamail-sslnotes.html)
- [Add certificates to the JRE keystore](https://www.ibm.com/docs/en/cognos-tm1/10.2.2?topic=ictocyoiatwas-add-certificates-jre-keystore)
- [Keytool error: java.lang.Exception: Certificate not imported, alias <mykey> already exists](https://www.ibm.com/support/pages/keytool-error-javalangexception-certificate-not-imported-alias-already-exists)



