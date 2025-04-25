package com.webhook.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WebhookService {
    
    @Value("${app.webhook.base-url}")
    private String baseUrl;
    
    private final RestTemplate restTemplate;
    private final Gson gson;
    private final MutualFollowersService mutualFollowersService;
    private final NthLevelFollowersService nthLevelFollowersService;

    @PostConstruct
    public void processWebhook() {
        try {
            // Step 1: Generate webhook
            Map<String, String> request = Map.of(
                "name", "John Doe",
                "regNo", "REG12347",
                "email", "john@example.com"
            );

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            String url = baseUrl + "/generateWebhook";
            System.out.println("Making request to: " + url);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("Failed to generate webhook. Status: " + response.getStatusCode());
            }
            
            System.out.println("Received response: " + response.getBody());
            
            // Parse response using JsonParser
            JsonObject jsonResponse = JsonParser.parseString(response.getBody()).getAsJsonObject();
            String webhook = jsonResponse.get("webhook").getAsString();
            String accessToken = jsonResponse.get("accessToken").getAsString();
            JsonObject data = jsonResponse.getAsJsonObject("data");

            System.out.println("Webhook URL: " + webhook);
            System.out.println("Processing data...");

            // Step 2: Process data based on regNo
            String regNo = "REG12347";
            boolean isOddRegNo = Integer.parseInt(regNo.replaceAll("\\D", "")) % 2 == 1;
            
            String result;
            if (isOddRegNo) {
                System.out.println("Processing mutual followers (Question 1)");
                result = mutualFollowersService.findMutualFollowers(data);
            } else {
                System.out.println("Processing nth level followers (Question 2)");
                result = nthLevelFollowersService.findNthLevelFollowers(data);
            }

            System.out.println("Result: " + result);

            // Step 3: Send result to webhook with retry
            sendResultToWebhook(webhook, accessToken, result);
            
            System.out.println("Webhook processing completed successfully");
        } catch (Exception e) {
            System.err.println("Error in webhook processing: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Webhook processing failed", e);
        }
    }

    @Retryable(
        value = Exception.class,
        maxAttempts = 4,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    private void sendResultToWebhook(String webhook, String accessToken, String result) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);
        
        HttpEntity<String> entity = new HttpEntity<>(result, headers);
        
        System.out.println("Sending result to webhook: " + webhook);
        System.out.println("With headers: " + headers);
        System.out.println("And body: " + result);
        
        ResponseEntity<String> response = restTemplate.postForEntity(webhook, entity, String.class);
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to send result to webhook: " + response.getStatusCode());
        }
        
        System.out.println("Result sent successfully");
    }
} 