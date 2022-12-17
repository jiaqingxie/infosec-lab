#!/bin/bash
PATH_TO_VM_IMAGE=isl-lab-2022.ova
USERNAME=isl
PW=isl
SCP_PORT=2222
SSH_PORT=2222
TASK_FOLDER="/home/isl/t2_3"
TEST_FOLDER="/home/isl/t2_3/test"
VM_NAME=isl-lab-2022-grade
FILE_NAME=password_checker_3.c
rm -f forked

if ! VBoxManage list vms | grep $VM_NAME; then  
    VBoxManage import $PATH_TO_VM_IMAGE --vsys 0 --vmname $VM_NAME
else 
    echo "[GRADER] VM by name $VM_NAME already exists. Not importing new VM. To import a fresh VM delete the existsing VM first"
fi

if ! VBoxManage list runningvms | grep $VM_NAME; then 
    VBoxManage startvm "$VM_NAME" --type headless
else 
    echo "[GRADER] VM by name $VM_NAME is already running. Not restarting it."
fi


sleep 5
sshpass -p "$PW" scp -P $SCP_PORT -o StrictHostKeyChecking=no $FILE_NAME $USERNAME@127.0.0.1:$TASK_FOLDER/
ret=$?
if [ $ret -ne 0 ]; then
	echo "[GRADER] Error copying solution script. Please check logs"
	exit 1
fi
echo '[GRADER] copied solution solution c file to VM'
sshpass -p "$PW" ssh -p $SSH_PORT -o StrictHostKeyChecking=no $USERNAME@127.0.0.1 rm -f $TASK_FOLDER/a.out
sshpass -p "$PW" ssh -p $SSH_PORT -o StrictHostKeyChecking=no $USERNAME@127.0.0.1 $TASK_FOLDER/build.sh
echo "[GRADER] built binary"
sshpass -p "$PW" ssh -p $SSH_PORT -o StrictHostKeyChecking=no $USERNAME@127.0.0.1 chmod +x $TEST_FOLDER/*.sh
sshpass -p "$PW" ssh -p $SSH_PORT -o StrictHostKeyChecking=no $USERNAME@127.0.0.1 rm -rf $TEST_FOLDER/traces
sshpass -p "$PW" ssh -p $SSH_PORT -o StrictHostKeyChecking=no $USERNAME@127.0.0.1 mkdir $TEST_FOLDER/traces
sshpass -p "$PW" ssh -p $SSH_PORT -o StrictHostKeyChecking=no $USERNAME@127.0.0.1 python3 $TEST_FOLDER/run_tracer.py
echo '[GRADER] ran tracer'
if sshpass -p "$PW" scp -P $SCP_PORT -o StrictHostKeyChecking=no $USERNAME@localhost:$TEST_FOLDER/forked ./ >&/dev/null;
      then echo "[GRADER] found forked"; 
fi

if ! sshpass -p "$PW" scp -P $SCP_PORT -o StrictHostKeyChecking=no $USERNAME@localhost:$TEST_FOLDER/functionality.csv .; then 
      echo "[GRADER] functionality.csv not found"
fi
echo '[GRADER] run diff now'

sshpass -p "$PW" ssh -p $SSH_PORT -o StrictHostKeyChecking=no $USERNAME@127.0.0.1 python3 $TEST_FOLDER/diff_traces.py

if ! sshpass -p "$PW" scp -P $SCP_PORT -o StrictHostKeyChecking=no $USERNAME@localhost:$TEST_FOLDER/diff_traces.csv .; then
      echo "[GRADER] diff_traces.csv not found"
fi


