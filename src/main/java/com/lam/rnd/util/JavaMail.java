package com.lam.rnd.util;

import java.io.File;
import java.util.Properties;


import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaMail
{
    private static final Logger logger = LoggerFactory.getLogger(JavaMail.class);

    private static String email = "";

    private static String key = "";

    static {
        email = JsonParsing.parsingJson(JsonParsing.readFile("src/main/resources/email.json"), "email");
        key = JsonParsing.parsingJson(JsonParsing.readFile("src/main/resources/email.json"), "key");
    }

    public static boolean sendMailWithFile(String email, String title, String content, File file)
    {
        try
        {
            MimeMessage message = getMimeMessage();
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject(title);


            MimeMultipart mimeMultipart = new MimeMultipart();

            MimeBodyPart contentBodyPart = new MimeBodyPart();
            contentBodyPart.setContent(content, "text/html;charset=UTF-8");
            mimeMultipart.addBodyPart(contentBodyPart);

            MimeBodyPart fileBodyPart = new MimeBodyPart();
            fileBodyPart.attachFile(file);
            mimeMultipart.addBodyPart(fileBodyPart);

            message.setContent(mimeMultipart);

            Transport.send(message);

            return true;
        }
        catch (Exception e)
        {
            logger.debug("===============================================================");
            logger.debug("MailSend Fail : " + e);
            logger.debug("===============================================================");
            return false;
        }
    }


    /**
     * 메일 발송
     * 파라미터 : 이메일주소, 제목, 내용(html 태그)
     * 리턴타입 : boolean
     * **/
    public static boolean sendMail(String email, String title, String content) // mail은 메일 받을 주소
    {
        try
        {
            Message message = getMessage();
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject(title);
            message.setContent(content, "text/html; charset=utf-8");

            Transport.send(message);
        }
        catch (Exception e)
        {
            logger.debug("===============================================================");
            logger.debug("MailSend Fail : " + e);
            logger.debug("===============================================================");
            return false;
        }
        logger.debug("===============================================================");
        logger.debug("MailSend Success");
        logger.debug("===============================================================");
        return true;
    }

    /**
     * 메소드명 :  getMessage
     * 설명 : Message리턴
     **/
    private static Message getMessage() throws Exception
    {
        Message message = new MimeMessage(getSession());
        message.setFrom(new InternetAddress("dbrudals85@gmail.com", "LookAtMe", "utf-8"));
        return message;
    }


    private static MimeMessage getMimeMessage() throws Exception
    {
        MimeMessage message = new MimeMessage(getSession());
        message.setFrom(new InternetAddress("dbrudals85@gmail.com", "LookAtMe", "utf-8"));
        return message;
    }

    private static Session getSession()
    {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        return Session.getInstance(props, new Authenticator()
        {
            @Override
            protected PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(email, key);
            }
        });
    }


}