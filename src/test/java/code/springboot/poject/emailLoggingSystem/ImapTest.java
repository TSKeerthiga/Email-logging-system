package code.springboot.poject.emailLoggingSystem;

import jakarta.mail.*;
import java.util.Properties;

public class ImapTest {
    public static void main(String[] args) {
        try {
            String host = "imap.gmail.com";
            String port = "993";
            String username = "keerthiga.thurvassurendran@gmail.com";
            String password = "esxm fsto zgvo jepn";

            Properties properties = new Properties();
            properties.put("mail.imap.host", host);
            properties.put("mail.imap.port", port);
            properties.put("mail.imap.starttls.enable", "true");
            properties.put("mail.imap.ssl.enable", "true");

            Session session = Session.getInstance(properties);
            Store store = session.getStore("imap");

            store.connect(host, username, password);
            System.out.println("Successfully connected to IMAP server!");

            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
