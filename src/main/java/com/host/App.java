package com.host;

import java.util.UUID;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.host.utils.Scheduler;
import lombok.Getter;
import lombok.SneakyThrows;
import org.eclipse.jetty.server.Server;

/**
 * Hello world!
 *
 */
public class App {
    @Getter
    private static DockerClientConfig standard;
    @Getter
    private static DockerClient client;
    @SneakyThrows
    public static void main(String[] args ) {
        standard = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        client = DockerClientBuilder.getInstance(standard).build();
        System.out.println("Enabling!");
        System.setProperty("AUTH_TOKEN", UUID.randomUUID().toString());

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Shutting down...");
                System.setProperty("AUTH_TOKEN", "");
            }
        });
        String containerId = "containerID";

        // Keep the program running
        Server server = new Server(8080);
        server.start();
        server.join();
        Scheduler scheduler = new Scheduler();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.stopLogMonitoring();
            System.out.println("Scheduler stopped");
        }));
    }

}
