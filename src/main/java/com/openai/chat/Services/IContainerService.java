package com.openai.chat.Services;

import java.util.List;
import java.util.Map;

import com.openai.chat.Models.DockerContainer;

public interface IContainerService {

    public String createContainer(String imageName, String containerName) throws Exception;
    public String createContainer(String imageName, String containerName, Map<String,String> volumes) throws Exception;
    public void startContainer(String containerId) throws Exception;
    public void stopContainer(String containerId) throws Exception;
    public boolean isContainerRunning(String containerId) throws Exception;
    public List<DockerContainer> listContainers() throws Exception;
    public void deleteContainerIfExists(String containerName) throws Exception;

}
