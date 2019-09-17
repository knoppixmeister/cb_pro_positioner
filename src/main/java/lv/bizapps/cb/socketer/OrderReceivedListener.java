package lv.bizapps.cb.socketer;

public interface OrderReceivedListener {
	public void onOrderReceived(final Trade orderData, final String rawData);
}
