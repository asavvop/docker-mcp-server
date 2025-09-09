package com.openai.chat;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import com.openai.chat.Services.IContainerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ChatApplication.class)
class ContainerServiceTests {
    
    @Autowired
    private IContainerService containerService;

    @Test
    public void createContainer() throws Exception {

       String containerId = containerService.createContainer("nginx:latest", "test-nginx");

       assertThat(containerId).isNotEmpty();
    }

    @Test
    public void createContainerWithBinds() throws Exception {

       String containerId = containerService.createContainer("nginx:latest", "test-nginx-binds", Map.of("/mnt//host/c/Code/bruno","/myvolume" ));

       assertThat(containerId).isNotEmpty();
    }

    @Test
    public void listContainers() throws Exception { 

        var containers = containerService.listContainers();

        assertThat(containers).isNotNull();
    }

    @Test
    public void startStopContainer() throws Exception { 

        String containerId = containerService.createContainer("nginx:latest", "test-nginx-start-stop");

        containerService.startContainer(containerId);

        assertThat(containerService.isContainerRunning(containerId)).isTrue();

        containerService.stopContainer(containerId);

        assertThat(containerService.isContainerRunning(containerId)).isFalse();
    }


    @Test
    public void verifyAlreadyRunningContainer() throws Exception { 

        String containerId = containerService.createContainer("nginx:latest", "test-nginx-start-stop");

        containerService.startContainer(containerId);

        assertThat(containerService.isContainerRunning(containerId)).isTrue();

        containerService.startContainer(containerId);

        assertThat(containerService.isContainerRunning(containerId)).isTrue();
    }

    @Test
    public void removeContainerIfExists() throws Exception { 

        String containerId = containerService.createContainer("nginx:latest", "test-nginx-remove");

        containerService.startContainer(containerId);

        assertThat(containerService.isContainerRunning(containerId)).isTrue();

        containerService.deleteContainerIfExists("test-nginx-remove");

        // give some time for docker to remove the container
        Thread.sleep(2000);

        var containers = containerService.listContainers();

        var exists = containers.stream().anyMatch(c -> c.getId().equals(containerId));

        assertThat(exists).isFalse();
    }
}
