package lv.bizapps.cb.socketer;

public interface OrderEventListener {
	public void onOrderEvent(final String event, final Match orderData, final String rawData);
}
