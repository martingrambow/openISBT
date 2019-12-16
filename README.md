# openISBT
A Benchmark tool to test your REST service based on its openAPI3.0 interface description.

## Overview
[Here](http://ec2-34-245-184-88.eu-west-1.compute.amazonaws.com:9090/) is an example instance of openISBT for evaluation. Please, use it to test and evaluate the compatibility with your REST services only; do not benchmark public APIs or similar, the example instance is limited to 100 pattern requests per day.

System setup, at least two instances (one for the SUT and another for openISBT). 
If your benchmark client (the openISBT instance) becomes a bottleneck, 
you're benchmarking the client and should start at least one more instance with some more workers.

<img src="doc/overview.png" alt="Overview" width="600"/>

----

## Setup

### Prerequisites
- Git
- Java 1.8
- Gradle

### SUT
1. Setup your REST service and note the url.
2. Find or generate the openAPI3.0 description file.

### Backend (details in the backend folder)
0. Clone this repository: `git clone https://github.com/martingrambow/openISBT.git`
1. Go to openISBTBackend folder: `cd openISBT/openISBTBackend/`
2. Build/Compile backend: `gradle clean build jar` (jar is in /build/libs/openISBTBackend-1.0-SNAPSHOT.jar)
3. Start backend:
   * `java -jar build/libs/openISBTBackend-1.0-SNAPSHOT.jar` 
   * start in background and write output into file: `java -jar build/libs/openISBTBackend-1.0-SNAPSHOT.jar > backend.log &`

Per default, the backend listens at port 8080

### Frontend (details in frontend folder)
0. Clone this repository: `git clone https://github.com/martingrambow/openISBT.git`
1. Go to openISBT frontend folder: `cd openISBT/openISBTFrontend/` 
2. Build and run frontend: `gradle clean build run`

Per default, the frontend tries to connect to the backend at localhost:8080 and listens at port 9090

### Worker (details in worker folder)
1. Clone this repository: `git clone https://github.com/martingrambow/openISBT.git`
2. Go to openISBTWorker folder: `cd openISBT/openISBTWorker/`
3. Build worker jar: `gradle clean build jar` (jar is in /build/libs/openISBTWorker-1.0-SNAPSHOT.jar)
4. Start worker and define port as first parameter:
   * start at port 8000: `java -jar build/libs/openISBTWorker-1.0-SNAPSHOT.jar 8000` 
   * start in background and write output into file: `java -jar build/libs/openISBTWorker-1.0-SNAPSHOT.jar 8000 > worker1.log &`

----

## Benchmark 
(with interactive browser GUI)

### Paste OpenAPI 3.0 specification file

### Define Workload

### Adjust Matching

### Generate Workload

### Define Workers

### Run Benchmark

### Get Results

## Batch Benchmark
(via terminal)
-> Not yet implemented
