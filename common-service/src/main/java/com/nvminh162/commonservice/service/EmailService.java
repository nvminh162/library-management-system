package com.nvminh162.commonservice.service;

import java.io.File;

import org.springframework.core.io.FileSystemResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {
    
    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(String to, String subject, String text, boolean isHtml, File attachment) {
        try{
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text,isHtml);

            // Add attachment if provided
            if(attachment != null){
                FileSystemResource fileSystemResource = new FileSystemResource(attachment);
                helper.addAttachment(fileSystemResource.getFilename(),fileSystemResource);
            }

            javaMailSender.send(message);
            log.info("Email sent successfully to {}",to);

        }catch (MessagingException e){
            log.error("Failed to send email to {}",to,e);
            // Handle the exception (retry logic, save to dlq...)
        }
    }
}
