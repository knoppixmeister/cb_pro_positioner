Coinbase Pro Positions Manager

Helps mananage position's buy/sell. In other words it allows (at least idea is) position entry, take profits (TP) and stop loses (SL).
This is free version (replacement) of such services like 3commas.
Created "AS IS".
Use on your own responsibility.

Inspired by https://github.com/jsappme/node-binance-trader - positions manager built w/ NodeJs 

HOW TO RUN:

- first compile & build application jar:

  mvn clean package

- run app:

  java -jar target/app.jar

By default it runs on 8083 port

run w/ changed port number (e.g. on 8080):

java -jar -Dserver.port=8080 target/app.jar

-----------------------------------------------------------------------------------------------------------------------------------------

Used libraries:
- OkHttp - https://github.com/square/okhttp/
- Moshi - https://github.com/square/moshi
- JodaTime - https://github.com/JodaOrg/joda-time
