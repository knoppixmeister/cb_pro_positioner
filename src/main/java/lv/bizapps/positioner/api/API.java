package lv.bizapps.positioner.api;

import java.io.*;

public final class API {
	public static String API_PASSPHRASE	=	"";
	public static String API_KEY		=	"";
	public static String API_SECRET		=	"";

	// static init
	static {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("creds.txt"));

			int idx = 1;

			String line;
			while((line = reader.readLine()) != null) {
				System.out.println(idx+") "+line);

				if(idx == 1) API.API_KEY = line;
				else if(idx == 2) API.API_PASSPHRASE = line;
				else if(idx == 3) API.API_SECRET = line;

				++idx;
			}

			System.out.println("----------------------");

			reader.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		};
	};
}
