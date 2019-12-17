#Setup EC2 instance
#0. Connect to EC2 instance
#1. Install git: yum install git -y
#2. Clone openISBT repo: git clone https://github.com/martingrambow/openISBT.git
#3. cd to openISBT: cd openISBT
#4.1 Create swap: ./initSwap.sh
#4.2 run this script: ./startOpenISBT.sh

#install git
sudo yum install git -y
#Install Java
sudo yum install java -y
# Install gradle
wget -N https://services.gradle.org/distributions/gradle-4.10.3-bin.zip
sudo mkdir /opt/gradle
sudo unzip -d /opt/gradle gradle-4.10.3-bin.zip
export PATH=$PATH:/opt/gradle/gradle-4.10.3/bin

#Stop Backend
#Stop Frontend
#Stop Worker

#Build and Start Backend
cd openISBTBackend
gradle clean build jar
java -jar build/libs/openISBTBackend-1.0-SNAPSHOT.jar > backend.log &
cd ..

#Build and run Frontend
cd openISBTFrontend
gradle clean build run
cd ..

#Build and run one Worker
cd openISBTWorker
gradle clean build jar
java -jar build/libs/openISBTWorker-1.0-SNAPSHOT.jar 8000 > worker1.log &