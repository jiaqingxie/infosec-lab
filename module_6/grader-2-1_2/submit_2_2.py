import sys
import os
import shutil

def out(out_id, hacked_pwd):
	os.makedirs(os.path.dirname(f"/home/isl/t2_2/output/oput_{out_id}"), exist_ok=True)
	oput = open(f"/home/isl/t2_2/output/oput_{out_id}", "w") 
	oput.write(hacked_pwd)
	oput.close()

def trace_ch(ch, guess):
	os.chdir("/home/isl/pin-3.11-97998-g7ecce2dac-gcc-linux-master/source/tools/SGXTrace")
	trace = f"/home/isl/t2_2/traces/trace_{ch}"
	os.makedirs(os.path.dirname(trace), exist_ok=True)
	os.system(f"../../../pin -t ./obj-intel64/SGXTrace.so -o {trace} -trace 1 -- ~/t2_2/password_checker_2 {guess}")

def ch_pos(ch, pwd_dict):
	count = 0
	with open(f"/home/isl/t2_2/traces/trace_{ch}") as f:
		for line in f:
			if "0x401d97" in line:
				count += 1
			if "0x401d83" in line:
				pwd_dict[count] = ch

if __name__ == "__main__":

	ALPHABETS = [chr(letter) for letter in range(97, 123)]
	pwd_dict = {}
	for ch in ALPHABETS:
		trace_ch(ch, ch * 31)
		ch_pos(ch, pwd_dict)

	hacked_pwd= ""
	for idx in range(1, max(pwd_dict.keys()) + 1):
		hacked_pwd += pwd_dict[idx]
	hacked_pwd += ",complete"
	out(sys.argv[1], hacked_pwd)
	shutil.rmtree("/home/isl/t2_2/traces")
