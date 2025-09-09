package com.openai.chat.Services;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.openai.chat.Config.DockerClientConfig;
import com.openai.chat.Models.DockerContainer;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;

@Service
public class DockerContainerService implements IContainerService {

    private static Logger logger = LoggerFactory.getLogger(DockerContainerService.class);

    private final DockerClient dockerClient;

    public DockerContainerService(DockerClientConfig dockerClientConfig) {

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
    public String createContainer(String imageName, String containerName) throws Exception {
        return createContainerOptional(imageName, containerName, Optional.empty());
    }

    @Override
    public String createContainer(String imageName, String containerName, Map<String, String> volumes)
            throws Exception {
        return createContainerOptional(imageName, containerName, Optional.of(volumes));
    }

    @Override
    public void stopContainer(String containerId) throws Exception {
        dockerClient.stopContainerCmd(containerId).exec();
    }

    @Override
    public boolean isContainerRunning(String containerId) throws Exception {
        return dockerClient.inspectContainerCmd(containerId).exec().getState().getRunning() ? true : false;
    }

    @Override
    public List<DockerContainer> listContainers() throws Exception {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec().stream().map(c -> new DockerContainer(c.getId(), c.getNames()[0])).collect(Collectors.toList());
    }

    @Override
    public void startContainer(String containerId) throws Exception {
        try {
            dockerClient.startContainerCmd(containerId).exec();
        } catch (NotModifiedException e) {
            if (e.getMessage().contains("304")) {
                logger.info("Container " + containerId + " is already started.");
            } else {
                throw e; // rethrow if it's a different conflict
            }
        }
    }

    private String createContainerOptional(String imageName, String containerName,
            Optional<Map<String, String>> volumes)
            throws Exception {

        try {
            CreateContainerCmd containerCmd = dockerClient.createContainerCmd(imageName)
                    .withName(containerName);

            if (volumes.isEmpty() || volumes.get().isEmpty()) {
                CreateContainerResponse response = containerCmd.exec();
                return response.getId();
            } else {

                HostConfig hostConfig = HostConfig.newHostConfig();

                List<Bind> binds = new java.util.ArrayList<>();

                volumes.get().forEach((hostPath, containerPath) -> {
                    logger.info("Binding host path " + hostPath + " to container path " + containerPath);
                    var bind = new Bind(hostPath, new Volume(containerPath));

                    binds.add(bind);

                });

                hostConfig.withBinds(binds);

                CreateContainerResponse response = containerCmd
                        .withHostConfig(hostConfig)
                        .exec();
                return response.getId();
            }
        } catch (ConflictException e) {
            if (e.getMessage().contains("Conflict. The container name")) {
                logger.info(containerName + " already exists, returning existing container ID.");
                // Container with the same name already exists, return existing container ID
                return listContainers().stream()
                        .filter(container -> container.getName() != null
                                && List.of(container.getName()).contains("/" + containerName))
                        .findFirst()
                        .map(container -> container.getId())
                        .orElseThrow(() -> new Exception("Container with name " + containerName
                                + " already exists but could not retrieve its ID."));
            } else {
                throw e; // rethrow if it's a different conflict
            }
        }

    }

    @Override
    public void deleteContainerIfExists(String containerName) throws Exception {

        listContainers().stream()
                .filter(container -> container.getName() != null
                        && List.of(container.getName()).contains("/" + containerName))
                .findFirst()
                .map(container -> container.getId())
                .ifPresent(containerId -> {
                    try {
                        stopContainer(containerId);
                        dockerClient.removeContainerCmd(containerId).exec();
                    } catch (Exception e) {
                        logger.error("Error deleting container " + containerId, e);
                    }
                });

    }

}