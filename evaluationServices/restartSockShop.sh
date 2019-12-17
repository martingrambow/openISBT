#Installs
sudo yum install git -y
sudo yum install docker -y
sudo curl -L "https://github.com/docker/compose/releases/download/1.24.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose


#Stop Running Instance
sudo /usr/local/bin/docker-compose -f user/docker-compose.yml down

#Restart Docker
sudo service docker stop
sudo service docker start

#Clone Repo & start
rm -rf user
git clone https://github.com/microservices-demo/user.git

cd user
sudo /usr/local/bin/docker-compose up -d user-db
sudo /usr/local/bin/docker-compose up -d user