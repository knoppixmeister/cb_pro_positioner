package lv.bizapps.positioner;

import java.util.Arrays;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import com.squareup.moshi.*;
import lv.bizapps.cb.rest.*;
import lv.bizapps.cb.socketer.*;
import lv.bizapps.position.*;
import lv.bizapps.positioner.api.API;
import lv.bizapps.positioner.utils.*;

@SpringBootApplication
public class Application implements Observer {
	public static final LinkedBlockingQueue<String> WS_EVENTS = new LinkedBlockingQueue<>();

	public static final CBSocketer CB_SOCKETER = new CBSocketer();

	private static final Scanner SCANNER = new Scanner(System.in);

	public static final List<Position> POSITIONS = new CopyOnWriteArrayList<>();

	public static double CURRENT_PRICE = -1.0;

	public static CBRest CB_REST_API = new CBRest(API.API_KEY, API.API_PASSPHRASE, API.API_SECRET);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);

		Ticker t = CB_REST_API.getTicker("BTC-EUR");
		if(t != null) {
			CURRENT_PRICE = Utils.round(Double.parseDouble(t.price), 2);
		}

		CB_SOCKETER.connect();

		CB_SOCKETER.addPair("BTC-EUR");

		CB_SOCKETER.addTradeListener(new TradeListener() {
			@Override
			public void onNewTrade(final Trade trade, String rawData) {
				if(trade == null || trade.price == null) return;

				try {
					CURRENT_PRICE = Utils.round(Double.parseDouble(trade.price), 2);

					// reject poses w/ status Submitted (S) or Received (R) if sell price reached -------------------------------------------
					for(Position p : POSITIONS) {
						if(!p.rejectSellPriceReached) continue;
						else {
							if(
								p.sellPrice != null &&
								Double.parseDouble(trade.price) >= p.sellPrice &&
								Arrays.asList("S", "R").contains(p.status)
							)
							{
								int idx = POSITIONS.indexOf(p);
								if(
									idx != -1 &&
									p.buyOrder != null &&
									p.buyOrder instanceof Order
								)
								{
									if(CB_REST_API.cancelOrder(((Order)p.buyOrder).id)) {
										System.out.println("\r\nREJECTED NEW (S/R) POS. [ ID: "+p.uuid+" | OP: "+p.buyPrice+" | TP: "+p.sellPrice+" | ST: "+p.status+" | AM: "+p.amount+" | RPSR: "+p.rejectSellPriceReached+" | DESC: "+p.description+" ]");

										POSITIONS.remove(idx);
									}
								}
							}
						}
					}
					// --------------------------------------------------------------------------------------------------------
				}
				catch(Exception e) {
				}
			}
		});

		CB_SOCKETER.addOrderEventListener(new OrderEventListener() {
			@Override
			public void onOrderEvent(final String event, final Trade orderData, String rawData) {
				if(POSITIONS == null || POSITIONS.isEmpty()) return;

				for(Position p : POSITIONS) {
					if(Arrays.asList("PE", "E").contains(p.status.toUpperCase())) {
						;

						continue;
					}

					if(Arrays.asList("S").contains(p.status.toUpperCase())) {
						if(
							event.equals("received") &&
							p.buyOrderClientOid.equals(orderData.client_oid)
						)
						{
							System.out.println("AAA: "+orderData.client_oid);

							int idx = POSITIONS.indexOf(p);
							if(idx != -1) {
								System.out.println("IDX: "+idx);

								POSITIONS.get(idx).status = "R";
							}
						}

						continue;
					}

					if(Arrays.asList("R").contains(p.status.toUpperCase())) {
						/*
							{
								"type":				"match"
								"trade_id":			21019112
								"maker_order_id":	"28d26817-5a8a-4464-bbcc-53598067bb58"
								"taker_order_id":	"d553e0d6-953a-4845-abbf-565ecd3977c5"
								"side":				"sell"
								"size":				"0.00112322"
								"price":			"8970.00000000"
								"product_id":		"BTC-EUR"
								"sequence":			5675706208
								"time":				"2019-07-23T16:46:17.973000Z"
							}
						*/
						if(
							event.toLowerCase().equals("match") &&
							orderData.maker_order_id != null &&
							p.buyOrder != null &&
							p.buyOrder instanceof Order &&
							orderData.maker_order_id.equalsIgnoreCase(((Order)p.buyOrder).id)
						)
						{
							int idx = POSITIONS.indexOf(p);
							if(idx != -1) {
								POSITIONS.get(idx).boughtAmount += Double.parseDouble(orderData.size);

								if(POSITIONS.get(idx).boughtAmount < POSITIONS.get(idx).amount) {
									POSITIONS.get(idx).status = "PE";

									/*
									if(
										!POSITIONS.get(idx).waitFullBuy &&
										POSITIONS.get(idx).sellPrice != null &&
										POSITIONS.get(idx).sellPrice > CURRENT_PRICE &&

										Double.parseDouble(orderData.size) >= 0.001
									)
									{
										//set sell order
										Order so = CB_REST_API.openOrder(
											OrderType.LIMIT,
											OrderSide.SELL,
											p.sellPrice,
											Double.parseDouble(orderData.size),
											POSITIONS.get(idx).sellOrderClientOid
										);
										if(so != null) {
											POSITIONS.get(idx).status = "PSE";
										}
										else POSITIONS.get(idx).status = "SE";
									}
									*/
								}
								else {
									/*
									if(POSITIONS.get(idx).status.equals("PE")) {
										// cancel partial sell orders
										if(!POSITIONS.get(idx).waitFullBuy) {
											;
										}
									}
									*/

									POSITIONS.get(idx).status = "E";

									POSITIONS.get(idx).sellOrderClientOid = UUID.randomUUID().toString();

									//set sell order for full amount
									/*
									Order so = CB_REST_API.openOrder(
										OrderType.LIMIT,
										OrderSide.SELL,
										p.sellPrice,
										p.amount,
										POSITIONS.get(idx).sellOrderClientOid
									);
									if(so != null) {
										else POSITIONS.get(idx).status = "E";
									}
									else POSITIONS.get(idx).status = "SE";
									*/
								}
							}

							continue;
						}

						if(event.toLowerCase().equals("done"))
						{
							if(orderData.reason.equals("canceled")) {
								if(
									p.buyOrder != null &&
									p.buyOrder instanceof Order &&
									((Order)p.buyOrder).id.equals(orderData.order_id)
								)
								{
									int idx = POSITIONS.indexOf(p);
									if(idx != -1) {
										System.out.println("\r\nCANCELLED NEW POS. [ ID: "+p.uuid+" | OP: "+p.buyPrice+" | TP: "+p.sellPrice+" | ST: "+p.status+" | AM: "+p.amount+" | DESC: "+p.description+" ]");

										POSITIONS.remove(idx);
									}
								}

								continue;
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
						case "s":	stat();
									break;
						case "l":	System.out.println("LIST POSITIONS ...");
									listPositions();
									break;
						case "p":	System.out.println("SHOW FULL POSITION STATUS/DESCRIPTION ...");
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

	private static void stat() {
		long subPosCnt = 0;
		long recPosCnt = 0;
		long pePosCnt = 0;
		long exPosCnt  = 0;
		long cmpPosCnt = 0;
		long rejCntPos = 0;

		for(Position p : POSITIONS) {
			if(p.status.equals("S")) ++subPosCnt;
			if(p.status.equals("R")) ++recPosCnt;
			if(p.status.equals("PE")) ++pePosCnt;
			if(p.status.equals("E")) ++exPosCnt;
			if(p.status.equals("C")) ++cmpPosCnt;
			if(p.status.equals("R")) ++rejCntPos;
		}

		System.out.println(
			"----------------------------------------\r\n"+
			"CURRENT_PRICE: "+CURRENT_PRICE+"\r\n"+
			"----------------------------------------\r\n"+
			"ALL_POSes_CNT: "+Application.POSITIONS.size()+"\r\n"+
			"SUB_POS_CNT:   "+subPosCnt+"\r\n"+
			"REC_POS_CNT:   "+recPosCnt+"\r\n"+
			"PE_POS_CNT:    "+pePosCnt+"\r\n"+
			"EX_POS_CNT:    "+exPosCnt+"\r\n"+
			"CMP_POS_CNT:   "+cmpPosCnt+"\r\n"+
			"REJ_POS_CNT:   "+rejCntPos+"\r\n"+
			"----------------------------------------\r\n"
		);
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
				System.out.println(idx+"] ID: "+p.uuid+" | OP: "+p.buyPrice+" | TP: "+(p.sellPrice != null ? p.sellPrice : "<not set>")+" | AM: "+p.amount+" | ST: "+p.status+" | RSPR: "+p.rejectSellPriceReached+" | DESC: "+p.description);
			}
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if(o != null && o instanceof Position) {
			
		}
	}
}
