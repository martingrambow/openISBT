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

Setup SockShop
1. install git: 'sudo yum install git'
2. install docker: 'sudo yum install docker'
3. start docker: 'sudo service docker start'
4. install docker compose: 'sudo curl -L "https://github.com/docker/compose/releases/download/1.24.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose'
5. make it executable: 'sudo chmod +x /usr/local/bin/docker-compose'
6. clone repo: 'git clone https://github.com/microservices-demo/user.git'
7. enter directory: 'cd user'
8. start user-db: 'sudo docker-compose up -d user-db'
9. start service: 'sudo docker-compose up -d user'

Shutdown SockShop
1. Stop Docker Compose: 'sudo docker-compose down'

Setup FlaskDemo
1. install docker: 'sudo yum install docker'
2. start docker: 'sudo service docker start'
3. Run Service: 'sudo docker run -it --rm --publish 5000:5000 frolvlad/flask-restplus-server-example'
4. Get OAuth tocken: Call http://ec2-34-248-221-160.eu-west-1.compute.amazonaws.com:5000/auth/oauth2/token?grant_type=password&client_id=documentation&username=root&password=q
5. Copy access_token (looks like LzaqK9qKEypzUsu85IY5umkpmJZotB)
6. Replace the token in api spec (5 times)


Shutdown:
1. List running containers: 'sudo docker ps'
2. Copy ID
3. Stop Container: 'sudo docker stop 7df79d676c41'


