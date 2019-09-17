package lv.bizapps.cb.socketer;

public interface OrderDoneListener {
	public void onOrderDone(final Trade orderData, final String rawData);
}
