# Grading Task 1
# We Use a VM that uses a different Master Key in the Peripheral to produce the flags field
# Therefore: check if the output is correct

import hashlib
import subprocess
import sys


def calc_flag_t1(userID, mode):
    return "31970c12162608b34e38713bee3b804861f083df"

def calc_flag_t2(userID, mode):
    return "3e8f497cc43083efd0e3416bde449c041d0bb805"


def grading_t1(userID):
    flag_1_orig = calc_flag_t1(userID,0)
    flag_2_orig = calc_flag_t2(userID,0)
    print("FLAG 1- EXPECTED: " + flag_1_orig)
    print("FLAG 2- EXPECTED: " + flag_2_orig)
    
    fileoutput = ""
    status = ""

    status = "Grading Task 1; Mat-Nr: " + str(userID)
    fileoutput = fileoutput + status + "\n"
    print(status)

    #Testing for first Flag
    try:
        f = open("./flag1-1", "r")
        flag = f.readline()
        print("FLAG 1 READ: "+ flag)
        if flag_1_orig in flag:
            status = "T1_1: Correct Result - 20P"
            fileoutput = fileoutput + status + "\n"
            print(status)
        else:
            status = "T1_1: Invalid Flag - 0P"
            fileoutput = fileoutput + status + "\n"
            print(status)
        f.close()
    except IOError:    #This means that the file does not exist (or some other IOError)
        status = "T1_1: Error opening Flag File - 0P"
        fileoutput = fileoutput + status + "\n"
        print(status)
    except:
        status = "T1_1: Error during Flag validation - 0P"
        fileoutput = fileoutput + status + "\n"
        print(status)

    #Testing for second flag
    try:
        f = open("./flag1-2", "r")
        flag = f.readline()
        print("FLAG 2 READ: " + flag)
        if flag_2_orig in flag:
            status = "T1_2: Correct Result - 20P"
            fileoutput = fileoutput + status + "\n"
            print(status)
        else:
            status = "T1_2: Invalid Flag - 0P"
            fileoutput = fileoutput + status + "\n"
            print(status)
        f.close()
    except IOError:    #This means that the file does not exist (or some other IOError)
        status = "T1_2: Error opening Flag File - 0P"
        fileoutput = fileoutput + status + "\n"
        print(status)
    except:
        status = "T1_2: Error during Flag validation - 0P"
        fileoutput = fileoutput + status + "\n"
        print(status)
        
    
    try:
        gradingOutputLog = open("./grade-" + str(userID), 'w')
        gradingOutputLog.write(fileoutput)
        gradingOutputLog.close()
        print("Wrote grade file to ./grade-" + str(userID))
    except IOError:
        print("ERROR while writing gradeFile; Task is NOT graded successfully!")

grading_t1("11-111-111")