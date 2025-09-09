package com.openai.chat.Services;

import java.util.List;

public interface IImageService {

    String pullImage(String imageName) throws Exception;
    boolean imageExists(String imageName) throws Exception;
    void removeImage(String imageName) throws Exception;
    String getImageId(String imageName) throws Exception;
    List<String> listImages() throws Exception;
}
