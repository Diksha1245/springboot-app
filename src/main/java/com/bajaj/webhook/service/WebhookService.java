package com.bajaj.webhook.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bajaj.webhook.model.SolutionRequest;
import com.bajaj.webhook.model.WebhookRequest;
import com.bajaj.webhook.model.WebhookResponse;

@Service
public class WebhookService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String WEBHOOK_GENERATION_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
    private static final String SOLUTION_SUBMISSION_URL = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

    public void processWebhookChallenge() {
        try {
            // Generate webhook first
            WebhookResponse response = generateWebhook();
            
            if (response != null) {
                System.out.println("Got webhook: " + response.getWebhook());
                System.out.println("Token: " + response.getAccessToken());
                
                // Solve based on registration number
                String regNo = "REG12347";
                String query = solveSQLProblem(regNo);
                
                // Submit the answer
                submitSolution(query, response.getAccessToken());
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private WebhookResponse generateWebhook() {
        try {
            WebhookRequest request = new WebhookRequest("John Doe", "REG12347", "john@example.com");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<WebhookRequest> entity = new HttpEntity<>(request, headers);
            
            return restTemplate.postForObject(WEBHOOK_GENERATION_URL, entity, WebhookResponse.class);
        } catch (Exception e) {
            System.err.println("Failed to generate webhook: " + e.getMessage());
            return null;
        }
    }

    private String solveSQLProblem(String regNo) {
        // Check if reg number ends in odd or even digit
        char lastChar = regNo.charAt(regNo.length() - 1);
        int lastDigit = Character.getNumericValue(lastChar);
        
        String sql;
        
        if (lastDigit % 2 == 1) {
            // Odd - find highest salary not on 1st day
            sql = """
                SELECT 
                    p.AMOUNT as SALARY,
                    CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) as NAME,
                    TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) as AGE,
                    d.DEPARTMENT_NAME
                FROM PAYMENTS p
                JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID
                JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID
                WHERE DAY(p.PAYMENT_TIME) != 1
                ORDER BY p.AMOUNT DESC
                LIMIT 1
                """;
            System.out.println("Solving odd question (highest salary)");
        } else {
            // Even - count younger employees by department
            sql = """
                SELECT 
                    e1.EMP_ID,
                    e1.FIRST_NAME,
                    e1.LAST_NAME,
                    d.DEPARTMENT_NAME,
                    COUNT(e2.EMP_ID) as YOUNGER_EMPLOYEES_COUNT
                FROM EMPLOYEE e1
                JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID
                LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT 
                    AND e2.DOB > e1.DOB
                GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME
                ORDER BY e1.EMP_ID DESC
                """;
            System.out.println("Solving even question (younger employees)");
        }
        
        return sql;
    }

    private void submitSolution(String query, String token) {
        try {
            SolutionRequest request = new SolutionRequest(query);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", token);
            
            HttpEntity<SolutionRequest> entity = new HttpEntity<>(request, headers);
            
            String result = restTemplate.postForObject(SOLUTION_SUBMISSION_URL, entity, String.class);
            System.out.println("Submitted! Response: " + result);
        } catch (Exception e) {
            System.err.println("Submit failed: " + e.getMessage());
        }
    }
}
