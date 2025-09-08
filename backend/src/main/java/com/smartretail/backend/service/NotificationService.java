package com.smartretail.backend.service;

import com.smartretail.backend.models.Notification;
import java.util.List;

public interface NotificationService {
    Notification createNotification(Notification notification);
    List<Notification> getAllNotifications();
}