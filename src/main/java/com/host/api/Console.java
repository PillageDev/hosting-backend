package com.host.api;

import java.io.IOException;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("api/v1/console")
public class Console {
    private Session session;
    
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Opened!");
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Message: " + message);
    }
    
    @OnClose
    public void onClose() {
        System.out.println("Closed!");
    }


    public void sendMessage(String message) {
        try {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
            } else {
                System.out.println("Session is null or closed!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
