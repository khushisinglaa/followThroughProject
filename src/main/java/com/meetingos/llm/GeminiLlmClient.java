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
@ConditionalOnProperty(name = "llm.provider", havingValue = "gemini", matchIfMissing = true)
@Slf4j
public class GeminiLlmClient implements LlmClient {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.base-url}")
    private String baseUrl;

    public GeminiLlmClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @CircuitBreaker(name = "llm", fallbackMethod = "fallback")
    public String call(String prompt) {
        // Gemini API: POST {baseUrl}/models/{model}:generateContent?key={apiKey}
        String url = baseUrl + "/models/" + model + ":generateContent?key=" + apiKey;

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                )),
                "generationConfig", Map.of(
                        "temperature", 0.1,
                        "maxOutputTokens", 4096
                )
        );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestJson = objectMapper.writeValueAsString(body);
            HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ExtractionException("Gemini API returned status: " + response.getStatusCode());
            }

            // Parse Gemini response: candidates[0].content.parts[0].text
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode text = root.path("candidates").path(0)
                    .path("content").path("parts").path(0).path("text");

            if (!text.isMissingNode()) {
                return text.asText();
            }
            throw new ExtractionException("Unexpected Gemini API response structure");

        } catch (RestClientException e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new ExtractionException("Gemini API unavailable: " + e.getMessage());
        } catch (ExtractionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage());
            throw new ExtractionException("Gemini API error: " + e.getMessage());
        }
    }

    private String fallback(String prompt, Throwable t) {
        log.warn("Circuit breaker OPEN — LLM unavailable: {}", t.getMessage());
        throw new ExtractionException("LLM service temporarily unavailable (circuit breaker open). Try again later.");
    }
}
