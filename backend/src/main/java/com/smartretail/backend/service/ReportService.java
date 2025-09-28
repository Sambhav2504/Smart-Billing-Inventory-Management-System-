package com.smartretail.backend.service;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.models.Product;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface ReportService {
    Map<String, Object> getSalesReport(Date startDate, Date endDate, Locale locale);
    Map<String, Object> getInventoryReport(int lowStockThreshold, int expiryDays, Locale locale);
}