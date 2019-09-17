package lv.bizapps.positioner;

import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import lv.bizapps.position.Position;

@RestController
public class RestPositionsController {
	@GetMapping(value = "/positions")
	public List<Position> positions() {
		return Application.POSITIONS;
	}

	@GetMapping(value = "/positions/{id}")
	public ResponseEntity<String> positions(@PathVariable(name = "id") String id) {
		return new ResponseEntity<String>("", HttpStatus.OK);
	}

	@PostMapping(value = "/positions")
	public ResponseEntity<String> createPosition() {
		;

		return new ResponseEntity<String>("", HttpStatus.OK);
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
