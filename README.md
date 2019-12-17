# openISBT
A Benchmark tool to test your REST service based on its openAPI3.0 interface description.

## Overview
[Here](http://ec2-54-171-220-114.eu-west-1.compute.amazonaws.com:9090/) is an example instance of openISBT for evaluation. Please, use it to test and evaluate the compatibility with your REST services only; do not benchmark public APIs or similar, the example instance is limited to 100 pattern requests per day.

System setup, at least two instances (one for the SUT and another for openISBT). 
If your benchmark client (the openISBT instance) becomes a bottleneck, 
you're benchmarking the client and should start at least one more instance with some more workers.

<img src="doc/overview.png" alt="Overview" width="600"/>

----

## Setup

### SUT
1. Setup your REST service and note the url.
2. Find or generate the openAPI3.0 description file.

### OpenISBT
0. Connect to (EC2) instance
1. Install git: `sudo yum install git -y`
2. Clone openISBT repository: `git clone https://github.com/martingrambow/openISBT.git`
3. cd to openISBT: `cd openISBT`
4. Make *.sh files executable: `chmod +x *.sh`
5. Create swap (required for t2.micro instances): `./initSwap.sh`
6. run setup script: `./startOpenISBT.sh`


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
