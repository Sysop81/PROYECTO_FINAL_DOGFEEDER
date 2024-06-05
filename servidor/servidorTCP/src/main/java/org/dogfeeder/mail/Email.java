package org.dogfeeder.mail;

import org.dogfeeder.cli.CLInterface;
import org.dogfeeder.model.Logger4j;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Clase Email
 * Cliente de correo de DOG-FEEDER, se encarga del envío de correos a los distintos usuarios de la aplicación cliente.
 */
public class Email {
    private Session sesion;
    private String userAccount;
    private String password;

    private static Logger4j logger = new Logger4j(Email.class);

    public Email(){
        userAccount = System.getenv("MAIL_USER_ACCOUNT");
        password = System.getenv("MAIL_USER_PASSWORD");

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
        Properties props = new Properties();

        // Step 2. Añadimos al objeto las propiedades necesarias para el envio de correos electronicos.
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.port", "465");

        // Step 4. Retornamos el objeto properties.
        return props;
    }

    public boolean sendMailTo(String recipientAccount, String msgSubject, String msgText, String attachedFile){
        boolean isOk = false;
        try{

            // Step 2. Instanciamos y damos forma al mensaje con las propiedades de la sesion y del usuario al que va
            //         destinado.
            Message message = new MimeMessage(sesion);
            message.setFrom(new InternetAddress(this.userAccount));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientAccount));
            message.setSubject(msgSubject);

            // Step 5. Se establece el continido del email
            if(attachedFile == null) {
                message.setText(msgText);
            }else{
                // Se crea el cuerpo y el fichero de informe asociado
                MimeBodyPart msg = new MimeBodyPart();
                msg.setText(msgText);
                MimeBodyPart file = new MimeBodyPart();
                file.attachFile(attachedFile);
                // Se establece el contenido del correo
                message.setContent(new MimeMultipart(msg,file));
            }

            // Step 4. Se realiza el envío del mensaje y se establece la varaible de retorno a verdadero.
            Transport.send(message);
            isOk = true;
        }catch (MessagingException e){
            var msg = "Ha ocurrido un error al enviar el correo a " + recipientAccount + "." + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        } catch (IOException e) {
            var msg = "Ha ocurrido un error E/S al enviar el correo a " + recipientAccount + "." + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return isOk;
    }


    /**
     * Método sendMailTo [Sobrecarga]
     * Se encarga de enviar un mismo correo a varios destinatarios. Normalmente, se emplea en las notificaciones
     * @param recipientAccounts
     * @param msgSubject
     * @param msgText
     * @return boolean Resultado de la operación
     */
    public boolean sendMailTo(ArrayList<String> recipientAccounts, String msgSubject, String msgText) {
        boolean isOk = false;
        try {
            // Step 2. Instanciamos y damos forma al mensaje con las propiedades de la sesion y del usuario al que va
            //         destinado.
            Message message = new MimeMessage(sesion);
            message.setFrom(new InternetAddress(this.userAccount));

            // Convertimos el listado de correos en formato cadenas de texto en un array de instancias InternetAddress
            InternetAddress[] recipientAddresses = recipientAccounts.stream()
                    .map(recipient -> {
                        try {
                            return new InternetAddress(recipient);
                        } catch (AddressException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(InternetAddress[]::new);
            message.setRecipients(Message.RecipientType.TO, recipientAddresses);
            message.setSubject(msgSubject);
            message.setText(msgText);

            // Step 4. Se realiza el envío del mensaje y se establece la variable de retorno a verdadero.
            Transport.send(message);
            isOk = true;
        } catch (MessagingException e) {
            var msg = "Ha ocurrido un error al enviar los correos." + e.getMessage();
            CLInterface.showAlertDanger(msg);
            logger.setError(msg);
        }

        return isOk;
    }
}
