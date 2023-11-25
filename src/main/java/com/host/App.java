package com.host;

import java.util.UUID;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

/**
 * Hello world!
 *
 */
public class App {
    private static DockerClientConfig standard;
    private static DockerClient client;
    public static void main( String[] args ) {
        standard = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        client = DockerClientBuilder.getInstance(standard).build();
        System.out.println("Enabling!");
        System.setProperty("AUTH_TOKEN", UUID.randomUUID().toString());

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Shutting down...");
                System.setProperty("AUTH_TOKEN", null);
            }
        });
        String containerId = "containerID";
        
    }

    public static DockerClientConfig getStandard() {
        return standard;
    }

    public static DockerClient getClient() {
        return client;
    }
}
