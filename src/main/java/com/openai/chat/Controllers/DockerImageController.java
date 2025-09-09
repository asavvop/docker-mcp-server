package com.openai.chat.Controllers;

import java.util.Map;
import java.util.logging.Logger;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.ResponseEntity;
import static org.springframework.http.ResponseEntity.ok;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openai.chat.Services.IImageService;
import com.openai.chat.Tools.DockerImageTools;

@RestController
@RequestMapping("/images")
public class DockerImageController {

    private final static Logger logger = Logger.getLogger(DockerImageController.class.getName());

    private final ChatClient chatClient;

    public DockerImageController(OpenAiChatModel openAiChatModel, IImageService imageService) {
        this.chatClient = ChatClient.builder(openAiChatModel).defaultTools(new DockerImageTools(imageService)).build();
    }

    @GetMapping(value = "/list")
    ResponseEntity<String> listImages() {

        logger.info("Retrieving docker images via controller");

        Prompt p = new Prompt("List all docker images on the host");

        return ok(chatClient.prompt(p)
                .call()
                .content());
    }

    @GetMapping(value = "/pull/{imageName}")
    ResponseEntity<String> pullImage(@PathVariable String imageName) {   
        logger.info("Pulling docker image via controller");

        PromptTemplate pt = new PromptTemplate("Pull the docker image '%imageName' from Docker Hub");
        Prompt p = pt.create(Map.of("imageName", imageName));

        return ok(chatClient.prompt(p)
                .call()
                .content());
    }

}
