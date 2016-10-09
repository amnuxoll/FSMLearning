import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;


public class SendAttachmentInEmail extends Authenticator {

    public void sendEmail(String emailAdress, String Password, Address[] addresses, double gWeight, int MinTries) {
        //
        // Defines the E-Mail information.
        //
    	
    	
        final String from = emailAdress;
        final Address[] to = addresses;
        String subject = "Agent Complete with gWeight and minTries: " + gWeight + " " + MinTries;
        String bodyText = "Here lies the results to our AI: AIReport.csv";
        
        final String password = Password;
        
        int index = emailAdress.lastIndexOf("@");
        final String username = emailAdress.substring(index+1);

        //
        // The attachment file name.
        //
        String attachmentName = ("P:/git/FSMLearning/FSML1/AIReport.csv");
       

        //
        // Creates a Session with the following properties.
        //
        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true"); 
        props.put("mail.smtp.host", "smtp.office365.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");  
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                   protected PasswordAuthentication getPasswordAuthentication() {
                      return new PasswordAuthentication(from, password);
                   }
                });

        try {
            InternetAddress fromAddress = new InternetAddress(from);
            //InternetAddress toAddress = new InternetAddress(to);

            //
            // Create an Internet mail message.
            //
            MimeMessage message = new MimeMessage(session);
            message.setFrom(fromAddress);
            //message.setRecipient(Message.RecipientType.TO, toAddress);
            message.setSubject(subject);
            message.setSentDate(new Date());

            //
            // Set the email message text.
            //
            MimeBodyPart messagePart = new MimeBodyPart();
            messagePart.setText(bodyText);

            //
            // Set the email attachment file
            //
            FileDataSource fileDataSource = new FileDataSource(attachmentName);

            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setDataHandler(new DataHandler(fileDataSource));
            attachmentPart.setFileName(fileDataSource.getName());

            //
            // Create Multipart E-Mail.
            //
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messagePart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            //
            // Send the message. Don't forget to set the username and password to authenticate to the
            // mail server.
            //
            
            System.out.println("Sending email ...");
            Transport.send(message, addresses);
        } catch (MessagingException e) {
        	System.out.println("No email sent ...");
        	e.printStackTrace();
            return;
        }
    }
}
