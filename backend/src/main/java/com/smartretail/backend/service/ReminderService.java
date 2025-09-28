package com.smartretail.backend.service;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.models.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReminderService {
    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);
    private final CustomerService customerService;
    private final NotificationService notificationService;
    private final MessageSource messageSource;

    public ReminderService(CustomerService customerService, NotificationService notificationService, MessageSource messageSource) {
        this.customerService = customerService;
        this.notificationService = notificationService;
        this.messageSource = messageSource;
    }

    @Scheduled(cron = "0 0 9 1 * ?") // Run at 9:00 AM on the 1st of every month
    public void sendMonthlyPurchaseReminders() {
        logger.info("[REMINDER SERVICE] Starting monthly purchase reminders");

        List<Customer> customers = customerService.getAllCustomers();
        for (Customer customer : customers) {
            if (customer.getEmail() == null || customer.getEmail().isEmpty()) {
                logger.debug("[REMINDER SERVICE] {}",
                        messageSource.getMessage("reminder.no.email", new Object[]{customer.getMobile()}, Locale.ENGLISH));
                continue;
            }

            List<Bill> purchaseHistory = customerService.getCustomerPurchaseHistory(
                    customer.getId(), null, null, Locale.ENGLISH);
            if (purchaseHistory.isEmpty()) {
                logger.debug("[REMINDER SERVICE] {}",
                        messageSource.getMessage("reminder.no.purchase.history", new Object[]{customer.getMobile()}, Locale.ENGLISH));
                continue;
            }

            // Identify frequently purchased items (purchased at least twice)
            Map<String, Long> productFrequency = purchaseHistory.stream()
                    .flatMap(bill -> bill.getItems().stream())
                    .collect(Collectors.groupingBy(
                            Bill.BillItem::getProductName,
                            Collectors.counting()
                    ));

            List<String> frequentItems = productFrequency.entrySet().stream()
                    .filter(entry -> entry.getValue() >= 2)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (frequentItems.isEmpty()) {
                logger.debug("[REMINDER SERVICE] {}",
                        messageSource.getMessage("reminder.no.frequent.items", new Object[]{customer.getMobile()}, Locale.ENGLISH));
                continue;
            }

            // Send reminder email
            String subject = messageSource.getMessage("reminder.subject", null, Locale.ENGLISH);
            String body = messageSource.getMessage(
                    "reminder.body",
                    new Object[]{customer.getName(), String.join("\n", frequentItems)},
                    Locale.ENGLISH
            );
            notificationService.sendEmail(customer.getEmail(), subject, body);
            logger.info("[REMINDER SERVICE] Sent reminder to customer {} for items: {}", customer.getMobile(), frequentItems);
        }

        logger.info("[REMINDER SERVICE] Completed monthly purchase reminders");
    }
}