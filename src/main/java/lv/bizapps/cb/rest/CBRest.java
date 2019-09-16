package lv.bizapps.cb.rest;

import java.time.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import com.squareup.moshi.*;
import lv.bizapps.positioner.utils.Utils;
import okhttp3.*;

public class CBRest {
	public static enum OrderType {
		LIMIT,
		MARKET
	}
	public static enum OrderSide {
		BUY,
		SELL
	}

	private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

	public static final String REST_API_BASE_URL 			= "https://api.pro.coinbase.com";
	public static final String SANDBOX_REST_API_BASE_URL	= "https://api-public.sandbox.pro.coinbase.com";

	public boolean useSanboxApi = false;

	private String apiKey, apiSecret, apiPassword;

	private final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder().retryOnConnectionFailure(true)
																		.pingInterval(10, TimeUnit.SECONDS)
																		.connectTimeout(10, TimeUnit.SECONDS)
																		.build();

	public CBRest(String apiKey, String password, String secret) {
		this.apiKey			= apiKey;
		this.apiPassword	= password;
		this.apiSecret		= secret;
	}

	public CBRest(String apiKey, String password, String secret, boolean useSandboxApi) {
		this(apiKey, password, secret);
		this.useSanboxApi = useSandboxApi;
	}

	public Order getOrder(String id) {
		List<Order> orders = orders(id);

		return	orders == null || orders.isEmpty() ? null :
				orders.get(0);
	}

	public List<Order> getOrders() {
		return orders(null);
	}
	
	public List<Order> getOrders(String id) {
		return orders(id);
	}

