ls
cd handout
ls
cd exercise
cd exercise1
ls
clear
ls
cd handout
ls
cd exercise1
ls
vi exploit1a.py
sudo vi exploit1a.py
ls
export USER=student
export HOST=isl-desktop6.inf.ethz.ch
export PORT=2236
sudo sshfs -o allow_other,IdentityFile=~/.ssh/isl_id_ed25519 -p $PORT $USER@$HOST:/home/student/ /mnt/islremotefs/
CLEAAR
clear
ls
cd handout
ls
cd exercise1
ls
vim exploit1a.py
sudo vim exploit1a.py
ls
cd handout
ls
cd exercise 1
cd exercise1
ls
clear
ls
vi exploit1a.py
ls
cd ../../
ls
export USER=student
export HOST=<isl-desktop6.inf.ethz.ch>
export HOST=isl-desktop6.inf.ethz.ch
export PORT=2236
ssh-keygen -t ed25519 -f ~/.ssh/isl_id_ed25519
cat << EOF >> ~/.ssh/config
AddKeysToAgent yes
ServerAliveInterval 5
Host isl-env
User $USER
HostName $HOST
Port $PORT
IdentityFile ~/.ssh/isl_id_ed25519
EOF

eval $(ssh-agent)
ssh-add ~/.ssh/isl_id_ed25519
cat ~/.ssh/isl_id_ed25519.pub | ssh isl-env "mkdir -p ~/.ssh && cat >>
~/.ssh/authorized_keys"
cat ~/.ssh/isl_id_ed25519.pub | ssh isl-env "mkdir -p ~/.ssh && cat >>
~/.ssh/authorized_keys"
cat ~/.ssh/isl_id_ed25519.pub | ssh isl-env "mkdir -p ~/.ssh && cat >>
~/.ssh/authorized_keys"
cat ~/.ssh/isl_id_ed25519.pub | ssh isl-env
cat ~/.ssh/isl_id_ed25519.pub | ssh isl-env "mkdir -p ~/.ssh && cat >>
~/.ssh/authorized_keys"
cat ~/.ssh/isl_id_ed25519.pub | ssh isl-env
clear
cat ~/.ssh/isl_id_ed25519.pub | ssh isl-env
cat ~/.ssh/isl_id_ed25519.pub | ssh isl-env "mkdir -p ~/.ssh && cat >>


~/.ssh/authorized_keys"
cat ~/.ssh/isl_id_ed25519.pub | ssh isl-env "mkdir -p ~/.ssh && cat >>
~/.ssh/authorized_keys"
cat ~/.ssh/isl_id_ed25519.pub | ssh isl-env "mkdir -p ~/.ssh && cat >>~/.ssh/authorized_keys"
clear
ssh isl-env
ssh isl-evn
ls
sudo apt-get update
clear
ls
clear
