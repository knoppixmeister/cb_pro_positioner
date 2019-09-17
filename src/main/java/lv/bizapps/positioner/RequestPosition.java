package lv.bizapps.positioner;

import com.squareup.moshi.Json;

public class RequestPosition {
	@Json(name="api_key")
	public String apiKey;

	@Json(name="api_key")
	public String apiSecrtet;

	@Json(name="api_passphrase")
	public String apiPassphrase;

	// -------------------------------------------------------------------------------

	@Json(name="buy_order_client_oid")
	public String buyOrderClientOid;

	@Json(name="buy_order_type")
	public String buyOrderType = "limit";// "market"

	@Json(name="buy_order_amount")
	public double buyOrderAmount;

	@Json(name="buy_order_price")
	public double buyOrderPrice;

	@Json(name="set_sell_after_buy")
	public boolean setSellAfterBuy = true;

	@Json(name="sell_price")
	public double sellPrice;

	@Json(name="sell_same_amount")
	public boolean sellSameAmount = true;

	@Json(name="sell_amount")
	public double sellAmount;

	public boolean setOrdersByPostOnly = true;
	public boolean setOrdersUntilAccepted = false;

	public double setUntilAcceptedPriceStep = 0.01;
}
