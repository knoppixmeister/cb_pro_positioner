package lv.bizapps.cb.socketer;

public interface TradeListener {
	void onNewTrade(final Match trade, final String pair, final String rawData);
}
