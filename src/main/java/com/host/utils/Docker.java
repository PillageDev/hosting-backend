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

    public static void createServer(String containerId, String lang, int maxMemory, int maxCpu, int maxDisk, int maxBandwidth) {
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

        BuildImageCmd buildImageCmd = App.getClient().buildImageCmd(new File(dockerFile));
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
        final String storageFinal = maxDisk + "gb";
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withCpuShares(maxCpu) // % cpu
                .withCpuPeriod((long) 100000) // 100000 microseconds (100 milliseconds) of CPU time
                .withCpuQuota((long) 50000) // 50000 microseconds (50 milliseconds) of CPU time
                .withMemory((long) maxMemory)
                .withMemorySwap((long) 512 * 1024 * 1024) // 512 MB (Swap)
                .withStorageOpt(new HashMap<String, String>() {
                    {
                        put("size", storageFinal); // 5 GB storage
                    }
                });

        App.getClient().createContainerCmd("server-" + containerId)
                .withName(containerIdentifier)
                .withHostConfig(hostConfig)
                .withEnv("MAX_BANDWIDTH=" + maxBandwidth)
                .exec().getId();
    }

    public static void changeServerResources(String containerId, int maxMemory, int maxCpu, int maxDisk, int maxBandwidth, String language) {
        Bind[] volumeMounts = App.getClient().inspectContainerCmd(containerId)
            .exec()
            .getHostConfig()
            .getBinds();

        String dockerFile = "dockerfiles/";
        if (language.equals("java")) {
            dockerFile += "JDADockerfile";
        } else if (language.equals("python")) {
            dockerFile += "NodejsDockerfile";
        } else if (language.equals("javascript")) {
            dockerFile += "PythonDockerfile";
        } else {
            throw new IllegalArgumentException("Invalid language!");
        }

        String upgradeLevelEnv = "MAX_BANDWIDTH=" + maxBandwidth;

        HostConfig hostConfig = HostConfig.newHostConfig()
            .withCpuShares(maxCpu) // % cpu
            .withCpuPeriod((long) 100000) // 100000 microseconds (100 milliseconds) of CPU time
            .withCpuQuota((long) 50000) // 50000 microseconds (50 milliseconds) of CPU time
            .withMemory((long) maxMemory)
            .withMemorySwap((long) 512 * 1024 * 1024) // 512 MB (Swap)
            .withStorageOpt(new HashMap<String, String>() {
                {
                    put("size", maxDisk + "gb"); // 5 GB storage
                }
            });

        App.getClient().createContainerCmd(containerId)
            .withBinds(volumeMounts)
            .withEnv(upgradeLevelEnv)
            .withHostConfig(hostConfig)
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

    public static void deleteServer(String containerId) {
        String containerIdentifier = "server-" + containerId;
        String dockerContainerId = getContainerId(containerIdentifier);
        App.getClient().removeContainerCmd(dockerContainerId).exec();
    }
}
