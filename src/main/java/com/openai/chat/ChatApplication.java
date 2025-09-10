package com.openai.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatApplication {

	private static Logger logger = LoggerFactory.getLogger(ChatApplication.class);

	public static void main(String[] args) {
		logger.info("Chat application started");

		SpringApplication.run(ChatApplication.class, args);
	}

}
