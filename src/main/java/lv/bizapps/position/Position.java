package lv.bizapps.position;

import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import org.joda.time.*;
import com.squareup.moshi.*;
import lv.bizapps.cb.rest.Order;
import lv.bizapps.cb.socketer.Trade;
import lv.bizapps.positioner.JodaDateTimeAdapter;

public class Position extends Observable {
	public final String uuid = UUID.randomUUID().toString();

	@Json(name = "product_id")
	public final String productId = "BTC-EUR";

	@Json(name="buy_price")
	public double buyPrice;

	public double amount;

	@Json(name = "sell_price")
	public Double sellPrice;

	@Json(name = "created_time")
	public DateTime createdTime = new DateTime();

	@Json(name = "executed_time")
	public DateTime executedTime = null;

	@Json(name = "completed_time")
	public DateTime completedTime = null;

	@Json(name="bought_amount")
	public double boughtAmount = 0.0;

	@Json(name="set_for_sell_amount")
	public double setForSellAmount = 0.0;

	@Json(name="sold_amount")
	public double soldAmount = 0.0;

	public String status = "N";		//	N - new, 
									//	S - submitted,
									//	R - received,
									//	PE - partially executed,
									//	E - executed,
									//	C - completed

									// BE - set buy order error
									// SE - set sell order error

	public String description = "";

	@Json(name = "buy_order_client_oid")
	public final String buyOrderClientOid = UUID.randomUUID().toString();

	@Json(name = "buy_order")
	public Order buyOrder = null;

	@Json(name = "buy_trades")
	public List<Trade> buyTrades = new CopyOnWriteArrayList<>();

	@Json(name = "sell_order_client_oid")
	public String sellOrderClientOid = "";

	@Json(name = "sell_order")
	public Order sellOrder = null;

	@Json(name = "sell_trades")
	public List<Trade> sellTrades = new CopyOnWriteArrayList<>();

	public Position() {
	}

	public Position(double buyAmount, double buyPrice, Double sellPrice) {
		this.buyPrice		=	buyPrice;
		this.sellPrice		=	sellPrice;
		this.amount			=	buyAmount;
	}

	public String toString() {
		return new Moshi.Builder().add(new JodaDateTimeAdapter()).build().adapter(Position.class).toJson(this);
	}
}
