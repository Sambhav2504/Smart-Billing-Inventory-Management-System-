package com.smartretail.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AnalyticsService {
    private final RestTemplate restTemplate;
    @Value("${flask.base.url}")
    private String flaskBaseUrl;

    public AnalyticsService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public Object getDailySales() {
        String url = flaskBaseUrl + "/analytics/daily";
        return restTemplate.getForObject(url, Object.class);
    }

    public Object getMonthlySales() {
        String url = flaskBaseUrl + "/analytics/monthly";
        return restTemplate.getForObject(url, Object.class);
    }

    public Object getReport() {
        String url = flaskBaseUrl + "/analytics/report";
        return restTemplate.getForObject(url, Object.class);
    }
}
