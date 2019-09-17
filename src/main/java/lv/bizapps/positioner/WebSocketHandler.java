package lv.bizapps.positioner;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.*;

public class WebSocketHandler extends TextWebSocketHandler {
	private List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessions.add(session);
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		session.sendMessage(new TextMessage("{\"status\":\"ok\",\"message\":\"accepted\"}"));
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		sessions.remove(session);
	}	
}
