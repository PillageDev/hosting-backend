package com.host.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.host.api.Users;
import lombok.SneakyThrows;
import lombok.val;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {
    private static final String JDBC_URL = "mysql://mysql:Fireship101/202@admin.polarix.host:8090/host";
    private static final Connection connection;
    static {
        try {
            connection = DriverManager.getConnection(JDBC_URL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public static void createUser(Users.User user) {
        String sql = "INSERT INTO users (id, json) VALUES (" + user.getId() + ", ?)";
        val statement = connection.prepareStatement(sql);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode userJson = mapper.convertValue(user, JsonNode.class);
        statement.setString(1, userJson.toString());
        statement.executeUpdate();
    }
}
