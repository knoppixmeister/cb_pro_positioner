package lv.bizapps.cb.rest;

import com.squareup.moshi.Moshi;

public class Product {
	/*
		"id": "BTC-USD",
	    "base_currency": "BTC",
	    "quote_currency": "USD",
	    "base_min_size": "0.001",
	    "base_max_size": "10000.00",
	    "quote_increment": "0.01"
	 */

	public String id, base_min_size, base_currency, quote_increment, quote_currency, base_max_size;

	public String toString() {
		return new Moshi.Builder().build().adapter(Product.class).toJson(this);
	}
}
