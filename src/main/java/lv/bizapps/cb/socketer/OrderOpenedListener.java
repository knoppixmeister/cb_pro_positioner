package lv.bizapps.cb.socketer;

public interface OrderOpenedListener {
	public void onOrderOpened(final Trade orderData, final String rawData);
}
