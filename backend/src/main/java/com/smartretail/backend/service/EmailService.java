integrate..instead of gmail using resend
package com.smartretail.backend.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.resend.com/emails";

            JSONObject payload = new JSONObject();
            payload.put("from", fromEmail);
            payload.put("to", to);
            payload.put("subject", subject);
            payload.put("html", body);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> request = new HttpEntity<>(payload.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("[EMAIL] Sent to " + to);
            } else {
                System.err.println("[EMAIL] Failed: " + response.getStatusCode() + " - " + response.getBody());
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[EMAIL] Exception: " + e.getMessage());
        }
    }
}
