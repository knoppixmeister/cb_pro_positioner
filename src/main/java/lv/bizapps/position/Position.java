package lv.bizapps.position;

import java.util.List;
import java.util.Observable;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.joda.time.*;
import com.squareup.moshi.Moshi;
import lv.bizapps.cb.rest.Order;
import lv.bizapps.cb.socketer.Trade;

public class Position extends Observable {
	public final String uuid = UUID.randomUUID().toString();

	public double buyPrice;
	public double amount;
	public Double sellPrice;
	public DateTime createdTime, executedTime, completedTime;

	public double	boughtAmount = 0.0,
					setForSellAmount = 0.0,
					soldAmount = 0.0;

	public String status;			//	N - new, 
									//	S - submitted,
									//	R - received,
									//	PE - partially executed,
									//	E - executed,
									//	C - completed

									// BE - set buy order error
									// SE - set sell order error

	public String description = "";

	public final String buyOrderClientOid = UUID.randomUUID().toString();
	public Order buyOrder;
	public List<Trade> buyTrades = new CopyOnWriteArrayList<>();

	public String sellOrderClientOid = UUID.randomUUID().toString();
	public Order sellOrder;
	public List<Trade> sellTrades = new CopyOnWriteArrayList<>();

	public Position(double buyAmount, double buyPrice, Double sellPrice) {
		this.buyPrice		=	buyPrice;
		this.sellPrice		=	sellPrice;
		this.createdTime	=	new DateTime();
		this.status			=	"N";
		this.amount			=	buyAmount;
	}

	public String toString() {
		return new Moshi.Builder().build().adapter(Position.class).toJson(this);
	}
}
