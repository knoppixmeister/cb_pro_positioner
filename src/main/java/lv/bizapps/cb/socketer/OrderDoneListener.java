package lv.bizapps.cb.socketer;

public interface OrderDoneListener {
	public void onOrderDone(final Match orderData, final String rawData);
}
