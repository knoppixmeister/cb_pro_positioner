package lv.bizapps.positioner;

import com.squareup.moshi.Json;

public class RequestPosition {
	@Json(name="buy_order_client_oid")
	public String buyOrderClientOid;

	@Json(name="buy_order_type")
	public String buyOrderType = "limit";// "market"

	@Json(name="buy_order_amount")
	public double buyOrderAmount;

	@Json(name="buy_order_price")
	public double buyOrderPrice;

	public boolean setOrdersByPostOnly = true;
	public boolean setOrdersUntilAccepted = false;

	public double setUntilAcceptedPriceStep = 0.01;
}
