Setup Backend + Fronend
1. Create and connect to AWS EC2 instance(s)
2. Install Java: 'sudo yum install java'
3. Update Backend url in class Workerhandler, function setListener
4. build and copy backend.jar to instance, gradle tasks: clean build
5. start backend 'java -jar openISBTBackend-1.0-SNAPSHOT.jar'; runs then on port 8080
6. Adjust Backend url in class Backend (module frontend)
7. (optional) get OAuth token and replace in api spec if Flask service should be benchmarked
8. start Frontend, gradle tasks: clean build run


Setup Worker(s)

1. Create and connect to AWS EC2 instance(s)
2. Install Java: 'sudo yum install java'
3. build and copy worker.jar to instance, gradle tasks: clean build
4. Start Worker(s) on port 9000: 'java -jar openISBTWorker-1.0-SNAPSHOT.jar 9000'


Setup Petstore
1. Install docker: 'sudo yum install docker'
2. Start Petstore: 'sudo docker run -d -e OPENAPI_BASE_PATH=/v3 -e DISABLE_OAUTH=1 -p 80:8080 openapitools/openapi-petstore'

Shutdown petstore:
1. List running containers: 'sudo docker ps'
2. Copy ID
3. Stop Container: 'sudo docker stop 7df79d676c41'
