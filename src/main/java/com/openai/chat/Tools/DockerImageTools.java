package com.openai.chat.Tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import com.openai.chat.Services.IImageService;

@Service
public class DockerImageTools {

    private final IImageService imageService;

    public DockerImageTools(IImageService imageService) {
        this.imageService = imageService;
    }

    @Tool(name = "pull_docker_image", description = "Pull a docker image from a registry")
    public String pullImage(String imageName) {
        try {
            var response = imageService.pullImage(imageName);
            return "Docker image '" + imageName + "' successfully pulled with id: " + response;
        } catch (Exception e) {
            return "Failed to pull docker image '" + imageName + "': " + e.getMessage();
        }
    }

    @Tool(name = "list_docker_images", description = "List all docker images on the host")
    public String listImages() {
        try {
            var images = imageService.listImages();
            if (images.isEmpty()) {
                return "No docker images found on the host.";
            }
            StringBuilder response = new StringBuilder("docker images on the host :\n");
            for (String image : images) {
                response.append("- ").append(image).append("\n");
            }
            return response.toString();

        } catch (Exception e) {
            return "Failed to retrieve the docker images : " + e.getMessage();
        }
    }

    @Tool(name = "check_docker_image", description = "Check if a docker image is available on the host")
    public String isImageAvailable(String imageName) {
        try {
            boolean exists = imageService.imageExists(imageName);
            if (exists) {
                return "Docker image '" + imageName + "' is available on the host.";
            } else {
                return "Docker image '" + imageName + "' is NOT available on the host.";
            }
        } catch (Exception e) {
            return "Failed to check docker image '" + imageName + "': " + e.getMessage();
        }
    }

}
