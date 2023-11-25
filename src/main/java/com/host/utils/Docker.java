package com.host.utils;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.SaveImageCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.host.App;

@UtilityClass
public class Docker {

    public static void startServer(String containerId) {
        String containerIdentifier = "server-" + containerId;
        String containerDockerId = getContainerId(containerIdentifier);
        App.getClient().startContainerCmd(containerDockerId).exec();
    }

    public static void restartServer(String containerId) {
        String containerIdentifier = "server-" + containerId;
        String containerDockerId = getContainerId(containerIdentifier);
        App.getClient().restartContainerCmd(containerDockerId).exec();
    }

    public static void stopServer(String containerId) {
        String containerIdentifier = "server-" + containerId;
        String containerDockerId = getContainerId(containerIdentifier);
        App.getClient().stopContainerCmd(containerDockerId).exec();
    }

    public static void createServer(String containerId, String lang, int upgradeLevel) {
        String dockerFile = "dockerfiles/";
        
        switch (lang) {
            case "java":
                dockerFile += "JDADockerfile";
                break;
            case "python":
                dockerFile += "NodejsDockerfile";
                break;
            case "javascript":
                dockerFile += "PythonDockerfile";
                break;
            default:
                throw new IllegalArgumentException("Invalid language!");
        }

        BuildImageCmd buildImageCmd = App.getClient().buildImageCmd(new Files(dockerFile));
            buildImageCmd.withTag("server-" + containerId);
            buildImageCmd.exec(new BuildImageResultCallback() {
                @Override
                public void onNext(BuildResponseItem item) {
                    // Handle build progress (optional)
                    System.out.println(item.getStream());
                    super.onNext(item);
                }
            }).awaitImageId();
        
        String containerIdentifier = "server-" + containerId;
        long memory;
        int cpuShares;
        String storage = "";
        if (upgradeLevel == 0) {
            memory = 512 * 1024 * 1024; // 512 MB
            cpuShares = (int) 0.5 * 1024; // 50% cpu
            storage = "5g"; // 5 GB storage
        } if (upgradeLevel == 1 || upgradeLevel == 2) {
            memory = 1024 * 1024 * 1024; // 1 GB
            cpuShares = (int) 0.75 * 1024; // 75% cpu
            storage = "10g"; // 10 GB storage
        } else {
            memory = 512 * 1024 * 1024; // 512 MB
            cpuShares = (int) 0.5 * 1024; // 50% cpu
            storage = "5g"; // 5 GB storage
        }   
        final String storageFinal = storage;
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withCpuShares(cpuShares) // % cpu  
                .withCpuPeriod((long) 100000) // 100000 microseconds (100 milliseconds) of CPU time
                .withCpuQuota((long) 50000) // 50000 microseconds (50 milliseconds) of CPU time
                .withMemory(memory)
                .withMemorySwap((long) 512 * 1024 * 1024) // 512 MB (Swap)
                .withStorageOpt(new HashMap<String, String>() {
                    {
                        put("size", storageFinal); // 5 GB storage
                    }
                });

        App.getClient().createContainerCmd("server-" + containerId)
                .withName(containerIdentifier)
                .withHostConfig(hostConfig)
                .withEnv("UPGRADE_LEVEL=" + upgradeLevel)
                .exec().getId();
    }

    public static void alterServerLevel(String containerId, int upgradeLevel) {
        Bind[] volumeMounts = App.getClient().inspectContainerCmd(containerId)
            .exec()
            .getHostConfig()
            .getBinds();

        String image = App.getClient().inspectContainerCmd(containerId).exec().getImageId();

        String upgradeLevelEnv = "UPGRADE_LEVEL=" + upgradeLevel;

        App.getClient().createContainerCmd(image)
            .withBinds(volumeMounts)
            .withEnv(upgradeLevelEnv)
            .exec()
            .getId();
    }

    public static String getFiles(String containerId) {
        String containerIdentifier = "server-" + containerId;
        DockerClient client = App.getClient();
        SaveImageCmd saveImageCmd = client.saveImageCmd(containerIdentifier);
        InputStream imageStream = saveImageCmd.exec();
    
        StringBuilder base64String = new StringBuilder();
        byte[] buffer = new byte[8192]; 
        int bytesRead;
        try {
            while ((bytesRead = imageStream.read(buffer)) != -1) {
                byte[] chunk = Arrays.copyOf(buffer, bytesRead); 
                base64String.append(Base64.getEncoder().encodeToString(chunk));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    
        return base64String.toString();
    }
    
    public static String getContainerId(String containerName) {
        String containerId = App.getClient().listContainersCmd()
                .withNameFilter(Collections.singletonList(containerName))
                .exec().get(0).getId();
        return containerId;
    }
}
