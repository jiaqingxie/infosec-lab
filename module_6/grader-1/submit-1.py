import os 
import subprocess
import time 

def kill_all():
    os.system("pkill -9 node")
    os.system("pkill -9 gdb")
    os.system("pkill -f sp_server.py")
    time.sleep(2)
    return 
    
def manager_peripheral():
    os.system("cd /home/isl/t1  && /home/isl/t1/run_manager.sh ") 
    os.system("cd /home/isl/t1  && /home/isl/t1/run_peripheral.sh ")
    return 
    
def run(gdb,cmd):
    string = (cmd + "\n").encode()
    time.sleep(1)
    gdb.stdin.write(string)
    gdb.stdin.flush() 

def _1_1(): 
    gdb = subprocess.Popen(['gdb', '--args', 'python3', '/home/isl/t1/sp_server.py'], stdin=subprocess.PIPE) 

    time.sleep(1) 
    run(gdb,"add-symbol-file /home/isl/.local/lib/stringparser_core.so")

    run(gdb,"break gcm_crypt_and_tag")
    run(gdb,"r")
    time.sleep(1)
    os.system("node --no-warnings /home/isl/t1/remote_party  | tee /home/isl/t1/remote_party.log &")
    run(gdb,"break gcm_crypt_and_tag")
    run(gdb,"c")
    run(gdb,'set {char[40]} input = "<mes><action type=\\"key-update\\"/></mes>" ')
    run(gdb,"c 100")
    time.sleep(2)

    gdb.terminate()
    os.system("pkill -9 gdb")
    
def _1_2():
    gdb = subprocess.Popen(['gdb', '--args', 'python3', '/home/isl/t1/sp_server.py'], stdin=subprocess.PIPE) 

    time.sleep(1) 
    run(gdb,"add-symbol-file /home/isl/.local/lib/stringparser_core.so")

    run(gdb,"break gcm_crypt_and_tag")
    run(gdb,"r")
    time.sleep(1)
    os.system("node --no-warnings /home/isl/t1/remote_party  | tee /home/isl/t1/remote_party.log &")
    run(gdb,"break gcm_crypt_and_tag")
    run(gdb,"c")
    run(gdb,'set {char[40]} input = "redeemToken, token" ')
    run(gdb,"c 100")
    time.sleep(2)

    gdb.terminate()
    os.system("pkill -9 gdb")

if __name__ == "__main__":

    kill_all()
    manager_peripheral()
    _1_1()
    _1_2()

    kill_all()
    #os.system("cd /home/isl/t1 && /home/isl/t1/run.sh")