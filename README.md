# Bajaj Finserv Webhook Service

A Spring Boot application that automatically interacts with a remote API at application startup, without any manual HTTP trigger for BAJAJ FINSERV 

## Features

- Automatically calls the `/generateWebhook` endpoint on startup
- Processes user data to find either:
  - Mutual followers (for odd registration numbers)
  - Nth-level followers (for even registration numbers)
- Sends results to the provided webhook with JWT authentication
- Implements retry policy (up to 4 attempts)

## Technical Requirements

- Java 11
- Spring Boot 2.7.0
- Maven

## Building the Project

```bash
mvn clean install
```

## Running the Application

```bash
mvn spring-boot:run
```

## Project Structure

- `WebhookServiceApplication.java` - Main application class
- `WebhookRequest.java` - Model for initial webhook request
- `WebhookResponse.java` - Model for webhook response
- `WebhookService.java` - Core service implementation
- `WebhookInitializer.java` - Startup trigger component

## Download

You can download the latest JAR file from the [releases](https://github.com/manvsaxena/bajaj-finserv-webhook/releases) page.

Direct download link: [webhook-service-0.0.1-SNAPSHOT.jar](https://github.com/manvsaxena/bajaj-finserv-webhook/releases/download/v1.0.0/webhook-service-0.0.1-SNAPSHOT.jar) # bajaj-finserv-webhook
