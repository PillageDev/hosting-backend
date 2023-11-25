package com.host.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.ExecStartCmd;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.host.App;
import com.host.utils.Docker;
import com.host.utils.Pocketbase;
import com.host.utils.Pocketbase.User;

@Path("api/v1/file")
public class Files {
    @GET
    @Path("/getDir")
    @Consumes("application/json")
    @Produces("application/json")
    public Map<String, String> getDir(@HeaderParam("token") String token, String id, String dir) {
        User user = Pocketbase.getUser(token);
        if (user == null) {
            return null;
        } else {
            DockerClient client = App.getClient();
            String containerID = Docker.getContainerId("server-" + id);
            ExecCreateCmdResponse command = client.execCreateCmd(containerID).withCmd("pwd").exec();
            ExecStartCmd start = client.execStartCmd(command.getId());
            ExecStartResultCallback callback = new ExecStartResultCallback();
            start.exec(callback);
            String workingDir = "";
            try {
                workingDir = callback.awaitCompletion().toString();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Map<String, String> files = new HashMap<>();
            File file = new File(workingDir + dir);
            File[] filesInDir = file.listFiles();
            for (File f : filesInDir) {
                String name = f.getName();
                String type = f.isDirectory() ? "dir" : getFileExtension(name);
                files.put(name, type);
            }

            return files;
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }

    @GET
    @Path("/getContents")
    @Consumes("application/json")
    @Produces("application/json")
    /**
     * Returns the contents of a file
     * @param token The user's token
     * @param id The server's id
     * @param path The path to the file, including the file name and extension
     * @return A map of the file's contents, with the key being the line number and the value being the line's contents
     */
    public Map<String, String> getContents(@HeaderParam("token") String token, String id, String path) {
        User user = Pocketbase.getUser(token);
        if (user == null) {
            return null;
        } else {
            DockerClient client = App.getClient();
            String containerID = Docker.getContainerId("server-" + id);
            ExecCreateCmdResponse command = client.execCreateCmd(containerID).withCmd("pwd").exec();
            ExecStartCmd start = client.execStartCmd(command.getId());
            ExecStartResultCallback callback = new ExecStartResultCallback();
            start.exec(callback);
            String workingDir = "";
            try {
                workingDir = callback.awaitCompletion().toString();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Map<String, String> files = new HashMap<>();
            File file = new File(workingDir + path); 
            
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                int lineNumber = 1;
                while ((line = reader.readLine()) != null) {
                    files.put(String.valueOf(lineNumber), line);
                    lineNumber++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return files;
        }
    }
}
