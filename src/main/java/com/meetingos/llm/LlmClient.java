package com.meetingos.llm;

/**
 * Abstraction over LLM providers. Implementations handle
 * authentication, request formatting, and response extraction
 * specific to each provider (Claude, Gemini, etc).
 */
public interface LlmClient {
    /**
     * Send a prompt and return the raw text response.
     * @throws com.meetingos.service.ExtractionException if the API call fails
     */
    String call(String prompt);
}
