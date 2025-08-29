package com.bajaj.webhook.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.bajaj.webhook.service.WebhookService;

@Component
public class StartupRunner implements ApplicationRunner {

    @Autowired
    private WebhookService webhookService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("Starting webhook process...");
        webhookService.processWebhookChallenge();
    }
}
