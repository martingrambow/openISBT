#Install Docker
sudo yum install docker -y

#Stop Running Flask
Container="$(sudo docker container ls | grep flask | cut -f 1 -d " ")"
sudo docker container stop $Container

#Stop docker service
sudo service docker stop

#Start Docker service
sudo service docker start

#Start Flask
sudo docker run -it --rm --publish 5000:5000 frolvlad/flask-restplus-server-example

# Get OAuth tocken: Call http://host:5000/auth/oauth2/token?grant_type=password&client_id=documentation&username=root&password=q
# Copy access_token (looks like LzaqK9qKEypzUsu85IY5umkpmJZotB)
# Replace the token in api spec (5 times)