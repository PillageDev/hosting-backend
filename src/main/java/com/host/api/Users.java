package com.host.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.WebConnection;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;

@Path("api/v1/users")
public class Users {
    private final String POCKETBASE_URL = "https://pocketbase.polarix.host/api/collections/users";

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
    public User createUser(String username, String password, String email, String discordId, @HeaderParam("AUTHORIZATION") String token) {
        if (!validOathCode(token)) {
            return null;
        }
        Client client = ClientBuilder.newClient();
        JsonObjectBuilder userBuilder = Json.createObjectBuilder()
                .add("username", username)
                .add("password", password)
                .add("passwordConfirm", password)
                .add("email", email);
        ObjectMapper mapper = new ObjectMapper();
        JsonObject response =  client.target(POCKETBASE_URL).request().post(Entity.json(userBuilder.build())).readEntity(JsonObject.class);
        User user = mapper.convertValue(response, User.class);
        user.setDiscordId(discordId);
        return user;
    }

    @POST
    @Path("/login")
    @Consumes("application/json")
    @Produces("application/json")
    public User login(String username, String password) {
        Client client = ClientBuilder.newClient();
        JsonObjectBuilder userBuilder = Json.createObjectBuilder()
                .add("username", username)
                .add("password", password);
        ObjectMapper mapper = new ObjectMapper();
        JsonObject response =  client.target(POCKETBASE_URL + "/auth-with-password").request().post(Entity.json(userBuilder.build())).readEntity(JsonObject.class);
        User user = mapper.convertValue(response.getJsonObject("record"), User.class);
        user.setToken(response.getString("token"));
        return user;
    }

    @GET
    @Path("/get/{id}")
    @Produces("application/json")
    public User getUser(@PathParam("id") String id) {
        // todo: get user from database
        return null;
    }

    @POST
    @Path("/update-username")
    @Consumes("application/json")
    @Produces("application/json")
    public User updateUser(String username, String id) {
        Client client = ClientBuilder.newClient();
        JsonObjectBuilder userBuilder = Json.createObjectBuilder()
                .add("username", username);
        ObjectMapper mapper = new ObjectMapper();
        JsonObject response =  client.target(POCKETBASE_URL + "/records/" + id).request().post(Entity.json(userBuilder.build())).readEntity(JsonObject.class);
        if (response.containsKey("code")) {
            return null;
        }
        //todo: update and return user from database
        return null;
    }

    @DELETE
    @Path("/delete/{id}")
    @Produces("application/json")
    public boolean deleteUser(@PathParam("id") String id, @HeaderParam("AUTHORIZATION") String token) {
        if (!validOathCode(token)) {
            return false;
        }
        Client client = ClientBuilder.newClient();
        JsonObject response =  client.target(POCKETBASE_URL + "/records/" + id).request().delete().readEntity(JsonObject.class);
        //todo: delete user from database
        return true;
    }

    @GET
    @Path("/get-all")
    @Produces("application/json")
    public User[] getAllUsers(@HeaderParam("AUTHORIZATION") String token) {
        if (!validOathCode(token)) {
            return null;
        }
        //todo: get all users from database
        return null;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class User {
        private String id;
        private String token;
        private String username;
        private String password;
        private String email;
        private String discordId;
        private Server[] servers;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class Server {
        private String language;
        private int maxMemory;
        private int maxCPU;
        private int maxDisk;
        private int maxBandwidth;
        private int maxDatabases;
        private PermissibleUser[] users;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Builder
    public static class PermissibleUser {
        private User user;
        private boolean console;
        // other stuff
    }
}
