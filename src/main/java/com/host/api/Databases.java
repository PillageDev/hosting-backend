package com.host.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import java.sql.DriverManager;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Path("api/v1/databases")
public class Databases {
    private final String JDBC_URL = "jdbc:mysql://localhost:3306/"; //TODO: Change to machine root ip
    private final String USERNAME = "root";
    private final String PASSWORD = "password"; //TODO: Change
    private final Connection connection;

    public Databases() {
        try {
            connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @POST
    @Path("/create")
    @Consumes("application/json")
    @Produces("application/json")
    public Map<String, String> createDatabase(@HeaderParam("token") String token, String id, String name, String connections) {
        try {
            Statement statement = connection.createStatement();
            String sql = "CREATE DATABASE " + id + "_" + name;
            statement.executeUpdate(sql);
            
            String dbUser = "user_" + id;
            String dbPassword = genPassword();
            String createUser = "CREATE USER '" + dbUser + "'@'%' IDENTIFIED BY '" + dbPassword + "'";
            statement.executeUpdate(createUser);
            String grantPerms = "GRANT ALL PRIVILEGES ON " + id + "_" + name + ".* TO '" + dbUser + "'@'%'";
            statement.executeUpdate(grantPerms);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String genPassword() {
        return UUID.randomUUID().toString().substring(0, 16);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Database {
        private String name;
        private String internalName;
        private String connections;
    }
}
