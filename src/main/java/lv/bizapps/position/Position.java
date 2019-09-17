package lv.bizapps.position;

import java.util.Observable;
import java.util.UUID;
import org.joda.time.*;
import lv.bizapps.cb.rest.Order;
import lv.bizapps.positioner.utils.Utils;

public class Position extends Observable {
	public double buySum, buyPrice;
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

	public final String userBuyOrderUuid = UUID.randomUUID().toString();
	public Order buyOrder = null;

	public Position(double buySum, double buyPrice, Double sellPrice) {
		this.buySum			=	buySum;
		this.buyPrice		=	buyPrice;
		this.sellPrice		=	sellPrice;
		this.createdTime	=	new DateTime();
		this.status			=	"N";
		this.amount			=	Utils.round(buySum/buyPrice, 8);
	}
}
