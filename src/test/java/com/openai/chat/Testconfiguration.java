package com.openai.chat;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.SocatContainer;
import org.testcontainers.junit.jupiter.Container;

@SpringBootConfiguration
public class Testconfiguration {

    public static final String DEFAULT_MODEL = "ai/gemma3";

    @SuppressWarnings("resource")
    @Container
    private static final SocatContainer socat = new SocatContainer().withTarget(80, "model-runner.docker.internal");


    @Bean
    public static SocatContainer socat() {
        socat.start();
        return socat;
    }

    @Bean
    public OpenAiApi chatCompletionApi() {
        var baseUrl = "http://%s:%d/engines".formatted(socat.getHost(), socat.getMappedPort(80));
        return OpenAiApi.builder().baseUrl(baseUrl).apiKey("test").build();
    }

    @Bean
    public OpenAiChatModel openAiClient(OpenAiApi openAiApi) {
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder().maxTokens(2048).model(DEFAULT_MODEL).build())
                .build();
    }

}
