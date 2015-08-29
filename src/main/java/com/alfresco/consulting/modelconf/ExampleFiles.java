package com.alfresco.consulting.modelconf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexmahabir on 8/28/15.
 */
@Component
@ConfigurationProperties(prefix = "example")
public class ExampleFiles {
    List<String> files = new ArrayList<>();

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
}
