package lv.bizapps.positioner.utils;

import java.math.*;
import org.joda.time.*;

public class Utils {
	public static String isoDateTime() {
		String crTm = new DateTime().toString();

		return crTm.substring(0, crTm.lastIndexOf("+"))+"Z";		
	}

	public static double round(double value, int places) {
		if(places < 0) throw new IllegalArgumentException();

		return (new BigDecimal(value)).setScale(places, RoundingMode.HALF_UP).doubleValue();
	}
}
