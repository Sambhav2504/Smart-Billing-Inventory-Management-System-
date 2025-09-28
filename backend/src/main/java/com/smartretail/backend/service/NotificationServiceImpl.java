package com.smartretail.backend.service;

import com.smartretail.backend.models.Notification;
import com.smartretail.backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final MessageSource messageSource;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   JavaMailSender mailSender,
                                   MessageSource messageSource) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
        this.messageSource = messageSource;
    }

    @Override
    public Notification createNotification(Notification notification) {
        System.out.println("[SERVICE] Creating notification for: " + notification.getTo());
        notification.setSentAt(new Date());

        // Save notification first
        Notification savedNotification = notificationRepository.save(notification);

        // Send email without saving again
        sendEmailDirect(notification.getTo(), notification.getSubject(), notification.getMessage());

        return savedNotification;
    }

    @Override
    public List<Notification> getAllNotifications() {
        System.out.println("[SERVICE] Fetching all notifications.");
        return notificationRepository.findAll();
    }

    @Override
    public void sendEmail(String to, String subject, String text) {
        // Create and save notification first
        Notification notification = new Notification(to, subject, text, new Date());
        notificationRepository.save(notification);

        // Then send email
        sendEmailDirect(to, subject, text);
    }

    // Private method to avoid duplicate saving
    private void sendEmailDirect(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false); // false = plain text
            mailSender.send(message);
            System.out.println("[NOTIFICATION] Email sent to: " + to);
        } catch (MessagingException e) {
            System.err.println("[NOTIFICATION] Failed to send email to " + to + ": " + e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendBillNotification(String customerEmail, String billId, double total, byte[] pdfContent, Locale locale) {
        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            System.err.println("[NOTIFICATION] Customer email is null or empty for bill: " + billId);
            return;
        }

        try {
            // Use default message if property not found
            String subject;
            String text;

            try {
                subject = messageSource.getMessage("bill.notification.subject", new Object[]{billId}, locale);
                text = messageSource.getMessage("bill.email.body", new Object[]{billId, String.format("%.2f", total)}, locale);
            } catch (Exception e) {
                // Fallback messages if properties are missing
                subject = "Your Bill Receipt - " + billId;
                text = String.format(
                        "Dear Customer,\n\nThank you for your purchase!\n\nBill ID: %s\nTotal Amount: ₹%.2f\n\nPlease find your bill attached.\n\nThank you for shopping with us!\n\nSmartRetail Team",
                        billId, total
                );
                System.out.println("[NOTIFICATION] Using fallback email message for bill: " + billId);
            }

            System.out.println("[NOTIFICATION] Attempting to send email to: " + customerEmail);
            System.out.println("[NOTIFICATION] Subject: " + subject);

            // Create and save notification first
            Notification notification = new Notification(customerEmail, subject, text, new Date());
            notificationRepository.save(notification);

            // Send email with attachment
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(customerEmail);
            helper.setSubject(subject);
            helper.setText(text, false);

            if (pdfContent != null && pdfContent.length > 0) {
                helper.addAttachment("Bill_" + billId + ".pdf", new ByteArrayResource(pdfContent));
                System.out.println("[NOTIFICATION] PDF attachment added, size: " + pdfContent.length + " bytes");
            }

            mailSender.send(message);
            System.out.println("[NOTIFICATION] Bill notification sent successfully to: " + customerEmail + " for bill: " + billId);

        } catch (MessagingException e) {
            System.err.println("[NOTIFICATION] Failed to send bill notification to " + customerEmail + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send bill notification", e);
        }
    }

    @Override
    public void sendLowStockNotification(String managerEmail, String productName, int quantity, int reorderLevel) {
        String emailSubject = "SmartRetail: Low Stock Alert for " + productName;
        String emailText = String.format(
                "Alert: Product '%s' is low on stock.\nCurrent Quantity: %d\nReorder Level: %d\nPlease restock soon.",
                productName, quantity, reorderLevel
        );
        sendEmail(managerEmail, emailSubject, emailText);
    }
}