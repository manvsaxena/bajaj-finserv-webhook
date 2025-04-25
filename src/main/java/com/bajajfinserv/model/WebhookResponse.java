package com.bajajfinserv.model;

import lombok.Data;
import java.util.List;

@Data
public class WebhookResponse {
    private String webhook;
    private String accessToken;
    private WebhookData data;

    @Data
    public static class WebhookData {
        private List<User> users;
    }

    @Data
    public static class User {
        private int id;
        private String name;
        private List<Integer> follows;
    }
} 