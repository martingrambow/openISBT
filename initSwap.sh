#Create swapfile
sudo dd if=/dev/zero of=/swap_file bs=500M count=1
sudo chmod 600 /swap_file
sudo mkswap /swap_file
echo "/swap_file  swap  swap  defaults  0 0" | sudo tee -a /etc/fstab
sudo swapon /swap_file