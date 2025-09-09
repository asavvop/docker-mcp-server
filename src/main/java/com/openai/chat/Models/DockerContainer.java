package com.openai.chat.Models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DockerContainer {

    private String id;
    private String name;

    public DockerContainer(String id, String name) {
        this.name = name;
        this.id = id;  
    }
    
}
