package com.smartretail.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AnalyticsService {
    private final RestTemplate restTemplate;

    @Value("${flask.base.url:http://localhost:5001}")
    private String flaskBaseUrl;

    public AnalyticsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Object getDailySales() {
        String url = flaskBaseUrl + "/analytics/daily";
        return restTemplate.getForObject(url, Object.class);
    }

    public Object getMonthlySales() {
        String url = flaskBaseUrl + "/analytics/monthly";
        return restTemplate.getForObject(url, Object.class);
    }

    public Object getTopProducts() {
        String url = flaskBaseUrl + "/analytics/top-products";
        return restTemplate.getForObject(url, Object.class);
    }

    public Object getRevenueTrend() {
        String url = flaskBaseUrl + "/analytics/revenue-trend";
        return restTemplate.getForObject(url, Object.class);
    }

    public Object getReport() {
        String url = flaskBaseUrl + "/analytics/report";
        return restTemplate.getForObject(url, Object.class);
    }

    public Object getTextReport() {
        String url = flaskBaseUrl + "/analytics/report/text";
        return restTemplate.getForObject(url, Object.class);
    }
}