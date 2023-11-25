package com.host.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import com.google.common.net.HttpHeaders;
import com.host.api.Servers;
import com.host.api.Servers.Server;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Pocketbase {
    public static List<User> users = new ArrayList<>();
    public static Map<String, User> tokens = new HashMap<>(); // Querying by token 
    private static final Client CLIENT = ClientBuilder.newClient();
    private static final String BASE_URL = "https://host-pocketbase.nepahq.easypanel.host";

    public static User getUser(String token) {
        if (tokens.containsKey(token)) {
            return tokens.get(token);
        } else {
            return null;
        }
    }

    @GET
    @Path("/api/v1/users/authenticate")
    @Consumes("application/json")
    @Produces("application/json")
    public static User authenticate(String identity, String password) {
        WebTarget target = CLIENT.target(BASE_URL + "/api/collections/users/auth-with-password");
        String jsonPayload = "{\"username\": \"" + identity + "\", \"password\": \"" + password + "\"}";
        JsonObject response = target.request().post(Entity.json(jsonPayload), JsonObject.class);
        if (response.containsKey("code")) {
            return null;
        }
        return User.builder()
            .token(response.getString("token"))
            .record(User.Record.builder()
                .id(response.getJsonObject("record").getString("id"))
                .collectionId(response.getJsonObject("record").getString("collectionId"))
                .collectionName(response.getJsonObject("record").getString("collectionName"))
                .created(response.getJsonObject("record").getString("created"))
                .updated(response.getJsonObject("record").getString("updated"))
                .username(response.getJsonObject("record").getString("username"))
                .verified(response.getJsonObject("record").getBoolean("verified"))
                .emailVisibility(response.getJsonObject("record").getBoolean("emailVisibility"))
                .email(response.getJsonObject("record").getString("email"))
                .discordAvatarUrl(response.getJsonObject("record").getString("discordAvatarUrl"))
                .discordId(response.getJsonObject("record").getString("discordId"))
                .server(Server.builder()
                    .serverName(response.getJsonObject("record").getJsonObject("server").getString("id"))
                    .language(response.getJsonObject("record").getJsonObject("server").getString("language"))
                    .maxMemory(response.getJsonObject("record").getJsonObject("server").getInt("maxMemory"))
                    .maxDisk(response.getJsonObject("record").getJsonObject("server").getInt("maxDisk"))
                    .maxCPU(response.getJsonObject("record").getJsonObject("server").getInt("maxCPU"))
                    .maxNetwork(response.getJsonObject("record").getJsonObject("server").getInt("maxNetwork"))
                    .database(response.getJsonObject("record").getJsonObject("server").getBoolean("database"))
                    .build())
                .build())
            .build();
    }

    @GET
    @Path("/api/v1/users/refresh")
    @Consumes("application/json")
    @Produces("application/json")
    public static User refresh(String token) {
        WebTarget target = CLIENT.target(BASE_URL + "/api/collections/users/auth-refresh");
        JsonObject response = target.request().header(HttpHeaders.AUTHORIZATION, "Bearer " + token).post(Entity.json(""), JsonObject.class);
        if (response.containsKey("code")) {
            if (response.getInt("code") == 401) {
                System.err.println("Token not valid.");
            } else if (response.getInt("code") == 403) {
                System.err.println("Token not allowed to perform action.");
            } else if (response.getInt("code") == 404) {
                System.err.println("Missing auth header.");
            } else {
                System.err.println("Unknown error.");
            }
            return null;
        }   
        return User.builder()
            .token(response.getString("token"))
            .record(User.Record.builder()
                .id(response.getJsonObject("record").getString("id"))
                .collectionId(response.getJsonObject("record").getString("collectionId"))
                .collectionName(response.getJsonObject("record").getString("collectionName"))
                .created(response.getJsonObject("record").getString("created"))
                .updated(response.getJsonObject("record").getString("updated"))
                .username(response.getJsonObject("record").getString("username"))
                .verified(response.getJsonObject("record").getBoolean("verified"))
                .emailVisibility(response.getJsonObject("record").getBoolean("emailVisibility"))
                .email(response.getJsonObject("record").getString("email"))
                .discordAvatarUrl(response.getJsonObject("record").getString("discordAvatarUrl"))
                .discordId(response.getJsonObject("record").getString("discordId"))
                .server(Server.builder()
                    .serverName(response.getJsonObject("record").getJsonObject("server").getString("id"))
                    .language(response.getJsonObject("record").getJsonObject("server").getString("language"))
                    .maxMemory(response.getJsonObject("record").getJsonObject("server").getInt("maxMemory"))
                    .maxDisk(response.getJsonObject("record").getJsonObject("server").getInt("maxDisk"))
                    .maxCPU(response.getJsonObject("record").getJsonObject("server").getInt("maxCPU"))
                    .maxNetwork(response.getJsonObject("record").getJsonObject("server").getInt("maxNetwork"))
                    .database(response.getJsonObject("record").getJsonObject("server").getBoolean("database"))
                    .build())
                .build())
            .build();      
    }

    @GET
    @Path("/api/v1/users/request-verification")
    @Consumes("application/json")
    @Produces("application/json")
    public static boolean requestVerification(String token) {
        String email = tokens.get(token).getRecord().getEmail();
        WebTarget target = CLIENT.target(BASE_URL + "/api/collections/users/request-verification");
        JsonObject response = target.request().post(Entity.json("{\"email\": \"" + email + "\"}"), JsonObject.class);
        if (response == null) {
            return true;
        } else {
            return false;
        }
    }

    @GET
    @Path("/api/v1/users/verify")
    @Consumes("application/json")
    @Produces("application/json")
    public static boolean verify(String token, String code) {
        WebTarget target = CLIENT.target(BASE_URL + "/api/collections/users/confirm-verification");
        JsonObject response = target.request().post(Entity.json("{\"token\": \"" + code + "\"}"), JsonObject.class);
        if (response == null) {
            User user = tokens.get(token);
            user.getRecord().setVerified(true);
            return true;
        } else {
            return false;
        }
    }

    @GET
    @Path("/api/v1/users/request-password-reset")
    @Consumes("application/json")
    @Produces("application/json")
    public static boolean requestPasswordReset(String token) {
        WebTarget target = CLIENT.target(BASE_URL + "/api/collections/users/request-password-reset");
        String email = tokens.get(token).getRecord().getEmail();
        JsonObject response = target.request().post(Entity.json("{\"email\": \"" + email + "\"}"), JsonObject.class);
        if (response == null) {
            return true;
        } else {
            return false;
        }
    }

    @GET
    @Path("/api/v1/users/reset-password")
    @Consumes("application/json")
    @Produces("application/json")
    public static boolean resetPassword(String code, String newPassword) {
        WebTarget target = CLIENT.target(BASE_URL + "/api/collections/users/confirm-password-reset");
        JsonObject response = target.request().post(Entity.json("{\"token\": \"" + code + "\", \"password\": \"" + newPassword + "\", \"passwordConfirm\": \"" + newPassword + "\"}"), JsonObject.class);
        if (response == null) {
            return true;
        } else {
            return false;
        }
    }

    @GET
    @Path("/api/v1/users/request-email-change")
    @Consumes("application/json")
    @Produces("application/json")
    public static boolean requestEmailChange(String token, String newEmail) {
        WebTarget target = CLIENT.target(BASE_URL + "/api/collections/users/request-email-change");
        JsonObject response = target.request().header(HttpHeaders.AUTHORIZATION, "Bearer " + token).post(Entity.json("{\"newEmail\": \"" + newEmail + "\"}"), JsonObject.class);
        if (response == null) {
            return true;
        } else {
            return false;
        }
    }

    @GET
    @Path("/api/v1/users/confirm-email-change")
    @Consumes("application/json")
    @Produces("application/json")
    public static boolean changeEmail(String token, String code, String password, String email) {
        WebTarget target = CLIENT.target(BASE_URL + "/api/collections/users/confirm-email-change");
        JsonObject response = target.request().post(Entity.json("{\"token\": \"" + code + "\", \"password\": \"" + password + "\"}"), JsonObject.class);
        if (response == null) {
            User user = tokens.get(token);
            user.getRecord().setEmail(email);
            return true;
        } else {
            return false;
        }
    }

    @GET
    @Path("/api/v1/users/create")
    @Consumes("application/json")
    @Produces("application/json")
    public static User.Record create(String username, String email, String password, String discordAvatarUrl, String discordId) {
        WebTarget target = CLIENT.target(BASE_URL + "/api/collections/users/records");
        String payload = "{\"username\": \"" + username + "\", \"email\": \"" + email + "\", \"password\": \"" + password + "\", \"passwordConfirm\": \"" + password + "\", \"discord_avatar_url\": \"" + discordAvatarUrl + "\", \"discord_id\": \"" + discordId + "\"}";
        JsonObject response = target.request().post(Entity.json(payload), JsonObject.class);
        if (response.containsKey("code")) {
            return null;
        }
        return User.Record.builder()
            .id(response.getString("id"))
            .collectionId(response.getString("collectionId"))
            .collectionName(response.getString("collectionName"))
            .created(response.getString("created"))
            .updated(response.getString("updated"))
            .username(response.getString("username"))
            .verified(response.getBoolean("verified"))
            .emailVisibility(response.getBoolean("emailVisibility"))
            .email(response.getString("email"))
            .discordAvatarUrl(response.getString("discordAvatarUrl"))
            .discordId(response.getString("discordId"))
            .server(Server.builder()
                .serverName(response.getJsonObject("server").getString("id"))
                .language(response.getJsonObject("server").getString("language"))
                .maxMemory(response.getJsonObject("server").getInt("maxMemory"))
                .maxDisk(response.getJsonObject("server").getInt("maxDisk"))
                .maxCPU(response.getJsonObject("server").getInt("maxCPU"))
                .maxNetwork(response.getJsonObject("server").getInt("maxNetwork"))
                .database(response.getJsonObject("server").getBoolean("database"))
                .build())
            .build();
    }

    @GET
    @Path("/api/v1/users/update/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    public static User.Record update(String token, @HeaderParam("id") String id, String username, String email, String password, String discordAvatarUrl, String discordId, Servers.Server server) {
        WebTarget target = CLIENT.target(BASE_URL + "/api/collections/users/records/" + id);
        String payload = "";
        if (server == null) {
            payload = "{\"username\": \"" + username + "\", \"email\": \"" + email + "\", \"password\": \"" + password + "\", \"passwordConfirm\": \"" + password + "\", \"discord_avatar_url\": \"" + discordAvatarUrl + "\", \"discord_id\": \"" + discordId + "\"}";
        } else {
            payload = "{\"username\": \"" + username + "\", \"email\": \"" + email + "\", \"password\": \"" + password + "\", \"passwordConfirm\": \"" + password + "\", \"discord_avatar_url\": \"" + discordAvatarUrl + "\", \"discord_id\": \"" + discordId + "\", \"server\": {\"id\": \"" + server.getServerName() + "\", \"language\": \"" + server.getLanguage() + "\", \"maxMemory\": " + server.getMaxMemory() + ", \"maxDisk\": " + server.getMaxDisk() + ", \"maxCPU\": " + server.getMaxCPU() + ", \"maxNetwork\": " + server.getMaxNetwork() + ", \"database\": " + server.isDatabase() + "}}";
        }
        JsonObject response = target.request().method("PATCH", Entity.json(payload), JsonObject.class);
        if (response.containsKey("code")) {
            return null;
        }
        return User.Record.builder()
            .id(response.getString("id"))
            .collectionId(response.getString("collectionId"))
            .collectionName(response.getString("collectionName"))
            .created(response.getString("created"))
            .updated(response.getString("updated"))
            .username(response.getString("username"))
            .verified(response.getBoolean("verified"))
            .emailVisibility(response.getBoolean("emailVisibility"))
            .email(response.getString("email"))
            .discordAvatarUrl(response.getString("discordAvatarUrl"))
            .discordId(response.getString("discordId"))
            .server(Server.builder()
                .serverName(response.getJsonObject("server").getString("id"))
                .language(response.getJsonObject("server").getString("language"))
                .maxMemory(response.getJsonObject("server").getInt("maxMemory"))
                .maxDisk(response.getJsonObject("server").getInt("maxDisk"))
                .maxCPU(response.getJsonObject("server").getInt("maxCPU"))
                .maxNetwork(response.getJsonObject("server").getInt("maxNetwork"))
                .database(response.getJsonObject("server").getBoolean("database"))
                .build())
            .build();
    }

    @GET
    @Path("/api/v1/users/delete/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    public static boolean delete(String token, @HeaderParam("id") String id) {
        WebTarget target = CLIENT.target(BASE_URL + "/api/collections/users/records/" + id);
        JsonObject response = target.request().delete(JsonObject.class);
        if (response == null) {
            return true;
        } else {
            return false;
        }
    }

    @GET
    @Path("/api/v1/users/get/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    public static User.Record get(@HeaderParam("id") String id) {
        WebTarget target = CLIENT.target(BASE_URL + "/api/collections/users/records/" + id);
        JsonObject response = target.request().get(JsonObject.class);
        if (response.containsKey("code")) {
            return null;
        }
        return User.Record.builder()
            .id(response.getString("id"))
            .collectionId(response.getString("collectionId"))
            .collectionName(response.getString("collectionName"))
            .created(response.getString("created"))
            .updated(response.getString("updated"))
            .username(response.getString("username"))
            .verified(response.getBoolean("verified"))
            .emailVisibility(response.getBoolean("emailVisibility"))
            .email(response.getString("email"))
            .discordAvatarUrl(response.getString("discordAvatarUrl"))
            .discordId(response.getString("discordId"))
            .server(Server.builder()
                .serverName(response.getJsonObject("server").getString("id"))
                .language(response.getJsonObject("server").getString("language"))
                .maxMemory(response.getJsonObject("server").getInt("maxMemory"))
                .maxDisk(response.getJsonObject("server").getInt("maxDisk"))
                .maxCPU(response.getJsonObject("server").getInt("maxCPU"))
                .maxNetwork(response.getJsonObject("server").getInt("maxNetwork"))
                .database(response.getJsonObject("server").getBoolean("database"))
                .build())
            .build();
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    public static class User {
        private String token;
        private Record record;

        @Getter
        @Setter
        @Builder
        @AllArgsConstructor
        public static class Record {
            private String id;
            private String collectionId;
            private String collectionName;
            private String created;
            private String updated;
            private String username;
            private boolean verified;
            private boolean emailVisibility;
            private String discordAvatarUrl;
            private String email;
            private String discordId;
            private Servers.Server server;

            public JsonObject toJson() {
                JsonObjectBuilder builder = Json.createObjectBuilder()
                    .add("id", id)
                    .add("collectionId", collectionId)
                    .add("collectionName", collectionName)
                    .add("created", created)
                    .add("updated", updated)
                    .add("username", username)
                    .add("verified", verified)
                    .add("email", email)
                    .add("emailVisibility", emailVisibility)
                    .add("discordAvatarUrl", discordAvatarUrl)
                    .add("discordId", discordId)
                    .add("server", server.toJson());
                return builder.build();
            }
        }

        public JsonObject toJson() {
            JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("token", token)
                .add("record", record.toJson());
            return builder.build();
        }
    }
}
