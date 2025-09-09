package com.openai.chat.Tools;

import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import com.openai.chat.Services.IContainerService;

@Service
public class DockerContainerTools {

    private final IContainerService containerService;

    public DockerContainerTools(IContainerService containerService) {
        this.containerService = containerService;
    }

    @Tool(description = "Create a Docker container from a specified image with volume bindings")
    String createDockerContainer(String imageName, String containerName, Map<String, String> volumes) throws Exception {
        var response = containerService.createContainer(imageName, containerName, volumes);

        return "Docker container created with ID: " + response;
    }

    @Tool(description = "Run a Docker container from a specified image")
    String runDockerContainer(String imageName) throws Exception {

        var response = containerService.createContainer(imageName, imageName);

        containerService.startContainer(response);

        var isRunning = containerService.isContainerRunning(response);

        return String.format("Docker container from image is %s running", isRunning ? "successfully" : "not and has ID " + response);
    }

    @Tool(description = "Stop a Docker container by its ID")
    String stopDockerContainer(String containerId) throws Exception {
        containerService.stopContainer(containerId);
        return "Docker container with ID " + containerId + " has been stopped.";
    }

    @Tool(description = "Check if a Docker container is running by its ID")
    boolean isDockerContainerRunning(String containerId) throws Exception {
        return containerService.isContainerRunning(containerId); 
    }

}
