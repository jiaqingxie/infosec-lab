#!/bin/bash
#$1 : number of test cases

PATH_TO_VM_IMAGE=isl-lab-2022.ova
USERNAME=isl
PW=isl
SCP_PORT=2222
SSH_PORT=2222
TASK_FOLDER="/home/isl/t2_2"
OP_FOLDER="/home/isl/t2_2/output"
SAMPLES_FOLDER="/home/isl/t2_2/samples"
TIME=30
VM_NAME=isl-lab-2022-grade
FILE_NAME=submit_2_2.py

rm -f oput_*

if ! VBoxManage list vms | grep $VM_NAME; then  
    VBoxManage import $PATH_TO_VM_IMAGE --vsys 0 --vmname $VM_NAME
else 
    echo "[GRADER] VM by name $VM_NAME already exists. Not importing new VM. To import a fresh VM delete the existing VM first"
fi

if ! VBoxManage list runningvms | grep $VM_NAME; then 
    VBoxManage startvm "$VM_NAME" --type headless
else 
    echo "[GRADER] VM by name $VM_NAME is already running. Not restarting it."
fi


sleep 5
sshpass -p "$PW" scp -P $SCP_PORT -o StrictHostKeyChecking=no $FILE_NAME $USERNAME@127.0.0.1:$TASK_FOLDER
ret=$?
if [ $ret -ne 0 ]; then
	echo "[GRADER] Error copying solution script. Please check logs"
	exit 1
fi

echo '[GRADER] copied solution solution python file to VM'
sshpass -p "$PW" ssh -p $SSH_PORT -o StrictHostKeyChecking=no $USERNAME@127.0.0.1 rm -rf $OP_FOLDER
sshpass -p "$PW" ssh -p $SSH_PORT -o StrictHostKeyChecking=no $USERNAME@127.0.0.1 mkdir $OP_FOLDER
rm -f executions.txt
touch executions.txt
COUNT=0
STR="success"
echo "testcase","no_executions","status" >> executions.txt

NO_TESTS=5
if [ ! -z  "$1" ]; then
    NO_TESTS=$1
fi

for (( i=1;i<=$NO_TESTS;i++ ))
do
    sshpass -p "$PW" ssh -p $SSH_PORT -o StrictHostKeyChecking=no $USERNAME@127.0.0.1 rm -f $TASK_FOLDER/password.txt
    sshpass -p "$PW" scp -P $SCP_PORT -o StrictHostKeyChecking=no  ./samples/$i/password.txt $USERNAME@127.0.0.1:$TASK_FOLDER
    sshpass -p "$PW" ssh -p $SSH_PORT -o StrictHostKeyChecking=no $USERNAME@127.0.0.1 timeout $TIME python3 $TASK_FOLDER/$FILE_NAME $i
    COUNT=1
    OP=$OP_FOLDER/oput_$i
    if ! sshpass -p "$PW" scp -P $SCP_PORT -o StrictHostKeyChecking=no  $USERNAME@127.0.0.1:$OP ./; then
        echo "[GRADER] Testcase $i : Output file not created on first execution: running the script again "
        sshpass -p "$PW" ssh -p $SSH_PORT -o StrictHostKeyChecking=no $USERNAME@127.0.0.1 timeout $TIME python3 $TASK_FOLDER/$FILE_NAME $i
        COUNT=2
        if ! sshpass -p "$PW" scp -P $SCP_PORT -o StrictHostKeyChecking=no  $USERNAME@127.0.0.1:$OP ./; then
            echo "[GRADER] Testcase $i: Output file not created on second execution: running the script again "
            sshpass -p "$PW" ssh -p $SSH_PORT -o StrictHostKeyChecking=no $USERNAME@127.0.0.1 timeout $TIME python3 $TASK_FOLDER/$FILE_NAME $i
            COUNT=3
            if ! sshpass -p "$PW" scp -P $SCP_PORT -o StrictHostKeyChecking=no  $USERNAME@127.0.0.1:$OP ./; then
                echo "[GRADER] Testcase $i : Output file not created on third execution: stopping now "
                echo "[GRADER] Failed for: " $i
                STR="failed"
            fi
        fi
    fi   
    echo $i,$COUNT,$STR >> executions.txt
done




