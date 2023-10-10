package com.catanai.server.config;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Handles game WebSocket messages.
 */
@Component
public class GameSocketHandler extends TextWebSocketHandler {
  List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
  Map<String, SocketCommandHandler> sessionCommandHandlerMap = new HashMap<>();


  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message)
      throws InterruptedException, IOException {
    SocketCommandHandler commandHandler = sessionCommandHandlerMap.get(session.getId());
    var value = new Gson().fromJson(message.getPayload(), Map.class);
    String command = value.get("command").toString();
    String action = value.get("action") == null ? null : value.get("action").toString();
    String playerID = value.get("playerID") == null ? null : value.get("playerID").toString();
    session.sendMessage(new TextMessage(commandHandler.handleCommand(command, action, playerID)));
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    sessions.add(session);
    sessionCommandHandlerMap.put(session.getId(), new SocketCommandHandler());
  }
}
