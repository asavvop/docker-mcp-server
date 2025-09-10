package com.openai.chat;

import java.io.IOException;
import java.util.List;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.openai.chat.Services.IContainerService;
import com.openai.chat.Services.IImageService;
import com.openai.chat.Tools.DockerContainerTools;
import com.openai.chat.Tools.DockerImageTools;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(classes = {Testconfiguration.class, ChatApplication.class}) 
class ChatApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(ChatApplicationTests.class);

	@Autowired
	private OpenAiChatModel chatModel;


	@Autowired
	private IContainerService containerService;

	@Autowired
	private IImageService imageService;

	@BeforeAll
	public static void beforeAll() throws IOException, InterruptedException {
		logger.info("Start pulling the '" + Testconfiguration.DEFAULT_MODEL + "' generative ... would take several minutes ...");

		String baseUrl = "http://%s:%d".formatted(Testconfiguration.socat().getHost(), Testconfiguration.socat().getMappedPort(80));

		RestAssured.given().baseUri(baseUrl).body("""
				{
				    "from": "%s"
				}
				""".formatted(Testconfiguration.DEFAULT_MODEL)).post("/models/create").prettyPeek().then().statusCode(200);

		logger.info(Testconfiguration.DEFAULT_MODEL + " pulling competed!");
	}

	@Test
	void basicTest() {
		UserMessage userMessage = new UserMessage(
				"Tell all about todays weather for Nuremberg, Germany.");
		SystemMessage systemMessage = new SystemMessage("I am a weather forecast AI agent.");
		Prompt prompt = new Prompt(List.of(userMessage, systemMessage));
		ChatResponse response = this.chatModel.call(prompt);
		assertThat(response.getResults()).hasSize(1);
		assertThat(response.getResults().get(0).getOutput().getText()).contains("weather forecast");
	}

	@Test
	void createDockerContainerTest() throws Exception {

		imageService.pullImage("nginx:latest");
		containerService.deleteContainerIfExists("mynginx");

		UserMessage userMessage = new UserMessage(
				"Create a docker container from the nginx:latest image with the name mynginx and bind the volume /mnt/host/c/Code/bruno to /myvolume in the container");
		Prompt prompt = new Prompt(userMessage);
		ChatResponse response = ChatClient.create(chatModel).prompt(prompt).tools(new DockerContainerTools(containerService)).call().chatResponse();

		assertThat(response.getResults()).hasSize(1);
		assertThat(response.getResults().get(0).getOutput().getText()).contains("created");
		assertThat(response.getResults().get(0).getOutput().getText()).contains("ID");

	}


	@Test
	void runDockerContainerTest() throws Exception {

		imageService.pullImage("nginx:latest");
		String containerId = containerService.createContainer("nginx:latest", "nginx-run-test");

		UserMessage userMessage = new UserMessage(
				"Run a docker container with the ID " + containerId);
		Prompt prompt = new Prompt(userMessage);
		ChatResponse response = ChatClient.create(chatModel).prompt(prompt).tools(new DockerContainerTools(containerService)).call().chatResponse();

		assertThat(response.getResults()).hasSize(1);
		assertThat(response.getResults().get(0).getOutput().getText()).contains("running");

	}


	@Test
	void stopContainerTest() throws Exception {

		imageService.pullImage("nginx:latest");
		String containerId = containerService.createContainer("nginx:latest", "nginx");
		containerService.startContainer(containerId);

		UserMessage userMessage = new UserMessage(
				"Stop the docker container with the ID " + containerId);
		Prompt prompt = new Prompt(userMessage);
		ChatResponse response = ChatClient.create(chatModel).prompt(prompt).tools(new DockerContainerTools(containerService)).call().chatResponse();

		assertThat(response.getResults()).hasSize(1);
		assertThat(response.getResults().get(0).getOutput().getText()).contains("has been stopped");

	}

	@Test
	void listContainersTest() throws Exception {

		imageService.pullImage("nginx:latest");
		containerService.createContainer("nginx:latest", "nginx-list-1");
		containerService.createContainer("nginx:latest", "nginx-list-2");

		UserMessage userMessage = new UserMessage("Get all available docker containers in this host.");
		Prompt prompt = new Prompt(userMessage);
		ChatResponse response = ChatClient.create(chatModel).prompt(prompt)
				.tools(new DockerContainerTools(containerService)).call().chatResponse();

		assertThat(response.getResult()).isNotNull();
		
	}

	@Test
	void checkImageExistsInHost() throws Exception {
		imageService.pullImage("nginx:latest");

		UserMessage userMessage = new UserMessage(
				"Check if the docker image nginx:latest is available on the host");
		Prompt prompt = new Prompt(userMessage);
		ChatResponse response = ChatClient.create(chatModel).prompt(prompt)
				.tools(new DockerImageTools(imageService)).call().chatResponse();

		assertThat(response.getResults()).hasSize(1);
		assertThat(response.getResults().get(0).getOutput().getText())
				.contains("is available on the host");

		logger.info(response.getResults().get(0).getOutput().getText());
	}

	@Test
	void retrieveImageList() {
		UserMessage userMessage = new UserMessage(
				"List all docker images on the host");
		Prompt prompt = new Prompt(userMessage);
		ChatResponse response = ChatClient.create(chatModel).prompt(prompt)
				.tools(new DockerImageTools(imageService)).call().chatResponse();

		assertThat(response.getResults()).hasSize(1);
		assertThat(response.getResults().get(0).getOutput().getText())
				.contains("docker images on the host");

		logger.info(response.getResults().get(0).getOutput().getText());
	}


	@Test
	void pullImageTest() throws Exception {
		UserMessage userMessage = new UserMessage(
				"Pull the docker image nginx:latest");
		Prompt prompt = new Prompt(userMessage);
		ChatResponse response = ChatClient.create(chatModel).prompt(prompt)
				.tools(new DockerImageTools(imageService)).call().chatResponse();

		assertThat(response.getResults()).hasSize(1);
		assertThat(response.getResults().get(0).getOutput().getText()).contains("successfully pulled");

		logger.info("Check if nginx:latest is really pulled ...");
		assertThat(imageService.imageExists("nginx:latest")).isTrue();
		logger.info("nginx:latest is really pulled ...");
	}

}