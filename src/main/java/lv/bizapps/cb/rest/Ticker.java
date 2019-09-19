package lv.bizapps.cb.rest;

import com.squareup.moshi.Moshi;

public class Ticker {
	/*
		"trade_id": 4729088,
		"price": "333.99",
		"size": "0.193",
		"bid": "333.98",
		"ask": "333.99",
		"volume": "5957.11914015",
		"time": "2015-11-14T20:46:03.511254Z"
	*/

	public String price, size, time;

	public String toString() {
		return new Moshi.Builder().build().adapter(Ticker.class).toJson(this);
	}
}
