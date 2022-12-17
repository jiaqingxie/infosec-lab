PATH_TO_VM_IMAGE=isl-lab-2022.ova
USERNAME=isl
PW=isl
SCP_PORT=2222
SSH_PORT=2222
SCRIPT_FOLDER=/home/isl/scripts
DATA_FOLDER=/home/isl/t1/
VM_NAME=isl-lab-2022-grade
FILE_NAME=submit-1.py

if ! VBoxManage list vms | grep $VM_NAME; then  
   VBoxManage import $PATH_TO_VM_IMAGE --vsys 0 --vmname $VM_NAME
else 
   echo "[GRADER] VM by name $VM_NAME already exists. Not importing new VM. To import a fresh VM delete the existing VM first"
fi

if ! VBoxManage list runningvms | grep  $VM_NAME; then 
    VBoxManage startvm "$VM_NAME" --type headless
else 
    echo "[GRADER] VM by name $VM_NAME is already running. Not restarting it."
fi

sleep 5
sshpass -p "$PW" scp -P $SCP_PORT -o StrictHostKeyChecking=no $FILE_NAME $USERNAME@localhost:$SCRIPT_FOLDER/

ret=$?
if [ $ret -ne 0 ]; then
	echo "[GRADER] Error copying solution script. Please check logs"
	exit 1
fi

echo '[GRADER] copied solution script to VM'

sshpass -p "$PW" ssh -p $SSH_PORT -o StrictHostKeyChecking=no $USERNAME@localhost "bash -c 'rm -f $SCRIPT_FOLDER/flag1-*'"
echo '[GRADER] Removed any existing flag files'

#ssh -p $SSH_PORT -i ./id_rsa -o StrictHostKeyChecking=no $USERNAME@localhost "python3 $SCRIPT_FOLDER/$FILE_NAME &"
sshpass -p "$PW" ssh -p $SSH_PORT -o StrictHostKeyChecking=no $USERNAME@localhost "bash -c 'nohup python3 $SCRIPT_FOLDER/$FILE_NAME > /dev/null 2>&1 &'"
echo '[GRADER] sent command to execute solution script; waiting for 60 seconds...'
sleep 60
#sshpass -p "$PW" ssh -p $SSH_PORT -o StrictHostKeyChecking=no $USERNAME@localhost "bash -c 'nohup pkill -9 python3 > /dev/null 2>&1 &'"
sshpass -p "$PW" ssh -p $SSH_PORT -o StrictHostKeyChecking=no $USERNAME@localhost "bash -c 'nohup pkill -9 gdb > /dev/null 2>&1 &'"
echo '[GRADER] ran solution script'
sleep 3

sshpass -p "$PW" ssh -p $SSH_PORT -o StrictHostKeyChecking=no $USERNAME@localhost "bash -c 'nohup python3 $DATA_FOLDER/test_setup.py > $SCRIPT_FOLDER/testlog.otp 2>&1 &'"
echo '[GRADER] ran component healthcheck'

if ! sshpass -p "$PW" scp -P $SCP_PORT -o StrictHostKeyChecking=no $USERNAME@localhost:$SCRIPT_FOLDER/flag1-* .; then
            echo "[GRADER] No flag files generated to read"
fi
sleep 1
if ! sshpass -p "$PW" scp -P $SCP_PORT -o StrictHostKeyChecking=no $USERNAME@localhost:$SCRIPT_FOLDER/testlog.otp .; then
            echo "[GRADER] No healthcheck files generated to read"
fi
