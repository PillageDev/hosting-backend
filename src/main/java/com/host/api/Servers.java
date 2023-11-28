package com.host.api;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.websocket.server.PathParam;
import javax.ws.rs.*;

import com.host.utils.Docker;
import com.host.api.Users.User;

import com.host.utils.Scheduler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;
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
    public Users.User createServer(String id, String name, int maxMemory, int maxCpu, int maxDisk, int maxBandwidth, String language, @HeaderParam("AUTHORIZATION") String token) {
        if (!validOathCode(token)) {
            return null;
        }
        String serverId = UUID.randomUUID().toString().substring(0, 16);
        Docker.createServer(serverId, language, maxMemory, maxCpu, maxDisk, maxBandwidth);

    }
}
