package com.openai.chat.Controllers;

import java.util.Map;
import java.util.logging.Logger;

import static org.springframework.http.ResponseEntity.ok;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openai.chat.Services.IContainerService;
import com.openai.chat.Tools.DockerContainerTools;

@RestController
@RequestMapping("/api/containers")
public class DockerContainerController {

    private final static Logger logger = Logger.getLogger(DockerContainerController.class.getName());

    private final ChatClient chatClient;

    public DockerContainerController(OpenAiChatModel openAiChatModel, IContainerService containerService) {
        this.chatClient = ChatClient.builder(openAiChatModel).defaultTools(new DockerContainerTools(containerService))
                .build();
    }

    @GetMapping("/create/{imageName}/{containerName}")
    public ResponseEntity<String> runContainer(@PathVariable String imageName, @PathVariable String containerName) throws Exception {

        logger.info("Running container via controller");

        PromptTemplate pt = new PromptTemplate("Create a Docker container from the '{imageName}' image with container name '{containerName}'");
        Prompt p = pt.create(Map.of("imageName", imageName, "containerName", containerName));

        return ok(chatClient.prompt(p)
                .call()
                .content());

    }

    @GetMapping("/start/{containerId}")
    public ResponseEntity<String> startContainer(@PathVariable String containerId) throws Exception {
        logger.info("Starting container via controller");

        PromptTemplate pt = new PromptTemplate("Run a Docker container with the ID '{containerId}'");
        Prompt p = pt.create(Map.of("containerId", containerId));

        return ok(chatClient.prompt(p)
                .call()
                .content());
    }

}
