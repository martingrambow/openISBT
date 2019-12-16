#Setup EC2 instance

#Create/Increase swapfile (t2.micro)
sudo dd if=/dev/zero of=/swap_file bs=500M count=1
sudo chmod 600 /swap_file
sudo mkswap /swap_file
echo "/swap_file  swap  swap  defaults  0 0" | sudo tee -a /etc/fstab
sudo swapon /swap_file

#install git
sudo yum install git -y

#Install Java
sudo yum install java -y

# Install gradle
wget -N https://services.gradle.org/distributions/gradle-4.10.3-bin.zip
sudo mkdir /opt/gradle
sudo unzip -d /opt/gradle gradle-4.10.3-bin.zip
export PATH=$PATH:/opt/gradle/gradle-4.10.3/bin