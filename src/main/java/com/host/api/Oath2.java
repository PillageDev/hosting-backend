package com.host.api;

import java.util.UUID;

import javax.websocket.server.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("api/v1/oath2")
public class Oath2 {
    @GET
    @Path("/token/request")
    @Consumes("application/json")
    @Produces("application/json")
    public String getOathCode(@PathParam("client_id") String clientID, @PathParam("client_secret") String clientSecret) {
        String authToken = System.getenv("AUTH_TOKEN");
        String validClientID = System.getenv("CLIENT_ID");
        String validClientSecret = System.getenv("CLIENT_SECRET");
        if (validClientID.equals(clientID) && validClientSecret.equals(clientSecret)) {
            if (authToken == null) {
                return genToken();
            } else {
                return authToken;
            }
        } else {
            return null;
        }
    }

    private String genToken() {
        String random = UUID.randomUUID().toString();
        System.setProperty("AUTH_TOKEN", random);
        return UUID.randomUUID().toString();
    }
}
