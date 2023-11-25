package com.host.api;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.websocket.server.PathParam;
import javax.ws.rs.*;

import com.host.utils.Docker;
import com.host.utils.Pocketbase;
import com.host.utils.Pocketbase.User;

import com.host.utils.Scheduler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.TimeUnit;

@Path("api/v1/servers")
public class Servers {
    private boolean validOathCode(String code) {
        String authToken = System.getenv("AUTH_TOKEN");
        if (authToken == null) {
            return false;
        } else {
            return authToken.equals(code);
        }
    }

    @POST
    @Path("/create")
    @Consumes("application/json")
    @Produces("application/json")
    public boolean createServer(String oathCode, Server server, User.Record user) {
        if (validOathCode(oathCode)) {
            String language = server.getLanguage();
            int maxMemory = server.getMaxMemory();
            int maxCPU = server.getMaxCPU();
            boolean database = server.isDatabase();
            int upgradeLevel = 0;
            if (maxMemory == 716 && maxCPU == 716) {
                upgradeLevel = 1;
                if (database) {
                    upgradeLevel = 2;
                }
            }
            Docker.createServer(Docker.getContainerId("server-" + user.getDiscordId()), language, upgradeLevel);
            return true;
        } else {
            return false;
        }
    }

    @PUT
    @Path("/update/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    public boolean updateServer(@PathParam("id") String serverID, String oathCode, Server server, User.Record user) {
        if (validOathCode(oathCode)) {
            String language = server.getLanguage();
            int maxMemory = server.getMaxMemory();
            int maxCPU = server.getMaxCPU();
            boolean database = server.isDatabase();
            int upgradeLevel = 0;
            if (maxMemory == 716 && maxCPU == 716) {
                upgradeLevel = 1;
                if (database) {
                    upgradeLevel = 2;
                }
            }
            Docker.alterServerLevel(Docker.getContainerId("server-" + user.getDiscordId()), upgradeLevel);
        }
        return false;
    }

    @DELETE
    @Path("/delete/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    public boolean deleteServer(@PathParam("id") String serverID, String oathCode) {
        if (validOathCode(oathCode)) {
            Docker.deleteServer(Docker.getContainerId("server-" + serverID));
            return false;
        } else {
            return false;
        }
    }

    @GET
    @Path("/get/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    public Server getServer(@PathParam("id") String serverID, String oathCode) {
        if (validOathCode(oathCode)) {
            for (User user : Pocketbase.users) {
                if (user.getRecord().getServer().getId().equals(serverID)) {
                    return Server.builder()
                        .serverName(user.getRecord().getServer().getServerName())
                        .language(user.getRecord().getServer().getLanguage())
                        .maxMemory(user.getRecord().getServer().getMaxMemory())
                        .maxDisk(user.getRecord().getServer().getMaxDisk())
                        .maxCPU(user.getRecord().getServer().getMaxCPU())
                        .maxNetwork(user.getRecord().getServer().getMaxNetwork())
                        .database(user.getRecord().getServer().isDatabase())
                        .id(user.getRecord().getServer().getId())
                        .build();
                }
            }
        }
        return null;
    }

    @GET
    @Path("/server/{id}/start")
    @Consumes("application/json")
    @Produces("application/json")
    public boolean startServer(@PathParam("id") String serverID, String jwtToken) {
        User user = Pocketbase.getUser(jwtToken);
        if (user == null) {
            Scheduler scheduler = new Scheduler();
            scheduler.getScheduledExecutorService().scheduleAtFixedRate(() -> {
                String container = "server-" + serverID;
                scheduler.sendOutput(container);
            }, 0, 10, TimeUnit.SECONDS);
            return false;

        }
        if (!(serverID.equals(user.getRecord().getDiscordId()))) {
            return false;
        }
        //TODO: Check jwt token against pocketbase and ensure permissions and start server
        return false;
    }

    @GET
    @Path("/server/{id}/stop")
    @Consumes("application/json")
    @Produces("application/json")
    public boolean stopServer(@PathParam("id") String serverID, String jwtToken) {
        User user = Pocketbase.getUser(jwtToken);
        if (user == null) {

            return false;
        }
        //TODO: Check jwt token against pocketbase and ensure permissions and stop server
        return false;
    }

    @GET
    @Path("/server/{id}/restart")
    @Consumes("application/json")
    @Produces("application/json")
    public boolean restartServer(@PathParam("id") String serverID, String jwtToken) {
        User user = Pocketbase.getUser(jwtToken);
        if (user == null) {
            return false;
        }
        //TODO: Check jwt token against pocketbase and ensure permissions and restart server
        return false;
    }

    @GET
    @Path("/server/{id}/stats")
    @Consumes("application/json")
    @Produces("application/json")
    public ServerStats getServerStats(@PathParam("id") String serverID, String jwtToken) {
        User user = Pocketbase.getUser(jwtToken);
        if (user == null) {
            return null;
        }
        //TODO: Check jwt token against pocketbase and ensure permissions and get server stats
        return null;
    }

    @Getter
    @AllArgsConstructor
    @FieldDefaults(makeFinal = true)
    @Builder
    public static class Server {
        private String serverName;
        private String language;
        private int maxMemory; // In MB
        private int maxDisk; // In GB
        private int maxCPU; // In %
        private int maxNetwork; // In MB/s
        private boolean database; // Whether or not to include a database
        private String id;

        public JsonObject toJson() {
            JsonObjectBuilder json = Json.createObjectBuilder()
                .add("serverName", serverName)
                .add("id", id)
                .add("language", language)
                .add("maxMemory", maxMemory)
                .add("maxDisk", maxDisk)
                .add("maxCPU", maxCPU)
                .add("maxNetwork", maxNetwork)
                .add("database", database);
            return json.build();
        }
    }  

    @Getter
    @AllArgsConstructor
    @FieldDefaults(makeFinal = true) 
    public static class ServerStats {
        private float cpuUsage;
        private float memoryUsage;
        private float diskUsage;
        private float networkUsage;
    }
}
