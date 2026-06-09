package com.nvminh162.notification.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final Configuration freeMarkerConfiguration;

    public void sendEmail(String to, String subject, String text, boolean html, File attachment) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, html);
            addAttachment(helper, attachment);
            javaMailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (MessagingException exception) {
            log.error("Failed to send email to {}", to, exception);
        }
    }

    public void sendEmailWithTemplate(
            String to,
            String subject,
            String templateName,
            Map<String, Object> placeholders,
            File attachment) {
        try {
            Template template = freeMarkerConfiguration.getTemplate(templateName);
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, placeholders);
            sendEmail(to, subject, html, true, attachment);
        } catch (IOException | TemplateException exception) {
            log.error("Failed to render email template {} for {}", templateName, to, exception);
        }
    }

    private void addAttachment(MimeMessageHelper helper, File attachment) throws MessagingException {
        if (attachment == null) {
            return;
        }

        FileSystemResource resource = new FileSystemResource(attachment);
        helper.addAttachment(resource.getFilename(), resource);
    }
}
