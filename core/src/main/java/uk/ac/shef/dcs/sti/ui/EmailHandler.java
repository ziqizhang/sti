package uk.ac.shef.dcs.sti.ui;


import org.apache.commons.io.FileUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Created by - on 15/07/2016.
 */
public class EmailHandler {

    public static void sendCompletionEmail(String propFile, String toEmail, String message) throws IOException, EmailException {
        String myJSONString = FileUtils.readFileToString(new File(propFile));
        JSONObject object = new JSONObject(myJSONString);
        String[] keys = JSONObject.getNames(object);

        String login = null, pass = null, host = null, port = null, from = null;
        for (String key : keys) {
            String val = object.get(key).toString();
            switch (key) {
                case "emaillogin":
                    login = val;
                    break;
                case "emailpass":
                    pass = val;
                    break;
                case "emailhost":
                    host = val;
                    break;
                case "emailport":
                    port = val;
                    break;
                case "emailfrom":
                    from = val;
                    break;
            }
        }

        Email email = new SimpleEmail();
        email.setHostName(host);
        email.setSmtpPort(Integer.valueOf(port));
        email.setAuthenticator(new DefaultAuthenticator(login, pass));
        email.setSSLOnConnect(true);
        email.setFrom(from);
        email.setSubject("TableMiner+ Task");
        email.setMsg(message);
        email.addTo(toEmail);
        email.send();

    }

}
