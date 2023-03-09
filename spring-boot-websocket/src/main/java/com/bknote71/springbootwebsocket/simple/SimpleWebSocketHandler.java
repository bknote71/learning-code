package com.bknote71.springbootwebsocket.simple;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseDataSource;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SimpleWebSocketHandler extends TextWebSocketHandler {

    // 동시 접근이 가능한 room
    private final Map<String, List<WebSocketSession>> rooms = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("after connection established");
        String roomName = getRoomName(session);
        log.info("room name={}", roomName);
        log.info("session id={}", session.getId());
        List<WebSocketSession> room = rooms.get(roomName);
        if (room == null) {
            rooms.put(roomName, new ArrayList<>());
        } else {
            room.add(session);
        }
        // 방에 입장하였습니다.
        TextMessage message = new TextMessage(session.getId() + "님이 방에 입장하였습니다.");
        sendMessage(roomName, message);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        sendMessage(getRoomName(session), message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("session id={}", session.getId());
        super.handleTransportError(session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("session id={}", session.getId());
        String roomName = getRoomName(session);
        List<WebSocketSession> room = rooms.get(roomName);
        room.remove(session);
        TextMessage message = new TextMessage(session.getId() + "님이 퇴장하였습니다.");
        sendMessage(roomName, message);
    }

    public String getRoomName(WebSocketSession session) {
        URI uri = session.getUri();
        String path = uri.getPath();
        return path.split("/room/")[1];
    }

    public void sendMessage(String roomName, TextMessage message) throws IOException {
        List<WebSocketSession> room = rooms.get(roomName);
        for (WebSocketSession receiver : room) {
            if (receiver != null && receiver.isOpen()) {
                receiver.sendMessage(message);
            }
        }
    }
}
