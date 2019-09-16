package lv.bizapps.cb.socketer;

public interface OrderReceivedListener {
	public void onOrderReceived(final Match orderData, final String rawData);
}
