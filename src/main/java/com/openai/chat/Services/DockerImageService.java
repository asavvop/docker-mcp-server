package com.openai.chat.Services;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.openai.chat.Config.DockerClientConfig;

@Service
public class DockerImageService implements IImageService {

    private static Logger logger = LoggerFactory.getLogger(DockerImageService.class);

    private final DockerClient dockerClient;

    public DockerImageService(DockerClientConfig dockerClientConfig) {
        Properties properties = new Properties();
        properties.setProperty("DOCKER_HOST", dockerClientConfig.getHost());

        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withProperties(properties).build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(1)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    @Override
    public String pullImage(String imageName) throws Exception {
        dockerClient.pullImageCmd(imageName).start().awaitCompletion();
        return dockerClient.inspectImageCmd(imageName).exec().getId();
    }

    @Override
    public boolean imageExists(String imageName) throws Exception {
        try {
            var response = dockerClient.inspectImageCmd(imageName).exec();
            return response != null;
        } catch (NotFoundException e) {
            return false;
        }
    }

    @Override
    public void removeImage(String imageName) throws Exception {

        try {
            dockerClient.removeImageCmd(imageName).exec();
        } catch (NotFoundException e) {
            logger.warn(imageName + " not found when attempting to remove image.");
        }
    }

    @Override
    public String getImageId(String imageName) throws Exception {

        try {
            var response = dockerClient.inspectImageCmd(imageName).exec();
            return response.getId();

        } catch (NotFoundException e) {
            return null;
        }

    }

    @Override
    public List<String> listImages() throws Exception {
        var images = dockerClient.listImagesCmd().exec();
        List<String> imageNames = images.stream()
                .map(image -> {
                    String imageName = image.getRepoTags()[0];
                    String[] parts = imageName.split("/");
                    return parts[parts.length - 1];
                })
                .toList();

        return imageNames;
    }

}
