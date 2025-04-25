package com.bajajfinserv.service;

import com.bajajfinserv.model.WebhookRequest;
import com.bajajfinserv.model.WebhookResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class WebhookService {

    private static final String GENERATE_WEBHOOK_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook";
    private static final int MAX_RETRIES = 4;

    @Autowired
    private RestTemplate restTemplate;

    public void processWebhook() {
        // Create initial request
        WebhookRequest request = new WebhookRequest();
        request.setName("John Doe");
        request.setRegNo("REG12347");
        request.setEmail("john@example.com");

        // Get webhook response
        WebhookResponse response = getWebhookResponse(request);
        if (response == null) {
            return;
        }

        // Process the data based on registration number
        List<List<Integer>> result = processData(response);
        if (result == null) {
            return;
        }

        // Send result to webhook
        sendResultToWebhook(response.getWebhook(), response.getAccessToken(), result);
    }

    private WebhookResponse getWebhookResponse(WebhookRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<WebhookRequest> entity = new HttpEntity<>(request, headers);
            return restTemplate.postForObject(GENERATE_WEBHOOK_URL, entity, WebhookResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<List<Integer>> processData(WebhookResponse response) {
        if (response == null || response.getData() == null || response.getData().getUsers() == null) {
            return null;
        }

        // Check if registration number is odd or even
        String regNo = "REG12347";
        int lastDigit = Character.getNumericValue(regNo.charAt(regNo.length() - 1));
        
        if (lastDigit % 2 == 1) {
            return findMutualFollowers(response.getData().getUsers());
        } else {
            return findNthLevelFollowers(response.getData().getUsers(), 2, 1);
        }
    }

    private List<List<Integer>> findMutualFollowers(List<WebhookResponse.User> users) {
        List<List<Integer>> result = new ArrayList<>();
        Map<Integer, Set<Integer>> followsMap = new HashMap<>();

        // Build follows map
        for (WebhookResponse.User user : users) {
            followsMap.put(user.getId(), new HashSet<>(user.getFollows()));
        }

        // Find mutual followers
        for (WebhookResponse.User user : users) {
            for (Integer followsId : user.getFollows()) {
                if (followsMap.containsKey(followsId) && 
                    followsMap.get(followsId).contains(user.getId())) {
                    List<Integer> pair = Arrays.asList(
                        Math.min(user.getId(), followsId),
                        Math.max(user.getId(), followsId)
                    );
                    if (!result.contains(pair)) {
                        result.add(pair);
                    }
                }
            }
        }

        return result;
    }

    private List<List<Integer>> findNthLevelFollowers(List<WebhookResponse.User> users, int n, int findId) {
        Set<Integer> result = new HashSet<>();
        Map<Integer, Set<Integer>> followsMap = new HashMap<>();

        // Build follows map
        for (WebhookResponse.User user : users) {
            followsMap.put(user.getId(), new HashSet<>(user.getFollows()));
        }

        // Find nth level followers
        Set<Integer> currentLevel = new HashSet<>();
        currentLevel.add(findId);

        for (int level = 1; level <= n; level++) {
            Set<Integer> nextLevel = new HashSet<>();
            for (Integer userId : currentLevel) {
                if (followsMap.containsKey(userId)) {
                    nextLevel.addAll(followsMap.get(userId));
                }
            }
            if (level == n) {
                result.addAll(nextLevel);
            }
            currentLevel = nextLevel;
        }

        return Collections.singletonList(new ArrayList<>(result));
    }

    private void sendResultToWebhook(String webhookUrl, String accessToken, List<List<Integer>> result) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("outcome", result);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
                );
                if (response.getStatusCode() == HttpStatus.OK) {
                    return;
                }
            } catch (Exception e) {
                if (attempt == MAX_RETRIES - 1) {
                    e.printStackTrace();
                }
            }
        }
    }
} 