	@SuppressWarnings("unchecked")
	public List<Order> orders(String id) {
		try {
			//if(id != null && !id.isEmpty() && !Utils.isUUID(id)) return null;

			final long TS = Instant.now().getEpochSecond();

			final String requestPath = "/orders/";
													//+(id != null && !id.isEmpty() ? "/"+id : "");

			final Request request = new Request.Builder().url((this.useSanboxApi ? CBRest.SANDBOX_REST_API_BASE_URL : CBRest.REST_API_BASE_URL)+requestPath)
															.addHeader("CB-ACCESS-KEY", this.apiKey)
															.addHeader("CB-ACCESS-SIGN", Utils.signGenerate(this.apiSecret, requestPath, "GET", "", TS+""))
															.addHeader("CB-ACCESS-TIMESTAMP", TS+"")
															.addHeader("CB-ACCESS-PASSPHRASE", this.apiPassword)
															.build();
			final Response response = HTTP_CLIENT.newCall(request).execute();			
			if(response != null) {
				if(response.isSuccessful()) {
					final String json = response.body().string();
					if(json != null && !json.isEmpty()) {
						//System.out.println("\r\nRESP_JSON: "+json+"\r\n");

						if(id != null && !id.isEmpty()) {
							return Arrays.asList(new Moshi.Builder().build().adapter(Order.class).fromJson(json));
						}
						else {
							return (List<Order>)new Moshi.Builder().build().adapter(Types.newParameterizedType(List.class, Order.class)).fromJson(json);
						}
					}
					//else System.out.println("<EMPTY_RESPONSE_JSON>");
				}
				else {
					System.out.println("\r\nRESPONSE_ERROR\r\n");

					System.out.println(response.code()+"|"+response.message());
					System.out.println(response.body().string());
				}

				response.close();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public Order openOrder(OrderType type, OrderSide side, double price, double amount) {
		return openOrder(type, side, price, amount, null);
	}

	@SuppressWarnings("deprecation")
	public Order openOrder(OrderType type, OrderSide side, double price, double amount, String uuid) {
		/*
		if(amount < 0.001) {
			System.err.println("Order amount lower than min. allowed 0.001 BTC. Please increase buy sum at least to "+Utils.round((price*0.001/1), 2)+" EUR");

			return null;
		}
		*/

		//!Utils.isUUID(uuid)
		if(uuid == null || uuid.isEmpty()) uuid = "";
		else uuid = "\"client_oid\":\""+uuid+"\",";
													//UUID.randomUUID().toString();

		try {
			final long TS = Instant.now().getEpochSecond();

			final String requestPath	=	"/orders";

			final String body			=	"{"+uuid+"\"type\":\""+(type == OrderType.LIMIT ? "limit" : "market")+"\",\"size\":\""+amount+"\",\"price\":\""+price+"\",\"side\":\""+(side == OrderSide.BUY ? "buy" : "sell")+"\",\"product_id\":\"BTC-EUR\",\"post_only\":\"false\",\"time_in_force\":\"GTC\"}";

			final Request request = new Request.Builder().url((this.useSanboxApi ? CBRest.SANDBOX_REST_API_BASE_URL : CBRest.REST_API_BASE_URL)+requestPath)
															.addHeader("CB-ACCESS-KEY", this.apiKey)
															.addHeader("CB-ACCESS-SIGN", Utils.signGenerate(this.apiSecret, requestPath, "POST", body, TS+""))
															.addHeader("CB-ACCESS-TIMESTAMP", TS+"")
															.addHeader("CB-ACCESS-PASSPHRASE", this.apiPassword)
															.post(RequestBody.create(CBRest.MEDIA_TYPE_JSON, body))
															.build();
			final Response response = HTTP_CLIENT.newCall(request).execute();
			if(response != null) {
				if(response.isSuccessful()) {
					final String json = response.body().string();
					if(json != null && !json.isEmpty()) {
						System.out.println("\r\n"+json+"\r\n");

						return new Moshi.Builder().build().adapter(Order.class).fromJson(json);
					}
					//else System.out.println("<EMPTY_RESPONSE_JSON>");
				}
				else {
					System.out.println("\r\nRESPONSE_ERROR\r\n");

					System.out.println(response.code()+"|"+response.message());
					System.out.println(response.body().string());
				}

				response.close();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	public boolean cancelOrder(String id) {
		boolean result = false;

		// if(id == null || id.isEmpty()) return result;
		// !utils.Utils.isUUID(id)

		try {
			final long TS = Instant.now().getEpochSecond();

			final String requestPath = "/orders/"+id;

			final Request request = new Request.Builder().url((this.useSanboxApi ? CBRest.SANDBOX_REST_API_BASE_URL : CBRest.REST_API_BASE_URL)+requestPath)
															.addHeader("CB-ACCESS-KEY", this.apiKey)
															.addHeader("CB-ACCESS-SIGN", Utils.signGenerate(this.apiSecret, requestPath, "DELETE", "", TS+""))
															.addHeader("CB-ACCESS-TIMESTAMP", TS+"")
															.addHeader("CB-ACCESS-PASSPHRASE", this.apiPassword)
															.delete(RequestBody.create(CBRest.MEDIA_TYPE_JSON, ""))
															.build();
			final Response response = HTTP_CLIENT.newCall(request).execute();
			if(response != null) {
				if(response.isSuccessful()) {
					final String json = response.body().string();
					if(json != null && !json.isEmpty()) {
						System.out.println("\r\nRESP_JSON: "+json+"\r\n");

						final List<String> res = (List<String>) new Moshi.Builder().build().adapter(Types.newParameterizedType(List.class, String.class)).fromJson(json);
						if(res != null && res.size() == 1 && res.get(0).equals(id)) result = true;
					}
				}
				else {
					System.out.println("\r\nRESPONSE_ERROR\r\n");

					System.out.println(response.code()+"|"+response.message());
					System.out.println(response.body().string());
				}

				response.close();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Fill> getFills(String productId) {
		if(productId == null || productId.isEmpty()) return null;

		final String requestPath = "/fills?product_id="+productId;

		try {
			final long TS = Instant.now().getEpochSecond();

			final Request request = new Request.Builder().url((this.useSanboxApi ? CBRest.SANDBOX_REST_API_BASE_URL : REST_API_BASE_URL)+requestPath)
															.addHeader("CB-ACCESS-KEY", this.apiKey)
															.addHeader("CB-ACCESS-SIGN", Utils.signGenerate(this.apiSecret, requestPath, "GET", "", TS+""))
															.addHeader("CB-ACCESS-TIMESTAMP", TS+"")
															.addHeader("CB-ACCESS-PASSPHRASE", this.apiPassword)
															.build();

			final Response response = HTTP_CLIENT.newCall(request).execute();
			if(response != null) {
				if(response.isSuccessful()) {
					final String json = response.body().string();

					//System.out.println(json.replaceAll("},", "}\r\n").replaceAll("\\[", "")+"\r\n");

					if(json != null && !json.isEmpty()) {
						return (List<Fill>) new Moshi.Builder().build().adapter(Types.newParameterizedType(List.class, Fill.class)).fromJson(json);
					}
				}

				response.close();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
