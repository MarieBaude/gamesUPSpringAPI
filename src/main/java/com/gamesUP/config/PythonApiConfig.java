package com.gamesUP.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PythonApiConfig {

    @Value("${python.api.url:http://localhost:8000}")
    private String pythonApiUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public String pythonApiUrl() {
        return pythonApiUrl;
    }
}