import sys
import os

def out(id, hacked_pwd):
	os.makedirs(os.path.dirname(f"/home/isl/t2_1/output/oput_{id}"), exist_ok=True)
	output_file = open(f"/home/isl/t2_1/output/oput_{id}", "w")
	output_file.write(hacked_pwd)
	output_file.close()

if __name__ == "__main__":	
	traces = os.listdir(sys.argv[1]) # all traces file [given sample pwd]
	good = False 					 # if current pwd is correct
	hacked_pwd = {} 				 # my hacked pwd
	pwd_len = -1 					 # pwd length
	for trace in traces:
		count = 0					 # current bit
		num_bit_of_good = 0 		 # correct number of bits guessed
		shift_overflow = False            # shift will cause 'z' -> 'a' -> ... or not 
		shift = 0		             # num of bits to be shifted
		with open(sys.argv[1] + "/" + trace) as f:
			for line in f:
				if "0x401211" in line:  # correct bit
					num_bit_of_good += 1
					hacked_pwd[count] = trace[count - 1]
				elif "0x40126f" in line:
					shift_overflow = True
				elif "0x401286" in line:  # shift one bit 
					shift += 1
				elif "0x401292" in line:  # shift ended -> next bit; if no shift then just simply move to next bit
					if shift > 0:
						if not shift_overflow:
							hacked_pwd[count] = chr(ord(trace[count - 1]) + shift - 1) # shift will not cause 'z' -> 'a' -> ...
						else:
							hacked_pwd[count] = chr(((ord(trace[count - 1]) - ord('a') + shift - 1) % 26) + ord('a')) # avoid shift overflow 
					# defualt setting:	
					shift = 0	
					shift_overflow = False 	
					count += 1
				elif "0x4012a8" in line:
					good = True 			  # guessed password is true for this trace (all pwds) just for check here // could be commented
		
		if len(trace) - 3 > count: # fill _
			pwd_len = count - 1

	good = pwd_len != -1
	pwd = ""
	for index in range(1, max(hacked_pwd.keys()) + 1):
		if index in hacked_pwd: # if bit traced 
			pwd += hacked_pwd[index]
		else:
			good = False # if one bit false, then partial
			pwd += "_"

	pwd += ",complete" if good else ",partial"
	out(sys.argv[2], pwd)