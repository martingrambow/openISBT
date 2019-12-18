#Create swapfile
sudo dd if=/dev/zero of=/$1 bs=500M count=1
sudo chmod 600 /$1
sudo mkswap /$1
echo "/$1  swap  swap  defaults  0 0" | sudo tee -a /etc/fstab
sudo swapon /$1