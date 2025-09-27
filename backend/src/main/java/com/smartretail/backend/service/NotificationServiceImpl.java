package com.smartretail.backend.service;

import com.smartretail.backend.models.Notification;
import com.smartretail.backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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

    public NotificationServiceImpl(NotificationRepository notificationRepository, JavaMailSender mailSender, MessageSource messageSource) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
        this.messageSource = messageSource;
    }

    @Override
    public Notification createNotification(Notification notification) {
        System.out.println("[SERVICE] Creating notification for: " + notification.getTo());
        notification.setSentAt(new Date());

        // Save notification in DB
        Notification savedNotification = notificationRepository.save(notification);

        // Send email
        sendEmail(notification.getTo(), notification.getSubject(), notification.getMessage());

        return savedNotification;
    }

    @Override
    public List<Notification> getAllNotifications() {
        System.out.println("[SERVICE] Fetching all notifications.");
        return notificationRepository.findAll();
    }

    @Override
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);

            System.out.println("[NOTIFICATION] Email sent to: " + to);

            // Save notification to MongoDB
            Notification notification = new Notification(to, subject, text, new Date());
            notificationRepository.save(notification);

        } catch (Exception e) {
            System.err.println("[NOTIFICATION] Failed to send email to " + to + ": " + e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendBillNotification(String customerEmail, String billId, double total, String pdfAccessToken, Locale locale) {
        if (customerEmail != null && !customerEmail.isEmpty()) {
            String emailSubject = messageSource.getMessage("bill.notification.subject", new Object[]{billId}, locale);
            String pdfLink = String.format("http://localhost:8080/api/billing/%s/pdf?token=%s", billId, pdfAccessToken);
            String emailText = messageSource.getMessage("bill.created.notification", new Object[]{billId, String.format("%.2f", total), pdfLink}, locale);
            sendEmail(customerEmail, emailSubject, emailText);
        }
    }


    @Override
    public void sendLowStockNotification(String managerEmail, String productName, int quantity, int reorderLevel) {
        String emailSubject = "SmartRetail: Low Stock Alert for " + productName;
        String emailText = "Alert: Product '" + productName + "' is low on stock.\n" +
                "Current Quantity: " + quantity + "\n" +
                "Reorder Level: " + reorderLevel + "\n" +
                "Please restock soon.";
        sendEmail(managerEmail, emailSubject, emailText);
    }
}