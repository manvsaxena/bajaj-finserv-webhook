package com.bajajfinserv.component;

import com.bajajfinserv.service.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class WebhookInitializer implements CommandLineRunner {

    @Autowired
    private WebhookService webhookService;

    @Override
    public void run(String... args) {
        webhookService.processWebhook();
    }
} 