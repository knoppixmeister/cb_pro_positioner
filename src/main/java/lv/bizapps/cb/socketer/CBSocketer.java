package lv.bizapps.cb.socketer;

import java.util.*;
import java.util.concurrent.*;
import com.squareup.moshi.*;
import lv.bizapps.positioner.utils.*;
import okhttp3.*;
import okhttp3.internal.ws.*;

public class CBSocketer {
	private static final String WS_URL 			=	"wss://ws-feed.pro.coinbase.com";
	private static final String SANDBOX_WS_URL	=	"wss://ws-feed-public.sandbox.pro.coinbase.com";

	private RealWebSocket webSocket = null;

	public boolean useSandbox = false;

	public final CopyOnWriteArrayList<String> PAIRS = new CopyOnWriteArrayList<>();

	private final LinkedBlockingQueue<String> CB_WS_MESSAGES_QUEUE	=	new LinkedBlockingQueue<>();
	private final MessagesParserThread messagesParserThread			=	new MessagesParserThread();

	private final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder().retryOnConnectionFailure(true)
																		.connectTimeout(10, TimeUnit.SECONDS)
																		.pingInterval(10, TimeUnit.SECONDS)
																		.build();

	private final Moshi MOSHI											=	new Moshi.Builder().build();

	private final List<TradeListener> tradeListeners					=	new CopyOnWriteArrayList<>();
	private final List<OrderReceivedListener> orderReceivedListeners	=	new CopyOnWriteArrayList<>();
	private final List<OrderDoneListener> orderDoneListeners			=	new CopyOnWriteArrayList<>();
	private final List<OrderOpenedListener> orderOpenedListeners		=	new CopyOnWriteArrayList<>();

	private final List<OrderEventListener> orderEventListeners			=	new CopyOnWriteArrayList<>();

	private final List<ConnectedListener> connectedListeners			=	new CopyOnWriteArrayList<>();
	private final List<DisconnectedListener> disconnectedListeners		=	new CopyOnWriteArrayList<>();

	private final List<SocketMessageListener> socketMessageListeners	=	new CopyOnWriteArrayList<>();

	private final JsonAdapter<Trade> matchJsonAdapter					=	MOSHI.adapter(Trade.class);
	private Trade m;

	private int reconnectCnt = -1;

	private boolean allowReconnect = true;

	public CBSocketer() {
		messagesParserThread.start();
	}

	public void addPair(String pair) {
		if(webSocket != null) {
			webSocket.send(
				"{\"type\":\"subscribe\",\"channels\":["+
				"	{\"name\":\"full\",\"product_ids\":[\""+pair+"\"]},"+
				"	{\"name\":\"heartbeat\",\"product_ids\":[\""+pair+"\"]}"+
				"]}"
			);			
		}

		if(!PAIRS.contains(pair)) PAIRS.add(pair);
	}

	public int getReconnectCnt() {
		return reconnectCnt;
	}

	public void connect() {
		Log.i("CB_SOCKETER. CONNECTING ... ");

		reconnectCnt += 1;

		if(webSocket != null) {
			allowReconnect = false;

			webSocket.close(1000, "CLOSE WS BEFORE CONNECT IF ALREADY CREATED CONNECTION");
			webSocket = null;
		}

		allowReconnect = true;

		webSocket = (RealWebSocket) HTTP_CLIENT.newWebSocket(
			new Request.Builder().url(useSandbox ? SANDBOX_WS_URL : WS_URL).build(),
			new WebSocketListener() {
				public void onOpen(final WebSocket socket, final Response response) {
					Log.i("CB_SOCKETER. CONNECTING DONE");

					try {
						Log.i("CB_WS_ON_OPEN"+(response != null ? ": "+response.body().string() : ""));
					}
					catch(Exception e) {
						e.printStackTrace();
					}

					socket.send("{\"type\":\"subscribe\",\"channels\":[{\"name\":\"status\"}]}");

					for(String pair : PAIRS) {
						socket.send(
							"{\"type\":\"subscribe\",\"channels\":["+
							"	{\"name\":\"full\",\"product_ids\":[\""+pair+"\"]},"+// BTC-EUR
							"	{\"name\":\"heartbeat\",\"product_ids\":[\""+pair+"\"]}"+
							"]}"
						);
					}

					for(ConnectedListener cl : connectedListeners) {
						cl.onConnected();
					}
				}

				public void onClosed(final WebSocket socket, final int code, final String reason) {
					Log.i("CB_WS_CLOSED. CODE: "+code+"; REASON: '"+reason+"'");

					for(DisconnectedListener dl : disconnectedListeners) {
						dl.onDisconnected();
					}

					if(allowReconnect) connect();
				}

				public void onFailure(final WebSocket socket, final Throwable t, final Response response) {
					t.printStackTrace();

					if(response != null) {
						try {
							System.out.println(response.body().string());
						}
						catch(Exception e) {
							e.printStackTrace();
						}
					}

					for(DisconnectedListener dl : disconnectedListeners) {
						dl.onDisconnected();
					}

					if(allowReconnect) connect();
				}

				public void onMessage(final WebSocket socket, final String msg) {
					CB_WS_MESSAGES_QUEUE.add(msg);

					for(SocketMessageListener socketMessageListener : socketMessageListeners) {
						socketMessageListener.onWebSocketMessage(msg);
					}
				}
			}
		);
	}

