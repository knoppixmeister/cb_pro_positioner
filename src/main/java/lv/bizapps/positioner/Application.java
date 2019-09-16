package lv.bizapps.positioner;

import java.util.Scanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
	private static final Scanner SCANNER = new Scanner(System.in);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);

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
