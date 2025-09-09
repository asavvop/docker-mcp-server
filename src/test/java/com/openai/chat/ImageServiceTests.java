package com.openai.chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import com.openai.chat.Services.IImageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ChatApplication.class)
class ImageServiceTests {

    @Autowired
    private IImageService imageService;

    @Test
    public void pullImage() throws Exception {
        var response = imageService.pullImage("nginx:latest");

        assertThat(response).isNotEmpty();
    }

    @Test
    public void checkImageExists() throws Exception {

        imageService.pullImage("nginx:latest");
        var response = imageService.imageExists("nginx:latest");

        assertThat(response).isTrue();
    }

    @Test
    public void checkImageDoesntExist() throws Exception {

        var response = imageService.imageExists("test:latest");

        assertThat(response).isFalse();
    }

    @Test
    public void removeImage() throws Exception {

        imageService.pullImage("nginx:latest");
        imageService.removeImage("nginx:latest");

        assertThat(imageService.imageExists("nginx:latest")).isFalse();
    }

    @Test
    public void removeNonExistingImage() throws Exception {

        imageService.removeImage("test:latest");

        assertThat(imageService.imageExists("test:latest")).isFalse();
    }

    @Test
    public void getImageId() throws Exception {

        imageService.pullImage("nginx:latest");
        var response = imageService.getImageId("nginx:latest");

        assertThat(response).isNotEmpty();

        imageService.removeImage("nginx:latest");

        response = imageService.getImageId("nginx:latest");

        assertThat(response).isNull();
    }


    @Test
    public void listAvailableImages() throws Exception {

        imageService.pullImage("nginx:latest");

        var imageList = imageService.listImages();

        assertThat(imageList).isNotNull();
        assertThat(imageList.size()).isGreaterThan(0);
        assertThat(imageList).contains("nginx:latest");
    }
}
