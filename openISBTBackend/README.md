###Backend
0. Clone this repository: `git clone https://github.com/martingrambow/openISBT.git`
1. Go to openISBTBackend folder: `cd openISBT/openISBTBackend/`
2. Build/Compile backend: `gradle clean build jar` (jar is in /build/libs/openISBTBackend-1.0-SNAPSHOT.jar)
3. Start backend:
   * `java -jar build/libs/openISBTBackend-1.0-SNAPSHOT.jar` 
   * start in background and write output into file: `java -jar build/libs/openISBTBackend-1.0-SNAPSHOT.jar > backend.log &`

Per default, the backend listens at port 8080
