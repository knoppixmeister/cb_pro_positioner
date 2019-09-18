package lv.bizapps.positioner;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.squareup.moshi.Moshi;

import lv.bizapps.cb.rest.CBRest;
import lv.bizapps.cb.rest.Ticker;
import lv.bizapps.cb.socketer.CBSocketer;
import lv.bizapps.cb.socketer.OrderEventListener;
import lv.bizapps.cb.socketer.Trade;
import lv.bizapps.cb.socketer.TradeListener;
import lv.bizapps.position.Position;
import lv.bizapps.positioner.utils.Log;
import lv.bizapps.positioner.utils.Utils;

@SpringBootApplication
public class Application {
	public static final CBSocketer CB_SOCKETER = new CBSocketer();

	private static final Scanner SCANNER = new Scanner(System.in);

	public static final List<Position> POSITIONS = new CopyOnWriteArrayList<>();

	public static double CURRENT_PRICE = -1.0;

	public static void main(String[] args) {	
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					Thread.sleep(200);

					System.out.println("------------------------------------------------\r\nGET OUT OF APP PROC\r\n-------------------------");

					SCANNER.close();

	        		Thread.sleep(200);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});

		SpringApplication.run(Application.class, args);

		CBRest nonAuthCbRest = new CBRest();

		Ticker t = nonAuthCbRest.getTicker("BTC-EUR");
		if(t != null) {
			CURRENT_PRICE = Utils.round(Double.parseDouble(t.price), 2);
		}

		CB_SOCKETER.connect();

		CB_SOCKETER.addPair("BTC-EUR");

		CB_SOCKETER.addTradeListener(new TradeListener() {
			@Override
			public void onNewTrade(Trade trade, String rawData) {
				try {
					CURRENT_PRICE = Utils.round(Double.parseDouble(trade.price), 2);
				}
				catch(Exception e) {
				}
			}
		});

		CB_SOCKETER.addOrderEventListener(new OrderEventListener() {
			@Override
			public void onOrderEvent(String event, Trade orderData, String rawData) {
				if(POSITIONS == null || POSITIONS.isEmpty()) return;

				for(Position p : POSITIONS) {
					if(Arrays.asList("PE", "E").contains(p.status)) {
						;
					}
					else if(Arrays.asList("S", "R").contains(p.status)) {
						if(
							event.equals("received") &&
							p.status.equals("S") &&
							p.buyOrderClientOid.equals(orderData.client_oid)
						)
						{
							int idx = POSITIONS.indexOf(p);
							if(idx != -1) {
								POSITIONS.get(idx).status = "R";
							}
						}
					}
				}
			}
		});

		String cmd;
		try {
			while(true) {
				System.out.print(">_: ");
				cmd = SCANNER.nextLine();

				if(cmd != null && !cmd.equals("")) {
					cmd = cmd.trim();

					switch(cmd) {
						case "l":	Log.i("LIST POSITIONS ...");
									listPositions();
									break;
						case "p":	Log.i("SHOW FULL POSITION STATUS/DESCRIPTION ...");
									showPosition();
									break;
						case "q":	
									break;
					}
				}
			}
		}
		catch(Exception e) {
		}
	}

	private static void showPosition() {
		String posId;

		System.out.print("ENTER POSITION ID: ");
		posId = SCANNER.nextLine().trim();

		if(!Utils.isUUID(posId)) {
			Log.i("WRONG POSITION ID(UUID): "+posId);
			return;
		}

		Position p = null;
		for(Position p1 : POSITIONS) {
			if(p1.uuid.equalsIgnoreCase(posId)) {
				p = p1;
				break;
			}
		}

		if(p == null) {
			Log.i("POSITION NOT FOUND: "+posId);
			return;
		}

		System.out.println(new Moshi.Builder().add(new JodaDateTimeAdapter()).build().adapter(Position.class).toJson(p)+"\r\n");
	}

	private static void listPositions() {
		int idx = 1;

		if(POSITIONS == null || POSITIONS.isEmpty()) System.out.println("<NO POSITIONS>");
		else {
			for(Position p : POSITIONS) {
				System.out.println(idx+"] ID: "+p.uuid+" | OP: "+p.buyPrice+" | TP: "+(p.sellPrice != null ? p.sellPrice : "<not set>")+" | AM: "+p.amount+" | ST: "+p.status+" | DESC: "+p.description);
			}
		}
	}
}
