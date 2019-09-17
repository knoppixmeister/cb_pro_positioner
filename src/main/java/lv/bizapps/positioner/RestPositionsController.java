package lv.bizapps.positioner;

import java.util.List;

import javax.websocket.server.PathParam;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import lv.bizapps.position.Position;

@RestController
public class RestPositionsController {
	@GetMapping(value = "/positions")
	public List<Position> positions() {

		return null;
	}

	@GetMapping(value = "/positions/{id}")
	public ResponseEntity<String> positions(@PathVariable(name = "id") String id) {

		return null;
	}

	@PostMapping(value = "/positions")
	public ResponseEntity<String> createPosition() {
		

		return new ResponseEntity<String>("", HttpStatus.OK);
	}

	@DeleteMapping(value = "/positions")
	public String deletePositions() {

		return "";
	}

	@DeleteMapping(value = "/positions/{id}")
	public String deletePosition(@PathVariable(name = "id") String id) {
		
		
		return "";
	}
}