	public void addConnectedListener(ConnectedListener connectedListener) {
		connectedListeners.add(connectedListener);
	}

	public void addDisconnectedListener(DisconnectedListener disconnectedListener) {
		disconnectedListeners.add(disconnectedListener);
	}

	public void addTradeListener(TradeListener tradeListener) {
		tradeListeners.add(tradeListener);
	}

	public void addOrderReceivedListener(OrderReceivedListener orderReceivedListener) {
		orderReceivedListeners.add(orderReceivedListener);
	}

	public void addOrderDoneListener(OrderDoneListener orderDoneListener) {
		orderDoneListeners.add(orderDoneListener);
	}

	public void addOrderOpenedListener(OrderOpenedListener orderOpenedListener) {
		orderOpenedListeners.add(orderOpenedListener);
	}

	public void addOrderEventListener(OrderEventListener orderEventListener) {
		orderEventListeners.add(orderEventListener);
	}

	public void addSocketMessageListener(SocketMessageListener socketMessageListener) {
		socketMessageListeners.add(socketMessageListener);
	}

	class MessagesParserThread extends Thread {
		@Override
		public void run() {
			String msg;

			while(true) {
				try {
					msg = CB_WS_MESSAGES_QUEUE.take();

					if(msg != null && !msg.isEmpty()) parseMessage(msg);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

		private void parseMessage(String message) {
			// Log.i("ON_QUEUE_MSG: "+message);

			if(message.contains("heartbeat")) {
				// Log.i("ON_MSG_HB: "+message+"\r\n");

				return;
			}

			if(message.contains("\"type\":\"status\"")) {
				// Log.i("ON_MSG_STATUS: "+message+"\r\n");

				return;
			}

			if(message.contains("\"type\":\"open\"")) {
				// System.out.println("\r\nON_MSG_OPEN: "+message+"\r\n");

				try {
					m = matchJsonAdapter.fromJson(message);

					for(OrderOpenedListener ool : orderOpenedListeners) {
						ool.onOrderOpened(m, message);
					}

					for(OrderEventListener oel : orderEventListeners) {
						oel.onOrderEvent("open", m, message);
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}

				return;
			}

			if(message.contains("\"type\":\"done\"")) {
				// System.out.println("\r\nON_MSG_DONE: "+message+"\r\n");

				try {
					m = matchJsonAdapter.fromJson(message);

					for(OrderDoneListener odl : orderDoneListeners) {
						odl.onOrderDone(m, message);
					}

					for(OrderEventListener oel : orderEventListeners) {
						oel.onOrderEvent("done", m, message);
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}

				return;
			}

			/*
				{
					"type":			"received",
					"order_id":		"809da0c5-da95-4e4b-9cc5-c4c08c433d3d",
					"order_type":	"limit",
					"size":			"0.18000000",
					"price":		"9372.91000000",
					"side":			"buy",
					"client_oid":	"",
					"product_id":	"BTC-EUR",
					"sequence":		5644104570,
					"time":			"2019-07-19T12:19:09.348000Z"
				}
			*/
			if(message.contains("\"type\":\"received\"")) {
				// System.out.println("\r\nON_MSG_REC: "+message+"\r\n");

				try {
					m = matchJsonAdapter.fromJson(message);

					for(OrderReceivedListener orl : orderReceivedListeners) {
						orl.onOrderReceived(m, message);
					}

					for(OrderEventListener oel : orderEventListeners) {
						oel.onOrderEvent("received", m, message);
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}

				return;
			}

			if(message.contains("\"type\":\"match\"")) {
				// Log.i("\r\nON_QUEUE_MSG: "+message+"\r\n");

				try {
					m = matchJsonAdapter.fromJson(message);

					for(TradeListener tl : tradeListeners) {
						tl.onNewTrade(m, message);
					}

					for(OrderEventListener oel : orderEventListeners) {
						oel.onOrderEvent("match", m, message);
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}

				return;
			}
		}
	}
}
