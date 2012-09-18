/**
 *  Stoker-web
 *
 *  Copyright (C) 2011  Gary Bak
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package sweb.server.alerts.delivery;

import javax.mail.PasswordAuthentication;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import sweb.server.StokerWebProperties;
 
public class EmailDelivery {
     
    public void send(String recipeintEmail, 
            String subject, 
            String messageText, 
            String []attachments) 
            throws MessagingException, AddressException {
       
       // TODO: add to constants
        String senderEmail = StokerWebProperties.getInstance().getProperty("mail.smtp.user");
        String senderMailPassword = StokerWebProperties.getInstance().getProperty("mail.password");
        String gmail = StokerWebProperties.getInstance().getProperty("mail.smtp.host");
        int port = new Integer(StokerWebProperties.getInstance().getProperty("mail.smtp.socketFactory.port")).intValue();
         
        //Properties props = System.getProperties();
        
 
       // props.put("mail.smtp.user", senderEmail);
       // props.put("mail.smtp.host", "smtp.gmail.com");
       // props.put("mail.smtp.port", "465");
       // props.put("mail.smtp.starttls.enable", "true");
       // props.put("mail.smtp.debug", "true");
       // props.put("mail.smtp.auth", "true");
       // props.put("mail.smtp.socketFactory.port", "465");
       // props.put("mail.smtp.socketFactory.class", 
       //       "javax.net.ssl.SSLSocketFactory");
       // props.put("mail.smtp.socketFactory.fallback", "false");
 
        // Required to avoid security exception.
        MyAuthenticator authentication = 
              new MyAuthenticator(senderEmail,senderMailPassword);
        Session session = 
              Session.getDefaultInstance(StokerWebProperties.getInstance(),authentication);
        session.setDebug(true);
 
        MimeMessage message = new MimeMessage(session);
         
        BodyPart messageBodyPart = new MimeBodyPart();      
        messageBodyPart.setText(messageText);
         
        // Add message text
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
         
        // Attachments should reside in your server.
        // Example "c:\file.txt" or "/home/user/photo.jpg"
 
        for (int i=0; i < attachments.length; i++) {        
            messageBodyPart = new MimeBodyPart();       
            DataSource source = new FileDataSource(attachments[i]);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(attachments [i]);          
            multipart.addBodyPart(messageBodyPart) ;  
        }
         
    
            
        message.setContent(multipart);                
        message.setSubject(subject);
        message.setFrom(new InternetAddress(senderEmail));
        message.addRecipient(Message.RecipientType.TO,
            new InternetAddress(recipeintEmail));
 
        Transport transport = session.getTransport("smtps");
        transport.connect(gmail,port, senderEmail, senderMailPassword);
        
        transport.sendMessage(message, message.getAllRecipients());
         
        transport.close();
         
    }
     
    private class MyAuthenticator extends javax.mail.Authenticator {
        String User;
        String Password;
        public MyAuthenticator (String user, String password) {
            User = user;
            Password = password;
        }
         
        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return new javax.mail.PasswordAuthentication(User, Password);
        }
    }
     
}
