package com.smartretail.backend.controller;

import com.smartretail.backend.models.Notification;
import com.smartretail.backend.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<Notification> createNotification(@Valid @RequestBody Notification notification) {
        Notification savedNotification = notificationService.createNotification(notification);
        return ResponseEntity.status(201).body(savedNotification);
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        List<Notification> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }
}