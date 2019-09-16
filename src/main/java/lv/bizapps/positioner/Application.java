package lv.bizapps.positioner;

import java.util.Scanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lv.bizapps.cb.socketer.CBSocketer;

@SpringBootApplication
public class Application {
	public static final CBSocketer CB_SOCKETER = new CBSocketer();

	private static final Scanner SCANNER = new Scanner(System.in);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);

		CB_SOCKETER.connect();

		CB_SOCKETER.addPair("BTC-EUR");

		String cmd;
		try {
			while(true) {
				System.out.print(">_: ");
				cmd = SCANNER.nextLine();

				if(cmd != null && !cmd.equals("")) {
					cmd = cmd.trim();

					switch(cmd) {
					}
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
