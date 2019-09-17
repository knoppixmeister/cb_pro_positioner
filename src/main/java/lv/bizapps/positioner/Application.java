package lv.bizapps.positioner;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lv.bizapps.cb.rest.CBRest;
import lv.bizapps.cb.rest.Ticker;
import lv.bizapps.cb.socketer.CBSocketer;
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
				System.out.println("TR: "+rawData);

				try {
					CURRENT_PRICE = Utils.round(Double.parseDouble(trade.price), 2);
				}
				catch(Exception e) {
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
						case "q":	
									break;
					}
				}
			}
		}
		catch(Exception e) {
		}
	}

	private static void listPositions() {
		int idx = 1;
		if(POSITIONS == null || POSITIONS.isEmpty()) {
			System.out.println("<NO POSITIONS>");
		}
		else {
			for(Position p : POSITIONS) {
				System.out.println(idx+"] "+p.buyPrice);
			}
		}
	}
}
