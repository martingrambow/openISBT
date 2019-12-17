#Install Docker
sudo yum install docker -y

#Stop Running Petstore
Container="$(sudo docker container ls | grep petstore | cut -f 1 -d " ")"
sudo docker container stop $Container

#Stop docker service
sudo service docker stop

#Start Docker service
sudo service docker start

#Start Petstore
sudo docker run -d -e OPENAPI_BASE_PATH=/v3 -e DISABLE_OAUTH=1 -p 80:9010 openapitools/openapi-petstore
