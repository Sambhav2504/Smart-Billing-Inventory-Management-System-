package com.smartretail.backend.service;

import com.smartretail.backend.models.Notification;

import java.util.List;

public interface NotificationService {
    Notification createNotification(Notification notification);
    List<Notification> getAllNotifications();
    void sendEmail(String to, String subject, String text);
    void sendBillNotification(String customerEmail, String billId, double total);
    void sendLowStockNotification(String managerEmail, String productName, int quantity, int reorderLevel);
}