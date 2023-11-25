package com.host.api;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.websocket.server.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.host.utils.Pocketbase;
import com.host.utils.Pocketbase.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

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
    public boolean createServer(String oathCode, Server server) {
        if (validOathCode(oathCode)) {
            
            
            
            return false;
        } else {
            return false;
        }
    }

    @GET
    @Path("/update/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    public boolean updateServer(@PathParam("id") String serverID, String oathCode, Server server) {
        if (validOathCode(oathCode)) {
            //TODO: Update server  
            return false;
        } else {
            return false;
        }
    }

    @GET
    @Path("/delete/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    public boolean deleteServer(@PathParam("id") String serverID, String oathCode) {
        if (validOathCode(oathCode)) {
            //TODO: Delete server
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
            //TODO: Get server
            return null;
        } else {
            return null;
        }
    }

    @GET
    @Path("/server/{id}/start")
    @Consumes("application/json")
    @Produces("application/json")
    public boolean startServer(@PathParam("id") String serverID, String jwtToken) {
        User user = Pocketbase.getUser(jwtToken);
        if (user == null) {
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

        public JsonObject toJson() {
            JsonObjectBuilder json = Json.createObjectBuilder()
                .add("serverName", serverName)
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
