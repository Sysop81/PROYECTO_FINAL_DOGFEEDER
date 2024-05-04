package org.dogfeeder.mail;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Email {
    private Session sesion;
    private String userAccount;
    private String password;

    public Email(){
        userAccount = System.getenv("MAIL_USER_ACCOUNT");
        password = System.getenv("MAIL_USER_PASSWORD");

        System.out.println("Cuenta configurada para DOGFEEDER: " + userAccount);

        // Step 2. Instanciamos la propiedad sesión mediante el objeto properties y realizando el login con el usuario
        //         y password obtenido de nuestro fichero properties.
        sesion = Session.getDefaultInstance(getMailProperties(),
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(userAccount, password);
                    }
                });

    }

    private Properties getMailProperties(){
        // Step 1. Instanciamos objeto properties
        Properties propiedades = new Properties();

        // Step 2. Añadimos al objeto las propiedades necesarias para el envio de correos electronicos.
        propiedades.put("mail.smtp.host", "smtp.gmail.com");
        propiedades.put("mail.smtp.auth", "true");
        propiedades.put("mail.smtp.socketFactory.port", "465");
        propiedades.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        propiedades.put("mail.smtp.port", "465");

        // Step 4. Retornamos el objeto properties.
        return propiedades;
    }

    public boolean sendMailTo(String recipientAccount, String msgSubject, String msgText){
        boolean isOk = false;
        try{

            // Step 2. Instanciamos y damos forma al mensaje con las propiedades de la sesion y del usuario al que va
            //         destinado.
            Message message = new MimeMessage(sesion);
            message.setFrom(new InternetAddress(this.userAccount));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientAccount));
            message.setSubject(msgSubject);

            // Step 3. Se realiza el envío del mensaje y se establece la varaible de retorno a verdadero.
            message.setText(msgText);
            Transport.send(message);
            isOk = true;
        }catch (MessagingException e){
            System.err.println("Ha ocurrido un error al enviar el correo a " + recipientAccount + "\n." + e.getMessage());
        }

        return isOk;
    }
}
