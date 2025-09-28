package com.smartretail.backend.service;

import com.smartretail.backend.models.Bill;
import com.smartretail.backend.models.Product;
import com.smartretail.backend.repository.BillRepository;
import com.smartretail.backend.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
    private final BillRepository billRepository;
    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;
    private final MessageSource messageSource;

    public ReportServiceImpl(BillRepository billRepository, ProductRepository productRepository,
                             MongoTemplate mongoTemplate, MessageSource messageSource) {
        this.billRepository = billRepository;
        this.productRepository = productRepository;
        this.mongoTemplate = mongoTemplate;
        this.messageSource = messageSource;
    }

    @Override
    public Map<String, Object> getSalesReport(Date startDate, Date endDate, Locale locale) {
        logger.debug("[SERVICE] Generating sales report for range: {} to {}", startDate, endDate);
        if (startDate.after(endDate)) {
            logger.error("[SERVICE] Invalid date range: startDate={} is after endDate={}", startDate, endDate);
            throw new IllegalArgumentException(
                    messageSource.getMessage("report.date.range.invalid", new Object[]{startDate, endDate}, locale));
        }

        // Simplified aggregation without complex SpEL expressions
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdAt").gte(startDate).lte(endDate)),
                Aggregation.group()
                        .count().as("billCount")
                        .sum("totalAmount").as("totalSales")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "bills", Map.class);
        Map<String, Object> report = results.getUniqueMappedResult();

        Map<String, Object> result = new HashMap<>();
        if (report == null || report.isEmpty()) {
            logger.info("[SERVICE] No bills found for range: {} to {}", startDate, endDate);
            result.put("totalSales", 0.0);
            result.put("billCount", 0);
            result.put("averageBillAmount", 0.0);
            result.put("bills", Collections.emptyList());
        } else {
            // Get bills with null safety for createdAt field
            List<Bill> bills = billRepository.findAll().stream()
                    .filter(bill -> bill.getCreatedAt() != null) // Filter out bills with null createdAt
                    .filter(bill -> !bill.getCreatedAt().before(startDate) && !bill.getCreatedAt().after(endDate))
                    .limit(100)
                    .collect(Collectors.toList());

            // Extract values from aggregation result with proper type casting
            int billCount = 0;
            double totalSales = 0.0;
            double averageBillAmount = 0.0;

            // Handle different possible number types from MongoDB
            Object billCountObj = report.get("billCount");
            Object totalSalesObj = report.get("totalSales");

            if (billCountObj instanceof Integer) {
                billCount = (Integer) billCountObj;
            } else if (billCountObj instanceof Long) {
                billCount = ((Long) billCountObj).intValue();
            } else if (billCountObj instanceof Double) {
                billCount = ((Double) billCountObj).intValue();
            }

            if (totalSalesObj instanceof Double) {
                totalSales = (Double) totalSalesObj;
            } else if (totalSalesObj instanceof Integer) {
                totalSales = ((Integer) totalSalesObj).doubleValue();
            } else if (totalSalesObj instanceof Long) {
                totalSales = ((Long) totalSalesObj).doubleValue();
            }

            // Calculate average in Java
            averageBillAmount = billCount > 0 ? totalSales / billCount : 0.0;

            result.put("totalSales", totalSales);
            result.put("billCount", billCount);
            result.put("averageBillAmount", Math.round(averageBillAmount * 100.0) / 100.0); // Round to 2 decimal places
            result.put("bills", bills);
        }

        logger.info("[SERVICE] Sales report generated: totalSales={}, billCount={}, averageBillAmount={}",
                result.get("totalSales"), result.get("billCount"), result.get("averageBillAmount"));
        return result;
    }

    @Override
    public Map<String, Object> getInventoryReport(int lowStockThreshold, int expiryDays, Locale locale) {
        logger.debug("[SERVICE] Generating inventory report: lowStockThreshold={}, expiryDays={}", lowStockThreshold, expiryDays);
        if (lowStockThreshold < 0) {
            logger.error("[SERVICE] Invalid low stock threshold: {}", lowStockThreshold);
            throw new IllegalArgumentException(
                    messageSource.getMessage("report.lowStockThreshold.invalid", null, locale));
        }
        if (expiryDays < 0) {
            logger.error("[SERVICE] Invalid expiry days: {}", expiryDays);
            throw new IllegalArgumentException(
                    messageSource.getMessage("report.expiryDays.invalid", null, locale));
        }

        // Calculate expiry date
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.DAY_OF_MONTH, expiryDays);
        Date expiryDate = calendar.getTime();

        // Fetch all products
        List<Product> allProducts = productRepository.findAll();
        if (allProducts.isEmpty()) {
            logger.warn("[SERVICE] No products found");
            throw new IllegalArgumentException(
                    messageSource.getMessage("report.no.products.found", null, locale));
        }

        // Filter low-stock and expiring products
        List<Product> lowStockProducts = allProducts.stream()
                .filter(p -> p.getQuantity() <= lowStockThreshold)
                .collect(Collectors.toList());
        List<Product> expiringProducts = allProducts.stream()
                .filter(p -> p.getExpiryDate() != null && !p.getExpiryDate().after(expiryDate))
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalProducts", allProducts.size());
        result.put("lowStockProducts", lowStockProducts);
        result.put("expiringProducts", expiringProducts);
        result.put("lowStockCount", lowStockProducts.size());
        result.put("expiringCount", expiringProducts.size());

        logger.info("[SERVICE] Inventory report generated: totalProducts={}, lowStockCount={}, expiringCount={}",
                allProducts.size(), lowStockProducts.size(), expiringProducts.size());
        return result;
    }
}