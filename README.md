Coinbase Pro Position Manage Helper

Helps mananage position's buy/sell. In other words it allows (at least idea is) position entry, take profits (TP) and stop loses (SL).
This is free version (replacement) of such services like 3commas.
Created "AS IS".
Use on your own responsibility.

Inspired by some ... (NodeJs positions manager - sorry for missed link on guthub project) 

HOW TO RUN:

- first compile & build application jar:

  mvn clean package

- run app:

  java -jar target/app.jar

By default it runs on 8083 port

run w/ changed port number (e.g. on 8080):

java -jar -Dserver.port=8080 target/app.jar


