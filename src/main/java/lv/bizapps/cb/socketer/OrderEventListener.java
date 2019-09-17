package lv.bizapps.cb.socketer;

public interface OrderEventListener {
	public void onOrderEvent(final String event, final Trade orderData, final String rawData);
}
