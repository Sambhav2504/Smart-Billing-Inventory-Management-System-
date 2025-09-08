package com.smartretail.backend.service;

import com.smartretail.backend.models.Notification;
import com.smartretail.backend.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification createNotification(Notification notification) {
        System.out.println("[SERVICE] Creating notification for: " + notification.getTo());
        notification.setSentAt(new Date());
        Notification savedNotification = notificationRepository.save(notification);
        System.out.println("[SERVICE] Notification created successfully: " + savedNotification.getId());
        return savedNotification;
    }

    @Override
    public List<Notification> getAllNotifications() {
        System.out.println("[SERVICE] Fetching all notifications.");
        return notificationRepository.findAll();
    }
}