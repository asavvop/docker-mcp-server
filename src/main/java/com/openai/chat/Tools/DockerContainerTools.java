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

    @Tool(description = "Create a Docker container from a specified image with container name and volume bindings")
    String createDockerContainer(String imageName, String containerName, Map<String, String> volumes) throws Exception {
        var response = containerService.createContainer(imageName, containerName, volumes);

        return "Container with ID: " + response + " has been created from image: " + imageName;
    }

    @Tool(description = "Run a Docker container from its ID")
    String runDockerContainer(String containerId) throws Exception {

        containerService.startContainer(containerId);

        var isRunning = containerService.isContainerRunning(containerId);

        return String.format("Docker container with ID %s is %d running", containerId , isRunning ? "successfully" : "not");
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
