package lv.bizapps.positioner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import lv.bizapps.cb.rest.CBRest;
import lv.bizapps.cb.rest.Order;
import lv.bizapps.cb.rest.CBRest.OrderSide;
import lv.bizapps.cb.rest.CBRest.OrderType;
import lv.bizapps.position.Position;
import lv.bizapps.positioner.api.API;

@RestController
public class RestPositionsController {
	private JsonAdapter<RequestPosition> rpJsonAdapter = new Moshi.Builder().build().adapter(RequestPosition.class);

	private final CBRest cbr = new CBRest(API.API_KEY, API.API_PASSPHRASE, API.API_SECRET);

	@GetMapping(value = "/positions")
	public List<Position> positions() {
		return Application.POSITIONS;
	}

	@GetMapping(value = "/positions/{id}")
	public ResponseEntity<String> positions(@PathVariable(name = "id") String id) {
		return new ResponseEntity<String>("", HttpStatus.OK);
	}

	@PostMapping(value = "/positions")
	public ResponseEntity<String> createPosition(@RequestBody String body) {
		// System.out.println(body);
		final List<String> ORDER_TYPES = Arrays.asList("limit", "market");

		if(body == null || body.isEmpty()) {
			return new ResponseEntity<String>("{\"message\":\"no position description\"}", HttpStatus.BAD_REQUEST);
		}

		try {
			final RequestPosition rp = rpJsonAdapter.fromJson(body);

			if(rp == null) {
				return new ResponseEntity<String>("{\"message\":\"Invalid position description\"}", HttpStatus.BAD_REQUEST);
			}
			
			if(!ORDER_TYPES.contains(rp.buyOrderType)) {
				/*
				400|Bad Request
				{"message":"Invalid order_type limIt"}
				*/

				return new ResponseEntity<String>("{\"message\":\"Invalid order type "+rp.buyOrderType+"\"}", HttpStatus.BAD_REQUEST);
			}
			if(rp.buyPrice >= Application.CURRENT_PRICE && !rp.postOnlyOrders) {
				return new ResponseEntity<String>("{\"message\":\"Non \"post-only\" buy order price could not be greater than current price\"}", HttpStatus.BAD_REQUEST);
			}

			System.out.println(	new Moshi.Builder().build().adapter(RequestPosition.class).toJson(rp)	);

			new Thread(new Runnable() {
				@Override
				public void run() {
					// submit data to Coinbase TME

					Position p = new Position(rp.buyAmount, rp.buyPrice, null);

					p.status = "S";
					Application.POSITIONS.add(p);

					Order o = cbr.openOrder(
						rp.buyOrderType.equals("limit") ? OrderType.LIMIT : OrderType.MARKET,
						OrderSide.BUY,
						rp.buyPrice,
						rp.buyAmount,
						p.buyOrderClientOid
					);
					if(o != null) {
						if(o.status.equals("rejected")) p.status = "BE";
					}
					else p.status = "BE";
				}
			}).start();

			return new ResponseEntity<String>("{\"status\":\"accepted\"}", HttpStatus.OK);
		}
		catch(IOException e) {
			e.printStackTrace();

			return new ResponseEntity<String>("{\"message\":\"Invalid position description\"}", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping(value = "/positions")
	public ResponseEntity<String> deletePositions() {
		;

		return new ResponseEntity<String>("", HttpStatus.OK);
	}

	@DeleteMapping(value = "/positions/{id}")
	public ResponseEntity<String> deletePosition(@PathVariable(name = "id") String id) {
		;

		return new ResponseEntity<String>("", HttpStatus.OK);
	}
}
