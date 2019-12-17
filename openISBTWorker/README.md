### Worker (details in worker folder)
1. Clone this repository: `git clone https://github.com/martingrambow/openISBT.git`
2. Go to openISBTWorker folder: `cd openISBT/openISBTWorker/`
3. Build worker jar: `gradle clean build jar` (jar is in /build/libs/openISBTWorker-1.0-SNAPSHOT.jar)
4. Start worker and define port as first parameter:
   * start at port 8000: `java -jar build/libs/openISBTWorker-1.0-SNAPSHOT.jar 8000` 
   * start in background and write output into file: `java -jar build/libs/openISBTWorker-1.0-SNAPSHOT.jar 8000 > worker1.log &`
