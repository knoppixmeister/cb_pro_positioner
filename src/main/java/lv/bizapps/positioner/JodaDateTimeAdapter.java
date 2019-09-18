package lv.bizapps.positioner;

import org.joda.time.DateTime;
import com.squareup.moshi.*;

public class JodaDateTimeAdapter {
	@FromJson
	public DateTime jsonToJodaDateTime(String s) {
		if(s == null || s.isEmpty()) return null;
		else return new DateTime(); 
	}

	@ToJson
	public String jodaDateTimeToJson(DateTime dt) {
		String tm; 

		if(dt == null) tm = "";
		else {
			tm = dt.toString();
			tm = tm.substring(0, tm.lastIndexOf("+"));
		}

		return tm;
	}
}
