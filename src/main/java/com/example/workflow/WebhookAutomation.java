package com.example.workflow;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class WebhookAutomation implements ApplicationRunner {

    WebClient webClient = WebClient.create();

    @Override
    public void run(ApplicationArguments args) {
        // Step 1: Send the initial POST request
        WebhookResponse resp = webClient.post()
            .uri("https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
            {
              "name": "Vipul Sharma",
              "regNo": "2210992534",
              "email": "vipul2534.be22@chitkara.edu.in"
            }
            """)
            .retrieve()
            .bodyToMono(WebhookResponse.class)
            .block();

        String finalQuery = "SELECT e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, d.DEPARTMENT_NAME, COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT FROM EMPLOYEE e JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID LEFT JOIN EMPLOYEE e2 ON e2.DEPARTMENT = e.DEPARTMENT AND e2.DOB > e.DOB GROUP BY e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, d.DEPARTMENT_NAME ORDER BY e.EMP_ID DESC";

        // Step 2: Submit the SQL query
        webClient.post()
            .uri(resp.webhookUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", resp.accessToken)
            .bodyValue("{\"finalQuery\": \"" + finalQuery.replace("\"", "\\\"") + "\"}")
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }

    // Response DTO
    public static class WebhookResponse {
        @JsonProperty("webhookUrl")
        public String webhookUrl;
        @JsonProperty("accessToken")
        public String accessToken;
    }
}