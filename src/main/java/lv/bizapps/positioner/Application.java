package lv.bizapps.positioner;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lv.bizapps.cb.socketer.CBSocketer;
import lv.bizapps.cb.socketer.ConnectedListener;
import lv.bizapps.position.Position;
import lv.bizapps.positioner.utils.Log;

@SpringBootApplication
public class Application {
	public static final CBSocketer CB_SOCKETER = new CBSocketer();

	private static final Scanner SCANNER = new Scanner(System.in);

	public static final List<Position> POSITIONS = new CopyOnWriteArrayList<>();

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);

		CB_SOCKETER.connect();

		CB_SOCKETER.addPair("BTC-EUR");

		CB_SOCKETER.addConnectedListener(new ConnectedListener() {
			@Override
			public void onConnectedListener() {
				String cmd;
				try {
					while(true) {
						System.out.print(">_: ");
						cmd = SCANNER.nextLine();

						if(cmd != null && !cmd.equals("")) {
							cmd = cmd.trim();

							switch(cmd) {
								case "l":	Log.i("LIST POSITIONS ...");
											listPositions();
											break;
								case "q":	
											break;
							}
						}
					}
				}
				catch(Exception e) {
				}	
			}
		});
	}

	private static void listPositions() {
		int idx = 1;
		if(POSITIONS == null || POSITIONS.isEmpty()) {
			System.out.println("<NO POSITIONS>");
		}
		else {
			for(Position p : POSITIONS) {
				System.out.println(idx+"] "+p.buyPrice);
			}
		}
	}
}
