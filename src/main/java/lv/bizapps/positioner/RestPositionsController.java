package lv.bizapps.positioner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.squareup.moshi.*;
import lv.bizapps.cb.rest.*;
import lv.bizapps.cb.rest.CBRest.*;
import lv.bizapps.position.Position;
import lv.bizapps.positioner.utils.Utils;

@RestController
public class RestPositionsController {
	private JsonAdapter<RequestPosition> rpJsonAdapter = new Moshi.Builder().build().adapter(RequestPosition.class);

	//private final CBRest cbr = new CBRest(API.API_KEY, API.API_PASSPHRASE, API.API_SECRET);

	@GetMapping(value = "/positions")
	public ResponseEntity<String> positions() {
		return new ResponseEntity<String>(
			new Moshi.Builder().add(new JodaDateTimeAdapter())
								.build()
								.adapter(Types.newParameterizedType(List.class, Position.class))
								.toJson(Application.POSITIONS),
			HttpStatus.OK
		);
	}

	@GetMapping(value = "/positions/{id}")
	public ResponseEntity<String> positions(@PathVariable(name="id", required=true) String id) {
		if(id == null || id.isEmpty() || !Utils.isUUID(id)) {
			return new ResponseEntity<String>("{\"message\":\"Invalid position id\"}", HttpStatus.BAD_REQUEST);
		}

		for(Position p : Application.POSITIONS) {
			if(p.uuid.equalsIgnoreCase(id)) {
				return new ResponseEntity<String>(new Moshi.Builder().add(new JodaDateTimeAdapter()).build().adapter(Position.class).toJson(p), HttpStatus.NOT_FOUND);
			}
		}

		return new ResponseEntity<String>("{\"message\":\"position not found\"}", HttpStatus.NOT_FOUND);
	}

	@PostMapping(value="/positions")
	// body parameter marked as not required here. All checks will be processed below
	public ResponseEntity<String> createPosition(@RequestBody(required=false) String body) {
		final List<String> ORDER_TYPES = Arrays.asList("limit");// "market"

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

				return new ResponseEntity<String>("{\"message\":\"Invalid buy_order_type "+rp.buyOrderType+"\"}", HttpStatus.BAD_REQUEST);
			}
			if(rp.buyAmount == null || rp.buyAmount.isEmpty()) {
				return new ResponseEntity<String>("{\"message\":\"buy_amount must be a number\"}", HttpStatus.BAD_REQUEST);
			}
			if(Double.parseDouble(rp.buyAmount) < 0.001) {
				return new ResponseEntity<String>("{\"message\":\"buy_amount is too small. Minimum size is 0.00100000\"}", HttpStatus.BAD_REQUEST);
			}
			if(rp.buyPrice == null || rp.buyPrice.isEmpty()) {
				return new ResponseEntity<String>("{\"message\":\"buy_price must be a number\"}", HttpStatus.BAD_REQUEST);
			}
			if(Double.parseDouble(rp.buyPrice) >= Application.CURRENT_PRICE && !rp.postOnlyOrders) {
				return new ResponseEntity<String>("{\"message\":\"Non \"post-only\" buy order price could not be greater than current price\"}", HttpStatus.BAD_REQUEST);
			}
			if(rp.sellPrice != null && !rp.sellPrice.isEmpty()) {
				/*
					400|Bad Request
					{"message":"price must be a number"}
				 */

				try {
					Double.parseDouble(rp.sellPrice);
				}
				catch(Exception e) {
					return new ResponseEntity<String>("{\"message\":\"sell price must be a number\"}", HttpStatus.BAD_REQUEST);
				}
			}
			if(!rp.productId.equals("BTC-EUR")) {
				return new ResponseEntity<String>("{\"message\":\"Invalid product_id\"}", HttpStatus.BAD_REQUEST);
			}

			System.out.println(	new Moshi.Builder().build().adapter(RequestPosition.class).toJson(rp)	);

			CBRest cbr1 = new CBRest(rp.apiKey, rp.apiPassphrase, rp.apiSecret);

			Position p = new Position(
				Double.parseDouble(rp.buyAmount),
				Double.parseDouble(rp.buyPrice),
				(rp.sellPrice != null && !rp.sellPrice.isEmpty()) ? Double.valueOf(rp.sellPrice) : null
			);
			p.description = rp.description;
			Application.POSITIONS.add(p);

			new Thread(new Runnable() {
				@Override
				public void run() {
					// submit data to Coinbase TME

					int pidx = Application.POSITIONS.indexOf(p);
					if(pidx == -1) return;

					Application.POSITIONS.get(pidx).status = "S";

					//rp.buyOrderType.equals("limit") ?
					final Order o = cbr1.openOrder(
						OrderType.LIMIT,
						OrderSide.BUY,
						Double.parseDouble(rp.buyPrice),
						Double.parseDouble(rp.buyAmount),
						Application.POSITIONS.get(pidx).buyOrderClientOid
					);
					if(o != null) {
						p.buyOrder = o;

						if(o.status.equals("rejected")) {
							// start set buy order by set_step lower than inital buy_price until will be set

							Application.POSITIONS.get(pidx).status = "BE";
						}
					}
					else Application.POSITIONS.get(pidx).status = "BE";
				}
			}).start();

			return new ResponseEntity<String>("{\"position_id\":\""+p.uuid+"\"}", HttpStatus.OK);
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
