import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
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
    private static final String BASE_URL = "https://bfhldevapigw.healthrx.co.in/hiring";
    private static final String REG_NO = "REG12347";
    
    private final RestTemplate restTemplate;
    private final MutualFollowersService mutualFollowersService;
    private final NthLevelFollowersService nthLevelFollowersService;
    private final Gson gson = new Gson();

    @PostConstruct
    public void processWebhook() {
        try {
            // Step 1: Generate webhook
            WebhookResponse webhookResponse = generateWebhook();
            
            // Step 2: Process the data based on regNo
            String result = processData(webhookResponse.getData());
            
            // Step 3: Send result to webhook
            sendResult(webhookResponse.getWebhook(), webhookResponse.getAccessToken(), result);
        } catch (Exception e) {
            // Log error and potentially notify monitoring systems
            System.err.println("Error in webhook processing: " + e.getMessage());
            throw new RuntimeException("Webhook processing failed", e);
        }
    }

    private WebhookResponse generateWebhook() {
        String url = BASE_URL + "/generateWebhook";
        
        Map<String, String> request = Map.of(
            "name", "John Doe",
            "regNo", REG_NO,
            "email", "john@example.com"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to generate webhook");
        }
        
        return gson.fromJson(response.getBody(), WebhookResponse.class);
    }

    private String processData(Map<String, Object> data) {
        // Determine question based on regNo
        boolean isOddRegNo = Integer.parseInt(REG_NO.replaceAll("\\D", "")) % 2 == 1;
        
        if (isOddRegNo) {
            return mutualFollowersService.findMutualFollowers(data);
        } else {
            return nthLevelFollowersService.findNthLevelFollowers(data);
        }
    }

    @Retryable(
        value = Exception.class,
        maxAttempts = 4,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    private void sendResult(String webhook, String accessToken, String result) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);
        
        HttpEntity<String> entity = new HttpEntity<>(result, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(webhook, entity, String.class);
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to send result to webhook: " + response.getStatusCode());
        }
    }
} 