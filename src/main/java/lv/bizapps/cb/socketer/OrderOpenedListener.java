package lv.bizapps.cb.socketer;

public interface OrderOpenedListener {
	public void onOrderOpened(final Match orderData, final String rawData);
}
