package com.meetingos.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetingos.service.ExtractionException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "claude")
@Slf4j
public class ClaudeLlmClient implements LlmClient {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${claude.api-key}")
    private String apiKey;

    @Value("${claude.model}")
    private String model;

    @Value("${claude.base-url}")
    private String baseUrl;

    @Value("${claude.max-tokens}")
    private int maxTokens;

    public ClaudeLlmClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @CircuitBreaker(name = "llm", fallbackMethod = "fallback")
    public String call(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        try {
            String requestJson = objectMapper.writeValueAsString(body);
            HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/messages", HttpMethod.POST, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ExtractionException("Claude API returned status: " + response.getStatusCode());
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode content = root.path("content");
            if (content.isArray() && !content.isEmpty()) {
                return content.get(0).path("text").asText();
            }
            throw new ExtractionException("Unexpected Claude API response structure");

        } catch (RestClientException e) {
            log.error("Claude API call failed: {}", e.getMessage());
            throw new ExtractionException("Claude API unavailable: " + e.getMessage());
        } catch (ExtractionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling Claude API: {}", e.getMessage());
            throw new ExtractionException("Claude API error: " + e.getMessage());
        }
    }

    private String fallback(String prompt, Throwable t) {
        log.warn("Circuit breaker OPEN — LLM unavailable: {}", t.getMessage());
        throw new ExtractionException("LLM service temporarily unavailable (circuit breaker open). Try again later.");
    }
}
